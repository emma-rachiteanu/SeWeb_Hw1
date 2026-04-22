package com.example.recipes.controller;

import com.example.recipes.model.UserProfile;
import com.example.recipes.service.XmlService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final XmlService xmlService;

    public HomeController(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String loggedUserId = (String) session.getAttribute("loggedUserId");
        UserProfile loggedUser = xmlService.findUserById(loggedUserId);

        model.addAttribute("loggedUser", loggedUser);

        return "index";
    }
}