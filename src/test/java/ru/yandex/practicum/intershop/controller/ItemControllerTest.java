package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.service.FileService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ItemControllerTest extends BasicTestConfiguration {
    @MockitoBean
    private FileService fileService;

    private Long itemId;

    @BeforeEach
    void setUpData() {
        itemId = insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
    }

    @Test
    void getItem() throws Exception {
        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void modifyCartItemPlus() throws Exception {
        mockMvc.perform(post("/items/{id}", itemId).param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/" + itemId));

        Integer inCart = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(inCart).isEqualTo(1);
    }

    @Test
    void showAddItemForm() throws Exception {
        mockMvc.perform(get("/items/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-item"));
    }

    @Test
    void addItem() throws Exception {
        reset(fileService);
        when(fileService.saveImage(any())).thenReturn("filename.jpg");

        MockMultipartFile image = new MockMultipartFile(
                "filename", "filename.jpg", "image/jpeg", "filebody".getBytes());

        Integer before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM item", Integer.class);
        assertThat(before).isEqualTo(1);

        mockMvc.perform(multipart("/items/add")
                        .file(image)
                        .param("title", "Новый товар")
                        .param("description", "Описание")
                        .param("count", "5")
                        .param("price", "123.45")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items/*"));

        Integer after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM item", Integer.class);
        assertThat(after).isEqualTo(2);

        String filename = jdbcTemplate.queryForObject(
                "SELECT filename FROM item ORDER BY id DESC LIMIT 1", String.class);
        assertThat(filename).isEqualTo("filename.jpg");
    }
}
