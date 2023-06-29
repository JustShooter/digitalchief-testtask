package by.iyunski.library.service;

import by.iyunski.library.persistence.model.Author;
import by.iyunski.library.persistence.model.Book;
import by.iyunski.library.persistence.repository.AuthorRepository;
import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;
import by.iyunski.library.service.impl.AuthorServiceImpl;
import by.iyunski.library.service.mapper.AuthorMapper;
import by.iyunski.library.service.mapper.BookMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static by.iyunski.library.util.AuthorTestData.ID_1;
import static by.iyunski.library.util.AuthorTestData.INVALID_ID;
import static by.iyunski.library.util.AuthorTestData.getAuthorByAuthorRequestDto;
import static by.iyunski.library.util.AuthorTestData.getAuthorById;
import static by.iyunski.library.util.AuthorTestData.getAuthorDtoListByAuthorList;
import static by.iyunski.library.util.AuthorTestData.getAuthorList;
import static by.iyunski.library.util.AuthorTestData.getBookDtoListByBookList;
import static by.iyunski.library.util.AuthorTestData.getValidAuthorRequestDto;
import static by.iyunski.library.util.AuthorTestData.getValidBookRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
    @Mock
    private AuthorRepository authorRepository;
    @Spy
    private AuthorMapper authorMapper = Mappers.getMapper(AuthorMapper.class);
    @Spy
    private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);
    @InjectMocks
    private AuthorServiceImpl authorService;

    @Nested
    class GetAllAuthors {
        @Test
        void shouldReturnAllAuthors_whenInvoke_getAllAuthors() {

            List<Author> authors = getAuthorList();
            List<AuthorDto> authorDtos = getAuthorDtoListByAuthorList(authors);

            when(authorRepository.findAll()).thenReturn(authors);

            List<AuthorDto> allAuthors = authorService.getAllAuthors();

            assertEquals(authors.size(), allAuthors.size());
            assertIterableEquals(authorDtos, allAuthors);
        }

        @Test
        void shouldCallRepositoryAndMapper_whenInvoke_getAllAuthors() {

            List<Author> authors = getAuthorList();

            when(authorRepository.findAll()).thenReturn(authors);

            authorService.getAllAuthors();

            verify(authorRepository, times(1)).findAll();
            verify(authorMapper, times(authors.size())).toDto(any(Author.class));
        }

        @Test
        void shouldCallRepositoryAndTrowExceptionIfNoAuthorsInDb_whenInvoke_getAllAuthors() {

            when(authorRepository.findAll()).thenReturn(Collections.emptyList());

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getAllAuthors());

            assertEquals(HttpStatus.NO_CONTENT, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findAll();
        }
    }

    @Nested
    class SaveNewAuthor {
        @Test
        void shouldReturnSavedAuthor_whenSendAuthorRequestDtoAndInvoke_saveNewAuthor() {

            AuthorRequestDto validAuthorRequestDto = getValidAuthorRequestDto();
            Author savedAuthor = getAuthorByAuthorRequestDto(validAuthorRequestDto);

            when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

            AuthorDto authorDto = authorService.saveNewAuthor(validAuthorRequestDto);

            assertNotNull(savedAuthor.getId());
            assertEquals(savedAuthor.getFirstName(), authorDto.firstName());
            assertEquals(savedAuthor.getLastName(), authorDto.lastName());
            assertEquals(savedAuthor.getDateOfBirth(), authorDto.dateOfBirth());
            assertEquals(savedAuthor.getCountry(), authorDto.country());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorRequestDtoAndInvoke_saveNewAuthor() {

            AuthorRequestDto validAuthorRequestDto = getValidAuthorRequestDto();
            Author savedAuthor = getAuthorByAuthorRequestDto(validAuthorRequestDto);

            when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

            authorService.saveNewAuthor(validAuthorRequestDto);

            verify(authorRepository, times(1)).save(any(Author.class));
            verify(authorMapper, times(1)).toEntity(validAuthorRequestDto);
            verify(authorMapper, times(1)).toDto(savedAuthor);
        }
    }

    @Nested
    class GetAuthorById {
        @Test
        void shouldReturnAuthorDto_whenSendAuthorsIdAndInvoke_getAuthorById() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            AuthorDto authorById = authorService.getAuthorById(ID_1);

            assertEquals(author.get().getFirstName(), authorById.firstName());
            assertEquals(author.get().getLastName(), authorById.lastName());
            assertEquals(author.get().getDateOfBirth(), authorById.dateOfBirth());
            assertEquals(author.get().getCountry(), authorById.country());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndInvoke_getAuthorById() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            authorService.getAuthorById(ID_1);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorMapper, times(1)).toDto(author.get());
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_getAuthorById() {

            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getAuthorById(INVALID_ID));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
        }
    }

    @Nested
    class UpdateAuthor {
        @Test
        void shouldReturnAuthorDto_whenSendAuthorsIdAndAuthorRequestDtoAndInvoke_updateAuthor() {

            AuthorRequestDto authorRequestDto = getValidAuthorRequestDto();
            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Author savedAuthor = getAuthorByAuthorRequestDto(authorRequestDto);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);
            when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

            AuthorDto updatedAuthor = authorService.updateAuthor(ID_1, authorRequestDto);

            assertEquals(savedAuthor.getId(), updatedAuthor.id());
            assertEquals(authorRequestDto.firstName(), updatedAuthor.firstName());
            assertEquals(authorRequestDto.lastName(), updatedAuthor.lastName());
            assertEquals(authorRequestDto.dateOfBirth(), updatedAuthor.dateOfBirth());
            assertEquals(authorRequestDto.country(), updatedAuthor.country());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndAuthorRequestDtoAndInvoke_updateAuthor() {

            AuthorRequestDto authorRequestDto = getValidAuthorRequestDto();
            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Author savedAuthor = getAuthorByAuthorRequestDto(authorRequestDto);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);
            when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

            authorService.updateAuthor(ID_1, authorRequestDto);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, times(1)).save(any(Author.class));
            verify(authorMapper, times(1)).toDto(savedAuthor);
            verify(authorMapper, times(1)).partialUpdate(authorRequestDto, authorById.get());
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndAuthorRequestDtoAndInvoke_updateAuthor() {

            AuthorRequestDto authorRequestDto = getValidAuthorRequestDto();
            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.updateAuthor(INVALID_ID, authorRequestDto));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(authorRepository, never()).save(any(Author.class));
            verify(authorMapper, never()).toDto(any(Author.class));
            verify(authorMapper, never()).partialUpdate(any(AuthorRequestDto.class), any(Author.class));
        }
    }

    @Nested
    class DeleteAuthorById {
        @Test
        void shouldReturnDeletedAuthorId_whenSendAuthorsIdAndInvoke_deleteAuthorById() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            Long deletedAuthorsId = authorService.deleteAuthorById(ID_1);

            assertEquals(author.get().getId(), deletedAuthorsId);
            assertEquals(ID_1, deletedAuthorsId);
        }

        @Test
        void shouldCallRepository_whenSendAuthorsIdAndInvoke_deleteAuthorById() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            authorService.deleteAuthorById(ID_1);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, times(1)).delete(author.get());
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_deleteAuthorById() {

            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.deleteAuthorById(INVALID_ID));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(authorRepository, never()).delete(any(Author.class));
        }
    }

    @Nested
    class GetAllBooksOfAuthorByAuthorId {
        @Test
        void shouldReturnAllBookOfAuthor_whenSendAuthorsIdAndInvoke_getAllBooksOfAuthorByAuthorId() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));
            List<BookDto> bookDtoListByBookList = getBookDtoListByBookList(author.get().getBooks());

            when(authorRepository.findById(ID_1)).thenReturn(author);

            List<BookDto> books = authorService.getAllBooksOfAuthorByAuthorId(ID_1);

            assertEquals(author.get().getBooks().size(), books.size());
            assertIterableEquals(bookDtoListByBookList, books);
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndInvoke_getAllBooksOfAuthorByAuthorId() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            authorService.getAllBooksOfAuthorByAuthorId(ID_1);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(bookMapper, times(author.get().getBooks().size())).toDto(any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_getAllBooksOfAuthorByAuthorId() {

            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getAllBooksOfAuthorByAuthorId(INVALID_ID));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(bookMapper, never()).toDto(any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowExceptionIfNoBooksInDb_whenSendAuthorsIdAndInvoke_getAllBooksOfAuthorByAuthorId() {

            Optional<Author> author = Optional.of(getAuthorById(ID_1));
            author.ifPresent(a -> a.setBooks(Collections.emptyList()));

            when(authorRepository.findById(ID_1)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getAllBooksOfAuthorByAuthorId(ID_1));

            assertEquals(HttpStatus.NO_CONTENT, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(ID_1);
            verify(bookMapper, never()).toDto(any(Book.class));
        }
    }

    @Nested
    class GetBookOfAuthorByBookId {
        @Test
        void shouldReturnBookOfAuthor_whenSendAuthorsIdAndBooksIdAndInvoke_getBookOfAuthorByBookId() {
            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            BookDto bookOfAuthorByBookId = authorService.getBookOfAuthorByBookId(ID_1, book.getId());

            assertEquals(book.getId(), bookOfAuthorByBookId.id());
            assertEquals(book.getTitle(), bookOfAuthorByBookId.title());
            assertEquals(book.getGenre(), bookOfAuthorByBookId.genre());
            assertEquals(book.getYearOfPublication().getValue(), bookOfAuthorByBookId.yearOfPublication());
            assertEquals(book.getNumberOfPages(), bookOfAuthorByBookId.numberOfPages());
            assertEquals(book.getIsbn(), bookOfAuthorByBookId.isbn());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndBooksIdAndInvoke_getBookOfAuthorByBookId() {
            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            authorService.getBookOfAuthorByBookId(ID_1, book.getId());

            verify(authorRepository, times(1)).findById(ID_1);
            verify(bookMapper, times(1)).toDto(book);
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_getBookOfAuthorByBookId() {

            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getBookOfAuthorByBookId(INVALID_ID, ID_1));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(bookMapper, never()).toDto(any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidBookIdAndInvoke_getBookOfAuthorByBookId() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class, () -> authorService.getBookOfAuthorByBookId(ID_1, INVALID_ID));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(ID_1);
            verify(bookMapper, never()).toDto(any(Book.class));
        }
    }

    @Nested
    class SaveNewBookOfAuthorByAuthorId {
        @Test
        void shouldReturnSavedBookOfAuthor_whenSendAuthorsIdAndBookRequestDtoAndInvoke_saveNewBookOfAuthorByAuthorId() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            BookDto bookDto = authorService.saveNewBookOfAuthorByAuthorId(ID_1, validBookRequestDto);

            assertEquals(validBookRequestDto.title(), bookDto.title());
            assertEquals(validBookRequestDto.genre(), bookDto.genre());
            assertEquals(validBookRequestDto.yearOfPublication(), bookDto.yearOfPublication());
            assertEquals(validBookRequestDto.numberOfPages(), bookDto.numberOfPages());
            assertEquals(validBookRequestDto.isbn(), bookDto.isbn());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndBookRequestDtoAndInvoke_saveNewBookOfAuthorByAuthorId() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            authorService.saveNewBookOfAuthorByAuthorId(ID_1, validBookRequestDto);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, times(1)).save(any(Author.class));
            verify(bookMapper, times(1)).toEntity(validBookRequestDto);
            verify(bookMapper, times(1)).toDto(any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_saveNewBookOfAuthorByAuthorId() {

            Optional<Author> author = Optional.empty();
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class,
                            () -> authorService.saveNewBookOfAuthorByAuthorId(INVALID_ID, validBookRequestDto));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(authorRepository, never()).save(any(Author.class));
            verify(bookMapper, never()).toDto(any(Book.class));
            verify(bookMapper, never()).toEntity(validBookRequestDto);
        }
    }

    @Nested
    class UpdateAuthorsBook {
        @Test
        void shouldReturnUpdatedBookOfAuthor_whenSendAuthorsIdAndBooksIDAndBookRequestDtoAndInvoke_updateAuthorsBook() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            BookDto bookDto = authorService.updateAuthorsBook(ID_1, book.getId(), validBookRequestDto);

            assertEquals(book.getId(), bookDto.id());
            assertEquals(validBookRequestDto.title(), bookDto.title());
            assertEquals(validBookRequestDto.genre(), bookDto.genre());
            assertEquals(validBookRequestDto.yearOfPublication(), bookDto.yearOfPublication());
            assertEquals(validBookRequestDto.numberOfPages(), bookDto.numberOfPages());
            assertEquals(validBookRequestDto.isbn(), bookDto.isbn());
        }

        @Test
        void shouldCallRepositoryAndMapper_whenSendAuthorsIdAndBooksIDAndBookRequestDtoAndInvoke_updateAuthorsBook() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            authorService.updateAuthorsBook(ID_1, book.getId(), validBookRequestDto);

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, times(1)).save(any(Author.class));
            verify(bookMapper, times(1)).partialUpdate(validBookRequestDto, book);
            verify(bookMapper, times(1)).toDto(any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_updateAuthorsBook() {

            Optional<Author> author = Optional.empty();
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class,
                            () -> authorService.updateAuthorsBook(INVALID_ID, ID_1, validBookRequestDto));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(authorRepository, never()).save(any(Author.class));
            verify(bookMapper, never()).toEntity(validBookRequestDto);
            verify(bookMapper, never()).toDto(any(Book.class));
            verify(bookMapper, never()).partialUpdate(eq(validBookRequestDto), any(Book.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidBookIdAndInvoke_updateAuthorsBook() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class,
                            () -> authorService.updateAuthorsBook(ID_1, INVALID_ID, validBookRequestDto));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, never()).save(any(Author.class));
            verify(bookMapper, never()).toEntity(validBookRequestDto);
            verify(bookMapper, never()).toDto(any(Book.class));
            verify(bookMapper, never()).partialUpdate(eq(validBookRequestDto), any(Book.class));
        }
    }

    @Nested
    class DeleteAuthorsBookById {
        @Test
        void shouldReturnDeletedBookId_whenSendAuthorsIdAndBooksIdAndInvoke_deleteAuthorsBookById() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            Long deletedBookId = authorService.deleteAuthorsBookById(ID_1, book.getId());

            assertEquals(book.getId(), deletedBookId);
        }

        @Test
        void shouldCallRepository_whenSendAuthorsIdAndBooksIdAndInvoke_deleteAuthorsBookById() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));
            Book book = authorById.get().getBooks().get(0);

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            authorService.deleteAuthorsBookById(ID_1, book.getId());

            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, times(1)).save(any(Author.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidAuthorsIdAndInvoke_deleteAuthorsBookById() {

            Optional<Author> author = Optional.empty();

            when(authorRepository.findById(INVALID_ID)).thenReturn(author);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class,
                            () -> authorService.deleteAuthorsBookById(INVALID_ID, ID_1));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(INVALID_ID);
            verify(authorRepository, never()).save(any(Author.class));
        }

        @Test
        void shouldCallRepositoryAndTrowException_whenSendInvalidBookIdAndInvoke_deleteAuthorsBookById() {

            Optional<Author> authorById = Optional.of(getAuthorById(ID_1));

            when(authorRepository.findById(ID_1)).thenReturn(authorById);

            ErrorResponseException errorResponseException =
                    assertThrows(ErrorResponseException.class,
                            () -> authorService.deleteAuthorsBookById(ID_1, INVALID_ID));

            assertEquals(HttpStatus.NOT_FOUND, errorResponseException.getStatusCode());
            verify(authorRepository, times(1)).findById(ID_1);
            verify(authorRepository, never()).save(any(Author.class));
        }
    }
}