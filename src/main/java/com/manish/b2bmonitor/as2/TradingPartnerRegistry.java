package com.manish.b2bmonitor.as2;

import org.springframework.stereotype.Component;

@Component
public class TradingPartnerRegistry {
    private final As2Properties properties;

    public TradingPartnerRegistry(As2Properties properties) {
        this.properties = properties;
    }

    public TradingPartner resolveInbound(String as2From, String as2To) {
        requireHeader(as2From, "AS2-From");
        requireHeader(as2To, "AS2-To");

        String sender = decodeAs2Name(as2From);
        String receiver = decodeAs2Name(as2To);
        if (!receiver.equals(properties.getLocalIdentifier())) {
            throw new UnknownTradingPartnerException("Unknown AS2-To identifier: " + receiver);
        }

        return properties.getPartners().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .filter(entry -> sender.equals(entry.getValue().getAs2Identifier()))
                .map(entry -> new TradingPartner(entry.getKey(), entry.getValue().getAs2Identifier()))
                .findFirst()
                .orElseThrow(() -> new UnknownTradingPartnerException(
                        "Unknown or disabled AS2-From identifier: " + sender));
    }

    private void requireHeader(String value, String headerName) {
        if (value == null || value.isBlank()) {
            throw new InvalidAs2RequestException("Missing required " + headerName + " header");
        }
    }

    static String decodeAs2Name(String value) {
        String name = value.trim();
        if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        if (name.isEmpty() || name.length() > 128 || !name.chars().allMatch(c -> c >= 32 && c <= 126)) {
            throw new InvalidAs2RequestException("Invalid AS2 identifier");
        }
        return name;
    }
}
