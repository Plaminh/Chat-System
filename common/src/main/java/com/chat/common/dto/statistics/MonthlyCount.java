package com.chat.common.dto.statistics;

public record MonthlyCount(int year, int month, long count) {
  public MonthlyCount {
    if (year < 1 || month < 1 || month > 12 || count < 0) {
      throw new IllegalArgumentException("monthly count values are invalid");
    }
  }
}
