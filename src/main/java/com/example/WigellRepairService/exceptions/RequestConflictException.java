package com.example.WigellRepairService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class RequestConflictException extends RuntimeException {

  private final String resource;
  private final String action;
  private final String reason;

  public RequestConflictException(String resource, String action, String reason) {
    super(String.format("Conflict due to %s not being eligible for action: %s - Reason: %s", resource, action, reason));
    this.resource = resource;
    this.action = action;
    this.reason = reason;
  }

  public String getResource() {return resource;}
  public String getAction() {return action;}
  public String getReason() {return reason;}

}
