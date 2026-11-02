package com.chat.admin.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.MaskFormatter;

/**
 * Shared Desktop Admin Studio design tokens, programmatic Java2D vector icons, and reusable Swing
 * component factories for the Admin Client UI.
 */
public final class AdminUIStyle {
  // Core Canvas & Surfaces (from Stitch DESIGN.md)
  public static final Color CANVAS_BG = new Color(0xF8FAFC);
  public static final Color SURFACE_WHITE = new Color(0xFFFFFF);
  public static final Color SIDEBAR_BG = new Color(0x0F172A);
  public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
  public static final Color SIDEBAR_DIVIDER = new Color(0x334155);

  // Accents & Semantic Tones
  public static final Color PRIMARY = new Color(0x2563EB);
  public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
  public static final Color PRIMARY_SOFT_BG = new Color(0xEFF6FF);

  public static final Color SUCCESS_TEXT = new Color(0x065F46);
  public static final Color SUCCESS_BG = new Color(0xECFDF5);
  public static final Color SUCCESS_DOT = new Color(0x10B981);

  public static final Color DANGER_TEXT = new Color(0x991B1B);
  public static final Color DANGER_BG = new Color(0xFEF2F2);
  public static final Color DANGER_BTN = new Color(0xDC2626);

  public static final Color WARNING_TEXT = new Color(0x92400E);
  public static final Color WARNING_BG = new Color(0xFFFBEB);

  // Text & Borders
  public static final Color TEXT_PRIMARY = new Color(0x0F172A);
  public static final Color TEXT_SECONDARY = new Color(0x475569);
  public static final Color TEXT_MUTED = new Color(0x64748B);
  public static final Color BORDER_COLOR = new Color(0xE2E8F0);
  public static final Color TABLE_ALT_ROW = new Color(0xF8FAFC);
  public static final Color TABLE_SELECTION_BG = new Color(0xDBEAFE);

  // Typography
  public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
  public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
  public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 11);
  public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
  public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
  public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
  public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD, 11);
  public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  public static final DateTimeFormatter DATE_INPUT_FMT =
      DateTimeFormatter.ofPattern("uuuu/MM/dd").withResolverStyle(ResolverStyle.STRICT);
  public static final DateTimeFormatter DATETIME_FMT =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
  public static final DateTimeFormatter FULL_DATETIME_FMT =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

  private AdminUIStyle() {}

  /** Applies global FlatLaf rounded corners and clean table defaults. */
  public static void applyGlobalThemeDefaults() {
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("Table.rowHeight", 36);
    UIManager.put("Table.showHorizontalLines", true);
    UIManager.put("Table.showVerticalLines", false);
    UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
    UIManager.put("Table.gridColor", BORDER_COLOR);
  }

  public static Border createCardBorder() {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(14, 16, 14, 16));
  }

  public static JPanel createCardPanel(BorderLayout layout) {
    JPanel card = new JPanel(layout);
    card.setBackground(SURFACE_WHITE);
    card.setBorder(createCardBorder());
    return card;
  }

  public static JPanel createKpiCard(String label, JLabel valueLabel, String caption, Color accent) {
    JPanel card = new JPanel(new BorderLayout(4, 4));
    card.setBackground(SURFACE_WHITE);
    card.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14))));

    JLabel title = new JLabel(label.toUpperCase());
    title.setFont(FONT_SECTION);
    title.setForeground(TEXT_MUTED);

    valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    valueLabel.setForeground(TEXT_PRIMARY);

    JLabel sub = new JLabel(caption);
    sub.setFont(FONT_SMALL);
    sub.setForeground(TEXT_SECONDARY);

    card.add(title, BorderLayout.NORTH);
    card.add(valueLabel, BorderLayout.CENTER);
    card.add(sub, BorderLayout.SOUTH);
    card.setMinimumSize(new Dimension(160, 72));
    return card;
  }

  /**
   * Creates a responsive FlowLayout that recalculates its preferred height when components wrap onto
   * multiple lines during window resizing.
   */
  public static FlowLayout createWrapFlowLayout(int align, int hgap, int vgap) {
    return new FlowLayout(align, hgap, vgap) {
      @Override
      public Dimension preferredLayoutSize(Container target) {
        return computeSize(target, true);
      }

      @Override
      public Dimension minimumLayoutSize(Container target) {
        Dimension min = computeSize(target, false);
        min.width = 0;
        return min;
      }

      private Dimension computeSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
          int targetWidth = target.getSize().width;
          Container parent = target.getParent();
          while (targetWidth <= 0 && parent != null) {
            targetWidth = parent.getSize().width;
            parent = parent.getParent();
          }
          if (targetWidth <= 0) {
            targetWidth = Integer.MAX_VALUE;
          }
          int hgapVal = getHgap();
          int vgapVal = getVgap();
          Insets insets = target.getInsets();
          int horizontalInsetsAndGap = insets.left + insets.right + (hgapVal * 2);
          int maxWidth = Math.max(100, targetWidth - horizontalInsetsAndGap);

          Dimension dim = new Dimension(0, 0);
          int rowWidth = 0;
          int rowHeight = 0;
          int nmembers = target.getComponentCount();

          for (int i = 0; i < nmembers; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
              Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
              if (rowWidth > 0 && rowWidth + hgapVal + d.width > maxWidth) {
                dim.width = Math.max(dim.width, rowWidth);
                if (dim.height > 0) {
                  dim.height += vgapVal;
                }
                dim.height += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
              }
              if (rowWidth != 0) {
                rowWidth += hgapVal;
              }
              rowWidth += d.width;
              rowHeight = Math.max(rowHeight, d.height);
            }
          }
          dim.width = Math.max(dim.width, rowWidth);
          if (dim.height > 0) {
            dim.height += vgapVal;
          }
          dim.height += rowHeight;
          dim.width += horizontalInsetsAndGap;
          dim.height += insets.top + insets.bottom + vgapVal * 2;
          return dim;
        }
      }
    };
  }

  public static JTextField createSearchField(String placeholder, int columns) {
    JTextField field = new JTextField(columns);
    field.setFont(FONT_BODY);
    field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    field.putClientProperty("JTextField.placeholderText", placeholder);
    field.putClientProperty("JTextField.showClearButton", true);
    return field;
  }

  public static JTextField createDateField(String initialValue) {
    MaskFormatter formatter;
    try {
      formatter = new MaskFormatter("####/##/##");
      formatter.setPlaceholderCharacter('_');
    } catch (ParseException ex) {
      throw new IllegalStateException("Invalid date mask pattern", ex);
    }

    JFormattedTextField field =
        new JFormattedTextField(formatter) {
          @Override
          public void setText(String t) {
            putClientProperty("AdminUI.lastAlertKey", null);
            putClientProperty("JComponent.outline", null);
            setForeground(TEXT_PRIMARY);
            if (t == null || t.isBlank()) {
              super.setText("____/__/__");
            } else {
              super.setText(t.trim().replace('-', '/'));
            }
          }
        };
    field.setFocusLostBehavior(JFormattedTextField.PERSIST);
    field.setColumns(11);
    field.setFont(FONT_MONO);
    field.setPreferredSize(new Dimension(Math.max(field.getPreferredSize().width, 118), 34));
    field.setToolTipText("Format: yyyy/MM/dd (e.g. 2025/05/01)");
    field.setText(initialValue);
    return field;
  }

  public static boolean isDateMaskBlank(String raw) {
    if (raw == null) {
      return true;
    }
    String trimmed = raw.trim();
    return trimmed.isEmpty() || "____/__/__".equals(trimmed);
  }

  public static boolean isDateMaskComplete(String raw) {
    if (raw == null) {
      return false;
    }
    String trimmed = raw.trim();
    return trimmed.length() == 10 && !trimmed.contains("_");
  }

  public static void setDateFieldError(JTextField field, boolean hasError, String tooltip) {
    field.putClientProperty("JComponent.outline", hasError ? "error" : null);
    field.setForeground(hasError ? DANGER_TEXT : TEXT_PRIMARY);
    field.setToolTipText(
        hasError && tooltip != null ? tooltip : "Format: yyyy/MM/dd (e.g. 2025/05/01)");
    if (!hasError) {
      field.putClientProperty("AdminUI.lastAlertKey", null);
    }
    field.repaint();
  }

  public static void showDateValidationErrorOnce(
      Component parent, JComponent field, String alertKey, String message, String title) {
    Object prev = field.getClientProperty("AdminUI.lastAlertKey");
    if (Objects.equals(prev, alertKey)) {
      return;
    }
    field.putClientProperty("AdminUI.lastAlertKey", alertKey);
    SwingUtilities.invokeLater(
        () -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE));
  }

  /**
   * Validates a pair of masked date fields ({@code yyyy/MM/dd}). Highlights invalid fields with a
   * red outline and displays a non-duplicate error dialog when a complete date is invalid, when a
   * partial date is left incomplete on Enter/focus-loss, or when {@code fromDate > toDate}.
   *
   * @return a 2-element array {@code [fromDate, toDate]} when valid, or {@code null} if filtering
   *     should be aborted.
   */
  public static LocalDate[] parseAndValidateDateRange(
      Component parent,
      JTextField fromField,
      JTextField toField,
      boolean checkIncomplete) {
    LocalDate fromDate = parseSingleMaskedDate(parent, fromField, "Start Date", checkIncomplete);
    if (Boolean.TRUE.equals(fromField.getClientProperty("AdminUI.abortFilter"))) {
      return null;
    }
    LocalDate toDate = parseSingleMaskedDate(parent, toField, "End Date", checkIncomplete);
    if (Boolean.TRUE.equals(toField.getClientProperty("AdminUI.abortFilter"))) {
      return null;
    }

    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      String fromRaw = fromField.getText().trim();
      String toRaw = toField.getText().trim();
      String rangeKey = "range:" + fromRaw + ">" + toRaw;
      fromField.putClientProperty("JComponent.outline", "error");
      toField.putClientProperty("JComponent.outline", "error");
      fromField.setForeground(DANGER_TEXT);
      toField.setForeground(DANGER_TEXT);
      Object prevRangeKey = toField.getClientProperty("AdminUI.lastRangeAlertKey");
      if (!Objects.equals(prevRangeKey, rangeKey)) {
        toField.putClientProperty("AdminUI.lastRangeAlertKey", rangeKey);
        SwingUtilities.invokeLater(
            () ->
                JOptionPane.showMessageDialog(
                    parent,
                    "Start Date (" + fromRaw + ") cannot be later than End Date (" + toRaw + ").",
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE));
      }
      return null;
    }

    toField.putClientProperty("AdminUI.lastRangeAlertKey", null);
    return new LocalDate[] {fromDate, toDate};
  }

  private static LocalDate parseSingleMaskedDate(
      Component parent, JTextField field, String label, boolean checkIncomplete) {
    String raw = field.getText() == null ? "" : field.getText().trim();
    if (isDateMaskBlank(raw)) {
      setDateFieldError(field, false, null);
      field.putClientProperty("AdminUI.abortFilter", false);
      return null;
    }
    if (!isDateMaskComplete(raw)) {
      if (checkIncomplete) {
        field.putClientProperty("JComponent.outline", "error");
        field.setForeground(DANGER_TEXT);
        field.setToolTipText("Incomplete date. Use yyyy/MM/dd (e.g. 2025/05/01)");
        showDateValidationErrorOnce(
            parent,
            field,
            "incomplete:" + raw,
            label
                + " ('"
                + raw
                + "') is incomplete. Please enter all digits in yyyy/MM/dd format (e.g. 2025/05/01).",
            "Incomplete Date");
      } else {
        setDateFieldError(field, false, null);
      }
      field.putClientProperty("AdminUI.abortFilter", true);
      return null;
    }

    try {
      LocalDate parsed = LocalDate.parse(raw, DATE_INPUT_FMT);
      setDateFieldError(field, false, null);
      field.putClientProperty("AdminUI.abortFilter", false);
      return parsed;
    } catch (DateTimeParseException ex) {
      field.putClientProperty("JComponent.outline", "error");
      field.setForeground(DANGER_TEXT);
      field.setToolTipText("Invalid calendar date: " + raw);
      showDateValidationErrorOnce(
          parent,
          field,
          "invalid:" + raw,
          label
              + " ('"
              + raw
              + "') is not a valid calendar date. Please check the year, month (01-12), and day.",
          "Invalid Date");
      field.putClientProperty("AdminUI.abortFilter", true);
      return null;
    }
  }

  public static void attachDateFieldValidationTriggers(
      JTextField field, Runnable onCommitOrFocusLost) {
    field.addActionListener(e -> onCommitOrFocusLost.run());
    field.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              onCommitOrFocusLost.run();
            }
          }
        });
  }

  public static <T> JComboBox<T> createComboBox(T[] items) {
    JComboBox<T> combo = new JComboBox<>(items);
    combo.setFont(FONT_BODY);
    combo.setBackground(SURFACE_WHITE);
    combo.setPreferredSize(new Dimension(combo.getPreferredSize().width + 16, 34));
    return combo;
  }

  public static JButton createPrimaryButton(String text) {
    JButton btn =
        new JButton(text) {
          @Override
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 12, Math.max(d.height, 34));
          }
        };
    btn.setFont(FONT_BODY_BOLD);
    btn.setBackground(PRIMARY);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  public static JButton createSecondaryButton(String text) {
    JButton btn =
        new JButton(text) {
          @Override
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 12, Math.max(d.height, 34));
          }
        };
    btn.setFont(FONT_BODY_BOLD);
    btn.setBackground(SURFACE_WHITE);
    btn.setForeground(TEXT_PRIMARY);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  public static JButton createDangerButton(String text) {
    JButton btn =
        new JButton(text) {
          @Override
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 12, Math.max(d.height, 34));
          }
        };
    btn.setFont(FONT_BODY_BOLD);
    btn.setBackground(DANGER_BTN);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  public static void styleTable(JTable table) {
    table.setFont(FONT_BODY);
    table.setRowHeight(36);
    table.setFillsViewportHeight(true);
    table.setSelectionBackground(TABLE_SELECTION_BG);
    table.setSelectionForeground(TEXT_PRIMARY);
    table.setGridColor(BORDER_COLOR);
    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(true);

    JTableHeader header = table.getTableHeader();
    header.setFont(FONT_SECTION);
    header.setBackground(CANVAS_BG);
    header.setForeground(TEXT_MUTED);
    header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
    header.setReorderingAllowed(false);

    DefaultTableCellRenderer baseRenderer =
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c =
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
              c.setBackground(row % 2 == 0 ? SURFACE_WHITE : TABLE_ALT_ROW);
              c.setForeground(TEXT_PRIMARY);
            }
            if (c instanceof JComponent jc) {
              jc.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            }
            return c;
          }
        };
    table.setDefaultRenderer(Object.class, baseRenderer);
    table.setDefaultRenderer(Integer.class, createRightAlignedRenderer());
    table.setDefaultRenderer(Long.class, createRightAlignedRenderer());
  }

  public static DefaultTableCellRenderer createRightAlignedRenderer() {
    DefaultTableCellRenderer renderer =
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c =
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
              c.setBackground(row % 2 == 0 ? SURFACE_WHITE : TABLE_ALT_ROW);
              c.setForeground(TEXT_PRIMARY);
            }
            if (c instanceof JComponent jc) {
              jc.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 12));
            }
            return c;
          }
        };
    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    return renderer;
  }

  public static JScrollPane wrapTableInScrollPane(JTable table) {
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(SURFACE_WHITE);
    scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    return scroll;
  }

  /** Renders status cells ("Active", "Locked", "Pending", "Group Admin", "Member") as pill badges. */
  public static DefaultTableCellRenderer createStatusBadgeRenderer() {
    return new DefaultTableCellRenderer() {
      private Color pillBg = new Color(0xF1F5F9);
      private Color rowBg = SURFACE_WHITE;

      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text = value == null ? "" : value.toString();
        super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        rowBg = isSelected ? TABLE_SELECTION_BG : (row % 2 == 0 ? SURFACE_WHITE : TABLE_ALT_ROW);
        setBackground(rowBg);
        setFont(FONT_BADGE);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        String lower = text.toLowerCase();
        if (lower.contains("active") || lower.contains("online") || lower.contains("success")) {
          pillBg = SUCCESS_BG;
          setForeground(SUCCESS_TEXT);
          setText("● " + text);
        } else if (lower.contains("lock") || lower.contains("ban")) {
          pillBg = DANGER_BG;
          setForeground(DANGER_TEXT);
          setText("● " + text);
        } else if (lower.contains("admin")) {
          pillBg = PRIMARY_SOFT_BG;
          setForeground(PRIMARY);
          setText("★ " + text);
        } else if (lower.contains("pending")) {
          pillBg = WARNING_BG;
          setForeground(WARNING_TEXT);
          setText("● " + text);
        } else {
          pillBg = new Color(0xF1F5F9);
          setForeground(TEXT_SECONDARY);
          setText(text);
        }
        return this;
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(rowBg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int pillW = Math.min(getWidth() - 12, Math.max(68, getPreferredSize().width));
        int pillH = Math.min(getHeight() - 10, 24);
        int x = (getWidth() - pillW) / 2;
        int y = (getHeight() - pillH) / 2;
        g2.setColor(pillBg);
        g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
        g2.dispose();

        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
      }
    };
  }

  /** Programmatic crisp vector icons for sidebar navigation and headers. */
  public enum NavIconType {
    SHIELD,
    USERS,
    GROUPS,
    SPAM,
    NEW_USER,
    FRIENDS,
    ACTIVITY,
    CHARTS,
    LOGOUT
  }

  public static Icon createVectorIcon(NavIconType type, Color color, int size) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(x, y);

        switch (type) {
          case SHIELD -> {
            Path2D shield = new Path2D.Float();
            shield.moveTo(size / 2.0, 2);
            shield.lineTo(size - 2, 5);
            shield.lineTo(size - 2, size / 2.0 + 1);
            shield.curveTo(size - 2, size - 4, size / 2.0, size - 1, size / 2.0, size - 1);
            shield.curveTo(size / 2.0, size - 1, 2, size - 4, 2, size / 2.0 + 1);
            shield.lineTo(2, 5);
            shield.closePath();
            g2.draw(shield);
          }
          case USERS -> {
            g2.drawOval(4, 2, 6, 6);
            g2.drawArc(2, 9, 10, 6, 0, 180);
            g2.drawOval(10, 3, 4, 4);
          }
          case GROUPS -> {
            g2.drawRoundRect(2, 3, size - 4, size - 7, 4, 4);
            g2.drawLine(5, size - 4, 3, size - 1);
          }
          case SPAM -> {
            Path2D tri = new Path2D.Float();
            tri.moveTo(size / 2.0, 2);
            tri.lineTo(size - 2, size - 2);
            tri.lineTo(2, size - 2);
            tri.closePath();
            g2.draw(tri);
            g2.drawLine(size / 2, 6, size / 2, 10);
            g2.fillOval(size / 2 - 1, 12, 2, 2);
          }
          case NEW_USER -> {
            g2.drawOval(3, 3, 6, 6);
            g2.drawArc(1, 10, 10, 5, 0, 180);
            g2.drawLine(13, 5, 13, 11);
            g2.drawLine(10, 8, 16, 8);
          }
          case FRIENDS -> {
            g2.drawOval(2, 6, 4, 4);
            g2.drawOval(size - 6, 2, 4, 4);
            g2.drawOval(size - 6, size - 6, 4, 4);
            g2.drawLine(6, 7, size - 6, 4);
            g2.drawLine(6, 9, size - 6, size - 4);
          }
          case ACTIVITY -> {
            Path2D poly = new Path2D.Float();
            poly.moveTo(1, size / 2.0);
            poly.lineTo(5, size / 2.0);
            poly.lineTo(7, 3);
            poly.lineTo(10, size - 3);
            poly.lineTo(12, size / 2.0);
            poly.lineTo(size - 1, size / 2.0);
            g2.draw(poly);
          }
          case CHARTS -> {
            g2.drawLine(2, size - 2, size - 2, size - 2);
            g2.fillRect(3, 9, 3, size - 11);
            g2.fillRect(8, 5, 3, size - 7);
            g2.fillRect(13, 2, 3, size - 4);
          }
          case LOGOUT -> {
            g2.drawRect(2, 2, 7, size - 4);
            g2.drawLine(7, size / 2, size - 2, size / 2);
            g2.drawLine(size - 5, size / 2 - 3, size - 2, size / 2);
            g2.drawLine(size - 5, size / 2 + 3, size - 2, size / 2);
          }
        }
        g2.dispose();
      }

      @Override
      public int getIconWidth() {
        return size;
      }

      @Override
      public int getIconHeight() {
        return size;
      }
    };
  }
}
