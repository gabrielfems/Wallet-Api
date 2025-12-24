package com.walletapi.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User receiver;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private WalletStatus status;
    @OneToMany(mappedBy = "wallet")
    private List<Transaction> transactions;
}
