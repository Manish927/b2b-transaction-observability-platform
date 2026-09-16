package com.manish.b2bmonitor.service;

import com.manish.b2bmonitor.domain.TransactionRecord;

public record IngestResult(
        TransactionRecord transaction,
        boolean duplicate
) {
    public static IngestResult created(TransactionRecord transaction) {
        return new IngestResult(transaction, false);
    }

    public static IngestResult duplicate(TransactionRecord transaction) {
        return new IngestResult(transaction, true);
    }
}
