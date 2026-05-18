package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.UnauthorizedUserAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ViaCepService viaCepService;
    private final UserRepository userRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User findReceiverById(Long id) {
        return userRepository.findById(id).orElseThrow(ReceiverUserNotFoundException::new);
    }

    public void saveUser(User user) {
        this.userRepository.save(user);
    }

    @Transactional
    public void createUser(UserRegisterDTO data) {
        Wallet newWallet = new Wallet();
        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setStatus(WalletStatus.ACTIVE);

        User newUser = new User();
        newUser.setEmail(data.login());
        newUser.setWallet(newWallet);

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        UserCredentials newUserCred = new UserCredentials(data.login(), encryptedPassword, data.role());
        newUserCred.setUser(newUser);
        newWallet.setUser(newUser);

        userRepository.save(newUser);
        userCredentialsRepository.save(newUserCred);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    private void checkAccess(UserCredentials credentials, Long targetUserId) {
        boolean isAdmin = credentials.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = credentials.getUser().getId().equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedUserAccessException();
        }
    }

    public User updateUser(Long id, UserUpdateDTO dto, UserCredentials credentials) {
        checkAccess(credentials, id);

        User user = findUserById(id);

        if (dto.cep() != null) {
            ViaCepResponseDTO endereco = viaCepService.buscarEnderecoPorCep(dto.cep());
            String enderecoCompleto = viaCepService.montarEnderecoCompleto(
                    endereco,
                    dto.numero() != null ? dto.numero() : user.getHouse_number(),
                    dto.complemento() != null ? dto.complemento() : user.getComplement()
            );
            user.setAddress(enderecoCompleto);
            user.setCep(dto.cep());
            user.setHouse_number(dto.numero() != null ? dto.numero() : user.getHouse_number());
            user.setComplement(dto.complemento() != null ? dto.complemento() : user.getComplement());
        }

        user.setPhone(dto.phone() != null ? dto.phone() : user.getPhone());
        user.setName(dto.name() != null ? dto.name() : user.getName());
        user.setBirthDate(dto.birthDate() != null ? dto.birthDate() : user.getBirthDate());
        user.setDocument(dto.document() != null ? dto.document() : user.getDocument());

        return userRepository.save(user);
    }

    public void deleteUser(Long id, UserCredentials credentials) {
        checkAccess(credentials, id);
        userRepository.delete(findUserById(id));
    }
}