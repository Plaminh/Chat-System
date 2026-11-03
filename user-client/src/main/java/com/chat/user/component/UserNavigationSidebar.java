package com.chat.user.component;

import com.chat.user.view.UserMainFrame.Destination;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class UserNavigationSidebar extends JPanel {
  private final Map<Destination, JButton> buttons = new EnumMap<>(Destination.class);

  public UserNavigationSidebar(Consumer<Destination> navigationListener) {
    super(new BorderLayout());
    setBackground(UserUiTheme.SIDEBAR_BG);
    setBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, UserUiTheme.SIDEBAR_DIVIDER));

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBackground(UserUiTheme.SIDEBAR_BG);
    content.setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD,
            UserUiTheme.SPACE_MD));

    JLabel brand = new JLabel("CHAT SYSTEM");
    brand.setFont(new Font("SansSerif", Font.BOLD, 15));
    brand.setForeground(Color.WHITE);
    brand.setAlignmentX(LEFT_ALIGNMENT);
    content.add(brand);
    content.add(Box.createRigidArea(new Dimension(0, UserUiTheme.SPACE_LG)));

    for (Destination destination : Destination.values()) {
      JButton button = createNavigationButton(destination);
      button.addActionListener(event -> navigationListener.accept(destination));
      buttons.put(destination, button);
      content.add(button);
      content.add(Box.createRigidArea(new Dimension(0, UserUiTheme.SPACE_XS)));
    }
    add(content, BorderLayout.NORTH);
  }

  public void setActiveDestination(Destination activeDestination) {
    for (Map.Entry<Destination, JButton> entry : buttons.entrySet()) {
      boolean active = entry.getKey() == activeDestination;
      JButton button = entry.getValue();
      button.setBackground(active ? UserUiTheme.PRIMARY : UserUiTheme.SIDEBAR_BG);
      button.setForeground(active ? Color.WHITE : new Color(0xCBD5E1));
      button.setFont(active ? UserUiTheme.FONT_BODY_BOLD : UserUiTheme.FONT_BODY);
    }
  }

  public JButton getButton(Destination destination) {
    return buttons.get(destination);
  }

  private JButton createNavigationButton(Destination destination) {
    JButton button = new JButton(destination.label());
    button.setHorizontalAlignment(JButton.LEFT);
    button.setFont(UserUiTheme.FONT_BODY);
    button.setForeground(new Color(0xCBD5E1));
    button.setBackground(UserUiTheme.SIDEBAR_BG);
    button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    button.setFocusPainted(false);
    button.setContentAreaFilled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setAlignmentX(LEFT_ALIGNMENT);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    button.setPreferredSize(new Dimension(210, 42));
    return button;
  }
}
