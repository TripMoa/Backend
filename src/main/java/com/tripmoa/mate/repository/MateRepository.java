package com.tripmoa.mate.repository;

import com.tripmoa.mate.domain.MatePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MateRepository extends JpaRepository <MatePost, Long>, LikeRepositoryCustom{

    @Query("SELECT p.likesCount FROM MatePost p WHERE p.id = :postId")
    Long countLikes(@Param("postId") Long postId);

    @Query("SELECT m FROM MatePost m JOIN FETCH m.user")
    List<MatePost> findAllWithUser();

    @Query("SELECT m FROM MatePost m JOIN FETCH m.user WHERE m.id = :id")
    Optional<MatePost> findByIdWithUser(Long id);

    @Modifying
    @Query("UPDATE MatePost m SET m.viewsCount = m.viewsCount + 1 WHERE m.id = :postId")
    void updateViewsCount(@Param("postId") Long postId);

    Page<MatePost> findByEndDateBeforeOrderByEndDateDesc(LocalDate date, Pageable pageable);
}
