package ru.yandex.practicum.intershop;

import org.springframework.boot.SpringApplication;

public class TestIntershopApplication {

	public static void main(String[] args) {
		SpringApplication.from(IntershopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
