package com.manish.b2bmonitor.as2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "b2b.as2")
public class As2Properties {
    private String localIdentifier;
    private Map<String, Partner> partners = new LinkedHashMap<>();

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public void setLocalIdentifier(String localIdentifier) {
        this.localIdentifier = localIdentifier;
    }

    public Map<String, Partner> getPartners() {
        return partners;
    }

    public void setPartners(Map<String, Partner> partners) {
        this.partners = partners;
    }

    public static class Partner {
        private String as2Identifier;
        private boolean enabled = true;

        public String getAs2Identifier() {
            return as2Identifier;
        }

        public void setAs2Identifier(String as2Identifier) {
            this.as2Identifier = as2Identifier;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
