package org.oenexa.kyc.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    void testGetKycStatus() {
        ResponseEntity<KycProfileDto> response = kycController.getKycStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(testUserId);
        assertThat(response.getBody().status()).isEqualTo(KycStatus.PENDING);
    }

    @Test
    void testSubmitKyc() {
        kycController.getKycStatus(); // create profile first
        SubmitKycRequest request = new SubmitKycRequest("Documents attached");
        ResponseEntity<KycProfileDto> response = kycController.submitKyc(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(KycStatus.IN_REVIEW);
    }

    @Test
    void testUploadDocument() {
        kycController.getKycStatus(); // create profile first
        MockMultipartFile file = new MockMultipartFile("file", "license.png", "image/png", "license data".getBytes());
        ResponseEntity<DocumentDto> response = kycController.uploadDocument(DocumentType.DRIVERS_LICENSE, file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().documentType()).isEqualTo(DocumentType.DRIVERS_LICENSE);
        assertThat(response.getBody().documentUrl()).isNotBlank();
    }

    @Test
    void testAdminReview() {
        kycController.getKycStatus(); // create profile first
        AdminReviewRequest reviewRequest = new AdminReviewRequest(KycStatus.VERIFIED, null);
        ResponseEntity<KycProfileDto> response = kycController.adminReview(testUserId, reviewRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(KycStatus.VERIFIED);
    }
}
