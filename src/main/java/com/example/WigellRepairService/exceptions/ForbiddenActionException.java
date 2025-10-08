package com.example.WigellRepairService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenActionException extends RuntimeException {

    private final String action;
    private final String reason;

    public ForbiddenActionException(String action, String reason) {
        super(String.format("Action: %S - not valid due to %s", action, reason));
        this.action = action;
        this.reason = reason;
    }

    public String getAction() {return action;}
    public String getReason() {return reason;}

}
