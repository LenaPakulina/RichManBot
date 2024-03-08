package ru.pakula.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pakula.bot.StringConstants;
import ru.pakula.bot.config.BotConfig;
import ru.pakula.bot.repository.CategoryStorage;
import ru.pakula.bot.repository.ExpenseRepository;
import ru.pakula.bot.repository.PersonStorage;

import java.util.*;

import static ru.pakula.bot.StringConstants.SIMPLE_EXPENSE_INFO;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private ExpenseRepository expenseRepository;

    final BotConfig config;

    @Autowired
    private PersonStorage personStorage;

    @Autowired
    private CategoryStorage categoryStorage;

    private boolean isDeleteOperation = false;

    private final Map<Long, ExpenseOperation> operations = new HashMap<>(20);

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "приветственное сообщение"));
        listOfCommands.add(new BotCommand("/show_categories", "показать все категории трат"));
        listOfCommands.add(new BotCommand("/add_expense", "добавить трату для анализа"));
        listOfCommands.add(new BotCommand("/add_simple_expense", "добавить трату в текстовом формате"));
        listOfCommands.add(new BotCommand("/help", "помощь"));
        listOfCommands.add(new BotCommand("/delete_expense_by_id", "удалить из архива трату"));
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
        String answer = EmojiParser.parseToUnicode("Привет, " + name + "! :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void sendMessageAndSaveMessageId(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            var message1 = execute(message);
            Long value = Long.valueOf(message1.getMessageId());
            operations.get(chatId).addMessageIdToDelete(value);
        } catch (TelegramApiException e) {
            log.error(StringConstants.LOG_ERROR + e.getMessage());
        }
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

        if (isDeleteOperation) {
            try {
                Long id = Long.valueOf(msgText);
                if (expenseRepository.findById(id).isEmpty()) {
                    isDeleteOperation = false;
                    throw new IllegalArgumentException("Не удалось удалить трату с id = ");
                }
                expenseRepository.deleteById(id);
                sendMessage(chatId, "Трата с id = " + id + " удалена.");
            } catch (Exception e) {
                sendMessage(chatId, e.getMessage());
                log.error(StringConstants.LOG_ERROR + e.getMessage());
            }
        } else if (!operations.containsKey(chatId)) {
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
                    ExpenseOperation operation = new ExpenseOperation(chatId, false);
                    operation.addMessageIdToDelete(messageId);
                    operations.put(chatId, operation);
                    executeMessage(operation.createOperation(update));
                    break;
                case "/add_simple_expense":
                    ExpenseOperation simple = new ExpenseOperation(chatId, true);
                    simple.addMessageIdToDelete(messageId);
                    operations.put(chatId, simple);
                    sendMessageAndSaveMessageId(chatId, SIMPLE_EXPENSE_INFO
                            + System.lineSeparator()
                            + System.lineSeparator()
                            + categoryStorage.printAllCategories());
                    break;
                case "/delete_expense_by_id":
                    isDeleteOperation = true;
                    sendMessage(chatId, "Введите id траты:");
                    break;
                default:
                    sendMessage(chatId, "Извините, команда не распознана.");
            }
        } else if (operations.get(chatId).isTextExpense()) {
            isDeleteOperation = false;
            operations.get(chatId).addMessageIdToDelete(messageId);
            List<String> expenseInfo = msgText.lines().toList();
            System.out.println(expenseInfo.size());
            try {
                operations.get(chatId).parseTextExpense(expenseInfo, categoryStorage.getValidCategories());
                expenseRepository.save(operations.get(chatId).getCurrentExpense());
                String text = operations.get(chatId).printInfo();
                removeOperation(chatId);
                sendMessage(chatId, text);
            } catch (Exception e) {
                sendMessage(chatId, e.getMessage());
                log.error(StringConstants.LOG_ERROR + e.getMessage());
            }
        } else if (operations.get(chatId).hasValidCategory()) {
            isDeleteOperation = false;
            try {
                operations.get(chatId).addMessageIdToDelete(messageId);
                double value = Integer.parseInt(msgText);
                operations.get(chatId).setPrice(value);
                expenseRepository.save(operations.get(chatId).getCurrentExpense());
                String text = operations.get(chatId).printInfo();
                removeOperation(chatId);
                sendMessage(chatId, text);
            } catch (NumberFormatException e) {
                sendMessageAndSaveMessageId(chatId, StringConstants.INCORRECT_PRICE);
                log.error(StringConstants.LOG_ERROR + e.getMessage());
            }
        } else {
            isDeleteOperation = false;
            removeOperation(chatId);
        }
    }

    private void handlingCallbackQuery(Update update) {
        isDeleteOperation = false;
        String callBackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (operations.containsKey(chatId)) {
            if (categoryStorage.isCategorySelection(callBackData)) {
                EditMessageText ans = operations.get(chatId).afterSelectingCategory(update, messageId, callBackData);
                if (ans != null) {
                    operations.get(chatId).addMessageIdToDelete(messageId);
                    executeEditMessageText(ans);
                }
            } else if (operations.get(chatId).checkIsInlineCalendarClicked(update)) {
                EditMessageText ans = operations.get(chatId).afterSelectingDate(update, messageId);
                if (ans != null) {
                    executeEditMessageText(ans);
                } else if (operations.get(chatId).hasValidDate()) {
                    EditMessageText message = new EditMessageText();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Выберите категорию:");
                    message.setMessageId((int) messageId);

                    InlineKeyboardMarkup markup = categoryStorage.createInlineKeyboardMarkup();
                    message.setReplyMarkup(markup);

                    executeEditMessageText(message);
                }
            }
        }
    }

    private void removeOperation(long chatId) {
        if (!operations.containsKey(chatId)) {
            return;
        }
        List<Long> list = operations.get(chatId).getMessageIdsToDeleteList();
        for (Long messageId : list) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId.intValue());
            try {
                execute(deleteMessage);
            } catch (TelegramApiException tae) {
                throw new RuntimeException(tae);
            }
        }
        operations.remove(chatId);
    }
}
