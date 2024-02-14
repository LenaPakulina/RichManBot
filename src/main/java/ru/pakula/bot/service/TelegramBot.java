package ru.pakula.bot.service;

import com.vdurmont.emoji.EmojiParser;
import io.github.dostonhamrakulov.InlineCalendarBuilder;
import io.github.dostonhamrakulov.InlineCalendarCommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.pakula.bot.config.BotConfig;
import ru.pakula.bot.model.Category;
import ru.pakula.bot.repository.CategoryInMemory;
import ru.pakula.bot.repository.CategoryRepository;
import ru.pakula.bot.model.Person;
import ru.pakula.bot.repository.UserRepository;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.dostonhamrakulov.LanguageEnum.RU;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryInMemory categoryInMemory = new CategoryInMemory();

    private Map<Long, ExpenseOperation> operations = new HashMap<>(20);

    final BotConfig config;

    static final String HELP_TEXT = "...";

    static final String YES_BUTTON = "YES_BUTTON";

    static final String NO_BUTTON = "NO_BUTTON";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/add_cost", "add cost for statistic"));
        listOfCommands.add(new BotCommand("/delete_costs", "delete expenses for the day"));
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
            String msgText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (msgText) {
                case "/start":
                    registerPerson(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/register":
                    register(chatId);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/add_category":
                    break;
                case "/show_categories":
                    sendMessage(chatId, categoryInMemory.printAllCategories());
                    break;
                case "/add_expense":
                    ExpenseOperation operation = new ExpenseOperation(chatId);
                    operations.put(chatId, operation);
                    executeMessage(operation.createOperation(update));
                    break;
                default:
                    sendMessage(chatId, "Sorry, command was not recognized.");
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            System.out.println(callBackData);

            if (operations.containsKey(chatId) && categoryInMemory.isCategorySelection(callBackData)) {
                EditMessageText ans = operations.get(chatId).afterSelectingCategory(update, messageId, callBackData);
                if (ans != null) {
                    try {
                        execute(ans);
                    } catch (TelegramApiException e) {
                        log.error("Error #0003: " + e.getMessage());
                    }
                }
            } else if (operations.containsKey(chatId)
                    && operations.get(chatId).checkIsInlineCalendarClicked(update)) {
                EditMessageText ans = operations.get(chatId).afterSelectingDate(update, messageId);
                if (ans != null) {
                    try {
                        execute(ans);
                    } catch (TelegramApiException e) {
                        log.error("Error #0003: " + e.getMessage());
                    }
                } else if (operations.get(chatId).hasValidDate()) {
                    EditMessageText message = new EditMessageText();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("Choose category:");
                    message.setMessageId((int) messageId);

                    InlineKeyboardMarkup markup = categoryInMemory.createInlineKeyboardMarkup();
                    message.setReplyMarkup(markup);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("Error #0003: " + e.getMessage());
                    }
                }
            } else if (callBackData.equals(YES_BUTTON)) {
                String text = "You pressed YES button;";
                executeMessageText(text, chatId, messageId);
            } else if (callBackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button;";
                executeMessageText(text, chatId, messageId);
            }
        }
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        var yes_btn = new InlineKeyboardButton();
        yes_btn.setText("Yes");
        yes_btn.setCallbackData(YES_BUTTON);

        var no_btn = new InlineKeyboardButton();
        no_btn.setText("No");
        no_btn.setCallbackData(NO_BUTTON);

        row.add(yes_btn);
        row.add(no_btn);
        rowsInLine.add(row);
        markup.setKeyboard(rowsInLine);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    private void registerPerson(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            Person person = new Person();
            person.setChatId(chatId);
            person.setFirstName(chat.getFirstName());
            person.setLastName(chat.getLastName());
            person.setUserName(chat.getUserName());
            person.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(person);
            log.info("person saved: " + person);
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

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("random");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("check my data");
        row.add("register");
        row.add("delete data");
        row.add("add my data");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    private void executeMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error #0003: " + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
