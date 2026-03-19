package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}