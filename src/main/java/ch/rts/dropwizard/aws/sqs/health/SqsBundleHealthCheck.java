package ch.rts.dropwizard.aws.sqs.health;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.codahale.metrics.health.HealthCheck;

public class SqsBundleHealthCheck extends HealthCheck {

    private AmazonSQS sqs;

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
            return Result.unhealthy("Could not reach AWS to list queues");
        }

    }

}
