package com.tripmoa.voucher.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.global.file.FileDirectories;
import com.tripmoa.global.file.FileStorageService;
import com.tripmoa.global.file.StoredFileInfo;
import com.tripmoa.schedule.domain.Schedule;
import com.tripmoa.schedule.domain.ScheduleItem;
import com.tripmoa.schedule.repository.ScheduleItemRepository;
import com.tripmoa.schedule.repository.ScheduleRepository;
import com.tripmoa.trip.entity.Trip;
import com.tripmoa.trip.service.TripPermissionService;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.UserRepository;
import com.tripmoa.voucher.dto.VoucherCreateRequest;
import com.tripmoa.voucher.dto.VoucherResponse;
import com.tripmoa.voucher.dto.VoucherUpdateRequest;
import com.tripmoa.voucher.entity.Voucher;
import com.tripmoa.voucher.enums.VoucherFileType;
import com.tripmoa.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final TripPermissionService tripPermissionService;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public VoucherResponse create(Long tripId, Long userId, VoucherCreateRequest request, MultipartFile file) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Trip trip = tripPermissionService.getTripOr404(tripId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Long scheduleItemId = resolveScheduleItemId(tripId, request.scheduleItemId());

        StoredFileInfo storedFile = fileStorageService.store(file, FileDirectories.VOUCHER);

        Voucher voucher = Voucher.builder()
                .trip(trip)
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .fileUrl(storedFile.fileUrl())
                .fileName(storedFile.originalFileName())
                .fileType(resolveFileType(file))
                .fileSize(storedFile.fileSize())
                .createdByUser(user)
                .scheduleItemId(scheduleItemId)
                .build();

        voucherRepository.save(voucher);
        return VoucherResponse.from(voucher);
    }

    public List<VoucherResponse> getVouchers(Long tripId, Long userId, Long scheduleItemId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        List<Voucher> vouchers = scheduleItemId != null
                ? voucherRepository.findAllByTrip_IdAndScheduleItemIdOrderByCreatedAtDesc(tripId, scheduleItemId)
                : voucherRepository.findAllByTrip_IdOrderByCreatedAtDesc(tripId);

        return vouchers.stream()
                .map(VoucherResponse::from)
                .toList();
    }

    public VoucherResponse getVoucher(Long tripId, Long voucherId, Long userId) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Voucher voucher = voucherRepository.findByIdAndTrip_Id(voucherId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바우처를 찾을 수 없습니다."));

        return VoucherResponse.from(voucher);
    }

    @Transactional
    public VoucherResponse update(Long tripId, Long voucherId, Long userId,
                                  VoucherUpdateRequest request, MultipartFile file) {
        tripPermissionService.assertOwnerOrMember(tripId, userId);

        Voucher voucher = voucherRepository.findByIdAndTrip_Id(voucherId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바우처를 찾을 수 없습니다."));

        Long scheduleItemId = resolveScheduleItemId(tripId, request.scheduleItemId());

        String fileUrl = voucher.getFileUrl();
        String fileName = voucher.getFileName();
        VoucherFileType fileType = voucher.getFileType();
        Long fileSize = voucher.getFileSize();

        if (file != null && !file.isEmpty()) {
            StoredFileInfo storedFile = fileStorageService.store(file, FileDirectories.VOUCHER);

            String oldFileUrl = voucher.getFileUrl();

            fileUrl = storedFile.fileUrl();
            fileName = storedFile.originalFileName();
            fileType = resolveFileType(file);
            fileSize = storedFile.fileSize();

            fileStorageService.deleteFile(oldFileUrl);
        }

        voucher.update(
                request.type(),
                request.title(),
                request.description(),
                fileUrl,
                fileName,
                fileType,
                fileSize,
                scheduleItemId
        );

        return VoucherResponse.from(voucher);
    }

    @Transactional
    public void delete(Long tripId, Long voucherId, Long userId) {
        tripPermissionService.assertOwner(tripId, userId);

        Voucher voucher = voucherRepository.findByIdAndTrip_Id(voucherId, tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바우처를 찾을 수 없습니다."));

        fileStorageService.deleteFile(voucher.getFileUrl());
        voucherRepository.delete(voucher);
    }

    // scheduleItemId가 오면 해당 일정 항목이 실제로 존재하고, 같은 트립 소속인지 검증
    private Long resolveScheduleItemId(Long tripId, Long scheduleItemId) {
        if (scheduleItemId == null) {
            return null;
        }

        ScheduleItem item = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연결하려는 일정 항목을 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "연결하려는 일정 항목을 찾을 수 없습니다."));

        if (!schedule.getTripId().equals(tripId)) {
            throw new BusinessException(ErrorCode.TRIP_FORBIDDEN, "다른 여행의 일정 항목에는 연결할 수 없습니다.");
        }

        return scheduleItemId;
    }

    private VoucherFileType resolveFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            return VoucherFileType.IMG;
        }

        String normalized = contentType.toLowerCase();

        if (normalized.contains("pdf")) {
            return VoucherFileType.PDF;
        }
        if (normalized.contains("jpeg") || normalized.contains("jpg")) {
            return VoucherFileType.JPG;
        }
        return VoucherFileType.IMG;
    }
}
