package mate.academy.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(
        @NotBlank(message = "Shipping address cannot be empty")
        @Size(max = 256, message = "Shipping address must be less than 256 characters")
        String shippingAddress
) {
}
