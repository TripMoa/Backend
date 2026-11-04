package com.tripmoa.story.repository;

import com.tripmoa.story.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.tripmoa.story.enums.StoryType;

/* 여행기 Repository
 - Story 엔티티의 DB 접근 담당 */

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // 특정 사용자가 작성한 여행기 목록 조회 (최신순)
    List<Story> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    // 전체 조회 (최신순)
    List<Story> findAllByOrderByCreatedAtDesc();

    // 태그 포함된 게시글 조회
    List<Story> findByTagsContaining(String tag);

    // 특정 여행(tripId)에 대해 해당 작성자(authorId)의 리뷰가 이미 존재하는지 확인
    boolean existsByTrip_IdAndAuthor_Id(Long tripId, Long authorId);

    // 특정 여행(tripId)에 리뷰가 이미 있는지 (여행 공동 소유 - 여행당 리뷰 1개)
    boolean existsByTrip_IdAndType(Long tripId, StoryType type);

    // 내 여행기 목록: 내가 쓴 글 + 내가 멤버인 여행의 리뷰 (최신순)
    @Query("""
            select s from Story s
            where s.author.id = :userId
               or (s.type = com.tripmoa.story.enums.StoryType.REVIEW
                   and s.trip is not null
                   and exists (select 1 from TripMember tm
                               where tm.trip = s.trip and tm.user.id = :userId))
            order by s.createdAt desc
            """)
    List<Story> findMyStoriesIncludingTripReviews(@Param("userId") Long userId);

    // 공개 글만 조회 (피드용) - REVIEW는 공개만, FREE는 다 보임
    List<Story> findByIsPublicTrueOrTypeOrderByCreatedAtDesc(StoryType type);

    // 태그 + 조건
    List<Story> findByTagsContainingAndIsPublicTrueOrTagsContainingAndTypeOrderByCreatedAtDesc(String tag1, String tag2, StoryType type);
}
