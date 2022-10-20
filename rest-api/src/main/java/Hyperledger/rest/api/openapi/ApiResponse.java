package Hyperledger.rest.api.openapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
 
public final class ApiResponse<T> {
 
    private ErrorResponse error;
    private T data;
    private Status status;
 
    private ApiResponse(Status status, ErrorResponse error) {
        this.error = error;
        this.status = status;
    }
 
    private ApiResponse(Status status, T data) {
        this.data = data;
        this.status = status;
    }
 
    @JsonCreator
    public static <T> ApiResponse<T> success(
            @JsonProperty("status") Status status,
            @JsonProperty("data") T data) {
        return new ApiResponse<>(status, data);
    }
 
    @JsonCreator
    public static <T> ApiResponse<T> error(
            @JsonProperty("status") Status status,
            @JsonProperty("error") ErrorResponse error) {
        return new ApiResponse<>(status, error);
    }
 
    public enum Status {
        SUCCESS("ok"), ERROR("error");
 
        private final String status;
 
        Status(String status) {
            this.status = status;
        }
 
        @JsonValue
        public String getStatus() {
            return this.status;
        }
    }
 
    public ErrorResponse getError() {
        return this.error;
    }
 
    public T getData() {
        return this.data;
    }
 
    public Status getStatus() {
        return this.status;
    }
}