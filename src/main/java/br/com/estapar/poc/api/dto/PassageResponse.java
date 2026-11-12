package br.com.estapar.poc.api.dto;

import br.com.estapar.poc.domain.model.PassageStatus;
import br.com.estapar.poc.domain.model.PassageTransaction;
import br.com.estapar.poc.domain.model.PassageType;

import java.math.BigDecimal;
import java.time.Instant;

public record PassageResponse(
        Long id,
        String tagCode,
        PassageType passageType,
        String locationName,
        BigDecimal amount,
        PassageStatus status,
        String externalConectcarId,
        Instant occurredAt
) {
    public static PassageResponse fromEntity(PassageTransaction tx) {
        return new PassageResponse(
                tx.getId(),
                tx.getTag().getTagCode(),
                tx.getPassageType(),
                tx.getLocationName(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getExternalConectcarId(),
                tx.getOccurredAt()
        );
    }
}
