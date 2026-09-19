package com.hackathon.payment.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByOrderIdOrderByCreatedAtDesc(String orderId);

    List<Transaction> findByOrderIdContainingIgnoreCaseOrCustomerReferenceContainingIgnoreCase(
            String orderId, String customerReference);
}
