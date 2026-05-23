package com.app.chat_app.core.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String code;
    private String path;
    private Map<String, String> fieldErrors = Collections.emptyMap();

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        ApiErrorResponse r = new ApiErrorResponse();
        r.setTimestamp(Instant.now());
        r.setStatus(status);
        r.setError(error);
        r.setMessage(message);
        r.setPath(path);
        return r;
    }

    public static ApiErrorResponse validationFailed(String message, String path, Map<String, String> fieldErrors) {
        ApiErrorResponse r = of(400, "Bad Request", message, path);
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            r.setFieldErrors(Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors)));
        }
        return r;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }
}
