package com.paymybuddy.app.integration;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setProvider("local");
        userRepository.save(testUser);
    }

    @Test
    void testGetLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().attributeExists("loginForm"));
    }

    @Test
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/success"))
                .andExpect(cookie().exists("authToken"));
    }

    @Test
    void testFailedLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testFailedLoginWithNonExistentUser() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("email", "nonexistent@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testSuccessfulRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "newpassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/success"))
                .andExpect(cookie().exists("authToken"));

        // Vérifier que l'utilisateur est bien créé
        assert userRepository.findFirstByEmail("newuser@example.com").isPresent();
    }

    @Test
    void testRegistrationWithExistingEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "anotheruser")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testRegistrationWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "invalidemail")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testRegistrationWithShortPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout/auth"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testLogout() throws Exception {
        // D'abord se connecter
        var loginResult = mockMvc.perform(post("/auth/login")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String authToken = Objects.requireNonNull(loginResult.getResponse().getCookie("authToken")).getValue();

        // Puis se déconnecter
        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("authToken", authToken)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(cookie().maxAge("authToken", 0));
    }
}