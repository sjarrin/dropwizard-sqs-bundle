package ch.rts.dropwizard.aws.sqs.health;

import com.codahale.metrics.health.HealthCheck;

public class SqsBundleHealthCheck extends HealthCheck {

    public SqsBundleHealthCheck() {

    }

    @Override
    protected Result check() throws Exception {
        //TODO
        return Result.unhealthy("Not implemented yet");
    }

}
