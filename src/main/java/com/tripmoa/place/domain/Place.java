package com.tripmoa.place.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Place (장소 엔티티)
 *
 * - 여행(trip)에 속한 장소를 저장
 * - 기존 프론트 localStorage → DB로 이동시키는 핵심 엔티티
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tripId;

    // 장소 이름
    private String name;
    
    // 카테고리(관광지, 맛집)
    private String category;

    // 위도/경도
    private Double lat;
    private Double lng;

    // 추가: 주소
    private String address;

    @Column(length = 500)
    private String description;  // 추가: 장소 설명

    // 카테고리 수정
    public void update(String category) {
        if (category != null) this.category = category;
    }
}
