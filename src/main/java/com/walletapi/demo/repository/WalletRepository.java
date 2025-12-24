package com.walletapi.demo.repository;

import com.walletapi.demo.model.User;
import com.walletapi.demo.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(String user);
    boolean existsByUser(String user);
}
