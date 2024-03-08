package ru.pakula.bot.service;

import io.github.dostonhamrakulov.InlineCalendarBuilder;
import io.github.dostonhamrakulov.InlineCalendarCommandUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.pakula.bot.model.Expense;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.dostonhamrakulov.LanguageEnum.RU;
import static ru.pakula.bot.StringConstants.CHOOSE_DAY;

public class ExpenseOperation {

    private final Expense currentExpense = new Expense();

    private final List<Long> messageIdsToDelete = new ArrayList<>(20);

    public ExpenseOperation(long chatId) {
        currentExpense.setChatId(chatId);
    }

    public SendMessage createOperation(Update update) {
        currentExpense.setLocalDate(null);
        currentExpense.setCategoryId(-1);
        currentExpense.setPrice(-1);

        InlineCalendarBuilder inlineCalendarBuilder = new InlineCalendarBuilder(RU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(currentExpense.getChatId()));
        sendMessage.setText(CHOOSE_DAY);
        sendMessage.setReplyMarkup(inlineCalendarBuilder.build(update));
        return sendMessage;
    }

    public boolean checkIsInlineCalendarClicked(Update update) {
        return InlineCalendarCommandUtil.isInlineCalendarClicked(update);
    }

    public EditMessageText afterSelectingDate(Update update, long messageId) {
        if (InlineCalendarCommandUtil.isCalendarIgnoreButtonClicked(update)) {
            currentExpense.setLocalDate(null);
            return null;
        }

        InlineCalendarBuilder inlineCalendarBuilder = new InlineCalendarBuilder(RU);
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(currentExpense.getChatId()));
        message.setText(CHOOSE_DAY);
        message.setMessageId((int) messageId);

        if (InlineCalendarCommandUtil.isCalendarNavigationButtonClicked(update)) {
            message.setReplyMarkup(inlineCalendarBuilder.build(update));
            return message;
        }
        currentExpense.setLocalDate(InlineCalendarCommandUtil.extractDate(update));
        return null;
    }

    public EditMessageText afterSelectingCategory(Update update, long messageId, String callBackData) {
        currentExpense.setCategoryId(Integer.parseInt(callBackData.split(":")[1]));

        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(currentExpense.getChatId()));
        message.setText("Введите цену в рублях: ");
        message.setMessageId((int) messageId);

        return message;
    }

    public boolean hasValidDate() {
        return currentExpense.getLocalDate() != null;
    }

    public boolean hasValidCategory() {
        return currentExpense.getCategoryId() != -1;
    }

    public void setPrice(double value) {
        currentExpense.setPrice(value);
    }

    public String printInfo() {
        return "Сохранена трата: " +
                "дата = " + currentExpense.getLocalDate() +
                ",\nкатегория = " + currentExpense.getCategoryId() +
                ",\nцена = " + currentExpense.getPrice() +
                ",\nкомментарий = " + currentExpense.getDesc();
    }

    public List<Long> getMessageIdsToDeleteList() {
        return messageIdsToDelete;
    }

    public void addMessageIdToDelete(Long id) {
        messageIdsToDelete.add(id);
    }

    public Expense getCurrentExpense() {
        return currentExpense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExpenseOperation that = (ExpenseOperation) o;
        return currentExpense.equals(that.currentExpense);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentExpense);
    }
}
