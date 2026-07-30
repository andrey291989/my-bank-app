package ru.yandex.practicum.accounts.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke/context + persistence тест accounts-service (замечания ревью #2, #20):
 * поднимает реальный PostgreSQL (Testcontainers), проверяет, что контекст стартует,
 * применяются Flyway-миграции и проходит Hibernate ddl-auto=validate
 * (это ловит рассинхрон типов, например INTEGER vs NUMERIC для денежных полей).
 *
 * <p>Требует Docker. Без Docker тест автоматически пропускается
 * ({@code disabledWithoutDocker = true}), поэтому не ломает сборку в средах без него.
 */
@SpringBootTest(properties = {
        // OAuth2 client тянет OIDC discovery к Keycloak на старте — в тесте не нужен
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration",
        // Kafka-брокер в этом тесте не поднимается; админ не должен падать
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@Testcontainers(disabledWithoutDocker = true)
class AccountsPersistenceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bank_db")
            .withUsername("bank_user")
            .withPassword("bank_password");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * Заглушка JwtDecoder, чтобы resource-server не обращался к Keycloak за JWKS на старте.
     */
    @TestConfiguration
    static class StubDecoderConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token).header("alg", "none").claim("sub", "test").build();
        }
    }

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void contextLoadsFlywayAppliesAndValidatePasses() {
        assertNotNull(accountRepository);
        // V2__insert_test_data наполняет таблицу — значит миграции применились и схема валидна
        assertFalse(accountRepository.findAll().isEmpty(), "Flyway должен был вставить тестовые данные");
    }
}
