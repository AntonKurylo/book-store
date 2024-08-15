package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.shoppingcart.CartItemDto;
import mate.academy.dto.shoppingcart.CreateCartItemRequestDto;
import mate.academy.dto.shoppingcart.ShoppingCartDto;
import mate.academy.model.CartItem;
import mate.academy.model.ShoppingCart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = BookMapper.class)
public interface ShoppingCartMapper {
    @Mapping(source = "user.id", target = "userId")
    ShoppingCartDto toShoppingCartDto(ShoppingCart shoppingCart);

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    CartItemDto toCartItemDto(CartItem cartItem);

    @Mapping(source = "bookId", target = "book", qualifiedByName = "bookById")
    CartItem toCartItemEntity(CreateCartItemRequestDto requestDto,
                              @Context ShoppingCart shoppingCart);

    @AfterMapping
    default void setShoppingCart(@MappingTarget CartItem cartItem,
                                 @Context ShoppingCart shoppingCart) {
        cartItem.setShoppingCart(shoppingCart);
    }
}
