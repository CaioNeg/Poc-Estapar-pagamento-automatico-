package br.com.estapar.poc.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTagRequest(
        @NotNull(message = "O ID do cliente e obrigatorio")
        Long customerId,

        @NotBlank(message = "O codigo da Tag Zul+ e obrigatorio")
        @Size(max = 50, message = "O codigo da Tag deve ter no maximo 50 caracteres")
        String tagCode,

        @NotBlank(message = "A placa do veiculo e obrigatoria")
        @Size(min = 7, max = 10, message = "A placa deve ter entre 7 e 10 caracteres")
        String licensePlate,

        @PositiveOrZero(message = "O saldo inicial nao pode ser negativo")
        BigDecimal initialBalance
) {
}
