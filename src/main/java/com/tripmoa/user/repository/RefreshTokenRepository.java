package com.tripmoa.user.repository;

import com.tripmoa.user.entity.RefreshToken;
import com.tripmoa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 값으로 리프레시 토큰 조회 (재발급 시 검증용)
    Optional<RefreshToken> findByToken(String token);

    // 특정 유저의 리프레시 토큰 조회 (로그인 시 update/insert 분기용)
    Optional<RefreshToken> findByUser(User user);

    // 특정 유저의 리프레시 토큰 삭제 (로그아웃/탈퇴 시 세션 제거)
    void deleteByUser(User user);

}
