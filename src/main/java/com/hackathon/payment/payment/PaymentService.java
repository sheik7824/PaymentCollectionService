package com.hackathon.payment.payment;

import com.hackathon.payment.audit.AuditService;
import com.hackathon.payment.common.exception.ResourceNotFoundException;
import com.hackathon.payment.gateway.GatewayChargeRequest;
import com.hackathon.payment.gateway.GatewayChargeResponse;
import com.hackathon.payment.gateway.PaymentGateway;
import com.hackathon.payment.gateway.PaymentGatewayException;
import com.hackathon.payment.payment.dto.CreatePaymentRequest;
import com.hackathon.payment.payment.dto.PaymentResponse;
import com.hackathon.payment.payment.idempotency.IdempotencyService;
import com.hackathon.payment.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final TransactionPersistenceService persistence;
    private final IdempotencyService idempotencyService;
    private final PaymentGateway paymentGateway;
    private final RetryTemplate gatewayRetryTemplate;
    private final AuditService auditService;

    /**
     * Flow: idempotency check -> create PROCESSING txn -> gateway (with retry) -> persist outcome.
     */
    public PaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey,
                                         AuthenticatedUser user) {
        String requestHash = IdempotencyService.hash(request.canonical());

        if (idempotencyKey != null) {
            Optional<String> existingTxn = idempotencyService.findExisting(idempotencyKey, requestHash);
            if (existingTxn.isPresent()) {
                log.info("Idempotent replay for key={} txn={}", idempotencyKey, existingTxn.get());
                return getByTransactionId(existingTxn.get(), user);
            }
        }

        Transaction txn = persistence.createProcessing(request, user.id(), paymentGateway.getName());
        if (idempotencyKey != null) {
            idempotencyService.store(idempotencyKey, requestHash, txn.getTransactionId());
        }

        GatewayChargeRequest chargeRequest = new GatewayChargeRequest(
                txn.getTransactionId(), request.orderId(), request.amount(), request.currency(),
                request.customerReference());

        try {
            GatewayChargeResponse gatewayResponse =
                    gatewayRetryTemplate.execute(ctx -> paymentGateway.charge(chargeRequest));
            txn = persistence.applyGatewayResult(txn.getId(), gatewayResponse);
        } catch (PaymentGatewayException ex) {
            log.warn("Gateway failed after retries for txn={}: {}", txn.getTransactionId(), ex.getMessage());
            txn = persistence.markFailed(txn.getId(), "Gateway error: " + ex.getMessage());
        }

        auditService.record(user.id(), "PAYMENT_CREATE", "TRANSACTION", txn.getTransactionId(),
                txn.getStatus().name(), txn.getFailureReason());
        log.info("Payment txn={} order={} status={}", txn.getTransactionId(), txn.getOrderId(), txn.getStatus());
        return PaymentResponse.from(txn);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByTransactionId(String transactionId, AuthenticatedUser user) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        if (!canView(txn, user)) {
            throw new AccessDeniedException("Not permitted to view this transaction");
        }
        return PaymentResponse.from(txn);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByOrderId(String orderId, AuthenticatedUser user) {
        List<PaymentResponse> visible = transactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .filter(t -> canView(t, user))
                .map(PaymentResponse::from)
                .toList();
        if (visible.isEmpty()) {
            throw new ResourceNotFoundException("No transactions found for order: " + orderId);
        }
        return visible;
    }

    private static boolean canView(Transaction txn, AuthenticatedUser user) {
        return user.isAdmin() || txn.getCreatedBy().equals(user.id());
    }
}
