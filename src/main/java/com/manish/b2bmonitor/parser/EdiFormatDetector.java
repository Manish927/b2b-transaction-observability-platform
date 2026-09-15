package com.manish.b2bmonitor.parser;

import com.manish.b2bmonitor.domain.TransactionFormat;
import org.springframework.stereotype.Component;

@Component
public class EdiFormatDetector {
    public TransactionFormat detect(String payload) {
        if (payload == null || payload.isBlank()) {
            return TransactionFormat.UNKNOWN;
        }
        String normalized = payload.stripLeading();
        if (normalized.startsWith("ISA")) {
            return TransactionFormat.X12;
        }
        if (normalized.startsWith("UNB") || normalized.startsWith("UNA") || normalized.contains("UNB+")) {
            return TransactionFormat.EDIFACT;
        }
        return TransactionFormat.UNKNOWN;
    }
}
