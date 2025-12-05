package org.example.digitalisidomero.model;

public class Application {
    private int id;
    private String name;           // pl. "chrome.exe"
    private String displayName;    // pl. "Google Chrome"
    private Category category;

    // Üres konstruktor
    public Application() {}

    // Konstruktor ID nélkül (új app létrehozáshoz)
    public Application(String name, String displayName, Category category) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
    }

    // Teljes konstruktor
    public Application(int id, String name, String displayName, Category category) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", category=" + category +
                '}';
    }
}