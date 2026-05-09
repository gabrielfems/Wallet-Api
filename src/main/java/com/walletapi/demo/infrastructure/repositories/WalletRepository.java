package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
