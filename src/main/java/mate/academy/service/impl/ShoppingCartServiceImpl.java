package mate.academy.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.shoppingcart.CreateCartItemRequestDto;
import mate.academy.dto.shoppingcart.ShoppingCartDto;
import mate.academy.dto.shoppingcart.UpdateCartItemRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.ShoppingCartMapper;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.shoppingcart.CartItemRepository;
import mate.academy.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.security.AuthenticationService;
import mate.academy.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final AuthenticationService authService;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    public void createShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public ShoppingCartDto findByUser() {
        return shoppingCartMapper.toShoppingCartDto(
                shoppingCartRepository.findByUserId(
                        authService.getAuthenticatedUserId()));
    }

    @Override
    public ShoppingCartDto addCartItem(CreateCartItemRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.bookId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find a book by id: " + requestDto.bookId()));
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(
                authService.getAuthenticatedUserId());
        CartItem cartItem = shoppingCart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(requestDto.bookId()))
                .findFirst()
                .map(item -> {
                    item.setQuantity(item.getQuantity() + requestDto.quantity());
                    return item;
                }).orElseGet(() -> {
                    CartItem entity = shoppingCartMapper.toCartItemEntity(requestDto, shoppingCart);
                    entity.setBook(book);
                    return entity;
                });
        shoppingCart.getCartItems().add(cartItem);
        shoppingCartRepository.save(shoppingCart);
        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto updateCartItemById(
            Long cartItemId, UpdateCartItemRequestDto requestDto) {
        Long authUserId = authService.getAuthenticatedUserId();
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(authUserId);
        CartItem cartItem = shoppingCart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find a cartItem by id: "
                                + cartItemId + " for user id: " + authUserId));
        cartItem.setQuantity(requestDto.quantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public void deleteCartItemById(Long cartItemId) {
        Long authUserId = authService.getAuthenticatedUserId();
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(authUserId);
        shoppingCart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find a cartItem by id: "
                                + cartItemId + " for user id: " + authUserId));
        cartItemRepository.deleteById(cartItemId);
    }
}
