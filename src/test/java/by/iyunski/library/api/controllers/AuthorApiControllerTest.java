package by.iyunski.library.api.controllers;

import by.iyunski.library.service.AuthorService;
import by.iyunski.library.service.dtos.AuthorDto;
import by.iyunski.library.service.dtos.AuthorRequestDto;
import by.iyunski.library.service.dtos.BookDto;
import by.iyunski.library.service.dtos.BookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.ErrorResponseException;

import java.util.List;
import java.util.stream.Stream;

import static by.iyunski.library.util.AuthorTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthorApiController.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthorApiControllerTest {
    public static final String API_V_1_AUTHORS = "/api/v1/authors";
    public static final String API_V_1_AUTHORS_ID = "/api/v1/authors/{id}";
    public static final String API_V_1_AUTHORS_ID_BOOKS = "/api/v1/authors/{id}/books";
    public static final String API_V_1_AUTHORS_ID_BOOKS_ID = "/api/v1/authors/{id}/books/{id}";
    public static final String ISBN_VALIDATION_ERROR_MESSAGE = "ISBN must contain only digits, be a length of 13 digits and start with 978 or 979";
    public static final String MUST_BE_A_PAST_DATE = "must be a past date";
    public static final String MUST_NOT_BE_BLANK = "must not be blank";
    public static final String MUST_NOT_BE_NULL = "must not be null";
    public static final String MUST_NOT_BE_EMPTY = "must not be empty";
    public static final String SIZE_MUST_BE_BETWEEN_0_AND_50 = "size must be between 0 and 50";
    public static final String YEAR_VALIDATION_ERROR_MESSAGE = "must be a past date and be in range from the first to the current year";
    public static final String MUST_BE_LESS_THAN_OR_EQUAL_TO_9999 = "must be less than or equal to 9999";
    public static final String MUST_BE_GREATER_THAN_OR_EQUAL_TO_1 = "must be greater than or equal to 1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @Nested
    class GetAll {
        @Test
        void shouldReturn200AndCallBusinessLogicWhenGetAllAuthorInvoke() throws Exception {

            List<AuthorDto> authorDtoList = getAuthorDtoList();

            when(authorService.getAllAuthors()).thenReturn(authorDtoList);

            MvcResult mvcResult = mockMvc.perform(
                    get(
                            API_V_1_AUTHORS
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(authorDtoList))
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).getAllAuthors();

            String contentAsString = mvcResult.getResponse().getContentAsString();

            assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(authorDtoList));
        }

        @Test
        void shouldReturn204IfThereIsNoAuthorsInDb() throws Exception {

            when(authorService.getAllAuthors())
                    .thenThrow(new ErrorResponseException(HttpStatus.NO_CONTENT));

            mockMvc.perform(
                            get(
                                    API_V_1_AUTHORS
                            ).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(authorService, times(1)).getAllAuthors();
        }
    }

    @Nested
    class GetAuthorById {
        @Test
        void shouldReturn200AndCallBusinessLogicWhenGetAuthorByIdInvoke() throws Exception {

            AuthorDto authorDto = getAuthorDto();

            when(authorService.getAuthorById(ID_1)).thenReturn(authorDto);

            MvcResult mvcResult = mockMvc.perform(
                            get(
                                    API_V_1_AUTHORS_ID,
                                    ID_1
                            ).contentType(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(authorDto.id()))
                    .andExpect(jsonPath("firstName").value(authorDto.firstName()))
                    .andExpect(jsonPath("lastName").value(authorDto.lastName()))
                    .andExpect(jsonPath("dateOfBirth").value(authorDto.dateOfBirth().toString()))
                    .andExpect(jsonPath("country").value(authorDto.country())).andReturn();

            verify(authorService, times(1)).getAuthorById(ID_1);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            Assertions.assertThat(contentAsString)
                    .isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(authorDto));
        }

        @Test
        void shouldReturn404WhenGetAuthorWithInvalidId() throws Exception {

            when(authorService.getAuthorById(INVALID_ID))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                            get(
                                    API_V_1_AUTHORS_ID,
                                    INVALID_ID
                            ).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(authorService, times(1)).getAuthorById(INVALID_ID);
        }
    }

    @Nested
    class PostAuthor {
        @Test
        void shouldReturnAddedAuthorWhenSaveValidAuthor() throws Exception {

            AuthorRequestDto validAuthorRequestDto = getValidAuthorRequestDto();
            AuthorDto authorDto = getAuthorDtoByAuthorRequestDto(validAuthorRequestDto);

            when(authorService.saveNewAuthor(validAuthorRequestDto)).thenReturn(authorDto);

            MvcResult mvcResult = mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    validAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(validAuthorRequestDto)))
                    .andExpect(status().isCreated()).andReturn();

            verify(authorService, times(1)).saveNewAuthor(validAuthorRequestDto);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(authorDto));
        }
    }

    @Nested
    class ValidationPostAuthor {
        @Test
        void shouldReturn400WhenSaveAuthorWithInvalidFirstnameLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(INVALID_LENGTH_STRING, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("firstName", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithInvalidLastnameLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, INVALID_LENGTH_STRING, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("lastName", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithDateOfBirthInFuture() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, INVALID_DATE_OF_BIRTH_FUTURE, VALID_COUNTRY);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("dateOfBirth", Is.is(MUST_BE_A_PAST_DATE)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithInvalidCountryLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, INVALID_LENGTH_STRING);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("country", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithNullFirstname() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(NULL_STRING, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            post
                                    (API_V_1_AUTHORS,
                                            invalidAuthorRequestDto
                                    ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("firstName", Is.is(MUST_NOT_BE_BLANK)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithNullLastname() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, NULL_STRING, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("lastName", Is.is(MUST_NOT_BE_BLANK)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithNullDateOfBirth() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, NULL_LOCALDATE, VALID_COUNTRY);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("dateOfBirth", Is.is(MUST_NOT_BE_NULL)));
        }

        @Test
        void shouldReturn400WhenSaveAuthorWithNullCountry() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, NULL_STRING);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("country", Is.is(MUST_NOT_BE_BLANK)));
        }
    }

    @Nested
    class PutAuthor {
        @Test
        void shouldReturnUpdatedAuthorWhenUpdateValidAuthor() throws Exception {

            AuthorRequestDto validAuthorRequestDto = getValidAuthorRequestDto();
            AuthorDto authorDto = getAuthorDtoByAuthorRequestDto(validAuthorRequestDto);

            when(authorService.updateAuthor(ID_1, validAuthorRequestDto)).thenReturn(authorDto);

            MvcResult mvcResult = mockMvc.perform(
                    put(
                            API_V_1_AUTHORS_ID,
                            ID_1,
                            validAuthorRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validAuthorRequestDto))
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).updateAuthor(ID_1, validAuthorRequestDto);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(authorDto));
        }

        @Test
        void shouldReturn404WhenUpdateAuthorWithInvalidId() throws Exception {

            AuthorRequestDto validAuthorRequestDto = getValidAuthorRequestDto();

            when(authorService.updateAuthor(INVALID_ID, validAuthorRequestDto))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    put(
                            API_V_1_AUTHORS_ID,
                            INVALID_ID,
                            validAuthorRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validAuthorRequestDto))
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).updateAuthor(INVALID_ID, validAuthorRequestDto);
        }
    }

    @Nested
    class ValidationPutAuthor {
        @Test
        void shouldReturn400WhenUpdateAuthorWithInvalidFirstnameLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(INVALID_LENGTH_STRING, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("firstName", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithInvalidLastnameLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, INVALID_LENGTH_STRING, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("lastName", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithDateOfBirthInFuture() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, INVALID_DATE_OF_BIRTH_FUTURE, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("dateOfBirth", Is.is(MUST_BE_A_PAST_DATE)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithInvalidCountryLength() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, INVALID_LENGTH_STRING);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("country", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithNullFirstname() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(NULL_STRING, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest()).andExpect(content()
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("firstName", Is.is(MUST_NOT_BE_BLANK)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithNullLastname() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, NULL_STRING, VALID_DATE_OF_BIRTH, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("lastName", Is.is(MUST_NOT_BE_BLANK)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithNullDateOfBirth() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, NULL_LOCALDATE, VALID_COUNTRY);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("dateOfBirth", Is.is(MUST_NOT_BE_NULL)));
        }

        @Test
        void shouldReturn400WhenUpdateAuthorWithNullCountry() throws Exception {

            AuthorRequestDto invalidAuthorRequestDto = new AuthorRequestDto(VALID_FIRST_NAME, VALID_LAST_NAME, VALID_DATE_OF_BIRTH, NULL_STRING);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID,
                                    ID_1,
                                    invalidAuthorRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidAuthorRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("country", Is.is(MUST_NOT_BE_BLANK)));
        }
    }

    @Nested
    class DeleteAuthorById {
        @Test
        void shouldReturn200AndCallBusinessLogicWhenDeleteAuthorByIdInvoke() throws Exception {

            when(authorService.deleteAuthorById(ID_1)).thenReturn(ID_1);

            MvcResult mvcResult = mockMvc.perform(
                            delete(
                                    API_V_1_AUTHORS_ID,
                                    ID_1
                            ).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).deleteAuthorById(ID_1);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            Assertions.assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(ID_1));
        }

        @Test
        void shouldReturn404WhenDeleteAuthorWithInvalidId() throws Exception {

            when(authorService.deleteAuthorById(INVALID_ID))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    delete(
                            API_V_1_AUTHORS_ID,
                            INVALID_ID
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).deleteAuthorById(INVALID_ID);
        }
    }

    @Nested
    class GetAllBooksOfAuthor {
        @Test
        void shouldReturn200AndCallBusinessLogicWhenGetAllBookOfAuthorInvoke() throws Exception {

            List<BookDto> bookDtoList = getBookDtoList();

            when(authorService.getAllBooksOfAuthorByAuthorId(ID_1)).thenReturn(bookDtoList);

            MvcResult mvcResult = mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS,
                            ID_1
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).getAllBooksOfAuthorByAuthorId(ID_1);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            Assertions.assertThat(contentAsString)
                    .isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(bookDtoList));
        }

        @Test
        void shouldReturn204IfAuthorHasNoBooksInDb() throws Exception {

            when(authorService.getAllBooksOfAuthorByAuthorId(ID_1))
                    .thenThrow(new ErrorResponseException(HttpStatus.NO_CONTENT));

            mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS,
                            ID_1
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNoContent());

            verify(authorService, times(1)).getAllBooksOfAuthorByAuthorId(ID_1);
        }

        @Test
        void shouldReturn404WhenGetAuthorWithInvalidId() throws Exception {

            when(authorService.getAllBooksOfAuthorByAuthorId(INVALID_ID))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS,
                            INVALID_ID
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).getAllBooksOfAuthorByAuthorId(INVALID_ID);
        }
    }

    @Nested
    class GetBookOfAuthorById {
        @Test
        void shouldReturn200AndCallBusinessLogicWhenGetBookOfAuthorInvoke() throws Exception {

            BookDto bookDto = getBookDto();

            when(authorService.getBookOfAuthorByBookId(ID_1, ID_1)).thenReturn(bookDto);

            MvcResult mvcResult = mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            ID_1
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).getBookOfAuthorByBookId(ID_1, ID_1);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(bookDto));
        }

        @Test
        void shouldReturn404WhenGetBookOfAuthorWithInvalidAuthorId() throws Exception {

            when(authorService.getBookOfAuthorByBookId(INVALID_ID, ID_1))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            INVALID_ID,
                            ID_1
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).getBookOfAuthorByBookId(INVALID_ID, ID_1);
        }

        @Test
        void shouldReturn404WhenGetBookOfAuthorWithInvalidBookId() throws Exception {

            when(authorService.getBookOfAuthorByBookId(ID_1, INVALID_ID))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    get(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            INVALID_ID
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).getBookOfAuthorByBookId(ID_1, INVALID_ID);
        }
    }

    @Nested
    class PostBookOfAuthor {
        @Test
        void shouldReturnAddedBookWhenSaveValidBook() throws Exception {

            BookRequestDto validBookRequestDto = getValidBookRequestDto();
            BookDto bookDto = getBookDtoByBookRequestDto(validBookRequestDto);

            when(authorService.saveNewBookOfAuthorByAuthorId(ID_1, validBookRequestDto))
                    .thenReturn(bookDto);

            MvcResult mvcResult = mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    validBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(validBookRequestDto)))
                    .andExpect(status().isCreated()).andReturn();

            verify(authorService, times(1)).saveNewBookOfAuthorByAuthorId(ID_1, validBookRequestDto);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            Assertions.assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(bookDto));
        }

        @Test
        void shouldReturn404WhenSaveBookOfAuthorWithInvalidAuthorId() throws Exception {

            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorService.saveNewBookOfAuthorByAuthorId(INVALID_ID, validBookRequestDto))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    post(
                            API_V_1_AUTHORS_ID_BOOKS,
                            INVALID_ID,
                            validBookRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validBookRequestDto))
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).saveNewBookOfAuthorByAuthorId(INVALID_ID, validBookRequestDto);
        }
    }

    @Nested
    class ValidationPostBookOfAuthor {
        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithInvalidTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(INVALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is("size must be between 0 and 255")));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithNullTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(NULL_STRING, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithEmptyTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(EMPTY_STRING, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithInvalidGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, INVALID_LENGTH_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithNullGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, NULL_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithEmptyGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, EMPTY_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithYearOfPublicationInFuture() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, YEAR_OF_PUBLICATION_IN_FUTURE, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("yearOfPublication", Is.is(YEAR_VALIDATION_ERROR_MESSAGE)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithNullYearOfPublication() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, NULL_YEAR, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("yearOfPublication", Is.is(MUST_NOT_BE_NULL)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithTooMuchPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, 10000, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_BE_LESS_THAN_OR_EQUAL_TO_9999)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithZeroPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, 0, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_BE_GREATER_THAN_OR_EQUAL_TO_1)));
        }

        @Test
        void shouldReturn400WhenSaveNewBookOfAuthorWithNullNumberOfPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, null, VALID_ISBN);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_NOT_BE_NULL)));
        }

        @ParameterizedTest
        @DisplayName("Validation should return 400 when ISBN is invalid in some possible ways")
        @MethodSource({"by.iyunski.library.api.controllers.AuthorApiControllerTest#getInvalidArgumentsForIsbn"})
        void shouldReturn400WhenSaveNewBookOfAuthorWithInvalidIsbn(String isbn, String error) throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, isbn);

            mockMvc.perform(
                            post(
                                    API_V_1_AUTHORS_ID_BOOKS,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("isbn", Is.is(error)));
        }
    }

    @Nested
    class PutBookOfAuthor {
        @Test
        void shouldReturnAddedBookWhenUpdateValidBook() throws Exception {

            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            BookDto bookDto = getBookDtoByBookRequestDto(validBookRequestDto);

            when(authorService.updateAuthorsBook(ID_1, ID_1, validBookRequestDto)).thenReturn(bookDto);

            MvcResult mvcResult = mockMvc.perform(
                    put(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            ID_1,
                            validBookRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validBookRequestDto))
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).updateAuthorsBook(ID_1, ID_1, validBookRequestDto);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(bookDto));
        }

        @Test
        void shouldReturn404WhenUpdateBookOfAuthorWithInvalidAuthorId() throws Exception {

            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorService.updateAuthorsBook(INVALID_ID, ID_1, validBookRequestDto))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    put(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            INVALID_ID,
                            ID_1,
                            validBookRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validBookRequestDto))
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).updateAuthorsBook(INVALID_ID, ID_1, validBookRequestDto);
        }

        @Test
        void shouldReturn404WhenUpdateBookOfAuthorWithInvalidBookId() throws Exception {

            BookRequestDto validBookRequestDto = getValidBookRequestDto();

            when(authorService.updateAuthorsBook(ID_1, INVALID_ID, validBookRequestDto))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    put(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            INVALID_ID,
                            validBookRequestDto
                    ).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(validBookRequestDto))
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).updateAuthorsBook(ID_1, INVALID_ID, validBookRequestDto);
        }
    }

    @Nested
    class ValidationPutBookOfAuthor {
        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithInvalidTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(INVALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is("size must be between 0 and 255")));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithNullTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(NULL_STRING, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithEmptyTitle() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(EMPTY_STRING, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("title", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithInvalidGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, INVALID_LENGTH_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(SIZE_MUST_BE_BETWEEN_0_AND_50)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithNullGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, NULL_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithEmptyGenre() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, EMPTY_STRING, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("genre", Is.is(MUST_NOT_BE_EMPTY)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithYearOfPublicationInFuture() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, YEAR_OF_PUBLICATION_IN_FUTURE, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("yearOfPublication", Is.is(YEAR_VALIDATION_ERROR_MESSAGE)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithNullYearOfPublication() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, NULL_YEAR, VALID_NUMBER_OF_PAGES, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("yearOfPublication", Is.is(MUST_NOT_BE_NULL)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithTooMuchPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, 10000, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_BE_LESS_THAN_OR_EQUAL_TO_9999)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithZeroPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, 0, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_BE_GREATER_THAN_OR_EQUAL_TO_1)));
        }

        @Test
        void shouldReturn400WhenUpdateNewBookOfAuthorWithNullNumberOfPages() throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, null, VALID_ISBN);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("numberOfPages", Is.is(MUST_NOT_BE_NULL)));
        }

        @ParameterizedTest
        @DisplayName("Validation should return 400 when ISBN is invalid in some possible ways")
        @MethodSource({"by.iyunski.library.api.controllers.AuthorApiControllerTest#getInvalidArgumentsForIsbn"})
        void shouldReturn400WhenSaveNewBookOfAuthorWithInvalidIsbn(String isbn, String error) throws Exception {

            BookRequestDto invalidBookRequestDto = new BookRequestDto(VALID_TITLE, VALID_GENRE, VALID_YEAR_OF_PUBLICATION, VALID_NUMBER_OF_PAGES, isbn);

            mockMvc.perform(
                            put(
                                    API_V_1_AUTHORS_ID_BOOKS_ID,
                                    ID_1,
                                    ID_1,
                                    invalidBookRequestDto
                            ).contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(invalidBookRequestDto))
                    ).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("isbn", Is.is(error)));
        }
    }

    @Nested
    class DeleteBookOfAuthor {
        @Test
        void shouldReturn200WhenDeleteBookOfAuthor() throws Exception {

            when(authorService.deleteAuthorsBookById(ID_1, ID_2)).thenReturn(ID_2);

            MvcResult mvcResult = mockMvc.perform(
                    delete(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            ID_2
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            verify(authorService, times(1)).deleteAuthorsBookById(ID_1, ID_2);

            String contentAsString = mvcResult.getResponse().getContentAsString();

            Assertions.assertThat(contentAsString).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(ID_2));
        }

        @Test
        void shouldReturn404WhenDeleteBookOfAuthorWithInvalidAuthorId() throws Exception {

            when(authorService.deleteAuthorsBookById(INVALID_ID, ID_2))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    delete(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            INVALID_ID,
                            ID_2
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).deleteAuthorsBookById(INVALID_ID, ID_2);
        }

        @Test
        void shouldReturn404WhenDeleteBookOfAuthorWithInvalidBookId() throws Exception {

            when(authorService.deleteAuthorsBookById(ID_1, INVALID_ID))
                    .thenThrow(new ErrorResponseException(HttpStatus.NOT_FOUND));

            mockMvc.perform(
                    delete(
                            API_V_1_AUTHORS_ID_BOOKS_ID,
                            ID_1,
                            INVALID_ID
                    ).contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());

            verify(authorService, times(1)).deleteAuthorsBookById(ID_1, INVALID_ID);
        }
    }

    private static Stream<Arguments> getInvalidArgumentsForIsbn() {
        return Stream.of(
                Arguments.of(EMPTY_STRING, ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of(" ", ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of("111 22 33", ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of("11one2233", ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of("12345678901234567890123456", ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of(NULL_STRING, MUST_NOT_BE_NULL),
                Arguments.of("1234567890123", ISBN_VALIDATION_ERROR_MESSAGE),
                Arguments.of("isbn654832", ISBN_VALIDATION_ERROR_MESSAGE));
    }

}
