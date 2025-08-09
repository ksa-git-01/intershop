package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;


    public List<OrderView> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order ->
                    OrderView.builder()
                            .id(order.getId())
                            .items(order.getOrderItems().stream()
                                    .map(this::toItemDto)
                                    .toList())
                            .build()
                    )
                .toList();
    }

    private ItemView toItemDto(OrderItem orderItem) {
        Item item = orderItem.getItem();
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getFilename(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }

    public OrderView getOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(o -> OrderView.builder()
                .id(o.getId())
                .items(o.getOrderItems().stream()
                        .map(this::toItemDto)
                        .toList())
                .build())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Transactional
    public Long buy() {
        List<Cart> cartItems = cartRepository.findAll();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        for (Cart c : cartItems) {
            Item item = itemRepository.findById(c.getItem().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Item not found: " + c.getItem().getId()));
            if (item.getCount() < c.getCount()) {
                throw new IllegalStateException("Not enough stock for item " + item.getId());
            }
        }

        Order order = new Order();
        order = orderRepository.save(order);

        for (Cart c : cartItems) {
            Item item = itemRepository.getReferenceById(c.getItem().getId());

            item.setCount(item.getCount() - c.getCount());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setCount(c.getCount());
            orderItem.setPrice(item.getPrice());
            orderItemRepository.save(orderItem);
        }

        cartRepository.deleteAllInBatch();

        return order.getId();
    }
}
