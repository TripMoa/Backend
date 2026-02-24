package com.tripmoa.community.mate.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final EntityManager em;

    @Override
    @Transactional
    public void updateLikeCount(Long postId, Long Count) {
        em.createQuery("UPDATE MatePost p SET p.likesCount = :count WHERE p.id = :postId")
                .setParameter("count", Count)
                .setParameter("postId", postId)
                .executeUpdate();
    }

}
