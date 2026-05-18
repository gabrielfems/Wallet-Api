package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxResponseDTO;
import com.walletapi.demo.application.dto.GoalBoxUpdateDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.UnauthorizedBoxAccessException;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.infrastructure.repositories.GoalBoxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalBoxService {

    private final GoalBoxRepository boxRepository;

    public GoalBox createBox(User user, GoalBoxCreateDTO dto) {
        GoalBox box = new GoalBox();
        box.setName(dto.name());
        box.setDescription(dto.description());
        box.setTargetAmount(dto.targetAmount());
        box.setUser(user);

        return boxRepository.save(box);
    }

    public List<GoalBoxResponseDTO> getUserBoxes(User user) {
        return boxRepository.findByUser(user)
                .stream()
                .map(GoalBoxResponseDTO::from)
                .toList();
    }

    public GoalBox getBox(User user, Long boxId) {
        return boxRepository.findByIdAndUser(boxId, user)
                .orElseThrow(() -> new GoalBoxNotFoundException(boxId));
    }

    @Transactional
    public GoalBox deposit(User user, Long boxId, BigDecimal amount) {
        GoalBox box = getBox(user, boxId);
        Wallet wallet = user.getWallet();

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        box.setCurrentBalance(box.getCurrentBalance().add(amount));
        wallet.setBalance(wallet.getBalance().subtract(amount));

        return boxRepository.save(box);
    }

    @Transactional
    public void deleteBox(User user, Long boxId) {
        GoalBox box = getBox(user, boxId);

        user.getWallet().setBalance(user.getWallet().getBalance().add(box.getCurrentBalance()));

        boxRepository.delete(box);
    }

    @Transactional
    public GoalBox withdraw(User user, Long boxId, BigDecimal amount) {
        GoalBox box = getBox(user, boxId);
        Wallet wallet = user.getWallet();

        if (box.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        box.setCurrentBalance(box.getCurrentBalance().subtract(amount));
        wallet.setBalance(wallet.getBalance().add(amount));

        return boxRepository.save(box);
    }

    public GoalBox updateBox(User user, Long boxId, GoalBoxUpdateDTO dto) {
        GoalBox box = getBox(user, boxId);

        if (dto.name() != null) box.setName(dto.name());
        if (dto.description() != null) box.setDescription(dto.description());
        if (dto.targetAmount() != null) box.setTargetAmount(dto.targetAmount());

        return boxRepository.save(box);
    }
}