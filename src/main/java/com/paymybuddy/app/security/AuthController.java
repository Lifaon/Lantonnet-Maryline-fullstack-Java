package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public static void createJWTCookie(HttpServletResponse response, String token) throws IOException {
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(JwtService.getValidityPeriod());
        cookie.setAttribute("SameSite", SameSiteCookies.STRICT.getValue());
        response.addCookie(cookie);
        response.sendRedirect("/auth/success");
    }

    private void setRegisterDefaultAttributes(Model model) {
        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Inscription - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/auth/register");
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        setRegisterDefaultAttributes(model);
        SignUpForm signUpForm = new SignUpForm();
        model.addAttribute("signUpForm", signUpForm);
        return "layout/auth";
    }

    @PostMapping("/register")
    public String postRegister(@Valid @ModelAttribute SignUpForm form,
                             BindingResult bindingResult,
                             HttpServletResponse response,
                             Model model) {

        setRegisterDefaultAttributes(model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("loginForm", form);
        } else {
            try {
                userService.createUser(form);
                createJWTCookie(response, userService.verify(form));
            }
            catch (DataIntegrityViolationException e) {
                log.debug(e.getMessage());
                model.addAttribute("loginForm", form);
                model.addAttribute("error", "Mail déjà utilisé. Veuillez utiliser un autre mail.");
            }
            catch (Exception e) {
                log.debug(e.getMessage());
                model.addAttribute("loginForm", form);
                model.addAttribute("error", "Nom, Mail, ou mot de passe incorrect.");
            }
        }

        return "layout/auth";
    }

    private void setLoginDefaultAttributes(Model model) {
        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Connexion - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/auth/login");
    }

    @GetMapping("/login")
    public String login(Model model) {
        setLoginDefaultAttributes(model);
        LoginForm loginForm = new LoginForm();
        model.addAttribute("loginForm", loginForm);
        return "layout/auth";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form,
                        BindingResult bindingResult,
                        HttpServletResponse response,
                        Model model) {

        setLoginDefaultAttributes(model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("loginForm", form);
        } else {
            try {
                createJWTCookie(response, userService.verify(form));
            }
            catch (Exception e) {
                log.debug(e.getMessage());
                model.addAttribute("loginForm", form);
                model.addAttribute("error", "Mail ou mot de passe incorrect.");
            }
        }

        return "layout/auth";
    }

    @GetMapping("/success")
    public String success(Model model) {
        return "pages/auth/success";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie authCookie = new Cookie("authToken", null);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        authCookie.setHttpOnly(true);
        authCookie.setSecure(true);
        response.addCookie(authCookie);
        return "redirect:/auth/login";
    }
}
