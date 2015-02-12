package ch.rts.dropwizard.aws.sqs.managed;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqsReceiver implements Managed {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Thread receiverThread;

    private AmazonSQS sqs;
    private String queueUrl;

    private AtomicBoolean isHealthy = new AtomicBoolean(false);

    public SqsReceiver(AmazonSQS sqs, String queueUrl) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
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
//        System.out.println("  Message");
//        System.out.println("    MessageId:     " + message.getMessageId());
//        System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//        System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//        System.out.println("    Body:          " + message.getBody());
//        for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
//            System.out.println("  Attribute");
//            System.out.println("    Name:  " + entry.getKey());
//            System.out.println("    Value: " + entry.getValue());
//        }
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

}
