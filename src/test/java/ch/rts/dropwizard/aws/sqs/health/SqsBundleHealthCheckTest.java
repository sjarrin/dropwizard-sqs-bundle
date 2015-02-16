package ch.rts.dropwizard.aws.sqs.health;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.codahale.metrics.health.HealthCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqsBundleHealthCheckTest {

    @Mock
    private AmazonSQS sqs;

    private SqsBundleHealthCheck sqsBundleHealthCheck;

    @Before
    public void setUp() {
        sqsBundleHealthCheck = new SqsBundleHealthCheck(sqs);
    }

    @Test
    public void shouldBeHealthy() throws Exception {
        //GIVEN
        String queueUrl1 = "https://eu-central-1/queue.amazonaws.com/123456/test-queue";
        when(sqs.listQueues()).thenReturn(new ListQueuesResult().withQueueUrls(queueUrl1));

        //WHEN
        HealthCheck.Result result = sqsBundleHealthCheck.check();

        //THEN
        assertThat(result.isHealthy()).isTrue();
    }

    @Test
    public void shouldBeUnhealthyIfQueuesListCannotBeFetched() throws Exception {
        //GIVEN

        //WHEN
        HealthCheck.Result result = sqsBundleHealthCheck.check();

        //THEN
        assertThat(result.isHealthy()).isFalse();
    }

    @Test
    public void shouldBeUnhealthyWhenAwsIsUnreachable() throws Exception {
        //GIVEN
        when(sqs.listQueues()).thenThrow(new AmazonClientException("Simulate Amazon is unreachable"));

        //WHEN
        HealthCheck.Result result = sqsBundleHealthCheck.check();

        //THEN
        assertThat(result.isHealthy()).isFalse();
    }

}
