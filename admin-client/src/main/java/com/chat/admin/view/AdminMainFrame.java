package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.view.AdminUIStyle.NavIconType;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Root desktop workspace shell for the Admin Client UI (Task 1 / Screen 2).
 *
 * <p>Provides a dark slate left navigation sidebar, top contextual header bar, status footer, and a
 * central {@link CardLayout} container hosting all administrative modules.
 */
public final class AdminMainFrame extends JFrame {
  public static final String CARD_USERS = "USER_DIRECTORY";
  public static final String CARD_GROUPS = "GROUP_CHATS";
  public static final String CARD_SPAM = "SPAM_REPORTS";
  public static final String CARD_NEW_USERS = "NEW_REGISTRATIONS";
  public static final String CARD_FRIENDS = "FRIEND_STATISTICS";
  public static final String CARD_ACTIVITY = "ACTIVITY_METRICS";
  public static final String CARD_CHARTS = "VISUAL_CHARTS";

  private final String adminUsername;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardContainer = new JPanel(cardLayout);
  private final Map<String, JButton> navButtons = new LinkedHashMap<>();
  private final Map<String, NavIconType> navIcons = new LinkedHashMap<>();
  private final Map<String, String> navTitles = new LinkedHashMap<>();

  private final JLabel headerTitleLabel = new JLabel("User Directory & Management");
  private final JLabel headerBreadcrumbLabel = new JLabel("Console  ›  Oversight  ›  User Directory");
  private Runnable signOutHandler;

  public AdminMainFrame() {
    this("admin");
  }

  public AdminMainFrame(String adminUsername) {
    super("ChatAdmin Console v1.0 — System Administration");
    this.adminUsername = adminUsername == null || adminUsername.isBlank() ? "admin" : adminUsername;
    AdminUIStyle.applyGlobalThemeDefaults();
    initFrameLayout();
    registerDefaultCards();
    showCard(CARD_USERS);
  }

  public void setSignOutHandler(Runnable signOutHandler) {
    this.signOutHandler = signOutHandler;
  }

  /** Registers or replaces a module view inside the central CardLayout container. */
  public void registerModuleView(String cardKey, JComponent viewComponent) {
    cardContainer.add(viewComponent, cardKey);
    cardContainer.revalidate();
    cardContainer.repaint();
  }

  /** Switches the active central view to the specified card key and updates sidebar state. */
  public void showCard(String cardKey) {
    cardLayout.show(cardContainer, cardKey);
    String title = navTitles.getOrDefault(cardKey, "Administration");
    headerTitleLabel.setText(title);
    String section =
        (CARD_USERS.equals(cardKey) || CARD_GROUPS.equals(cardKey) || CARD_SPAM.equals(cardKey))
            ? "Oversight"
            : "Analytics & Intelligence";
    headerBreadcrumbLabel.setText("Console  ›  " + section + "  ›  " + title);

    for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
      String key = entry.getKey();
      JButton btn = entry.getValue();
      boolean active = key.equals(cardKey);
      btn.setBackground(active ? AdminUIStyle.PRIMARY : AdminUIStyle.SIDEBAR_BG);
      btn.setForeground(active ? Color.WHITE : new Color(0xCBD5E1));
      btn.setFont(active ? AdminUIStyle.FONT_BODY_BOLD : AdminUIStyle.FONT_BODY);
      NavIconType iconType = navIcons.get(key);
      if (iconType != null) {
        btn.setIcon(
            AdminUIStyle.createVectorIcon(
                iconType, active ? Color.WHITE : new Color(0x94A3B8), 16));
      }
    }
  }

  private void initFrameLayout() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(1120, 720));
    setSize(1280, 800);
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(AdminUIStyle.CANVAS_BG);

    root.add(buildSidebar(), BorderLayout.WEST);

    JPanel centerWrapper = new JPanel(new BorderLayout());
    centerWrapper.setBackground(AdminUIStyle.CANVAS_BG);
    centerWrapper.add(buildTopHeader(), BorderLayout.NORTH);

    cardContainer.setBackground(AdminUIStyle.CANVAS_BG);
    cardContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    centerWrapper.add(cardContainer, BorderLayout.CENTER);
    centerWrapper.add(buildStatusFooter(), BorderLayout.SOUTH);

    root.add(centerWrapper, BorderLayout.CENTER);
    setContentPane(root);
  }

  private JPanel buildSidebar() {
    JPanel sidebar = new JPanel(new BorderLayout());
    sidebar.setBackground(AdminUIStyle.SIDEBAR_BG);
    sidebar.setPreferredSize(new Dimension(245, 800));
    sidebar.setBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, AdminUIStyle.SIDEBAR_DIVIDER));

    JPanel topBox = new JPanel();
    topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
    topBox.setBackground(AdminUIStyle.SIDEBAR_BG);
    topBox.setBorder(BorderFactory.createEmptyBorder(18, 16, 12, 16));

    // Brand row
    JPanel brandRow = new JPanel(new BorderLayout());
    brandRow.setBackground(AdminUIStyle.SIDEBAR_BG);
    brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel logoLabel = new JLabel("ChatAdmin");
    logoLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
    logoLabel.setForeground(Color.WHITE);
    logoLabel.setIcon(
        AdminUIStyle.createVectorIcon(NavIconType.SHIELD, new Color(0x60A5FA), 18));
    logoLabel.setIconTextGap(8);

    JLabel suiteBadge = new JLabel("v1.0");
    suiteBadge.setFont(AdminUIStyle.FONT_BADGE);
    suiteBadge.setForeground(new Color(0x93C5FD));
    suiteBadge.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x1E3A8A), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));

    brandRow.add(logoLabel, BorderLayout.WEST);
    brandRow.add(suiteBadge, BorderLayout.EAST);
    topBox.add(brandRow);
    topBox.add(Box.createVerticalStrut(14));

    // Admin Profile Snippet Card
    JPanel profileCard = new JPanel(new BorderLayout(10, 0));
    profileCard.setBackground(AdminUIStyle.SIDEBAR_HOVER);
    profileCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
    profileCard.setMaximumSize(new Dimension(220, 54));

    JLabel avatarBadge = new JLabel("SA", SwingConstants.CENTER);
    avatarBadge.setPreferredSize(new Dimension(32, 32));
    avatarBadge.setOpaque(true);
    avatarBadge.setBackground(AdminUIStyle.PRIMARY);
    avatarBadge.setForeground(Color.WHITE);
    avatarBadge.setFont(AdminUIStyle.FONT_BODY_BOLD);

    JPanel profileText = new JPanel(new GridLayout(2, 1));
    profileText.setOpaque(false);
    JLabel adminName = new JLabel("System Admin (@" + adminUsername + ")");
    adminName.setFont(AdminUIStyle.FONT_SMALL);
    adminName.setForeground(Color.WHITE);
    JLabel onlineDot = new JLabel("● Online Session");
    onlineDot.setFont(new Font("SansSerif", Font.BOLD, 11));
    onlineDot.setForeground(AdminUIStyle.SUCCESS_DOT);
    profileText.add(adminName);
    profileText.add(onlineDot);

    profileCard.add(avatarBadge, BorderLayout.WEST);
    profileCard.add(profileText, BorderLayout.CENTER);
    topBox.add(profileCard);

    sidebar.add(topBox, BorderLayout.NORTH);

    // Navigation Menu List
    JPanel navList = new JPanel();
    navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
    navList.setBackground(AdminUIStyle.SIDEBAR_BG);
    navList.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

    navList.add(createSidebarSectionLabel("OVERSIGHT & MANAGEMENT"));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_USERS,
            "User Directory",
            "User Directory & Management",
            NavIconType.USERS));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_GROUPS,
            "Group Chats",
            "Group Chat Oversight & Member Roles",
            NavIconType.GROUPS));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_SPAM,
            "Spam Reports",
            "Spam Reports Moderation",
            NavIconType.SPAM));

    navList.add(Box.createVerticalStrut(18));
    navList.add(createSidebarSectionLabel("ANALYTICS & INTELLIGENCE"));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_NEW_USERS,
            "New Registrations",
            "New User Registrations Timeline",
            NavIconType.NEW_USER));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_FRIENDS,
            "Friend Statistics",
            "Friend Network Statistics",
            NavIconType.FRIENDS));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_ACTIVITY,
            "Activity Metrics",
            "User Engagement & Chat Activity",
            NavIconType.ACTIVITY));
    navList.add(Box.createVerticalStrut(4));
    navList.add(
        createNavButton(
            CARD_CHARTS,
            "Visual Charts",
            "Annual Registration & Activity Charts",
            NavIconType.CHARTS));

    sidebar.add(navList, BorderLayout.CENTER);

    // Bottom Sign Out Area
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBackground(AdminUIStyle.SIDEBAR_BG);
    bottomPanel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AdminUIStyle.SIDEBAR_DIVIDER),
            BorderFactory.createEmptyBorder(12, 12, 14, 12)));

    JButton signOutBtn = new JButton("Sign Out");
    signOutBtn.setFont(AdminUIStyle.FONT_BODY_BOLD);
    signOutBtn.setHorizontalAlignment(SwingConstants.LEFT);
    signOutBtn.setIcon(
        AdminUIStyle.createVectorIcon(NavIconType.LOGOUT, new Color(0xFCA5A5), 16));
    signOutBtn.setIconTextGap(10);
    signOutBtn.setBackground(new Color(0x1E293B));
    signOutBtn.setForeground(new Color(0xFCA5A5));
    signOutBtn.setFocusPainted(false);
    signOutBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    signOutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    signOutBtn.addActionListener(e -> handleSignOut());

    bottomPanel.add(signOutBtn, BorderLayout.CENTER);
    sidebar.add(bottomPanel, BorderLayout.SOUTH);

    return sidebar;
  }

  private JLabel createSidebarSectionLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(AdminUIStyle.FONT_SECTION);
    label.setForeground(new Color(0x64748B));
    label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  private JButton createNavButton(
      String cardKey, String menuText, String fullTitle, NavIconType iconType) {
    navTitles.put(cardKey, fullTitle);
    navIcons.put(cardKey, iconType);

    JButton btn = new JButton(menuText);
    btn.setFont(AdminUIStyle.FONT_BODY);
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setIconTextGap(10);
    btn.setFocusPainted(false);
    btn.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
    btn.setMaximumSize(new Dimension(220, 38));
    btn.setPreferredSize(new Dimension(220, 38));
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.addActionListener(e -> showCard(cardKey));

    navButtons.put(cardKey, btn);
    return btn;
  }

  private JPanel buildTopHeader() {
    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setBackground(AdminUIStyle.SURFACE_WHITE);
    header.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AdminUIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)));

    JPanel leftTitles = new JPanel(new BorderLayout(0, 2));
    leftTitles.setOpaque(false);
    headerBreadcrumbLabel.setFont(AdminUIStyle.FONT_SMALL);
    headerBreadcrumbLabel.setForeground(AdminUIStyle.TEXT_MUTED);
    headerTitleLabel.setFont(AdminUIStyle.FONT_TITLE);
    headerTitleLabel.setForeground(AdminUIStyle.TEXT_PRIMARY);
    leftTitles.add(headerBreadcrumbLabel, BorderLayout.NORTH);
    leftTitles.add(headerTitleLabel, BorderLayout.SOUTH);

    JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
    rightInfo.setOpaque(false);

    JLabel dateBadge =
        new JLabel("Session Date: " + LocalDateTime.now().format(AdminUIStyle.DATETIME_FMT));
    dateBadge.setFont(AdminUIStyle.FONT_MONO);
    dateBadge.setForeground(AdminUIStyle.TEXT_SECONDARY);
    dateBadge.setOpaque(true);
    dateBadge.setBackground(AdminUIStyle.CANVAS_BG);
    dateBadge.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AdminUIStyle.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));

    JLabel roleBadge = new JLabel("● Role: Administrator");
    roleBadge.setFont(AdminUIStyle.FONT_BADGE);
    roleBadge.setForeground(AdminUIStyle.SUCCESS_TEXT);
    roleBadge.setOpaque(true);
    roleBadge.setBackground(AdminUIStyle.SUCCESS_BG);
    roleBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    rightInfo.add(dateBadge);
    rightInfo.add(roleBadge);

    header.add(leftTitles, BorderLayout.WEST);
    header.add(rightInfo, BorderLayout.EAST);
    return header;
  }

  private JPanel buildStatusFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(AdminUIStyle.SURFACE_WHITE);
    footer.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AdminUIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 20, 6, 20)));

    AdminMockDataStore store = AdminMockDataStore.getInstance();
    JLabel leftStatus =
        new JLabel(
            "● Workspace Ready   |   Total Accounts: "
                + store.getUsers().size()
                + "   |   Group Chats: "
                + store.getGroups().size()
                + "   |   Spam Reports: "
                + store.getSpamReports().size());
    leftStatus.setFont(AdminUIStyle.FONT_SMALL);
    leftStatus.setForeground(AdminUIStyle.TEXT_SECONDARY);

    store.addChangeListener(
        () ->
            leftStatus.setText(
                "● Workspace Ready   |   Total Accounts: "
                    + store.getUsers().size()
                    + "   |   Group Chats: "
                    + store.getGroups().size()
                    + "   |   Spam Reports: "
                    + store.getSpamReports().size()));

    JLabel rightVersion = new JLabel("University Java Chat System — Admin Client UI (Version 1)");
    rightVersion.setFont(AdminUIStyle.FONT_SMALL);
    rightVersion.setForeground(AdminUIStyle.TEXT_MUTED);

    footer.add(leftStatus, BorderLayout.WEST);
    footer.add(rightVersion, BorderLayout.EAST);
    return footer;
  }

  private void registerDefaultCards() {
    for (Map.Entry<String, String> entry : navTitles.entrySet()) {
      cardContainer.add(createOverviewPlaceholderCard(entry.getValue()), entry.getKey());
    }
  }

  private JPanel createOverviewPlaceholderCard(String title) {
    JPanel panel = AdminUIStyle.createCardPanel(new BorderLayout(16, 16));
    JLabel heading = new JLabel(title, SwingConstants.CENTER);
    heading.setFont(AdminUIStyle.FONT_TITLE);
    heading.setForeground(AdminUIStyle.TEXT_PRIMARY);
    panel.add(heading, BorderLayout.CENTER);
    return panel;
  }

  private void handleSignOut() {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out of the Admin Console?",
            "Confirm Sign Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (choice == JOptionPane.YES_OPTION) {
      dispose();
      if (signOutHandler != null) {
        signOutHandler.run();
      }
    }
  }
}
