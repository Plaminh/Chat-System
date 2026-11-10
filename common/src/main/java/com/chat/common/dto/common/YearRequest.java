package com.chat.common.dto.common;

public record YearRequest(int year) {
  public YearRequest {
    if (year < 1) {
      throw new IllegalArgumentException("year must be positive");
    }
  }
}
