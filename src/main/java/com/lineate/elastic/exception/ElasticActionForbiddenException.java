package com.lineate.elastic.exception;

public class ElasticActionForbiddenException extends RuntimeException {

    public ElasticActionForbiddenException(String message) {
        super(message);
    }

    public ElasticActionForbiddenException(Throwable cause) {
        super(cause);
    }

    public ElasticActionForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
