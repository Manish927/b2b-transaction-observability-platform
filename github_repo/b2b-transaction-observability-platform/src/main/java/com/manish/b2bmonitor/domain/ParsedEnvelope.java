package com.manish.b2bmonitor.domain;

import java.util.List;

public record ParsedEnvelope(
        TransactionFormat format,
        String messageType,
        String interchangeControlNumber,
        List<String> validationErrors
) {
    public boolean valid() {
        return validationErrors == null || validationErrors.isEmpty();
    }
}
