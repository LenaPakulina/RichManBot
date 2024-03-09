package ru.pakula.bot;

import com.vdurmont.emoji.EmojiParser;

public class StringConstants {
    public static final String HELP_TEXT = "Этот бот сохраняет записи о расходах и анализирует их.\n\n" +
            "Вы можете выбрать команды из главного меню или ввести их вручную:\n\n" +
            "Введи /start для отражения приветственного сообщения;\n\n" +
            "Введи /show_categories для отображения всех доступных категорий трат;\n\n" +
            "Введи /add_expense для сохранения записи о расходах;\n\n" +
            "Введи /add_simple_expense для сохранения записи о расходах в текстовой форме;\n\n" +
            "Введи /add_simple_expense для сохранения записи о расходах в текстовой форме;\n\n" +
            "Введи /show_expenses_for_2_last_months для получения статистики по категориям расходов за 2 последних месяца.\n\n";

    public static final String INCORRECT_PRICE = "Была введена некорректная цена ."
            + System.lineSeparator() + "Введите цену в рублях:";

    public static final String LOG_ERROR = "Произошла ошибка: ";

    public static final String CHOOSE_DAY= "Выберите дату совершения траты:";

    public static final String SIMPLE_EXPENSE_INFO = "Введите трату в следующем формате [в скобках указаны примеры ввода]:\n\n" +
            "1. Номер категории [1]\n" +
            "2. Введите сумму операции в рублях [222]\n" +
            "3. Можете оставить комментарий к покупке [Перекресток, щорса].";

    public static final String EMOJI_CONSTRUCTOR = EmojiParser.parseToUnicode(":construction:");
}
