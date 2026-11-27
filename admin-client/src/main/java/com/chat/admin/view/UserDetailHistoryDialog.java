package com.chat.admin.view;

import com.chat.admin.controller.AdminAuthController;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.model.FriendConnection;
import com.chat.admin.network.SocketClient;
import com.chat.common.dto.admin.LoginHistoryEntry;
import com.chat.common.dto.admin.LoginHistoryRequest;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.SortDirection;
import com.chat.common.ui.ChatTheme;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 * User Login Audit Trail & Friend Network Inspection Modal Dialog (Task 4 / Screen 4).
 *
 * <p>Covers Requirement 1.2.1 (inspect login history & friends of a user) and Requirement 1.2.2
 * (view chronological login history across users showing Login Timestamp, Username, and Full Name).
 */
public final class UserDetailHistoryDialog extends JDialog {
  private static final String[] LOGIN_COLUMNS = {
    "LOGIN TIMESTAMP", "USERNAME", "FULL NAME", "SESSION STATUS"
  };
  /** Login times are shown in Vietnam time, the zone the server uses for date filters. */
  static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private static final DateTimeFormatter LOGIN_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final long RESPONSE_TIMEOUT_SECONDS = 5;
  private static final String[] FRIEND_COLUMNS = {
    "FRIEND USERNAME", "FULL NAME", "MUTUAL FRIENDS", "PRESENCE STATUS"
  };

  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final AdminUser user;
  private final SocketClient socketClient;

  private final JComboBox<String> loginScopeCombo;
  private final JComboBox<String> sortDirectionCombo;
  private int currentPage = 0;
  private int totalPages = 1;
  private int loginRequestSeq = 0;
  private final JLabel pageInfoLabel = new JLabel("Page 1 of 1");
  private final JButton prevPageBtn = ChatTheme.createSecondaryButton("◀ Prev");
  private final JButton nextPageBtn = ChatTheme.createSecondaryButton("Next ▶");
  private final JButton refreshBtn = ChatTheme.createSecondaryButton("Refresh");
  private final JLabel loginStatusLabel = new JLabel(" ");

  private final DefaultTableModel loginTableModel =
      new DefaultTableModel(LOGIN_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JLabel totalLoginsValueLabel = new JLabel("0");

  public UserDetailHistoryDialog(Frame owner, AdminUser user) {
    this(owner, user, AdminAuthController.getSharedSocketClient());
  }

  public UserDetailHistoryDialog(Frame owner, AdminUser user, SocketClient socketClient) {
    super(
        owner,
        "User Inspection & Audit — @" + user.getUsername() + " (" + user.getFullName() + ")",
        true);
    this.user = user;
    this.socketClient = socketClient;
    this.loginScopeCombo =
        ChatTheme.createComboBox(
            new String[] {
              "Scope: Selected User (@" + user.getUsername() + ")",
              "Scope: All System Users (Chronological)"
            });
    this.sortDirectionCombo =
        ChatTheme.createComboBox(new String[] {"Newest First", "Oldest First"});

    this.loginScopeCombo.addActionListener(
        e -> {
          currentPage = 0;
          populateLoginTable();
        });
    this.sortDirectionCombo.addActionListener(
        e -> {
          currentPage = 0;
          populateLoginTable();
        });

    refreshBtn.addActionListener(e -> populateLoginTable());
    prevPageBtn.addActionListener(
        e -> {
          if (currentPage > 0) {
            currentPage--;
            populateLoginTable();
          }
        });
    nextPageBtn.addActionListener(
        e -> {
          if (currentPage < totalPages - 1) {
            currentPage++;
            populateLoginTable();
          }
        });

    initDialogLayout();
    populateLoginTable();
  }

  private void initDialogLayout() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setSize(760, 560);
    setMinimumSize(new Dimension(680, 480));
    setLocationRelativeTo(getOwner());

    JPanel root = new JPanel(new BorderLayout(0, 0));
    root.setBackground(ChatTheme.CANVAS_BG);

    root.add(buildUserProfileBanner(), BorderLayout.NORTH);
    root.add(buildTabbedCenter(), BorderLayout.CENTER);
    root.add(buildDialogFooter(), BorderLayout.SOUTH);

    setContentPane(root);
  }

  private JPanel buildUserProfileBanner() {
    JPanel banner = new JPanel(new BorderLayout(14, 0));
    banner.setBackground(ChatTheme.SURFACE_WHITE);
    banner.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ChatTheme.BORDER),
            BorderFactory.createEmptyBorder(16, 22, 16, 22)));

    String initials = extractInitials(user.getFullName());
    JLabel avatarLabel = new JLabel(initials, SwingConstants.CENTER);
    avatarLabel.setPreferredSize(new Dimension(48, 48));
    avatarLabel.setOpaque(true);
    avatarLabel.setBackground(ChatTheme.PRIMARY);
    avatarLabel.setForeground(Color.WHITE);
    avatarLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

    JPanel identityInfo = new JPanel(new GridLayout(2, 1, 0, 4));
    identityInfo.setOpaque(false);

    JLabel nameTitle =
        new JLabel(user.getFullName() + "   (@" + user.getUsername() + ")");
    nameTitle.setFont(ChatTheme.FONT_TITLE);
    nameTitle.setForeground(ChatTheme.TEXT_PRIMARY);

    JLabel subDetails =
        new JLabel(
            "Email: "
                + user.getEmail()
                + "   •   Address: "
                + user.getAddress()
                + "   •   Registered: "
                + user.getCreatedAt().format(ChatTheme.DATE_FMT));
    subDetails.setFont(ChatTheme.FONT_SMALL);
    subDetails.setForeground(ChatTheme.TEXT_SECONDARY);

    identityInfo.add(nameTitle);
    identityInfo.add(subDetails);

    JPanel rightBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    rightBadges.setOpaque(false);

    JLabel statusBadge = new JLabel("● Status: " + user.getStatusText());
    statusBadge.setFont(ChatTheme.FONT_BADGE);
    statusBadge.setOpaque(true);
    if (user.isLocked()) {
      statusBadge.setBackground(ChatTheme.DANGER_BG);
      statusBadge.setForeground(ChatTheme.DANGER_TEXT);
    } else {
      statusBadge.setBackground(ChatTheme.SUCCESS_BG);
      statusBadge.setForeground(ChatTheme.SUCCESS_TEXT);
    }
    statusBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    JLabel idBadge = new JLabel("#" + user.getId());
    idBadge.setFont(ChatTheme.FONT_MONO);
    idBadge.setForeground(ChatTheme.TEXT_MUTED);
    idBadge.setOpaque(true);
    idBadge.setBackground(ChatTheme.CANVAS_BG);
    idBadge.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ChatTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

    rightBadges.add(statusBadge);
    rightBadges.add(idBadge);

    banner.add(avatarLabel, BorderLayout.WEST);
    banner.add(identityInfo, BorderLayout.CENTER);
    banner.add(rightBadges, BorderLayout.EAST);
    return banner;
  }

  private JTabbedPane buildTabbedCenter() {
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(ChatTheme.FONT_BODY_BOLD);
    tabs.setBackground(ChatTheme.SURFACE_WHITE);
    tabs.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

    tabs.addTab("Login History & Audit Stream", buildLoginHistoryTab());
    tabs.addTab("Friend Network & Connections", buildFriendNetworkTab());
    return tabs;
  }

  private JPanel buildLoginHistoryTab() {
    JPanel tab = new JPanel(new BorderLayout(0, 12));
    tab.setBackground(ChatTheme.CANVAS_BG);
    tab.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

    // Top bar with KPI and Scope Selector
    JPanel topBar = new JPanel(new BorderLayout(12, 0));
    topBar.setOpaque(false);

    JPanel kpiRow = new JPanel(new GridLayout(1, 2, 12, 0));
    kpiRow.setOpaque(false);
    kpiRow.add(
        AdminUIComponents.createKpiCard(
            "Recorded Sessions",
            totalLoginsValueLabel,
            "Chronological login events",
            ChatTheme.PRIMARY));
    kpiRow.add(
        AdminUIComponents.createKpiCard(
            "App Launch Count",
            new JLabel(user.getAppOpenCount() + " opens"),
            "Total application opens by @" + user.getUsername(),
            ChatTheme.SUCCESS_DOT));

    JPanel scopeControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    scopeControl.setOpaque(false);
    scopeControl.add(sortDirectionCombo);
    scopeControl.add(loginScopeCombo);

    topBar.add(kpiRow, BorderLayout.CENTER);
    topBar.add(scopeControl, BorderLayout.EAST);

    // Chronological Login Table
    JTable loginTable = new JTable(loginTableModel);
    AdminUIComponents.styleTable(loginTable);
    loginTable.getColumnModel().getColumn(0).setPreferredWidth(180);
    loginTable.getColumnModel().getColumn(1).setPreferredWidth(140);
    loginTable.getColumnModel().getColumn(2).setPreferredWidth(180);
    loginTable.getColumnModel().getColumn(3).setPreferredWidth(130);
    loginTable
        .getColumnModel()
        .getColumn(3)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    pageControls.setOpaque(false);
    pageInfoLabel.setFont(ChatTheme.FONT_SMALL);
    pageInfoLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    pageControls.add(refreshBtn);
    pageControls.add(pageInfoLabel);
    pageControls.add(prevPageBtn);
    pageControls.add(nextPageBtn);

    loginStatusLabel.setFont(ChatTheme.FONT_SMALL);
    loginStatusLabel.setForeground(ChatTheme.DANGER_TEXT);
    JPanel paginationBar = new JPanel(new BorderLayout(8, 0));
    paginationBar.setOpaque(false);
    paginationBar.add(loginStatusLabel, BorderLayout.CENTER);
    paginationBar.add(pageControls, BorderLayout.EAST);

    tab.add(topBar, BorderLayout.NORTH);
    tab.add(AdminUIComponents.wrapTableInScrollPane(loginTable), BorderLayout.CENTER);
    tab.add(paginationBar, BorderLayout.SOUTH);
    return tab;
  }

  private JPanel buildFriendNetworkTab() {
    JPanel tab = new JPanel(new BorderLayout(0, 12));
    tab.setBackground(ChatTheme.CANVAS_BG);
    tab.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

    // Two Metric Cards: Direct Friends & Friends-of-Friends
    JPanel kpiGrid = new JPanel(new GridLayout(1, 2, 14, 0));
    kpiGrid.setOpaque(false);
    kpiGrid.add(
        AdminUIComponents.createKpiCard(
            "Direct Friends (1st-Degree)",
            new JLabel(user.getDirectFriendCount() + " Users"),
            "Confirmed direct friend relationships",
            ChatTheme.PRIMARY));
    kpiGrid.add(
        AdminUIComponents.createKpiCard(
            "Friends-of-Friends (2nd-Degree)",
            new JLabel(user.getFriendsOfFriendsCount() + " Extended"),
            "Second-degree friend network reach",
            ChatTheme.SUCCESS_DOT));

    DefaultTableModel friendModel =
        new DefaultTableModel(FRIEND_COLUMNS, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    List<FriendConnection> friends = dataStore.getFriendsForUser(user.getUsername());
    for (FriendConnection f : friends) {
      friendModel.addRow(
          new Object[] {
            "@" + f.friendUsername(),
            f.friendFullName(),
            f.mutualFriendsCount() + " mutual friends",
            f.online() ? "Online" : "Offline"
          });
    }

    JTable friendTable = new JTable(friendModel);
    AdminUIComponents.styleTable(friendTable);
    friendTable.getColumnModel().getColumn(0).setPreferredWidth(180);
    friendTable.getColumnModel().getColumn(1).setPreferredWidth(220);
    friendTable.getColumnModel().getColumn(2).setPreferredWidth(180);
    friendTable.getColumnModel().getColumn(3).setPreferredWidth(140);
    friendTable
        .getColumnModel()
        .getColumn(3)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    tab.add(kpiGrid, BorderLayout.NORTH);
    tab.add(AdminUIComponents.wrapTableInScrollPane(friendTable), BorderLayout.CENTER);
    return tab;
  }

  private JPanel buildDialogFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(ChatTheme.SURFACE_WHITE);
    footer.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ChatTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));

    JLabel hint =
        new JLabel("Audit records ordered chronologically (newest login timestamp first)");
    hint.setFont(ChatTheme.FONT_SMALL);
    hint.setForeground(ChatTheme.TEXT_MUTED);

    JButton closeBtn = ChatTheme.createPrimaryButton("Close Window");
    closeBtn.addActionListener(e -> dispose());

    footer.add(hint, BorderLayout.CENTER);
    footer.add(closeBtn, BorderLayout.EAST);
    return footer;
  }

  private void populateLoginTable() {
    boolean allUsers = loginScopeCombo.getSelectedIndex() == 1;
    Long targetUserId = allUsers ? null : parseUserId(user.getId());
    SortDirection direction =
        sortDirectionCombo.getSelectedIndex() == 1 ? SortDirection.ASC : SortDirection.DESC;
    int seq = ++loginRequestSeq; // a late response to an older request is ignored

    if (socketClient != null && socketClient.isConnected()) {
      LoginHistoryRequest req =
          new LoginHistoryRequest(
              targetUserId,
              new SortSpec("loggedInAt", direction),
              new PageRequest(currentPage, 50));
      socketClient
          .sendAsync(ActionType.ADMIN_LIST_LOGIN_HISTORY, req)
          .orTimeout(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
          .thenAccept(
              response -> {
                SwingUtilities.invokeLater(
                    () -> {
                      if (seq != loginRequestSeq) {
                        return;
                      }
                      if (response != null && response.success() && response.data() != null) {
                        try {
                          PageResult<LoginHistoryEntry> pageResult =
                              mapper.readValue(
                                  mapper.treeAsTokens(response.data()),
                                  new TypeReference<>() {});
                          loginTableModel.setRowCount(0);
                          for (LoginHistoryEntry entry : pageResult.items()) {
                            String status = entry.loggedOutAt() != null ? "Closed" : "Active";
                            loginTableModel.addRow(
                                new Object[] {
                                  formatLoginTime(entry.loggedInAt()),
                                  "@" + entry.username(),
                                  entry.fullName(),
                                  status
                                });
                          }
                          totalLoginsValueLabel.setText(pageResult.totalItems() + " sessions");
                          currentPage = pageResult.page();
                          totalPages = Math.max(1, pageResult.totalPages());
                          pageInfoLabel.setText(
                              String.format(
                                  "Page %d of %d (%d items)",
                                  currentPage + 1, totalPages, pageResult.totalItems()));
                          prevPageBtn.setEnabled(currentPage > 0);
                          nextPageBtn.setEnabled(currentPage < totalPages - 1);
                          loginStatusLabel.setText(" ");
                        } catch (Exception ex) {
                          showLoginError("Could not read the login history returned by the server.");
                        }
                      } else {
                        String reason =
                            response == null
                                ? "no response"
                                : response.message() != null
                                    ? response.message()
                                    : String.valueOf(response.errorCode());
                        showLoginError("Could not load login history: " + reason);
                      }
                    });
              })
          .exceptionally(
              ex -> {
                SwingUtilities.invokeLater(
                    () -> {
                      if (seq == loginRequestSeq) {
                        showLoginError("The server did not respond. Please try again.");
                      }
                    });
                return null;
              });
    } else {
      showLoginError("Not connected to the server.");
    }
  }

  /** Shows an error instead of data; no sample records are shown in place of real history. */
  private void showLoginError(String message) {
    loginTableModel.setRowCount(0);
    totalLoginsValueLabel.setText("0 sessions");
    currentPage = 0;
    totalPages = 1;
    pageInfoLabel.setText("Page 1 of 1 (0 items)");
    prevPageBtn.setEnabled(false);
    nextPageBtn.setEnabled(false);
    loginStatusLabel.setText(message);
  }

  /** Formats a login time in {@link #DISPLAY_ZONE}, whatever offset the server sent. */
  static String formatLoginTime(OffsetDateTime time) {
    return time == null ? "" : time.atZoneSameInstant(DISPLAY_ZONE).format(LOGIN_TIME_FMT);
  }

  private static Long parseUserId(String rawId) {
    if (rawId == null || rawId.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(rawId);
    } catch (NumberFormatException e) {
      String digits = rawId.replaceAll("\\D+", "");
      return digits.isEmpty() ? null : Long.parseLong(digits);
    }
  }

  private static String extractInitials(String fullName) {
    if (fullName == null || fullName.isBlank()) {
      return "U";
    }
    String[] parts = fullName.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0].substring(0, 1).toUpperCase();
    }
    return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
  }
}
