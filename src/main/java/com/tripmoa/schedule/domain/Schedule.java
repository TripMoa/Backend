package com.tripmoa.schedule.domain;

import com.tripmoa.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;

/**
 * Schedule (Day 단위 일정)
 *
 * - 하나의 여행(trip)은 여러 개의 Day를 가짐
 * - 예: Day1, Day2, Day3
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 여행에 속하는 일정인지
    private Long tripId;

    // 몇 번째 날인지 (1,2,3...)
    private int day;
}
