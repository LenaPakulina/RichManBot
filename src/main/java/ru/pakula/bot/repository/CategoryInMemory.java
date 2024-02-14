package ru.pakula.bot.repository;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.pakula.bot.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryInMemory {
    static final String SPARKLES = EmojiParser.parseToUnicode(":sparkles:");

    static final String LINE_BREAK = System.lineSeparator();

    static final String CATEGORY_ID = "CATEGORY_ID:";

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

    public InlineKeyboardMarkup createInlineKeyboardMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (int i = 0; i < (categoryMemory.size() / 2 + categoryMemory.size() % 2); i++) {
            rowsInLine.add(new ArrayList<>());
        }
        System.out.println("rowsInLine size = " + rowsInLine.size());
        for (int i = 0; i < categoryMemory.size(); i++) {
            var btn = new InlineKeyboardButton();
            btn.setText(categoryMemory.get(i).getShortName());
            btn.setCallbackData(CATEGORY_ID + categoryMemory.get(i).getId().toString());
            rowsInLine.get(i / 2).add(btn);
        }
        markup.setKeyboard(rowsInLine);
        return markup;
    }

    public boolean isCategorySelection(String message) {
        return message.startsWith(CATEGORY_ID);
    }
}
