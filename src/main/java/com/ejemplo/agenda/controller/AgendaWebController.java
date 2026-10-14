package com.ejemplo.agenda.controller;

import com.ejemplo.agenda.dto.ContactResponseDTO;
import com.ejemplo.agenda.model.User;
import com.ejemplo.agenda.service.AuthService;
import com.ejemplo.agenda.service.ContactService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class AgendaWebController {

    private final ContactService contactService;
    private final AuthService authService;

    public AgendaWebController(ContactService contactService, AuthService authService) {
        this.contactService = contactService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/agenda";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/agenda")
    public String agendaPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            try {
                User user = authService.getCurrentAuthenticatedUser();
                List<ContactResponseDTO> contacts = contactService.getAllContactsForCurrentUser();
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("contacts", contacts);
            } catch (Exception e) {
                model.addAttribute("contacts", Collections.emptyList());
            }
        } else {
            model.addAttribute("contacts", Collections.emptyList());
        }

        return "agenda";
    }
}
