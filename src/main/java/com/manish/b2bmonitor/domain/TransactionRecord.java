package com.manish.b2bmonitor.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record TransactionRecord(
        String transactionId,
        String tradingPartnerId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String as2MessageId,
        TransactionFormat format,
        String messageType,
        String interchangeControlNumber,
        TransactionStatus status,
        Instant receivedAt,
        Instant updatedAt,
        List<String> validationErrors
) {
}
