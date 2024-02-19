package ru.pakula.bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pakula.bot.model.Expense;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {
}
