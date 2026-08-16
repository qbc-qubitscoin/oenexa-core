package org.oenexa.kyc.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.oenexa.kyc.KycServiceApplication;
import org.oenexa.kyc.config.TestKafkaConfig;
import org.oenexa.kyc.controller.KycController;
import org.oenexa.kyc.dto.request.SubmitKycRequest;
import org.oenexa.kyc.dto.response.KycProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@SpringBootTest(classes = {KycServiceApplication.class, TestKafkaConfig.class})
@ActiveProfiles("test")
public class KycStepDefinitions {

    @Autowired
    private KycController kycController;

    private UUID userId;
    private ResponseEntity<KycProfileDto> response;

    @Given("a KYC user is authenticated")
    public void a_kyc_user_is_authenticated() {
        userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @When("the user requests their KYC status")
    public void the_user_requests_their_kyc_status() {
        response = kycController.getKycStatus();
    }

    @Then("the status should be {string}")
    public void the_status_should_be(String expectedStatus) {
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(expectedStatus, response.getBody().status().name());
    }

    @When("the user submits KYC details with full name {string} and country {string}")
    public void the_user_submits_kyc_details(String fullName, String country) {
        SubmitKycRequest request = new SubmitKycRequest("User: " + fullName + ", Country: " + country);
        response = kycController.submitKyc(request);
    }
}
