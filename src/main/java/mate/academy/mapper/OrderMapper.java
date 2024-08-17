package mate.academy.mapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.config.MapperConfig;
import mate.academy.dto.order.OrderDto;
import mate.academy.dto.order.OrderItemDto;
import mate.academy.dto.order.UpdateOrderResponseDto;
import mate.academy.model.CartItem;
import mate.academy.model.Order;
import mate.academy.model.OrderItem;
import mate.academy.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    OrderDto toDto(Order order);

    UpdateOrderResponseDto toUpdateDto(Order order);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "shoppingCart.cartItems", target = "total", qualifiedByName = "total")
    })
    Order shoppingCartToOrder(ShoppingCart shoppingCart, String shippingAddress);

    @Mapping(source = "book.id", target = "bookId")
    OrderItemDto toOrderItemDto(OrderItem orderItem);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "book.price", target = "price")
    })
    OrderItem cartItemToOrderItem(CartItem cartItem);

    @Named("total")
    default BigDecimal getTotal(Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default void setOrderItemsFromShoppingCart(Order order, ShoppingCart shoppingCart) {
        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(this::cartItemToOrderItem)
                .collect(Collectors.toSet());
        order.setOrderItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order));
    }
}
