package com.paymybuddy.app.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.sound.midi.Receiver;
import java.util.Date;

public class TransactionDTO {

    static private final ObjectWriter ow = new ObjectMapper().writer();

    private Long id;
    private String description;
    private Double amount;
    private Date date;
    private String sender;
    private String receiver;

    public TransactionDTO() {
    }

    public TransactionDTO(Long id, String description, Double amount, Date date, String sender, String receiver) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
