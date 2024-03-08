package ru.pakula.bot.repository;

import com.vdurmont.emoji.EmojiParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.pakula.bot.model.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CategoryStorage {

    @Autowired
    private CategoryRepository categoryRepository;

    static final String SPARKLES = EmojiParser.parseToUnicode(":sparkles:");

    static final String LINE_BREAK = System.lineSeparator();

    static final String CATEGORY_ID = "CATEGORY_ID:";

    List<Category> categoryMemory = new ArrayList<>(20);

    @PostConstruct
    public void init() {
        categoryMemory.add(new Category(1L, "Продукты", "Простые покупки молока, хлеба. " +
                "Без бытовой химии и без кафе."));
        categoryMemory.add(new Category(2L, "Бытовая химия", "Бытовая химия и косметика."));
        categoryMemory.add(new Category(3L, "Развлечения", "Кафе, развлечения, кино."));
        categoryMemory.add(new Category(4L, "Коммунальные услуги", "Коммунальные услуги, " +
                "связь и другие обязательные платежи."));
        categoryMemory.add(new Category(5L, "Услуги", "Такси, парикмахерские, салоны красоты."));
        categoryMemory.add(new Category(6L, "Автомобиль", "Авто и все связанное с ним."));
        categoryMemory.add(new Category(7L, "Медицина", "Медицинские услуги, стоматология."));

        for (Category category : categoryMemory) {
            if (categoryRepository.findById(category.getId()).isEmpty()) {
                categoryRepository.save(category);
            }
        }
    }

    public String printAllCategories() {
        StringBuilder answer = new StringBuilder();
        answer.append(SPARKLES).append(SPARKLES).append(SPARKLES);
        answer.append("Доступные категории:");
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

    public Set<Integer> getValidCategories() {
        Set<Integer> validCategories = new HashSet<>();
        categoryMemory.forEach(category -> validCategories.add(category.getId().intValue()));
        return validCategories;
    }
}
