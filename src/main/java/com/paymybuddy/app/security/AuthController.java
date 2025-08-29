package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public static void createJWTCookie(HttpServletResponse response, String token) throws IOException {
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setAttribute("SameSite", SameSiteCookies.STRICT.getValue());
        response.addCookie(cookie);
        response.sendRedirect("/users");
    }

    @PostMapping("/register")
    public void register(@RequestBody User user) {
        userService.createUser(user);
    }

    @GetMapping("/login")
    public String login(Model model) {
        LoginForm loginForm = new LoginForm();
        model.addAttribute("loginForm", loginForm);
        model.addAttribute("title", "Connexion - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/auth/login");
        return "layout/base";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm loginForm,
                        BindingResult bindingResult,
                        @RequestParam(required = false) String redirect,
                        HttpServletResponse response,
                        Model model) throws IOException {

        model.addAttribute("title", "Connexion - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/auth/login");

        if (bindingResult.hasErrors()) {
            model.addAttribute("loginForm", loginForm);
//            model.addAttribute("pageTitle", "Contact - Erreurs");
//            model.addAttribute("contentTemplate", "pages/contact-content");
        } else {
            try {
                createJWTCookie(response, userService.verify(loginForm.toUserDetails()));
            }
            catch (Exception e) {
                model.addAttribute("loginForm", loginForm);
                model.addAttribute("error", "Mail ou mot de passe incorrect.");
            }
        }

        return "layout/base";
    }
}
