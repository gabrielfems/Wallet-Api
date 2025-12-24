package com.walletapi.demo.repository;

import com.walletapi.demo.model.User;
import com.walletapi.demo.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByPassword(String password);
    boolean existsByPassword(String password);

    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);

    Optional<User> findByAddress(String address);
    boolean existsByAddress(String address);
}
