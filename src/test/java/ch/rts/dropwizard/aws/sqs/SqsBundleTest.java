package ch.rts.dropwizard.aws.sqs;

import ch.rts.dropwizard.aws.sqs.config.SqsBaseConfiguration;
import ch.rts.dropwizard.aws.sqs.config.SqsConfigurationHolder;
import ch.rts.dropwizard.aws.sqs.exception.CannotCreateSenderException;
import ch.rts.dropwizard.aws.sqs.health.SqsBundleHealthCheck;
import ch.rts.dropwizard.aws.sqs.managed.SqsReceiverHandler;
import ch.rts.dropwizard.aws.sqs.service.SqsSender;
import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SqsBundleTest {

    @Mock
    private SqsConfigurationHolder configurationHolder;

    @Mock
    private Environment environment;

    private SqsBundle bundle;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        SqsBaseConfiguration configuration = new SqsBaseConfiguration();
        configuration.setRegion(Regions.EU_CENTRAL_1.getName());
        when(configurationHolder.getSqsConfiguration()).thenReturn(configuration);

//        HealthCheckRegistry healthCheckRegistry = Mockito.mock(HealthCheckRegistry.class);
//        when(this.environment.healthChecks()).thenReturn(healthCheckRegistry);

        bundle = new SqsBundle();
    }

//    @Test
//    public void shouldSetCorrectRegion() throws Exception {
//        //GIVEN
//        AmazonSQS sqs = mock(AmazonSQS.class);
//        field("sqs").ofType(AmazonSQS.class).in(bundle).set(sqs);
//
//        LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
//        doNothing().when(lifecycle).manage((Managed) anyObject());
//        when(environment.lifecycle()).thenReturn(lifecycle);
//
//        HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
//        doNothing().when(healthChecks).register(anyObject(), anyObject());
//        when(environment.healthChecks()).thenReturn(healthChecks);
//
//        //WHEN
//        bundle.run(configurationHolder, environment);
//
//        //THEN
//        verify(sqs, times(1)).setRegion(RegionUtils.getRegion(Regions.EU_CENTRAL_1.getName()));
//    }

    @Test
    public void shouldGetCorrectQueueUrl() throws Exception {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);
        field("sqs").ofType(AmazonSQS.class).in(bundle).set(sqs);

        String queueUrl = "https://eu-central-1/queue.amazonaws.com/123456/test-queue";
        when(sqs.getQueueUrl("test-queue")).thenReturn(new GetQueueUrlResult()
                .withQueueUrl(queueUrl));

        //WHEN
        Optional<String> urlForQueue = bundle.getUrlForQueue("test-queue");

        //THEN
        assertThat(urlForQueue.isPresent()).isTrue();
        assertThat(urlForQueue.get()).isEqualTo(queueUrl);
    }

    @Test
    public void shouldCreateNewQueueWhenNoQueueUrlIsFound() throws Exception {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);
        field("sqs").ofType(AmazonSQS.class).in(bundle).set(sqs);

        String queueUrl = "https://eu-central-1/queue.amazonaws.com/123456/test-queue";
        when(sqs.getQueueUrl("test-queue")).thenThrow(new QueueDoesNotExistException("Simulates that queue does not exist"));
        when(sqs.createQueue(new CreateQueueRequest("test-queue"))).thenReturn(new CreateQueueResult().withQueueUrl(queueUrl));

        //WHEN
        Optional<String> urlForQueue = bundle.getUrlForQueue("test-queue");

        //THEN
        assertThat(urlForQueue.isPresent()).isTrue();
        assertThat(urlForQueue.get()).isEqualTo(queueUrl);
    }

    @Test
    public void shouldRegisterHealthCheck() throws Exception {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);

        SqsBundle spiedBundle = spy(bundle);
        doReturn(sqs).when(spiedBundle).getAmazonSQS();

        LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
        doNothing().when(lifecycle).manage((Managed) anyObject());
        when(environment.lifecycle()).thenReturn(lifecycle);

        HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
        doNothing().when(healthChecks).register(anyObject(), anyObject());
        when(environment.healthChecks()).thenReturn(healthChecks);

        //WHEN
        spiedBundle.run(configurationHolder, environment);

        //THEN
        verify(healthChecks, times(1)).register(eq("SqsBundle"), any(SqsBundleHealthCheck.class));
    }

    @Test
    public void shouldCorrectlyCreateSenderIfQueueExists() throws Exception, CannotCreateSenderException {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);
        field("sqs").ofType(AmazonSQS.class).in(bundle).set(sqs);

        String queueUrl = "https://eu-central-1/queue.amazonaws.com/123456/test-queue";
        when(sqs.getQueueUrl("test-queue")).thenReturn(new GetQueueUrlResult()
                .withQueueUrl(queueUrl));

        //WHEN
        SqsSender sender = bundle.createSender("test-queue");

        //THEN
        assertThat(sender).isNotNull();
    }

    @Test(expected = CannotCreateSenderException.class)
    public void shouldThrowExceptionWhenCreatingSenderIfQueueDoesNotExists() throws Exception, CannotCreateSenderException {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);
        field("sqs").ofType(AmazonSQS.class).in(bundle).set(sqs);

        when(sqs.getQueueUrl(anyString())).thenThrow(new QueueDoesNotExistException("Simulate queue does not exist"));
        when(sqs.createQueue((CreateQueueRequest) any())).thenThrow(new AmazonClientException("Simulate queue cannot be created"));

        //WHEN
        bundle.createSender("test-queue");

        //THEN
    }

    @Test
    public void shouldCorrectlyRegisterReceiver() throws Exception {
        //GIVEN
        AmazonSQS sqs = mock(AmazonSQS.class);

        String queueUrl = "https://eu-central-1/queue.amazonaws.com/123456/test-queue";
        when(sqs.getQueueUrl("test-queue")).thenReturn(new GetQueueUrlResult()
                .withQueueUrl(queueUrl));

        LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
        doNothing().when(lifecycle).manage((Managed) anyObject());
        when(environment.lifecycle()).thenReturn(lifecycle);

        HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
        doNothing().when(healthChecks).register(anyObject(), anyObject());
        when(environment.healthChecks()).thenReturn(healthChecks);

        SqsBundle spiedBundle = spy(bundle);
        doReturn(sqs).when(spiedBundle).getAmazonSQS();

        spiedBundle.run(configurationHolder, environment);

        //WHEN
        spiedBundle.registerReceiver("test-queue", (m) -> process(m));

        //THEN
        verify(spiedBundle, times(1)).internalRegisterReceiver(eq("test-queue"), any(SqsReceiverHandler.class));
    }

    private void process(Object m) {
        // nothing to do here
    }

}
