package mate.academy.dto.order;

import jakarta.validation.constraints.NotNull;
import mate.academy.model.Order;
import mate.academy.validation.ValidEnum;

public record UpdateOrderRequestDto(
        @NotNull(message = "Order status cannot be empty")
        @ValidEnum(enumClass = Order.Status.class)
        Order.Status status
) {
}
