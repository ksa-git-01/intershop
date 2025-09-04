package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.dto.CartView;
import ru.yandex.practicum.intershop.dto.ItemView;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public Mono<Void> modifyCartByItem(Long id, CartItemAction action) {
        return switch (action) {
            case PLUS -> addItemToCart(id);
            case MINUS -> removeOneItemFromCart(id);
            case DELETE -> deleteItemFromCart(id);
        };
    }

    private Mono<Void> deleteItemFromCart(Long id) {
        return cartRepository.findByItemId(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Cart with item not found")))
                .flatMap(cartRepository::delete);
    }

    private Mono<Void> removeOneItemFromCart(Long id) {
        return cartRepository.findByItemId(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Cart with item not found")))
                .flatMap(cart -> {
                    cart.setCount(cart.getCount() - 1);
                    return cartRepository.save(cart);
                })
                .then();
    }

    private Mono<Void> addItemToCart(Long id) {
        return cartRepository.findByItemId(id)
                .flatMap(this::incrementCartItem)
                .switchIfEmpty(createNewCartItem(id))
                .then();
    }

    private Mono<Cart> incrementCartItem(Cart cart) {
        cart.setCount(cart.getCount() + 1);
        return cartRepository.save(cart);
    }

    private Mono<Cart> createNewCartItem(Long itemId) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Item not found")))
                .flatMap(item -> {
                    Cart cart = new Cart();
                    cart.setItemId(item.getId());
                    cart.setCount(1);
                    return cartRepository.save(cart);
                });
    }

    public Mono<CartView> getCart() {
        return cartRepository.findAll()
                .collectList()
                .flatMap(this::buildCartView);
    }

    private Mono<CartView> buildCartView(List<Cart> cartList) {
        if (cartList.isEmpty()) {
            return Mono.just(createEmptyCartView());
        }

        return loadItemsForCart(cartList)
                .map(items -> createCartViewWithItems(cartList, items));
    }

    private CartView createEmptyCartView() {
        return CartView.builder()
                .items(new ArrayList<>())
                .total(0D)
                .empty(true)
                .build();
    }

    private Mono<List<Item>> loadItemsForCart(List<Cart> cartList) {
        List<Long> itemIds = cartList.stream()
                .map(Cart::getItemId)
                .toList();

        return itemRepository.findAllById(itemIds).collectList();
    }

    private CartView createCartViewWithItems(List<Cart> cartList, List<Item> items) {
        List<ItemView> itemViews = cartList.stream()
                .map(cart -> createItemView(cart, items))
                .sorted(Comparator.comparing(ItemView::getTitle))
                .toList();

        return CartView.builder()
                .items(itemViews)
                .total(calculateTotal(itemViews))
                .empty(false)
                .build();
    }

    private ItemView createItemView(Cart cart, List<Item> items) {
        Item item = findItemById(cart.getItemId(), items);
        ItemView itemView = itemMapper.itemToItemDto(item);
        itemView.setCount(cart.getCount());
        itemView.setPrice(roundToTwoDecimals(cart.getCount() * item.getPrice()));
        return itemView;
    }

    private Item findItemById(Long itemId, List<Item> items) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
    }

    private Double calculateTotal(List<ItemView> items) {
        return roundToTwoDecimals(
                items.stream()
                        .mapToDouble(ItemView::getPrice)
                        .sum()
        );
    }

    private Double roundToTwoDecimals(Double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}