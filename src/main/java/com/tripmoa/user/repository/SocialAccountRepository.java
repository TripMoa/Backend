package com.tripmoa.user.repository;

import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount,Long> {

    // 유저 탈퇴
    @Transactional
    void deleteByUser(User user);

}
