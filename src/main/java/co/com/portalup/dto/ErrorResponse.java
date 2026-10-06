package co.com.portalup.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<String> details;

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details == null ? Collections.<String>emptyList() : details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<String> getDetails() {
        return details;
    }
}
