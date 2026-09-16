package com.manish.b2bmonitor.service;

import com.manish.b2bmonitor.as2.As2MessageConflictException;
import com.manish.b2bmonitor.parser.EdiFormatDetector;
import com.manish.b2bmonitor.parser.EdifactParser;
import com.manish.b2bmonitor.parser.X12Parser;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionServiceTest {
    private static final String X12 = "ISA*00*          *00*          *12*SENDER         *12*RECEIVER       "
            + "*240101*1200*U*00401*000000905*0*P*>~ST*850*0001~";

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(
                new EdiFormatDetector(),
                List.of(new X12Parser(), new EdifactParser()),
                new SimpleMeterRegistry());
    }

    @Test
    void returnsOriginalTransactionForIdenticalAs2Retry() {
        IngestResult first = service.ingestAs2("partner-a", "<message-1@example.test>", X12);
        IngestResult retry = service.ingestAs2("partner-a", "message-1@example.test", X12);

        assertThat(first.duplicate()).isFalse();
        assertThat(retry.duplicate()).isTrue();
        assertThat(retry.transaction().transactionId()).isEqualTo(first.transaction().transactionId());
        assertThat(service.list()).hasSize(1);
    }

    @Test
    void rejectsMessageIdReusedForDifferentContent() {
        service.ingestAs2("partner-a", "<message-1@example.test>", X12);

        assertThatThrownBy(() -> service.ingestAs2(
                "partner-a", "<message-1@example.test>", X12 + "SE*2*0001~"))
                .isInstanceOf(As2MessageConflictException.class);
    }

    @Test
    void scopesMessageIdByTradingPartner() {
        IngestResult first = service.ingestAs2("partner-a", "<shared@example.test>", X12);
        IngestResult second = service.ingestAs2("partner-b", "<shared@example.test>", X12);

        assertThat(second.duplicate()).isFalse();
        assertThat(second.transaction().transactionId()).isNotEqualTo(first.transaction().transactionId());
    }
}
