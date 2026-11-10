package com.chat.user.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class ChatTheme {
  public static final Color CANVAS_BG = new Color(0xF8FAFC);
  public static final Color SURFACE_WHITE = new Color(0xFFFFFF);
  public static final Color SIDEBAR_BG = new Color(0x0F172A);
  public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
  public static final Color SIDEBAR_DIVIDER = new Color(0x334155);

  public static final Color PRIMARY = new Color(0x2563EB);
  public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
  public static final Color PRIMARY_SOFT_BG = new Color(0xEFF6FF);
  public static final Color SUCCESS = new Color(0x10B981);
  public static final Color SUCCESS_DOT = SUCCESS;
  public static final Color SUCCESS_TEXT = new Color(0x065F46);
  public static final Color SUCCESS_BG = new Color(0xECFDF5);
  public static final Color DANGER = new Color(0xDC2626);
  public static final Color DANGER_BTN = DANGER;
  public static final Color DANGER_SOFT = new Color(0xFCA5A5);
  public static final Color DANGER_TEXT = new Color(0x991B1B);
  public static final Color DANGER_BG = new Color(0xFEF2F2);
  public static final Color WARNING_TEXT = new Color(0x92400E);
  public static final Color WARNING_BG = new Color(0xFFFBEB);

  public static final Color TEXT_PRIMARY = new Color(0x0F172A);
  public static final Color TEXT_SECONDARY = new Color(0x475569);
  public static final Color TEXT_MUTED = new Color(0x64748B);
  public static final Color BORDER = new Color(0xE2E8F0);
  public static final Color TABLE_ALT_ROW = CANVAS_BG;
  public static final Color TABLE_SELECTION_BG = new Color(0xDBEAFE);

  public static final int SPACE_XS = 4;
  public static final int SPACE_SM = 8;
  public static final int SPACE_MD = 16;
  public static final int SPACE_LG = 24;
  public static final int SIDEBAR_WIDTH = 245;
  public static final int BUTTON_HEIGHT = 36;
  public static final int CARD_PADDING = 16;
  public static final int HEADER_PADDING_VERTICAL = 16;
  public static final int HEADER_PADDING_HORIZONTAL = 24;

  public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
  public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
  public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 11);
  public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
  public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
  public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
  public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD, 11);
  public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateTimeFormatter DATE_INPUT_FMT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
  public static final DateTimeFormatter DATETIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  public static final DateTimeFormatter FULL_DATETIME_FMT = DATETIME_FMT;

  private ChatTheme() {}

  public static void applyGlobalDefaults() {
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("Table.rowHeight", BUTTON_HEIGHT);
    UIManager.put("Table.showHorizontalLines", true);
    UIManager.put("Table.showVerticalLines", false);
    UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
    UIManager.put("Table.gridColor", BORDER);
  }

  public static Border createCardBorder() {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER, 1, true),
        BorderFactory.createEmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING));
  }

  public static JPanel createCardPanel() {
    return createCardPanel(new BorderLayout());
  }

  public static JPanel createCardPanel(BorderLayout layout) {
    JPanel panel = new JPanel(layout);
    panel.setBackground(SURFACE_WHITE);
    panel.setBorder(createCardBorder());
    return panel;
  }

  public static JButton createPrimaryButton(String text) {
    return createButton(text, PRIMARY, Color.WHITE);
  }

  public static JButton createSecondaryButton(String text) {
    return createButton(text, SURFACE_WHITE, TEXT_PRIMARY);
  }

  public static JButton createDangerButton(String text) {
    return createButton(text, DANGER, Color.WHITE);
  }

  public static JTextField createTextField(int columns) {
    JTextField field = new JTextField(columns);
    field.setFont(FONT_BODY);
    field.setPreferredSize(new Dimension(field.getPreferredSize().width, BUTTON_HEIGHT));
    return field;
  }

  public static JTextField createSearchField(String placeholder, int columns) {
    JTextField field = createTextField(columns);
    field.putClientProperty("JTextField.placeholderText", placeholder);
    field.putClientProperty("JTextField.showClearButton", true);
    return field;
  }

  public static <T> JComboBox<T> createComboBox(T[] items) {
    JComboBox<T> combo = new JComboBox<>(items);
    combo.setFont(FONT_BODY);
    combo.setBackground(SURFACE_WHITE);
    combo.setPreferredSize(new Dimension(combo.getPreferredSize().width + 16, BUTTON_HEIGHT));
    return combo;
  }

  private static JButton createButton(String text, Color background, Color foreground) {
    JButton button = new JButton(text);
    button.setFont(FONT_BODY_BOLD);
    button.setBackground(background);
    button.setForeground(foreground);
    button.setFocusPainted(false);
    button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(button.getPreferredSize().width, BUTTON_HEIGHT));
    return button;
  }

  public enum NavigationIcon {
    CHAT,
    FRIENDS,
    REQUESTS,
    GROUPS,
    PROFILE,
    LOGOUT
  }

  public static Icon createNavigationIcon(NavigationIcon type, Color color, int size) {
    return new Icon() {
      @Override
      public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.translate(x, y);

        switch (type) {
          case CHAT -> {
            g.drawRoundRect(2, 3, size - 4, size - 6, 5, 5);
            g.drawLine(5, size - 3, 3, size - 1);
          }
          case FRIENDS -> {
            g.drawOval(3, 2, 6, 6);
            g.drawArc(1, 9, 10, 6, 0, 180);
            g.drawOval(10, 3, 4, 4);
          }
          case REQUESTS -> {
            g.drawOval(2, 2, 7, 7);
            g.drawArc(0, 9, 11, 6, 0, 180);
            g.drawLine(13, 5, 13, 11);
            g.drawLine(10, 8, 16, 8);
          }
          case GROUPS -> {
            g.drawOval(5, 1, 6, 6);
            g.drawArc(2, 8, 12, 7, 0, 180);
            g.drawArc(0, 5, 6, 7, 0, 150);
            g.drawArc(10, 5, 6, 7, 30, 150);
          }
          case PROFILE -> {
            g.drawOval(5, 2, 6, 6);
            g.drawArc(2, 9, 12, 7, 0, 180);
          }
          case LOGOUT -> {
            g.drawRect(2, 2, 7, size - 4);
            g.drawLine(7, size / 2, size - 2, size / 2);
            g.drawLine(size - 5, size / 2 - 3, size - 2, size / 2);
            g.drawLine(size - 5, size / 2 + 3, size - 2, size / 2);
          }
        }
        g.dispose();
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
