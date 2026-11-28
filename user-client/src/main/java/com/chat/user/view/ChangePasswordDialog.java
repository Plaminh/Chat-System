package com.chat.user.view;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.ui.ChatTheme;
import com.chat.user.network.SocketClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public final class ChangePasswordDialog extends JPanel {
  private final JPasswordField currentPasswordField = new JPasswordField(24);
  private final JPasswordField newPasswordField = new JPasswordField(24);
  private final JPasswordField confirmPasswordField = new JPasswordField(24);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private final JButton submitButton = ChatTheme.createPrimaryButton("Change Password");
  private final Function<PasswordChangeRequest, CompletableFuture<ResponseEnvelope>> requestSender;
  private JDialog dialog;
  private boolean requestInProgress;
  private boolean changeSubmitted;

  public ChangePasswordDialog(SocketClient socketClient) {
    this(
        request ->
            Objects.requireNonNull(socketClient, "socketClient")
                .sendAsync(ActionType.CHANGE_PASSWORD, request));
  }

  ChangePasswordDialog(
      Function<PasswordChangeRequest, CompletableFuture<ResponseEnvelope>> requestSender) {
    super(new BorderLayout());
    this.requestSender = Objects.requireNonNull(requestSender, "requestSender");
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Change Password", JDialog.ModalityType.APPLICATION_MODAL);
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
    if (requestInProgress) {
      return;
    }

    char[] current = currentPasswordField.getPassword();
    char[] next = newPasswordField.getPassword();
    char[] confirmation = confirmPasswordField.getPassword();
    boolean currentBlank = isBlank(current);
    boolean newBlank = isBlank(next);
    boolean confirmationBlank = isBlank(confirmation);
    boolean matches = Arrays.equals(next, confirmation);
    changeSubmitted = false;
    if (currentBlank || newBlank || confirmationBlank) {
      clearPasswordFields(current, next, confirmation);
      showFeedback("Complete all password fields.", ChatTheme.DANGER);
      return;
    }
    if (!matches) {
      clearPasswordFields(current, next, confirmation);
      showFeedback("The new password and confirmation do not match.", ChatTheme.DANGER);
      return;
    }
    if (next.length < 8) {
      clearPasswordFields(current, next, confirmation);
      showFeedback("New password must contain at least 8 characters.", ChatTheme.DANGER);
      return;
    }

    PasswordChangeRequest request =
        new PasswordChangeRequest(new String(current), new String(next));
    clearPasswordFields(current, next, confirmation);
    requestInProgress = true;
    setPasswordFieldsEnabled(false);
    submitButton.setEnabled(false);
    showFeedback("Changing password...", ChatTheme.TEXT_MUTED);

    CompletableFuture<ResponseEnvelope> pending;
    try {
      pending = requestSender.apply(request);
    } catch (RuntimeException exception) {
      SwingUtilities.invokeLater(() -> handleResponse(null, exception));
      return;
    }
    if (pending == null) {
      SwingUtilities.invokeLater(() -> handleResponse(null, new IllegalStateException()));
      return;
    }
    pending.whenComplete(
        (response, error) -> SwingUtilities.invokeLater(() -> handleResponse(response, error)));
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));

    JLabel title = new JLabel("Change Password", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JPanel form = new JPanel();
    form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
    form.setOpaque(false);
    addField(form, "Current password", currentPasswordField);
    addField(form, "New password", newPasswordField);
    addField(form, "Confirm new password", confirmPasswordField);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);
    JButton cancelButton = ChatTheme.createSecondaryButton("Cancel");
    cancelButton.addActionListener(event -> closeDialog());
    submitButton.addActionListener(event -> submitChange());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void addField(JPanel form, String labelText, JPasswordField field) {
    JLabel label = new JLabel(labelText);
    label.setFont(ChatTheme.FONT_BODY_BOLD);
    label.setForeground(ChatTheme.TEXT_SECONDARY);
    form.add(label);
    form.add(field);
    form.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
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

  private void clearPasswordFields(char[]... values) {
    for (char[] value : values) {
      Arrays.fill(value, '\0');
    }
    currentPasswordField.setText("");
    newPasswordField.setText("");
    confirmPasswordField.setText("");
  }

  private void setPasswordFieldsEnabled(boolean enabled) {
    currentPasswordField.setEnabled(enabled);
    newPasswordField.setEnabled(enabled);
    confirmPasswordField.setEnabled(enabled);
  }

  private void handleResponse(ResponseEnvelope response, Throwable error) {
    requestInProgress = false;
    setPasswordFieldsEnabled(true);
    submitButton.setEnabled(true);
    if (error != null || response == null) {
      showFeedback("Could not change password. Please try again later.", ChatTheme.DANGER);
      return;
    }
    if (!response.success()) {
      showFeedback(
          response.message() == null || response.message().isBlank()
              ? "Could not change password."
              : response.message(),
          ChatTheme.DANGER);
      return;
    }

    changeSubmitted = true;
    showFeedback("Password changed successfully.", ChatTheme.SUCCESS);
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
