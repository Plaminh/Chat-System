package com.chat.admin.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DateRangePickerBarTest {

  @Test
  void testInitialStateAndPresets() {
    LocalDate start = LocalDate.of(2026, 1, 1);
    LocalDate end = LocalDate.of(2026, 1, 31);
    AtomicReference<LocalDate> callbackStart = new AtomicReference<>();
    AtomicReference<LocalDate> callbackEnd = new AtomicReference<>();

    DateRangePickerBar bar =
        new DateRangePickerBar(
            start,
            end,
            (s, e) -> {
              callbackStart.set(s);
              callbackEnd.set(e);
            });

    assertEquals(start, bar.getStartDate());
    assertEquals(end, bar.getEndDate());

    // setDateRange with fireEvent
    LocalDate newStart = LocalDate.of(2026, 2, 1);
    LocalDate newEnd = LocalDate.of(2026, 2, 28);
    bar.setDateRange(newStart, newEnd, true);

    assertEquals(newStart, bar.getStartDate());
    assertEquals(newEnd, bar.getEndDate());
    assertEquals(newStart, callbackStart.get());
    assertEquals(newEnd, callbackEnd.get());
  }

  @Test
  void testRangePickerPanelPreservesBothDatesWhenSelectingRightThenLeft() {
    LocalDate initialStart = LocalDate.of(2026, 5, 10);
    LocalDate initialEnd = LocalDate.of(2026, 5, 20);

    DateRangePickerBar bar = new DateRangePickerBar(initialStart, initialEnd);
    DateRangePickerBar.RangePickerPanel panel =
        bar.createPickerPanel((s, e) -> {}, () -> {});

    assertEquals(initialStart, panel.getTempStart());
    assertEquals(initialEnd, panel.getTempEnd());

    // Select right date (end date) first
    panel.setCurrentTarget(DateRangePickerBar.RangePickerPanel.TargetField.END);
    LocalDate newEnd = LocalDate.of(2026, 5, 25);
    panel.onDayClickedForTest(newEnd);

    // Verify tempEnd is updated AND tempStart is NOT wiped out!
    assertEquals(newEnd, panel.getTempEnd());
    assertEquals(initialStart, panel.getTempStart());
    assertFalse(panel.getErrorLabel().isVisible());
    assertTrue(panel.getApplyBtn().isEnabled());

    // Now select left date (start date)
    panel.setCurrentTarget(DateRangePickerBar.RangePickerPanel.TargetField.START);
    LocalDate newStart = LocalDate.of(2026, 5, 15);
    panel.onDayClickedForTest(newStart);

    // Verify tempStart is updated AND tempEnd is STILL 2026-05-25!
    assertEquals(newStart, panel.getTempStart());
    assertEquals(newEnd, panel.getTempEnd());
    assertFalse(panel.getErrorLabel().isVisible());
    assertTrue(panel.getApplyBtn().isEnabled());
  }

  @Test
  void testRangePickerPanelHighlightsErrorWhenStartDateAfterEndDate() {
    LocalDate initialStart = LocalDate.of(2026, 5, 10);
    LocalDate initialEnd = LocalDate.of(2026, 5, 20);

    DateRangePickerBar bar = new DateRangePickerBar(initialStart, initialEnd);
    DateRangePickerBar.RangePickerPanel panel =
        bar.createPickerPanel((s, e) -> {}, () -> {});

    // User chooses end date to be 2026-05-05 (which is BEFORE tempStart 2026-05-10)
    panel.setCurrentTarget(DateRangePickerBar.RangePickerPanel.TargetField.END);
    LocalDate invalidEnd = LocalDate.of(2026, 5, 5);
    panel.onDayClickedForTest(invalidEnd);

    // Both dates must be retained, error must be flagged, apply button must be disabled
    assertEquals(initialStart, panel.getTempStart());
    assertEquals(invalidEnd, panel.getTempEnd());
    assertTrue(panel.getErrorLabel().isVisible(), "Error label should be visible");
    assertFalse(panel.getApplyBtn().isEnabled(), "Apply button should be disabled when start > end");
    assertEquals("error", panel.getStartInput().getClientProperty("JComponent.outline"));
    assertEquals("error", panel.getEndInput().getClientProperty("JComponent.outline"));

    // User picks a valid start date: 2026-05-01 (<= 2026-05-05)
    panel.setCurrentTarget(DateRangePickerBar.RangePickerPanel.TargetField.START);
    LocalDate validStart = LocalDate.of(2026, 5, 1);
    panel.onDayClickedForTest(validStart);

    // Error must clear
    assertEquals(validStart, panel.getTempStart());
    assertEquals(invalidEnd, panel.getTempEnd());
    assertFalse(panel.getErrorLabel().isVisible(), "Error label should disappear when valid");
    assertTrue(panel.getApplyBtn().isEnabled(), "Apply button should be enabled when valid");
  }
}
