package ru.yandex.practicum.transfer.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "from_balance_before", nullable = false)
    private Integer fromBalanceBefore;

    @Column(name = "from_balance_after", nullable = false)
    private Integer fromBalanceAfter;

    @Column(name = "to_balance_before", nullable = false)
    private Integer toBalanceBefore;

    @Column(name = "to_balance_after", nullable = false)
    private Integer toBalanceAfter;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TransferTransaction() {}

    public TransferTransaction(String fromLogin, String toLogin, Integer amount,
                               Integer fromBalanceBefore, Integer fromBalanceAfter,
                               Integer toBalanceBefore, Integer toBalanceAfter,
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

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getFromBalanceBefore() { return fromBalanceBefore; }
    public void setFromBalanceBefore(Integer fromBalanceBefore) { this.fromBalanceBefore = fromBalanceBefore; }

    public Integer getFromBalanceAfter() { return fromBalanceAfter; }
    public void setFromBalanceAfter(Integer fromBalanceAfter) { this.fromBalanceAfter = fromBalanceAfter; }

    public Integer getToBalanceBefore() { return toBalanceBefore; }
    public void setToBalanceBefore(Integer toBalanceBefore) { this.toBalanceBefore = toBalanceBefore; }

    public Integer getToBalanceAfter() { return toBalanceAfter; }
    public void setToBalanceAfter(Integer toBalanceAfter) { this.toBalanceAfter = toBalanceAfter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}