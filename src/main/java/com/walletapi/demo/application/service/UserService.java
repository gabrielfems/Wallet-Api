package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor()
public class UserService {

    private final ViaCepService viaCepService;
    private final UserRepository userRepository;

    public User findUserById(Long id) { return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));}
    public User findSenderById(Long id) { return userRepository.findById(id).orElseThrow(SenderUserNotFoundException::new);}
    public User findReceiverById(Long id) { return userRepository.findById(id).orElseThrow(ReceiverUserNotFoundException::new);}

    public void saveUser(User user) {
        this.userRepository.save(user);
    }

    public void createUser(UserRegisterDTO data) {
        Wallet newWallet = new Wallet();
        User newUser = new User();

        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setStatus(WalletStatus.ACTIVE);

        newUser.setLogin(data.login());
        newUser.setRole(data.role());
        newUser.setWallet(newWallet);

        newWallet.setUser(newUser);
        newWallet.setUser(newUser);

        this.userRepository.save(newUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    public User updateUser(Long id, UserUpdateDTO dto) {
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

    public void deleteUser(Long id) {
        userRepository.delete(findUserById(id));
    }
}



