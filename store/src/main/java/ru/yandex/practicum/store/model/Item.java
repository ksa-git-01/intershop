package ru.yandex.practicum.store.model;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table("item")
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    private Long id;
    private String title;
    private String description;
    private String filename;
    private Integer count;
    private Double price;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}