package br.com.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShoppingTesteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingTesteApplication.class, args);
	}

}
