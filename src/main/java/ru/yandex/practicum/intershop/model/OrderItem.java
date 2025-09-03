package ru.yandex.practicum.intershop.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table("order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    private Integer count;
    private Double price;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
}