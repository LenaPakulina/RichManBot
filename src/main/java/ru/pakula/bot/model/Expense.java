package ru.pakula.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.Locale;

@Entity(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue
    private Long id;

    int categoryId;

    double price;

    long chatId;

    LocalDate localDate = null;

    public Expense() {
        localDate = LocalDate.now();
    }

    public Expense(int categoryId, double price, long chatId, LocalDate date) {
        this.categoryId = categoryId;
        this.price = price;
        this.chatId = chatId;
        this.localDate = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
