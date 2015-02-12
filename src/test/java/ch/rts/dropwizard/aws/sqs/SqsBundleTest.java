package ch.rts.dropwizard.aws.sqs;

import ch.rts.dropwizard.aws.sqs.config.SqsBaseConfiguration;
import ch.rts.dropwizard.aws.sqs.config.SqsConfigurationHolder;
import com.amazonaws.regions.Regions;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

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

        HealthCheckRegistry healthCheckRegistry = Mockito.mock(HealthCheckRegistry.class);
        when(this.environment.healthChecks()).thenReturn(healthCheckRegistry);
//        when(healthCheckRegistry.register("testQueue", )).then(doNothing());

        bundle = new SqsBundle();
    }

    @Test
    public void shouldSetCorrectRegion() throws Exception {
        //GIVEN

        //WHEN
//        bundle.run(configurationHolder, environment);

        //THEN
//        verify(sqs, times(1)).setRegion()
    }

}
