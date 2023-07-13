package by.iyunski.library.api.controllers;

import by.iyunski.library.service.AuthorService;
import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Author Controller",
        description = "Library API for working with Authors and their books")
@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthorApiController {

    private final AuthorService authorService;

    @Operation(
            summary = "Get all authors",
            description = "Get list of all authors",
            responses = {@ApiResponse(
                    responseCode = "200",
                    description = "The request has succeeded.",
                    useReturnTypeSchema = true
            ), @ApiResponse(
                    responseCode = "204",
                    description = "There is no Authors in Library yet."
            )})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuthorDto>> getAllAuthors() {
        List<AuthorDto> authorDtoList = authorService.getAllAuthors();
        log.debug("Getting {} authors from database ", authorDtoList.size());
        return new ResponseEntity<>(authorDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Save new Author")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorDto> save(@RequestBody @Valid AuthorRequestDto authorRequestDto) {
        log.debug("Input data for creating new Author: {} ", authorRequestDto);
        AuthorDto savedAuthorDto = authorService.saveNewAuthor(authorRequestDto);
        return new ResponseEntity<>(savedAuthorDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Author by ID")
    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorDto> getAuthor(@PathVariable(name = "id") Long id) {
        log.debug("Getting author with id {}", id);
        AuthorDto authorDto = authorService.getAuthorById(id);
        return new ResponseEntity<>(authorDto, HttpStatus.OK);
    }

    @Operation(summary = "Update Author by ID")
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorDto> updateAuthor(@PathVariable(name = "id") Long id, @RequestBody @Valid AuthorRequestDto authorRequestDto) {
        log.debug("Updating author with id {} with input data {}", id, authorRequestDto);
        AuthorDto authorDto = authorService.updateAuthor(id, authorRequestDto);
        return new ResponseEntity<>(authorDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete Author by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteAuthor(@PathVariable(name = "id") Long id) {
        log.debug("Deleting author with ID: {}", id);
        Long deletedAuthorId = authorService.deleteAuthorById(id);
        return new ResponseEntity<>(deletedAuthorId, HttpStatus.OK);
    }

    @Operation(summary = "Get all book of author",
            description = "Get list of all book of author by its ID")
    @GetMapping(value = "/{id}/books",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookDto>> getAllBooksOfAuthor(@PathVariable(name = "id") Long id) {
        log.debug("Getting all books of author with ID: {}", id);
        List<BookDto> bookDtoList = authorService.getAllBooksOfAuthorByAuthorId(id);
        return new ResponseEntity<>(bookDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Get Book by ID of Author by ID")
    @GetMapping(value = "/{id}/books/{bookId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> getBookOfAuthor(@PathVariable(name = "id") Long authorId, @PathVariable(name = "bookId") Long bookId) {
        log.debug("Getting book with ID: {} of author with ID: {}", bookId, authorId);
        BookDto bookDto = authorService.getBookOfAuthorByBookId(authorId, bookId);
        return new ResponseEntity<>(bookDto, HttpStatus.OK);
    }

    @Operation(summary = "Save new Book of Author by ID")
    @PostMapping(value = "/{id}/books",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> saveNewBookOfAuthor(@PathVariable(name = "id") Long id,
                                                       @RequestBody @Valid BookRequestDto bookRequestDto) {
        log.debug("Saving new book of author with ID: {}, with input data: {} ", id, bookRequestDto);
        BookDto bookDto = authorService.saveNewBookOfAuthorByAuthorId(id, bookRequestDto);
        return new ResponseEntity<>(bookDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Book with ID of Author with ID")
    @PutMapping(value = "/{id}/books/{bookId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDto> updateAuthorsBook(@PathVariable(name = "id") Long authorId,
                                                     @PathVariable(name = "bookId") Long bookId,
                                                     @RequestBody @Valid BookRequestDto bookRequestDto) {
        BookDto bookDto = authorService.updateAuthorsBook(authorId, bookId, bookRequestDto);
        return new ResponseEntity<>(bookDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete Book with ID of Author with ID")
    @DeleteMapping("/{id}/books/{bookId}")
    public ResponseEntity<Long> deleteBookOfAuthor(@PathVariable(name = "id") Long authorId,
                                                   @PathVariable(name = "bookId") Long bookId) {
        log.debug("Deleting book with ID: {} of author with ID: {}", bookId, authorId);
        Long deletedBookId = authorService.deleteAuthorsBookById(authorId, bookId);
        return new ResponseEntity<>(deletedBookId, HttpStatus.OK);
    }
}
