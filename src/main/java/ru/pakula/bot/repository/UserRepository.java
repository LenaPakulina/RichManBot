package ru.pakula.bot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.pakula.bot.model.Person;

public interface UserRepository extends CrudRepository<Person, Long> {
}
