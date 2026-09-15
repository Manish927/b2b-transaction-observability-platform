package com.manish.b2bmonitor.domain;

import java.time.Instant;
import java.util.List;

public record TransactionRecord(
        String transactionId,
        String tradingPartnerId,
        TransactionFormat format,
        String messageType,
        String interchangeControlNumber,
        TransactionStatus status,
        Instant receivedAt,
        Instant updatedAt,
        List<String> validationErrors
) {
}
