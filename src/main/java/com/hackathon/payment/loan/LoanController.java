package com.hackathon.payment.loan;

import com.hackathon.payment.loan.dto.CreateLoanRequest;
import com.hackathon.payment.loan.dto.LoanResponse;
import com.hackathon.payment.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Apply for a loan")
    public ResponseEntity<LoanResponse> apply(
            @RequestBody CreateLoanRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.apply(request, user));
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Get a loan application")
    public LoanResponse getLoan(@PathVariable UUID loanId,
                                @AuthenticationPrincipal AuthenticatedUser user) {
        return loanService.getLoan(loanId, user);
    }

    @GetMapping
    @Operation(summary = "List loan applications")
    public List<LoanResponse> listLoans(@AuthenticationPrincipal AuthenticatedUser user) {
        return loanService.listLoans(user);
    }
}
