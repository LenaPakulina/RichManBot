package ru.pakula.bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pakula.bot.model.Person;

public interface UserRepository extends CrudRepository<Person, Long> {
}
