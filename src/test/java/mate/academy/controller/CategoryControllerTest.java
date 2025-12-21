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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CreateCategoryRequestDto;
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
public class CategoryControllerTest {
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
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new category")
    @Sql(scripts = "classpath:/database/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(
            scripts = {
                    "classpath:/database/categories/remove-all-categories.sql",
                    "classpath:/database/categories/add-three-default-categories.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createCategory_ValidRequest_Success() throws Exception {
        // Given
        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("Test Name", "Test Description");
        CategoryDto expected = new CategoryDto(1L, "Test Name", "Test Description");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get all categories")
    public void findAllCategories_GivenCategoriesInCatalog_ReturnAllCategories() throws Exception {
        // Given
        List<CategoryDto> expected = new ArrayList<>();
        expected.add(new CategoryDto(1L, "Fantasy", "Fantasy books category"));
        expected.add(new CategoryDto(2L, "History", "Historical books category"));
        expected.add(new CategoryDto(3L, "Detective", "Detective books category"));

        // When
        MvcResult result = mockMvc.perform(
                        get("/categories")
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        CategoryDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CategoryDto[].class);

        assertThat(actual).hasSize(3);
        assertThat(List.of(actual)).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get a category by id")
    public void findCategoryById_ValidCategoryId_ReturnCategory() throws Exception {
        // Given
        long id = 1L;
        CategoryDto expected = new CategoryDto(id, "Fantasy", "Fantasy books category");

        // When
        MvcResult result = mockMvc.perform(
                        get("/categories/" + id)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get a category by nonexistent id")
    public void findCategoryById_NonexistentCategoryId_ReturnCategory() throws Exception {
        // Given
        long nonexistentId = 100L;
        String expected = "Cannot find a category by id: " + nonexistentId;

        // When
        MvcResult result = mockMvc.perform(
                        get("/categories/" + nonexistentId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(
                responseContent, new TypeReference<Map<String, Object>>() {});
        List<String> errors = (List<String>) responseMap.get("errors");
        String actual = errors.get(0);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update a category by id")
    public void updateCategoryById_ValidRequest_UpdateCategory() throws Exception {
        // Given
        long id = 1L;
        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("Fantasy", "Fantasy books category");
        CategoryDto expected = new CategoryDto(id, requestDto.name(), requestDto.description());
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/categories/" + id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update a category by nonexistent id")
    public void updateCategoryById_NonexistentCategoryId_ShouldThrowException() throws Exception {
        // Given
        long nonexistentId = 100L;
        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("History", "Historical books category");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "Cannot find a category by id: " + nonexistentId;

        // When
        MvcResult result = mockMvc.perform(
                        put("/categories/" + nonexistentId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(
                responseContent, new TypeReference<Map<String, Object>>() {});
        List<String> errors = (List<String>) responseMap.get("errors");
        String actual = errors.get(0);

        assertThat(actual).isEqualTo(expected);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete a category by id")
    @Sql(
            scripts = {
                    "classpath:/database/categories/remove-all-categories.sql",
                    "classpath:/database/categories/add-three-default-categories.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteCategoryById_ValidRequest_DeleteCategory() throws Exception {
        // Given
        long id = 1L;

        // When + Then
        mockMvc.perform(
                delete("/categories/" + id)
        ).andExpect(status().isNoContent());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Get all books by category id")
    @Sql(
            scripts = {
                    "classpath:/database/books/add-three-default-books.sql",
                    "classpath:/database/books_categories/add-three-default-books-categories.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(
            scripts = {
                    "classpath:/database/books_categories/remove-all-books-categories.sql",
                    "classpath:/database/books/remove-all-books.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllBooksByCategoryId_GivenBooksInCatalog_ReturnTwoBooks() throws Exception {
        // Given
        long id = 1L;

        // When
        MvcResult result = mockMvc.perform(
                        get(String.format("/categories/%d/books", id))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        CategoryDto[] expected = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto[].class);

        assertThat(expected).hasSize(2);
    }
}
