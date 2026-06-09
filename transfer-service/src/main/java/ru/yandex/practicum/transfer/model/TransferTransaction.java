package ru.yandex.practicum.transfer.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_transactions", schema = "transfer_schema")
public class TransferTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_login", nullable = false, length = 50)
    private String fromLogin;

    @Column(name = "to_login", nullable = false, length = 50)
    private String toLogin;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "from_balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal fromBalanceBefore;

    @Column(name = "from_balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal fromBalanceAfter;

    @Column(name = "to_balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal toBalanceBefore;

    @Column(name = "to_balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal toBalanceAfter;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TransferTransaction() {}

    public TransferTransaction(String fromLogin, String toLogin, BigDecimal amount,
                               BigDecimal fromBalanceBefore, BigDecimal fromBalanceAfter,
                               BigDecimal toBalanceBefore, BigDecimal toBalanceAfter,
                               String status) {
        this.fromLogin = fromLogin;
        this.toLogin = toLogin;
        this.amount = amount;
        this.fromBalanceBefore = fromBalanceBefore;
        this.fromBalanceAfter = fromBalanceAfter;
        this.toBalanceBefore = toBalanceBefore;
        this.toBalanceAfter = toBalanceAfter;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromLogin() { return fromLogin; }
    public void setFromLogin(String fromLogin) { this.fromLogin = fromLogin; }

    public String getToLogin() { return toLogin; }
    public void setToLogin(String toLogin) { this.toLogin = toLogin; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFromBalanceBefore() { return fromBalanceBefore; }
    public void setFromBalanceBefore(BigDecimal fromBalanceBefore) { this.fromBalanceBefore = fromBalanceBefore; }

    public BigDecimal getFromBalanceAfter() { return fromBalanceAfter; }
    public void setFromBalanceAfter(BigDecimal fromBalanceAfter) { this.fromBalanceAfter = fromBalanceAfter; }

    public BigDecimal getToBalanceBefore() { return toBalanceBefore; }
    public void setToBalanceBefore(BigDecimal toBalanceBefore) { this.toBalanceBefore = toBalanceBefore; }

    public BigDecimal getToBalanceAfter() { return toBalanceAfter; }
    public void setToBalanceAfter(BigDecimal toBalanceAfter) { this.toBalanceAfter = toBalanceAfter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}