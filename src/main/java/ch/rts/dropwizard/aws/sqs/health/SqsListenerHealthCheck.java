package ch.rts.dropwizard.aws.sqs.health;

import ch.rts.dropwizard.aws.sqs.managed.SqsReceiverHandler;
import com.codahale.metrics.health.HealthCheck;

public class SqsListenerHealthCheck extends HealthCheck {

    private SqsReceiverHandler receiverHandler;

    public SqsListenerHealthCheck(SqsReceiverHandler receiverHandler) {
        this.receiverHandler = receiverHandler;
    }

    @Override
    protected Result check() throws Exception {
        return receiverHandler.getHealthCheck().execute();
    }

}
