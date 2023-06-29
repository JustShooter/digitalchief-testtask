package by.iyunski.library.service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link by.iyunski.library.persistence.model.Author}
 */
public record AuthorRequestDto(
        @NotBlank
        @Size(max = 50)
        String firstName,
        @NotBlank
        @Size(max = 50)
        String lastName,
        @NotNull
        @Past LocalDate dateOfBirth,
        @NotBlank
        @Size(max = 50)
        String country) implements Serializable {
}
