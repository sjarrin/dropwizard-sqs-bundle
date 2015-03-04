package ch.rts.dropwizard.aws.sqs.health;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqsBundleHealthCheck extends HealthCheck {

    private AmazonSQS sqs;

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsBundleHealthCheck.class);

    public SqsBundleHealthCheck(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    @Override
    protected Result check() throws Exception {
        try {
            ListQueuesResult listQueuesResult = sqs.listQueues();
            if (listQueuesResult != null) {
                return Result.healthy("OK");
            }
            else {
                return Result.unhealthy("Could not fetch queues list from AWS");
            }
        } catch (AmazonClientException e) {
            LOGGER.error(e.getMessage(),e);
            return Result.unhealthy("Could not reach AWS to list queues");
        }

    }

}
