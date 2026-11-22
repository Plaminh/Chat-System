package com.chat.server.exception;

import com.chat.common.enums.ErrorCode;
import java.util.List;

/**
 * Thrown when a request asks to sort by a field that the query does not allow. Handlers return
 * {@link #errorCode()} ({@code VALIDATION_FAILED}) and the message, which names the rejected field
 * and the allowed fields.
 */
public class InvalidSortFieldException extends RuntimeException {
  private final String field;
  private final List<String> allowedFields;

  public InvalidSortFieldException(String field, List<String> allowedFields) {
    super("Unsupported sort field '" + field + "'; allowed: " + String.join(", ", allowedFields));
    this.field = field;
    this.allowedFields = List.copyOf(allowedFields);
  }

  public String field() {
    return field;
  }

  public List<String> allowedFields() {
    return allowedFields;
  }

  public ErrorCode errorCode() {
    return ErrorCode.VALIDATION_FAILED;
  }
}
