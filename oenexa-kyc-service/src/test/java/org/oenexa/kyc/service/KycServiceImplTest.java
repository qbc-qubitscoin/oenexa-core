package org.oenexa.kyc.service;

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
import org.oenexa.kyc.service.impl.InternalKycProviderServiceImpl;
import org.oenexa.kyc.service.impl.KycServiceImpl;
import org.oenexa.kyc.service.impl.S3StorageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
public class KycServiceImplTest {

    @Autowired
    private KycServiceImpl kycService;

    @Autowired
    private InternalKycProviderServiceImpl kycProviderService;

    @Autowired
    private S3StorageServiceImpl s3StorageService;

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        documentRepository.deleteAll();
        kycProfileRepository.deleteAll();
    }

    @Test
    @DisplayName("Given new user ID, When getKycStatus is requested, Then a pending KYC profile is created and returned")
    void testGetKycStatus_WhenNewProfile_ShouldCreateAndReturnPending() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        KycProfileDto profile = kycService.getKycStatus(userId);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.userId()).isEqualTo(userId);
        assertThat(profile.status()).isEqualTo(KycStatus.PENDING);
        assertThat(profile.documents()).isEmpty();
    }

    @Test
    @DisplayName("Given existing user with uploaded documents, When getKycStatus is requested, Then profile with attached documents is returned")
    void testGetKycStatus_WhenExistingProfile_ShouldReturnExistingWithDocs() {
        // Given
        UUID userId = UUID.randomUUID();
        kycService.getKycStatus(userId);
        MockMultipartFile file = new MockMultipartFile("file", "passport.jpg", "image/jpeg", "dummy content".getBytes());
        kycService.uploadDocument(userId, DocumentType.PASSPORT, file);

        // When
        KycProfileDto profile = kycService.getKycStatus(userId);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.userId()).isEqualTo(userId);
        assertThat(profile.documents()).hasSize(1);
        assertThat(profile.documents().get(0).documentType()).isEqualTo(DocumentType.PASSPORT);
    }

    @Test
    @DisplayName("Given existing profile and valid submission request, When submitKyc is called, Then profile status transitions to IN_REVIEW")
    void testSubmitKyc_WhenProfileExists_ShouldSetInReview() {
        // Given
        UUID userId = UUID.randomUUID();
        kycService.getKycStatus(userId);
        MockMultipartFile file = new MockMultipartFile("file", "passport.jpg", "image/jpeg", "dummy content".getBytes());
        kycService.uploadDocument(userId, DocumentType.PASSPORT, file);
        SubmitKycRequest request = new SubmitKycRequest("Documents uploaded");

        // When
        KycProfileDto result = kycService.submitKyc(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(KycStatus.IN_REVIEW);
        assertThat(result.documents()).hasSize(1);
    }

    @Test
    @DisplayName("Given non-existent user ID, When submitKyc is called, Then RuntimeException is thrown with message KYC profile not found")
    void testSubmitKyc_WhenProfileNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        SubmitKycRequest request = new SubmitKycRequest("Documents uploaded");

        // When & Then
        assertThatThrownBy(() -> kycService.submitKyc(nonExistentUserId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("KYC profile not found");
    }

    @Test
    @DisplayName("Given existing user profile and valid file, When uploadDocument is called, Then document is saved and returned with document URL")
    void testUploadDocument_WhenProfileExists_ShouldSaveDocument() {
        // Given
        UUID userId = UUID.randomUUID();
        kycService.getKycStatus(userId);
        MockMultipartFile file = new MockMultipartFile("file", "id.png", "image/png", "sample bytes".getBytes());

        // When
        DocumentDto doc = kycService.uploadDocument(userId, DocumentType.NATIONAL_ID, file);

        // Then
        assertThat(doc).isNotNull();
        assertThat(doc.documentType()).isEqualTo(DocumentType.NATIONAL_ID);
        assertThat(doc.documentUrl()).isNotBlank();
    }

    @Test
    @DisplayName("Given non-existent user ID, When uploadDocument is called, Then RuntimeException is thrown indicating profile not found")
    void testUploadDocument_WhenProfileNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "id.png", "image/png", "sample bytes".getBytes());

        // When & Then
        assertThatThrownBy(() -> kycService.uploadDocument(nonExistentUserId, DocumentType.NATIONAL_ID, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("KYC profile not found");
    }

    @Test
    @DisplayName("Given profile under review and approved review decision, When adminReview is executed, Then status becomes VERIFIED and rejection reason is null")
    void testAdminReview_Approved_ShouldClearRejectionReason() {
        // Given
        UUID userId = UUID.randomUUID();
        kycService.getKycStatus(userId);
        MockMultipartFile file = new MockMultipartFile("file", "passport.jpg", "image/jpeg", "dummy content".getBytes());
        kycService.uploadDocument(userId, DocumentType.PASSPORT, file);
        AdminReviewRequest request = new AdminReviewRequest(KycStatus.VERIFIED, null);

        // When
        KycProfileDto result = kycService.adminReview(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(KycStatus.VERIFIED);
        assertThat(result.rejectionReason()).isNull();
        assertThat(result.documents()).hasSize(1);
    }

    @Test
    @DisplayName("Given profile under review and rejected review decision, When processAdminReview is called, Then status becomes REJECTED and rejection reason is recorded")
    void testAdminReview_Rejected_ShouldSetRejectionReason() {
        // Given
        UUID userId = UUID.randomUUID();
        kycService.getKycStatus(userId);
        MockMultipartFile file = new MockMultipartFile("file", "passport.jpg", "image/jpeg", "dummy content".getBytes());
        kycService.uploadDocument(userId, DocumentType.PASSPORT, file);
        AdminReviewRequest request = new AdminReviewRequest(KycStatus.REJECTED, "Document illegible");

        // When
        KycProfileDto result = kycProviderService.processAdminReview(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(KycStatus.REJECTED);
        assertThat(result.rejectionReason()).isEqualTo("Document illegible");
        assertThat(result.documents()).hasSize(1);
    }

    @Test
    @DisplayName("Given non-existent user ID, When processAdminReview is called, Then RuntimeException is thrown indicating profile not found")
    void testAdminReview_WhenProfileNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        AdminReviewRequest request = new AdminReviewRequest(KycStatus.VERIFIED, null);

        // When & Then
        assertThatThrownBy(() -> kycProviderService.processAdminReview(nonExistentUserId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("KYC profile not found");
    }

    @Test
    @DisplayName("Given multipart file, When uploadFile is called on S3StorageService, Then mock storage URL is returned")
    void testS3StorageService_UploadFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf bytes".getBytes());

        // When
        String url = s3StorageService.uploadFile(file);

        // Then
        assertThat(url).isNotBlank().startsWith("https://mock-storage.oenexa.com/kyc/");
    }
}
