package com.manish.b2bmonitor.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class As2ControllerTest {
    private static final String X12 = "ISA*00*          *00*          *12*SENDER         *12*RECEIVER       "
            + "*240101*1200*U*00401*000000905*0*P*>~ST*850*0001~";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ingestsConfiguredPartnerAndDeduplicatesRetry() throws Exception {
        String messageId = "<controller-dedup@example.test>";

        mockMvc.perform(as2Request(messageId, X12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andExpect(jsonPath("$.transaction.tradingPartnerId").value("partner-a"))
                .andExpect(jsonPath("$.transaction.as2MessageId").value("controller-dedup@example.test"))
                .andExpect(jsonPath("$.transaction.format").value("X12"))
                .andExpect(jsonPath("$.transaction.status").value("VALIDATED"));

        mockMvc.perform(as2Request(messageId, X12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true));
    }

    @Test
    void rejectsMissingMessageId() throws Exception {
        mockMvc.perform(post("/api/v1/as2/messages")
                        .contentType("application/EDI-X12")
                        .header("AS2-From", "PARTNER-A")
                        .header("AS2-To", "MANISH-B2B")
                        .content(X12))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid AS2 request"));
    }

    @Test
    void rejectsUnknownTradingPartner() throws Exception {
        mockMvc.perform(post("/api/v1/as2/messages")
                        .contentType("application/EDI-X12")
                        .header("AS2-From", "UNKNOWN-PARTNER")
                        .header("AS2-To", "MANISH-B2B")
                        .header("Message-ID", "<unknown-partner@example.test>")
                        .content(X12))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Trading partner not authorized"));
    }

    @Test
    void rejectsMessageIdReusedWithDifferentContent() throws Exception {
        String messageId = "<controller-conflict@example.test>";

        mockMvc.perform(as2Request(messageId, X12))
                .andExpect(status().isOk());

        mockMvc.perform(as2Request(messageId, X12 + "SE*2*0001~"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("AS2 Message-ID conflict"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder as2Request(
            String messageId, String payload) {
        return post("/api/v1/as2/messages")
                .contentType(MediaType.parseMediaType("application/EDI-X12"))
                .header("AS2-From", "PARTNER-A")
                .header("AS2-To", "MANISH-B2B")
                .header("Message-ID", messageId)
                .content(payload);
    }
}
