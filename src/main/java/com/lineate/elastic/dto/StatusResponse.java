package com.lineate.elastic.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Locale;

/**
 * Simple status response.
 */
@ApiModel("Status response (ok or failed)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {
    public static final StatusResponse OK = new StatusResponse(Status.OK);

    @ApiModelProperty(value = "status. `failed` for exceptions.", required = true)
    private final Status status;

    @ApiModelProperty(value = "error message string", required = true)
    private final String message;

    /**
     * Create response with status.
     *
     * @param status status value
     */
    public StatusResponse(final Status status) {
        this.status = status;
        this.message = null;
    }

    /**
     * Create failed response with custom message.
     *
     * @param message message text
     */
    public StatusResponse(final String message) {
        this.status = Status.ERROR;
        this.message = message;
    }

    /**
     * Create object with status and message.
     *
     * @param status  status value
     * @param message optional message
     */
    @JsonCreator
    public StatusResponse(@JsonProperty("status") final Status status, @JsonProperty("message") final String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Represent status of the response.
     */
    public enum Status {
        OK, ERROR;

        @JsonValue
        public String toValue() {
            return this.toString().toLowerCase(Locale.getDefault());
        }

        @JsonCreator
        public static Status forValue(String value) {
            return Status.valueOf(value.toUpperCase(Locale.getDefault()));
        }
    }
}
