package com.chat.user.view;

import com.chat.user.component.UserNavigationSidebar;
import com.chat.user.component.UserUiTheme;
import com.chat.user.model.UserSessionState;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class UserMainFrame extends JPanel {
  public enum Destination {
    CHATS("Chats"),
    FRIENDS("Friends"),
    REQUESTS("Requests / Search"),
    GROUPS("Groups"),
    PROFILE("Profile");

    private final String label;

    Destination(String label) {
      this.label = label;
    }

    public String label() {
      return label;
    }
  }

  private final UserSessionState sessionState;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardContainer = new JPanel(cardLayout);
  private final UserNavigationSidebar navigationSidebar =
      new UserNavigationSidebar(this::showDestination);
  private final Map<Destination, JComponent> views = new EnumMap<>(Destination.class);
  private final JLabel headerTitle = new JLabel();
  private Destination activeDestination;

  public UserMainFrame(UserSessionState sessionState) {
    super(new BorderLayout());
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    UserUiTheme.applyGlobalDefaults();
    buildLayout();
    for (Destination destination : Destination.values()) {
      registerView(destination, createPlaceholder(destination));
    }
    showDestination(Destination.CHATS);
  }

  public void registerView(Destination destination, JComponent view) {
    Objects.requireNonNull(destination, "destination");
    Objects.requireNonNull(view, "view");
    JComponent previous = views.put(destination, view);
    if (previous != null) {
      cardContainer.remove(previous);
    }
    cardContainer.add(view, destination.name());
    cardContainer.revalidate();
    cardContainer.repaint();
  }

  public void showDestination(Destination destination) {
    Objects.requireNonNull(destination, "destination");
    if (!views.containsKey(destination)) {
      throw new IllegalArgumentException("No view registered for " + destination);
    }
    activeDestination = destination;
    cardLayout.show(cardContainer, destination.name());
    headerTitle.setText(destination.label());
    navigationSidebar.setActiveDestination(destination);
  }

  Destination getActiveDestination() {
    return activeDestination;
  }

  UserNavigationSidebar getNavigationSidebar() {
    return navigationSidebar;
  }

  JComponent getRegisteredView(Destination destination) {
    return views.get(destination);
  }

  private void buildLayout() {
    setBackground(UserUiTheme.CANVAS_BG);
    navigationSidebar.setPreferredSize(new Dimension(UserUiTheme.SIDEBAR_WIDTH, 0));
    add(navigationSidebar, BorderLayout.WEST);

    JPanel workspace = new JPanel(new BorderLayout());
    workspace.setBackground(UserUiTheme.CANVAS_BG);
    workspace.add(buildHeader(), BorderLayout.NORTH);

    cardContainer.setBackground(UserUiTheme.CANVAS_BG);
    cardContainer.setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG));
    workspace.add(cardContainer, BorderLayout.CENTER);
    add(workspace, BorderLayout.CENTER);
  }

  private JPanel buildHeader() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(UserUiTheme.SURFACE_WHITE);
    header.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UserUiTheme.BORDER),
            BorderFactory.createEmptyBorder(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG,
                UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG)));

    headerTitle.setFont(UserUiTheme.FONT_TITLE);
    headerTitle.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel account = new JLabel(sessionState.getProfile().fullName());
    account.setFont(UserUiTheme.FONT_BODY);
    account.setForeground(UserUiTheme.TEXT_SECONDARY);
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    right.setOpaque(false);
    right.add(account);

    header.add(headerTitle, BorderLayout.WEST);
    header.add(right, BorderLayout.EAST);
    return header;
  }

  private JComponent createPlaceholder(Destination destination) {
    JPanel placeholder = new JPanel(new BorderLayout());
    placeholder.setBackground(UserUiTheme.SURFACE_WHITE);
    placeholder.setBorder(UserUiTheme.createCardBorder());
    JLabel message = new JLabel(destination.label() + " screen", SwingConstants.CENTER);
    message.setFont(UserUiTheme.FONT_SUBTITLE);
    message.setForeground(UserUiTheme.TEXT_MUTED);
    placeholder.add(message, BorderLayout.CENTER);
    return placeholder;
  }
}
