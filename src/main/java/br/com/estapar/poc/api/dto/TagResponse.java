package br.com.estapar.poc.api.dto;

import br.com.estapar.poc.domain.model.Tag;
import br.com.estapar.poc.domain.model.TagStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TagResponse(
        Long id,
        String tagCode,
        String licensePlate,
        Long customerId,
        BigDecimal balance,
        TagStatus status,
        Instant createdAt
) {
    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getTagCode(),
                tag.getLicensePlate(),
                tag.getCustomer().getId(),
                tag.getBalance(),
                tag.getStatus(),
                tag.getCreatedAt()
        );
    }
}
