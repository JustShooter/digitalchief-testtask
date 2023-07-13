package by.iyunski.library.service;

import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> getAllAuthors();

    AuthorDto saveNewAuthor(AuthorRequestDto authorDto);

    AuthorDto getAuthorById(Long id);

    AuthorDto updateAuthor(Long id, AuthorRequestDto authorRequestDto);

    Long deleteAuthorById(Long id);

    List<BookDto> getAllBooksOfAuthorByAuthorId(Long id);

    BookDto getBookOfAuthorByBookId(Long authorId, Long bookId);

    BookDto saveNewBookOfAuthorByAuthorId(Long id, BookRequestDto bookRequestDto);

    BookDto updateAuthorsBook(Long authorId, Long bookId, BookRequestDto bookRequestDto);

    Long deleteAuthorsBookById(Long authorId, Long bookId);
}
