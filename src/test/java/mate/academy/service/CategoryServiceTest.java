package mate.academy.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CreateCategoryRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CategoryMapper;
import mate.academy.model.Category;
import mate.academy.repository.category.CategoryRepository;
import mate.academy.service.impl.CategoryServiceImpl;
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

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("Verify save() method")
    public void save_ValidCreateCategoryRequestDto_ReturnSavedCategory() {
        // Given
        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("Test Name", "Test Description");
        Category category = new Category();
        category.setName(requestDto.name());
        category.setDescription(requestDto.description());

        CategoryDto categoryDto = new CategoryDto(1L, requestDto.name(), requestDto.description());

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto savedCategoryDto = categoryService.save(requestDto);

        // Then
        Assertions.assertThat(savedCategoryDto).isEqualTo(categoryDto);

        verify(categoryMapper, Mockito.times(1)).toEntity(requestDto);
        verify(categoryRepository, Mockito.times(1)).save(category);
        verify(categoryMapper, Mockito.times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify findAll() method works")
    public void findAll_ValidPageable_ReturnAllCategories() {
        // Given
        Category category = new Category();
        category.setName("Test Name");
        category.setDescription("Test Description");

        CategoryDto categoryDto =
                new CategoryDto(1L, category.getName(), category.getDescription());

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        List<CategoryDto> categoryDtos = categoryService.findAll(pageable);

        // Then
        Assertions.assertThat(categoryDtos).hasSize(1);
        Assertions.assertThat(categoryDtos.get(0)).isEqualTo(categoryDto);

        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify findById() method works")
    public void findById_ValidCategoryId_ReturnCategory() {
        // Given
        long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Test Name");
        category.setDescription("Test Description");

        CategoryDto categoryDto =
                new CategoryDto(category.getId(), category.getName(), category.getDescription());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto categoryFromDbDto = categoryService.findById(id);

        // Then
        Assertions.assertThat(categoryFromDbDto).isEqualTo(categoryDto);

        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify findById() method works with nonexistent categoryId")
    public void findById_NonexistentCategoryId_ShouldThrowException() {
        // Given
        long nonExistentId = 100L;
        String expected = "Cannot find a category by id: " + nonExistentId;

        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When + Then
        Assertions.assertThatThrownBy(() -> categoryService.findById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(expected);

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify updateById() method works")
    public void updateById_ValidCategoryId_ReturnUpdatedCategory() {
        // Given
        long id = 1L;

        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("Test Name", "Test Description");
        Category category = new Category();
        category.setName(requestDto.name());
        category.setDescription(requestDto.description());

        CategoryDto categoryDto = new CategoryDto(id, requestDto.name(), requestDto.description());

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto updatedCategoryDto = categoryService.updateById(id, requestDto);

        // Then
        Assertions.assertThat(updatedCategoryDto).isEqualTo(categoryDto);

        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify updateById() method works with nonexistent categoryId")
    public void updateById_NonexistentCategoryId_ShouldThrowException() {
        // Given
        long nonExistentId = 100L;
        CreateCategoryRequestDto requestDto =
                new CreateCategoryRequestDto("Test Name", "Test Description");
        String expected = "Cannot find a category by id: " + nonExistentId;

        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When + Then
        Assertions.assertThatThrownBy(() -> categoryService.updateById(nonExistentId, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(expected);

        verifyNoMoreInteractions(categoryRepository);
    }
}
