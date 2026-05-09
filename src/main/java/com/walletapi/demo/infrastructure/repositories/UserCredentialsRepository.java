package com.walletapi.demo.infrastructure.repositories;

import com.walletapi.demo.domain.entities.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String> {
    UserDetails findByLogin(String login);

}
