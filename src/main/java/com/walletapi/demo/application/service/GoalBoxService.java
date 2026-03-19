// GoalBoxService.java
package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxResponseDTO;
import com.walletapi.demo.application.dto.GoalBoxUpdateDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.UnauthorizedBoxAccessException;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.GoalBoxRepository;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalBoxService {

    private final GoalBoxRepository boxRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public GoalBox createBox(Long userId, GoalBoxCreateDTO dto) {
        User user = userService.findById(userId);

        GoalBox box = new GoalBox();
        box.setName(dto.name());
        box.setDescription(dto.description());
        box.setTargetAmount(dto.targetAmount());
        box.setUser(user);

        return boxRepository.save(box);
    }

    public List<GoalBoxResponseDTO> getUserBoxes(Long userId) {
        userService.findById(userId);
        return boxRepository.findByUserId(userId)
                .stream()
                .map(GoalBoxResponseDTO::from)
                .toList();
    }

    public GoalBox getBox(Long userId, Long boxId) {
        GoalBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new GoalBoxNotFoundException(boxId));

        if (!box.getUser().getId().equals(userId)) {
            throw new UnauthorizedBoxAccessException();
        }

        return box;
    }

    public GoalBox deposit(Long userId, Long boxId, BigDecimal amount) {
        GoalBox box = getBox(userId, boxId);
        User user = userRepository.findById(userId).get();

        if (user.getWallet().getBalance().compareTo(amount) >= 0) {

            box.setCurrentBalance(box.getCurrentBalance().add(amount));
            user.getWallet().setBalance(user.getWallet().getBalance().subtract(amount));
            userRepository.save(user);

        } else {
            throw new InsufficientBalanceException("Saldo insuficiente");

        }
        return boxRepository.save(box);
    }

    public void deleteBox(Long userId, Long boxId) {
        GoalBox box = getBox(userId, boxId);
        boxRepository.delete(box);
    }

    public GoalBox withdraw(Long userId, Long boxId, BigDecimal amount) {
        GoalBox box = getBox(userId, boxId);

        if (box.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente na caixinha");
        }

        box.setCurrentBalance(box.getCurrentBalance().subtract(amount));

        return boxRepository.save(box);
    }

    public GoalBox updateBox(Long userId, Long boxId, GoalBoxUpdateDTO dto) {
        GoalBox box = getBox(userId, boxId);

        if (dto.name() != null) box.setName(dto.name());
        if (dto.description() != null) box.setDescription(dto.description());
        if (dto.targetAmount() != null) box.setTargetAmount(dto.targetAmount());

        return boxRepository.save(box);
    }
}