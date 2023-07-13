package by.iyunski.library.service.dtos;

import java.io.Serializable;

/**
 * DTO for {@link by.iyunski.library.persistence.model.Book}
 */
public record BookDto(Long id,
                      String title,
                      String genre,
                      Integer yearOfPublication,
                      Integer numberOfPages,
                      String isbn) implements Serializable {
}