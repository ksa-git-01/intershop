package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.ItemView;
import ru.yandex.practicum.intershop.dto.OrderView;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.model.Order;
import ru.yandex.practicum.intershop.model.OrderItem;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;
import ru.yandex.practicum.intershop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;

    public Flux<OrderView> getOrders() {
        return orderRepository.findAll()
                .flatMap(this::buildOrderView);
    }

    private Mono<OrderView> buildOrderView(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(this::orderItemToItemView)
                .collectList()
                .map(items -> OrderView.builder()
                        .id(order.getId())
                        .items(items)
                        .build());
    }

    private Mono<ItemView> orderItemToItemView(OrderItem orderItem) {
        return itemRepository.findById(orderItem.getItemId())
                .map(item -> createItemView(orderItem, item));
    }

    private ItemView createItemView(OrderItem orderItem, Item item) {
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getFilename(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }

    public Mono<OrderView> getOrder(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Order not found")))
                .flatMap(this::buildOrderView);
    }

    @Transactional
    public Mono<Long> buy() {
        return cartRepository.findAll()
                .collectList()
                .flatMap(this::processOrder);
    }

    private Mono<Long> processOrder(List<Cart> cartItems) {
        if (cartItems.isEmpty()) {
            return Mono.error(new IllegalStateException("Cart is empty"));
        }

        return validateStock(cartItems)
                .then(createOrder())
                .flatMap(order -> processCartItems(cartItems, order))
                .flatMap(order -> clearCart().thenReturn(order.getId()));
    }

    private Mono<Void> validateStock(List<Cart> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(this::validateCartItemStock)
                .then();
    }

    private Mono<Void> validateCartItemStock(Cart cartItem) {
        return itemRepository.findById(cartItem.getItemId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Item not found: " + cartItem.getItemId())))
                .flatMap(item -> {
                    if (item.getCount() < cartItem.getCount()) {
                        return Mono.error(new IllegalStateException("Not enough stock for item " + item.getId()));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Order> createOrder() {
        Order order = new Order();
        return orderRepository.save(order);
    }

    private Mono<Order> processCartItems(List<Cart> cartItems, Order order) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> processCartItem(cartItem, order))
                .then(Mono.just(order));
    }

    private Mono<Void> processCartItem(Cart cartItem, Order order) {
        return itemRepository.findById(cartItem.getItemId())
                .flatMap(item -> updateItemStock(item, cartItem.getCount()))
                .flatMap(item -> createOrderItem(order, item, cartItem))
                .then();
    }

    private Mono<Item> updateItemStock(Item item, Integer purchasedCount) {
        item.setCount(item.getCount() - purchasedCount);
        return itemRepository.save(item);
    }

    private Mono<OrderItem> createOrderItem(Order order, Item item, Cart cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setItemId(item.getId());
        orderItem.setCount(cartItem.getCount());
        orderItem.setPrice(item.getPrice());
        return orderItemRepository.save(orderItem);
    }

    private Mono<Void> clearCart() {
        return cartRepository.deleteAll();
    }
}