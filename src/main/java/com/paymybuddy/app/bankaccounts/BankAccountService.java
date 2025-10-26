package com.paymybuddy.app.bankaccounts;

import com.paymybuddy.app.security.JwtService;
import com.paymybuddy.app.transaction.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BankAccountService {
    private final BankAccountRepository repo;

    public BankAccountService(BankAccountRepository repo) {
        this.repo = repo;
    }

    public BankAccount getLoggedUserBankAccount() {
        return getBankAccountByUserId(JwtService.getLoggedUserId());
    }

    public BankAccount getBankAccountByUserId(Long userId) {
        return repo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Bank account not found for userId: " + userId));
    }

    @Transactional
    public void saveTransaction(Transaction transaction) {
        BankAccount sender = transaction.getSender(),
                    receiver = transaction.getReceiver();

        if (sender.getBalance() < transaction.getAmountSent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant saisi supérieur à la balance du compte");
        }

        sender.setBalance(sender.getBalance() - transaction.getAmountSent());
        receiver.setBalance(receiver.getBalance() + transaction.getAmountReceived());

        repo.saveAll(List.of(sender, receiver));
    }
}
