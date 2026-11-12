package br.com.estapar.poc.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RechargeTagRequest(
        @NotNull(message = "O valor da recarga e obrigatorio")
        @Positive(message = "O valor da recarga deve ser maior que zero")
        BigDecimal amount
) {
}
