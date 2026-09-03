package com.tripmoa.user.repository;

import com.tripmoa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // 이미 있는 사용자인지 확인
    Optional<User> findByEmail(String email);

}
