package com.lineate.elastic.exception;

public class ElasticActionFailedException extends RuntimeException {

    public ElasticActionFailedException(String message) {
        super(message);
    }

    public ElasticActionFailedException(Throwable cause) {
        super(cause);
    }

    public ElasticActionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
