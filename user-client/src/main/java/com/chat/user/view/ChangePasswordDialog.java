package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class ChangePasswordDialog extends JPanel {
  private final JPasswordField currentPasswordField = new JPasswordField(24);
  private final JPasswordField newPasswordField = new JPasswordField(24);
  private final JPasswordField confirmPasswordField = new JPasswordField(24);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private JDialog dialog;
  private boolean changeSubmitted;

  public ChangePasswordDialog() {
    super(new BorderLayout());
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Đổi mật khẩu", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(480, 370));
    dialog.setSize(480, 390);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JPasswordField getCurrentPasswordField() {
    return currentPasswordField;
  }

  JPasswordField getNewPasswordField() {
    return newPasswordField;
  }

  JPasswordField getConfirmPasswordField() {
    return confirmPasswordField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isChangeSubmitted() {
    return changeSubmitted;
  }

  void submitChange() {
    char[] current = currentPasswordField.getPassword();
    char[] next = newPasswordField.getPassword();
    char[] confirmation = confirmPasswordField.getPassword();
    boolean currentBlank = isBlank(current);
    boolean newBlank = isBlank(next);
    boolean confirmationBlank = isBlank(confirmation);
    boolean matches = Arrays.equals(next, confirmation);
    Arrays.fill(current, '\0');
    Arrays.fill(next, '\0');
    Arrays.fill(confirmation, '\0');
    currentPasswordField.setText("");
    newPasswordField.setText("");
    confirmPasswordField.setText("");

    if (currentBlank || newBlank || confirmationBlank) {
      showFeedback("Vui lòng nhập đầy đủ các trường mật khẩu.", UserUiTheme.DANGER);
      return;
    }
    if (!matches) {
      showFeedback("Mật khẩu mới và xác nhận không khớp.", UserUiTheme.DANGER);
      return;
    }

    changeSubmitted = true;
    showFeedback(
        "Yêu cầu đổi mật khẩu mẫu; mật khẩu chưa được thay đổi trên máy chủ.",
        UserUiTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(UserUiTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG));

    JLabel title = new JLabel("Đổi mật khẩu", SwingConstants.CENTER);
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JPanel form = new JPanel();
    form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
    form.setOpaque(false);
    addField(form, "Mật khẩu hiện tại", currentPasswordField);
    addField(form, "Mật khẩu mới", newPasswordField);
    addField(form, "Xác nhận mật khẩu mới", confirmPasswordField);

    feedbackLabel.setFont(UserUiTheme.FONT_SMALL);
    feedbackLabel.setForeground(UserUiTheme.DANGER);
    JButton cancelButton = UserUiTheme.createSecondaryButton("Hủy");
    cancelButton.addActionListener(event -> closeDialog());
    JButton submitButton = UserUiTheme.createPrimaryButton("Xác nhận mẫu");
    submitButton.addActionListener(event -> submitChange());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JPanel content = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void addField(JPanel form, String labelText, JPasswordField field) {
    JLabel label = new JLabel(labelText);
    label.setFont(UserUiTheme.FONT_BODY_BOLD);
    label.setForeground(UserUiTheme.TEXT_SECONDARY);
    form.add(label);
    form.add(field);
    form.add(javax.swing.Box.createVerticalStrut(UserUiTheme.SPACE_SM));
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private boolean isBlank(char[] value) {
    for (char character : value) {
      if (!Character.isWhitespace(character)) {
        return false;
      }
    }
    return true;
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
