package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class GroupMembersDialog extends JPanel {
  public enum MemberAction {
    REMOVE,
    TOGGLE_ADMIN
  }

  private final GroupConversation group;
  private final UserProfile currentUser;
  private final List<UserProfile> directory;
  private final BiPredicate<GroupMember, MemberAction> confirmationHandler;
  private final JPanel memberList = new JPanel();
  private final JComboBox<UserProfile> addMemberSelector = new JComboBox<>();
  private final JLabel feedbackLabel = new JLabel(" ");
  private JDialog dialog;

  public GroupMembersDialog(
      GroupConversation group,
      UserProfile currentUser,
      List<UserProfile> directory,
      BiPredicate<GroupMember, MemberAction> confirmationHandler) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    this.group = Objects.requireNonNull(group, "group");
    this.currentUser = Objects.requireNonNull(currentUser, "currentUser");
    this.directory = new ArrayList<>(Objects.requireNonNull(directory, "directory"));
    this.confirmationHandler =
        Objects.requireNonNull(confirmationHandler, "confirmationHandler");
    buildContent();
    refresh();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Group Members", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new java.awt.Dimension(560, 440));
    dialog.setSize(560, 480);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JComboBox<UserProfile> getAddMemberSelector() {
    return addMemberSelector;
  }

  JPanel getMemberList() {
    return memberList;
  }

  void addSelectedMember() {
    if (!isCurrentUserAdmin()) {
      feedbackLabel.setText("Only group administrators can add members.");
      return;
    }
    UserProfile selected = (UserProfile) addMemberSelector.getSelectedItem();
    if (selected == null) {
      feedbackLabel.setText("No eligible members are available to add.");
      return;
    }
    if (group.containsMember(selected.username())) {
      feedbackLabel.setText("This user is already a member.");
      return;
    }
    group.addMember(selected);
    feedbackLabel.setText("Member added to the sample group.");
    refresh();
  }

  boolean performMemberAction(String username, MemberAction action) {
    if (!isCurrentUserAdmin()) {
      feedbackLabel.setText("Only group administrators can manage members.");
      return false;
    }
    GroupMember member =
        group.members().stream()
            .filter(candidate -> candidate.user().username().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    if (member == null) {
      return false;
    }
    if (action == MemberAction.REMOVE) {
      if (!confirmationHandler.test(member, action)) {
        return false;
      }
      boolean removed = group.removeMember(username);
      refresh();
      return removed;
    }

    GroupMember.Role nextRole =
        member.role() == GroupMember.Role.ADMIN
            ? GroupMember.Role.MEMBER
            : GroupMember.Role.ADMIN;
    group.setRole(username, nextRole);
    refresh();
    return true;
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_LG));
    JLabel title = new JLabel("Members: " + group.name(), SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    addMemberSelector.setRenderer(new javax.swing.DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
        if (value instanceof UserProfile profile) {
          value = profile.fullName() + "  •  @" + profile.username();
        }
        return super.getListCellRendererComponent(list, value, index, selected, focus);
      }
    });
    JButton addButton = ChatTheme.createPrimaryButton("Add Member");
    addButton.addActionListener(event -> addSelectedMember());
    JPanel addRow = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, 0));
    addRow.setOpaque(false);
    addRow.add(addMemberSelector, BorderLayout.CENTER);
    addRow.add(addButton, BorderLayout.EAST);

    memberList.setLayout(new javax.swing.BoxLayout(memberList, javax.swing.BoxLayout.Y_AXIS));
    memberList.setBackground(ChatTheme.SURFACE_WHITE);
    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    JScrollPane memberScroll = new JScrollPane(memberList);
    memberScroll.setBorder(BorderFactory.createEmptyBorder());
    content.add(memberScroll, BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.TEXT_MUTED);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(addRow, BorderLayout.SOUTH);
    boolean administrator = isCurrentUserAdmin();
    addMemberSelector.setEnabled(administrator);
    addButton.setEnabled(administrator);
    if (!administrator) {
      feedbackLabel.setText("Only group administrators can manage members.");
    }
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void refresh() {
    memberList.removeAll();
    for (GroupMember member : group.members()) {
      memberList.add(createMemberRow(member));
      memberList.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_XS));
    }
    addMemberSelector.removeAllItems();
    directory.stream()
        .filter(profile -> group.members().stream()
            .noneMatch(member -> member.user().username().equalsIgnoreCase(profile.username())))
        .forEach(addMemberSelector::addItem);
    memberList.revalidate();
    memberList.repaint();
  }

  private JPanel createMemberRow(GroupMember member) {
    JPanel row = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    row.setBackground(ChatTheme.CANVAS_BG);
    row.setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_SM, ChatTheme.SPACE_SM, ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    JLabel identity = new JLabel(
        member.user().fullName() + "  •  @" + member.user().username() + "  •  " + member.role());
    identity.setFont(ChatTheme.FONT_BODY);
    identity.setForeground(ChatTheme.TEXT_PRIMARY);
    JButton roleButton = ChatTheme.createSecondaryButton(
        member.role() == GroupMember.Role.ADMIN ? "Remove Admin Role" : "Make Admin");
    roleButton.addActionListener(
        event -> performMemberAction(member.user().username(), MemberAction.TOGGLE_ADMIN));
    JButton removeButton = ChatTheme.createDangerButton("Remove from Group");
    removeButton.addActionListener(
        event -> performMemberAction(member.user().username(), MemberAction.REMOVE));
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ChatTheme.SPACE_XS, 0));
    actions.setOpaque(false);
    boolean administrator = isCurrentUserAdmin();
    roleButton.setEnabled(administrator);
    removeButton.setEnabled(administrator);
    if (administrator) {
      actions.add(roleButton);
      actions.add(removeButton);
    }
    row.add(identity, BorderLayout.CENTER);
    row.add(actions, BorderLayout.EAST);
    return row;
  }

  private boolean isCurrentUserAdmin() {
    return group.members().stream()
        .anyMatch(
            member ->
                member.user().username().equalsIgnoreCase(currentUser.username())
                    && member.role() == GroupMember.Role.ADMIN);
  }

}
