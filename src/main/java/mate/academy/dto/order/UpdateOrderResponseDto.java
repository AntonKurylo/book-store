package mate.academy.dto.order;

import mate.academy.model.Order;

public record UpdateOrderResponseDto(
        Long id,
        Order.Status status
) {
}
