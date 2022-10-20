package Hyperledger.rest.api.openapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ErrorResponse {
    public final String code;
    public final String message;
 
    @JsonCreator
    public ErrorResponse(
            @JsonProperty("code") String code,
            @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }
}