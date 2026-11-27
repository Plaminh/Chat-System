package com.chat.admin.chart;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ChartYearsTest {

  @Test
  void offersTheCurrentVietnamYearFirstAndTheFourBeforeIt() {
    int current = LocalDate.now(ChartYears.ZONE).getYear();

    assertArrayEquals(
        new Integer[] {current, current - 1, current - 2, current - 3, current - 4},
        ChartYears.selectableYears());
  }
}
