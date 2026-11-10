package com.chat.common.protocol;

import java.util.List;
import java.util.Objects;

public final class ContractChecks {
  private ContractChecks() {}

  public static String text(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public static long positive(long value, String field) {
    if (value <= 0) {
      throw new IllegalArgumentException(field + " must be positive");
    }
    return value;
  }

  public static int positive(int value, String field) {
    if (value <= 0) {
      throw new IllegalArgumentException(field + " must be positive");
    }
    return value;
  }

  public static <T> T required(T value, String field) {
    return Objects.requireNonNull(value, field + " is required");
  }

  public static <T> List<T> list(List<T> values, String field) {
    required(values, field);
    if (values.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException(field + " must not contain null values");
    }
    return List.copyOf(values);
  }

  public static byte[] bytes(byte[] value, String field) {
    required(value, field);
    return value.clone();
  }
}
