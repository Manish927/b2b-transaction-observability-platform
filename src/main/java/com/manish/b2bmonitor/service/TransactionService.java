package com.manish.b2bmonitor.service;

import com.manish.b2bmonitor.api.IngestTransactionRequest;
import com.manish.b2bmonitor.api.TransactionStats;
import com.manish.b2bmonitor.as2.As2MessageConflictException;
import com.manish.b2bmonitor.as2.InvalidAs2RequestException;
import com.manish.b2bmonitor.domain.ParsedEnvelope;
import com.manish.b2bmonitor.domain.TransactionFormat;
import com.manish.b2bmonitor.domain.TransactionRecord;
import com.manish.b2bmonitor.domain.TransactionStatus;
import com.manish.b2bmonitor.parser.EdiFormatDetector;
import com.manish.b2bmonitor.parser.EdiParser;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {
    private final EdiFormatDetector detector;
    private final Map<TransactionFormat, EdiParser> parsers = new EnumMap<>(TransactionFormat.class);
    private final Map<String, TransactionRecord> store = new ConcurrentHashMap<>();
    private final Map<String, IdempotencyEntry> as2MessageIndex = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public TransactionService(EdiFormatDetector detector, List<EdiParser> parserList, MeterRegistry meterRegistry) {
        this.detector = detector;
        this.meterRegistry = meterRegistry;
        parserList.forEach(parser -> parsers.put(parser.format(), parser));
    }

    public TransactionRecord ingest(IngestTransactionRequest request) {
        return ingest(request.tradingPartnerId(), null, request.payload());
    }

    public synchronized IngestResult ingestAs2(String tradingPartnerId, String messageId, String payload) {
        String normalizedMessageId = normalizeMessageId(messageId);
        String idempotencyKey = tradingPartnerId + "\u0000" + normalizedMessageId;
        String payloadDigest = sha256(payload);
        IdempotencyEntry existing = as2MessageIndex.get(idempotencyKey);
        if (existing != null) {
            if (!existing.payloadDigest().equals(payloadDigest)) {
                throw new As2MessageConflictException(
                        "Message-ID was already used with different content");
            }
            meterRegistry.counter("b2b.as2.duplicates", "partner", tradingPartnerId).increment();
            return IngestResult.duplicate(store.get(existing.transactionId()));
        }

        TransactionRecord record = ingest(tradingPartnerId, normalizedMessageId, payload);
        as2MessageIndex.put(idempotencyKey, new IdempotencyEntry(record.transactionId(), payloadDigest));
        return IngestResult.created(record);
    }

    private TransactionRecord ingest(String tradingPartnerId, String as2MessageId, String payload) {
        TransactionFormat format = detector.detect(payload);
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
            ParsedEnvelope parsed = parsers.get(format).parse(payload);
            errors.addAll(parsed.validationErrors());
            messageType = parsed.messageType();
            controlNumber = parsed.interchangeControlNumber();
            status = parsed.valid() ? TransactionStatus.VALIDATED : TransactionStatus.REJECTED;
        }

        TransactionRecord record = new TransactionRecord(
                id,
                tradingPartnerId,
                as2MessageId,
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

    private String normalizeMessageId(String messageId) {
        String normalized = messageId.trim();
        if (normalized.length() >= 2 && normalized.startsWith("<") && normalized.endsWith(">")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        boolean invalidCharacter = normalized.chars().anyMatch(c -> c < 33 || c > 126);
        if (normalized.isBlank() || normalized.length() > 998 || invalidCharacter) {
            throw new InvalidAs2RequestException("Invalid AS2 Message-ID");
        }
        return normalized;
    }

    private String sha256(String payload) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private record IdempotencyEntry(String transactionId, String payloadDigest) {
    }

    public TransactionRecord get(String id) {
        TransactionRecord record = store.get(id);
        if (record == null) {
            throw new TransactionNotFoundException("Transaction not found: " + id);
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
