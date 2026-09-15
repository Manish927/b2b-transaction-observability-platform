package com.manish.b2bmonitor.parser;

import com.manish.b2bmonitor.domain.ParsedEnvelope;
import com.manish.b2bmonitor.domain.TransactionFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class X12Parser implements EdiParser {

    @Override
    public TransactionFormat format() {
        return TransactionFormat.X12;
    }

    @Override
    public ParsedEnvelope parse(String payload) {
        List<String> errors = new ArrayList<>();
        String[] segments = splitSegments(payload);

        String isa = findSegment(segments, "ISA");
        String st = findSegment(segments, "ST");

        if (isa == null) errors.add("Missing ISA interchange header");
        if (st == null) errors.add("Missing ST transaction-set header");

        String controlNumber = null;
        if (isa != null) {
            String[] elements = isa.split("\\*", -1);
            if (elements.length > 13) {
                controlNumber = elements[13].trim();
            } else {
                errors.add("ISA segment does not contain interchange control number (ISA13)");
            }
        }

        String messageType = null;
        if (st != null) {
            String[] elements = st.split("\\*", -1);
            if (elements.length > 1) {
                messageType = elements[1].trim();
            } else {
                errors.add("ST segment does not contain transaction-set identifier");
            }
        }

        return new ParsedEnvelope(TransactionFormat.X12, messageType, controlNumber, errors);
    }

    private String[] splitSegments(String payload) {
        return Arrays.stream(payload.replace("\r", "").replace("\n", "~").split("~"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private String findSegment(String[] segments, String prefix) {
        return Arrays.stream(segments)
                .filter(s -> s.startsWith(prefix + "*"))
                .findFirst()
                .orElse(null);
    }
}
