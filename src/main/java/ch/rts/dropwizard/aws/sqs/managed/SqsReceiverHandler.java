package ch.rts.dropwizard.aws.sqs.managed;

import ch.rts.dropwizard.aws.sqs.exception.SqsBaseExceptionHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqsReceiverHandler<T> implements Managed {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Thread receiverThread;

    private AmazonSQS sqs;
    private String queueUrl;
    private SqsReceiver<T> receiver;
    private ObjectMapper objectMapper;
    private SqsBaseExceptionHandler exceptionHandler;

    private AtomicBoolean isHealthy = new AtomicBoolean(false);

    public SqsReceiverHandler(AmazonSQS sqs, String queueUrl, SqsReceiver<T> receiver, ObjectMapper objectMapper, SqsBaseExceptionHandler exceptionHandler) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.receiver = receiver;
        this.objectMapper = objectMapper;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void start() throws Exception {
        receiverThread = new Thread() {
            @Override
            public void run() {

                if (logger.isInfoEnabled()) {
                    logger.info("Start listening to queue: " + queueUrl);
                }
                while (!isInterrupted()) {
                    try {
                        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

                        for (Message message : messages) {
                            processMessage(message);
                            deleteMessage(message);
                        }

                    } catch (Exception e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("An error occurred while listening to queue " + queueUrl, e);
                        }
                    }
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Listener stopped for queue " + queueUrl);
                }
            }

        };

        receiverThread.start();
    }

    @Override
    public void stop() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Stop SQS receiver for queue " + queueUrl);
        }
        this.receiverThread.interrupt();
    }

    private void processMessage(Message message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Process message " + message);
        }

        receiver.receive((T) message);
    }

    private void deleteMessage(Message message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Delete message " + message);
        }
        String messageRecieptHandle = message.getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
    }

    public boolean isHealthy() {
        return this.isHealthy.get();
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public HealthCheck getHealthCheck() {
        return new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                if (isHealthy()) {
                    return Result.healthy("OK");
                } else {
                    return Result.unhealthy("KO");
                }
            }
        };
    }

}
