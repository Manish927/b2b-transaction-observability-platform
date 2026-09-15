package com.manish.b2bmonitor.api;

import jakarta.validation.constraints.NotBlank;

public record IngestTransactionRequest(
        @NotBlank String tradingPartnerId,
        @NotBlank String payload
) {
}
