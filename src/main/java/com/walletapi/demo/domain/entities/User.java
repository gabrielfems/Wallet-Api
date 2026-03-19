package com.walletapi.demo.domain.entities;
import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.domain.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

//Dados do usuario

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
    private String email;

    @Column(unique = true)
    private String document;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phone;

    private String cep;

    private String numero;

    private String complemento;

    private LocalDate birthDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallet wallet;

    public User(UserCreateDTO data) {
        this.name = data.name();
        this.email = data.email();
        this.document = data.document();
        this.password = data.password();
        this.phone = data.phone();
        this.cep = data.cep();
        this.numero = data.numero();
        this.complemento = data.complemento();
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

