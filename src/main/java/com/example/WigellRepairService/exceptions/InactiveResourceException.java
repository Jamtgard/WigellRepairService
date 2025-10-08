package com.example.WigellRepairService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InactiveResourceException extends RuntimeException {

    private final String resource;
    private final String reason;

    public InactiveResourceException(String resource, String reason) {
        super(String.format("%s is not available due to reason: %s", resource, reason));
        this.resource = resource;
        this.reason = reason;
    }

    public String getResource() {return resource;}
    public String getReason() {return reason;}

}
