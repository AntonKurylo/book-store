package mate.academy.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.order.CreateOrderRequestDto;
import mate.academy.dto.order.OrderDto;
import mate.academy.dto.order.OrderItemDto;
import mate.academy.dto.order.UpdateOrderRequestDto;
import mate.academy.dto.order.UpdateOrderResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.OrderMapper;
import mate.academy.model.Order;
import mate.academy.model.OrderItem;
import mate.academy.model.ShoppingCart;
import mate.academy.repository.order.OrderItemRepository;
import mate.academy.repository.order.OrderRepository;
import mate.academy.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.security.AuthenticationService;
import mate.academy.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final AuthenticationService authService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    @Transactional
    public OrderDto save(CreateOrderRequestDto requestDto) {
        Long authUserId = authService.getAuthenticatedUserId();
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(authUserId);
        if (shoppingCart.getCartItems().isEmpty()) {
            throw new EntityNotFoundException(
                    "There are no items in the shopping cart for user id: " + authUserId);
        }
        Order order = orderMapper.shoppingCartToOrder(shoppingCart, requestDto.shippingAddress());
        orderMapper.setOrderItemsFromShoppingCart(order, shoppingCart);
        shoppingCart.clear();
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public List<OrderDto> findAllByUser(Pageable pageable) {
        List<Order> orders = orderRepository
                .findAllByUserId(authService.getAuthenticatedUserId(), pageable)
                .getContent();
        return orderMapper.toOrderDtos(orders);
    }

    @Override
    public UpdateOrderResponseDto updateById(Long id, UpdateOrderRequestDto requestDto) {
        Order updatedOrder = orderRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Cannot find an order by id: " + id));
        updatedOrder.setStatus(requestDto.status());
        return orderMapper.toUpdateDto(orderRepository.save(updatedOrder));
    }

    @Override
    public List<OrderItemDto> findAllOrderItemsByOrderId(Long orderId, Pageable pageable) {
        Long authUserId = authService.getAuthenticatedUserId();
        List<OrderItem> orderItems = orderItemRepository
                .findAllByOrderIdAndUserId(orderId, authUserId, pageable)
                .getContent();
        return orderMapper.toOrderItemDtos(orderItems);
    }

    @Override
    public OrderItemDto findOrderItemByIdAndOrderId(Long orderId, Long orderItemId) {
        Long authUserId = authService.getAuthenticatedUserId();
        OrderItem orderItem = orderItemRepository
                .findByIdAndOrderIdAndUserId(orderItemId, orderId, authUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find an order item by id: " + orderItemId
                                + " for order id: " + orderId + " and user id: " + authUserId));
        return orderMapper.toOrderItemDto(orderItem);
    }
}
