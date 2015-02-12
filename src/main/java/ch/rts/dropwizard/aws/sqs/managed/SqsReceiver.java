package ch.rts.dropwizard.aws.sqs.managed;

public interface SqsReceiver<T>{

    public void receive(T message);

}
