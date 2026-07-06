package com.tripmoa.place.service;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.place.domain.Place;
import com.tripmoa.place.dto.PlaceCreateRequest;
import com.tripmoa.place.dto.PlaceResponse;
import com.tripmoa.place.dto.PlaceUpdateRequest;
import com.tripmoa.place.repository.PlaceRepository;
import com.tripmoa.trip.service.TripPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PlaceService
  * - 장소 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TripPermissionService tripPermissionService;

    // 장소 생성
    public PlaceResponse save(PlaceCreateRequest request) {
        Place place = Place.builder()
                .tripId(request.getTripId())
                .name(request.getName())
                .category(request.getCategory())
                .lat(request.getLat())
                .lng(request.getLng())
                .address(request.getAddress())
                .description(request.getDescription())
                .build();

        return PlaceResponse.from(placeRepository.save(place));
    }
    
    // 장소 조회
    public List<PlaceResponse> getPlaces(Long tripId) {
        return placeRepository.findAllByTripId(tripId)
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    // 장소 카테고리 수정
    @Transactional
    public PlaceResponse update(Long placeId, Long userId, PlaceUpdateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        tripPermissionService.assertOwnerOrMember(place.getTripId(), userId);

        place.update(request.getCategory());

        return PlaceResponse.from(place);
    }

    //장소 삭제
    public void deletePlace(Long placeId, Long userId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        tripPermissionService.assertOwnerOrMember(place.getTripId(), userId);

        placeRepository.deleteById(placeId);
    }
}
