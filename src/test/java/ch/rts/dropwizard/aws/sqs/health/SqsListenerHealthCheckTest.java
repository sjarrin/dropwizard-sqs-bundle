package ch.rts.dropwizard.aws.sqs.health;

import ch.rts.dropwizard.aws.sqs.managed.SqsReceiverHandler;
import com.codahale.metrics.health.HealthCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SqsListenerHealthCheckTest {

    @Mock
    private SqsReceiverHandler receiverHandler;

    @Test
    public void shouldGetAndCallReceiverHandlerHealthCheck() throws Exception {
        //GIVEN
        HealthCheck healthCheck = mock(HealthCheck.class);
        when(healthCheck.execute()).thenReturn(HealthCheck.Result.healthy());

        when(receiverHandler.getHealthCheck()).thenReturn(healthCheck);
        SqsListenerHealthCheck listenerHealthCheck = new SqsListenerHealthCheck(receiverHandler);

        //WHEN
        listenerHealthCheck.check();

        //THEN
        verify(receiverHandler, times(1)).getHealthCheck();
        verify(healthCheck, times(1)).execute();
    }

}