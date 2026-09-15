# Target Production Architecture

```text
Trading Partner / Customer
        |
        | AS2 / HTTPS / SFTP
        v
+-------------------------+
| B2B Edge / AS2 Gateway  |
| - S/MIME                |
| - signing/encryption    |
| - synchronous/async MDN |
+------------+------------+
             |
             v
+-------------------------+
| Ingestion & Correlation |
| - partner identity      |
| - message id            |
| - idempotency           |
| - audit trail           |
+------------+------------+
             |
             v
+-------------------------+
| EDI Translation Layer   |
| X12 / EDIFACT           |
| -> Canonical Event      |
+------------+------------+
             |
             v
+-------------------------+
| Transaction Engine      |
| - validation            |
| - routing               |
| - retries               |
| - SLA state machine     |
+------------+------------+
             |
             v
        Kafka / DLQ
         /       \
        v         v
 Delivery      Monitoring / Analytics
 Connectors    - status & SLOs
               - anomaly detection
               - AI-assisted RCA
               - dashboards / alerts
```

## Why this architecture matters

The platform separates transport, EDI translation, canonical transaction state, routing, and observability. This allows each concern to evolve independently while preserving an end-to-end correlation identifier for operational support.

## AS2 scope for Phase 2

- HTTPS transport
- S/MIME signing and encryption
- Message-ID correlation
- MIC calculation
- synchronous and asynchronous MDNs
- certificate/trading-partner configuration
- retry and non-repudiation audit records

## Transaction state model

`RECEIVED -> VALIDATED -> ROUTED -> DELIVERED`

Failure branches move to `REJECTED` or `FAILED`. A production version will retain every state transition as an immutable event for auditability and RCA.
