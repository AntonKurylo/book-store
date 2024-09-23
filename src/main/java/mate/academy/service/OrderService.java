package mate.academy.service;

import java.util.List;
import mate.academy.dto.order.CreateOrderRequestDto;
import mate.academy.dto.order.OrderDto;
import mate.academy.dto.order.OrderItemDto;
import mate.academy.dto.order.UpdateOrderRequestDto;
import mate.academy.dto.order.UpdateOrderResponseDto;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto save(CreateOrderRequestDto requestDto);

    List<OrderDto> findAllByUser(Pageable pageable);

    UpdateOrderResponseDto updateById(Long id, UpdateOrderRequestDto requestDto);

    List<OrderItemDto> findAllOrderItemsByOrderId(Long orderId, Pageable pageable);

    OrderItemDto findOrderItemByIdAndOrderId(Long orderId, Long orderItemId);
}
