package com.example.deposits;

import com.example.deposits.api.dto.AccountResponse;
import com.example.deposits.api.dto.BatchImportRequest;
import com.example.deposits.api.dto.CreateAccountRequest;
import com.example.deposits.api.dto.DepositRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("deposits")
            .withUsername("deposits")
            .withPassword("deposits");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String base() {
        return "http://localhost:" + port;
    }

    @Test
    void createAccountDepositAndVerifyBalance() {
        // Create account
        CreateAccountRequest createReq = new CreateAccountRequest("EUR", "ACC-IT-1");
        ResponseEntity<AccountResponse> createResp = restTemplate.postForEntity(
                base() + "/accounts", createReq, AccountResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AccountResponse created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.currencyCode()).isEqualTo("EUR");
        long id = created.id();

        // Deposit 100
        DepositRequest depositReq = new DepositRequest(new BigDecimal("100.00"));
        ResponseEntity<AccountResponse> depositResp = restTemplate.postForEntity(
                base() + "/accounts/" + id + "/deposits", depositReq, AccountResponse.class);
        assertThat(depositResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // GET and verify balance = 100.0000
        ResponseEntity<AccountResponse> getResp = restTemplate.getForEntity(
                base() + "/accounts/" + id, AccountResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AccountResponse fetched = getResp.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.balance()).isEqualByComparingTo("100.00");
    }

    @Test
    void batchImportTransactions() {
        // Create account for batch import
        CreateAccountRequest createReq = new CreateAccountRequest("USD", "ACC-IT-BATCH");
        ResponseEntity<AccountResponse> createResp = restTemplate.postForEntity(
                base() + "/accounts", createReq, AccountResponse.class);
        long id = createResp.getBody().id();

        // Batch import
        BatchImportRequest batchReq = new BatchImportRequest(id, List.of(
                new BatchImportRequest.TransactionEntry("DEPOSIT", new BigDecimal("50.00")),
                new BatchImportRequest.TransactionEntry("DEPOSIT", new BigDecimal("25.00"))
        ));
        ResponseEntity<Void> batchResp = restTemplate.postForEntity(
                base() + "/transactions:batch-import", batchReq, Void.class);
        assertThat(batchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
