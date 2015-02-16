package ch.rts.dropwizard.aws.sqs.exception;

import com.amazonaws.services.sqs.model.Message;

public interface SqsBaseExceptionHandler {

    boolean onException(Message message, Exception exception);

}
