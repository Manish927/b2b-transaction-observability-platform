package com.manish.b2bmonitor.parser;

import com.manish.b2bmonitor.domain.ParsedEnvelope;
import com.manish.b2bmonitor.domain.TransactionFormat;

public interface EdiParser {
    TransactionFormat format();
    ParsedEnvelope parse(String payload);
}
