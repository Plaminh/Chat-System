package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.FriendEntry;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class FriendsPanel extends JPanel {
  public enum RelationshipAction {
    UNFRIEND,
    BLOCK
  }

  private final List<FriendEntry> friends;
  private final Set<String> blockedUsernames;
  private final BiPredicate<FriendEntry, RelationshipAction> confirmationHandler;
  private final Consumer<FriendEntry> openChatHandler;
  private final JTextField searchField = UserUiTheme.createTextField(24);
  private final JCheckBox onlineOnly = new JCheckBox("Chỉ hiển thị đang trực tuyến");
  private final JPanel friendList = new JPanel();
  private List<FriendEntry> visibleFriends = List.of();

  public FriendsPanel(
      List<FriendEntry> friends,
      Set<String> blockedUsernames,
      BiPredicate<FriendEntry, RelationshipAction> confirmationHandler,
      Consumer<FriendEntry> openChatHandler) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.friends = new ArrayList<>(Objects.requireNonNull(friends, "friends"));
    this.blockedUsernames =
        Objects.requireNonNull(blockedUsernames, "blockedUsernames");
    this.confirmationHandler =
        Objects.requireNonNull(confirmationHandler, "confirmationHandler");
    this.openChatHandler = Objects.requireNonNull(openChatHandler, "openChatHandler");
    buildContent();
    renderFriends();
  }

  public static List<FriendEntry> createSampleFriends() {
    return List.of(
        new FriendEntry(
            sampleProfile("mai.anh", "Lê Mai Anh", "maianh@example.test"), true),
        new FriendEntry(
            sampleProfile("minh.tran", "Trần Minh", "minh@example.test"), false),
        new FriendEntry(
            sampleProfile("lan.pham", "Phạm Lan", "lan@example.test"), true));
  }

  public static Set<String> createBlockedUsernames() {
    return new HashSet<>();
  }

  JTextField getSearchField() {
    return searchField;
  }

  JCheckBox getOnlineOnlyToggle() {
    return onlineOnly;
  }

  List<FriendEntry> getVisibleFriends() {
    return visibleFriends;
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));

    JLabel title = new JLabel("Bạn bè");
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("Tìm bạn bè, xem trạng thái và mở cuộc trò chuyện.");
    subtitle.setFont(UserUiTheme.FONT_BODY);
    subtitle.setForeground(UserUiTheme.TEXT_MUTED);
    JPanel heading = new JPanel(new GridLayout(0, 1, 0, UserUiTheme.SPACE_XS));
    heading.setOpaque(false);
    heading.add(title);
    heading.add(subtitle);

    searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên");
    onlineOnly.setFont(UserUiTheme.FONT_BODY);
    onlineOnly.setForeground(UserUiTheme.TEXT_SECONDARY);
    onlineOnly.setOpaque(false);
    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, UserUiTheme.SPACE_SM, 0));
    filters.setOpaque(false);
    filters.add(searchField);
    filters.add(onlineOnly);

    JPanel top = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    top.setOpaque(false);
    top.add(heading, BorderLayout.NORTH);
    top.add(filters, BorderLayout.SOUTH);

    friendList.setLayout(new javax.swing.BoxLayout(friendList, javax.swing.BoxLayout.Y_AXIS));
    friendList.setBackground(UserUiTheme.CANVAS_BG);
    JScrollPane scrollPane = new JScrollPane(friendList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getViewport().setBackground(UserUiTheme.CANVAS_BG);
    add(top, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        renderFriends();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        renderFriends();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        renderFriends();
      }
    });
    onlineOnly.addActionListener(event -> renderFriends());
  }

  private void renderFriends() {
    String filter = searchField.getText().trim().toLowerCase(Locale.ROOT);
    boolean onlyOnline = onlineOnly.isSelected();
    visibleFriends =
        friends.stream()
            .filter(friend -> !isBlocked(friend.username()))
            .filter(friend -> friend.fullName().toLowerCase(Locale.ROOT).contains(filter))
            .filter(friend -> !onlyOnline || friend.online())
            .toList();

    friendList.removeAll();
    if (visibleFriends.isEmpty()) {
      JLabel empty = new JLabel("Không tìm thấy bạn bè phù hợp.");
      empty.setFont(UserUiTheme.FONT_BODY);
      empty.setForeground(UserUiTheme.TEXT_MUTED);
      empty.setBorder(BorderFactory.createEmptyBorder(UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD,
          UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD));
      friendList.add(empty);
    } else {
      for (FriendEntry friend : visibleFriends) {
        friendList.add(createFriendRow(friend));
        friendList.add(javax.swing.Box.createVerticalStrut(UserUiTheme.SPACE_SM));
      }
    }
    friendList.revalidate();
    friendList.repaint();
  }

  private JPanel createFriendRow(FriendEntry friend) {
    JPanel row = UserUiTheme.createCardPanel();
    row.setLayout(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    JPanel identity = new JPanel(new GridLayout(0, 1, 0, UserUiTheme.SPACE_XS));
    identity.setOpaque(false);
    JLabel name = new JLabel(friend.fullName());
    name.setFont(UserUiTheme.FONT_BODY_BOLD);
    name.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel handle = new JLabel(
        "@" + friend.username() + "  •  " + (friend.online() ? "Đang trực tuyến" : "Ngoại tuyến"));
    handle.setFont(UserUiTheme.FONT_SMALL);
    handle.setForeground(friend.online() ? UserUiTheme.SUCCESS : UserUiTheme.TEXT_SECONDARY);
    identity.add(name);
    identity.add(handle);

    JButton chatButton = UserUiTheme.createPrimaryButton("Nhắn tin");
    chatButton.addActionListener(event -> openChatHandler.accept(friend));
    JButton unfriendButton = UserUiTheme.createSecondaryButton("Hủy kết bạn");
    unfriendButton.addActionListener(
        event -> handleRelationshipAction(friend, RelationshipAction.UNFRIEND));
    JButton blockButton = UserUiTheme.createSecondaryButton("Chặn");
    blockButton.addActionListener(
        event -> handleRelationshipAction(friend, RelationshipAction.BLOCK));

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UserUiTheme.SPACE_XS, 0));
    actions.setOpaque(false);
    actions.add(chatButton);
    actions.add(unfriendButton);
    actions.add(blockButton);
    row.add(identity, BorderLayout.CENTER);
    row.add(actions, BorderLayout.EAST);
    return row;
  }

  private void handleRelationshipAction(FriendEntry friend, RelationshipAction action) {
    if (!confirmationHandler.test(friend, action)) {
      return;
    }
    friends.remove(friend);
    if (action == RelationshipAction.BLOCK) {
      blockedUsernames.add(friend.username());
    }
    renderFriends();
  }

  private boolean isBlocked(String username) {
    return blockedUsernames.stream().anyMatch(blocked -> blocked.equalsIgnoreCase(username));
  }

  private static UserProfile sampleProfile(String username, String fullName, String email) {
    return new UserProfile(
        username, fullName, email, "Sample address", LocalDate.of(2000, 1, 1), "Not specified");
  }
}
