package com.hackathon.payment.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, UUID> {

    List<ScheduledPayment> findByCreatedByOrderByNextRunDateAsc(UUID createdBy);
}
