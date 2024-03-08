package ru.pakula.bot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.pakula.bot.model.Expense;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends CrudRepository<Expense, Long> {
    List<Expense> findByLocalDateGreaterThanAndLocalDateLessThanEqual(LocalDate from, LocalDate to);
}
