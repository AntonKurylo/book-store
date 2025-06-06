package mate.academy.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.dto.book.BookSearchParametersDto;
import mate.academy.dto.book.CreateBookRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.BookMapper;
import mate.academy.model.Book;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.book.spec.BookSpecificationBuilder;
import mate.academy.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @InjectMocks
    private BookServiceImpl bookService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Test
    @DisplayName("Verify save() method works")
    public void save_ValidCreateBookRequestDto_ReturnSavedBook() {
        // Given
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Test Title");
        requestDto.setAuthor("Test Author");
        requestDto.setIsbn("9781122334455");
        requestDto.setPrice(BigDecimal.valueOf(100));

        Book book = new Book();
        book.setTitle(requestDto.getTitle());
        book.setAuthor(requestDto.getAuthor());
        book.setIsbn(requestDto.getIsbn());
        book.setPrice(requestDto.getPrice());

        BookDto bookDto = new BookDto();
        bookDto.setId(1L);
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());

        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        BookDto savedBookDto = bookService.save(requestDto);

        // Then
        Assertions.assertThat(savedBookDto).isEqualTo(bookDto);

        verify(bookMapper, Mockito.times(1)).toEntity(requestDto);
        verify(bookRepository, Mockito.times(1)).save(book);
        verify(bookMapper, Mockito.times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAll() method works")
    public void findAll_ValidPageable_ReturnAllBooks() {
        // Given
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setIsbn("9781122334455");
        book.setPrice(BigDecimal.valueOf(100));

        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        List<BookDto> bookDtos = bookService.findAll(pageable);

        // Then
        Assertions.assertThat(bookDtos).hasSize(1);
        Assertions.assertThat(bookDtos.get(0)).isEqualTo(bookDto);

        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findById() method works")
    public void findById_ValidBookId_ReturnBook() {
        // Given
        long id = 1L;
        Book book = new Book();
        book.setId(id);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setIsbn("9781122334455");
        book.setPrice(BigDecimal.valueOf(100));

        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        BookDto bookFromDbDto = bookService.findById(id);

        // Then
        Assertions.assertThat(bookFromDbDto).isEqualTo(bookDto);

        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findById() method works with nonexistent bookId")
    public void findById_NonexistentBookId_ShouldThrowException() {
        // Given
        long nonExistentId = 100L;
        String expected = "Cannot find a book by id: " + nonExistentId;

        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When + Then
        Assertions.assertThatThrownBy(() -> bookService.findById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(expected);

        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify updateById() method works")
    public void updateById_ValidBookId_ReturnUpdatedBook() {
        // Given
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Test Title");
        requestDto.setAuthor("Test Author");
        requestDto.setIsbn("9781122334455");
        requestDto.setPrice(BigDecimal.valueOf(100));

        Book book = new Book();
        book.setTitle(requestDto.getTitle());
        book.setAuthor(requestDto.getAuthor());
        book.setIsbn(requestDto.getIsbn());
        book.setPrice(requestDto.getPrice());

        long id = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        BookDto updatedBookDto = bookService.updateById(id, requestDto);

        // Then
        Assertions.assertThat(updatedBookDto).isEqualTo(bookDto);

        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findById() method works with nonexistent bookId")
    public void updateById_NonexistentBookId_ShouldThrowException() {
        // Given
        long nonExistentId = 100L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Test Title");
        requestDto.setAuthor("Test Author");
        requestDto.setIsbn("9781122334455");
        requestDto.setPrice(BigDecimal.valueOf(100));
        String expected = "Cannot find a book by id: " + nonExistentId;

        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When + Then
        Assertions.assertThatThrownBy(() -> bookService.updateById(nonExistentId, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(expected);

        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify search() method works")
    public void search_ValidParametersProvided_ReturnOneBook() {
        // Given
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setIsbn("9781122334455");
        book.setPrice(BigDecimal.valueOf(100));

        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());

        BookSearchParametersDto requestDto = new BookSearchParametersDto("Title", "Author");
        Specification<Book> bookSpecification = Specification.where(null);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookSpecificationBuilder.build(requestDto)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        List<BookDto> bookDtos = bookService.search(requestDto, pageable);

        // Then
        Assertions.assertThat(bookDtos).hasSize(1);
        Assertions.assertThat(bookDtos.get(0)).isEqualTo(bookDto);

        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAllByCategoryId() method works")
    public void findAllByCategoryId_ValidCategoryProvided_ReturnOneBook() {
        // Given
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setIsbn("9781122334455");
        book.setPrice(BigDecimal.valueOf(100));
        book.setDescription("Test Description");
        book.setCoverImage("Test CoverImage");

        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                book.getCoverImage()
        );

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        long categoryId = 1L;

        when(bookRepository.findAllByCategoriesId(categoryId, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(bookDto);

        // When
        List<BookDtoWithoutCategoryIds> bookDtos =
                bookService.findAllByCategoryId(categoryId, pageable);

        // Then
        Assertions.assertThat(bookDtos).hasSize(1);
        Assertions.assertThat(bookDtos.get(0)).isEqualTo(bookDto);

        verifyNoMoreInteractions(bookRepository, bookMapper);
    }
}
