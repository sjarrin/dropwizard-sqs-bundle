package ch.rts.dropwizard.aws.sqs.exception;

public class CannotCreateSenderException extends Throwable {

    public CannotCreateSenderException() {
    }

    public CannotCreateSenderException(String message) {
        super(message);
    }

}
