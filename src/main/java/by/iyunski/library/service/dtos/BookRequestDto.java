package by.iyunski.library.service.dtos;

import by.iyunski.library.service.annotations.YearValidation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link by.iyunski.library.persistence.model.Book}
 */
public record BookRequestDto(@Size(max = 255)
                             @NotEmpty
                             String title,
                             @Size(max = 50)
                             @NotEmpty
                             String genre,
                             @NotNull
                             @YearValidation
                             Integer yearOfPublication,
                             @NotNull
                             @Min(1)
                             @Max(9999)
                             Integer numberOfPages,
                             @Pattern(regexp = "^97[89][0-9]{10}$",
                                     message = "ISBN must contain only digits, be a length of 13 digits and start with 978 or 979")
                             @NotNull
                             String isbn) implements Serializable {
}
