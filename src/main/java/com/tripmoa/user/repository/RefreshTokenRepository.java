package com.tripmoa.user.repository;

import com.tripmoa.user.entity.RefreshToken;
import com.tripmoa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 특정 유저의 모든 토큰 삭제
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);

}
