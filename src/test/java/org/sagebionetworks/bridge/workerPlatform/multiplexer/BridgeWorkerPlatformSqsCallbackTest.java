package org.sagebionetworks.bridge.workerPlatform.multiplexer;

import com.fasterxml.jackson.databind.JsonNode;
import org.sagebionetworks.bridge.exporter.request.BridgeExporterProcessor;
import org.sagebionetworks.bridge.json.DefaultObjectMapper;
import org.sagebionetworks.bridge.reporter.request.ReportScheduleName;
import org.sagebionetworks.bridge.reporter.worker.BridgeReporterProcessor;
import org.sagebionetworks.bridge.sqs.PollSqsWorkerBadRequestException;
import org.sagebionetworks.bridge.udd.worker.BridgeUddProcessor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BridgeWorkerPlatformSqsCallbackTest {
    public BridgeWorkerPlatformSqsCallbackTest() throws IOException {
    }

    private static final BridgeWorkerPlatformRequest MOCK_WORKER_REQUEST = mock(BridgeWorkerPlatformRequest.class);
    private static final BridgeReporterProcessor MOCK_REPORTER_PROCESSOR = mock(BridgeReporterProcessor.class);
    private static final BridgeExporterProcessor MOCK_EXPORTER_PROCESSOR = mock(BridgeExporterProcessor.class);
    private static final BridgeUddProcessor MOCK_UDD_PROCESSOR = mock(BridgeUddProcessor.class);

    // simple strings for test
    private static final String TEST_SCHEDULER = "test-scheduler";
    private static final String TEST_STUDY_ID = "api";
    private static final String TEST_EMAIL = "zhizhen.lin@sagebase.org";
    private static final ReportScheduleName TEST_SCHEDULE_TYPE = ReportScheduleName.DAILY;

    private static final String REQUEST_JSON_MSG = "{\n" +
            "   \"service\":\"Bridge-Reporter\",\n" +
            "   \"body\":" + "{\n" +
            "       \"scheduler\":\"test-scheduler\",\n" +
            "       \"scheduleType\":\"DAILY\",\n" +
            "       \"startDateTime\":\"2016-10-19T00:00:00.000Z\",\n" +
            "       \"endDateTime\":\"2016-10-20T23:59:59.000Z\"\n" +
            "   }\n" +
            "}";

    private static final String REQUEST_JSON_EXPORTER_MSG = "{\n" +
            "   \"service\":\"Bridge-Exporter\",\n" +
            "   \"body\":" + "{\n" +
            "       \"date\":\"2015-12-01\",\n" +
            "       \"tag\":\"test-tag\"\n" +
            "   }\n" +
            "}";

    private static final String REQUEST_JSON_UDD_MSG = "{\n" +
            "   \"service\":\"Bridge-UDD\",\n" +
            "   \"body\":" + "{\n" +
            "       \"studyId\":\"" + TEST_STUDY_ID +"\",\n" +
            "       \"username\":\"" + TEST_EMAIL + "\",\n" +
            "       \"startDate\":\"2015-03-09\",\n" +
            "       \"endDate\":\"2015-03-31\"\n" +
            "   }\n" +
            "}";

    private static final String REPORTER_REQUEST = "{\n" +
            "   \"scheduler\":\"" + TEST_SCHEDULER +"\",\n" +
            "   \"scheduleType\":\"" + TEST_SCHEDULE_TYPE.toString() + "\",\n" +
            "   \"startDateTime\":\"2016-10-19T00:00:00.000Z\",\n" +
            "   \"endDateTime\":\"2016-10-20T23:59:59.000Z\"\n" +
            "}";

    private static final String EXPORTER_REQUEST = "{\n" +
            "   \"date\":\"" + "2015-12-01" +"\",\n" +
            "   \"tag\":\"test-tag\"\n" +
            "}";

    private static final String UDD_REQUEST = "{\n" +
            "   \"studyId\":\"" + TEST_STUDY_ID +"\",\n" +
            "   \"username\":\"" + TEST_EMAIL + "\",\n" +
            "   \"startDate\":\"2015-03-09\",\n" +
            "   \"endDate\":\"2015-03-31\"\n" +
            "}";

    private final JsonNode REPORTER_REQUEST_JSON = DefaultObjectMapper.INSTANCE.readValue(REPORTER_REQUEST, JsonNode.class);
    private final JsonNode EXPORTER_REQUEST_JSON = DefaultObjectMapper.INSTANCE.readValue(EXPORTER_REQUEST, JsonNode.class);
    private final JsonNode UDD_REQUEST_JSON = DefaultObjectMapper.INSTANCE.readValue(UDD_REQUEST, JsonNode.class);



    // test members
    private BridgeWorkerPlatformSqsCallback callback;

    @BeforeMethod
    public void setup() throws Exception {
        // set up callback
        callback = new BridgeWorkerPlatformSqsCallback();
        callback.setBridgeReporterProcessor(MOCK_REPORTER_PROCESSOR);
        callback.setBridgeExporterProcessor(MOCK_EXPORTER_PROCESSOR);
        callback.setBridgeUddProcessor(MOCK_UDD_PROCESSOR);
    }

    @Test
    public void testBridgeReporter() throws Exception {
        callback.callback(REQUEST_JSON_MSG);

        verify(MOCK_REPORTER_PROCESSOR).process(eq(REPORTER_REQUEST_JSON));
    }

    @Test
    public void testBridgeExporter() throws Exception {
        callback.callback(REQUEST_JSON_EXPORTER_MSG);

        verify(MOCK_EXPORTER_PROCESSOR).process(eq(EXPORTER_REQUEST_JSON));
    }

    @Test
    public void testBridgeUdd() throws Exception {
        callback.callback(REQUEST_JSON_UDD_MSG);

        verify(MOCK_UDD_PROCESSOR).process(eq(UDD_REQUEST_JSON));
    }

    @Test(expectedExceptions = PollSqsWorkerBadRequestException.class)
    public void malformedRequest() throws Exception {
        callback.callback("not json");
    }
}
