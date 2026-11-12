package br.com.estapar.poc.api.dto;

import br.com.estapar.poc.domain.model.PassageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegisterPassageRequest(
        @NotBlank(message = "O codigo da Tag e obrigatorio")
        String tagCode,

        @NotNull(message = "O tipo de passagem (PARKING ou TOLL) e obrigatorio")
        PassageType passageType,

        @NotBlank(message = "O nome do local (estacionamento ou praca de pedagio) e obrigatorio")
        String locationName,

        @NotNull(message = "O valor da tarifa e obrigatorio")
        @Positive(message = "O valor da tarifa deve ser maior que zero")
        BigDecimal amount
) {
}
