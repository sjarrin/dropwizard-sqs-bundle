package ch.rts.dropwizard.aws.sqs.managed;

public interface SqsReceiver<T>{

    void receive(T message);

}
