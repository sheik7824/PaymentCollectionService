package com.hackathon.payment.payment.idempotency;

import com.hackathon.payment.common.exception.IdempotencyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    /**
     * Returns the transaction id previously associated with this key, if any.
     *
     * @throws IdempotencyConflictException when the key exists with a different payload
     */
    @Transactional(readOnly = true)
    public Optional<String> findExisting(String key, String requestHash) {
        return repository.findByIdempotencyKey(key).map(existing -> {
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key was already used with a different request payload");
            }
            return existing.getTransactionId();
        });
    }

    /**
     * Persisted in its own transaction so the key survives even if the payment
     * flow fails afterwards; the unique constraint guards concurrent duplicates.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(String key, String requestHash, String transactionId) {
        repository.save(IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .idempotencyKey(key)
                .requestHash(requestHash)
                .transactionId(transactionId)
                .build());
    }

    public static String hash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
