package com.chat.admin.chart;

import java.time.LocalDate;
import java.time.ZoneId;

/** Years offered by the annual charts: the current year in Vietnam time and the four before it. */
public final class ChartYears {
  /** The server counts months in Vietnam time, so the current year is taken in the same zone. */
  static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private static final int YEARS_SHOWN = 5;

  private ChartYears() {}

  public static int currentYear() {
    return LocalDate.now(ZONE).getYear();
  }

  /** Newest first, so the current year is the first choice. */
  public static Integer[] selectableYears() {
    int current = currentYear();
    Integer[] years = new Integer[YEARS_SHOWN];
    for (int i = 0; i < YEARS_SHOWN; i++) {
      years[i] = current - i;
    }
    return years;
  }
}
