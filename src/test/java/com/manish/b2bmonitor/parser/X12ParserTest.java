package com.manish.b2bmonitor.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class X12ParserTest {
    private final X12Parser parser = new X12Parser();

    @Test
    void extractsMessageTypeAndControlNumber() {
        String payload = "ISA*00*          *00*          *12*SENDER         *12*RECEIVER       *240101*1200*U*00401*000000905*0*P*>~" +
                "GS*PO*SENDER*RECEIVER*20240101*1200*1*X*004010~" +
                "ST*850*0001~SE*2*0001~GE*1*1~IEA*1*000000905~";

        var parsed = parser.parse(payload);
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.messageType()).isEqualTo("850");
        assertThat(parsed.interchangeControlNumber()).isEqualTo("000000905");
    }
}
