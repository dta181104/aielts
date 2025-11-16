package com.example.shopmohinh;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopmohinhApplication {

	public static void main(String[] args) {
		String env = System.getenv("SPRING_PROFILES_ACTIVE");
		if (env == null || env.equals("dev")) {
			System.out.println("Loading .env (DEV mode)...");
			Dotenv dotenv = Dotenv.load();
			dotenv.entries().forEach(e ->
					System.setProperty(e.getKey(), e.getValue())
			);
		} else {
			System.out.println("Running with environment variables (Docker mode)...");
		}

		SpringApplication.run(ShopmohinhApplication.class, args);
	}

}
