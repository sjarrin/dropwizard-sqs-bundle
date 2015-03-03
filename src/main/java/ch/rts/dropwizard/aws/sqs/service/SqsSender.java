package ch.rts.dropwizard.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SqsSender {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AmazonSQS sqs;

    private String queueUrl;

    private final ObjectMapper objectMapper;

    public SqsSender(AmazonSQS sqs, String queueUrl, ObjectMapper objectMapper) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
    }

    public void send(Object object) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(object);
            send(json, new HashMap<>());
        } catch (JsonProcessingException e) {
            logger.error("Could not send message to SQS, cause is " + e.getMessage(),e);
        }
    }

    public void send(String body) {
        send(body, new HashMap<>());
    }

    public void send(Object object, Map<String, MessageAttributeValue> attributes) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(object);
            send(json, attributes);
        } catch (JsonProcessingException e) {
            logger.error("Could not send message to SQS, cause is " + e.getMessage(),e);
        }
    }

    public void send(String body, Map<String, MessageAttributeValue> attributes) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withQueueUrl(queueUrl);
        sendMessageRequest.withMessageBody(body);
        for (Map.Entry<String, MessageAttributeValue> entry : attributes.entrySet()) {
            sendMessageRequest.addMessageAttributesEntry(entry.getKey(), entry.getValue());
        }
        sqs.sendMessage(sendMessageRequest);
    }

}
