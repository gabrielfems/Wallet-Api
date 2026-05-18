package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxResponseDTO;
import com.walletapi.demo.application.dto.GoalBoxUpdateDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.repositories.GoalBoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalBoxServiceTest {

    @InjectMocks
    private GoalBoxService goalBoxService;

    @Mock
    private GoalBoxRepository boxRepository;

    private User user;
    private Wallet wallet;
    private GoalBox box;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        user = new User();
        user.setId(1L);
        user.setName("Gabriel");
        user.setWallet(wallet);

        box = new GoalBox();
        box.setId(10L);
        box.setName("Viagem");
        box.setDescription("Férias em Floripa");
        box.setTargetAmount(new BigDecimal("2000.00"));
        box.setCurrentBalance(new BigDecimal("500.00"));
        box.setUser(user);
    }

    @Test
    @DisplayName("createBox: deve salvar e retornar a caixinha criada quando dados são válidos")
    void createBox_dadosValidos_retornaCaixinhaCriada() {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "Férias em Floripa", new BigDecimal("2000.00"));

        when(boxRepository.save(any(GoalBox.class))).thenReturn(box);

        GoalBox result = goalBoxService.createBox(user, dto);

        assertThat(result.getName()).isEqualTo("Viagem");
        assertThat(result.getTargetAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getUser()).isEqualTo(user);
        verify(boxRepository).save(any(GoalBox.class));
    }

    @Test
    @DisplayName("getUserBoxes: deve retornar lista de DTOs das caixinhas do usuário")
    void getUserBoxes_caixinhasExistem_retornaLista() {
        when(boxRepository.findByUser(user)).thenReturn(List.of(box));

        List<GoalBoxResponseDTO> result = goalBoxService.getUserBoxes(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Viagem");
        verify(boxRepository).findByUser(user);
    }

    @Test
    @DisplayName("getUserBoxes: deve retornar lista vazia quando usuário não tem caixinhas")
    void getUserBoxes_semCaixinhas_retornaListaVazia() {
        when(boxRepository.findByUser(user)).thenReturn(List.of());

        List<GoalBoxResponseDTO> result = goalBoxService.getUserBoxes(user);

        assertThat(result).isEmpty();
        verify(boxRepository).findByUser(user);
    }

    @Test
    @DisplayName("getBox: deve retornar a caixinha quando ela pertence ao usuário")
    void getBox_caixinhaExiste_retornaCaixinha() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));

        GoalBox result = goalBoxService.getBox(user, 10L);

        assertThat(result).isEqualTo(box);
        verify(boxRepository).findByIdAndUser(10L, user);
    }

    @Test
    @DisplayName("getBox: deve lançar GoalBoxNotFoundException quando a caixinha não existe ou não pertence ao usuário")
    void getBox_caixinhaNaoEncontrada_lancaGoalBoxNotFoundException() {
        when(boxRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalBoxService.getBox(user, 99L))
                .isInstanceOf(GoalBoxNotFoundException.class);

        verify(boxRepository).findByIdAndUser(99L, user);
    }

    @Test
    @DisplayName("deposit: deve adicionar valor à caixinha e subtrair da carteira quando saldo é suficiente")
    void deposit_saldoSuficiente_atualizaSaldos() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(GoalBox.class))).thenReturn(box);

        goalBoxService.deposit(user, 10L, new BigDecimal("200.00"));

        assertThat(box.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(boxRepository).save(box);
    }

    @Test
    @DisplayName("deposit: deve lançar InsufficientBalanceException quando saldo da carteira é insuficiente")
    void deposit_saldoInsuficiente_lancaInsufficientBalanceException() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> goalBoxService.deposit(user, 10L, new BigDecimal("9999.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("deposit: deve lançar GoalBoxNotFoundException quando caixinha não existe")
    void deposit_caixinhaNaoEncontrada_lancaGoalBoxNotFoundException() {
        when(boxRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalBoxService.deposit(user, 99L, new BigDecimal("100.00")))
                .isInstanceOf(GoalBoxNotFoundException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("withdraw: deve subtrair valor da caixinha e adicionar à carteira quando saldo é suficiente")
    void withdraw_saldoSuficiente_atualizaSaldos() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(GoalBox.class))).thenReturn(box);

        goalBoxService.withdraw(user, 10L, new BigDecimal("200.00"));

        assertThat(box.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("1200.00"));
        verify(boxRepository).save(box);
    }

    @Test
    @DisplayName("withdraw: deve lançar InsufficientBalanceException quando saldo da caixinha é insuficiente")
    void withdraw_saldoInsuficiente_lancaInsufficientBalanceException() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));

        assertThatThrownBy(() -> goalBoxService.withdraw(user, 10L, new BigDecimal("9999.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("withdraw: deve lançar GoalBoxNotFoundException quando caixinha não existe")
    void withdraw_caixinhaNaoEncontrada_lancaGoalBoxNotFoundException() {
        when(boxRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalBoxService.withdraw(user, 99L, new BigDecimal("100.00")))
                .isInstanceOf(GoalBoxNotFoundException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteBox: deve retornar saldo da caixinha à carteira e deletar a caixinha")
    void deleteBox_caixinhaExiste_retornaSaldoEDeleta() {
        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));

        goalBoxService.deleteBox(user, 10L);

        // saldo da caixinha (500.00) deve ser devolvido à carteira (1000.00)
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(boxRepository).delete(box);
    }

    @Test
    @DisplayName("deleteBox: deve lançar GoalBoxNotFoundException quando caixinha não existe")
    void deleteBox_caixinhaNaoEncontrada_lancaGoalBoxNotFoundException() {
        when(boxRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalBoxService.deleteBox(user, 99L))
                .isInstanceOf(GoalBoxNotFoundException.class);

        verify(boxRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateBox: deve atualizar apenas os campos não nulos informados no DTO")
    void updateBox_camposValidos_atualizaApenasOsInformados() {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("Novo Nome", null, new BigDecimal("3000.00"));

        when(boxRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(GoalBox.class))).thenReturn(box);

        GoalBox result = goalBoxService.updateBox(user, 10L, dto);

        assertThat(result.getName()).isEqualTo("Novo Nome");
        assertThat(result.getDescription()).isEqualTo("Férias em Floripa"); // não foi alterado
        assertThat(result.getTargetAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        verify(boxRepository).save(box);
    }

    @Test
    @DisplayName("updateBox: deve lançar GoalBoxNotFoundException quando caixinha não existe")
    void updateBox_caixinhaNaoEncontrada_lancaGoalBoxNotFoundException() {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("Novo Nome", null, null);

        when(boxRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalBoxService.updateBox(user, 99L, dto))
                .isInstanceOf(GoalBoxNotFoundException.class);

        verify(boxRepository, never()).save(any());
    }
}