package com.hoangbaoshop.hoangbaocosmetics_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HoangbaocosmeticsBackendApplication {

	public static void main(String[] args) {
		// Nạp biến môi trường từ file .env cục bộ (chuẩn 12-factor doanh nghiệp)
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(HoangbaocosmeticsBackendApplication.class, args);
	}

}
