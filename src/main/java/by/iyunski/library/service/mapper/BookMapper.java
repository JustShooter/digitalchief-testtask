package by.iyunski.library.service.mapper;

import by.iyunski.library.persistence.model.Book;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.Year;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "yearOfPublication", expression = "java(yearToInteger(book.getYearOfPublication()))")
    BookDto toDto(Book book);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(BookRequestDto bookRequestDto, @MappingTarget Book book);

    @Mapping(target = "yearOfPublication", expression = "java(integerToYear(bookRequestDto.yearOfPublication()))")
    Book toEntity(BookRequestDto bookRequestDto);

    default Integer yearToInteger(Year year) {
        return year.getValue();
    }

    default Year integerToYear(Integer integer) {
        return Year.of(integer);
    }
}
