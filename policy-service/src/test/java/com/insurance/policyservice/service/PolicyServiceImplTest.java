package com.insurance.policyservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.policyservice.client.AuthClient;
import com.insurance.policyservice.dto.PolicyResponse;
import com.insurance.policyservice.dto.PolicySummaryResponse;
import com.insurance.policyservice.dto.PurchasePolicyRequest;
import com.insurance.policyservice.entity.Policy;
import com.insurance.policyservice.entity.PolicyStatus;
import com.insurance.policyservice.exception.BadRequestException;
import com.insurance.policyservice.exception.ForbiddenException;
import com.insurance.policyservice.exception.ResourceNotFoundException;
import com.insurance.policyservice.exception.UnauthorizedException;
import com.insurance.policyservice.exception.UserNotFoundException;
import com.insurance.policyservice.repository.PolicyRepository;
import com.insurance.policyservice.service.PolicyEventPublisher;
import com.insurance.policyservice.service.impl.PolicyServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter policiesCreatedCounter;

    @Mock
    private PolicyEventPublisher policyEventPublisher;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Test
    void purchasePolicyCreatesPolicyForCustomer() {
        // Arrange
        PurchasePolicyRequest request = validRequest();
        when(authClient.validateUser(eq("user@test.com"))).thenReturn(true);
        when(meterRegistry.counter(eq("policies.created"), eq("service"), eq("policy-service")))
                .thenReturn(policiesCreatedCounter);
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> {
            Policy saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        PolicyResponse response = policyService.purchasePolicy(request, "user@test.com", "CUSTOMER");

        // Assert
        assertEquals("user@test.com", response.getCustomerEmail());
        assertEquals("Health", response.getPolicyType());
        assertEquals(PolicyStatus.ACTIVE, response.getStatus());
        assertThat(response.getPolicyNumber()).startsWith("POL-");
        verify(policyEventPublisher).publishPolicyCreated(any());
    }

    @Test
    void purchasePolicyThrowsWhenUserIsNotRegistered() {
        // Arrange
        PurchasePolicyRequest request = validRequest();
        when(authClient.validateUser(eq("user@test.com"))).thenReturn(false);

        // Act + Assert
        assertThrows(UserNotFoundException.class,
                () -> policyService.purchasePolicy(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void purchasePolicyThrowsForbiddenWhenEmailMismatch() {
        // Arrange
        PurchasePolicyRequest request = validRequest();
        request.setCustomerEmail("other@test.com");

        // Act + Assert
        assertThrows(ForbiddenException.class,
                () -> policyService.purchasePolicy(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void purchasePolicyThrowsBadRequestForInvalidDates() {
        // Arrange
        PurchasePolicyRequest request = validRequest();
        request.setEndDate(request.getStartDate().minusDays(1));

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> policyService.purchasePolicy(request, "user@test.com", "CUSTOMER"));
    }

    @Test
    void getPolicyByIdThrowsWhenMissing() {
        // Arrange
        when(policyRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.getPolicyById(1L, "admin@test.com", "ADMIN"));
    }

    @Test
    void getPolicyByIdThrowsForbiddenForDifferentCustomer() {
        // Arrange
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-1");
        policy.setCustomerEmail("owner@test.com");
        policy.setPolicyType("Health");
        policy.setCoverageAmount(BigDecimal.TEN);
        policy.setPremiumAmount(BigDecimal.ONE);
        policy.setStartDate(LocalDate.now().minusDays(1));
        policy.setEndDate(LocalDate.now().plusDays(10));
        policy.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(eq(1L))).thenReturn(Optional.of(policy));

        // Act + Assert
        assertThrows(ForbiddenException.class,
                () -> policyService.getPolicyById(1L, "other@test.com", "CUSTOMER"));
    }

    @Test
    void getPolicyByIdUpdatesStatusWhenOutdated() {
        // Arrange
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-1");
        policy.setCustomerEmail("user@test.com");
        policy.setPolicyType("Health");
        policy.setCoverageAmount(BigDecimal.TEN);
        policy.setPremiumAmount(BigDecimal.ONE);
        policy.setStartDate(LocalDate.now().minusDays(3));
        policy.setEndDate(LocalDate.now().plusDays(3));
        policy.setStatus(PolicyStatus.CREATED);

        when(policyRepository.findById(eq(1L))).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PolicyResponse response = policyService.getPolicyById(1L, "admin@test.com", "ADMIN");

        // Assert
        verify(policyRepository).save(any(Policy.class));
        assertEquals(PolicyStatus.ACTIVE, response.getStatus());
    }

    @Test
    void getPoliciesForCustomerReturnsMappedResponses() {
        // Arrange
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-1");
        policy.setCustomerEmail("user@test.com");
        policy.setPolicyType("Health");
        policy.setCoverageAmount(BigDecimal.TEN);
        policy.setPremiumAmount(BigDecimal.ONE);
        policy.setStartDate(LocalDate.now().minusDays(1));
        policy.setEndDate(LocalDate.now().plusDays(10));
        policy.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findAllByCustomerEmailIgnoreCase(eq("user@test.com")))
                .thenReturn(List.of(policy));

        // Act
        List<PolicyResponse> responses = policyService.getPoliciesForCustomer("user@test.com", "CUSTOMER");

        // Assert
        assertThat(responses).hasSize(1);
        assertEquals("POL-1", responses.get(0).getPolicyNumber());
    }

    @Test
    void getPoliciesForCustomerThrowsWhenRoleMissing() {
        // Act + Assert
        assertThrows(UnauthorizedException.class,
                () -> policyService.getPoliciesForCustomer("user@test.com", null));
    }

    @Test
    void createPolicyForAdminRequiresAdminRole() {
        // Arrange
        PurchasePolicyRequest request = validRequest();

        // Act + Assert
        assertThrows(ForbiddenException.class,
                () -> policyService.createPolicyForAdmin(request, "CUSTOMER"));
    }

    @Test
    void updatePolicyForAdminThrowsWhenPolicyMissing() {
        // Arrange
        PurchasePolicyRequest request = validRequest();
        when(policyRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.updatePolicyForAdmin(1L, request, "ADMIN"));
    }

    @Test
    void deletePolicyForAdminDeletesWhenFound() {
        // Arrange
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-1");
        when(policyRepository.findById(eq(1L))).thenReturn(Optional.of(policy));

        // Act
        policyService.deletePolicyForAdmin(1L, "ADMIN");

        // Assert
        verify(policyRepository).delete(eq(policy));
    }

    @Test
    void getPolicySummaryReturnsCountsForAdmin() {
        // Arrange
        when(policyRepository.count()).thenReturn(10L);
        when(policyRepository.countByStatus(PolicyStatus.ACTIVE)).thenReturn(6L);
        when(policyRepository.countByStatus(PolicyStatus.EXPIRED)).thenReturn(2L);

        // Act
        PolicySummaryResponse response = policyService.getPolicySummary("ADMIN");

        // Assert
        assertEquals(10L, response.getTotalPolicies());
        assertEquals(6L, response.getActivePolicies());
        assertEquals(2L, response.getExpiredPolicies());
    }

    private PurchasePolicyRequest validRequest() {
        PurchasePolicyRequest request = new PurchasePolicyRequest();
        request.setPolicyType(" Health ");
        request.setCoverageAmount(BigDecimal.valueOf(100000));
        request.setPremiumAmount(BigDecimal.valueOf(1000));
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setCustomerEmail("user@test.com");
        return request;
    }
}
