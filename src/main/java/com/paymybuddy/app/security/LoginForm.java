package com.paymybuddy.app.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class LoginForm {

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Length(min = 6, max = 64, message = "Le mot de passe doit faire entre 6 et 64 caractères")
    private String password;

    public LoginForm() {
    }

    public UserDetails toUserDetails() {
        return User.builder().username(email).password(password).build();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
