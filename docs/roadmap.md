# Roadmap

## Phase 1 - Working EDI ingestion (included)
- Java 21 / Spring Boot API
- X12 and EDIFACT format detection
- basic envelope parsing and validation
- canonical transaction record
- lifecycle status
- metrics / Prometheus endpoint
- unit tests and sample EDI messages

## Phase 2A - AS2 ingestion foundation (included)
- AS2-style HTTP endpoint
- configured trading-partner validation
- Message-ID correlation and duplicate protection
- X12 and EDIFACT media types

## Phase 2B - Production AS2 protocol
- S/MIME signing + encryption
- MDN generation / correlation
- trading-partner certificates
- non-repudiation audit evidence

## Phase 3 - Transaction platform
- Kafka-backed event flow
- PostgreSQL transaction/audit store
- Redis deduplication cache
- configurable routing rules
- retry policy + dead-letter queue
- SLA/SLO breach detection

## Phase 4 - Operations & observability
- OpenTelemetry traces
- Grafana dashboards
- partner/message-type health scorecards
- latency histograms and failure heatmaps
- anomaly detection over failure/latency patterns

## Phase 5 - AI-assisted operations
- RCA copilot grounded on transaction history, logs, traces and runbooks
- incident clustering and probable-cause ranking
- intelligent triage / Tier-1 resolution suggestions
- human approval before retry/replay/remediation
- evaluation set for RCA quality and hallucination control
