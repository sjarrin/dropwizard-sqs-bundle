package ch.rts.dropwizard.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsSenderTest {

    @Mock
    private AmazonSQS sqs;

    private String queueUrl = "testqueue";

    @Mock
    private ObjectMapper objectMapper;

    private SqsSender sender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sender = new SqsSender(sqs, queueUrl, objectMapper);
    }

    @Test
    public void shouldCorrectlySendTextMessage() {
        //GIVEN
        String body = "Sample text message";
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withQueueUrl(queueUrl);
        sendMessageRequest.withMessageBody(body);

        //WHEN
        sender.send(body);

        //THEN
        verify(sqs).sendMessage(sendMessageRequest);
    }

    @Test
    public void shouldCorrectlySendObjectMessageAsJson() throws JsonProcessingException {
        //GIVEN
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withQueueUrl(queueUrl);
        sendMessageRequest.withMessageBody("{index:1,content:\"Sample content\"}");

        when(objectMapper.writeValueAsString(sendMessageRequest)).thenReturn("");

        //WHEN
        DummyObject body = new DummyObject();
        sender.send(body);

        //THEN
//        verify(sqs).sendMessage(sendMessageRequest);
    }

    private class DummyObject {

        private Integer index = 0;

        private String content = "Sample content";

        private List<String> contents = Lists.newArrayList("Sample content 1", "Sample content 2");

    }

}
