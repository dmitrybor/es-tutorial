package com.lineate.elastic.exception;

public class ElasticEntityNotFoundException extends RuntimeException {

    public ElasticEntityNotFoundException(String message) {
        super(message);
    }

    public ElasticEntityNotFoundException(Throwable cause) {
        super(cause);
    }

    public ElasticEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
