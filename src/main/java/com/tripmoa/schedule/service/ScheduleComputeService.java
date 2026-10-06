package com.tripmoa.schedule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.dto.ScheduleItemPlaceRequest;
import com.tripmoa.schedule.dto.ScheduleItemPlanRequest;
import com.tripmoa.schedule.dto.ScheduleResponse;
import com.tripmoa.schedule.dto.ScheduleSettingsRequest;
import com.tripmoa.schedule.event.ScheduleChangedEvent;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 일정 시각 자동 계산
 *
 * 노드의 순서·머무는 시간·고정 시각이 정해지면 각 노드의 시작 시각은 엔진(Python /schedule/recompute-day)이
 * 계산한다. 이 서비스는 그 호출을 감싼다: 그날의 설정을 모으고, 계산 결과를 노드에 반영하고, 경고를 저장한다.
 *
 * - 자동 계산이 켜진 날만 다시 계산한다(autoCompute). 켜기 전의 일정은 시각을 건드리지 않는다.
 * - 엔진 호출은 트랜잭션 밖에서 한다(느린 외부 호출 동안 DB를 붙잡지 않는다). 그 사이 노드 구성이 바뀌었으면
 *   낡은 결과라 버린다 — 바꾼 쪽이 이벤트로 다시 계산을 부르므로 결국 최신 상태로 맞춰진다.
 * - 같은 여행의 일정 생성·일차 만들기와 겹치지 않도록 여행 단위 잠금을 잡는다.
 */
@Slf4j
@Service
public class ScheduleComputeService {

    private static final String DEFAULT_START = "09:00";
    private static final String DEFAULT_END = "21:00";
    private static final String DEFAULT_TRANSPORT = "대중교통";
    private static final String DEFAULT_LUNCH = "12:00";
    private static final String DEFAULT_DINNER = "18:30";
    private static final Set<String> TRANSPORT_MODES = Set.of("택시", "대중교통", "도보");
    private static final Set<String> ANCHOR_CATEGORIES = Set.of("숙소", "출발지");

    private static final Set<String> REPLACEABLE_CATEGORIES = Set.of("관광지", "맛집", "카페", "쇼핑");
    private static final int MAX_TITLE = 100;
    private static final int MAX_DESCRIPTION = 1000;

    private static final int MIN_STAY = 5;
    private static final int MAX_STAY = 720;
    private static final int INFERRED_STAY_MIN = 10;
    private static final int INFERRED_STAY_MAX = 240;

    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleAiService scheduleAiService;
    private final TripScheduleLock tripLock;
    private final TripPermissionService tripPermissionService;
    private final TransactionTemplate tx;   // 매번 새 트랜잭션 (AFTER_COMMIT 리스너 안에서도 쓴다)
    private final ObjectMapper json = new ObjectMapper();

    public ScheduleComputeService(
            ScheduleRepository scheduleRepository,
            ScheduleItemRepository scheduleItemRepository,
            ScheduleAiService scheduleAiService,
            TripScheduleLock tripLock,
            TripPermissionService tripPermissionService,
            PlatformTransactionManager txManager
    ) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleItemRepository = scheduleItemRepository;
        this.scheduleAiService = scheduleAiService;
        this.tripLock = tripLock;
        this.tripPermissionService = tripPermissionService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private record Settings(String start, String end, String mode, String lunch, String dinner) {}

    private record Snapshot(Map<String, Object> body, List<Long> itemIds) {}

    // ── 이벤트: 노드 구성이 바뀌면(추가·삭제·순서·이동) 커밋 뒤에 다시 계산 ────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onScheduleChanged(ScheduleChangedEvent event) {
        try {
            recompute(event.scheduleId(), false);
        } catch (Exception e) {
            // 사용자가 한 편집(이미 커밋됨)을 실패로 만들지 않는다. 시각은 다음 계산 때 맞춰진다.
            log.warn("일정 시각 자동 계산 실패 scheduleId={}: {}", event.scheduleId(), e.getMessage());
        }
    }

    // ── 화면에서 부르는 동작 ──────────────────────────────────────────────────────────

    /** 한 일차 조회 */
    public ScheduleResponse getDay(Long userId, Long scheduleId) {
        Schedule schedule = requireAccess(userId, scheduleId);
        return read(schedule.getId());
    }

    /**
     * 자동 계산 켜기/끄기
     * - 켤 때: 머무는 시간이 비어 있는(예전) 노드는 지금 시각 간격에서 추정해 채운 뒤 계산한다 —
     *   이 순간 예전 일정의 시각이 처음으로 엔진 값으로 바뀐다.
     * - 끌 때: 지금 시각을 그대로 두고 자동 갱신만 멈춘다.
     */
    public ScheduleResponse setAutoCompute(Long userId, Long scheduleId, boolean enabled) {
        Schedule schedule = requireAccess(userId, scheduleId);
        Long tripId = schedule.getTripId();
        if (!lock(tripId)) throw busy();
        try {
            tx.executeWithoutResult(s -> {
                Schedule sch = scheduleRepository.findById(scheduleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                if (enabled) {
                    // 빌려 쓰던 설정(같은 여행의 다른 일차·기본값)을 이 날에 저장해 화면에 그대로 보이게 한다
                    Settings resolved = resolveSettings(sch);
                    sch.updateSettings(resolved.start(), resolved.end(), resolved.mode());
                    inferMissingStays(sch);
                }
                sch.setAutoCompute(enabled);
            });
            if (enabled) compute(scheduleId);
        } finally {
            tripLock.unlock(tripId);
        }
        return read(scheduleId);
    }

    /**
     * 시작·종료 시각, 이동수단 변경 → 자동 계산이 켜진 날이면 바로 다시 계산
     * - 확인(assertFits)부터 저장까지 여행 단위 잠금을 잡은 채로 한다. 그러지 않으면 서로 다른 노드를 거의 동시에
     *   고치는 두 요청이 각자는 통과하고도 합쳐서 종료 시각을 넘기는 경쟁 상태가 생길 수 있다.
     */
    public ScheduleResponse updateSettings(Long userId, Long scheduleId, ScheduleSettingsRequest request) {
        Schedule schedule = requireAccess(userId, scheduleId);
        String start = request.getStartTime();
        String end = request.getEndTime();
        String mode = request.getTransportMode();
        if (start != null) requireClock(start, "startTime");
        if (end != null) requireClock(end, "endTime");
        if (mode != null && !TRANSPORT_MODES.contains(mode)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이동수단은 택시, 대중교통, 도보 중 하나여야 합니다.");
        }
        String effStart = start != null ? start : schedule.getStartTime();
        String effEnd = end != null ? end : schedule.getEndTime();
        if (effStart != null && effEnd != null && minutes(effStart) >= minutes(effEnd)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "종료 시각은 시작 시각보다 늦어야 합니다.");
        }

        Long tripId = schedule.getTripId();
        if (!lock(tripId)) throw busy();
        try {
            Settings cur = resolveSettings(schedule);
            assertFits(schedule,
                    new Settings(start != null ? start : cur.start(), end != null ? end : cur.end(),
                            mode != null ? mode : cur.mode(), cur.lunch(), cur.dinner()),
                    sortedItems(scheduleId).stream().map(ScheduleComputeService::bodyOf).toList());

            tx.executeWithoutResult(s -> scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND))
                    .updateSettings(start, end, mode));
            recomputeLocked(scheduleId);
        } finally {
            tripLock.unlock(tripId);
        }
        return read(scheduleId);
    }

    /** 지금 상태로 다시 계산 (자동 계산이 꺼진 날은 아무것도 하지 않는다) */
    public ScheduleResponse recomputeNow(Long userId, Long scheduleId) {
        requireAccess(userId, scheduleId);
        recompute(scheduleId, false);
        return read(scheduleId);
    }

    /**
     * 노드의 머무는 시간·고정 시각 변경 → 자동 계산이 켜진 날이면 바로 다시 계산
     * - 확인(assertFits)부터 저장까지 여행 단위 잠금을 잡은 채로 한다. 그러지 않으면 서로 다른 노드를 거의 동시에
     *   고치는 두 요청이 각자는 통과하고도 합쳐서 종료 시각을 넘기는 경쟁 상태가 생길 수 있다.
     */
    public ScheduleResponse updatePlan(Long userId, Long itemId, ScheduleItemPlanRequest request) {
        ScheduleItem found = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Long scheduleId = found.getScheduleId();
        Schedule schedule = requireAccess(userId, scheduleId);

        Integer stay = request.getStayMinutes();
        String pin = request.getPinnedTime();
        if (stay == null && pin == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "stayMinutes 또는 pinnedTime 중 하나는 필요합니다.");
        }
        if (stay != null && (stay < MIN_STAY || stay > MAX_STAY)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "머무는 시간은 " + MIN_STAY + "~" + MAX_STAY + "분이어야 합니다.");
        }
        if (pin != null && !pin.isEmpty()) requireClock(pin, "pinnedTime");

        Long tripId = schedule.getTripId();
        if (!lock(tripId)) throw busy();
        try {
            List<Map<String, Object>> proposed = new ArrayList<>();
            for (ScheduleItem it : sortedItems(scheduleId)) {
                if (it.getId().equals(itemId)) {
                    proposed.add(itemBody(it.getTitle(), it.getCategory(), it.getLat(), it.getLng(),
                            stay != null ? stay : it.getStayMinutes(),
                            pin != null ? (pin.isEmpty() ? null : pin) : it.getPinnedTime()));
                } else {
                    proposed.add(bodyOf(it));
                }
            }
            assertFits(schedule, null, proposed);

            tx.executeWithoutResult(s -> scheduleItemRepository.findById(itemId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND))
                    .updatePlan(stay != null, stay, pin != null, (pin == null || pin.isEmpty()) ? null : pin));
            recomputeLocked(scheduleId);
        } finally {
            tripLock.unlock(tripId);
        }
        return read(scheduleId);
    }

    /**
     * 노드의 장소 바꾸기 — 순서·머무는 시간·고정 시각은 그대로 두고 장소만 바꾼다.
     * 자동 계산이 켜진 날은 새 장소까지의 이동 시간으로 시각을 다시 계산하고, 종료 시각을 넘기게 되면 거절한다.
     */
    public ScheduleResponse replacePlace(Long userId, Long itemId, ScheduleItemPlaceRequest request) {
        ScheduleItem found = scheduleItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Long scheduleId = found.getScheduleId();
        Schedule schedule = requireAccess(userId, scheduleId);

        if (found.getCategory() != null && ANCHOR_CATEGORIES.contains(found.getCategory())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "숙소·출발지는 다른 장소로 바꿀 수 없어요.");
        }
        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        if (title.isEmpty() || title.length() > MAX_TITLE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "장소 이름은 1~" + MAX_TITLE + "자여야 합니다.");
        }
        if (request.getLat() == null || request.getLng() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "좌표가 없는 장소로는 바꿀 수 없어요.");
        }
        if (request.getLat() < -90 || request.getLat() > 90 || request.getLng() < -180 || request.getLng() > 180) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "좌표가 올바르지 않습니다.");
        }
        String category = request.getCategory();
        if (category == null || !REPLACEABLE_CATEGORIES.contains(category)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "카테고리는 관광지, 맛집, 카페, 쇼핑 중 하나여야 합니다.");
        }
        String description = request.getDescription();
        if (description != null && description.length() > MAX_DESCRIPTION) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "주소가 너무 깁니다.");
        }

        List<Map<String, Object>> proposed = new ArrayList<>();
        for (ScheduleItem it : sortedItems(scheduleId)) {
            proposed.add(it.getId().equals(itemId)
                    ? itemBody(title, category, request.getLat(), request.getLng(), it.getStayMinutes(), it.getPinnedTime())
                    : bodyOf(it));
        }
        assertFits(schedule, null, proposed);

        Long tripId = schedule.getTripId();
        if (!lock(tripId)) throw busy();
        try {
            tx.executeWithoutResult(s -> {
                List<ScheduleItem> items = sortedItems(scheduleId);
                for (int i = 0; i < items.size(); i++) {
                    if (!items.get(i).getId().equals(itemId)) continue;
                    items.get(i).replacePlace(title, description, category, request.getLat(), request.getLng());
                    // 바로 앞 노드의 "다음 장소까지" 정보도 낡았다 (자동 계산이 켜진 날은 아래 계산이 다시 채운다)
                    if (i > 0) items.get(i - 1).clearTravel();
                }
            });
            recomputeLocked(scheduleId);
        } finally {
            tripLock.unlock(tripId);
        }
        return read(scheduleId);
    }

    // ── 계산 본체 ────────────────────────────────────────────────────────────────────

    /** force=false면 자동 계산이 꺼진 날은 건너뛴다 */
    void recompute(Long scheduleId, boolean force) {
        Schedule head = scheduleRepository.findById(scheduleId).orElse(null);
        if (head == null || (!force && !head.isAutoComputeOn())) return;

        Long tripId = head.getTripId();
        if (!lock(tripId)) {
            log.info("일정 생성 등이 진행 중이라 자동 계산을 건너뜀 scheduleId={}", scheduleId);
            return;
        }
        try {
            compute(scheduleId);
        } finally {
            tripLock.unlock(tripId);
        }
    }

    /** 잠금을 잡은 상태에서 호출 — 자동 계산이 켜진 날만 다시 계산 */
    private void recomputeLocked(Long scheduleId) {
        Schedule sch = scheduleRepository.findById(scheduleId).orElse(null);
        if (sch != null && sch.isAutoComputeOn()) compute(scheduleId);
    }

    /** 잠금을 잡은 상태에서 호출 */
    private void compute(Long scheduleId) {
        Snapshot snapshot = tx.execute(s -> snapshotOf(scheduleId));
        if (snapshot == null) return;

        Map<String, Object> result = snapshot.itemIds().isEmpty()
                ? Map.of()
                : scheduleAiService.recomputeDay(snapshot.body());

        tx.executeWithoutResult(s -> apply(scheduleId, snapshot.itemIds(), result));
    }

    private Snapshot snapshotOf(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) return null;

        List<ScheduleItem> items = sortedItems(scheduleId);
        Settings settings = resolveSettings(schedule);

        List<Map<String, Object>> body = items.stream().map(ScheduleComputeService::bodyOf).toList();
        return new Snapshot(requestOf(settings, body), items.stream().map(ScheduleItem::getId).toList());
    }

    private static Map<String, Object> bodyOf(ScheduleItem item) {
        return itemBody(item.getTitle(), item.getCategory(), item.getLat(), item.getLng(),
                item.getStayMinutes(), item.getPinnedTime());
    }

    private static Map<String, Object> itemBody(String title, String category, Double lat, Double lng,
                                                Integer stay, String pinned) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", title);
        m.put("category", category != null ? category : "관광지");
        m.put("lat", lat);
        m.put("lng", lng);
        m.put("stay_minutes", stay);
        m.put("pinned_time", pinned);
        return m;
    }

    private static Map<String, Object> requestOf(Settings settings, List<Map<String, Object>> items) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("items", items);
        request.put("start_time", settings.start());
        request.put("end_time", settings.end());
        request.put("transportation_mode", settings.mode());
        request.put("lunch_time", settings.lunch());
        request.put("dinner_time", settings.dinner());
        return request;
    }

    // ── 종료 시각 초과 막기 ────────────────────────────────────────────────────────────
    // 자동 계산이 켜진 날은 직접 고쳐서 종료 시각을 넘기게 되는 변경(머무는 시간·고정 시각·순서·추가·이동·설정)을 거절한다.
    // 변경을 저장하기 전에 "바꾼 뒤 상태"로 엔진을 한 번 돌려 본다(저장 없는 순수 계산이라 빠르다).
    // - 이미 넘긴 날이라도 더 나빠지지 않는 변경(줄이거나 그대로)은 허용한다 — 넘긴 날을 고쳐 가는 길을 막지 않는다.
    // - 엔진에 닿지 못하면 확인할 수 없으므로 막지 않는다(편집을 못 하게 되는 쪽이 더 나쁘다).

    /** 순서를 바꾼 뒤의 상태가 종료 시각을 넘기면 거절 */
    public void checkOrder(Schedule schedule, List<ScheduleItem> ordered) {
        assertFits(schedule, null, ordered.stream().map(ScheduleComputeService::bodyOf).toList());
    }

    /** 노드를 맨 뒤에 추가한 뒤의 상태가 종료 시각을 넘기면 거절 (머무는 시간은 엔진 기본값) */
    public void checkAdd(Schedule schedule, String title, String category, Double lat, Double lng) {
        List<Map<String, Object>> proposed = new ArrayList<>(
                sortedItems(schedule.getId()).stream().map(ScheduleComputeService::bodyOf).toList());
        proposed.add(itemBody(title, category, lat, lng, null, null));
        assertFits(schedule, null, proposed);
    }

    /** 다른 날에서 온 노드를 이 날 맨 뒤에 붙인 뒤의 상태가 종료 시각을 넘기면 거절 */
    public void checkMoveIn(Schedule target, ScheduleItem moving) {
        if (target.getId().equals(moving.getScheduleId())) return;   // 같은 날 안의 이동은 구성이 그대로라 확인할 게 없다
        List<Map<String, Object>> proposed = new ArrayList<>(
                sortedItems(target.getId()).stream().map(ScheduleComputeService::bodyOf).toList());
        proposed.add(bodyOf(moving));
        assertFits(target, null, proposed);
    }

    private void assertFits(Schedule schedule, Settings proposedSettings, List<Map<String, Object>> proposedItems) {
        if (!schedule.isAutoComputeOn()) return;
        Settings current = resolveSettings(schedule);
        Settings next = proposedSettings != null ? proposedSettings : current;

        Integer after = simulateOver(next, proposedItems);
        if (after == null || after <= 0) return;

        Integer before = simulateOver(current, sortedItems(schedule.getId()).stream().map(ScheduleComputeService::bodyOf).toList());
        if (before != null && after <= before) return;

        throw new BusinessException(ErrorCode.INVALID_REQUEST,
                "종료 시간(" + next.end() + ")을 " + after + "분 넘기게 돼서 바꿀 수 없어요.");
    }

    /** 가설 상태의 종료 시각 초과 분. 계산할 수 없으면 null */
    @SuppressWarnings("unchecked")
    private Integer simulateOver(Settings settings, List<Map<String, Object>> items) {
        if (items.isEmpty()) return 0;
        try {
            Map<String, Object> result = scheduleAiService.recomputeDay(requestOf(settings, items));
            return result != null && result.get("over_minutes") instanceof Number n ? n.intValue() : null;
        } catch (Exception e) {
            log.warn("종료 시각 초과 확인 실패(막지 않음): {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void apply(Long scheduleId, List<Long> expectedIds, Map<String, Object> result) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) return;

        List<ScheduleItem> items = sortedItems(scheduleId);
        if (!items.stream().map(ScheduleItem::getId).toList().equals(expectedIds)) {
            log.info("계산 중 노드 구성이 바뀌어 결과를 버림 scheduleId={}", scheduleId);
            return;
        }
        if (items.isEmpty()) {
            schedule.updateWarnings("[]");
            return;
        }

        List<Map<String, Object>> computed = (List<Map<String, Object>>) result.get("items");
        List<Map<String, Object>> warnings = new ArrayList<>();
        for (int i = 0; i < items.size() && computed != null && i < computed.size(); i++) {
            Map<String, Object> c = computed.get(i);
            ScheduleItem item = items.get(i);
            // 동시에 추가된 노드가 같은 순서 번호를 받았을 수 있어, 계산할 때 0..n-1로 다시 매겨 바로잡는다
            if (item.getOrderIndex() != i) item.updateOrder(i);
            item.applyComputed(
                    (String) c.get("time"),
                    intOf(c.get("stay_minutes")),
                    intOf(c.get("travel_minutes")));
            for (Map<String, Object> w : (List<Map<String, Object>>) c.getOrDefault("warnings", List.of())) {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("itemId", item.getId());
                out.put("code", w.get("code"));
                out.put("message", w.get("message"));
                warnings.add(out);
            }
        }
        for (Map<String, Object> w : (List<Map<String, Object>>) result.getOrDefault("day_warnings", List.of())) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("itemId", null);
            out.put("code", w.get("code"));
            out.put("message", w.get("message"));
            out.put("overMinutes", intOf(w.get("over_minutes")));
            Integer suggest = intOf(w.get("suggest_move_index"));
            out.put("suggestMoveItemId", suggest != null && suggest >= 0 && suggest < items.size()
                    ? items.get(suggest).getId() : null);
            warnings.add(out);
        }
        try {
            schedule.updateWarnings(json.writeValueAsString(warnings));
        } catch (Exception e) {
            schedule.updateWarnings("[]");
        }
    }

    /**
     * 자동 계산을 처음 켤 때, 머무는 시간이 비어 있는 노드를 "다음 노드 시작 − 내 시작 − 이동시간"으로 추정해 채운다.
     * (마지막 노드·간격을 알 수 없는 노드는 비워 두어 엔진의 카테고리 기본값을 쓰게 한다. 숙소·출발지는 0분.)
     */
    private void inferMissingStays(Schedule schedule) {
        List<ScheduleItem> items = sortedItems(schedule.getId());
        for (int i = 0; i < items.size(); i++) {
            ScheduleItem item = items.get(i);
            if (item.getStayMinutes() != null) continue;
            if (item.getCategory() != null && ANCHOR_CATEGORIES.contains(item.getCategory())) {
                item.updatePlan(true, 0, false, null);
                continue;
            }
            if (i + 1 >= items.size()) continue;
            Integer from = ScheduleResponseMapper.toMinutes(item.getTime());
            Integer to = ScheduleResponseMapper.toMinutes(items.get(i + 1).getTime());
            if (from == null || to == null) continue;
            int travel = item.getTravelMinutes() != null ? item.getTravelMinutes() : 0;
            int gap = to - from - travel;
            if (gap <= 0) continue;
            int rounded = Math.round(gap / 10f) * 10;
            item.updatePlan(true, Math.max(INFERRED_STAY_MIN, Math.min(INFERRED_STAY_MAX, rounded)), false, null);
        }
    }

    /**
     * 그날의 설정 — 그날에 저장된 값이 있으면 그것, 없으면 같은 여행의 다른 일차(일차 순)에 저장된
     * 사용자 설정을 빌려 쓰고, 그것도 없으면 기본값.
     */
    private Settings resolveSettings(Schedule schedule) {
        List<Schedule> siblings = scheduleRepository.findAllByTripId(schedule.getTripId()).stream()
                .sorted(Comparator.comparingInt(Schedule::getDay))
                .toList();
        return new Settings(
                pick(schedule, siblings, Schedule::getStartTime, DEFAULT_START),
                pick(schedule, siblings, Schedule::getEndTime, DEFAULT_END),
                pick(schedule, siblings, Schedule::getTransportMode, DEFAULT_TRANSPORT),
                pick(schedule, siblings, Schedule::getLunchTime, DEFAULT_LUNCH),
                pick(schedule, siblings, Schedule::getDinnerTime, DEFAULT_DINNER));
    }

    private static String pick(Schedule own, List<Schedule> siblings,
                               Function<Schedule, String> getter, String fallback) {
        String v = getter.apply(own);
        if (v != null && !v.isBlank()) return v;
        for (Schedule s : siblings) {
            String sv = getter.apply(s);
            if (sv != null && !sv.isBlank()) return sv;
        }
        return fallback;
    }

    // ── 보조 ─────────────────────────────────────────────────────────────────────────

    private ScheduleResponse read(Long scheduleId) {
        return tx.execute(s -> {
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
            return ScheduleResponseMapper.toResponse(
                    schedule, scheduleItemRepository.findAllByScheduleId(scheduleId), null, null);
        });
    }

    private List<ScheduleItem> sortedItems(Long scheduleId) {
        return scheduleItemRepository.findAllByScheduleId(scheduleId).stream()
                .sorted(Comparator.comparingInt(ScheduleItem::getOrderIndex).thenComparing(ScheduleItem::getId))
                .toList();
    }

    private Schedule requireAccess(Long userId, Long scheduleId) {
        if (scheduleId == null) throw new BusinessException(ErrorCode.INVALID_REQUEST, "scheduleId는 필수입니다.");
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        tripPermissionService.assertOwnerOrMember(schedule.getTripId(), userId);
        return schedule;
    }

    private boolean lock(Long tripId) {
        return tripLock.tryLock(tripId, 5, TimeUnit.SECONDS);
    }

    private static BusinessException busy() {
        return new BusinessException(ErrorCode.CONFLICT, "지금 이 여행의 일정을 만들고 있어요. 잠시 후 다시 시도해주세요.");
    }

    private static void requireClock(String value, String field) {
        if (!value.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, field + "는 HH:MM 형식이어야 합니다.");
        }
    }

    private static int minutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private static Integer intOf(Object v) {
        return v instanceof Number n ? n.intValue() : null;
    }
}
