package com.manish.b2bmonitor.api;

import com.manish.b2bmonitor.domain.TransactionRecord;

public record As2IngestResponse(
        boolean duplicate,
        TransactionRecord transaction
) {
}
