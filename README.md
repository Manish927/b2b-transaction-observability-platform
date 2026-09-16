# B2B Transaction Observability Platform

A GitHub portfolio project for learning and demonstrating **EDI transaction management, X12/EDIFACT processing, AS2 architecture, B2B integration, observability, reliability engineering, and AI-assisted operations**.

The project deliberately starts as a small runnable Spring Boot service and evolves toward a production-style B2B transaction platform.

## Why this project

Enterprise B2B platforms need more than message parsing. They need:

- partner-aware transport and security
- EDI translation and validation
- end-to-end transaction correlation
- idempotency and replay protection
- routing and retries
- SLA/SLO monitoring
- auditability
- fast root-cause analysis

This repository is designed to demonstrate those concepts incrementally.

## Current capabilities

- Java 21 + Spring Boot
- REST ingestion endpoint
- AS2-style HTTP ingestion with partner validation and Message-ID idempotency
- X12 vs EDIFACT auto-detection
- basic X12 envelope parsing (`ISA`, `ST`)
- basic EDIFACT envelope parsing (`UNB`, `UNH`)
- canonical transaction metadata
- statuses: `RECEIVED`, `VALIDATED`, `REJECTED`, `ROUTED`, `DELIVERED`, `FAILED`
- in-memory transaction repository for a zero-dependency demo
- Spring Boot Actuator + Prometheus metrics
- unit tests
- sample X12 850 and EDIFACT ORDERS messages

## Architecture

See [`docs/architecture.md`](docs/architecture.md).

```text
Partner -> AS2/HTTPS -> Ingestion -> X12/EDIFACT parser -> Canonical Transaction
                                                     -> validation / routing
                                                     -> monitoring / metrics
                                                     -> later Kafka + audit store + AI RCA
```

## Run

```bash
mvn spring-boot:run
```

Health:

```bash
curl http://localhost:8080/actuator/health
```

Prometheus metrics:

```bash
curl http://localhost:8080/actuator/prometheus
```

## Ingest an X12 transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{
    "tradingPartnerId":"partner-a",
    "payload":"ISA*00*          *00*          *12*SENDER         *12*RECEIVER       *240101*1200*U*00401*000000905*0*P*>~GS*PO*SENDER*RECEIVER*20240101*1200*1*X*004010~ST*850*0001~SE*2*0001~GE*1*1~IEA*1*000000905~"
  }'
```

Expected result includes:

```json
{
  "format": "X12",
  "messageType": "850",
  "interchangeControlNumber": "000000905",
  "status": "VALIDATED"
}
```

## Ingest an EDIFACT transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{
    "tradingPartnerId":"partner-b",
    "payload":"UNB+UNOC:3+SENDER+RECEIVER+240101:1200+CTRL001\u0027UNH+MSG001+ORDERS:D:96A:UN\u0027UNT+2+MSG001\u0027UNZ+1+CTRL001\u0027"
  }'
```

## Query transactions

```bash
curl http://localhost:8080/api/v1/transactions
curl http://localhost:8080/api/v1/transactions/stats
```

## Ingest through the AS2 channel

The first AS2 milestone validates partner headers, correlates `Message-ID`, prevents
duplicate processing, and forwards raw X12 or EDIFACT payloads into the existing
transaction pipeline:

```bash
curl -X POST http://localhost:8080/api/v1/as2/messages \
  -H 'Content-Type: application/EDI-X12' \
  -H 'AS2-From: PARTNER-A' \
  -H 'AS2-To: MANISH-B2B' \
  -H 'Message-ID: <order-0001@partner-a>' \
  --data-binary 'ISA*00*          *00*          *12*SENDER         *12*RECEIVER       *240101*1200*U*00401*000000905*0*P*>~ST*850*0001~'
```

Repeating the same partner, `Message-ID`, and payload returns the original transaction
with `"duplicate": true`. Reusing that ID with different content returns HTTP `409`.

This milestone is deliberately **AS2-style ingestion**, not yet a complete AS2 protocol
implementation. S/MIME signatures, encryption, MIC calculation, certificates and MDNs
remain Phase 2 work.

## Portfolio talking points

This project provides concrete material to discuss:

- what AS2 adds beyond plain HTTPS
- why MDNs matter for B2B non-repudiation and delivery confirmation
- X12 vs EDIFACT envelope structure
- canonical data models
- partner-specific routing
- deduplication/idempotency
- retries and DLQs
- transaction correlation
- SLA/SLO monitoring
- anomaly detection
- AI-assisted RCA and intelligent operations

## Next milestones

See [`docs/roadmap.md`](docs/roadmap.md).

The next implementation milestone is **S/MIME security + certificates + Message-ID/MDN correlation**.
