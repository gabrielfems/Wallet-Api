package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
