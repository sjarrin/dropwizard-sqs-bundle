package ch.rts.dropwizard.aws.sqs.health;

import ch.rts.dropwizard.aws.sqs.managed.SqsReceiver;
import com.codahale.metrics.health.HealthCheck;

public class SqsListenerHealthCheck extends HealthCheck {

    private SqsReceiver receiver;

    public SqsListenerHealthCheck(SqsReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected Result check() throws Exception {
        //TODO
        return Result.unhealthy("Not yet implemented");
    }

}
