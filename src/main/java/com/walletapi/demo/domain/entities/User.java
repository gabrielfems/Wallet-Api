package com.walletapi.demo.domain.entities;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.domain.enums.UserRole;
import com.walletapi.demo.domain.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity(name= "users")
@Table(name = "users")
@EqualsAndHashCode(of="id")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String document;

    @Column(unique = true)
    private String phone;

    private String Address;

    private String cep;

    private String house_number;

    private String complement;

    private LocalDate birthDate;

    @Column(unique = true)
    private String email;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCredentials credentials;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallet wallet;

    public User(UserCreateDTO data) {
        this.name = data.name();
        this.document = data.document();
        this.phone = data.phone();
        this.cep = data.cep();
        this.house_number = data.numero();
        this.complement = data.complemento();
        this.birthDate = data.birthDate();
        this.wallet = createDefaultWallet();
    }

    private Wallet createDefaultWallet() {
        Wallet defaultW = new Wallet();
        defaultW.setBalance(BigDecimal.ZERO);
        defaultW.setStatus(WalletStatus.ACTIVE);
        defaultW.setUser(this);
        defaultW.setTransactions(new ArrayList<>());
        return defaultW;
    }
}

