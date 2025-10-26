package com.paymybuddy.app.integration;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.bankaccounts.BankAccountRepository;
import com.paymybuddy.app.bankaccounts.BankAccountService;
import com.paymybuddy.app.transaction.Transaction;
import com.paymybuddy.app.transaction.TransactionRepository;
import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BankAccountIntegrationTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;
    private BankAccount account1;
    private BankAccount account2;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer le premier utilisateur avec un compte bancaire
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setProvider("local");

        account1 = new BankAccount(user1, 1000.0);
        user1.setBankAccount(account1);
        user1 = userRepository.save(user1);
        account1 = user1.getBankAccount();

        // Créer le deuxième utilisateur avec un compte bancaire
        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setProvider("local");

        account2 = new BankAccount(user2, 500.0);
        user2.setBankAccount(account2);
        user2 = userRepository.save(user2);
        account2 = user2.getBankAccount();
    }

    @Test
    void testBankAccountCreation() {
        assertNotNull(account1.getId());
        assertNotNull(account2.getId());
        assertEquals(1000.0, account1.getBalance(), 0.01);
        assertEquals(500.0, account2.getBalance(), 0.01);
    }

    @Test
    void testGetBankAccountByUserId() {
        BankAccount foundAccount = bankAccountService.getBankAccountByUserId(user1.getId());

        assertNotNull(foundAccount);
        assertEquals(account1.getId(), foundAccount.getId());
        assertEquals(user1.getId(), foundAccount.getUser().getId());
    }

    @Test
    void testGetBankAccountByUserIdNotFound() {
        assertThrows(ResponseStatusException.class, () -> {
            bankAccountService.getBankAccountByUserId(999L);
        });
    }

    @Test
    void testSuccessfulTransaction() {
        Transaction transaction = new Transaction();
        transaction.setSender(account1);
        transaction.setReceiver(account2);
        transaction.setAmountSent(100.0);
        transaction.setAmountReceived(99.5);
        transaction.setDescription("Test transaction");
        transaction.setDate(new Date());

        double initialBalance1 = account1.getBalance();
        double initialBalance2 = account2.getBalance();

        bankAccountService.saveTransaction(transaction);

        // Recharger les comptes depuis la base de données
        account1 = bankAccountRepository.findById(account1.getId()).get();
        account2 = bankAccountRepository.findById(account2.getId()).get();

        assertEquals(initialBalance1 - 100.0, account1.getBalance(), 0.01);
        assertEquals(initialBalance2 + 99.5, account2.getBalance(), 0.01);
    }

    @Test
    void testTransactionWithInsufficientFunds() {
        Transaction transaction = new Transaction();
        transaction.setSender(account1);
        transaction.setReceiver(account2);
        transaction.setAmountSent(2000.0); // Plus que la balance disponible
        transaction.setAmountReceived(1990.0);
        transaction.setDescription("Test transaction");
        transaction.setDate(new Date());

        assertThrows(ResponseStatusException.class, () -> {
            bankAccountService.saveTransaction(transaction);
        });

        // Vérifier que les balances n'ont pas changé
        account1 = bankAccountRepository.findById(account1.getId()).get();
        account2 = bankAccountRepository.findById(account2.getId()).get();

        assertEquals(1000.0, account1.getBalance(), 0.01);
        assertEquals(500.0, account2.getBalance(), 0.01);
    }

    @Test
    void testMultipleTransactions() {
        // Première transaction
        Transaction transaction1 = new Transaction();
        transaction1.setSender(account1);
        transaction1.setReceiver(account2);
        transaction1.setAmountSent(100.0);
        transaction1.setAmountReceived(99.5);
        transaction1.setDescription("First transaction");
        transaction1.setDate(new Date());
        bankAccountService.saveTransaction(transaction1);

        // Deuxième transaction
        Transaction transaction2 = new Transaction();
        transaction2.setSender(account1);
        transaction2.setReceiver(account2);
        transaction2.setAmountSent(50.0);
        transaction2.setAmountReceived(49.75);
        transaction2.setDescription("Second transaction");
        transaction2.setDate(new Date());
        bankAccountService.saveTransaction(transaction2);

        // Vérifier les balances finales
        account1 = bankAccountRepository.findById(account1.getId()).get();
        account2 = bankAccountRepository.findById(account2.getId()).get();

        assertEquals(1000.0 - 100.0 - 50.0, account1.getBalance(), 0.01);
        assertEquals(500.0 + 99.5 + 49.75, account2.getBalance(), 0.01);
    }

    @Test
    void testBankAccountUserRelationship() {
        assertEquals(user1.getId(), account1.getUser().getId());
        assertEquals(account1.getId(), user1.getBankAccount().getId());
    }

    @Test
    void testUpdateBankAccountBalance() {
        account1.setBalance(2000.0);
        bankAccountRepository.save(account1);

        BankAccount updatedAccount = bankAccountRepository.findById(account1.getId()).get();
        assertEquals(2000.0, updatedAccount.getBalance(), 0.01);
    }

    @Test
    void testTransactionHistoryTracking() {
        // Créer plusieurs transactions
        for (int i = 0; i < 3; i++) {
            Transaction transaction = new Transaction();
            transaction.setSender(account1);
            transaction.setReceiver(account2);
            transaction.setAmountSent(10.0);
            transaction.setAmountReceived(9.95);
            transaction.setDescription("Transaction " + i);
            transaction.setDate(new Date());

            bankAccountService.saveTransaction(transaction);
            transactionRepository.save(transaction);
        }

        account1 = bankAccountRepository.findById(account1.getId()).orElseThrow();
        account2 = bankAccountRepository.findById(account2.getId()).orElseThrow();

        assertNotNull(account1.getSent());
        assertNotNull(account2.getReceived());
        assertTrue(account1.getSent().size() >= 3);
        assertTrue(account2.getReceived().size() >= 3);
    }

    @Test
    void testBankAccountBalanceConstraints() {
        // Test avec balance négative (devrait être rejeté par la logique métier)
        Transaction transaction = new Transaction();
        transaction.setSender(account1);
        transaction.setReceiver(account2);
        transaction.setAmountSent(1001.0);
        transaction.setAmountReceived(995.99);
        transaction.setDescription("Overdraft test");
        transaction.setDate(new Date());

        assertThrows(ResponseStatusException.class, () -> {
            bankAccountService.saveTransaction(transaction);
        });
    }

    @Test
    void testBankAccountCascadeOperations() {
        // Supprimer un utilisateur devrait supprimer son compte bancaire
        Long accountId = account1.getId();
        userRepository.delete(user1);

        assertFalse(bankAccountRepository.findById(accountId).isPresent());
    }

    @Test
    void testZeroAmountTransaction() {
        Transaction transaction = new Transaction();
        transaction.setSender(account1);
        transaction.setReceiver(account2);
        transaction.setAmountSent(0.0);
        transaction.setAmountReceived(0.0);
        transaction.setDescription("Zero amount test");
        transaction.setDate(new Date());

        bankAccountService.saveTransaction(transaction);

        // Les balances ne devraient pas changer
        account1 = bankAccountRepository.findById(account1.getId()).get();
        account2 = bankAccountRepository.findById(account2.getId()).get();

        assertEquals(1000.0, account1.getBalance(), 0.01);
        assertEquals(500.0, account2.getBalance(), 0.01);
    }

    @Test
    void testBidirectionalTransactions() {
        // Transaction de user1 vers user2
        Transaction transaction1 = new Transaction();
        transaction1.setSender(account1);
        transaction1.setReceiver(account2);
        transaction1.setAmountSent(100.0);
        transaction1.setAmountReceived(99.5);
        transaction1.setDescription("User1 to User2");
        transaction1.setDate(new Date());
        bankAccountService.saveTransaction(transaction1);

        // Transaction de user2 vers user1
        Transaction transaction2 = new Transaction();
        transaction2.setSender(account2);
        transaction2.setReceiver(account1);
        transaction2.setAmountSent(50.0);
        transaction2.setAmountReceived(49.75);
        transaction2.setDescription("User2 to User1");
        transaction2.setDate(new Date());
        bankAccountService.saveTransaction(transaction2);

        // Vérifier les balances finales
        account1 = bankAccountRepository.findById(account1.getId()).get();
        account2 = bankAccountRepository.findById(account2.getId()).get();

        assertEquals(1000.0 - 100.0 + 49.75, account1.getBalance(), 0.01);
        assertEquals(500.0 + 99.5 - 50.0, account2.getBalance(), 0.01);
    }
}
