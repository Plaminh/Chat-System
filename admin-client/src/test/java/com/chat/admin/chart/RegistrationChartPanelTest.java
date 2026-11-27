package com.chat.admin.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.statistics.MonthlyCount;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegistrationChartPanelTest {

  @Test
  void testPopulateFromMonthlyCountsUpdatesDatasetAndMetrics() {
    RegistrationChartPanel panel = new RegistrationChartPanel(null);

    List<MonthlyCount> counts = new ArrayList<>();
    // Jan: 10, Feb: 20, Mar: 30, ..., Dec: 120
    for (int month = 1; month <= 12; month++) {
      counts.add(new MonthlyCount(2026, month, month * 10L));
    }

    panel.populateFromMonthlyCounts(2026, counts);

    assertEquals(12, panel.getDataset().getColumnCount());
    assertEquals(10, panel.getDataset().getValue("New Registrations (2026)", "Jan").intValue());
    assertEquals(120, panel.getDataset().getValue("New Registrations (2026)", "Dec").intValue());

    // Total: 10 + 20 + ... + 120 = 780
    assertEquals("780", panel.getAnnualTotalLabel().getText());
    assertTrue(panel.getPeakMonthLabel().getText().contains("Dec"));
    assertTrue(panel.getPeakMonthLabel().getText().contains("120"));
    assertTrue(panel.getMonthlyAvgLabel().getText().contains("65.0"));
  }

  @Test
  void testSetSelectedYearUpdatesYearCombo() {
    RegistrationChartPanel panel = new RegistrationChartPanel(null);
    panel.setSelectedYear(2024);
    assertEquals(2024, panel.getSelectedYear());
    assertNotNull(panel.getDataset());
  }

  @Test
  void showsAnErrorAndNoSampleFiguresWhenDisconnected() {
    RegistrationChartPanel panel = new RegistrationChartPanel(null);

    assertEquals(0, panel.getDataset().getColumnCount());
    assertEquals("Not connected to the server.", panel.getStatusText());
    assertEquals("—", panel.getPeakMonthLabel().getText());
  }

  @Test
  void opensOnTheCurrentYear() {
    assertEquals(ChartYears.currentYear(), new RegistrationChartPanel(null).getSelectedYear());
  }

  @Test
  void showsNoPeakMonthForAYearWithoutData() {
    RegistrationChartPanel panel = new RegistrationChartPanel(null);
    List<MonthlyCount> counts = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      counts.add(new MonthlyCount(2026, month, 0L));
    }

    panel.populateFromMonthlyCounts(2026, counts);

    assertEquals(12, panel.getDataset().getColumnCount());
    assertEquals("—", panel.getPeakMonthLabel().getText());
    assertEquals(" ", panel.getStatusText());
  }
}
