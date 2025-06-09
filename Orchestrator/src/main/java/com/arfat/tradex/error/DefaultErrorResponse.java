package com.arfat.tradex.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DefaultErrorResponse {
    private final String message;
    private final int status;

    public DefaultErrorResponse() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = "Something went wrong";
    }

    public DefaultErrorResponse(HttpStatus httpStatus, String message) {
        this.status = httpStatus.value();
        this.message = message;
    }
}
