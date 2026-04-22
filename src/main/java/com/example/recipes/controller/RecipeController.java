package com.example.recipes.controller;

import com.example.recipes.model.Recipe;
import com.example.recipes.service.XmlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.recipes.model.UserProfile;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class RecipeController {

    private final XmlService xmlService;

    public RecipeController(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    @GetMapping("/recipes")
    public String showRecipes(Model model) {
        model.addAttribute("recipes", xmlService.getAllRecipes());
        return "recipes";
    }

    @GetMapping("/add-recipe")
    public String showAddRecipeForm(Model model) {
        model.addAttribute("cuisines", xmlService.getCuisines());
        model.addAttribute("difficulties", xmlService.getDifficulties());
        return "add-recipe";
    }

    @PostMapping("/add-recipe")
    public String addRecipe(
            @RequestParam String title,
            @RequestParam String cuisine1,
            @RequestParam String cuisine2,
            @RequestParam String difficulty,
            Model model
    ) {
        try {
            xmlService.addRecipe(title, cuisine1, cuisine2, difficulty);
            return "redirect:/recipes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cuisines", xmlService.getCuisines());
            model.addAttribute("difficulties", xmlService.getDifficulties());

            model.addAttribute("titleValue", title);
            model.addAttribute("cuisine1Value", cuisine1);
            model.addAttribute("cuisine2Value", cuisine2);
            model.addAttribute("difficultyValue", difficulty);

            return "add-recipe";
        }
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("cuisines", xmlService.getCuisines());
        model.addAttribute("difficulties", xmlService.getDifficulties());
        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUser(
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String skillLevel,
            @RequestParam String preferredCuisine,
            Model model
    ) {
        try {
            xmlService.addUser(name, surname, skillLevel, preferredCuisine);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cuisines", xmlService.getCuisines());
            model.addAttribute("difficulties", xmlService.getDifficulties());

            model.addAttribute("nameValue", name);
            model.addAttribute("surnameValue", surname);
            model.addAttribute("skillValue", skillLevel);
            model.addAttribute("cuisineValue", preferredCuisine);

            return "add-user";
        }
    }

    @GetMapping("/recommend-skill")
    public String recommendBySkill(
            @RequestParam(required = false) String skill,
            Model model,
            HttpSession session
    ) {
        String loggedUserId = (String) session.getAttribute("loggedUserId");
        UserProfile loggedUser = xmlService.findUserById(loggedUserId);

        if ((skill == null || skill.isBlank()) && loggedUser != null) {
            skill = loggedUser.getSkillLevel();
        }

        model.addAttribute("difficulties", xmlService.getDifficulties());
        model.addAttribute("selectedSkill", skill);
        model.addAttribute("loggedUser", loggedUser);

        if (skill != null && !skill.isBlank()) {
            model.addAttribute("recipes", xmlService.recommendBySkill(skill));
        }

        return "skill-filter";
    }

    @GetMapping("/recommend-skill-cuisine")
    public String recommendBySkillAndCuisine(Model model, HttpSession session) {
        String loggedUserId = (String) session.getAttribute("loggedUserId");

        UserProfile selectedUser = xmlService.findUserById(loggedUserId);

        if (selectedUser == null) {
            selectedUser = xmlService.getFirstUser();
        }

        model.addAttribute("title", "Recipes recommended for the selected user");
        model.addAttribute("selectedUser", selectedUser);

        if (selectedUser != null) {
            model.addAttribute("recipes", xmlService.recommendBySkillAndCuisineForUser(selectedUser.getId()));
        }

        return "recommendations";
    }

    @GetMapping("/filter-cuisine")
    public String filterByCuisine(@RequestParam(required = false) String cuisine, Model model) {
        model.addAttribute("cuisines", xmlService.getCuisines());
        model.addAttribute("selectedCuisine", cuisine);

        if (cuisine != null && !cuisine.isBlank()) {
            List<Recipe> recipes = xmlService.findRecipesByCuisine(cuisine);
            model.addAttribute("recipes", recipes);
        }

        return "cuisine";
    }

    @GetMapping("/recipe/{id}")
    public String showRecipeDetails(@PathVariable String id, Model model) {
        Recipe recipe = xmlService.findRecipeById(id);

        if (recipe == null) {
            return "redirect:/recipes";
        }

        model.addAttribute("recipe", recipe);
        return "recipe-details";
    }

    @GetMapping("/recipes-xsl")
    public String showRecipesWithXsl(
            @RequestParam(required = false) String userId,
            Model model,
            HttpSession session
    ) {
        if (userId == null || userId.isBlank()) {
            userId = (String) session.getAttribute("loggedUserId");
        }

        UserProfile selectedUser = xmlService.findUserById(userId);

        if (selectedUser == null) {
            selectedUser = xmlService.getFirstUser();
        }

        String selectedUserId = selectedUser != null ? selectedUser.getId() : null;

        model.addAttribute("users", xmlService.getAllUsers());
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("selectedUserId", selectedUserId);
        model.addAttribute("content", xmlService.transformRecipesWithXsl(selectedUserId));

        return "xsl-view";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("users", xmlService.getAllUsers());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, HttpSession session) {
        UserProfile user = xmlService.findUserById(userId);

        if (user != null) {
            session.setAttribute("loggedUserId", user.getId());
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedUserId");
        return "redirect:/";
    }
}