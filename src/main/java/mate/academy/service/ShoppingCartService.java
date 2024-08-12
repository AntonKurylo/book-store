package mate.academy.service;

import mate.academy.dto.cartitem.CreateCartItemRequestDto;
import mate.academy.dto.cartitem.UpdateCartItemRequestDto;
import mate.academy.dto.shoppingcart.ShoppingCartDto;
import mate.academy.model.User;

public interface ShoppingCartService {
    void createShoppingCart(User user);

    ShoppingCartDto findByUser();

    ShoppingCartDto addCartItem(CreateCartItemRequestDto requestDto);

    ShoppingCartDto updateCartItem(Long id, UpdateCartItemRequestDto requestDto);

    void deleteCartItem(Long cartItemId);
}
