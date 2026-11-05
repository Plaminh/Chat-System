package com.chat.user.view;

import com.chat.user.component.UserNavigationSidebar;
import com.chat.user.component.UserUiTheme;
import com.chat.user.controller.FriendsController;
import com.chat.user.model.Conversation;
import com.chat.user.model.FriendEntry;
import com.chat.user.model.GroupConversation;
import com.chat.user.model.UserProfile;
import com.chat.user.model.UserSessionState;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class UserMainFrame extends JPanel {
  public enum Destination {
    CHATS("Chats"),
    FRIENDS("Friends"),
    REQUESTS("Requests"),
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
  private final UserNavigationSidebar navigationSidebar;
  private final Map<Destination, JComponent> views = new EnumMap<>(Destination.class);
  private final Set<String> blockedUsernames = new HashSet<>();
  private final List<UserProfile> knownProfiles = new ArrayList<>();
  private final JLabel headerTitle = new JLabel();
  private final JLabel headerBreadcrumb = new JLabel();
  private ChatWorkspacePanel chatWorkspace;
  private GroupsPanel groupsPanel;
  private Destination activeDestination;
  private final FriendsController friendsController = new FriendsController();

  public UserMainFrame(UserSessionState sessionState) {
    super(new BorderLayout());
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    navigationSidebar =
        new UserNavigationSidebar(
            sessionState.getProfile().fullName(),
            sessionState.getProfile().username(),
            this::showDestination);
    UserUiTheme.applyGlobalDefaults();
    buildLayout();
    for (Destination destination : Destination.values()) {
      registerView(destination, createPlaceholder(destination));
    }
    installSampleViews();
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
    headerBreadcrumb.setText("Console  \u203a  Workspace  \u203a  " + destination.label());
    navigationSidebar.setActiveDestination(destination);
  }

  Destination getActiveDestination() {
    return activeDestination;
  }

  UserNavigationSidebar getNavigationSidebar() {
    return navigationSidebar;
  }

  javax.swing.JButton getNavigationButton(Destination destination) {
    return navigationSidebar.getButton(destination);
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
    workspace.add(buildStatusFooter(), BorderLayout.SOUTH);
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

    headerBreadcrumb.setFont(UserUiTheme.FONT_SMALL);
    headerBreadcrumb.setForeground(UserUiTheme.TEXT_MUTED);
    headerTitle.setFont(UserUiTheme.FONT_TITLE);
    headerTitle.setForeground(UserUiTheme.TEXT_PRIMARY);
    JPanel titleBlock = new JPanel(new BorderLayout(0, UserUiTheme.SPACE_XS));
    titleBlock.setOpaque(false);
    titleBlock.add(headerBreadcrumb, BorderLayout.NORTH);
    titleBlock.add(headerTitle, BorderLayout.SOUTH);

    JLabel sessionDate =
        new JLabel(
            "Session Date: "
                + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));
    sessionDate.setFont(UserUiTheme.FONT_SMALL);
    sessionDate.setForeground(UserUiTheme.TEXT_SECONDARY);
    sessionDate.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UserUiTheme.BORDER),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    JLabel role = new JLabel("Role: Member");
    role.setFont(UserUiTheme.FONT_SMALL);
    role.setForeground(UserUiTheme.PRIMARY);
    role.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xBFDBFE)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, UserUiTheme.SPACE_SM, 0));
    right.setOpaque(false);
    right.add(sessionDate);
    right.add(role);

    header.add(titleBlock, BorderLayout.WEST);
    header.add(right, BorderLayout.EAST);
    return header;
  }

  private JPanel buildStatusFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(UserUiTheme.SURFACE_WHITE);
    footer.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UserUiTheme.BORDER),
            BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_MD,
                UserUiTheme.SPACE_SM, UserUiTheme.SPACE_MD)));
    JLabel status = new JLabel("● Workspace Ready  |  Sample data only");
    status.setFont(UserUiTheme.FONT_SMALL);
    status.setForeground(UserUiTheme.TEXT_SECONDARY);
    JLabel version = new JLabel("Chat System — User Client UI (Version 1.0)");
    version.setFont(UserUiTheme.FONT_SMALL);
    version.setForeground(UserUiTheme.TEXT_MUTED);
    footer.add(status, BorderLayout.WEST);
    footer.add(version, BorderLayout.EAST);
    return footer;
  }

  private void installSampleViews() {
    UserProfile currentUser = sessionState.getProfile();
    List<FriendEntry> friends = FriendsPanel.createSampleFriends();
    List<UserProfile> directory = FriendRequestsSearchPanel.createSampleDirectory();
    knownProfiles.addAll(directory);
    for (FriendEntry friend : friends) {
      if (knownProfiles.stream()
          .noneMatch(profile -> profile.username().equalsIgnoreCase(friend.username()))) {
        knownProfiles.add(friend.profile());
      }
    }

    chatWorkspace =
        new ChatWorkspacePanel(
            ChatWorkspacePanel.createSampleConversations(), currentUser.username());
    chatWorkspace.setActionHandlers(
        this::openHistorySearch, this::openSpamReport, this::openWritingAssist);

    FriendsPanel friendsPanel =
        new FriendsPanel(
            friends,
            blockedUsernames,
            this::confirmFriendAction,
            friend -> openChat(friend.username(), friend.fullName()));
    FriendRequestsSearchPanel requestsPanel =
        new FriendRequestsSearchPanel(
            FriendRequestsSearchPanel.createSampleRequests(),
            directory,
            blockedUsernames,
            profile -> openChat(profile.username(), profile.fullName()),
            profile -> {
              showDestination(Destination.GROUPS);
              groupsPanel.startGroupCreationWith(profile);
            });
    List<GroupConversation> sampleGroups =
        GroupsPanel.createSampleGroups(currentUser, directory);
    groupsPanel =
        new GroupsPanel(
            currentUser,
            sampleGroups,
            directory,
            blockedUsernames,
            this::confirmGroupMemberAction,
            this::openGroupChat);
    ProfileSettingsPanel profilePanel = new ProfileSettingsPanel(sessionState);

    registerView(Destination.CHATS, chatWorkspace);
    registerView(Destination.FRIENDS, friendsPanel);
    registerView(Destination.REQUESTS, requestsPanel);
    registerView(Destination.GROUPS, groupsPanel);
    registerView(Destination.PROFILE, profilePanel);
  }

  private void openChat(String username, String displayName) {
    chatWorkspace.openConversationFor(username, displayName);
    showChatWorkspace();
  }

  private void openGroupChat(GroupConversation group) {
    GroupChatPanel groupChat = new GroupChatPanel(group, sessionState.getProfile().username());
    groupChat.setBackHandler(
        () -> {
          registerView(Destination.GROUPS, groupsPanel);
          showDestination(Destination.GROUPS);
        });
    registerView(Destination.GROUPS, groupChat);
    showDestination(Destination.GROUPS);
  }

  private void openHistorySearch() {
    ChatHistorySearchPanel historyPanel =
        new ChatHistorySearchPanel(
            chatWorkspace.getConversations(),
            chatWorkspace.getSelectedConversation(),
            this::navigateToSearchResult,
            this::confirmHistoryAction);
    historyPanel.setBackHandler(this::showChatWorkspace);
    registerView(Destination.CHATS, historyPanel);
    showDestination(Destination.CHATS);
  }

  private void navigateToSearchResult(ChatHistorySearchPanel.SearchResult result) {
    chatWorkspace.openSearchResult(result);
    showChatWorkspace();
  }

  private void showChatWorkspace() {
    registerView(Destination.CHATS, chatWorkspace);
    showDestination(Destination.CHATS);
  }

  private void openSpamReport(Conversation conversation) {
    if (!friendsController.isEligibleForSpamReport(conversation)) {
      JOptionPane.showMessageDialog(
          this,
          "You can only report an account after an interaction in a sample conversation.",
          "Report Not Available",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    UserProfile reportedUser = friendsController.resolveProfileForConversation(knownProfiles, conversation);
    new SpamReportDialog(reportedUser, true)
        .createDialog(javax.swing.SwingUtilities.getWindowAncestor(this))
        .setVisible(true);
  }
  private void openWritingAssist(String draft) {
    new WritingAssistDialog(draft, chatWorkspace::setDraftText)
        .createDialog(javax.swing.SwingUtilities.getWindowAncestor(this))
        .setVisible(true);
  }

  private boolean confirmFriendAction(
      FriendEntry friend, FriendsPanel.RelationshipAction action) {
    String actionLabel =
        action == FriendsPanel.RelationshipAction.BLOCK ? "block" : "remove as a friend";
    return JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to "
                + actionLabel
                + " @"
                + friend.username()
                + " in the sample data?",
            "Confirm Action",
            JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
  }

  private boolean confirmHistoryAction(
      ChatHistorySearchPanel.ActionConfirmation confirmation) {
    String message =
        switch (confirmation.action()) {
          case DELETE_FOR_ME ->
              "Hide "
                  + confirmation.messageCount()
                  + " message(s) from your sample UI?";
          case UNSEND ->
              "Mark "
                  + confirmation.messageCount()
                  + " message(s) as unsent in the sample UI?";
          case CLEAR_HISTORY ->
              "Clear this conversation's sample history for the current session?";
        };
    return JOptionPane.showConfirmDialog(
            this, message, "Confirm Sample Action", JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
  }

  private boolean confirmGroupMemberAction(
      GroupConversation group, GroupMembersDialog.MemberAction action) {
    if (action != GroupMembersDialog.MemberAction.REMOVE) {
      return true;
    }
    return JOptionPane.showConfirmDialog(
            this,
            "Remove this member from the sample group "
                + group.name()
                + "? This change only applies to the current session.",
            "Confirm Member Removal",
            JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
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
