package by.iyunski.library.service.dtos;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link by.iyunski.library.persistence.model.Author}
 */
public record AuthorDto(Long id,
                        String firstName,
                        String lastName,
                        LocalDate dateOfBirth,
                        String country) implements Serializable {
}