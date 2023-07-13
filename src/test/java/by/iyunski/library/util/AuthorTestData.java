package by.iyunski.library.util;

import by.iyunski.library.persistence.model.Author;
import by.iyunski.library.persistence.model.Book;
import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AuthorTestData {


    public static final String VALID_FIRST_NAME = "John";
    public static final String VALID_LAST_NAME = "Smith";
    public static final LocalDate VALID_DATE_OF_BIRTH = LocalDate.of(1950, 5, 20);
    public static final String VALID_COUNTRY = "England";
    public static final String INVALID_LENGTH_STRING = "Invalidnamenamenamenamenamenamenamenamenamenamename";
    public static final Integer YEAR_OF_PUBLICATION_IN_FUTURE = 2999;
    public static final Integer NULL_YEAR = null;
    public static final LocalDate INVALID_DATE_OF_BIRTH_FUTURE = LocalDate.of(2999, 1, 1);
    public static final String NULL_STRING = null;
    public static final String EMPTY_STRING = "";
    public static final LocalDate NULL_LOCALDATE = null;
    public static final long ID_1 = 1L;
    public static final long ID_2 = 2L;
    public static final long INVALID_ID = 999L;
    public static final String VALID_TITLE = "Best Novell";
    public static final String VALID_GENRE = "Science Fiction";
    public static final Integer VALID_YEAR_OF_PUBLICATION = 1998;
    public static final int VALID_NUMBER_OF_PAGES = 100;
    public static final String INVALID_TITLE = "Invalid titletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitletitle";
    public static final String VALID_ISBN = "9781234567890";
    public static final String ANOTHER_FIRST_NAME = "Alex";
    public static final String ANOTHER_LAST_NAME = "Jonson";
    public static final LocalDate ANOTHER_DATE_OF_BIRTH = LocalDate.of(1970, 1, 7);
    public static final String ANOTHER_COUNTRY = "USA";

    public static AuthorDto getAuthorDto() {
        return new AuthorDto(ID_1, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);
    }

    public static AuthorRequestDto getValidAuthorRequestDto() {
        return new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);
    }

    public static BookDto getBookDto() {
        return new BookDto(ID_1, VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);
    }

    public static BookRequestDto getValidBookRequestDto() {
        return new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);
    }

    public static BookDto getBookDtoByBookRequestDto(BookRequestDto bookRequestDto) {
        return new BookDto(ID_1,
                bookRequestDto.title(),
                bookRequestDto.genre(),
                bookRequestDto.yearOfPublication(),
                bookRequestDto.numberOfPages(),
                bookRequestDto.isbn());
    }

    public static AuthorDto getAuthorDtoByAuthorRequestDto(AuthorRequestDto validAuthorRequestDto) {
        return new AuthorDto(ID_1,
                validAuthorRequestDto.firstName(),
                validAuthorRequestDto.lastName(),
                validAuthorRequestDto.dateOfBirth(),
                validAuthorRequestDto.country());
    }

    public static List<AuthorDto> getAuthorDtoList() {
        List<AuthorDto> authorDtoList = new ArrayList<>();
        authorDtoList.add(getAuthorDto());
        authorDtoList.add(new AuthorDto(ID_2, ANOTHER_FIRST_NAME, ANOTHER_LAST_NAME, ANOTHER_DATE_OF_BIRTH, ANOTHER_COUNTRY));
        return authorDtoList;
    }

    public static List<BookDto> getBookDtoList() {
        List<BookDto> bookDtoList = new ArrayList<>();
        bookDtoList.add(getBookDto());
        bookDtoList.add(new BookDto(ID_2, "Best Novell 2", VALID_GENRE, 2020, 200, "9782345678901"));
        return bookDtoList;
    }

    public static List<Author> getAuthorList() {
        return Stream
                .generate(AuthorTestData::getAuthorWithRandomFields)
                .limit(5)
                .toList();
    }

    public static List<AuthorDto> getAuthorDtoListByAuthorList(List<Author> authors) {
        return authors.stream()
                .map(AuthorTestData::getAuthorDtoByAuthor)
                .toList();
    }

    public static Author getAuthorByAuthorRequestDto(AuthorRequestDto authorRequestDto) {
        Author author = new Author();
        author.setId(getRandomId());
        author.setFirstName(authorRequestDto.firstName());
        author.setLastName(authorRequestDto.lastName());
        author.setDateOfBirth(authorRequestDto.dateOfBirth());
        author.setCountry(authorRequestDto.country());
        return author;
    }

    private static AuthorDto getAuthorDtoByAuthor(Author author) {
        return new AuthorDto(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getDateOfBirth(),
                author.getCountry());
    }

    public static Author getAuthorWithRandomFields() {
        Author author = new Author();
        author.setId(getRandomId());
        author.setFirstName(getRandomAlphabeticString());
        author.setLastName(getRandomAlphabeticString());
        author.setDateOfBirth(getValidRandomDate());
        author.setCountry(getRandomAlphabeticString());
        author.setBooks(getListOfRandomsBook(author));
        return author;
    }

    public static Author getAuthorById(Long id) {
        Author author = getAuthorWithRandomFields();
        author.setId(id);
        author.setBooks(getListOfRandomsBook(author));
        return author;
    }

    public static List<BookDto> getBookDtoListByBookList(List<Book> books) {
        return books.stream()
                .map(AuthorTestData::getBookDtoByBook)
                .toList();
    }

    private static long getRandomId() {
        return RandomGenerator.getDefault().nextLong(1, 50);
    }

    private static LocalDate getValidRandomDate() {
        long minDay = LocalDate.of(1910, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        long randomDay = RandomGenerator.getDefault().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    private static String getRandomAlphabeticString() {
        int leftLimit = 97;
        int rightLimit = 123;
        int maxSize = 10;
        return RandomGenerator.getDefault().ints(leftLimit, rightLimit)
                .limit(maxSize)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static List<Book> getListOfRandomsBook(Author author) {
        return Stream
                .generate(() -> getBookWithRandomFields(author))
                .limit(5)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Book getBookWithRandomFields(Author author) {
        Book book = new Book();
        book.setId(getRandomId());
        book.setTitle(getRandomAlphabeticString());
        book.setGenre(getRandomAlphabeticString());
        book.setYearOfPublication(Year.of(getValidRandomDate().getYear()));
        book.setNumberOfPages(RandomGenerator.getDefault().nextInt(1, 500));
        book.setIsbn(getValidRandomIsbn());
        book.setAuthor(author);
        return book;
    }

    private static String getValidRandomIsbn() {

        int[] digits = new int[13];
        digits[0] = 9;
        digits[1] = 7;
        digits[2] = RandomGenerator.getDefault().nextInt(2) + 8;

        for (int i = 3; i < 12; i++) {
            digits[i] = RandomGenerator.getDefault().nextInt(10);
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += digits[i] * (i % 2 == 0 ? 1 : 3);
        }
        int checkDigit = 10 - (sum % 10);
        if (checkDigit == 10) {
            checkDigit = 0;
        }

        digits[12] = checkDigit;

        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }

    private static BookDto getBookDtoByBook(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getGenre(),
                book.getYearOfPublication().getValue(),
                book.getNumberOfPages(),
                book.getIsbn());
    }
}
