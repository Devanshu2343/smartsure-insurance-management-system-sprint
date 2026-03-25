package com.smartsure.policyservice.controller;


import com.smartsure.policyservice.entity.Policy;
import com.smartsure.policyservice.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;


    @PostMapping
    public ResponseEntity<?> createPolicy(
            @RequestBody Policy policy,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Only ADMIN can create policy");
        }

        return ResponseEntity.ok(policyService.createPolicy(policy, email));
    }


    @GetMapping
    public ResponseEntity<?> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicy(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Only ADMIN can delete");
        }

        policyService.deletePolicy(id);
        return ResponseEntity.ok("Deleted Successfully");
    }
}