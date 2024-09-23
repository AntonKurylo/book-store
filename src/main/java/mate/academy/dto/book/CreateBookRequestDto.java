package mate.academy.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import mate.academy.validation.Isbn;

@Getter
@Setter
public class CreateBookRequestDto {
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 128, message = "Title must be less than 128 characters")
    private String title;
    @NotBlank(message = "Author cannot be empty")
    @Size(max = 64, message = "Author must be less than 64 characters")
    private String author;
    @Isbn
    private String isbn;
    @NotNull(message = "Price cannot be empty")
    @Positive(message = "Price must be at least 1")
    private BigDecimal price;
    @Size(max = 1024, message = "Description must be less than 1024 characters")
    private String description;
    @Size(max = 256, message = "Cover image URL must be less than 256 characters")
    private String coverImage;
    private Set<Long> categoryIds;
}
