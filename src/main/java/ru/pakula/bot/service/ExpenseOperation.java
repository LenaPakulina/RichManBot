package ru.pakula.bot.service;

import io.github.dostonhamrakulov.InlineCalendarBuilder;
import io.github.dostonhamrakulov.InlineCalendarCommandUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Objects;

import static io.github.dostonhamrakulov.LanguageEnum.RU;

public class ExpenseOperation {
    LocalDate localDate = null;

    int categoryId;

    double price;

    final long chatId;

    private static final String CHOOSE_DAY= "Choose the date of expense:";

    public ExpenseOperation(long chatId) {
        this.chatId = chatId;
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
        return categoryId == that.categoryId &&
                Double.compare(price, that.price) == 0 &&
                chatId == that.chatId &&
                Objects.equals(localDate, that.localDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDate, categoryId, price, chatId);
    }

    public SendMessage createOperation(Update update) {
        localDate = null;
        categoryId = -1;
        price = -1;

        InlineCalendarBuilder inlineCalendarBuilder = new InlineCalendarBuilder(RU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(CHOOSE_DAY);
        sendMessage.setReplyMarkup(inlineCalendarBuilder.build(update));
        return sendMessage;
    }

    public boolean checkIsInlineCalendarClicked(Update update) {
        return InlineCalendarCommandUtil.isInlineCalendarClicked(update);
    }

    public EditMessageText afterSelectingDate(Update update, long messageId) {
        if (InlineCalendarCommandUtil.isCalendarIgnoreButtonClicked(update)) {
            localDate = null;
            return null;
        }

        InlineCalendarBuilder inlineCalendarBuilder = new InlineCalendarBuilder(RU);
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(CHOOSE_DAY);
        message.setMessageId((int) messageId);

        if (InlineCalendarCommandUtil.isCalendarNavigationButtonClicked(update)) {
            message.setReplyMarkup(inlineCalendarBuilder.build(update));
            return message;
        }
        localDate = InlineCalendarCommandUtil.extractDate(update);
        System.out.println("localDate = " + localDate);
        return null;
    }

    public boolean hasValidDate() {
        return localDate != null;
    }

    public EditMessageText afterSelectingCategory(Update update, long messageId, String callBackData) {
        categoryId = Integer.parseInt(callBackData);
        return null;
    }
}
