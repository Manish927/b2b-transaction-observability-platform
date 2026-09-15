package com.manish.b2bmonitor.parser;

import com.manish.b2bmonitor.domain.ParsedEnvelope;
import com.manish.b2bmonitor.domain.TransactionFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EdifactParser implements EdiParser {

    @Override
    public TransactionFormat format() {
        return TransactionFormat.EDIFACT;
    }

    @Override
    public ParsedEnvelope parse(String payload) {
        List<String> errors = new ArrayList<>();
        String[] segments = Arrays.stream(payload.replace("\r", "").replace("\n", "").split("'"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        String unb = findSegment(segments, "UNB");
        String unh = findSegment(segments, "UNH");

        if (unb == null) errors.add("Missing UNB interchange header");
        if (unh == null) errors.add("Missing UNH message header");

        String controlNumber = null;
        if (unb != null) {
            String[] elements = unb.split("\\+", -1);
            if (elements.length > 5) {
                controlNumber = elements[5].trim();
            } else {
                errors.add("UNB segment does not contain interchange control reference");
            }
        }

        String messageType = null;
        if (unh != null) {
            String[] elements = unh.split("\\+", -1);
            if (elements.length > 2) {
                messageType = elements[2].split(":", -1)[0].trim();
            } else {
                errors.add("UNH segment does not contain message identifier");
            }
        }

        return new ParsedEnvelope(TransactionFormat.EDIFACT, messageType, controlNumber, errors);
    }

    private String findSegment(String[] segments, String prefix) {
        return Arrays.stream(segments)
                .filter(s -> s.startsWith(prefix + "+"))
                .findFirst()
                .orElse(null);
    }
}
