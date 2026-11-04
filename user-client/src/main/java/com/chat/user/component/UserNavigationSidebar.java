package com.chat.user.component;

import com.chat.user.view.UserMainFrame.Destination;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class UserNavigationSidebar extends JPanel {
  private static final Color NAV_TEXT = new Color(0xCBD5E1);
  private static final Color NAV_ICON = new Color(0x94A3B8);
  private static final Color SECTION_TEXT = new Color(0x94A3B8);
  private static final int NAV_ITEM_WIDTH = 220;
  private static final int NAV_ITEM_HEIGHT = 38;

  private final Map<Destination, JButton> buttons = new EnumMap<>(Destination.class);
  private Destination activeDestination;
  private final JButton signOutButton = new JButton("Sign Out");

  public UserNavigationSidebar(
      String displayName, String username, Consumer<Destination> navigationListener) {
    super(new BorderLayout());
    setBackground(UserUiTheme.SIDEBAR_BG);
    setPreferredSize(new Dimension(UserUiTheme.SIDEBAR_WIDTH, 0));
    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UserUiTheme.SIDEBAR_DIVIDER));

    add(buildBrandAndProfile(displayName, username), BorderLayout.NORTH);
    add(buildNavigation(navigationListener), BorderLayout.CENTER);
    add(buildFooter(), BorderLayout.SOUTH);
    updateActiveStyles();
  }

  public void setActiveDestination(Destination activeDestination) {
    this.activeDestination = activeDestination;
    updateActiveStyles();
  }

  public JButton getButton(Destination destination) {
    return buttons.get(destination);
  }

  public JButton getSignOutButton() {
    return signOutButton;
  }

  private JPanel buildBrandAndProfile(String displayName, String username) {
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    top.setBackground(UserUiTheme.SIDEBAR_BG);
    top.setBorder(BorderFactory.createEmptyBorder(18, 16, 12, 16));

    JPanel brandRow = new JPanel(new BorderLayout());
    brandRow.setBackground(UserUiTheme.SIDEBAR_BG);
    JLabel brand = new JLabel("ChatUser");
    brand.setFont(new Font("SansSerif", Font.BOLD, 17));
    brand.setForeground(Color.WHITE);
    brand.setIcon(
        UserUiTheme.createNavigationIcon(
            UserUiTheme.NavigationIcon.CHAT, new Color(0x60A5FA), 18));
    brand.setIconTextGap(8);

    JLabel version = new JLabel("v1.0");
    version.setFont(UserUiTheme.FONT_SECTION);
    version.setForeground(new Color(0x93C5FD));
    version.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x1E3A8A), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
    brandRow.add(brand, BorderLayout.WEST);
    brandRow.add(version, BorderLayout.EAST);
    top.add(brandRow);
    top.add(Box.createVerticalStrut(14));

    JPanel profileCard =
        new JPanel(new BorderLayout(10, 0)) {
          @Override
          protected void paintComponent(java.awt.Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UserUiTheme.SIDEBAR_HOVER);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.dispose();
            super.paintComponent(graphics);
          }
        };
    profileCard.setOpaque(false);
    profileCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    profileCard.setAlignmentX(LEFT_ALIGNMENT);
    profileCard.setMaximumSize(new Dimension(220, 54));

    JLabel avatar =
        new JLabel(initials(displayName), SwingConstants.CENTER) {
          @Override
          protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UserUiTheme.PRIMARY);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            g.dispose();
            super.paintComponent(graphics);
          }
        };
    avatar.setPreferredSize(new Dimension(32, 32));
    avatar.setForeground(Color.WHITE);
    avatar.setFont(UserUiTheme.FONT_BODY_BOLD);
    avatar.setOpaque(false);

    JPanel profileText = new JPanel();
    profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
    profileText.setOpaque(false);
    JLabel name = new JLabel(displayName);
    name.setFont(UserUiTheme.FONT_SMALL);
    name.setForeground(Color.WHITE);
    name.setToolTipText(displayName + " (@" + username + ")");
    JLabel status = new JLabel("● Online Session");
    status.setFont(new Font("SansSerif", Font.BOLD, 11));
    status.setForeground(UserUiTheme.SUCCESS);
    profileText.add(name);
    profileText.add(status);

    profileCard.add(avatar, BorderLayout.WEST);
    profileCard.add(profileText, BorderLayout.CENTER);
    top.add(profileCard);
    return top;
  }

  private JPanel buildNavigation(Consumer<Destination> navigationListener) {
    JPanel navList = new JPanel();
    navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
    navList.setBackground(UserUiTheme.SIDEBAR_BG);
    navList.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

    navList.add(createSectionLabel("M E S S A G I N G"));
    navList.add(Box.createVerticalStrut(4));
    for (Destination destination : Destination.values()) {
      if (destination == Destination.PROFILE) {
        navList.add(Box.createVerticalStrut(14));
        navList.add(createSectionLabel("A C C O U N T"));
        navList.add(Box.createVerticalStrut(4));
      }
      JButton button = createNavigationButton(destination);
      button.addActionListener(event -> navigationListener.accept(destination));
      buttons.put(destination, button);
      navList.add(button);
      navList.add(Box.createVerticalStrut(4));
    }
    return navList;
  }

  private JPanel buildFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(UserUiTheme.SIDEBAR_BG);
    footer.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UserUiTheme.SIDEBAR_DIVIDER),
            BorderFactory.createEmptyBorder(12, 12, 14, 12)));

    signOutButton.setFont(UserUiTheme.FONT_BODY_BOLD);
    signOutButton.setHorizontalAlignment(SwingConstants.LEFT);
    signOutButton.setIcon(
        UserUiTheme.createNavigationIcon(
            UserUiTheme.NavigationIcon.LOGOUT, UserUiTheme.DANGER_SOFT, 16));
    signOutButton.setIconTextGap(10);
    signOutButton.setBackground(UserUiTheme.SIDEBAR_HOVER);
    signOutButton.setForeground(UserUiTheme.DANGER_SOFT);
    signOutButton.setFocusPainted(false);
    signOutButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    signOutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    footer.add(signOutButton, BorderLayout.CENTER);
    return footer;
  }

  private JLabel createSectionLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(UserUiTheme.FONT_SECTION);
    label.setForeground(SECTION_TEXT);
    label.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 4));
    label.setAlignmentX(LEFT_ALIGNMENT);
    return label;
  }

  private JButton createNavigationButton(Destination destination) {
    JButton button = new JButton(destination.label());
    button.setFont(UserUiTheme.FONT_BODY);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setIconTextGap(10);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
    button.setMaximumSize(new Dimension(NAV_ITEM_WIDTH, NAV_ITEM_HEIGHT));
    button.setPreferredSize(new Dimension(NAV_ITEM_WIDTH, NAV_ITEM_HEIGHT));
    button.setAlignmentX(LEFT_ALIGNMENT);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent event) {
            if (destination != activeDestination) {
              button.setBackground(UserUiTheme.SIDEBAR_HOVER);
            }
          }

          @Override
          public void mouseExited(MouseEvent event) {
            if (destination != activeDestination) {
              button.setBackground(UserUiTheme.SIDEBAR_BG);
            }
          }
        });
    return button;
  }

  private void updateActiveStyles() {
    for (Map.Entry<Destination, JButton> entry : buttons.entrySet()) {
      boolean active = entry.getKey() == activeDestination;
      JButton button = entry.getValue();
      button.setBackground(active ? UserUiTheme.PRIMARY : UserUiTheme.SIDEBAR_BG);
      button.setForeground(active ? Color.WHITE : NAV_TEXT);
      button.setFont(active ? UserUiTheme.FONT_BODY_BOLD : UserUiTheme.FONT_BODY);
      UserUiTheme.NavigationIcon iconType = iconFor(entry.getKey());
      button.setIcon(
          UserUiTheme.createNavigationIcon(
              iconType, active ? Color.WHITE : NAV_ICON, 16));
    }
  }

  private UserUiTheme.NavigationIcon iconFor(Destination destination) {
    return switch (destination) {
      case CHATS -> UserUiTheme.NavigationIcon.CHAT;
      case FRIENDS -> UserUiTheme.NavigationIcon.FRIENDS;
      case REQUESTS -> UserUiTheme.NavigationIcon.REQUESTS;
      case GROUPS -> UserUiTheme.NavigationIcon.GROUPS;
      case PROFILE -> UserUiTheme.NavigationIcon.PROFILE;
    };
  }

  private String initials(String displayName) {
    String[] parts = displayName.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0].substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }
    return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
        .toUpperCase(java.util.Locale.ROOT);
  }
}
