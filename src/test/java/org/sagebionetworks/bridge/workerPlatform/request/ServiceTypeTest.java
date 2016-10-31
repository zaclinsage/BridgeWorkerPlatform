package org.sagebionetworks.bridge.workerPlatform.request;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ServiceTypeTest {
    // branch coverage test to satisfy jacoco
    @Test
    public void valueOf() {
        assertEquals(ServiceType.valueOf("REPORTER"), ServiceType.REPORTER);
        assertEquals(ServiceType.valueOf("EXPORTER"), ServiceType.EXPORTER);
        assertEquals(ServiceType.valueOf("UDD"), ServiceType.UDD);
    }
}
