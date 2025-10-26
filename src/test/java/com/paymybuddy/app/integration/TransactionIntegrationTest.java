package com.paymybuddy.app.integration;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.bankaccounts.BankAccountRepository;
import com.paymybuddy.app.security.JwtService;
import com.paymybuddy.app.transaction.Transaction;
import com.paymybuddy.app.transaction.TransactionRepository;
import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRelation;
import com.paymybuddy.app.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User sender;
    private User receiver;
    private Cookie authCookie;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer l'utilisateur émetteur
        sender = new User();
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setPassword(passwordEncoder.encode("password123"));
        sender.setProvider("local");

        BankAccount senderAccount = new BankAccount(sender, 1000.0);
        sender.setBankAccount(senderAccount);
        sender = userRepository.save(sender);

        // Créer l'utilisateur récepteur
        receiver = new User();
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPassword(passwordEncoder.encode("password123"));
        receiver.setProvider("local");

        BankAccount receiverAccount = new BankAccount(receiver, 500.0);
        receiver.setBankAccount(receiverAccount);
        receiver = userRepository.save(receiver);

        // Créer la relation
        UserRelation relation = new UserRelation(sender, receiver);
        sender.getRelations().add(relation);
        sender = userRepository.save(sender);

        // Créer le token JWT
        String token = jwtService.generateToken(sender.toUserDetails());
        authCookie = new Cookie("authToken", token);
    }

    @Test
    void testGetTransactionPage() throws Exception {
        mockMvc.perform(get("/transfert")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/base"))
                .andExpect(model().attributeExists("transactionForm"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("latestTransactions"))
                .andExpect(model().attributeExists("bankAccountId"));
    }

    @Test
    void testSuccessfulTransaction() throws Exception {
        double initialSenderBalance = sender.getBankAccount().getBalance();
        double initialReceiverBalance = receiver.getBankAccount().getBalance();
        double amount = 100.0;

        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "Test transaction")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfert?success=1"));

        // Vérifier les balances
        BankAccount updatedSenderAccount = bankAccountRepository.findById(sender.getBankAccount().getId()).get();
        BankAccount updatedReceiverAccount = bankAccountRepository.findById(receiver.getBankAccount().getId()).get();

        assertEquals(initialSenderBalance - amount, updatedSenderAccount.getBalance(), 0.01);
        assertEquals(initialReceiverBalance + (amount * 0.995), updatedReceiverAccount.getBalance(), 0.01);

        // Vérifier que la transaction a été créée
        assertEquals(1, transactionRepository.count());
    }

    @Test
    void testTransactionWithInsufficientFunds() throws Exception {
        double amount = 2000.0; // Plus que la balance disponible

        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "Test transaction")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/base"))
                .andExpect(model().attributeExists("errorMessage"));

        // Vérifier qu'aucune transaction n'a été créée
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void testTransactionWithNonExistentRelation() throws Exception {
        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "nonexistent")
                        .param("description", "Test transaction")
                        .param("amount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/base"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testTransactionWithInvalidAmount() throws Exception {
        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "Test transaction")
                        .param("amount", "-50.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/base"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testTransactionWithEmptyDescription() throws Exception {
        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "")
                        .param("amount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/base"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testTransactionPageWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/transfert"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMultipleTransactions() throws Exception {
        // Première transaction
        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "First transaction")
                        .param("amount", "50.0"))
                .andExpect(status().is3xxRedirection());

        // Deuxième transaction
        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "Second transaction")
                        .param("amount", "30.0"))
                .andExpect(status().is3xxRedirection());

        // Vérifier qu'il y a bien 2 transactions
        assertEquals(2, transactionRepository.count());

        // Vérifier la balance finale
        BankAccount updatedSenderAccount = bankAccountRepository.findById(sender.getBankAccount().getId()).get();
        assertEquals(1000.0 - 50.0 - 30.0, updatedSenderAccount.getBalance(), 0.01);
    }

    @Test
    void testTransactionFeeCalculation() throws Exception {
        double amount = 100.0;
        double expectedReceived = amount * 0.995;

        mockMvc.perform(post("/transfert")
                        .cookie(authCookie)
                        .param("relation", "receiver")
                        .param("description", "Fee test")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().is3xxRedirection());

        Transaction transaction = transactionRepository.findAll().get(0);
        assertEquals(amount, transaction.getAmountSent(), 0.01);
        assertEquals(expectedReceived, transaction.getAmountReceived(), 0.01);
    }
}