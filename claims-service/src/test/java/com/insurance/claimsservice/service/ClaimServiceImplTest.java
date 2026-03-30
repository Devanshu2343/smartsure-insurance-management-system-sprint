package com.insurance.claimsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.claimsservice.client.AuthClient;
import com.insurance.claimsservice.client.PolicyClient;
import com.insurance.claimsservice.dto.ClaimResponse;
import com.insurance.claimsservice.dto.ClaimStatusResponse;
import com.insurance.claimsservice.dto.ClaimSummaryResponse;
import com.insurance.claimsservice.dto.InitiateClaimRequest;
import com.insurance.claimsservice.dto.PolicyInfoResponse;
import com.insurance.claimsservice.dto.UploadClaimRequest;
import com.insurance.claimsservice.entity.Claim;
import com.insurance.claimsservice.entity.ClaimStatus;
import com.insurance.claimsservice.exception.BadRequestException;
import com.insurance.claimsservice.exception.ForbiddenException;
import com.insurance.claimsservice.exception.ResourceNotFoundException;
import com.insurance.claimsservice.exception.UserNotFoundException;
import com.insurance.claimsservice.repository.ClaimRepository;
import com.insurance.claimsservice.service.ClaimEventPublisher;
import com.insurance.claimsservice.service.impl.ClaimServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyClient policyClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter claimsSubmittedCounter;

    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Test
    void initiateClaimCreatesDraftForValidCustomerAndPolicy() {
        // Arrange
        InitiateClaimRequest request = new InitiateClaimRequest();
        request.setPolicyId(10L);
        request.setClaimAmount(BigDecimal.valueOf(2500));
        request.setDescription("Accident damage");

        when(authClient.validateUser(eq("user@test.com"))).thenReturn(true);
        when(policyClient.getPolicyById(eq(10L)))
                .thenReturn(new PolicyInfoResponse(10L, "POL-10", "user@test.com", "ACTIVE"));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setUpdatedAt(LocalDateTime.now());
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        // Act
        ClaimResponse response = claimService.initiateClaim(request, "user@test.com", "CUSTOMER");

        // Assert
        assertEquals(ClaimStatus.DRAFT, response.getStatus());
        assertEquals("user@test.com", response.getCustomerEmail());
        assertThat(response.getClaimNumber()).startsWith("CLM-");
    }

    @Test
    void initiateClaimThrowsWhenUserNotRegistered() {
        // Arrange
        InitiateClaimRequest request = new InitiateClaimRequest();
        request.setPolicyId(10L);
        request.setClaimAmount(BigDecimal.valueOf(2500));
        request.setDescription("Accident damage");

        when(authClient.validateUser(eq("user@test.com"))).thenReturn(false);

        // Act + Assert
        assertThrows(UserNotFoundException.class,
                () -> claimService.initiateClaim(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void initiateClaimThrowsWhenPolicyNotActive() {
        // Arrange
        InitiateClaimRequest request = new InitiateClaimRequest();
        request.setPolicyId(10L);
        request.setClaimAmount(BigDecimal.valueOf(2500));
        request.setDescription("Accident damage");

        when(authClient.validateUser(eq("user@test.com"))).thenReturn(true);
        when(policyClient.getPolicyById(eq(10L)))
                .thenReturn(new PolicyInfoResponse(10L, "POL-10", "user@test.com", "EXPIRED"));

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> claimService.initiateClaim(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void uploadClaimDocumentTransitionsDraftToSubmitted() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setCustomerEmail("user@test.com");
        claim.setStatus(ClaimStatus.DRAFT);

        UploadClaimRequest request = new UploadClaimRequest();
        request.setClaimId(1L);
        request.setDocumentUrl("http://doc.example.com/claim.pdf");

        when(meterRegistry.counter(eq("claims.submitted"), eq("service"), eq("claims-service")))
                .thenReturn(claimsSubmittedCounter);
        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClaimResponse response = claimService.uploadClaimDocument(request, "user@test.com", "CUSTOMER");

        // Assert
        assertEquals(ClaimStatus.SUBMITTED, response.getStatus());
        assertEquals("http://doc.example.com/claim.pdf", response.getDocumentUrl());
        verify(claimEventPublisher).publishClaimSubmitted(any());
    }

    @Test
    void uploadClaimDocumentRejectsFinalStatus() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setCustomerEmail("user@test.com");
        claim.setStatus(ClaimStatus.APPROVED);

        UploadClaimRequest request = new UploadClaimRequest();
        request.setClaimId(1L);
        request.setDocumentUrl("http://doc.example.com/claim.pdf");

        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> claimService.uploadClaimDocument(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void getClaimStatusThrowsForbiddenForDifferentCustomer() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setCustomerEmail("owner@test.com");
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setUpdatedAt(LocalDateTime.now());

        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));

        // Act + Assert
        assertThrows(ForbiddenException.class,
                () -> claimService.getClaimStatus(1L, "other@test.com", "CUSTOMER"));
    }

    @Test
    void reviewClaimApprovesSubmittedClaim() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClaimResponse response = claimService.reviewClaim(1L, "APPROVED", "ok", "ADMIN");

        // Assert
        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        verify(claimEventPublisher).publishClaimReviewed(any());
    }

    @Test
    void reviewClaimRejectsInvalidDecision() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> claimService.reviewClaim(1L, "MAYBE", "", "ADMIN"));
    }

    @Test
    void getClaimSummaryReturnsCountsForAdmin() {
        // Arrange
        when(claimRepository.count()).thenReturn(10L);
        when(claimRepository.countByStatus(ClaimStatus.APPROVED)).thenReturn(4L);
        when(claimRepository.countByStatus(ClaimStatus.REJECTED)).thenReturn(1L);
        when(claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW)).thenReturn(2L);

        // Act
        ClaimSummaryResponse response = claimService.getClaimSummary("ADMIN");

        // Assert
        assertEquals(10L, response.getTotalClaims());
        assertEquals(4L, response.getApprovedClaims());
        assertEquals(1L, response.getRejectedClaims());
        assertEquals(2L, response.getUnderReviewClaims());
    }

    @Test
    void getClaimSummaryThrowsForbiddenForNonAdmin() {
        // Act + Assert
        assertThrows(ForbiddenException.class, () -> claimService.getClaimSummary("CUSTOMER"));
    }

    @Test
    void uploadClaimDocumentThrowsWhenClaimMissing() {
        // Arrange
        UploadClaimRequest request = new UploadClaimRequest();
        request.setClaimId(42L);
        request.setDocumentUrl("http://doc.example.com/claim.pdf");

        when(claimRepository.findById(eq(42L))).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> claimService.uploadClaimDocument(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void getClaimStatusReturnsStatusForOwner() {
        // Arrange
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-1");
        claim.setCustomerEmail("user@test.com");
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setUpdatedAt(LocalDateTime.now());

        when(claimRepository.findById(eq(1L))).thenReturn(Optional.of(claim));

        // Act
        ClaimStatusResponse response = claimService.getClaimStatus(1L, "user@test.com", "CUSTOMER");

        // Assert
        assertEquals(ClaimStatus.SUBMITTED, response.getStatus());
        assertEquals(1L, response.getClaimId());
    }
}
