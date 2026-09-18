package com.hackathon.payment.payment;

import com.hackathon.payment.gateway.GatewayChargeResponse;
import com.hackathon.payment.payment.dto.CreatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Each state change commits in its own short transaction so that the
 * (potentially slow) gateway call never holds a DB connection.
 */
@Service
@RequiredArgsConstructor
public class TransactionPersistenceService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createProcessing(CreatePaymentRequest request, UUID createdBy, String gatewayName) {
        Transaction txn = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionId(generateTransactionId())
                .orderId(request.orderId())
                .amount(request.amount())
                .currency(request.currency())
                .customerReference(request.customerReference())
                .status(TransactionStatus.PROCESSING)
                .gatewayName(gatewayName)
                .createdBy(createdBy)
                .build();
        return transactionRepository.save(txn);
    }

    @Transactional
    public Transaction applyGatewayResult(UUID id, GatewayChargeResponse response) {
        Transaction txn = transactionRepository.findById(id).orElseThrow();
        txn.setGatewayReference(response.gatewayReference());
        txn.setStatus(switch (response.status()) {
            case SUCCESS -> TransactionStatus.SUCCESS;
            case PENDING -> TransactionStatus.PENDING;
            case FAILED -> TransactionStatus.FAILED;
        });
        txn.setFailureReason(response.failureReason());
        return transactionRepository.save(txn);
    }

    @Transactional
    public Transaction markFailed(UUID id, String reason) {
        Transaction txn = transactionRepository.findById(id).orElseThrow();
        txn.setStatus(TransactionStatus.FAILED);
        txn.setFailureReason(reason);
        return transactionRepository.save(txn);
    }

    private static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
