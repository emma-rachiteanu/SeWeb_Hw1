package com.example.recipes.model;

public class Recipe {
    private String id;
    private String title;
    private String cuisine1;
    private String cuisine2;
    private String difficulty;

    public Recipe() {
    }

    public Recipe(String id, String title, String cuisine1, String cuisine2, String difficulty) {
        this.id = id;
        this.title = title;
        this.cuisine1 = cuisine1;
        this.cuisine2 = cuisine2;
        this.difficulty = difficulty;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCuisine1() {
        return cuisine1;
    }

    public String getCuisine2() {
        return cuisine2;
    }

    public String getDifficulty() {
        return difficulty;
    }
}