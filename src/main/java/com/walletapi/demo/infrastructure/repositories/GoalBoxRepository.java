// GoalBoxRepository.java
package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.GoalBox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalBoxRepository extends JpaRepository<GoalBox, Long> {
    List<GoalBox> findByUserId(Long userId);
}