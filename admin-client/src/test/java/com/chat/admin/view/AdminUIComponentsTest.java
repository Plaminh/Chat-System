package com.chat.admin.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class AdminUIComponentsTest {
  @Test
  void dateFieldsUseTheSharedIsoDateFormat() {
    JTextField from = AdminUIComponents.createDateField("2025/05/01");
    JTextField to = AdminUIComponents.createDateField("2025-05-31");

    assertEquals("2025-05-01", from.getText());
    LocalDate[] range = AdminUIComponents.parseAndValidateDateRange(null, from, to, false);

    assertNotNull(range);
    assertEquals(LocalDate.of(2025, 5, 1), range[0]);
    assertEquals(LocalDate.of(2025, 5, 31), range[1]);
  }
}
