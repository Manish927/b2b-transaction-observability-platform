package com.manish.b2bmonitor.parser;

import com.manish.b2bmonitor.domain.TransactionFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EdiFormatDetectorTest {
    private final EdiFormatDetector detector = new EdiFormatDetector();

    @Test
    void detectsX12() {
        assertThat(detector.detect("ISA*00*...~ST*850*0001~")).isEqualTo(TransactionFormat.X12);
    }

    @Test
    void detectsEdifact() {
        assertThat(detector.detect("UNB+UNOC:3+SENDER+RECEIVER+240101:1200+1'UNH+1+ORDERS:D:96A:UN'"))
                .isEqualTo(TransactionFormat.EDIFACT);
    }
}
