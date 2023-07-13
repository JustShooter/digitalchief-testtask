package by.iyunski.library.persistence.repository;

import by.iyunski.library.persistence.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
