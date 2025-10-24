package com.paymybuddy.app.user;

import com.paymybuddy.app.security.JwtService;
import com.paymybuddy.app.security.SignUpForm;
import com.paymybuddy.app.transaction.TransactionForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Profil - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/user/profile.html");
        model.addAttribute("navId", 2);

        SignUpForm updateForm = new SignUpForm();
        User user = userService.getLoggedUser();
        updateForm.setUsername(user.getUsername());
        updateForm.setEmail(user.getEmail());
        model.addAttribute("updateForm", updateForm);

        return "layout/base";
    }

    @PostMapping("/profile")
    public String postProfile(@Valid @ModelAttribute(value = "updateForm") SignUpForm updateForm,
                              BindingResult bindingResult,
                              Model model) {

        if (!bindingResult.hasErrors()) {
            try {
                User user = userService.getLoggedUser();
                user.setUsername(updateForm.getUsername());
                user.setEmail(updateForm.getEmail());
                user.setPassword(updateForm.getPassword());
                userService.updateUser(user);
                return "redirect:/user/profile";
            }
            catch (Exception e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }

        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Profil - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/user/profile.html");
        model.addAttribute("navId", 2);
        model.addAttribute("updateForm", updateForm);

        return "layout/base";
    }

    @GetMapping("/addrelation")
    public String getRelationForm(@RequestParam(required = false) Integer success, Model model) {
        if (success != null) {
            model.addAttribute("successMessage", "Relation ajoutée !");
        }
        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Ajouter une relation - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/user/addrelation.html");
        model.addAttribute("navId", 3);
        model.addAttribute("relationEmail", "");
        return "layout/base";
    }

    @PostMapping("/addrelation")
    public String addRelation(@RequestParam String relationEmail,
                              Model model) {
        try {
            userService.addRelationToLoggedUser(relationEmail);
            return "redirect:/user/addrelation?success=1";
        }
        catch (ResponseStatusException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
                model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Ajouter une relation - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/user/addrelation.html");
        model.addAttribute("navId", 3);
        model.addAttribute("relationEmail", relationEmail);
        return "layout/base";
    }

}
