package mate.academy.dto.shoppingcart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCartItemRequestDto(
        @NotNull(message = "Book ID cannot be empty")
        @Positive(message = "Book ID must be positive")
        Long bookId,
        @Positive(message = "Quantity must be at least 1")
        int quantity
) {
}
