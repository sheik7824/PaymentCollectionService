package com.hackathon.payment.payment;

import com.hackathon.payment.payment.dto.CreatePaymentRequest;
import com.hackathon.payment.payment.dto.PaymentResponse;
import com.hackathon.payment.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Collect a payment for an order")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @Parameter(description = "Client-supplied key; replays return the original result")
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @AuthenticationPrincipal AuthenticatedUser user) {
        PaymentResponse response = paymentService.createPayment(request, idempotencyKey, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a transaction by its transaction id")
    public PaymentResponse getByTransactionId(@PathVariable String transactionId,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return paymentService.getByTransactionId(transactionId, user);
    }

    @GetMapping("/search")
    @Operation(summary = "Search payments by order or customer reference")
    public List<PaymentResponse> searchPayments(@RequestParam String query,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        return paymentService.searchPayments(query, user);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "List transactions for an order id")
    public List<PaymentResponse> getByOrderId(@PathVariable String orderId,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return paymentService.getByOrderId(orderId, user);
    }
}
