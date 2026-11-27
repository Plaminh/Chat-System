package com.chat.admin.view;

import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

/**
 * Lightweight, zero-dependency calendar popup dialog for selecting dates in Swing.
 */
public final class MiniCalendarPopup extends JPopupMenu {
  private final Consumer<LocalDate> onDateSelected;
  private YearMonth currentYearMonth;
  private final JPanel calendarDaysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
  private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);

  public static void showPopup(
      JComponent invoker, LocalDate initialDate, Consumer<LocalDate> onDateSelected) {
    MiniCalendarPopup popup = new MiniCalendarPopup(initialDate, onDateSelected);
    popup.show(invoker, 0, invoker.getHeight() + 2);
  }

  private MiniCalendarPopup(LocalDate initialDate, Consumer<LocalDate> onDateSelected) {
    this.onDateSelected = onDateSelected;
    this.currentYearMonth = initialDate != null ? YearMonth.from(initialDate) : YearMonth.now();

    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ChatTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    setBackground(ChatTheme.SURFACE_WHITE);
    setLayout(new BorderLayout(0, 8));

    // Top: Navigation (Prev Month, Month/Year Label, Next Month)
    JPanel header = new JPanel(new BorderLayout(4, 0));
    header.setOpaque(false);

    JButton prevBtn = ChatTheme.createSecondaryButton("◀");
    prevBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
    prevBtn.setPreferredSize(new Dimension(30, 26));
    prevBtn.addActionListener(
        e -> {
          currentYearMonth = currentYearMonth.minusMonths(1);
          refreshCalendar();
        });

    JButton nextBtn = ChatTheme.createSecondaryButton("▶");
    nextBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
    nextBtn.setPreferredSize(new Dimension(30, 26));
    nextBtn.addActionListener(
        e -> {
          currentYearMonth = currentYearMonth.plusMonths(1);
          refreshCalendar();
        });

    monthYearLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    monthYearLabel.setForeground(ChatTheme.TEXT_PRIMARY);

    header.add(prevBtn, BorderLayout.WEST);
    header.add(monthYearLabel, BorderLayout.CENTER);
    header.add(nextBtn, BorderLayout.EAST);

    // Center: Day of Week Headers + Calendar Grid
    JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
    centerPanel.setOpaque(false);

    JPanel dowPanel = new JPanel(new GridLayout(1, 7, 2, 2));
    dowPanel.setOpaque(false);
    String[] dows = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    for (String dow : dows) {
      JLabel lbl = new JLabel(dow, SwingConstants.CENTER);
      lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
      lbl.setForeground(ChatTheme.TEXT_MUTED);
      dowPanel.add(lbl);
    }

    calendarDaysPanel.setOpaque(false);

    centerPanel.add(dowPanel, BorderLayout.NORTH);
    centerPanel.add(calendarDaysPanel, BorderLayout.CENTER);

    // Bottom: Today button
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
    footer.setOpaque(false);
    JButton todayBtn = ChatTheme.createSecondaryButton("Hôm nay");
    todayBtn.setFont(ChatTheme.FONT_SMALL);
    todayBtn.addActionListener(
        e -> {
          if (onDateSelected != null) {
            onDateSelected.accept(LocalDate.now());
          }
          setVisible(false);
        });
    footer.add(todayBtn);

    add(header, BorderLayout.NORTH);
    add(centerPanel, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);

    refreshCalendar();
  }

  private void refreshCalendar() {
    monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    calendarDaysPanel.removeAll();

    LocalDate firstDay = currentYearMonth.atDay(1);
    int dayOfWeek = firstDay.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
    int daysInMonth = currentYearMonth.lengthOfMonth();

    // Empty cells before first day
    for (int i = 1; i < dayOfWeek; i++) {
      JLabel empty = new JLabel("");
      calendarDaysPanel.add(empty);
    }

    LocalDate today = LocalDate.now();
    for (int d = 1; d <= daysInMonth; d++) {
      int day = d;
      LocalDate date = currentYearMonth.atDay(day);
      JButton dayBtn = new JButton(String.valueOf(day));
      dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
      dayBtn.setPreferredSize(new Dimension(30, 26));
      dayBtn.setMargin(new Insets(1, 1, 1, 1));
      dayBtn.setFocusPainted(false);

      if (date.equals(today)) {
        dayBtn.setBackground(ChatTheme.PRIMARY);
        dayBtn.setForeground(Color.WHITE);
        dayBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
      } else {
        dayBtn.setBackground(ChatTheme.SURFACE_WHITE);
        dayBtn.setForeground(ChatTheme.TEXT_PRIMARY);
      }

      dayBtn.addActionListener(
          e -> {
            if (onDateSelected != null) {
              onDateSelected.accept(date);
            }
            setVisible(false);
          });

      calendarDaysPanel.add(dayBtn);
    }

    calendarDaysPanel.revalidate();
    calendarDaysPanel.repaint();
  }
}
