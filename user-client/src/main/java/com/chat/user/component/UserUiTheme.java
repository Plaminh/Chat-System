package com.chat.user.component;

import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class UserUiTheme {
  public static final Color CANVAS_BG = new Color(0xF8FAFC);
  public static final Color SURFACE_WHITE = new Color(0xFFFFFF);
  public static final Color SIDEBAR_BG = new Color(0x0F172A);
  public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
  public static final Color SIDEBAR_DIVIDER = new Color(0x334155);

  public static final Color PRIMARY = new Color(0x2563EB);
  public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
  public static final Color PRIMARY_SOFT_BG = new Color(0xEFF6FF);
  public static final Color SUCCESS = new Color(0x10B981);
  public static final Color DANGER = new Color(0xDC2626);
  public static final Color DANGER_SOFT = new Color(0xFCA5A5);

  public static final Color TEXT_PRIMARY = new Color(0x0F172A);
  public static final Color TEXT_SECONDARY = new Color(0x475569);
  public static final Color TEXT_MUTED = new Color(0x64748B);
  public static final Color BORDER = new Color(0xE2E8F0);

  public static final int SPACE_XS = 4;
  public static final int SPACE_SM = 8;
  public static final int SPACE_MD = 16;
  public static final int SPACE_LG = 24;
  public static final int SIDEBAR_WIDTH = 245;

  public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
  public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
  public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
  public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
  public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
  public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 11);

  private UserUiTheme() {}

  public static void applyGlobalDefaults() {
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("Table.rowHeight", 36);
    UIManager.put("Table.showHorizontalLines", true);
    UIManager.put("Table.showVerticalLines", false);
  }

  public static Border createCardBorder() {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER, 1, true),
        BorderFactory.createEmptyBorder(SPACE_MD, SPACE_MD, SPACE_MD, SPACE_MD));
  }

  public static JPanel createCardPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(SURFACE_WHITE);
    panel.setBorder(createCardBorder());
    return panel;
  }

  public static JButton createPrimaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(FONT_BODY_BOLD);
    button.setBackground(PRIMARY);
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
    return button;
  }

  public static JButton createSecondaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(FONT_BODY_BOLD);
    button.setBackground(SURFACE_WHITE);
    button.setForeground(TEXT_PRIMARY);
    button.setFocusPainted(false);
    button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
    return button;
  }

  public static JTextField createTextField(int columns) {
    JTextField field = new JTextField(columns);
    field.setFont(FONT_BODY);
    field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
    return field;
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
