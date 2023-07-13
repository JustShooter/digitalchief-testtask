package by.iyunski.library.service.impl;

import by.iyunski.library.persistence.model.Author;
import by.iyunski.library.persistence.model.Book;
import by.iyunski.library.persistence.repository.AuthorRepository;
import by.iyunski.library.service.AuthorService;
import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;
import by.iyunski.library.service.mapper.AuthorMapper;
import by.iyunski.library.service.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;

    public static final String AUTHOR_WITH_ID_NOT_FOUND = "Author with ID: {} not found";
    public static final String BOOK_WITH_ID_OF_AUTHOR_WITH_ID_NOT_FOUND = "Book with ID: {} of author with ID: {} not found";

    @Override
    public List<AuthorDto> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        if (authors.isEmpty()) {
            log.warn("There is no authors in DB");
            throw new ErrorResponseException(HttpStatus.NO_CONTENT);
        } else {
            return authors.stream()
                    .map(authorMapper::toDto)
                    .toList();
        }
    }

    @Override
    public AuthorDto saveNewAuthor(AuthorRequestDto authorRequestDto) {
        Author author = authorMapper.toEntity(authorRequestDto);
        Author savedAuthor = authorRepository.save(author);
        return authorMapper.toDto(savedAuthor);
    }

    @Override
    public AuthorDto getAuthorById(Long id) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isPresent()) {
            return authorMapper.toDto(author.get());
        } else {
            log.warn("There is no author in DB with ID: {}", id);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorRequestDto authorRequestDto) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isPresent()) {
            Author updatedAuthor = authorMapper.partialUpdate(authorRequestDto, author.get());
            Author savedAuthor = authorRepository.save(updatedAuthor);
            return authorMapper.toDto(savedAuthor);
        } else {
            log.warn("Author cannot be updated, because there is no author in DB with ID: {}", id);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public Long deleteAuthorById(Long id) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isPresent()) {
            authorRepository.delete(author.get());
            return id;
        } else {
            log.warn("Author cannot be deleted, because there is no author in DB with ID: {}", id);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public List<BookDto> getAllBooksOfAuthorByAuthorId(Long id) {
        Optional<Author> author = authorRepository.findById(id);
        List<Book> books;
        if (author.isPresent()) {
            books = author.get().getBooks();
        } else {
            log.warn("There is no author in DB with ID: {}", id);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
        if (books.isEmpty()) {
            log.warn("Author with ID: {} have no books", id);
            throw new ErrorResponseException(HttpStatus.NO_CONTENT);
        } else {
            return books.stream()
                    .map(bookMapper::toDto)
                    .toList();
        }
    }

    @Override
    @Transactional
    public BookDto getBookOfAuthorByBookId(Long authorId, Long bookId) {
        Optional<Author> author = authorRepository.findById(authorId);
        Optional<Book> optionalBook = getOptionalBook(authorId, bookId, author);
        if (optionalBook.isPresent()) {
            return bookMapper.toDto(optionalBook.get());
        } else {
            log.warn(BOOK_WITH_ID_OF_AUTHOR_WITH_ID_NOT_FOUND, bookId, authorId);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public BookDto saveNewBookOfAuthorByAuthorId(Long id, BookRequestDto bookRequestDto) {
        Optional<Author> author = authorRepository.findById(id);
        if (author.isPresent()) {
            List<Book> books = author.get().getBooks();
            Book book = bookMapper.toEntity(bookRequestDto);
            book.setAuthor(author.get());
            books.add(book);
            authorRepository.save(author.get());
            return bookMapper.toDto(book);
        } else {
            log.warn(AUTHOR_WITH_ID_NOT_FOUND, id);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public BookDto updateAuthorsBook(Long authorId, Long bookId, BookRequestDto bookRequestDto) {
        Optional<Author> author = authorRepository.findById(authorId);
        Optional<Book> optionalBook = getOptionalBook(authorId, bookId, author);
        if (optionalBook.isPresent()) {
            bookMapper.partialUpdate(bookRequestDto, optionalBook.get());
            authorRepository.save(author.get());
            return bookMapper.toDto(optionalBook.get());
        } else {
            log.warn(BOOK_WITH_ID_OF_AUTHOR_WITH_ID_NOT_FOUND, bookId, authorId);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public Long deleteAuthorsBookById(Long authorId, Long bookId) {
        Optional<Author> author = authorRepository.findById(authorId);
        Optional<Book> optionalBook = getOptionalBook(authorId, bookId, author);
        if (optionalBook.isPresent()) {
            author.get().getBooks().remove(optionalBook.get());
            authorRepository.save(author.get());
            return (optionalBook.get()).getId();
        } else {
            log.warn(BOOK_WITH_ID_OF_AUTHOR_WITH_ID_NOT_FOUND, bookId, authorId);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
    }

    private static Optional<Book> getOptionalBook(Long authorId, Long bookId, Optional<Author> author) {
        Optional<Book> optionalBook;
        if (author.isPresent()) {
            optionalBook = author.get().getBooks().stream()
                    .filter(b -> bookId.equals(b.getId()))
                    .findFirst();
        } else {
            log.warn(AUTHOR_WITH_ID_NOT_FOUND, authorId);
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
        return optionalBook;
    }
}
