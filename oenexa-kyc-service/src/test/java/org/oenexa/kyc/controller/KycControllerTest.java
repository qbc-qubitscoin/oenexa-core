package org.oenexa.kyc.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.kyc.config.TestKafkaConfig;
import org.oenexa.kyc.dto.request.AdminReviewRequest;
import org.oenexa.kyc.dto.request.SubmitKycRequest;
import org.oenexa.kyc.dto.response.DocumentDto;
import org.oenexa.kyc.dto.response.KycProfileDto;
import org.oenexa.kyc.entity.DocumentType;
import org.oenexa.kyc.entity.KycStatus;
import org.oenexa.kyc.repository.DocumentRepository;
import org.oenexa.kyc.repository.KycProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
public class KycControllerTest {

    @Autowired
    private KycController kycController;

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        kycProfileRepository.deleteAll();

        testUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUserId.toString(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        documentRepository.deleteAll();
        kycProfileRepository.deleteAll();
    }

    @Test
    @DisplayName("Given authenticated user, When getKycStatus is requested, Then return 200 OK with pending KYC profile")
    void testGetKycStatus() {
        // Given - Authenticated user session established in setup

        // When
        ResponseEntity<KycProfileDto> response = kycController.getKycStatus();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(testUserId);
        assertThat(response.getBody().status()).isEqualTo(KycStatus.PENDING);
    }

    @Test
    @DisplayName("Given existing user profile and submission request, When submitKyc is called, Then return 200 OK with IN_REVIEW status")
    void testSubmitKyc() {
        // Given
        kycController.getKycStatus(); // create profile first
        SubmitKycRequest request = new SubmitKycRequest("Documents attached");

        // When
        ResponseEntity<KycProfileDto> response = kycController.submitKyc(request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(KycStatus.IN_REVIEW);
    }

    @Test
    @DisplayName("Given valid document upload payload, When uploadDocument is called, Then return 200 OK with saved DocumentDto")
    void testUploadDocument() {
        // Given
        kycController.getKycStatus(); // create profile first
        MockMultipartFile file = new MockMultipartFile("file", "license.png", "image/png", "license data".getBytes());

        // When
        ResponseEntity<DocumentDto> response = kycController.uploadDocument(DocumentType.DRIVERS_LICENSE, file);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().documentType()).isEqualTo(DocumentType.DRIVERS_LICENSE);
        assertThat(response.getBody().documentUrl()).isNotBlank();
    }

    @Test
    @DisplayName("Given admin review approval request, When adminReview is invoked, Then return 200 OK with VERIFIED status")
    void testAdminReview() {
        // Given
        kycController.getKycStatus(); // create profile first
        AdminReviewRequest reviewRequest = new AdminReviewRequest(KycStatus.VERIFIED, null);

        // When
        ResponseEntity<KycProfileDto> response = kycController.adminReview(testUserId, reviewRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(KycStatus.VERIFIED);
    }
}
