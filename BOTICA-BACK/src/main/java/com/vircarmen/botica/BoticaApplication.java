package com.vircarmen.botica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoticaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoticaApplication.class, args);
	}

}
