package com.chat.user.view;

import com.chat.user.ui.ChatTheme;
import com.chat.user.model.FriendRequest;
import com.chat.user.controller.FriendsController;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class FriendRequestsSearchPanel extends JPanel {
  private final List<FriendRequest> requests;
  private final List<UserProfile> directory;
  private final Set<String> blockedByMeUsernames;
  private final Set<String> blockedMeUsernames;
  private final Set<String> friendUsernames;
  private final Set<String> outgoingRequestUsernames;
  private final Consumer<UserProfile> openChatHandler;
  private final FriendsController controller = new FriendsController();
  private final Consumer<UserProfile> selectForGroupHandler;
  private final JTextField requestFilterField = ChatTheme.createTextField(24);
  private final JTextField searchField = ChatTheme.createTextField(24);
  private final JPanel requestList = new JPanel();
  private final JPanel searchResults = new JPanel();
  private List<FriendRequest> visibleRequests = List.of();
  private List<UserProfile> visibleSearchResults = List.of();

  public FriendRequestsSearchPanel(
      List<FriendRequest> requests,
      List<UserProfile> directory,
      Set<String> blockedUsernames,
      Consumer<UserProfile> openChatHandler,
      Consumer<UserProfile> selectForGroupHandler) {
    this(
        requests,
        directory,
        blockedUsernames,
        Set.of(),
        new HashSet<>(),
        new HashSet<>(),
        openChatHandler,
        selectForGroupHandler);
  }

  public FriendRequestsSearchPanel(
      List<FriendRequest> requests,
      List<UserProfile> directory,
      Set<String> blockedByMeUsernames,
      Set<String> blockedMeUsernames,
      Set<String> friendUsernames,
      Set<String> outgoingRequestUsernames,
      Consumer<UserProfile> openChatHandler,
      Consumer<UserProfile> selectForGroupHandler) {
    super(new BorderLayout());
    this.requests = new ArrayList<>(Objects.requireNonNull(requests, "requests"));
    this.directory = new ArrayList<>(Objects.requireNonNull(directory, "directory"));
    this.blockedByMeUsernames =
        Objects.requireNonNull(blockedByMeUsernames, "blockedByMeUsernames");
    this.blockedMeUsernames = Objects.requireNonNull(blockedMeUsernames, "blockedMeUsernames");
    this.friendUsernames = Objects.requireNonNull(friendUsernames, "friendUsernames");
    this.outgoingRequestUsernames =
        Objects.requireNonNull(outgoingRequestUsernames, "outgoingRequestUsernames");
    this.openChatHandler = Objects.requireNonNull(openChatHandler, "openChatHandler");
    this.selectForGroupHandler =
        Objects.requireNonNull(selectForGroupHandler, "selectForGroupHandler");
    buildContent();
    renderRequests();
    renderSearchResults();
  }

  public static List<FriendRequest> createSampleRequests() {
    return List.of(
        new FriendRequest(sampleProfile("quang.vo", "Võ Quang", "quang@example.test"),
            FriendRequest.Status.PENDING),
        new FriendRequest(sampleProfile("thu.nguyen", "Nguyễn Thu", "thu@example.test"),
            FriendRequest.Status.PENDING));
  }

  public static List<UserProfile> createSampleDirectory() {
    return List.of(
        sampleProfile("quang.vo", "Võ Quang", "quang@example.test"),
        sampleProfile("thu.nguyen", "Nguyễn Thu", "thu@example.test"),
        sampleProfile("minh.tran", "Trần Minh", "minh@example.test"),
        sampleProfile("blocked.user", "Blocked User", "blocked@example.test"));
  }

  JTextField getRequestFilterField() {
    return requestFilterField;
  }

  JTextField getSearchField() {
    return searchField;
  }

  List<FriendRequest> getVisibleRequests() {
    return visibleRequests;
  }

  List<FriendRequest> getRequests() {
    return List.copyOf(requests);
  }

  List<UserProfile> getVisibleSearchResults() {
    return visibleSearchResults;
  }

  private void buildContent() {
    setBackground(ChatTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));

    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(ChatTheme.FONT_BODY_BOLD);
    tabs.addTab("Friend Requests", buildRequestsTab());
    tabs.addTab("Find People", buildSearchTab());
    add(tabs, BorderLayout.CENTER);

    requestFilterField.putClientProperty("JTextField.placeholderText", "Filter by sender name");
    searchField.putClientProperty("JTextField.placeholderText", "Username or full name");
    requestFilterField.getDocument().addDocumentListener(documentListener(this::renderRequests));
    searchField.getDocument().addDocumentListener(documentListener(this::renderSearchResults));
  }

  private JPanel buildRequestsTab() {
    JPanel panel = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    panel.setBackground(ChatTheme.CANVAS_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_MD, 0, 0, 0));
    JLabel title = new JLabel("Friend Requests");
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(requestFilterField, BorderLayout.SOUTH);
    requestList.setLayout(new javax.swing.BoxLayout(requestList, javax.swing.BoxLayout.Y_AXIS));
    requestList.setBackground(ChatTheme.CANVAS_BG);
    JScrollPane scroll = new JScrollPane(requestList);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(ChatTheme.CANVAS_BG);
    panel.add(heading, BorderLayout.NORTH);
    panel.add(scroll, BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildSearchTab() {
    JPanel panel = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    panel.setBackground(ChatTheme.CANVAS_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_MD, 0, 0, 0));
    JLabel title = new JLabel("Find People");
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel description = new JLabel("Search by username or full name.");
    description.setFont(ChatTheme.FONT_BODY);
    description.setForeground(ChatTheme.TEXT_MUTED);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    heading.setOpaque(false);
    JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, ChatTheme.SPACE_XS));
    titleBlock.setOpaque(false);
    titleBlock.add(title);
    titleBlock.add(description);
    heading.add(titleBlock, BorderLayout.NORTH);
    heading.add(searchField, BorderLayout.SOUTH);
    searchResults.setLayout(new javax.swing.BoxLayout(searchResults, javax.swing.BoxLayout.Y_AXIS));
    searchResults.setBackground(ChatTheme.CANVAS_BG);
    JScrollPane scroll = new JScrollPane(searchResults);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(ChatTheme.CANVAS_BG);
    panel.add(heading, BorderLayout.NORTH);
    panel.add(scroll, BorderLayout.CENTER);
    return panel;
  }

  private void renderRequests() {
    String filter = requestFilterField.getText().trim().toLowerCase(Locale.ROOT);
    visibleRequests =
        controller.filterFriendRequests(
            requests, blockedByMeUsernames, blockedMeUsernames, filter);
    requestList.removeAll();
    if (visibleRequests.isEmpty()) {
      requestList.add(emptyLabel("No matching friend requests."));
    } else {
      for (FriendRequest request : visibleRequests) {
        requestList.add(createRequestRow(request));
        requestList.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
      }
    }
    requestList.revalidate();
    requestList.repaint();
  }

  private void renderSearchResults() {
    String entered = searchField.getText().trim().toLowerCase(Locale.ROOT);
    visibleSearchResults =
        controller.searchUsers(directory, blockedByMeUsernames, blockedMeUsernames, entered);
    searchResults.removeAll();
    if (entered.isEmpty()) {
      searchResults.add(emptyLabel("Enter a username or full name to start searching."));
    } else if (visibleSearchResults.isEmpty()) {
      searchResults.add(emptyLabel("No matching users found."));
    } else {
      for (UserProfile profile : visibleSearchResults) {
        searchResults.add(createSearchResultRow(profile));
        searchResults.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
      }
    }
    searchResults.revalidate();
    searchResults.repaint();
  }

  private JPanel createRequestRow(FriendRequest request) {
    JPanel row = ChatTheme.createCardPanel();
    row.setLayout(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    JLabel identity = new JLabel(
        request.requester().fullName() + "  •  @" + request.requester().username());
    identity.setFont(ChatTheme.FONT_BODY_BOLD);
    identity.setForeground(ChatTheme.TEXT_PRIMARY);
    JButton accept = ChatTheme.createPrimaryButton("Accept");
    accept.addActionListener(event -> updateRequest(request, FriendRequest.Status.ACCEPTED));
    JButton decline = ChatTheme.createSecondaryButton("Decline");
    decline.addActionListener(event -> updateRequest(request, FriendRequest.Status.DECLINED));
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ChatTheme.SPACE_XS, 0));
    actions.setOpaque(false);
    actions.add(accept);
    actions.add(decline);
    row.add(identity, BorderLayout.CENTER);
    row.add(actions, BorderLayout.EAST);
    return row;
  }

  private JPanel createSearchResultRow(UserProfile profile) {
    JPanel row = ChatTheme.createCardPanel();
    row.setLayout(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    JLabel identity = new JLabel(profile.fullName() + "  •  @" + profile.username());
    identity.setFont(ChatTheme.FONT_BODY_BOLD);
    identity.setForeground(ChatTheme.TEXT_PRIMARY);
    JButton chat = ChatTheme.createPrimaryButton("Message");
    chat.addActionListener(event -> openChatHandler.accept(profile));
    JButton addToGroup = ChatTheme.createSecondaryButton("Add to Group");
    addToGroup.addActionListener(event -> selectForGroupHandler.accept(profile));
    JButton addFriend = ChatTheme.createSecondaryButton("Add Friend");
    boolean alreadyFriend = containsUsername(friendUsernames, profile.username());
    boolean incomingRequest =
        requests.stream()
            .anyMatch(
                request ->
                    request.status() == FriendRequest.Status.PENDING
                        && request.requester().username().equalsIgnoreCase(profile.username()));
    boolean outgoingRequest = containsUsername(outgoingRequestUsernames, profile.username());
    if (alreadyFriend) {
      addFriend.setText("Friends");
    } else if (incomingRequest) {
      addFriend.setText("Request Received");
    } else if (outgoingRequest) {
      addFriend.setText("Request Sent");
    } else {
      addFriend.addActionListener(
          event -> {
            outgoingRequestUsernames.add(profile.username());
            renderSearchResults();
          });
    }
    addFriend.setEnabled(!alreadyFriend && !incomingRequest && !outgoingRequest);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ChatTheme.SPACE_XS, 0));
    actions.setOpaque(false);
    actions.add(addFriend);
    actions.add(chat);
    actions.add(addToGroup);
    row.add(identity, BorderLayout.CENTER);
    row.add(actions, BorderLayout.EAST);
    return row;
  }

  private void updateRequest(FriendRequest request, FriendRequest.Status status) {
    int index = requests.indexOf(request);
    if (index >= 0) {
      requests.set(index, request.withStatus(status));
      if (status == FriendRequest.Status.ACCEPTED) {
        friendUsernames.add(request.requester().username());
        outgoingRequestUsernames.removeIf(
            username -> username.equalsIgnoreCase(request.requester().username()));
      }
      renderRequests();
      renderSearchResults();
    }
  }

  private boolean containsUsername(Set<String> usernames, String username) {
    return usernames.stream().anyMatch(candidate -> candidate.equalsIgnoreCase(username));
  }

  private JLabel emptyLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(ChatTheme.FONT_BODY);
    label.setForeground(ChatTheme.TEXT_MUTED);
    label.setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD));
    return label;
  }

  private DocumentListener documentListener(Runnable update) {
    return new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        update.run();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        update.run();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        update.run();
      }
    };
  }

  private static UserProfile sampleProfile(String username, String fullName, String email) {
    return new UserProfile(
        username, fullName, email, "Sample address", LocalDate.of(2000, 1, 1), "Not specified");
  }
}
