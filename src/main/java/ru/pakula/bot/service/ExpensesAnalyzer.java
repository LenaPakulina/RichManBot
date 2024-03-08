package ru.pakula.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pakula.bot.model.Expense;
import ru.pakula.bot.repository.CategoryStorage;
import ru.pakula.bot.repository.ExpenseRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExpensesAnalyzer {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryStorage categoryStorage;

    public String getInfoForLastMonths() {
        StringBuilder info = new StringBuilder();
        LocalDate startDate = LocalDate.now();
        startDate = startDate.minusMonths(1);
        startDate = startDate.minusDays(startDate.getDayOfMonth());
        LocalDate finishDate = LocalDate.now();
        finishDate = finishDate.minusDays(finishDate.getDayOfMonth());
        info.append(calculateInfoForMonth(startDate, finishDate));
        info.append(System.lineSeparator());
        info.append(System.lineSeparator());
        info.append(calculateInfoForMonth(finishDate, LocalDate.now()));
        return info.toString();
    }

    private String calculateInfoForMonth(LocalDate startDate, LocalDate finishDate) {
        StringBuilder info = new StringBuilder();
        info.append(String.format("Траты с %s по %s:\n\n", startDate.toString(), finishDate.toString()));
        List<Expense> result = expenseRepository.findByLocalDateGreaterThanAndLocalDateLessThanEqual(startDate, finishDate);
        Map<Integer, Double> statistic = new HashMap<>();
        double allPrice = 0;
        for (Expense expense : result) {
            int categoryId = expense.getCategoryId();
            allPrice += expense.getPrice();
            if (statistic.containsKey(categoryId)) {
                statistic.put(categoryId, statistic.get(categoryId) + expense.getPrice());
            } else {
                statistic.put(categoryId, expense.getPrice());
            }
        }
        statistic.forEach((categoryId, price) -> {
            info.append(categoryStorage.getCategoryNameById(categoryId))
                    .append(" = ")
                    .append(price)
                    .append(System.lineSeparator());
        });
        info.append("Суммарные затраты за месяц: ");
        info.append(allPrice);
        info.append(" руб.");
        return info.toString();
    }
}
