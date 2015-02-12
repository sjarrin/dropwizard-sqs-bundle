package ch.rts.dropwizard.aws.sqs;

import ch.rts.dropwizard.aws.sqs.config.SqsConfigurationHolder;
import ch.rts.dropwizard.aws.sqs.exception.CannotCreateSenderException;
import ch.rts.dropwizard.aws.sqs.health.SqsBundleHealthCheck;
import ch.rts.dropwizard.aws.sqs.health.SqsListenerHealthCheck;
import ch.rts.dropwizard.aws.sqs.managed.SqsReceiver;
import ch.rts.dropwizard.aws.sqs.managed.SqsReceiverHandler;
import ch.rts.dropwizard.aws.sqs.service.SqsSender;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SqsBundle implements ConfiguredBundle<SqsConfigurationHolder>, Managed {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SqsConfigurationHolder configuration;
    private Environment environment;

    private AmazonSQS sqs;

    private ObjectMapper objectMapper;

    public SqsBundle() {
        logger.debug("Test");
    }

    @Override
    public void run(SqsConfigurationHolder configuration, Environment environment) throws Exception {
        this.configuration = configuration;
        this.environment = environment;

        objectMapper = environment.getObjectMapper();

        sqs = getAmazonSQS();

        setSqsRegion();

//        for (String queueName : this.configuration.getSqsConfiguration().getQueueNames()) {
//            Optional<String> queueUrl = getUrlForQueue(queueName);
//            if (queueUrl.isPresent()) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("got url " + queueUrl + " for queue " + queueName);
//                }
//
//                SqsReceiver receiver = new SqsReceiver(sqs, queueUrl.get());
//
//                this.environment.healthChecks().register("SQS",
//                        new SqsListenerHealthCheck(receiver)
//                );
//
//            }
//        }

        environment.lifecycle().manage(this);
        environment.healthChecks().register("SqsBundle", new SqsBundleHealthCheck());
    }

    public SqsSender createSender(String queueName) throws CannotCreateSenderException {
        Optional<String> queueUrl = getUrlForQueue(queueName);
        if (queueUrl.isPresent()) {
            SqsSender sqsSender = new SqsSender(sqs, queueUrl.get(), objectMapper);
            return sqsSender;
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not create sender for queue name " + queueName + ", no messages will be sent for this queue");
            }
            throw new CannotCreateSenderException("Could not create sender for queue name " + queueName + ", no messages will be sent for this queue");
        }
    }

    public <T> void registerReceiver(String queueName, SqsReceiver<T> receiver) {
        Optional<String> queueUrl = getUrlForQueue(queueName);
        if (queueUrl.isPresent()) {
            SqsReceiverHandler<T> handler = new SqsReceiverHandler<>(
                    sqs,
                    queueUrl.get(),
                    receiver,
                    objectMapper,
                    (message, exception) -> {
                        logger.error("Error processing received message - acknowledging it anyway");
                        return true;
                    }
            );
            internalRegisterReceiver(queueName, handler);
        }
        else {
            logger.error("Cannot register receiver for queue name : " + queueName);
        }
    }

    private <T> void internalRegisterReceiver(String queueName, SqsReceiverHandler<T> handler) {
        environment.lifecycle().manage(handler);
        environment.healthChecks().register("SQS receiver for " + queueName, handler.getHealthCheck());
    }

    private AmazonSQS getAmazonSQS() {
        AWSCredentials credentials = getAwsCredentials();

        return new AmazonSQSClient(credentials);
    }

    private AWSCredentials getAwsCredentials() throws AmazonClientException {
        // The ProfileCredentialsProvider will return your [default]
        // credential profile by reading from the credentials file located at
        // (~/.aws/credentials).
        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        return credentials;
    }

    private void setSqsRegion() {
        String regionName = this.configuration.getSqsConfiguration().getRegion();
        Region region = RegionUtils.getRegion(regionName);
        if (logger.isDebugEnabled()) {
            logger.debug("Setting SQS region to " + region.getName());
        }
        sqs.setRegion(region);
    }

    /**
     * Retrieves queue url for the given queue name. If the queue does not exist, tries to create it.
     *
     * @param queueName the queue name to get url for
     * @return an optional String representing the queue url
     */
    private Optional<String> getUrlForQueue(String queueName) {
        Optional<String> queueUrl = Optional.empty();
        try {
            GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(queueName);
            if (queueUrlResult.getQueueUrl() != null) {
                queueUrl = Optional.of(queueUrlResult.getQueueUrl());
            }
        } catch (QueueDoesNotExistException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Queue " + queueName + " does not exist, try to create it");
            }
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
            queueUrl = Optional.of(sqs.createQueue(createQueueRequest).getQueueUrl());
        }

        return queueUrl;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void start() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting SQS client");
        }
    }

    @Override
    public void stop() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping SQS client");
        }
    }

}