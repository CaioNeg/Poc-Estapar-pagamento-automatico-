package br.com.estapar.poc.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagTest {

    private Customer customer;
    private Tag tag;

    @BeforeEach
    void setUp() {
        customer = new Customer("Carlos Motorista", "carlos@estapar.com.br", "12345678901", "hash123");
        tag = new Tag("ZUL-0001", "abc1d23", customer, new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Deve normalizar a placa para maiusculo e iniciar como ACTIVE")
    void shouldNormalizeLicensePlateAndStartActive() {
        assertEquals("ABC1D23", tag.getLicensePlate());
        assertEquals(TagStatus.ACTIVE, tag.getStatus());
        assertEquals(new BigDecimal("50.00"), tag.getBalance());
    }

    @Test
    @DisplayName("Deve creditar saldo com sucesso quando valor for positivo")
    void shouldCreditBalanceSuccessfully() {
        tag.creditBalance(new BigDecimal("25.50"));
        assertEquals(new BigDecimal("75.50"), tag.getBalance());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar creditar valor zero ou negativo")
    void shouldThrowExceptionWhenCreditingInvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> tag.creditBalance(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> tag.creditBalance(new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Deve autorizar e debitar passagem quando Tag estiver ativa e com saldo suficiente")
    void shouldAuthorizeAndDebitPassageWhenActiveAndSufficientBalance() {
        BigDecimal tollAmount = new BigDecimal("18.40");

        assertTrue(tag.canAuthorizePassage(tollAmount));
        tag.debitPassage(tollAmount);

        assertEquals(new BigDecimal("31.60"), tag.getBalance());
    }

    @Test
    @DisplayName("Nao deve autorizar passagem quando saldo for insuficiente")
    void shouldNotAuthorizeWhenBalanceIsInsufficient() {
        BigDecimal expensiveParking = new BigDecimal("60.00");

        assertFalse(tag.canAuthorizePassage(expensiveParking));
        assertThrows(IllegalStateException.class, () -> tag.debitPassage(expensiveParking));
    }

    @Test
    @DisplayName("Nao deve autorizar passagem quando Tag estiver bloqueada mesmo com saldo")
    void shouldNotAuthorizeWhenTagIsBlocked() {
        tag.block();

        assertFalse(tag.canAuthorizePassage(new BigDecimal("10.00")));
        assertThrows(IllegalStateException.class, () -> tag.debitPassage(new BigDecimal("10.00")));
    }
}
