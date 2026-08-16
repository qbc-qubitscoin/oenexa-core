package org.oenexa.wallet.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.oenexa.wallet.dto.AuthResponse;
import org.oenexa.wallet.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthStepDefinitions {

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();

    private RegisterRequest request;
    private HttpResponse<String> result;
    private AuthResponse response;

    @Given("a new user with email {string} and password {string}")
    public void a_new_user_with_email_and_password(String email, String password) {
        request = new RegisterRequest(email, password);
    }

    @When("the user submits a registration request")
    public void the_user_submits_a_registration_request() throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();
                
        result = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
        if (result.statusCode() == 200) {
            response = objectMapper.readValue(result.body(), AuthResponse.class);
        }
    }

    @Then("the user should receive a valid JWT token")
    public void the_user_should_receive_a_valid_jwt_token() {
        assertEquals(200, result.statusCode());
        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Then("the user's email should be {string}")
    public void the_users_email_should_be(String expectedEmail) {
        assertEquals(expectedEmail, response.getEmail());
    }
}
