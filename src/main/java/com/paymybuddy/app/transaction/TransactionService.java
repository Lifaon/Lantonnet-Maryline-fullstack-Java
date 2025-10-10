package com.paymybuddy.app.transaction;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.bankaccounts.BankAccountService;
import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRelation;
import com.paymybuddy.app.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repo;
    private final BankAccountService bankAccService;
    private final UserService userService;

    public TransactionService(TransactionRepository repo, BankAccountService bankAccService, UserService userService) {
        this.repo = repo;
        this.bankAccService = bankAccService;
        this.userService = userService;
    }

    public List<Transaction> getLatestTransactions() {
        BankAccount account = bankAccService.getLoggedUserBankAccount();
        List<Transaction> transactions = new ArrayList<>();

        transactions.addAll(account.getReceived());
        transactions.addAll(account.getSent());

        transactions.sort(Comparator.comparing(Transaction::getDate, Comparator.reverseOrder()));
        return transactions;
    }

    public Long getLoggedUserBankAccountId() {
        return bankAccService.getLoggedUserBankAccount().getId();
    }

    @Transactional
    public void newTransaction(TransactionForm form) {

        Transaction transaction = new Transaction();
        transaction.setAmountSent(form.getAmount());
        transaction.setAmountReceived(Math.round(form.getAmount() * 95.) / 100.);
        transaction.setDescription(form.getDescription());
        transaction.setDate(new Date());

        // TODO: reporter l'id directement depuis la page html ?
        BankAccount receiver = userService.getLoggedUserRelations().stream()
                .filter(contact -> contact.getContact().getUsername().equals(form.getRelation()))
                .findFirst()
                .map(UserRelation::getContact)
                .map(User::getBankAccount)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No relation with username " + form.getRelation()));

        transaction.setSender(bankAccService.getLoggedUserBankAccount());
        transaction.setReceiver(receiver);

        bankAccService.saveTransaction(transaction);
        repo.save(transaction);

    }

}
