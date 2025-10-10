package com.paymybuddy.app.transaction;

import com.paymybuddy.app.bankaccounts.BankAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Date;

@Entity(name = "transaction")
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", foreignKey = @ForeignKey(name = "FK_TRANSACTION_SENDER"))
    private BankAccount sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", foreignKey = @ForeignKey(name = "FK_TRANSACTION_RECEIVER"))
    private BankAccount receiver;

    private Double amountSent;
    private Double amountReceived;
    private String description;
    private String currency = "€";
    private Date date;

    public Transaction() {
    }

    public Transaction(Long id, BankAccount sender, BankAccount receiver, Double amountSent, Double amountReceived, String description, Date date, String currency) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amountSent = amountSent;
        this.amountReceived = amountReceived;
        this.description = description;
        this.date = date;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankAccount getSender() {
        return sender;
    }

    public void setSender(BankAccount sender) {
        this.sender = sender;
    }

    public BankAccount getReceiver() {
        return receiver;
    }

    public void setReceiver(BankAccount receiver) {
        this.receiver = receiver;
    }

    public boolean isSender(Long id) {
        return sender.getId().equals(id);
    }

    private String _getFormattedAmount(Double amount) {
        return String.format("%.2f %s", amount, currency);
    }

    public String getFormattedAmountSent() {
        return "-" + _getFormattedAmount(amountSent);
    }

    public String getFormattedAmountReceived() {
        return "+" + _getFormattedAmount(amountReceived);
    }

    public Double getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(Double amount) {
        amountSent = amount;
    }

    public Double getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(Double amount) {
        amountReceived = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
