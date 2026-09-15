package com.manish.b2bmonitor.api;

import com.manish.b2bmonitor.domain.TransactionStatus;

import java.util.Map;

public record TransactionStats(
        long total,
        Map<TransactionStatus, Long> byStatus
) {
}
