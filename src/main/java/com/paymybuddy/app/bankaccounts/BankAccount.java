package com.paymybuddy.app.bankaccounts;

import com.paymybuddy.app.transaction.Transaction;
import com.paymybuddy.app.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.List;

@Entity(name = "bank_account")
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_BANK_ACCOUNT_USER"))
    private User user;

    private Double balance;

    @OneToMany(mappedBy = "sender", fetch = FetchType.EAGER)
    private List<Transaction> sent;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER)
    private List<Transaction> received;

    public BankAccount() {}

    public BankAccount(User user, Double balance) {
        this.user = user;
        this.balance = balance;
    }

    public BankAccount(Long id, User user, Double balance) {
        this.id = id;
        this.user = user;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Transaction> getSent() {
        return sent;
    }

    public void setSent(List<Transaction> sent) {
        this.sent = sent;
    }

    public List<Transaction> getReceived() {
        return received;
    }

    public void setReceived(List<Transaction> received) {
        this.received = received;
    }
}
