package ch.rts.dropwizard.aws.sqs.health;

import ch.rts.dropwizard.aws.sqs.managed.SqsReceiver;
import com.codahale.metrics.health.HealthCheck;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SqsListenerHealthCheckTest {

    SqsReceiver receiver;

    @Before
    public void setUp() throws Exception {
        receiver = Mockito.mock(SqsReceiver.class);
    }

    @After
    public void tearDown() throws Exception {
        receiver = null;
    }

    @Test
    public void shouldBeOkIfReceiverIsOk() throws Exception {
        //TODO
        //GIVEN
//        when(receiver.isHealthy()).thenReturn(true);
//
//        //WHEN
//        SqsListenerHealthCheck healthCheck = new SqsListenerHealthCheck(receiver);
//
//        //THEN
//        assertThat(healthCheck.check()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void shouldBeKoIfReceiverIsKo() throws Exception {
        //TODO
//        //GIVEN
//        when(receiver.isHealthy()).thenReturn(false);
//
//        //WHEN
//        SqsListenerHealthCheck healthCheck = new SqsListenerHealthCheck(receiver);
//
//        //THEN
//        assertThat(healthCheck.check()).isEqualTo(HealthCheck.Result.unhealthy("SQS Listener is not healthy for queue " + receiver.getQueueUrl()));
    }

}