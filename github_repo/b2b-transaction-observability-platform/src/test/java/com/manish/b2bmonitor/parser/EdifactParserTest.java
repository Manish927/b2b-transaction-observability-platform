package com.manish.b2bmonitor.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EdifactParserTest {
    private final EdifactParser parser = new EdifactParser();

    @Test
    void extractsMessageTypeAndControlNumber() {
        String payload = "UNB+UNOC:3+SENDER+RECEIVER+240101:1200+CTRL001'" +
                "UNH+MSG001+ORDERS:D:96A:UN'UNT+2+MSG001'UNZ+1+CTRL001'";

        var parsed = parser.parse(payload);
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.messageType()).isEqualTo("ORDERS");
        assertThat(parsed.interchangeControlNumber()).isEqualTo("CTRL001");
    }
}
