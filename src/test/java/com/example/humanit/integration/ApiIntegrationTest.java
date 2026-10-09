package com.example.humanit.integration;

import com.example.humanit.model.Client;
import com.example.humanit.repository.ClientRepository;
import com.example.humanit.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:api-integration-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.sql.init.mode=always"
})
class ApiIntegrationTest {

    private static final String PASSWORD = "integration-password-123";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void clearDatabase() {
        clientRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void register_createsAccount() throws Exception {
        String email = uniqueEmail();

        HttpResponse<String> response = send("POST", "/auth/register", registerBody(email), null);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat((Object) read(response.body(), "$.id")).isNotNull();
        assertThat((String) read(response.body(), "$.email")).isEqualTo(email);
        assertThat(response.body()).doesNotContain("\"password\"", "\"passwordHash\"");

        assertThat(userAccountRepository.existsByEmailIgnoreCase(email)).isTrue();
    }

    @Test
    void login_returnsTokenForRegisteredAccount() throws Exception {
        String email = uniqueEmail();
        register(email);

        HttpResponse<String> response = send("POST", "/auth/login", registerBody(email), null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat((String) read(response.body(), "$.accessToken")).isNotBlank();
        assertThat((String) read(response.body(), "$.tokenType")).isEqualTo("Bearer");
        assertThat((Object) read(response.body(), "$.expiresIn")).isNotNull();
    }

    @Test
    void create_persistsClient() throws Exception {
        String token = registerAndLogin();
        String taxIdentifier = "TAX-" + UUID.randomUUID();

        HttpResponse<String> response = send(
                "POST", "/clients", clientBody("John", "Doe", taxIdentifier, "john@example.com"), token
        );

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat((String) read(response.body(), "$.firstName")).isEqualTo("John");
        Number id = read(response.body(), "$.id");
        assertThat(clientRepository.findById(id.longValue())).isPresent();
    }

    @Test
    void getAll_returnsPersistedClients() throws Exception {
        String token = registerAndLogin();
        clientRepository.save(new Client("Jane", "Smith", "TAX-" + UUID.randomUUID(),
                "jane@example.com", null));

        HttpResponse<String> response = send("GET", "/clients", null, token);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat((String) read(response.body(), "$[0].firstName")).isEqualTo("Jane");
        assertThat((String) read(response.body(), "$[0].lastName")).isEqualTo("Smith");
    }

    @Test
    void getById_returnsPersistedClient() throws Exception {
        String token = registerAndLogin();
        Client client = clientRepository.save(new Client("Jane", "Smith", "TAX-" + UUID.randomUUID(),
                "jane@example.com", null));

        HttpResponse<String> response = send("GET", "/clients/" + client.getId(), null, token);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(((Number) read(response.body(), "$.id")).longValue()).isEqualTo(client.getId());
        assertThat((String) read(response.body(), "$.email")).isEqualTo("jane@example.com");
    }

    @Test
    void update_persistsClientChanges() throws Exception {
        String token = registerAndLogin();
        Client existing = clientRepository.save(new Client("John", "Doe", "TAX-" + UUID.randomUUID(),
                "john@example.com", null));
        String updatedEmail = "johnny@example.com";

        HttpResponse<String> response = send(
                "PUT",
                "/clients/" + existing.getId(),
                clientBody("Johnny", "Doe", existing.getTaxIdentifier(), updatedEmail),
                token
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat((String) read(response.body(), "$.firstName")).isEqualTo("Johnny");
        assertThat((String) read(response.body(), "$.email")).isEqualTo(updatedEmail);

        assertThat(clientRepository.findById(existing.getId()).orElseThrow().getEmail())
                .isEqualTo(updatedEmail);
    }

    @Test
    void delete_removesPersistedClient() throws Exception {
        String token = registerAndLogin();
        Client client = clientRepository.save(new Client("John", "Doe", "TAX-" + UUID.randomUUID(),
                "john@example.com", null));

        HttpResponse<String> response = send("DELETE", "/clients/" + client.getId(), null, token);

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(clientRepository.findById(client.getId())).isEmpty();
    }

    private String registerAndLogin() throws Exception {
        String email = uniqueEmail();
        register(email);
        HttpResponse<String> response = send("POST", "/auth/login", registerBody(email), null);
        assertThat(response.statusCode()).isEqualTo(200);
        return read(response.body(), "$.accessToken");
    }

    private void register(String email) throws Exception {
        HttpResponse<String> response = send("POST", "/auth/register", registerBody(email), null);
        assertThat(response.statusCode()).isEqualTo(201);
    }

    private HttpResponse<String> send(String method, String path, String body, String token)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json");
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        return httpClient.send(request.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String uniqueEmail() {
        return "integration-" + UUID.randomUUID() + "@example.com";
    }

    private String registerBody(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }

    private String clientBody(String firstName, String lastName, String taxIdentifier, String email) {
        return "{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName
                + "\",\"taxIdentifier\":\"" + taxIdentifier + "\",\"email\":\"" + email
                + "\",\"phoneNumber\":\"+1-555-0101\",\"documents\":[]}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
