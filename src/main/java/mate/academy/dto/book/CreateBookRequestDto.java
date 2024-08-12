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
    private static final int MIN_DESCRIPTION_LENGTH = 25;
    private static final int MAX_DESCRIPTION_LENGTH = 1024;

    @NotBlank
    private String title;
    @NotBlank
    private String author;
    @Isbn
    private String isbn;
    @NotNull
    @Positive
    private BigDecimal price;
    @Size(min = MIN_DESCRIPTION_LENGTH, max = MAX_DESCRIPTION_LENGTH)
    private String description;
    private String coverImage;
    private Set<Long> categoryIds;
}
