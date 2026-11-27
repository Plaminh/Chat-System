package com.chat.admin.view;

import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Unified Modern Date Range Picker Bar with 1-click Presets and 2-Click Popover Calendar.
 */
public final class DateRangePickerBar extends JPanel {
  private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String[] DAY_HEADERS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

  private LocalDate startDate;
  private LocalDate endDate;
  private BiConsumer<LocalDate, LocalDate> onRangeChanged;

  private final JButton rangeDisplayBtn = new JButton();
  private final List<JButton> presetButtons = new ArrayList<>();
  private JButton activePresetButton = null;

  public DateRangePickerBar(LocalDate initialStart, LocalDate initialEnd) {
    this(initialStart, initialEnd, null);
  }

  public DateRangePickerBar(
      LocalDate initialStart,
      LocalDate initialEnd,
      BiConsumer<LocalDate, LocalDate> onRangeChanged) {
    this.startDate = initialStart;
    this.endDate = initialEnd;
    this.onRangeChanged = onRangeChanged;

    setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
    setOpaque(false);

    styleRangeButton();
    rangeDisplayBtn.addActionListener(e -> showRangePopup());
    add(rangeDisplayBtn);

    add(Box.createHorizontalStrut(4));
    createPresets();

    updateDisplayLabel();
  }

  public void setOnRangeChanged(BiConsumer<LocalDate, LocalDate> listener) {
    this.onRangeChanged = listener;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setDateRange(LocalDate start, LocalDate end, boolean fireEvent) {
    this.startDate = start;
    this.endDate = end;
    updateDisplayLabel();
    clearPresetHighlights();
    if (fireEvent && onRangeChanged != null) {
      onRangeChanged.accept(startDate, endDate);
    }
  }

  private void styleRangeButton() {
    rangeDisplayBtn.setFont(ChatTheme.FONT_BODY_BOLD);
    rangeDisplayBtn.setFocusPainted(false);
    rangeDisplayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    rangeDisplayBtn.setBackground(ChatTheme.SURFACE_WHITE);
    rangeDisplayBtn.setForeground(ChatTheme.TEXT_PRIMARY);
    rangeDisplayBtn.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCBD5E1), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
  }

  private void createPresets() {
    LocalDate today = LocalDate.now();

    JButton todayBtn =
        createPresetBtn("Hôm nay", today, today);
    JButton d7Btn =
        createPresetBtn("7 ngày", today.minusDays(6), today);
    JButton d30Btn =
        createPresetBtn("30 ngày", today.minusDays(29), today);
    JButton monthBtn =
        createPresetBtn("Tháng này", today.withDayOfMonth(1), today);
    JButton yearBtn =
        createPresetBtn("Năm nay", today.withDayOfYear(1), today);
    JButton allBtn =
        createPresetBtn("Toàn bộ", LocalDate.of(2025, 1, 1), today);

    presetButtons.add(todayBtn);
    presetButtons.add(d7Btn);
    presetButtons.add(d30Btn);
    presetButtons.add(monthBtn);
    presetButtons.add(yearBtn);
    presetButtons.add(allBtn);

    for (JButton btn : presetButtons) {
      add(btn);
    }

    // Default active preset check
    if (LocalDate.of(2025, 1, 1).equals(startDate) && today.equals(endDate)) {
      highlightPreset(allBtn);
    }
  }

  private JButton createPresetBtn(String title, LocalDate start, LocalDate end) {
    JButton btn = new JButton(title);
    btn.setFont(ChatTheme.FONT_SMALL);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    stylePresetButton(btn, false);

    btn.addActionListener(
        e -> {
          this.startDate = start;
          this.endDate = end;
          updateDisplayLabel();
          highlightPreset(btn);
          if (onRangeChanged != null) {
            onRangeChanged.accept(startDate, endDate);
          }
        });
    return btn;
  }

  private void highlightPreset(JButton target) {
    activePresetButton = target;
    for (JButton b : presetButtons) {
      stylePresetButton(b, b == target);
    }
  }

  private void clearPresetHighlights() {
    activePresetButton = null;
    for (JButton b : presetButtons) {
      stylePresetButton(b, false);
    }
  }

  private void stylePresetButton(JButton btn, boolean active) {
    if (active) {
      btn.setBackground(ChatTheme.PRIMARY);
      btn.setForeground(Color.WHITE);
      btn.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(ChatTheme.PRIMARY, 1, true),
              BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    } else {
      btn.setBackground(ChatTheme.SURFACE_WHITE);
      btn.setForeground(ChatTheme.TEXT_SECONDARY);
      btn.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(ChatTheme.BORDER, 1, true),
              BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }
  }

  private void updateDisplayLabel() {
    String startStr = startDate != null ? startDate.format(ISO_FMT) : "Bất kỳ";
    String endStr = endDate != null ? endDate.format(ISO_FMT) : "Bất kỳ";

    String duration = "";
    if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
      long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
      duration = " (" + days + " ngày)";
    }

    rangeDisplayBtn.setText("📅  " + startStr + "  ➜  " + endStr + duration + "  ▾");
  }

  private void showRangePopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.setBorder(BorderFactory.createLineBorder(new Color(0x94A3B8), 1));
    popup.setBackground(ChatTheme.SURFACE_WHITE);

    RangePickerPanel pickerPanel =
        new RangePickerPanel(
            startDate,
            endDate,
            (newStart, newEnd) -> {
              this.startDate = newStart;
              this.endDate = newEnd;
              updateDisplayLabel();
              clearPresetHighlights();
              popup.setVisible(false);
              if (onRangeChanged != null) {
                onRangeChanged.accept(startDate, endDate);
              }
            },
            () -> popup.setVisible(false));

    popup.add(pickerPanel);
    popup.show(rangeDisplayBtn, 0, rangeDisplayBtn.getHeight() + 4);
  }

  RangePickerPanel createPickerPanel(BiConsumer<LocalDate, LocalDate> onApply, Runnable onCancel) {
    return new RangePickerPanel(startDate, endDate, onApply, onCancel);
  }

  /** Internal Range Picker Dialog/Popover Panel. */
  static final class RangePickerPanel extends JPanel {
    enum TargetField {
      START,
      END
    }

    private YearMonth currentYearMonth;
    private LocalDate tempStart;
    private LocalDate tempEnd;
    private TargetField currentTarget = TargetField.START;
    private boolean isSyncing = false;

    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysGrid = new JPanel(new GridLayout(0, 7, 3, 3));
    private final JTextField startInput = new JTextField(9);
    private final JTextField endInput = new JTextField(9);
    private final JLabel startLbl = new JLabel("Từ:", SwingConstants.RIGHT);
    private final JLabel endLbl = new JLabel("Đến:", SwingConstants.RIGHT);
    private final JLabel errorLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton applyBtn = ChatTheme.createPrimaryButton("Áp dụng");

    private final BiConsumer<LocalDate, LocalDate> onApply;
    private final Runnable onCancel;

    RangePickerPanel(
        LocalDate start,
        LocalDate end,
        BiConsumer<LocalDate, LocalDate> onApply,
        Runnable onCancel) {
      this.tempStart = start != null ? start : LocalDate.now().minusDays(30);
      this.tempEnd = end != null ? end : LocalDate.now();
      this.onApply = onApply;
      this.onCancel = onCancel;
      this.currentYearMonth = YearMonth.from(tempEnd != null ? tempEnd : LocalDate.now());

      setLayout(new BorderLayout(0, 10));
      setBackground(ChatTheme.SURFACE_WHITE);
      setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
      setPreferredSize(new Dimension(370, 420));

      add(buildHeaderNav(), BorderLayout.NORTH);
      add(buildCalendarBody(), BorderLayout.CENTER);
      add(buildFooterControls(), BorderLayout.SOUTH);

      initInteractions();
      syncInputs();
      validateAndRender();
    }

    private void initInteractions() {
      startInput.setToolTipText("Chọn hoặc nhập ngày bắt đầu (yyyy-MM-dd)");
      endInput.setToolTipText("Chọn hoặc nhập ngày kết thúc (yyyy-MM-dd)");

      startInput.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
              currentTarget = TargetField.START;
              validateAndRender();
            }
          });
      endInput.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
              currentTarget = TargetField.END;
              validateAndRender();
            }
          });

      startInput.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              currentTarget = TargetField.START;
              validateAndRender();
            }
          });
      endInput.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              currentTarget = TargetField.END;
              validateAndRender();
            }
          });

      startLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      startLbl.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              currentTarget = TargetField.START;
              startInput.requestFocusInWindow();
              validateAndRender();
            }
          });

      endLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      endLbl.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              currentTarget = TargetField.END;
              endInput.requestFocusInWindow();
              validateAndRender();
            }
          });

      startInput
          .getDocument()
          .addDocumentListener(
              new SimpleDocumentListener(() -> onInputTyped(TargetField.START)));
      endInput
          .getDocument()
          .addDocumentListener(
              new SimpleDocumentListener(() -> onInputTyped(TargetField.END)));
    }

    private void onInputTyped(TargetField field) {
      if (isSyncing) {
        return;
      }
      currentTarget = field;
      String text = (field == TargetField.START ? startInput : endInput).getText().trim();
      if (text.length() == 10) {
        try {
          LocalDate parsed = LocalDate.parse(text, ISO_FMT);
          if (field == TargetField.START) {
            tempStart = parsed;
          } else {
            tempEnd = parsed;
          }
          currentYearMonth = YearMonth.from(parsed);
        } catch (DateTimeParseException ignored) {
        }
      }
      validateAndRender();
    }

    private JPanel buildHeaderNav() {
      JPanel header = new JPanel(new BorderLayout(6, 0));
      header.setOpaque(false);

      JButton prevYearBtn =
          createNavBtn(
              "<<",
              "Lùi 1 năm",
              () -> {
                currentYearMonth = currentYearMonth.minusYears(1);
                renderDays();
              });
      JButton prevMonthBtn =
          createNavBtn(
              "<",
              "Lùi 1 tháng",
              () -> {
                currentYearMonth = currentYearMonth.minusMonths(1);
                renderDays();
              });

      JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
      leftNav.setOpaque(false);
      leftNav.add(prevYearBtn);
      leftNav.add(prevMonthBtn);

      monthLabel.setFont(ChatTheme.FONT_BODY_BOLD);
      monthLabel.setForeground(ChatTheme.TEXT_PRIMARY);

      JButton nextMonthBtn =
          createNavBtn(
              ">",
              "Tiến 1 tháng",
              () -> {
                currentYearMonth = currentYearMonth.plusMonths(1);
                renderDays();
              });
      JButton nextYearBtn =
          createNavBtn(
              ">>",
              "Tiến 1 năm",
              () -> {
                currentYearMonth = currentYearMonth.plusYears(1);
                renderDays();
              });

      JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
      rightNav.setOpaque(false);
      rightNav.add(nextMonthBtn);
      rightNav.add(nextYearBtn);

      header.add(leftNav, BorderLayout.WEST);
      header.add(monthLabel, BorderLayout.CENTER);
      header.add(rightNav, BorderLayout.EAST);
      return header;
    }

    private JButton createNavBtn(String text, String tip, Runnable action) {
      JButton btn = ChatTheme.createSecondaryButton(text);
      btn.setFont(ChatTheme.FONT_SMALL);
      btn.setPreferredSize(new Dimension(32, 26));
      btn.setMargin(new Insets(1, 1, 1, 1));
      btn.setToolTipText(tip);
      btn.addActionListener(e -> action.run());
      return btn;
    }

    private JPanel buildCalendarBody() {
      JPanel body = new JPanel(new BorderLayout(0, 4));
      body.setOpaque(false);

      JPanel headersPanel = new JPanel(new GridLayout(1, 7, 3, 0));
      headersPanel.setOpaque(false);
      for (String h : DAY_HEADERS) {
        JLabel lbl = new JLabel(h, SwingConstants.CENTER);
        lbl.setFont(ChatTheme.FONT_SMALL);
        lbl.setForeground(new Color(0x64748B));
        headersPanel.add(lbl);
      }

      daysGrid.setOpaque(false);
      body.add(headersPanel, BorderLayout.NORTH);
      body.add(daysGrid, BorderLayout.CENTER);
      return body;
    }

    private void renderDays() {
      monthLabel.setText(
          "Tháng " + currentYearMonth.getMonthValue() + ", " + currentYearMonth.getYear());
      daysGrid.removeAll();

      LocalDate firstOfMonth = currentYearMonth.atDay(1);
      int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun
      int leadingEmpty = dayOfWeek - 1;

      for (int i = 0; i < leadingEmpty; i++) {
        daysGrid.add(new JLabel(""));
      }

      int daysInMonth = currentYearMonth.lengthOfMonth();
      for (int day = 1; day <= daysInMonth; day++) {
        LocalDate date = currentYearMonth.atDay(day);
        JButton dayBtn = createDayButton(date);
        daysGrid.add(dayBtn);
      }

      daysGrid.revalidate();
      daysGrid.repaint();
    }

    private JButton createDayButton(LocalDate date) {
      boolean hasError = tempStart != null && tempEnd != null && tempStart.isAfter(tempEnd);
      boolean isStart = date.equals(tempStart);
      boolean isEnd = date.equals(tempEnd);
      boolean inRange =
          !hasError
              && tempStart != null
              && tempEnd != null
              && date.isAfter(tempStart)
              && date.isBefore(tempEnd);

      Color highlightColor = hasError ? new Color(0xEF4444) : ChatTheme.PRIMARY;

      JButton btn =
          new JButton(String.valueOf(date.getDayOfMonth())) {
            @Override
            protected void paintComponent(Graphics g) {
              if (isStart || isEnd) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(highlightColor);
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2.dispose();
              } else if (inRange) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ChatTheme.PRIMARY_SOFT_BG);
                g2.fillRect(0, 2, getWidth(), getHeight() - 4);
                g2.dispose();
              }
              super.paintComponent(g);
            }
          };

      btn.setFont(ChatTheme.FONT_SMALL);
      btn.setMargin(new Insets(1, 1, 1, 1));
      btn.setFocusPainted(false);
      btn.setContentAreaFilled(false);
      btn.setBorderPainted(false);
      btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      if (isStart || isEnd) {
        btn.setForeground(Color.WHITE);
      } else if (inRange) {
        btn.setForeground(ChatTheme.PRIMARY);
      } else if (date.equals(LocalDate.now())) {
        btn.setForeground(ChatTheme.PRIMARY);
        btn.setFont(ChatTheme.FONT_BODY_BOLD);
      } else {
        btn.setForeground(ChatTheme.TEXT_PRIMARY);
      }

      btn.addActionListener(e -> onDayClicked(date));
      return btn;
    }

    private void onDayClicked(LocalDate date) {
      if (currentTarget == TargetField.START) {
        tempStart = date;
        currentTarget = TargetField.END;
      } else {
        tempEnd = date;
        currentTarget = TargetField.START;
      }
      syncInputs();
      validateAndRender();
    }

    private JPanel buildFooterControls() {
      JPanel footer = new JPanel(new BorderLayout(0, 6));
      footer.setOpaque(false);
      footer.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createMatteBorder(1, 0, 0, 0, ChatTheme.BORDER),
              BorderFactory.createEmptyBorder(8, 0, 0, 0)));

      // Inputs row
      JPanel inputsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
      inputsRow.setOpaque(false);
      startInput.setFont(ChatTheme.FONT_SMALL);
      endInput.setFont(ChatTheme.FONT_SMALL);
      startInput.setPreferredSize(new Dimension(96, 28));
      endInput.setPreferredSize(new Dimension(96, 28));

      inputsRow.add(startLbl);
      inputsRow.add(startInput);
      JLabel arrowLbl = new JLabel("➜");
      arrowLbl.setForeground(ChatTheme.TEXT_MUTED);
      inputsRow.add(arrowLbl);
      inputsRow.add(endLbl);
      inputsRow.add(endInput);

      // Error message label
      errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
      errorLabel.setForeground(new Color(0xEF4444));
      errorLabel.setVisible(false);

      // Center container for inputs and error
      JPanel centerControls = new JPanel(new BorderLayout(0, 4));
      centerControls.setOpaque(false);
      centerControls.add(inputsRow, BorderLayout.NORTH);
      centerControls.add(errorLabel, BorderLayout.SOUTH);

      // Buttons row
      JPanel btnsRow = new JPanel(new BorderLayout());
      btnsRow.setOpaque(false);

      JButton todayBtn = ChatTheme.createSecondaryButton("Hôm nay");
      todayBtn.setFont(ChatTheme.FONT_SMALL);
      todayBtn.addActionListener(
          e -> {
            tempStart = LocalDate.now();
            tempEnd = LocalDate.now();
            currentYearMonth = YearMonth.from(tempStart);
            currentTarget = TargetField.START;
            syncInputs();
            validateAndRender();
          });

      JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
      rightActions.setOpaque(false);

      JButton cancelBtn = ChatTheme.createSecondaryButton("Đóng");
      cancelBtn.setFont(ChatTheme.FONT_SMALL);
      cancelBtn.addActionListener(e -> onCancel.run());

      applyBtn.setFont(ChatTheme.FONT_SMALL);
      applyBtn.addActionListener(e -> applySelection());

      rightActions.add(cancelBtn);
      rightActions.add(applyBtn);

      btnsRow.add(todayBtn, BorderLayout.WEST);
      btnsRow.add(rightActions, BorderLayout.EAST);

      footer.add(centerControls, BorderLayout.NORTH);
      footer.add(btnsRow, BorderLayout.SOUTH);
      return footer;
    }

    private void syncInputs() {
      isSyncing = true;
      try {
        startInput.setText(tempStart != null ? tempStart.format(ISO_FMT) : "");
        endInput.setText(tempEnd != null ? tempEnd.format(ISO_FMT) : "");
      } finally {
        isSyncing = false;
      }
    }

    private void validateAndRender() {
      boolean hasError = false;
      String errorMsg = "";

      String sText = startInput.getText().trim();
      String eText = endInput.getText().trim();

      LocalDate parsedStart = tempStart;
      LocalDate parsedEnd = tempEnd;

      if (!sText.isEmpty()) {
        try {
          parsedStart = LocalDate.parse(sText, ISO_FMT);
        } catch (DateTimeParseException ex) {
          hasError = true;
          errorMsg = "⚠️ Định dạng ngày bắt đầu không hợp lệ (yyyy-MM-dd)!";
        }
      }

      if (!eText.isEmpty() && !hasError) {
        try {
          parsedEnd = LocalDate.parse(eText, ISO_FMT);
        } catch (DateTimeParseException ex) {
          hasError = true;
          errorMsg = "⚠️ Định dạng ngày kết thúc không hợp lệ (yyyy-MM-dd)!";
        }
      }

      if (!hasError && parsedStart != null && parsedEnd != null && parsedStart.isAfter(parsedEnd)) {
        hasError = true;
        errorMsg = "⚠️ Ngày bắt đầu không thể sau ngày kết thúc. Vui lòng chọn lại!";
      }

      if (hasError) {
        errorLabel.setText(errorMsg);
        errorLabel.setVisible(true);
        applyBtn.setEnabled(false);
      } else {
        errorLabel.setVisible(false);
        applyBtn.setEnabled(true);
      }

      updateInputStyles(hasError);
      renderDays();
    }

    private void updateInputStyles(boolean hasError) {
      Color errColor = new Color(0xEF4444);
      Color activeBorder = ChatTheme.PRIMARY;
      Color normalBorder = ChatTheme.BORDER;

      if (hasError) {
        startInput.putClientProperty("JComponent.outline", "error");
        endInput.putClientProperty("JComponent.outline", "error");
        startInput.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(errColor, 2, true),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        endInput.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(errColor, 2, true),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        startLbl.setForeground(errColor);
        endLbl.setForeground(errColor);
      } else {
        startInput.putClientProperty(
            "JComponent.outline", currentTarget == TargetField.START ? "primary" : null);
        endInput.putClientProperty(
            "JComponent.outline", currentTarget == TargetField.END ? "primary" : null);

        startInput.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    currentTarget == TargetField.START ? activeBorder : normalBorder,
                    currentTarget == TargetField.START ? 2 : 1,
                    true),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        endInput.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    currentTarget == TargetField.END ? activeBorder : normalBorder,
                    currentTarget == TargetField.END ? 2 : 1,
                    true),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));

        startLbl.setForeground(
            currentTarget == TargetField.START ? ChatTheme.PRIMARY : ChatTheme.TEXT_SECONDARY);
        startLbl.setFont(
            currentTarget == TargetField.START ? ChatTheme.FONT_BODY_BOLD : ChatTheme.FONT_SMALL);
        endLbl.setForeground(
            currentTarget == TargetField.END ? ChatTheme.PRIMARY : ChatTheme.TEXT_SECONDARY);
        endLbl.setFont(
            currentTarget == TargetField.END ? ChatTheme.FONT_BODY_BOLD : ChatTheme.FONT_SMALL);
      }
    }

    private void applySelection() {
      if (tempStart != null && tempEnd != null && tempStart.isAfter(tempEnd)) {
        validateAndRender();
        return;
      }

      LocalDate finalStart = tempStart != null ? tempStart : tempEnd;
      LocalDate finalEnd = tempEnd != null ? tempEnd : tempStart;
      if (finalStart == null) {
        finalStart = LocalDate.now();
        finalEnd = LocalDate.now();
      }

      onApply.accept(finalStart, finalEnd);
    }

    // Testing helper accessors
    TargetField getCurrentTarget() {
      return currentTarget;
    }

    void setCurrentTarget(TargetField target) {
      this.currentTarget = target;
      validateAndRender();
    }

    LocalDate getTempStart() {
      return tempStart;
    }

    LocalDate getTempEnd() {
      return tempEnd;
    }

    JTextField getStartInput() {
      return startInput;
    }

    JTextField getEndInput() {
      return endInput;
    }

    JLabel getErrorLabel() {
      return errorLabel;
    }

    JButton getApplyBtn() {
      return applyBtn;
    }

    void onDayClickedForTest(LocalDate date) {
      onDayClicked(date);
    }
  }

  private static class SimpleDocumentListener implements DocumentListener {
    private final Runnable callback;

    SimpleDocumentListener(Runnable callback) {
      this.callback = callback;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      callback.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      callback.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      callback.run();
    }
  }
}
