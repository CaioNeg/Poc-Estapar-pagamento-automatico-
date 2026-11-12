package br.com.estapar.poc.integration.conectcar;

import br.com.estapar.poc.domain.model.PassageType;

import java.math.BigDecimal;
import java.time.Instant;

public record ConectCarPassageEvent(
        Long transactionId,
        String tagCode,
        String licensePlate,
        PassageType passageType,
        String locationName,
        BigDecimal amount,
        Instant occurredAt
) {
}
