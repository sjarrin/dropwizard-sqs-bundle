package ch.rts.dropwizard.aws.sqs.exception;

public interface SqsExceptionHandler extends SqsBaseExceptionHandler {

    boolean onException(String message, Exception exception);

}
