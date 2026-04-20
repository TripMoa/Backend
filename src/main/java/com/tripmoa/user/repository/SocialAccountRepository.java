package com.tripmoa.user.repository;

import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.Provider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount,Long> {

    // 유저 탈퇴
    @Transactional
    void deleteByUser(User user);

    // 특정 사용자(user)가 특정 소셜 제공자(provider)로 이미 연결된 계정이 있는지 조회
    Optional<SocialAccount> findByUserAndProvider(User user, Provider provider);

}
