package com.tripmoa.user.repository;

import com.tripmoa.user.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

}
