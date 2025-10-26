package com.paymybuddy.app.integration;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.security.JwtService;
import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRepository;
import com.paymybuddy.app.user.UserRole;
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

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String validToken;
    private Cookie validAuthCookie;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setProvider("local");
        testUser.setRoles(Set.of(UserRole.USER));
        testUser = userRepository.save(testUser);
        testUser.setBankAccount(new BankAccount(testUser, 0.));
        testUser = userRepository.save(testUser);

        validToken = jwtService.generateToken(testUser.toUserDetails());
        validAuthCookie = new Cookie("authToken", validToken);
    }

    @Test
    void testJwtTokenGeneration() {
        assertNotNull(validToken);
        assertTrue(jwtService.validateToken(validToken));
    }

    @Test
    void testJwtTokenValidation() {
        assertTrue(jwtService.validateToken(validToken));

        String invalidToken = "invalid.token.here";
        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void testJwtTokenExtraction() {
        String subject = jwtService.extractSubject(validToken);
        assertEquals(testUser.getId().toString(), subject);

        Set<UserRole> roles = jwtService.extractRoles(validToken);
        assertTrue(roles.contains(UserRole.USER));
    }

    @Test
    void testAccessProtectedEndpointWithValidToken() throws Exception {
        mockMvc.perform(get("/transfert")
                        .cookie(validAuthCookie))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/transfert"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        Cookie invalidCookie = new Cookie("authToken", "invalid.token.here");

        mockMvc.perform(get("/transfert")
                        .cookie(invalidCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessPublicEndpoint() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessStaticResources() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());
    }

    @Test
    void testPasswordEncryption() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    void testUserRolesHierarchy() {
        // Test USER role
        User userWithUserRole = new User();
        userWithUserRole.setRoles(Set.of(UserRole.USER));
        assertTrue(userWithUserRole.hasRole(UserRole.USER));
        assertFalse(userWithUserRole.hasRole(UserRole.ADMIN));

        // Test ADMIN role (should expand to include lower roles)
        Set<UserRole> expandedRoles = UserRole.expandHighest("ROLE_ADMIN");
        assertTrue(expandedRoles.contains(UserRole.ADMIN));
        assertTrue(expandedRoles.contains(UserRole.MANAGER));
        assertTrue(expandedRoles.contains(UserRole.USER));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Note: Ce test nécessiterait de modifier le temps de validité du token
        // pour être exécuté rapidement. Voici un exemple conceptuel.
        assertTrue(jwtService.validateToken(validToken));

        // Le token devrait être valide jusqu'à son expiration
        assertNotNull(jwtService.extractExpiration(validToken));
    }

    @Test
    void testMultipleAuthenticationMethods() throws Exception {
        // Test avec cookie
        mockMvc.perform(get("/transfert")
                        .cookie(validAuthCookie))
                .andExpect(status().isOk());

        // Test avec header Authorization
        mockMvc.perform(get("/transfert")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCsrfDisabled() throws Exception {
        // CSRF est désactivé dans la configuration
        mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testSessionManagement() throws Exception {
        // La session doit être STATELESS
        var result = mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andReturn();

        // Vérifier qu'un cookie JWT est créé plutôt qu'une session
        assertNotNull(result.getResponse().getCookie("authToken"));
    }

    @Test
    void testSecureCookieAttributes() throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andReturn();

        Cookie authToken = result.getResponse().getCookie("authToken");
        assertNotNull(authToken);
        assertTrue(authToken.isHttpOnly());
        assertTrue(authToken.getSecure());
        assertEquals("/", authToken.getPath());
    }

    @Test
    void testUserDetailsService() {
        var userDetails = testUser.toUserDetails();

        assertNotNull(userDetails);
        assertEquals(testUser.getId().toString(), userDetails.getUsername());
        assertFalse(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void testAuthenticationWithDifferentProviders() {
        // Test utilisateur local
        User localUser = new User();
        localUser.setUsername("local");
        localUser.setEmail("local@example.com");
        localUser.setPassword(passwordEncoder.encode("password"));
        localUser.setProvider("local");
        localUser = userRepository.save(localUser);

        assertNotNull(localUser.getId());
        assertEquals("local", localUser.getProvider());

        // Test utilisateur OAuth (simulé)
        User oauthUser = new User();
        oauthUser.setUsername("oauth");
        oauthUser.setEmail("oauth@example.com");
        oauthUser.setProvider("google");
        oauthUser.setProviderId("google123");
        oauthUser = userRepository.save(oauthUser);

        assertNotNull(oauthUser.getId());
        assertEquals("google", oauthUser.getProvider());
        assertEquals("google123", oauthUser.getProviderId());
    }
}
