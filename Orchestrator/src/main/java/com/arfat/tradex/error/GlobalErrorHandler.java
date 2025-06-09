package com.arfat.tradex.error;

import com.arfat.tradex.order.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseBody
    DefaultErrorResponse handleOrderNotFoundException(HttpServletRequest req, OrderNotFoundException ex) {
        log.error("Order not found:", ex);
        return new DefaultErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}
