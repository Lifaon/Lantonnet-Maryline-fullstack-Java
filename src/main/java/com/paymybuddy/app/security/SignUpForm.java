package com.paymybuddy.app.security;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class SignUpForm extends LoginForm {

    @NotBlank
    @Length(min = 3, max = 20)
    private String username;

    public SignUpForm() {
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
