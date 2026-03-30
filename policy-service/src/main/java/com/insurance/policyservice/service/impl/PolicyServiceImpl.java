package com.insurance.policyservice.service.impl;

import com.insurance.policyservice.client.AuthClient;
import com.insurance.policyservice.dto.PolicyResponse;
import com.insurance.policyservice.dto.PolicySummaryResponse;
import com.insurance.policyservice.dto.PurchasePolicyRequest;
import com.insurance.policyservice.dto.event.PolicyCreatedEvent;
import com.insurance.policyservice.entity.Policy;
import com.insurance.policyservice.entity.PolicyStatus;
import com.insurance.policyservice.exception.BadRequestException;
import com.insurance.policyservice.exception.ForbiddenException;
import com.insurance.policyservice.exception.ResourceNotFoundException;
import com.insurance.policyservice.exception.UnauthorizedException;
import com.insurance.policyservice.exception.UserNotFoundException;
import com.insurance.policyservice.repository.PolicyRepository;
import com.insurance.policyservice.service.PolicyEventPublisher;
import com.insurance.policyservice.service.PolicyService;
import feign.FeignException;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final PolicyRepository policyRepository;
    private final AuthClient authClient;
    private final MeterRegistry meterRegistry;
    private final PolicyEventPublisher policyEventPublisher;

    @Override
    public PolicyResponse purchasePolicy(PurchasePolicyRequest request, String userEmailHeader, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_CUSTOMER);
        validateDates(request);

        String customerEmail = resolveCustomerEmail(request, userEmailHeader);
        validateUserExists(customerEmail);

        Policy policy = new Policy();
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setCustomerEmail(customerEmail);
        policy.setPolicyType(request.getPolicyType().trim());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
        policy.setStatus(calculateStatus(request.getStartDate(), request.getEndDate()));

        Policy savedPolicy = policyRepository.save(policy);
        meterRegistry.counter("policies.created", "service", "policy-service").increment();
        policyEventPublisher.publishPolicyCreated(PolicyCreatedEvent.from(savedPolicy));
        log.info("Policy purchased successfully. policyNumber={}, customerEmail={}",
                savedPolicy.getPolicyNumber(),
                savedPolicy.getCustomerEmail());

        return mapToResponse(savedPolicy);
    }

    @Override
    public PolicyResponse getPolicyById(Long policyId, String userEmailHeader, String userRoleHeader) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

        validatePolicyAccess(policy, userEmailHeader, userRoleHeader);
        refreshPolicyStatus(policy);
        return mapToResponse(policy);
    }

    @Override
    public List<PolicyResponse> getPoliciesForCustomer(String userEmailHeader, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_CUSTOMER);
        String customerEmail = requireEmail(userEmailHeader);
        return policyRepository.findAllByCustomerEmailIgnoreCase(customerEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PolicyResponse createPolicyForAdmin(PurchasePolicyRequest request, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        validateDates(request);

        String customerEmail = resolveCustomerEmail(request, request.getCustomerEmail());
        validateUserExists(customerEmail);

        Policy policy = new Policy();
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setCustomerEmail(customerEmail);
        policy.setPolicyType(request.getPolicyType().trim());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
        policy.setStatus(calculateStatus(request.getStartDate(), request.getEndDate()));

        Policy savedPolicy = policyRepository.save(policy);
        meterRegistry.counter("policies.created", "service", "policy-service").increment();
        policyEventPublisher.publishPolicyCreated(PolicyCreatedEvent.from(savedPolicy));
        log.info("Admin created policy. policyNumber={}, customerEmail={}", savedPolicy.getPolicyNumber(), customerEmail);
        return mapToResponse(savedPolicy);
    }

    @Override
    public PolicyResponse updatePolicyForAdmin(Long policyId, PurchasePolicyRequest request, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        validateDates(request);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

        String customerEmail = resolveCustomerEmail(request, request.getCustomerEmail());
        validateUserExists(customerEmail);

        policy.setCustomerEmail(customerEmail);
        policy.setPolicyType(request.getPolicyType().trim());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
        policy.setStatus(calculateStatus(request.getStartDate(), request.getEndDate()));

        Policy updatedPolicy = policyRepository.save(policy);
        log.info("Admin updated policy. policyNumber={}, customerEmail={}", updatedPolicy.getPolicyNumber(), customerEmail);
        return mapToResponse(updatedPolicy);
    }

    @Override
    public void deletePolicyForAdmin(Long policyId, String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));
        policyRepository.delete(policy);
        log.info("Admin deleted policy. policyNumber={}", policy.getPolicyNumber());
    }

    @Override
    public PolicySummaryResponse getPolicySummary(String userRoleHeader) {
        requireRole(userRoleHeader, ROLE_ADMIN);
        long totalPolicies = policyRepository.count();
        long activePolicies = policyRepository.countByStatus(PolicyStatus.ACTIVE);
        long expiredPolicies = policyRepository.countByStatus(PolicyStatus.EXPIRED);

        return PolicySummaryResponse.builder()
                .totalPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .expiredPolicies(expiredPolicies)
                .build();
    }

    private void validateDates(PurchasePolicyRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Policy end date must be after start date");
        }
    }

    private String resolveCustomerEmail(PurchasePolicyRequest request, String userEmailHeader) {
        String headerEmail = requireEmail(userEmailHeader);

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            String requestEmail = request.getCustomerEmail().trim().toLowerCase(Locale.ROOT);
            if (!requestEmail.equalsIgnoreCase(headerEmail)) {
                throw new ForbiddenException("Customer email mismatch with authenticated user");
            }
        }

        return headerEmail.trim().toLowerCase(Locale.ROOT);
    }

    private void validateUserExists(String email) {
        try {
            Boolean exists = authClient.validateUser(email);
            if (!Boolean.TRUE.equals(exists)) {
                log.warn("User validation failed. customerEmail={}", email);
                throw new UserNotFoundException("User not registered. Cannot purchase policy");
            }
        } catch (FeignException.NotFound exception) {
            log.warn("User validation not found. customerEmail={}", email);
            throw new UserNotFoundException("User not registered. Cannot purchase policy");
        } catch (FeignException exception) {
            int status = exception.status();
            if (status == 401 || status == 403 || status == 404) {
                log.warn("User validation rejected. customerEmail={}, status={}", email, status);
                throw new UserNotFoundException("User not registered. Cannot purchase policy");
            }

            log.error("Auth service validation failed for customerEmail={}", email, exception);
            throw new BadRequestException("Unable to validate user at the moment");
        }
    }

    private String generatePolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private PolicyStatus calculateStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return PolicyStatus.CREATED;
        }

        if (today.isAfter(endDate)) {
            return PolicyStatus.EXPIRED;
        }

        return PolicyStatus.ACTIVE;
    }

    private void validatePolicyAccess(Policy policy, String userEmailHeader, String userRoleHeader) {
        if (ROLE_ADMIN.equalsIgnoreCase(userRoleHeader)) {
            return;
        }

        requireRole(userRoleHeader, ROLE_CUSTOMER);
        String email = requireEmail(userEmailHeader);
        if (!policy.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("Access denied for policy: " + policy.getPolicyNumber());
        }
    }

    private String requireEmail(String userEmailHeader) {
        if (userEmailHeader == null || userEmailHeader.isBlank()) {
            throw new UnauthorizedException("Missing user email header");
        }
        return userEmailHeader.trim().toLowerCase(Locale.ROOT);
    }

    private void requireRole(String userRoleHeader, String requiredRole) {
        if (userRoleHeader == null || userRoleHeader.isBlank()) {
            throw new UnauthorizedException("Missing user role header");
        }
        if (!requiredRole.equalsIgnoreCase(userRoleHeader)) {
            throw new ForbiddenException("Access denied for role: " + userRoleHeader);
        }
    }

    private void refreshPolicyStatus(Policy policy) {
        PolicyStatus recalculatedStatus = calculateStatus(policy.getStartDate(), policy.getEndDate());
        if (policy.getStatus() != recalculatedStatus) {
            policy.setStatus(recalculatedStatus);
            policyRepository.save(policy);
            log.info("Policy status updated. policyNumber={}, newStatus={}", policy.getPolicyNumber(), recalculatedStatus);
        }
    }

    private PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerEmail(policy.getCustomerEmail())
                .policyType(policy.getPolicyType())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
