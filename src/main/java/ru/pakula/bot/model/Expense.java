package ru.pakula.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity(name = "expenses")
@Data
public class Expense {

    @Id
    @GeneratedValue
    private Long id;

    int categoryId;

    double price;

    long chatId;

    LocalDate localDate = null;

    String desc;

    public Expense() {
        localDate = LocalDate.now();
    }

    public Expense(int categoryId, double price, long chatId, LocalDate date, String desc) {
        this.categoryId = categoryId;
        this.price = price;
        this.chatId = chatId;
        this.localDate = date;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Expense expense = (Expense) o;
        return categoryId == expense.categoryId
                && Double.compare(price, expense.price) == 0
                && chatId == expense.chatId
                && Objects.equals(id, expense.id)
                && Objects.equals(localDate, expense.localDate)
                && Objects.equals(desc, expense.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryId, price, chatId, localDate, desc);
    }
}
