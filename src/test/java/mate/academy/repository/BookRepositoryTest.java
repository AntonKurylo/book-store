package mate.academy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.book.BookSearchParametersDto;
import mate.academy.model.Book;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.book.spec.AuthorSpecificationProvider;
import mate.academy.repository.book.spec.BookSpecificationBuilder;
import mate.academy.repository.book.spec.BookSpecificationManager;
import mate.academy.repository.book.spec.TitleSpecificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@Import({
        BookSpecificationBuilder.class,
        BookSpecificationManager.class,
        AuthorSpecificationProvider.class,
        TitleSpecificationProvider.class
})
public class BookRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource
    ) throws SQLException {
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("/database/books/add-three-default-books.sql")
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
                    new ClassPathResource("/database/books/remove-all-books.sql")
            );
        }
    }

    @Test
    @DisplayName("""
            Find all books by author name containing a character set
            """
    )
    public void findAllBooksByAuthorName_CharacterSetProvided_ReturnsTwoBooks() {
        // Given
        BookSearchParametersDto searchParameters = new BookSearchParametersDto("", "ar");
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(searchParameters);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> actual = bookRepository.findAll(bookSpecification, pageable);

        // Then
        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("""
            Find all books by title containing a character set
            """
    )
    public void findAllBooksByTitle_CharacterSetProvided_ReturnsOneBook() {
        // Given
        BookSearchParametersDto searchParameters = new BookSearchParametersDto("ec", "");
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(searchParameters);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> actual = bookRepository.findAll(bookSpecification, pageable);

        // Then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("""
            Find all books by author name or title containing a character set
            """
    )
    public void findAllBooksByAuthorAndTitle_CharacterSetsProvided_ReturnsThreeBooks() {
        // Given
        BookSearchParametersDto searchParameters = new BookSearchParametersDto("ec", "ar");
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(searchParameters);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> actual = bookRepository.findAll(bookSpecification, pageable);

        // Then
        assertThat(actual).hasSize(3);
    }

    @Test
    @DisplayName("Find all books by category ID")
    @Sql(
            scripts = {
                    "classpath:/database/categories/add-three-default-categories.sql",
                    "classpath:/database/books_categories/add-three-default-books-categories.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(
            scripts = {
                    "classpath:/database/books_categories/remove-all-books-categories.sql",
                    "classpath:/database/categories/remove-all-categories.sql"
            },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllBooksByCategoryId_ValidCategoryId_ReturnsTwoBooks() {
        // Given
        long categoryId = 1L;

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> actual = bookRepository.findAllByCategoriesId(categoryId, pageable);

        // Then
        assertThat(actual).hasSize(2);
    }
}
