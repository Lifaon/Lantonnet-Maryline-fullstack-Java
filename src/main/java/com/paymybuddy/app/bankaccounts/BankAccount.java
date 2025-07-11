package com.paymybuddy.app.bankaccounts;

import com.paymybuddy.app.transaction.Transaction;
import com.paymybuddy.app.user.User;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class BankAccount {

    @Id
    @GeneratedValue
    private Long id;

    private String iban;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_BANK_ACCOUNT_USER"))
    private User user;

    private Double balance;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> sent;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> received;

    public BankAccount() {}

    public BankAccount(Long id, String iban, User user, Double balance) {
        this.id = id;
        this.iban = iban;
        this.user = user;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
