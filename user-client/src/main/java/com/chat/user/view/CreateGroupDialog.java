package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class CreateGroupDialog extends JPanel {
  private final UserProfile creator;
  private final List<UserProfile> directory;
  private final Consumer<GroupConversation> createdHandler;
  private final JTextField groupNameField = UserUiTheme.createTextField(28);
  private final JList<UserProfile> participantList;
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private JDialog dialog;
  private GroupConversation createdGroup;

  public CreateGroupDialog(
      UserProfile creator, List<UserProfile> directory, Consumer<GroupConversation> createdHandler) {
    this(creator, directory, List.of(), createdHandler);
  }

  public CreateGroupDialog(
      UserProfile creator,
      List<UserProfile> directory,
      List<UserProfile> preselectedMembers,
      Consumer<GroupConversation> createdHandler) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.creator = Objects.requireNonNull(creator, "creator");
    this.directory = new ArrayList<>(Objects.requireNonNull(directory, "directory"));
    Objects.requireNonNull(preselectedMembers, "preselectedMembers");
    this.createdHandler = Objects.requireNonNull(createdHandler, "createdHandler");
    List<UserProfile> selectableUsers =
        this.directory.stream()
            .filter(profile -> !profile.username().equalsIgnoreCase(creator.username()))
            .toList();
    participantList = new JList<>(selectableUsers.toArray(UserProfile[]::new));
    int[] selectedIndices =
        IntStream.range(0, selectableUsers.size())
            .filter(
                index ->
                    preselectedMembers.stream()
                        .anyMatch(
                            selected ->
                                selected.username()
                                    .equalsIgnoreCase(selectableUsers.get(index).username())))
            .toArray();
    participantList.setSelectedIndices(selectedIndices);
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Create Group Chat", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(560, 560));
    dialog.setSize(560, 600);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextField getGroupNameField() {
    return groupNameField;
  }

  JList<UserProfile> getParticipantList() {
    return participantList;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  GroupConversation getCreatedGroup() {
    return createdGroup;
  }

  void submitCreation() {
    String name = groupNameField.getText().trim();
    List<UserProfile> selected = participantList.getSelectedValuesList();
    if (name.isEmpty()) {
      showFeedback("Enter a group name to continue.", UserUiTheme.DANGER);
      return;
    }
    if (selected.isEmpty()) {
      showFeedback("Select at least one other user to create a group.", UserUiTheme.DANGER);
      return;
    }

    List<GroupMember> members = new ArrayList<>();
    members.add(new GroupMember(creator, GroupMember.Role.ADMIN));
    selected.forEach(user -> members.add(new GroupMember(user, GroupMember.Role.MEMBER)));
    createdGroup =
        new GroupConversation("group-" + UUID.randomUUID(), name, members, List.of());
    createdHandler.accept(createdGroup);
    showFeedback("Sample group created for this session.", UserUiTheme.SUCCESS);
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void buildContent() {
    setBackground(UserUiTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG));
    groupNameField.putClientProperty("JTextField.placeholderText", "Group name");
    participantList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    participantList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          JList<?> list, Object value, int index, boolean selected, boolean focus) {
        UserProfile profile = (UserProfile) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(
            list, profile.fullName() + "  •  @" + profile.username(), index, selected, focus);
        label.setBorder(BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM,
            UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
        return label;
      }
    });

    JLabel title = new JLabel("Create a New Group Chat", SwingConstants.CENTER);
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel nameLabel = new JLabel("Group name");
    nameLabel.setFont(UserUiTheme.FONT_BODY_BOLD);
    nameLabel.setForeground(UserUiTheme.TEXT_SECONDARY);
    JLabel participantLabel = new JLabel("Add at least one other user");
    participantLabel.setFont(UserUiTheme.FONT_BODY_BOLD);
    participantLabel.setForeground(UserUiTheme.TEXT_SECONDARY);

    JPanel form = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    form.setOpaque(false);
    JPanel nameGroup = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
    nameGroup.setOpaque(false);
    nameGroup.add(nameLabel, BorderLayout.NORTH);
    nameGroup.add(groupNameField, BorderLayout.CENTER);
    JPanel participantGroup = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
    participantGroup.setOpaque(false);
    participantGroup.add(participantLabel, BorderLayout.NORTH);
    participantGroup.add(new JScrollPane(participantList), BorderLayout.CENTER);
    form.add(nameGroup, BorderLayout.NORTH);
    form.add(participantGroup, BorderLayout.CENTER);

    feedbackLabel.setFont(UserUiTheme.FONT_SMALL);
    feedbackLabel.setForeground(UserUiTheme.DANGER);
    JButton cancel = UserUiTheme.createSecondaryButton("Cancel");
    cancel.addActionListener(event -> closeDialog());
    JButton create = UserUiTheme.createPrimaryButton("Create Sample Group");
    create.addActionListener(event -> submitCreation());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(feedbackLabel);
    actions.add(cancel);
    actions.add(create);

    add(title, BorderLayout.NORTH);
    add(form, BorderLayout.CENTER);
    add(actions, BorderLayout.SOUTH);
  }

  private void closeDialog() {
    createdGroup = null;
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
