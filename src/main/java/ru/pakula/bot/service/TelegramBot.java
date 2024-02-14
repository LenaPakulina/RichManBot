package ru.pakula.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pakula.bot.StringConstants;
import ru.pakula.bot.config.BotConfig;
import ru.pakula.bot.repository.CategoryStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final PersonStorage personStorage = new PersonStorage();

    private final CategoryStorage categoryStorage = new CategoryStorage();

    private final Map<Long, ExpenseOperation> operations = new HashMap<>(20);

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/show_categories", "show all available expense categories"));
        listOfCommands.add(new BotCommand("/add_expense", "add expense for statistic"));
        listOfCommands.add(new BotCommand("/help", "get info about bot"));
        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handlingSimpleQuery(update);
        } else if (update.hasCallbackQuery()) {
            handlingCallbackQuery(update);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + "! :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeEditMessageText(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(StringConstants.LOG_ERROR + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(StringConstants.LOG_ERROR + e.getMessage());
        }
    }

    private void handlingSimpleQuery(Update update) {
        String msgText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        long messageId = update.getMessage().getMessageId();

        if (operations.containsKey(chatId) && operations.get(chatId).hasValidCategory()) {
            try {
                double value = Integer.parseInt(msgText);
                operations.get(chatId).setPrice(value);
                sendMessage(chatId, operations.get(chatId).toString());
                operations.remove(chatId);
            } catch (NumberFormatException e) {
                sendMessage(chatId, StringConstants.INCORRECT_PRICE);
                log.error(StringConstants.LOG_ERROR + e.getMessage());
            }
        } else {
            operations.remove(chatId);
            switch (msgText) {
                case "/start":
                    personStorage.registerPerson(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, StringConstants.HELP_TEXT);
                    break;
                case "/show_categories":
                    sendMessage(chatId, categoryStorage.printAllCategories());
                    break;
                case "/add_expense":
                    ExpenseOperation operation = new ExpenseOperation(chatId);
                    operations.put(chatId, operation);
                    executeMessage(operation.createOperation(update));
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized.");
            }
        }
    }

    private void handlingCallbackQuery(Update update) {
        String callBackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        System.out.println(callBackData);

        if (operations.containsKey(chatId)) {
            if (categoryStorage.isCategorySelection(callBackData)) {
                EditMessageText ans = operations.get(chatId).afterSelectingCategory(update, messageId, callBackData);
                if (ans != null) {
                    executeEditMessageText(ans);
                }
            } else if (operations.get(chatId).checkIsInlineCalendarClicked(update)) {
                EditMessageText ans = operations.get(chatId).afterSelectingDate(update, messageId);
                if (ans != null) {
                    executeEditMessageText(ans);
                } else if (operations.get(chatId).hasValidDate()) {
                    EditMessageText message = new EditMessageText();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Choose category:");
                    message.setMessageId((int) messageId);

                    InlineKeyboardMarkup markup = categoryStorage.createInlineKeyboardMarkup();
                    message.setReplyMarkup(markup);

                    executeEditMessageText(message);
                }
            }
        }
    }
}
