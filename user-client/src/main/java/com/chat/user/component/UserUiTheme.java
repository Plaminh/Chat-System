package com.chat.user.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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

  private UserUiTheme() {}

  public static void applyGlobalDefaults() {
    UIManager.put("Button.arc", 8);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbArc", 999);
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
}
