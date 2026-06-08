package ru.yandex.practicum.cash.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_transactions", schema = "cash_schema")
public class CashTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_login", nullable = false, length = 50)
    private String userLogin;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 10)
    private String action; // PUT or GET

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CashTransaction() {}

    public CashTransaction(String userLogin, Integer amount, String action,
                           Integer balanceBefore, Integer balanceAfter) {
        this.userLogin = userLogin;
        this.amount = amount;
        this.action = action;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Integer getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(Integer balanceBefore) { this.balanceBefore = balanceBefore; }

    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}