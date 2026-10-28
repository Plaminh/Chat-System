package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public final class GroupsPanel extends JPanel {
  private final UserProfile currentUser;
  private final List<GroupConversation> groups;
  private final List<UserProfile> directory;
  private final Set<String> blockedUsernames;
  private final BiPredicate<GroupConversation, GroupMembersDialog.MemberAction>
      memberConfirmationHandler;
  private final Consumer<GroupConversation> openChatHandler;
  private final JPanel groupList = new JPanel();

  public GroupsPanel(
      UserProfile currentUser,
      List<GroupConversation> groups,
      List<UserProfile> directory,
      Set<String> blockedUsernames,
      BiPredicate<GroupConversation, GroupMembersDialog.MemberAction> memberConfirmationHandler,
      Consumer<GroupConversation> openChatHandler) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.currentUser = Objects.requireNonNull(currentUser, "currentUser");
    this.groups = new ArrayList<>(Objects.requireNonNull(groups, "groups"));
    this.directory = new ArrayList<>(Objects.requireNonNull(directory, "directory"));
    this.blockedUsernames = Objects.requireNonNull(blockedUsernames, "blockedUsernames");
    this.memberConfirmationHandler =
        Objects.requireNonNull(memberConfirmationHandler, "memberConfirmationHandler");
    this.openChatHandler = Objects.requireNonNull(openChatHandler, "openChatHandler");
    buildContent();
    renderGroups();
  }

  public static List<GroupConversation> createSampleGroups(
      UserProfile currentUser, List<UserProfile> directory) {
    UserProfile other =
        directory.stream()
            .filter(profile -> !profile.username().equalsIgnoreCase(currentUser.username()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sample group needs another user"));
    return List.of(
        new GroupConversation(
            "group-design-team",
            "Nhóm trò chuyện mẫu",
            List.of(
                new com.chat.user.model.GroupMember(
                    currentUser, com.chat.user.model.GroupMember.Role.ADMIN),
                new com.chat.user.model.GroupMember(
                    other, com.chat.user.model.GroupMember.Role.MEMBER)),
            List.of()));
  }

  List<GroupConversation> getGroups() {
    return List.copyOf(groups);
  }

  void addGroup(GroupConversation group) {
    Objects.requireNonNull(group, "group");
    if (groups.stream().anyMatch(existing -> existing.groupId().equals(group.groupId()))) {
      return;
    }
    groups.add(group);
    renderGroups();
  }

  void renameGroup(GroupConversation group, String name) {
    Objects.requireNonNull(group, "group");
    group.rename(name);
    renderGroups();
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    JLabel title = new JLabel("Hội nhóm");
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JButton create = UserUiTheme.createPrimaryButton("Tạo nhóm");
    create.addActionListener(event -> openCreateDialog());
    JPanel heading = new JPanel(new BorderLayout());
    heading.setOpaque(false);
    heading.add(title, BorderLayout.WEST);
    heading.add(create, BorderLayout.EAST);
    groupList.setLayout(new javax.swing.BoxLayout(groupList, javax.swing.BoxLayout.Y_AXIS));
    groupList.setBackground(UserUiTheme.CANVAS_BG);
    JScrollPane scroll = new JScrollPane(groupList);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(UserUiTheme.CANVAS_BG);
    add(heading, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  private void openCreateDialog() {
    List<UserProfile> allowedDirectory =
        directory.stream().filter(profile -> !isBlocked(profile.username())).toList();
    CreateGroupDialog dialog =
        new CreateGroupDialog(currentUser, allowedDirectory, this::addGroup);
    dialog.createDialog(javax.swing.SwingUtilities.getWindowAncestor(this)).setVisible(true);
  }

  private void renderGroups() {
    groupList.removeAll();
    if (groups.isEmpty()) {
      JLabel empty = new JLabel("Chưa có nhóm trò chuyện.", SwingConstants.CENTER);
      empty.setFont(UserUiTheme.FONT_BODY);
      empty.setForeground(UserUiTheme.TEXT_MUTED);
      groupList.add(empty);
    } else {
      for (GroupConversation group : groups) {
        groupList.add(createGroupRow(group));
        groupList.add(javax.swing.Box.createVerticalStrut(UserUiTheme.SPACE_SM));
      }
    }
    groupList.revalidate();
    groupList.repaint();
  }

  private JPanel createGroupRow(GroupConversation group) {
    JPanel row = UserUiTheme.createCardPanel();
    row.setLayout(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    JLabel info = new JLabel(
        group.name() + "  •  " + group.members().size() + " thành viên");
    info.setFont(UserUiTheme.FONT_BODY_BOLD);
    info.setForeground(UserUiTheme.TEXT_PRIMARY);
    JButton chat = UserUiTheme.createPrimaryButton("Mở trò chuyện");
    chat.addActionListener(event -> openChatHandler.accept(group));
    JButton members = UserUiTheme.createSecondaryButton("Thành viên");
    members.addActionListener(event -> openMembersDialog(group));
    JButton rename = UserUiTheme.createSecondaryButton("Đổi tên");
    rename.addActionListener(event -> promptRename(group));
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(chat);
    actions.add(members);
    actions.add(rename);
    row.add(info, BorderLayout.CENTER);
    row.add(actions, BorderLayout.EAST);
    return row;
  }

  private void openMembersDialog(GroupConversation group) {
    GroupMembersDialog dialog =
        new GroupMembersDialog(
            group,
            directory.stream().filter(profile -> !isBlocked(profile.username())).toList(),
            (member, action) -> memberConfirmationHandler.test(group, action));
    dialog.createDialog(javax.swing.SwingUtilities.getWindowAncestor(this)).setVisible(true);
  }

  private void promptRename(GroupConversation group) {
    String name =
        JOptionPane.showInputDialog(this, "Nhập tên nhóm mới:", group.name());
    if (name == null) {
      return;
    }
    try {
      renameGroup(group, name);
    } catch (IllegalArgumentException exception) {
      JOptionPane.showMessageDialog(
          this, exception.getMessage(), "Tên nhóm không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }
  }

  private boolean isBlocked(String username) {
    return blockedUsernames.stream().anyMatch(blocked -> blocked.equalsIgnoreCase(username));
  }
}
