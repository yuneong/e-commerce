package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByUserId(String userId);

    Optional<User> findByUserId(String userId);

}
