package com.ticketingsystem.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int code;
    private String status;
    private String message;
    private String path;
    private Map<String, Object> data;

    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.timestamp = LocalDateTime.now();
        this.code = httpStatus.value();
        this.status = httpStatus.name();
        this.message = message;
        this.path = "/error";
    }
}
