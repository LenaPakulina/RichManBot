package ru.pakula.bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.pakula.bot.model.Category;

public interface CategoryRepository extends CrudRepository<Category, Long> {

}
