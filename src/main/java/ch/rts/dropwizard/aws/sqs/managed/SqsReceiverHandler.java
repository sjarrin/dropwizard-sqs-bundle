package ch.rts.dropwizard.aws.sqs.managed;

import ch.rts.dropwizard.aws.sqs.exception.SqsBaseExceptionHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqsReceiverHandler<T> implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsReceiverHandler.class);

    private Thread receiverThread;

    private AmazonSQS sqs;
    private String queueUrl;
    private SqsReceiver<T> receiver;
    private SqsBaseExceptionHandler exceptionHandler;

    private AtomicBoolean isHealthy = new AtomicBoolean(false);

    public SqsReceiverHandler(AmazonSQS sqs, String queueUrl, SqsReceiver<T> receiver, SqsBaseExceptionHandler exceptionHandler) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.receiver = receiver;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void start() throws Exception {
        receiverThread = new Thread() {
            @Override
            public void run() {
                isHealthy.set(true);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Start listening to queue: " + queueUrl);
                }
                while (!isInterrupted()) {
                    try {
                        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                        receiveMessageRequest.setMaxNumberOfMessages(10);
                        List<Message> messages = sqs.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();

                        for (Message message : messages) {
                            processMessage(message);
                            deleteMessage(message);
                        }

                    } catch (Exception e) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("An error occurred while listening to queue " + queueUrl, e);
                        }
                    }
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Listener stopped for queue " + queueUrl);
                }
                isHealthy.set(false);
            }

        };

        receiverThread.start();
    }

    @Override
    public void stop() throws Exception {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Stop SQS receiver for queue " + queueUrl);
        }
        if (this.receiverThread != null) {
            this.receiverThread.interrupt();
        }
    }

    private void processMessage(Message message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Process message " + message);
        }

        try {
            receiver.receive((T) message);
        } catch (Exception e) {
            exceptionHandler.onException(message, e);
        }
    }

    private void deleteMessage(Message message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Delete message " + message);
        }
        String messageReceiptHandle = message.getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
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
