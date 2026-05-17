package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalBoxRepository extends JpaRepository<GoalBox, Long> {
    List<GoalBox> findByUser(User user);
    Optional<GoalBox> findByIdAndUser(Long id, User user);
}