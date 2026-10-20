package com.hoangbaoshop.hoangbaocosmetics_backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Lớp cơ sở trừu tượng cho toàn bộ Integration Test:
 * - Khởi động container MySQL 8.0 độc lập qua Docker.
 * - Tự động nạp schema & data mẫu qua Flyway.
 * - DynamicPropertySource tiêm thông tin kết nối động vào Spring Context.
 */
@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("hoangbao_cosmetics_test_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("jwt.secret", () -> "dGhpc0lzQVZlcnlTZWN1cmVKd3RTZWNyZXRLZXlGb3JUZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Ng==");
        registry.add("payment.sepay.api-key", () -> "test-sepay-api-key");
    }
}
