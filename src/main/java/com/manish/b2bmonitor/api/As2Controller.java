package com.manish.b2bmonitor.api;

import com.manish.b2bmonitor.as2.InvalidAs2RequestException;
import com.manish.b2bmonitor.as2.TradingPartner;
import com.manish.b2bmonitor.as2.TradingPartnerRegistry;
import com.manish.b2bmonitor.service.IngestResult;
import com.manish.b2bmonitor.service.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/as2")
public class As2Controller {
    private final TradingPartnerRegistry partnerRegistry;
    private final TransactionService transactionService;

    public As2Controller(TradingPartnerRegistry partnerRegistry, TransactionService transactionService) {
        this.partnerRegistry = partnerRegistry;
        this.transactionService = transactionService;
    }

    @PostMapping(
            path = "/messages",
            consumes = {"application/EDI-X12", "application/EDIFACT"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<As2IngestResponse> receive(
            @RequestHeader(name = "AS2-From", required = false) String as2From,
            @RequestHeader(name = "AS2-To", required = false) String as2To,
            @RequestHeader(name = "Message-ID", required = false) String messageId,
            @RequestBody String payload) {
        if (messageId == null || messageId.isBlank()) {
            throw new InvalidAs2RequestException("Missing required Message-ID header");
        }

        TradingPartner partner = partnerRegistry.resolveInbound(as2From, as2To);
        IngestResult result = transactionService.ingestAs2(partner.id(), messageId, payload);

        return ResponseEntity.ok(new As2IngestResponse(result.duplicate(), result.transaction()));
    }
}
