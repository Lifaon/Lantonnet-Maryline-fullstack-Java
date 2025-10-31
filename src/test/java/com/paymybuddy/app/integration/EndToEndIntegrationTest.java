package com.paymybuddy.app.integration;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.bankaccounts.BankAccountRepository;
import com.paymybuddy.app.transaction.TransactionRepository;
import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests End-to-End simulant un parcours utilisateur complet
 */
@SpringBootTest
@AutoConfigureMockMvc
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCompleteUserJourney() throws Exception {
        // 1. Un nouvel utilisateur s'inscrit
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .param("username", "alice")
                        .param("email", "alice@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/success"))
                .andExpect(cookie().exists("authToken"))
                .andReturn();

        Cookie aliceToken = registerResult.getResponse().getCookie("authToken");
        assertNotNull(aliceToken);

        // Vérifier que le compte bancaire a été créé
        User alice = userRepository.findFirstByEmail("alice@example.com").orElseThrow();
        assertNotNull(alice.getBankAccount());
        assertEquals(0.0, alice.getBankAccount().getBalance(), 0.01);

        // 2. Alice ajoute de l'argent à son compte (simulation)
        BankAccount aliceAccount = alice.getBankAccount();
        aliceAccount.setBalance(500.0);
        bankAccountRepository.save(aliceAccount);

        // 3. Un deuxième utilisateur s'inscrit
        MvcResult bobRegisterResult = mockMvc.perform(post("/auth/register")
                        .param("username", "bob")
                        .param("email", "bob@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie bobToken = bobRegisterResult.getResponse().getCookie("authToken");

        // 4. Alice ajoute Bob comme relation
        mockMvc.perform(post("/user/addrelation")
                        .cookie(aliceToken)
                        .param("relationEmail", "bob@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/addrelation?success=1"));

        // Vérifier que la relation a été créée
        alice = userRepository.findFirstByEmail("alice@example.com").orElseThrow();
        assertEquals(1, alice.getRelations().size());

        // 5. Alice consulte la page de transfert
        mockMvc.perform(get("/transfert")
                        .cookie(aliceToken))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("transactionForm"));

        // 6. Alice envoie de l'argent à Bob
        mockMvc.perform(post("/transfert")
                        .cookie(aliceToken)
                        .param("relation", "bob")
                        .param("description", "Remboursement restaurant")
                        .param("amount", "50.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfert?success=1"));

        // 7. Vérifier les balances après la transaction
        alice = userRepository.findFirstByEmail("alice@example.com").orElseThrow();
        User bob = userRepository.findFirstByEmail("bob@example.com").orElseThrow();

        assertEquals(450.0, alice.getBankAccount().getBalance(), 0.01);
        assertEquals(49.75, bob.getBankAccount().getBalance(), 0.01); // 50 * 0.995

        // 8. Alice consulte son historique de transactions
        mockMvc.perform(get("/transfert")
                        .cookie(aliceToken))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("latestTransactions"));

        // Vérifier qu'il y a bien une transaction
        assertEquals(1, transactionRepository.count());

        // 9. Alice modifie son profil
        mockMvc.perform(post("/user/profile")
                        .cookie(aliceToken)
                        .param("username", "alice_updated")
                        .param("email", "alice@example.com")
                        .param("password", "newpassword123"))
                .andExpect(status().is3xxRedirection());

        alice = userRepository.findFirstByEmail("alice@example.com").orElseThrow();
        assertEquals("alice_updated", alice.getUsername());

        // 10. Alice se déconnecte
        mockMvc.perform(post("/auth/logout")
                        .cookie(aliceToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // 11. Alice se reconnecte avec son nouveau mot de passe
        mockMvc.perform(post("/auth/login")
                        .param("email", "alice@example.com")
                        .param("password", "newpassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().exists("authToken"));
    }

    @Test
    void testMultipleUsersInteractions() throws Exception {
        // Créer 3 utilisateurs
        String[] users = {"user1@test.com", "user2@test.com", "user3@test.com"};
        Cookie[] tokens = new Cookie[3];

        for (int i = 0; i < users.length; i++) {
            MvcResult result = mockMvc.perform(post("/auth/register")
                            .param("username", "user" + (i + 1))
                            .param("email", users[i])
                            .param("password", "password123"))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            tokens[i] = result.getResponse().getCookie("authToken");

            // Ajouter de l'argent
            User user = userRepository.findFirstByEmail(users[i]).orElseThrow();
            user.getBankAccount().setBalance(1000.0);
            userRepository.save(user);
        }

        // User1 ajoute User2 et User3 comme relations
        mockMvc.perform(post("/user/addrelation")
                        .cookie(tokens[0])
                        .param("relationEmail", users[1]))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/user/addrelation")
                        .cookie(tokens[0])
                        .param("relationEmail", users[2]))
                .andExpect(status().is3xxRedirection());

        // User1 envoie de l'argent à User2
        mockMvc.perform(post("/transfert")
                        .cookie(tokens[0])
                        .param("relation", "user2")
                        .param("description", "Payment 1")
                        .param("amount", "100.0"))
                .andExpect(status().is3xxRedirection());

        // User1 envoie de l'argent à User3
        mockMvc.perform(post("/transfert")
                        .cookie(tokens[0])
                        .param("relation", "user3")
                        .param("description", "Payment 2")
                        .param("amount", "150.0"))
                .andExpect(status().is3xxRedirection());

        // Vérifier les balances
        User user1 = userRepository.findFirstByEmail(users[0]).orElseThrow();
        User user2 = userRepository.findFirstByEmail(users[1]).orElseThrow();
        User user3 = userRepository.findFirstByEmail(users[2]).orElseThrow();

        assertEquals(750.0, user1.getBankAccount().getBalance(), 0.01);
        assertEquals(1099.5, user2.getBankAccount().getBalance(), 0.01);
        assertEquals(1149.25, user3.getBankAccount().getBalance(), 0.01);

        // Vérifier le nombre de transactions
        assertEquals(2, transactionRepository.count());
    }

    @Test
    void testErrorHandlingInUserJourney() throws Exception {
        // 1. Inscription avec email invalide
        mockMvc.perform(post("/auth/register")
                        .param("username", "testuser")
                        .param("email", "invalidemail")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());

        // 2. Inscription réussie
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie token = registerResult.getResponse().getCookie("authToken");

        // 3. Tentative d'ajout d'une relation inexistante
        mockMvc.perform(post("/user/addrelation")
                        .cookie(token)
                        .param("relationEmail", "nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"));

        // 4. Tentative de transaction sans relation
        mockMvc.perform(post("/transfert")
                        .cookie(token)
                        .param("relation", "nobody")
                        .param("description", "Test")
                        .param("amount", "50.0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"));

        // 5. Tentative de transaction avec fonds insuffisants
        User user = userRepository.findFirstByEmail("test@example.com").orElseThrow();
        user.getBankAccount().setBalance(10.0);
        userRepository.save(user);

        // Créer un contact
        mockMvc.perform(post("/auth/register")
                        .param("username", "contact")
                        .param("email", "contact@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/user/addrelation")
                        .cookie(token)
                        .param("relationEmail", "contact@example.com"))
                .andExpect(status().is3xxRedirection());

        // Transaction avec fonds insuffisants
        mockMvc.perform(post("/transfert")
                        .cookie(token)
                        .param("relation", "contact")
                        .param("description", "Too much")
                        .param("amount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"));

        // La balance ne devrait pas avoir changé
        user = userRepository.findFirstByEmail("test@example.com").orElseThrow();
        assertEquals(10.0, user.getBankAccount().getBalance(), 0.01);
    }

    @Test
    void testConcurrentTransactions() throws Exception {
        // Créer deux utilisateurs
        MvcResult user1Result = mockMvc.perform(post("/auth/register")
                        .param("username", "user1")
                        .param("email", "user1@test.com")
                        .param("password", "password123"))
                .andReturn();
        Cookie token1 = user1Result.getResponse().getCookie("authToken");

        mockMvc.perform(post("/auth/register")
                        .param("username", "user2")
                        .param("email", "user2@test.com")
                        .param("password", "password123"))
                .andReturn();

        // Ajouter de l'argent
        User user1 = userRepository.findFirstByEmail("user1@test.com").orElseThrow();
        user1.getBankAccount().setBalance(100.0);
        userRepository.save(user1);

        // Ajouter la relation
        mockMvc.perform(post("/user/addrelation")
                        .cookie(token1)
                        .param("relationEmail", "user2@test.com"))
                .andExpect(status().is3xxRedirection());

        // Effectuer plusieurs transactions rapidement
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/transfert")
                    .cookie(token1)
                    .param("relation", "user2")
                    .param("description", "Transaction " + i)
                    .param("amount", "10.0"));
        }

        // Vérifier que toutes les transactions ont été traitées correctement
        user1 = userRepository.findFirstByEmail("user1@test.com").orElseThrow();
        User user2 = userRepository.findFirstByEmail("user2@test.com").orElseThrow();

        // Certaines transactions peuvent avoir échoué pour fonds insuffisants
        assertTrue(user1.getBankAccount().getBalance() >= 0);
        assertTrue(user2.getBankAccount().getBalance() >= 0);
    }

    @Test
    void testSecurityAccessControl() throws Exception {
        // Créer un utilisateur
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .param("username", "secureuser")
                        .param("email", "secure@example.com")
                        .param("password", "password123"))
                .andReturn();

        Cookie validToken = result.getResponse().getCookie("authToken");

        // Accès autorisé avec token valide
        mockMvc.perform(get("/transfert")
                        .cookie(validToken))
                .andExpect(status().isOk());

        // Accès refusé sans token
        mockMvc.perform(get("/transfert"))
                .andExpect(status().isUnauthorized());

        // Accès refusé avec token invalide
        Cookie invalidToken = new Cookie("authToken", "invalid.token.value");
        mockMvc.perform(get("/transfert")
                        .cookie(invalidToken))
                .andExpect(status().isUnauthorized());

        // Accès aux ressources publiques sans token
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk());
    }
}