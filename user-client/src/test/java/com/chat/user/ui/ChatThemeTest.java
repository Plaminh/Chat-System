package com.chat.user.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ChatThemeTest {
  @Test
  void providesTheProjectDateAndDateTimeFormats() {
    LocalDate date = LocalDate.of(2026, 11, 7);
    LocalDateTime dateTime = LocalDateTime.of(2026, 11, 7, 20, 33);

    assertEquals("2026-11-07", ChatTheme.DATE_FMT.format(date));
    assertEquals("2026-11-07 20:33", ChatTheme.DATETIME_FMT.format(dateTime));
    assertEquals(date, LocalDate.parse("2026-11-07", ChatTheme.DATE_INPUT_FMT));
  }

  @Test
  void keepsStandardControlsAtTheSharedHeight() {
    assertEquals(
        ChatTheme.BUTTON_HEIGHT,
        ChatTheme.createPrimaryButton("Continue").getPreferredSize().height);
    assertEquals(
        ChatTheme.BUTTON_HEIGHT,
        ChatTheme.createTextField(12).getPreferredSize().height);
    assertEquals(36, ChatTheme.BUTTON_HEIGHT);
  }

  @Test
  void usesTheSharedPaletteForPrimaryActions() {
    assertEquals(ChatTheme.PRIMARY, ChatTheme.createPrimaryButton("Continue").getBackground());
    assertEquals(ChatTheme.SURFACE_WHITE, ChatTheme.createCardPanel().getBackground());
  }
}
