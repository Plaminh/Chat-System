package com.chat.user.view;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.ui.ChatTheme;
import com.chat.user.network.SocketClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public final class PasswordRecoveryDialog extends JPanel {
  private final JTextField emailField = ChatTheme.createTextField(30);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private final JButton submitButton = ChatTheme.createPrimaryButton("Request Temporary Password");
  private final Function<PasswordRecoveryRequest, CompletableFuture<ResponseEnvelope>>
      requestSender;
  private JDialog dialog;
  private boolean requestInFlight;
  private boolean requestSubmitted;

  public PasswordRecoveryDialog(SocketClient socketClient) {
    this(
        request ->
            Objects.requireNonNull(socketClient, "socketClient")
                .sendAsync(ActionType.REQUEST_PASSWORD_RESET, request));
  }

  PasswordRecoveryDialog(
      Function<PasswordRecoveryRequest, CompletableFuture<ResponseEnvelope>> requestSender) {
    super(new BorderLayout());
    this.requestSender = Objects.requireNonNull(requestSender, "requestSender");
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Password Recovery", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(520, 300));
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextField getEmailField() {
    return emailField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isRequestSubmitted() {
    return requestSubmitted;
  }

  void submitRecoveryRequest() {
    if (requestInFlight) {
      return;
    }

    String email = emailField.getText().trim();
    if (email.isEmpty()) {
      showFeedback("Enter your email address.", ChatTheme.DANGER);
      return;
    }
    if (!isValidEmail(email)) {
      showFeedback("Enter a valid email address.", ChatTheme.DANGER);
      return;
    }

    requestSubmitted = false;
    requestInFlight = true;
    emailField.setEnabled(false);
    submitButton.setEnabled(false);
    showFeedback("Submitting password recovery request...", ChatTheme.TEXT_MUTED);

    CompletableFuture<ResponseEnvelope> pending;
    try {
      pending = requestSender.apply(new PasswordRecoveryRequest(email));
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

    JLabel title = new JLabel("Password Recovery", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel description =
        new JLabel(
            "<html><div style='text-align:center'>Enter your registered email address to receive a<br>"
                + "randomly generated temporary password.</div></html>",
            SwingConstants.CENTER);
    description.setFont(ChatTheme.FONT_BODY);
    description.setForeground(ChatTheme.TEXT_MUTED);

    JLabel emailLabel = new JLabel("Email address");
    emailLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    emailLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    JPanel form = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    form.setOpaque(false);
    form.add(emailLabel, BorderLayout.NORTH);
    form.add(emailField, BorderLayout.CENTER);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);

    JButton cancelButton = ChatTheme.createSecondaryButton("Back to Sign In");
    cancelButton.addActionListener(event -> closeDialog());
    submitButton.addActionListener(event -> submitRecoveryRequest());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, ChatTheme.SPACE_SM, 0));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(description, BorderLayout.SOUTH);
    content.add(heading, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);

    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  }

  private void handleResponse(ResponseEnvelope response, Throwable error) {
    requestInFlight = false;
    emailField.setEnabled(true);
    submitButton.setEnabled(true);
    if (error != null || response == null || !response.success()) {
      showFeedback(
          "Could not process the password recovery request. Please try again later.",
          ChatTheme.DANGER);
      return;
    }

    requestSubmitted = true;
    showFeedback(
        "If the address is registered, a temporary password will be sent.", ChatTheme.SUCCESS);
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
    feedbackLabel.revalidate();
    if (dialog != null) {
      dialog.pack();
      dialog.setLocationRelativeTo(dialog.getOwner());
    }
  }
}
