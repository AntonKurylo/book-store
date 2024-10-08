package mate.academy.service;

import mate.academy.dto.shoppingcart.CreateCartItemRequestDto;
import mate.academy.dto.shoppingcart.ShoppingCartDto;
import mate.academy.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.model.User;

public interface ShoppingCartService {
    void createShoppingCart(User user);

    ShoppingCartDto findByUser();

    ShoppingCartDto addCartItem(CreateCartItemRequestDto requestDto);

    ShoppingCartDto updateCartItemById(Long id, UpdateCartItemRequestDto requestDto);

    void deleteCartItemById(Long cartItemId);
}
