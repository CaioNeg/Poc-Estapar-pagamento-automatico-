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
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", nullable = false, unique = true, length = 50)
    private String tagCode;

    @Column(name = "license_plate", nullable = false, unique = true, length = 10)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Tag() {
    }

    public Tag(String tagCode, String licensePlate, Customer customer, BigDecimal initialBalance) {
        this.tagCode = tagCode;
        this.licensePlate = licensePlate.toUpperCase();
        this.customer = customer;
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.status = TagStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void creditBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor de recarga deve ser maior que zero.");
        }
        this.balance = this.balance.add(amount);
    }

    public boolean canAuthorizePassage(BigDecimal amount) {
        if (this.status != TagStatus.ACTIVE) {
            return false;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return this.balance.compareTo(amount) >= 0;
    }

    public void debitPassage(BigDecimal amount) {
        if (!canAuthorizePassage(amount)) {
            throw new IllegalStateException("Tag inativa ou saldo insuficiente para autorizar a passagem.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void block() {
        this.status = TagStatus.BLOCKED;
    }

    public void activate() {
        this.status = TagStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getTagCode() {
        return tagCode;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public TagStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
