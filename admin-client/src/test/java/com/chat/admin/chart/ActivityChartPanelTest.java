package com.chat.admin.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.statistics.MonthlyCount;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActivityChartPanelTest {

  @Test
  void testPopulateFromMonthlyCountsUpdatesDatasetAndMetrics() {
    ActivityChartPanel panel = new ActivityChartPanel(null);

    List<MonthlyCount> counts = new ArrayList<>();
    // Jan: 5, Feb: 15, ..., Dec: 115
    for (int month = 1; month <= 12; month++) {
      counts.add(new MonthlyCount(2026, month, month * 10L - 5L));
    }

    panel.populateFromMonthlyCounts(2026, counts);

    assertEquals(12, panel.getDataset().getColumnCount());
    assertEquals(5, panel.getDataset().getValue("Distinct Active Users (2026)", "Jan").intValue());
    assertEquals(115, panel.getDataset().getValue("Distinct Active Users (2026)", "Dec").intValue());

    // Total: 5 + 15 + ... + 115 = 720
    assertEquals("720", panel.getAnnualActiveTotalLabel().getText());
    assertTrue(panel.getPeakActivityMonthLabel().getText().contains("Dec"));
    assertTrue(panel.getPeakActivityMonthLabel().getText().contains("115"));
    assertTrue(panel.getMonthlyAvgActiveLabel().getText().contains("60.0"));
  }

  @Test
  void testSetSelectedYearUpdatesYearCombo() {
    ActivityChartPanel panel = new ActivityChartPanel(null);
    panel.setSelectedYear(2024);
    assertEquals(2024, panel.getSelectedYear());
    assertNotNull(panel.getDataset());
  }
}
