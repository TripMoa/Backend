package com.tripmoa.community.mate.repository;

import com.tripmoa.community.mate.domain.MateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<MateApplication, Long> {

    @Query("SELECT a FROM MateApplication a " +
            "JOIN FETCH a.matePost p " +
            "JOIN FETCH a.applicant u " +
            "WHERE p.user.id = :ownerId")
    List<MateApplication> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT a FROM MateApplication a " +
            "JOIN FETCH a.applicant " +
            "WHERE a.applicant.id = :applicantId")
    List<MateApplication> findByApplicantId(Long applicantId);

    boolean existsByMatePostIdAndApplicantId(Long postId, Long applicantId);
}
