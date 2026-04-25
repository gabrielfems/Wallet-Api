package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor()
public class UserService {

    private final ViaCepService viaCepService;
    private final UserRepository userRepository;

    public User findUserById(Long id) { return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));}
    public User findSenderById(Long id) { return userRepository.findById(id).orElseThrow(() -> new SenderUserNotFoundException());}
    public User findReceiverById(Long id) { return userRepository.findById(id).orElseThrow(() -> new ReceiverUserNotFoundException());}

    public void saveUser(User user) {
        this.userRepository.save(user);
    }

    public User createUser(UserCreateDTO data) {
        ViaCepResponseDTO enderecoViaCep = viaCepService.buscarEnderecoPorCep(data.cep());

        String enderecoCompleto = viaCepService.montarEnderecoCompleto(
                enderecoViaCep,
                data.numero(),
                data.complemento()
        );

        User newUser = new User(data);
        newUser.setCep(enderecoCompleto);
        newUser.setNumero(data.numero());
        newUser.setComplemento(data.complemento());
        userRepository.save(newUser);
        return newUser;
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
                    dto.numero() != null ? dto.numero() : user.getNumero(),
                    dto.complemento() != null ? dto.complemento() : user.getComplemento()
            );
            user.setCep(enderecoCompleto);
            user.setNumero(dto.numero() != null ? dto.numero() : user.getNumero());
            user.setComplemento(dto.complemento() != null ? dto.complemento() : user.getComplemento());
        }

        user.setPhone(dto.phone() != null ? dto.phone() : user.getPhone());
        user.setPassword(dto.password() != null ? dto.password() : user.getPassword());
        user.setName(dto.name() != null ? dto.name() : user.getName());
        user.setBirthDate(dto.birthDate() != null ? dto.birthDate() : user.getBirthDate());
        user.setEmail(dto.email() != null ? dto.email() : user.getEmail());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(findUserById(id));
    }
}



