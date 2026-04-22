package com.example.recipes.model;

public class UserProfile {
    private String id;
    private String name;
    private String surname;
    private String skillLevel;
    private String preferredCuisine;

    public UserProfile() {
    }

    public UserProfile(String id, String name, String surname, String skillLevel, String preferredCuisine) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.skillLevel = skillLevel;
        this.preferredCuisine = preferredCuisine;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public String getPreferredCuisine() {
        return preferredCuisine;
    }
}