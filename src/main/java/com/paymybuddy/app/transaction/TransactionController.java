package com.paymybuddy.app.transaction;

import com.paymybuddy.app.user.User;
import com.paymybuddy.app.user.UserRelation;
import com.paymybuddy.app.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("transfert")
public class TransactionController {

    private final TransactionService service;
    private final UserService userService;

    public TransactionController(TransactionService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    private void setDefaultAttributes(Model model) {
        model.addAttribute("style", "/css/style.css");
        model.addAttribute("title", "Transferts - PayMyBuddy");
        model.addAttribute("contentTemplate", "pages/transfert.html");

        List<String> relations = userService.getLoggedUserRelations().stream()
                .map(UserRelation::getContact)
                .map(User::getUsername)
                .toList();
        model.addAttribute("relations", relations);

        model.addAttribute("latestTransactions", service.getLatestTransactions());
        model.addAttribute("bankAccountId", service.getLoggedUserBankAccountId());
    }

    @GetMapping
    public String getTransaction(@RequestParam(required = false) Integer success, Model model) {

        if (success != null) {
            model.addAttribute("successMessage", "Transaction réalisée !");
        }
        setDefaultAttributes(model);
        model.addAttribute("transactionForm", new TransactionForm());

        return "layout/base";
    }

    @PostMapping
    public String newTransaction(@Valid @ModelAttribute TransactionForm form,
                                 BindingResult bindingResult,
                                 Model model) {

        if (!bindingResult.hasErrors()) {
            try {
                service.newTransaction(form);
                return "redirect:/transfert?success=1";
            }
            catch (ResponseStatusException e) {
                model.addAttribute("errorMessage", e.getReason());
            }
        }
        setDefaultAttributes(model);
        model.addAttribute("transactionForm", form);
        return "layout/base";
    }
}
