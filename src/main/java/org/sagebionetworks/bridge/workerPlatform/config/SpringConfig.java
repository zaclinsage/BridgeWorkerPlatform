package org.sagebionetworks.bridge.workerPlatform.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.sagebionetworks.bridge.config.Config;
import org.sagebionetworks.bridge.config.PropertiesConfig;
import org.sagebionetworks.bridge.file.FileHelper;
import org.sagebionetworks.bridge.heartbeat.HeartbeatLogger;
import org.sagebionetworks.bridge.sqs.PollSqsWorker;
import org.sagebionetworks.bridge.sqs.SqsHelper;
import org.sagebionetworks.bridge.workerPlatform.multiplexer.BridgeWorkerPlatformSqsCallback;
import org.sagebionetworks.client.SynapseAdminClientImpl;
import org.sagebionetworks.client.SynapseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// These configs get credentials from the default credential chain. For developer desktops, this is ~/.aws/credentials.
// For EC2 instances, this happens transparently.
// See http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html and
// http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-setup.html#set-up-creds for more info.
@ComponentScan("org.sagebionetworks.bridge.workerPlatform")
@Import({org.sagebionetworks.bridge.reporter.config.SpringConfig.class,
        org.sagebionetworks.bridge.udd.config.SpringConfig.class})
@Configuration("GeneralConfig")
public class SpringConfig {
    private static final String CONFIG_FILE = "BridgeWorkerPlatform.conf";
    private static final String DEFAULT_CONFIG_FILE = CONFIG_FILE;
    private static final String USER_CONFIG_FILE = System.getProperty("user.home") + "/" + CONFIG_FILE;

    @Bean(name = "workerPlatformConfigProperties")
    public Config bridgeConfig() {
        String defaultConfig = getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE).getPath();
        Path defaultConfigPath = Paths.get(defaultConfig);
        Path localConfigPath = Paths.get(USER_CONFIG_FILE);

        try {
            if (Files.exists(localConfigPath)) {
                return new PropertiesConfig(defaultConfigPath, localConfigPath);
            } else {
                return new PropertiesConfig(defaultConfigPath);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Bean
    public DynamoDB ddbClient() {
        return new DynamoDB(new AmazonDynamoDBClient());
    }

    @Bean
    public FileHelper fileHelper() {
        return new FileHelper();
    }

    @Bean
    public HeartbeatLogger heartbeatLogger() {
        HeartbeatLogger heartbeatLogger = new HeartbeatLogger();
        heartbeatLogger.setIntervalMinutes(bridgeConfig().getInt("heartbeat.interval.minutes"));
        return heartbeatLogger;
    }

    @Bean
    public SqsHelper sqsHelper() {
        SqsHelper sqsHelper = new SqsHelper();
        sqsHelper.setSqsClient(new AmazonSQSClient());
        return sqsHelper;
    }

    @Bean
    @Autowired
    public PollSqsWorker sqsWorker(BridgeWorkerPlatformSqsCallback callback) {
        Config config = bridgeConfig();

        PollSqsWorker sqsWorker = new PollSqsWorker();
        sqsWorker.setCallback(callback);
        sqsWorker.setQueueUrl(config.get("workerPlatform.request.sqs.queue.url"));
        sqsWorker.setSleepTimeMillis(config.getInt("workerPlatform.request.sqs.sleep.time.millis"));
        sqsWorker.setSqsHelper(sqsHelper());
        return sqsWorker;
    }

    @Bean(name="workerPlatformSynapseClient")
    public SynapseClient synapseClient() throws IOException {
        SynapseClient synapseClient = new SynapseAdminClientImpl();
        synapseClient.setUserName(bridgeConfig().get("synapse.user"));
        synapseClient.setApiKey(bridgeConfig().get("synapse.api.key"));
        return synapseClient;
    }
}
