package ru.yandex.practicum.intershop.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table("cart")
@Getter
@Setter
@NoArgsConstructor
public class Cart {
    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    private Integer count;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}