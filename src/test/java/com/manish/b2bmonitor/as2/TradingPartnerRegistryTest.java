package com.manish.b2bmonitor.as2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradingPartnerRegistryTest {
    private TradingPartnerRegistry registry;

    @BeforeEach
    void setUp() {
        As2Properties properties = new As2Properties();
        properties.setLocalIdentifier("MANISH-B2B");

        As2Properties.Partner partner = new As2Properties.Partner();
        partner.setAs2Identifier("PARTNER A");
        properties.setPartners(Map.of("partner-a", partner));
        registry = new TradingPartnerRegistry(properties);
    }

    @Test
    void resolvesConfiguredPartnerAndQuotedAs2Name() {
        TradingPartner partner = registry.resolveInbound("\"PARTNER A\"", "MANISH-B2B");

        assertThat(partner.id()).isEqualTo("partner-a");
        assertThat(partner.as2Identifier()).isEqualTo("PARTNER A");
    }

    @Test
    void treatsAs2IdentifiersAsCaseSensitive() {
        assertThatThrownBy(() -> registry.resolveInbound("partner a", "MANISH-B2B"))
                .isInstanceOf(UnknownTradingPartnerException.class);
    }

    @Test
    void rejectsMessagesAddressedToAnotherLocalSystem() {
        assertThatThrownBy(() -> registry.resolveInbound("\"PARTNER A\"", "OTHER-B2B"))
                .isInstanceOf(UnknownTradingPartnerException.class);
    }
}
