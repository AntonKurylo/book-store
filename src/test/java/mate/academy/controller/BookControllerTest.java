package mate.academy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.BookSearchParametersDto;
import mate.academy.dto.book.CreateBookRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext,
            @Autowired DataSource dataSource
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("/database/books/add-three-default-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("/database/categories/add-three-default-categories.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("/database/categories/remove-all-categories.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("/database/books/remove-all-books.sql")
            );
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new book")
    @Sql(
            scripts = {
                    "classpath:/database/books_categories/remove-all-books-categories.sql",
                    "classpath:/database/books/remove-all-books.sql",
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(
            scripts = {
                    "classpath:/database/books_categories/remove-all-books-categories.sql",
                    "classpath:/database/books/remove-all-books.sql",
                    "classpath:/database/books/add-three-default-books.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createBook_ValidRequest_Success() throws Exception {
        // Given
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Title")
                .setAuthor("Author")
                .setIsbn("1234567890")
                .setPrice(BigDecimal.valueOf(9.99))
                .setDescription("Description")
                .setCoverImage("CoverImage")
                .setCategoryIds(Set.of(1L));
        BookDto expected = new BookDto()
                .setTitle(requestDto.getTitle())
                .setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setCategoryIds(requestDto.getCategoryIds());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get all books")
    public void findAllBooks_GivenBooksInCatalog_ReturnAllBooks() throws Exception {
        // Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setId(1L).setTitle("History")
                .setAuthor("Arthur").setIsbn("9781122334455")
                .setPrice(BigDecimal.valueOf(29.99)).setCategoryIds(Set.of()));
        expected.add(new BookDto().setId(2L).setTitle("Ecology")
                .setAuthor("James").setIsbn("9589876543210")
                .setPrice(BigDecimal.valueOf(24.99)).setCategoryIds(Set.of()));
        expected.add(new BookDto().setId(3L).setTitle("Biology")
                .setAuthor("Archi").setIsbn("9687874523471")
                .setPrice(BigDecimal.valueOf(35.99)).setCategoryIds(Set.of()));

        // When
        MvcResult result = mockMvc.perform(
                        get("/books")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        BookDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), new TypeReference<BookDto[]>() {
                });

        assertThat(actual).hasSize(3);
        assertThat(List.of(actual)).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get a book by id")
    public void findBookById_ValidBookId_ReturnBook() throws Exception {
        // Given
        long id = 1L;
        BookDto expected = new BookDto()
                .setId(id).setTitle("History").setAuthor("Arthur").setIsbn("9781122334455")
                .setPrice(BigDecimal.valueOf(29.99)).setCategoryIds(Set.of());

        // When
        MvcResult result = mockMvc.perform(
                        get("/books/" + id)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get a book by nonexistent id")
    public void findBookById_NonexistentBookId_ShouldThrowException() throws Exception {
        // Given
        long nonexistentId = 100L;
        String expected = "Cannot find a book by id: " + nonexistentId;

        // When
        MvcResult result = mockMvc.perform(
                        get("/books/" + nonexistentId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(
                responseContent, new TypeReference<Map<String, Object>>() {
                });
        List<String> errors = (List<String>) responseMap.get("errors");
        String actual = errors.get(0);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update a book by id")
    @Sql(scripts = "classpath:/database/books_categories/remove-all-books-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateBookById_ValidRequest_ReturnUpdatedBook() throws Exception {
        // Given
        long id = 1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("History").setAuthor("Arthur").setIsbn("9781122334455")
                .setPrice(BigDecimal.valueOf(29.99)).setCategoryIds(Set.of(1L));
        BookDto expected = new BookDto()
                .setId(id).setTitle(requestDto.getTitle()).setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn()).setPrice(requestDto.getPrice())
                .setCategoryIds(requestDto.getCategoryIds());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/books/" + id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"ADMIN"})
    @Test
    @DisplayName("Update a book by non-existing id")
    public void updateBookById_NonexistentBookId_ShouldThrowException() throws Exception {
        // Given
        long nonExistentId = 100L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Ecology").setAuthor("James").setIsbn("9589876543210")
                .setPrice(BigDecimal.valueOf(77.77)).setCategoryIds(Set.of(1L));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "Cannot find a book by id: " + nonExistentId;

        // When
        MvcResult result = mockMvc.perform(
                        put("/books/" + nonExistentId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(
                responseContent, new TypeReference<Map<String, Object>>() {
                });
        List<String> errors = (List<String>) responseMap.get("errors");
        String actual = errors.get(0);
        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete a book by id")
    @Sql(
            scripts = {
                    "classpath:/database/books/remove-all-books.sql",
                    "classpath:/database/books/add-three-default-books.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteBookById_ValidBookId_ReturnNoContent() throws Exception {
        // Given
        long id = 1L;

        // When + Then
        mockMvc.perform(
                delete("/books/" + id)
        ).andExpect(status().isNoContent());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Search books by search parameters")
    public void searchBooks_BookSearchParametersProvided_ShouldReturnBooks() throws Exception {
        // Given
        BookSearchParametersDto requestDto = new BookSearchParametersDto("ec", "ar");

        // When
        MvcResult result = mockMvc.perform(
                        get("/books/search")
                                .param("title", requestDto.title())
                                .param("author", requestDto.author())
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        BookDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), new TypeReference<BookDto[]>() {
                });

        assertThat(actual).hasSize(3);
    }
}
