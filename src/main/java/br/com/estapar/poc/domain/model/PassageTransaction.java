package br.com.estapar.poc.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "passage_transactions")
public class PassageTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "passage_type", nullable = false, length = 20)
    private PassageType passageType;

    @Column(name = "location_name", nullable = false, length = 150)
    private String locationName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PassageStatus status;

    @Column(name = "external_conectcar_id", length = 100)
    private String externalConectcarId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected PassageTransaction() {
    }

    public PassageTransaction(Tag tag, PassageType passageType, String locationName, BigDecimal amount, PassageStatus status) {
        this.tag = tag;
        this.passageType = passageType;
        this.locationName = locationName;
        this.amount = amount;
        this.status = status;
        this.occurredAt = Instant.now();
    }

    public void markApprovedByConectCar(String externalConectcarId) {
        this.status = PassageStatus.APPROVED;
        this.externalConectcarId = externalConectcarId;
    }

    public void markRejected() {
        this.status = PassageStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public Tag getTag() {
        return tag;
    }

    public PassageType getPassageType() {
        return passageType;
    }

    public String getLocationName() {
        return locationName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PassageStatus getStatus() {
        return status;
    }

    public String getExternalConectcarId() {
        return externalConectcarId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
