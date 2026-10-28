package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class SpamReportDialog extends JPanel {
  private final UserProfile reportedUser;
  private final boolean hasPriorConversation;
  private final JTextArea reasonField = new JTextArea(5, 36);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private JDialog dialog;
  private boolean reportSubmitted;

  public SpamReportDialog(UserProfile reportedUser, boolean hasPriorConversation) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.reportedUser = Objects.requireNonNull(reportedUser, "reportedUser");
    this.hasPriorConversation = hasPriorConversation;
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Báo cáo tài khoản", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(560, 420));
    dialog.setSize(560, 460);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextArea getReasonField() {
    return reasonField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isReportSubmitted() {
    return reportSubmitted;
  }

  void submitReport() {
    if (!hasPriorConversation) {
      showFeedback(
          "Chỉ có thể báo cáo tài khoản đã có cuộc trò chuyện trước đó.",
          UserUiTheme.DANGER);
      return;
    }
    if (reasonField.getText().trim().isEmpty()) {
      showFeedback("Nhập lý do báo cáo trước khi tiếp tục.", UserUiTheme.DANGER);
      return;
    }
    reportSubmitted = true;
    showFeedback(
        "Báo cáo mẫu đã được ghi nhận trong giao diện; chưa gửi đến máy chủ.",
        UserUiTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(UserUiTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG));
    JLabel title = new JLabel("Báo cáo cuộc trò chuyện", SwingConstants.CENTER);
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel target =
        new JLabel(
            "Tài khoản được báo cáo: "
                + reportedUser.fullName()
                + "  •  @"
                + reportedUser.username());
    target.setFont(UserUiTheme.FONT_BODY_BOLD);
    target.setForeground(UserUiTheme.TEXT_SECONDARY);
    JLabel reasonLabel = new JLabel("Lý do báo cáo");
    reasonLabel.setFont(UserUiTheme.FONT_BODY_BOLD);
    reasonLabel.setForeground(UserUiTheme.TEXT_SECONDARY);
    reasonField.setFont(UserUiTheme.FONT_BODY);
    reasonField.setLineWrap(true);
    reasonField.setWrapStyleWord(true);
    reasonField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UserUiTheme.BORDER),
        BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM,
            UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM)));

    JPanel form = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    form.setOpaque(false);
    form.add(target, BorderLayout.NORTH);
    JPanel reasonGroup = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
    reasonGroup.setOpaque(false);
    reasonGroup.add(reasonLabel, BorderLayout.NORTH);
    reasonGroup.add(new JScrollPane(reasonField), BorderLayout.CENTER);
    form.add(reasonGroup, BorderLayout.CENTER);

    feedbackLabel.setFont(UserUiTheme.FONT_SMALL);
    feedbackLabel.setForeground(UserUiTheme.DANGER);
    JButton cancel = UserUiTheme.createSecondaryButton("Hủy");
    cancel.addActionListener(event -> closeDialog());
    JButton submit = UserUiTheme.createPrimaryButton("Gửi báo cáo mẫu");
    submit.addActionListener(event -> submitReport());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(feedbackLabel);
    actions.add(cancel);
    actions.add(submit);

    JPanel content = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    content.add(actions, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
