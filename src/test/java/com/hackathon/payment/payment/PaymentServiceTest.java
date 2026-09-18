package com.hackathon.payment.payment;

import com.hackathon.payment.audit.AuditService;
import com.hackathon.payment.common.exception.IdempotencyConflictException;
import com.hackathon.payment.common.exception.ResourceNotFoundException;
import com.hackathon.payment.gateway.GatewayChargeRequest;
import com.hackathon.payment.gateway.GatewayChargeResponse;
import com.hackathon.payment.gateway.PaymentGateway;
import com.hackathon.payment.gateway.PaymentGatewayException;
import com.hackathon.payment.payment.dto.CreatePaymentRequest;
import com.hackathon.payment.payment.dto.PaymentResponse;
import com.hackathon.payment.payment.idempotency.IdempotencyService;
import com.hackathon.payment.security.AuthenticatedUser;
import com.hackathon.payment.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock IdempotencyService idempotencyService;
    @Mock PaymentGateway paymentGateway;
    @Mock AuditService auditService;

    private PaymentService service;
    private final AuthenticatedUser consumer =
            new AuthenticatedUser(UUID.randomUUID(), "consumer", UserRole.API_CONSUMER);
    private final AuthenticatedUser admin =
            new AuthenticatedUser(UUID.randomUUID(), "admin", UserRole.ADMIN);
    private final CreatePaymentRequest request =
            new CreatePaymentRequest("ORD-1", new BigDecimal("1500.00"), "INR", "CUST-1");

    private Transaction saved;

    @BeforeEach
    void setUp() {
        RetryTemplate retry = RetryTemplate.builder()
                .maxAttempts(3).fixedBackoff(1).retryOn(PaymentGatewayException.class).build();
        TransactionPersistenceService persistence = new TransactionPersistenceService(transactionRepository);
        service = new PaymentService(transactionRepository, persistence, idempotencyService, paymentGateway,
                retry, auditService);
    }

    /** Simulates the repository by echoing saves and serving the last saved entity on findById. */
    private void captureSaves() {
        when(paymentGateway.getName()).thenReturn("MOCK");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            saved = inv.getArgument(0);
            return saved;
        });
        when(transactionRepository.findById(any(UUID.class))).thenAnswer(inv -> Optional.of(saved));
    }

    @Test
    void successfulGatewayChargeMarksTransactionSuccess() {
        captureSaves();
        when(paymentGateway.charge(any())).thenReturn(GatewayChargeResponse.success("PG-1"));

        PaymentResponse response = service.createPayment(request, null, consumer);

        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.gatewayReference()).isEqualTo("PG-1");
        assertThat(response.transactionId()).startsWith("TXN-");
        verify(idempotencyService, never()).store(anyString(), anyString(), anyString());
        verify(auditService).record(eq(consumer.id()), eq("PAYMENT_CREATE"), eq("TRANSACTION"),
                anyString(), eq("SUCCESS"), any());
    }

    @Test
    void declinedGatewayChargeMarksTransactionFailedWithReason() {
        captureSaves();
        when(paymentGateway.charge(any())).thenReturn(GatewayChargeResponse.failed("PG-2", "Insufficient funds"));

        PaymentResponse response = service.createPayment(request, null, consumer);

        assertThat(response.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Insufficient funds");
    }

    @Test
    void gatewayExceptionIsRetriedThreeTimesThenMarkedFailed() {
        captureSaves();
        when(paymentGateway.charge(any())).thenThrow(new PaymentGatewayException("timeout"));

        PaymentResponse response = service.createPayment(request, null, consumer);

        verify(paymentGateway, times(3)).charge(any(GatewayChargeRequest.class));
        assertThat(response.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.failureReason()).contains("timeout");
    }

    @Test
    void transientGatewayFailureRecoversOnRetry() {
        captureSaves();
        when(paymentGateway.charge(any()))
                .thenThrow(new PaymentGatewayException("blip"))
                .thenReturn(GatewayChargeResponse.success("PG-3"));

        PaymentResponse response = service.createPayment(request, null, consumer);

        verify(paymentGateway, times(2)).charge(any());
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    void idempotentReplayReturnsExistingTransactionWithoutChargingAgain() {
        Transaction existing = Transaction.builder()
                .id(UUID.randomUUID()).transactionId("TXN-EXISTING").orderId("ORD-1")
                .amount(request.amount()).currency("INR").status(TransactionStatus.SUCCESS)
                .createdBy(consumer.id()).build();
        when(idempotencyService.findExisting(eq("key-1"), anyString())).thenReturn(Optional.of("TXN-EXISTING"));
        when(transactionRepository.findByTransactionId("TXN-EXISTING")).thenReturn(Optional.of(existing));

        PaymentResponse response = service.createPayment(request, "key-1", consumer);

        assertThat(response.transactionId()).isEqualTo("TXN-EXISTING");
        verify(paymentGateway, never()).charge(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void newIdempotencyKeyIsStoredAgainstTransaction() {
        captureSaves();
        when(idempotencyService.findExisting(eq("key-2"), anyString())).thenReturn(Optional.empty());
        when(paymentGateway.charge(any())).thenReturn(GatewayChargeResponse.success("PG-4"));

        PaymentResponse response = service.createPayment(request, "key-2", consumer);

        verify(idempotencyService).store(eq("key-2"), anyString(), eq(response.transactionId()));
    }

    @Test
    void idempotencyConflictPropagates() {
        when(idempotencyService.findExisting(eq("key-3"), anyString()))
                .thenThrow(new IdempotencyConflictException("conflict"));

        assertThatThrownBy(() -> service.createPayment(request, "key-3", consumer))
                .isInstanceOf(IdempotencyConflictException.class);
        verify(paymentGateway, never()).charge(any());
    }

    @Test
    void consumerCannotViewAnotherUsersTransaction() {
        Transaction other = Transaction.builder()
                .id(UUID.randomUUID()).transactionId("TXN-OTHER").orderId("ORD-9")
                .amount(BigDecimal.TEN).currency("INR").status(TransactionStatus.SUCCESS)
                .createdBy(UUID.randomUUID()).build();
        when(transactionRepository.findByTransactionId("TXN-OTHER")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.getByTransactionId("TXN-OTHER", consumer))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(service.getByTransactionId("TXN-OTHER", admin).transactionId()).isEqualTo("TXN-OTHER");
    }

    @Test
    void unknownTransactionOrOrderReturnsNotFound() {
        when(transactionRepository.findByTransactionId("TXN-NOPE")).thenReturn(Optional.empty());
        when(transactionRepository.findByOrderIdOrderByCreatedAtDesc("ORD-NOPE")).thenReturn(List.of());

        assertThatThrownBy(() -> service.getByTransactionId("TXN-NOPE", admin))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getByOrderId("ORD-NOPE", admin))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
