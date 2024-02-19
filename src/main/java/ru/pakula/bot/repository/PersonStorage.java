package ru.pakula.bot.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.pakula.bot.model.Person;
import ru.pakula.bot.repository.UserRepository;

import java.sql.Timestamp;

@Slf4j
@Component
public class PersonStorage {

    @Autowired
    private UserRepository userRepository;

    public void registerPerson(Message msg) {
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
}
