package ru.pakula.bot;

public class StringConstants {
    public static final String HELP_TEXT = "Этот бот сохраняет записи о расходах и анализирует их.\n\n" +
            "Вы можете выбрать команды из главного меню или ввести их вручную:\n\n" +
            "Введи /start для отражения приветственного сообщения;\n\n" +
            "Введи /show_categories для отображения всех доступных категорий трат;\n\n" +
            "Введи /add_expense для сохранения записи о расходах.";

    public static final String INCORRECT_PRICE = "Была введена некорректная цена ."
            + System.lineSeparator() + "Введите цену в рублях:";

    public static final String LOG_ERROR = "Произошла ошибка: ";

    public static final String CHOOSE_DAY= "Выберите дату совершения траты:";
}
