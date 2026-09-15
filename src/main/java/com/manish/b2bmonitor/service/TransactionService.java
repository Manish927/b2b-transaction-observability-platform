package com.manish.b2bmonitor.service;

import com.manish.b2bmonitor.api.IngestTransactionRequest;
import com.manish.b2bmonitor.api.TransactionStats;
import com.manish.b2bmonitor.domain.ParsedEnvelope;
import com.manish.b2bmonitor.domain.TransactionFormat;
import com.manish.b2bmonitor.domain.TransactionRecord;
import com.manish.b2bmonitor.domain.TransactionStatus;
import com.manish.b2bmonitor.parser.EdiFormatDetector;
import com.manish.b2bmonitor.parser.EdiParser;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {
    private final EdiFormatDetector detector;
    private final Map<TransactionFormat, EdiParser> parsers = new EnumMap<>(TransactionFormat.class);
    private final Map<String, TransactionRecord> store = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public TransactionService(EdiFormatDetector detector, List<EdiParser> parserList, MeterRegistry meterRegistry) {
        this.detector = detector;
        this.meterRegistry = meterRegistry;
        parserList.forEach(parser -> parsers.put(parser.format(), parser));
    }

    public TransactionRecord ingest(IngestTransactionRequest request) {
        TransactionFormat format = detector.detect(request.payload());
        Instant now = Instant.now();
        String id = UUID.randomUUID().toString();

        List<String> errors = new ArrayList<>();
        String messageType = null;
        String controlNumber = null;
        TransactionStatus status;

        if (format == TransactionFormat.UNKNOWN) {
            errors.add("Unsupported EDI format. Expected X12 or EDIFACT envelope.");
            status = TransactionStatus.REJECTED;
        } else {
            ParsedEnvelope parsed = parsers.get(format).parse(request.payload());
            errors.addAll(parsed.validationErrors());
            messageType = parsed.messageType();
            controlNumber = parsed.interchangeControlNumber();
            status = parsed.valid() ? TransactionStatus.VALIDATED : TransactionStatus.REJECTED;
        }

        TransactionRecord record = new TransactionRecord(
                id,
                request.tradingPartnerId(),
                format,
                messageType,
                controlNumber,
                status,
                now,
                now,
                List.copyOf(errors)
        );

        store.put(id, record);
        meterRegistry.counter("b2b.transactions.received", "format", format.name()).increment();
        meterRegistry.counter("b2b.transactions.status", "status", status.name()).increment();
        return record;
    }

    public TransactionRecord get(String id) {
        TransactionRecord record = store.get(id);
        if (record == null) {
            throw new IllegalArgumentException("Transaction not found: " + id);
        }
        return record;
    }

    public List<TransactionRecord> list() {
        return store.values().stream()
                .sorted(Comparator.comparing(TransactionRecord::receivedAt).reversed())
                .toList();
    }

    public TransactionStats stats() {
        Map<TransactionStatus, Long> byStatus = new EnumMap<>(TransactionStatus.class);
        for (TransactionStatus status : TransactionStatus.values()) {
            byStatus.put(status, store.values().stream().filter(t -> t.status() == status).count());
        }
        return new TransactionStats(store.size(), byStatus);
    }
}
