package com.lineate.elastic.error;


import com.lineate.elastic.dto.StatusResponse;
import com.lineate.elastic.exception.ElasticActionFailedException;
import com.lineate.elastic.exception.ElasticActionForbiddenException;
import com.lineate.elastic.exception.ElasticEntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ApplicationErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationErrorHandler.class);
    private static final String MESSAGE_TEMPLATE = "Exception with type: '%s', message: '%s'";

    /**
     * Handles ElasticEntityNotFoundException.
     *
     * @param ex exception object
     * @return status object
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ElasticEntityNotFoundException.class)
    @ResponseBody
    public StatusResponse handleIndexManagementException(ElasticEntityNotFoundException ex) {
        String message = String.format(MESSAGE_TEMPLATE, ex.getClass().getSimpleName(), ex.getMessage());
        LOGGER.warn(message);
        return new StatusResponse(StatusResponse.Status.ERROR, ex.getMessage());
    }

    /**
     * Handles ElasticActionForbiddenException.
     *
     * @param ex exception object
     * @return status object
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ElasticActionForbiddenException.class)
    @ResponseBody
    public StatusResponse handleElasticActionForbiddenException(ElasticActionForbiddenException ex) {
        String message = String.format(MESSAGE_TEMPLATE, ex.getClass().getSimpleName(), ex.getMessage());
        LOGGER.warn(message);
        return new StatusResponse(StatusResponse.Status.ERROR, ex.getMessage());
    }

    /**
     * Handles ElasticActionFailedException.
     *
     * @param ex exception object
     * @return status object
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ElasticActionFailedException.class)
    @ResponseBody
    public StatusResponse handleElasticActionFailedException(ElasticActionFailedException ex) {
        String message = String.format(MESSAGE_TEMPLATE, ex.getClass().getSimpleName(), ex.getMessage());
        LOGGER.warn(message);
        return new StatusResponse(StatusResponse.Status.ERROR, ex.getMessage());
    }

    /**
     * Handles any unknown exception to be user friendly for api consumer.
     *
     * @param ex exception object (Throwable)
     * @return status object
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public StatusResponse handleUnknownException(Throwable ex) {
        String loggerMessage = String.format(MESSAGE_TEMPLATE, ex.getClass().getSimpleName(), ex.getMessage());
        LOGGER.warn(loggerMessage, ex);
        return new StatusResponse(StatusResponse.Status.ERROR, ex.getMessage());
    }


}
