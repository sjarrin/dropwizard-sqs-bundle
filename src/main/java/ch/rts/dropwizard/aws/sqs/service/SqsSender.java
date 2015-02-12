package ch.rts.dropwizard.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SqsSender {

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
            send(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void send(String body) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withQueueUrl(queueUrl);
        sendMessageRequest.withMessageBody(body);
        sqs.sendMessage(sendMessageRequest);
    }

}
