package ru.pakula.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "categories")
public class Category {

    @Id
    private Long id;

    private String shortName;

    private String description;

    public Category(Long id, String shortName, String description) {
        this.id = id;
        this.shortName = shortName;
        this.description = description;
    }

    public Category() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return id + ". [" + shortName + "] - " + description;
    }
}
