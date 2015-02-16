package ch.rts.dropwizard.aws.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsSenderTest {

    @Mock
    private AmazonSQS sqs;

    private String queueUrl = "testqueue";

    private ObjectMapper objectMapper;

    private SqsSender sender;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        sender = new SqsSender(sqs, queueUrl, objectMapper);
    }

    @Test
    public void shouldCorrectlySendTextMessage() {
        //GIVEN
        String body = "Sample text message";

        //WHEN
        sender.send(body);

        //THEN
        SendMessageRequest expected = new SendMessageRequest();
        expected.withQueueUrl(queueUrl)
                .withMessageBody(body);
        verify(sqs).sendMessage(expected);
    }

    @Test
    public void shouldCorrectlySendObjectMessageAsJson() throws JsonProcessingException {
        //GIVEN
        DummyObject bodyObject = new DummyObject();

        //WHEN
        sender.send(bodyObject);

        //THEN
        SendMessageRequest expected = new SendMessageRequest();
        expected.withQueueUrl(queueUrl)
                .withMessageBody(objectMapper.writeValueAsString(bodyObject));
        verify(sqs).sendMessage(expected);
    }

    @Test
    public void shouldSendMessageWithCorrectAttributes() {
        //GIVEN
        String body = "Sample text message";
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("attribute1", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue("value1"));
        attributes.put("attribute2", new MessageAttributeValue()
                .withDataType("Number")
                .withStringValue("230.000000000000000001"));

        //WHEN
        sender.send(body, attributes);

        //THEN
        SendMessageRequest expected = new SendMessageRequest();
        expected.withQueueUrl(queueUrl)
                .withMessageBody(body)
                .withMessageAttributes(attributes);
        verify(sqs).sendMessage(expected);
    }

    @Test
    public void shouldSendObjectMessageWithCorrectAttributes() throws JsonProcessingException {
        //GIVEN
        DummyObject bodyObject = new DummyObject();
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("attribute1", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue("value1"));
        attributes.put("attribute2", new MessageAttributeValue()
                .withDataType("Number")
                .withStringValue("230.000000000000000001"));

        //WHEN
        sender.send(bodyObject, attributes);

        //THEN
        SendMessageRequest expected = new SendMessageRequest();
        expected.withQueueUrl(queueUrl)
                .withMessageBody(objectMapper.writeValueAsString(bodyObject))
                .withMessageAttributes(attributes);
        verify(sqs).sendMessage(expected);
    }

    private class DummyObject implements Serializable {

        private Integer index = 0;

        private String content = "Sample content";

        private List<String> contents = Lists.newArrayList("Sample content 1", "Sample content 2");

        public DummyObject() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getContents() {
            return contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }

    }

}
