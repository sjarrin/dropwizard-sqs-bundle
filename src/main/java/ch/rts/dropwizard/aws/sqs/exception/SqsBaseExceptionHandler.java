package ch.rts.dropwizard.aws.sqs.exception;

public interface SqsBaseExceptionHandler {

    boolean onException(String message, Exception exception);

}
