package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.order.CreateOrderRequestDto;
import mate.academy.dto.order.OrderDto;
import mate.academy.dto.order.OrderItemDto;
import mate.academy.dto.order.UpdateOrderRequestDto;
import mate.academy.dto.order.UpdateOrderResponseDto;
import mate.academy.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public OrderDto createOrder(@RequestBody @Valid CreateOrderRequestDto requestDto) {
        return orderService.save(requestDto);
    }

    @Operation(summary = "Get all orders by user",
            description = "Returns a list of all user orders")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public List<OrderDto> findAllOrdersByUser(Pageable pageable) {
        return orderService.findAllByUser(pageable);
    }

    @Operation(summary = "Update an order by id", description = "Updates order status by id")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("{orderId}")
    public UpdateOrderResponseDto updateOrderById(
            @PathVariable Long orderId, @RequestBody @Valid UpdateOrderRequestDto requestDto) {
        return orderService.updateById(orderId, requestDto);
    }

    @Operation(summary = "Get all order items by order id",
            description = "Returns a list of all order items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("{orderId}/items")
    public List<OrderItemDto> findAllOrderItemsByOrderId(
            @PathVariable Long orderId, Pageable pageable) {
        return orderService.findAllOrderItemsByOrderId(orderId, pageable);
    }

    @Operation(summary = "Get a specific order item within an order",
            description = "Returns an order item by order id and order item id")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("{orderId}/items/{orderItemId}")
    public OrderItemDto findSpecificOrderItemWithinOrder(
            @PathVariable Long orderId, @PathVariable Long orderItemId) {
        return orderService.findOrderItemByIdAndOrderId(orderId, orderItemId);
    }
}
