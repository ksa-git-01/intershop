package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.intershop.dto.CartView;
import ru.yandex.practicum.intershop.dto.CartItemAction;
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

    public void modifyCartByItem(Long id, CartItemAction action) {
        switch (action) {
            case PLUS -> addItemToCart(id);
            case MINUS -> removeOneItemFromCart(id);
            case DELETE -> deleteItemFromCart(id);
        }
    }

    private void deleteItemFromCart(Long id) {
        cartRepository.findByItemId(id)
                .ifPresentOrElse(cartRepository::delete,
                        () -> {
                            throw new EntityNotFoundException("Cart with item not found");
                        }
                );
    }

    private void removeOneItemFromCart(Long id) {
        cartRepository.findByItemId(id)
                .ifPresentOrElse(cart -> {
                            cart.setCount(cart.getCount() - 1);
                            cartRepository.save(cart);
                        },
                        () -> {
                            throw new EntityNotFoundException("Cart with item not found");
                        }
                );
    }

    private void addItemToCart(Long id) {
        cartRepository.findByItemId(id)
                .ifPresentOrElse(cart -> {
                            cart.setCount(cart.getCount() + 1);
                            cartRepository.save(cart);
                        },
                        () -> {
                            Item item = itemRepository.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));
                            Cart cart = new Cart();
                            cart.setItem(item);
                            cart.setCount(1);
                            cartRepository.save(cart);
                        }

                );
    }

    public CartView getCart() {
        List<Cart> cart = cartRepository.findAll();
        if (cart.isEmpty()) {
            return CartView.builder()
                    .items(new ArrayList<>())
                    .total(0D)
                    .empty(true)
                    .build();
        }
        List<ItemView> items = cart.stream()
                .map(c -> {
                    ItemView itemDto = itemMapper.itemToItemDto(c.getItem());
                    itemDto.setCount(c.getCount());
                    itemDto.setPrice(roundToTwoDecimals(c.getCount() * c.getItem().getPrice()));
                    return itemDto;
                })
                .sorted(Comparator.comparing(ItemView::getTitle))
                .toList();
         return CartView.builder()
                .items(items)
                .total(calculateTotal(items))
                .empty(false)
                .build();
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
