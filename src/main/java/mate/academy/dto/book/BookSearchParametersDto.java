package mate.academy.dto.book;

import jakarta.validation.constraints.Size;

public record BookSearchParametersDto(
        @Size(max = 128, message = "Title must be less than 128 characters")
        String title,
        @Size(max = 64, message = "Author must be less than 64 characters")
        String author) {
}
