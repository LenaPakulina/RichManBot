package ru.pakula.bot.repository;

import com.vdurmont.emoji.EmojiParser;
import ru.pakula.bot.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryInMemory {
    static final String SPARKLES = EmojiParser.parseToUnicode(":sparkles:");

    static final String LINE_BREAK = System.lineSeparator();

    List<Category> categoryMemory = new ArrayList<>(20);

    public CategoryInMemory() {
        categoryMemory.add(new Category(1L, "Foodstuff", "Simple purchases of milk, bread. " +
                "Without household chemicals and without cafes."));
        categoryMemory.add(new Category(2L, "Chemistry", "Household chemicals and cosmetics."));
        categoryMemory.add(new Category(3L, "Entertainments", "Cafes, entertainment, cinema."));
        categoryMemory.add(new Category(4L, "Utilities", "Utilities, communications and other mandatory payments."));
        categoryMemory.add(new Category(5L, "Services", "Taxis, hairdressers, beauty salons."));
        categoryMemory.add(new Category(6L, "Car", "The car and everything connected with it."));
    }

    public String printAllCategories() {
        StringBuilder answer = new StringBuilder();
        answer.append(SPARKLES).append(SPARKLES).append(SPARKLES);
        answer.append("Available categories:");
        answer.append(SPARKLES).append(SPARKLES).append(SPARKLES);
        answer.append(LINE_BREAK).append(LINE_BREAK);
        for (Category category : categoryMemory) {
            answer.append(category.toString());
            answer.append(System.lineSeparator());
        }
        return answer.toString();
    }


}
