package ch.rts.dropwizard.aws.sqs.managed;

import ch.rts.dropwizard.aws.sqs.exception.SqsBaseExceptionHandler;
import ch.rts.dropwizard.aws.sqs.health.SqsBundleHealthCheck;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SqsReceiverHandlerTest {

    @Mock
    private AmazonSQS sqs;

    @Mock
    private SqsReceiver receiver;

    @Mock
    private SqsBaseExceptionHandler exceptionHandler;

    private String queueUrl = "testqueue";

    private SqsReceiverHandler receiverHandler;

    @Before
    public void setUp() {
        receiverHandler = new SqsReceiverHandler(sqs, queueUrl, receiver, exceptionHandler);
    }

    @After
    public void tearDown() {
        receiverHandler = null;
    }

    @Test
    public void shouldBeHealthyAfterStart() throws Exception {
        //GIVEN
        when(sqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(new ReceiveMessageResult());

        //WHEN
        receiverHandler.start();

        //THEN
        Thread.sleep(100);
        assertThat(receiverHandler.isHealthy()).isTrue();
    }

    @Test
    public void shouldBeUnhealthyAfterStop() throws Exception {
        //GIVEN
        when(sqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(new ReceiveMessageResult());

        //WHEN
        receiverHandler.start();
        receiverHandler.stop();

        //THEN
        Thread.sleep(100);
        assertThat(receiverHandler.isHealthy()).isFalse();
    }

    @Test
    public void shouldGetMessagesWithAttributes() throws Exception {
        //GIVEN
        when(sqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(new ReceiveMessageResult());

        //WHEN
        receiverHandler.start();

        //THEN
        Thread.sleep(100);
        verify(sqs, atLeastOnce()).receiveMessage(new ReceiveMessageRequest(queueUrl).withMessageAttributeNames("All"));
        verify(sqs, never()).receiveMessage(new ReceiveMessageRequest(queueUrl));
    }

    @Test
    public void messageShouldBeProcessedAfterBeingConsumed() throws Exception {
        //GIVEN
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        Message message1 = new Message()
                .withMessageId("aaaa-bbbb-cccc-dddd-eeee")
                .withBody("Sample test message");
        Message message2 = new Message()
                .withMessageId("ffff-gggg-hhhh-iiii-jjjj")
                .withBody("Another sample test message");
        receiveMessageResult.setMessages(Lists.newArrayList(message1, message2));
        when(sqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(receiveMessageResult, new ReceiveMessageResult());

        //WHEN
        receiverHandler.start();

        //THEN
        Thread.sleep(100);
        verify(receiver, times(2)).receive(any());
        verify(receiver, times(1)).receive(message1);
        verify(receiver, times(1)).receive(message2);
    }

    @Test
    public void messageShouldBeDeletedAfterBeingConsumed() throws Exception {
        //GIVEN
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        Message message1 = new Message()
                .withMessageId("aaaa-bbbb-cccc-dddd-eeee")
                .withBody("Sample test message")
                .withReceiptHandle("qwertz");
        Message message2 = new Message()
                .withMessageId("ffff-gggg-hhhh-iiii-jjjj")
                .withBody("Another sample test message")
                .withReceiptHandle("asdfgh");
        receiveMessageResult.setMessages(Lists.newArrayList(message1, message2));
        when(sqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(receiveMessageResult);

        //WHEN
        receiverHandler.start();

        //THEN
        Thread.sleep(100);
        verify(sqs, atLeastOnce()).deleteMessage(new DeleteMessageRequest(queueUrl, "asdfgh"));
    }

}
