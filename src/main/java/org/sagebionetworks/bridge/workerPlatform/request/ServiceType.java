package org.sagebionetworks.bridge.workerPlatform.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ServiceType {
    @JsonProperty("Bridge-Reporter")
    REPORTER("Bridge-Reporter"),
    @JsonProperty("Bridge-Exporter")
    EXPORTER("Bridge-Exporter"),
    @JsonProperty("Bridge-UDD")
    UDD("Bridge-UDD");

    private final String type;

    ServiceType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
