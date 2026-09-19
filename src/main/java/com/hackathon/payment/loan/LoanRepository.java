package com.hackathon.payment.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByBorrowerIdOrderByCreatedAtDesc(UUID borrowerId);
}
