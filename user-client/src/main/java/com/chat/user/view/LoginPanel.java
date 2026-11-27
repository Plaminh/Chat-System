package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.user.model.UserSessionState;
import com.chat.user.network.SocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class LoginPanel extends JPanel {
  private static final int FORM_WIDTH = 360;

  private final UserSessionState sessionState;
  private final Runnable signInHandler;
  private final Runnable registrationHandler;
  private final Runnable recoveryHandler;
  private final Function<LoginRequest, CompletableFuture<ResponseEnvelope>> loginSender;
  private final JTextField usernameField = ChatTheme.createTextField(32);
  private final JPasswordField passwordField = new JPasswordField(32);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private final JButton signInButton = ChatTheme.createPrimaryButton("Sign In");

  public LoginPanel(
      UserSessionState sessionState,
      Runnable signInHandler,
      Runnable registrationHandler,
      Runnable recoveryHandler) {
    this(
        sessionState,
        signInHandler,
        registrationHandler,
        recoveryHandler,
        request ->
            CompletableFuture.failedFuture(
                new IllegalStateException("Socket client is not configured")));
  }

  public LoginPanel(
      UserSessionState sessionState,
      Runnable signInHandler,
      Runnable registrationHandler,
      Runnable recoveryHandler,
      SocketClient socketClient) {
    this(
        sessionState,
        signInHandler,
        registrationHandler,
        recoveryHandler,
        request ->
            socketClient == null
                ? CompletableFuture.failedFuture(
                    new IllegalStateException("Socket client is not configured"))
                : CompletableFuture.runAsync(() -> connectIfNeeded(socketClient))
                    .thenCompose(ignored -> socketClient.sendAsync(ActionType.LOGIN, request)));
  }

  /** Reconnects when the server was down at startup or the connection was lost. */
  private static void connectIfNeeded(SocketClient socketClient) {
    if (socketClient.isConnected()) {
      return;
    }
    try {
      socketClient.connect();
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  LoginPanel(
      UserSessionState sessionState,
      Runnable signInHandler,
      Runnable registrationHandler,
      Runnable recoveryHandler,
      Function<LoginRequest, CompletableFuture<ResponseEnvelope>> loginSender) {
    super(new BorderLayout());
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    this.signInHandler = Objects.requireNonNull(signInHandler, "signInHandler");
    this.registrationHandler = Objects.requireNonNull(registrationHandler, "registrationHandler");
    this.recoveryHandler = Objects.requireNonNull(recoveryHandler, "recoveryHandler");
    this.loginSender = Objects.requireNonNull(loginSender, "loginSender");
    buildContent();
  }

  JTextField getUsernameField() {
    return usernameField;
  }

  JPasswordField getPasswordField() {
    return passwordField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  void submitSignIn() {
    String username = usernameField.getText().trim();
    char[] password = passwordField.getPassword();
    boolean passwordBlank = isBlank(password);
    char[] passwordCopy = Arrays.copyOf(password, password.length);
    Arrays.fill(password, '\0');
    passwordField.setText("");

    if (username.isEmpty() || passwordBlank) {
      showFeedback("Enter your username/email and password.", ChatTheme.DANGER);
      return;
    }

    // One request at a time: a second click would record a second login on the server.
    signInButton.setEnabled(false);
    loginSender
        .apply(new LoginRequest(username, new String(passwordCopy)))
        .whenComplete(
            (response, error) ->
                SwingUtilities.invokeLater(
                    () -> handleLoginResponse(response, error)));
    Arrays.fill(passwordCopy, '\0');
  }

  private void handleLoginResponse(ResponseEnvelope response, Throwable error) {
    signInButton.setEnabled(true);
    if (error != null) {
      showFeedback("Could not connect to the server.", ChatTheme.DANGER);
      return;
    }
    if (response != null && response.success()) {
      JsonNode user = response.data() == null ? null : response.data().get("user");
      if (user != null) {
        sessionState.signInAs(
            user.path("username").asText(),
            user.path("fullName").asText(),
            usernameField.getText().trim().contains("@")
                ? usernameField.getText().trim()
                : sessionState.getProfile().email());
      } else {
        sessionState.signIn();
      }
      showFeedback("Login successful.", ChatTheme.SUCCESS);
      signInHandler.run();
      return;
    }
    showFeedback(
        response != null && response.message() != null
            ? response.message()
            : "Login failed.",
        ChatTheme.DANGER);
  }

  private void buildContent() {
    setBackground(ChatTheme.CANVAS_BG);

    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(ChatTheme.SURFACE_WHITE);
    card.setBorder(BorderFactory.createLineBorder(ChatTheme.BORDER));
    card.setPreferredSize(new Dimension(480, 560));
    card.setMinimumSize(new Dimension(480, 560));

    JLabel windowTitle = new JLabel("ChatUser  —  Sign In");
    windowTitle.setFont(ChatTheme.FONT_BODY_BOLD);
    windowTitle.setForeground(Color.WHITE);
    windowTitle.setBorder(
        BorderFactory.createEmptyBorder(ChatTheme.SPACE_MD, ChatTheme.SPACE_LG,
            ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));
    JPanel titleBar = new JPanel(new BorderLayout());
    titleBar.setBackground(ChatTheme.SIDEBAR_BG);
    titleBar.add(windowTitle, BorderLayout.CENTER);

    JPanel form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.setBackground(ChatTheme.SURFACE_WHITE);
    form.setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, 34, ChatTheme.SPACE_LG, 34));

    JLabel brand = new JLabel("ChatUser", SwingConstants.CENTER);
    brand.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
    brand.setForeground(ChatTheme.TEXT_PRIMARY);
    brand.setAlignmentX(CENTER_ALIGNMENT);
    JLabel subtitle = new JLabel("Sign in to your account to start messaging");
    subtitle.setFont(ChatTheme.FONT_BODY);
    subtitle.setForeground(ChatTheme.TEXT_MUTED);
    subtitle.setAlignmentX(CENTER_ALIGNMENT);

    form.add(Box.createVerticalGlue());
    form.add(brand);
    form.add(Box.createVerticalStrut(ChatTheme.SPACE_SM));
    form.add(subtitle);
    form.add(Box.createVerticalStrut(20));
    usernameField.setFont(ChatTheme.FONT_BODY.deriveFont(14f));
    form.add(
        createFormWrapper(
            createFieldLabel("Username or Email"), 20, BorderLayout.WEST));
    form.add(Box.createVerticalStrut(ChatTheme.SPACE_XS));
    form.add(createFormWrapper(usernameField, 36, BorderLayout.CENTER));
    form.add(Box.createVerticalStrut(12));
    form.add(createFormWrapper(createFieldLabel("Password"), 20, BorderLayout.WEST));
    form.add(Box.createVerticalStrut(ChatTheme.SPACE_XS));
    passwordField.setFont(ChatTheme.FONT_BODY.deriveFont(14f));
    form.add(createFormWrapper(passwordField, 36, BorderLayout.CENTER));

    JPanel links = new JPanel(new BorderLayout());
    links.setOpaque(false);
    JButton recoveryButton = createTextButton("Forgot password?");
    recoveryButton.setMargin(new Insets(0, 0, 0, 0));
    recoveryButton.setBorder(BorderFactory.createEmptyBorder());
    recoveryButton.setCursor(
        java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    recoveryButton.addActionListener(event -> recoveryHandler.run());
    links.add(recoveryButton, BorderLayout.EAST);
    form.add(createFormWrapper(links, 28, BorderLayout.CENTER));

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);
    form.add(Box.createVerticalStrut(ChatTheme.SPACE_XS));
    form.add(createFormWrapper(feedbackLabel, 20, BorderLayout.CENTER));

    signInButton.addActionListener(event -> submitSignIn());
    form.add(Box.createVerticalStrut(10));
    form.add(createFormWrapper(signInButton, 42, BorderLayout.CENTER));

    JPanel registrationRow = new JPanel(new FlowLayout(FlowLayout.CENTER, ChatTheme.SPACE_XS, 0));
    registrationRow.setOpaque(false);
    registrationRow.add(new JLabel("Don't have an account?"));
    JButton registrationButton = createTextButton("Register now");
    registrationButton.addActionListener(event -> registrationHandler.run());
    registrationRow.add(registrationButton);
    form.add(Box.createVerticalStrut(ChatTheme.SPACE_MD));
    form.add(createFormWrapper(registrationRow, 32, BorderLayout.CENTER));
    form.add(Box.createVerticalGlue());

    JPanel footer = new JPanel(new GridLayout(1, 1));
    footer.setBackground(ChatTheme.PRIMARY_SOFT_BG);
    JLabel footerText =
        new JLabel("ChatUser  •  Secure account login", SwingConstants.CENTER);
    footerText.setFont(ChatTheme.FONT_SMALL);
    footerText.setForeground(ChatTheme.TEXT_SECONDARY);
    footerText.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    footer.add(footerText);

    card.add(titleBar, BorderLayout.NORTH);
    card.add(form, BorderLayout.CENTER);
    card.add(footer, BorderLayout.SOUTH);
    JPanel centered = new JPanel(new GridBagLayout());
    centered.setOpaque(false);
    GridBagConstraints cardConstraints = new GridBagConstraints();
    cardConstraints.gridx = 0;
    cardConstraints.gridy = 0;
    cardConstraints.anchor = GridBagConstraints.CENTER;
    cardConstraints.insets = new Insets(24, 24, 24, 24);
    centered.add(card, cardConstraints);
    add(centered, BorderLayout.CENTER);
  }

  private JPanel createFormWrapper(JComponent component, int height, String constraint) {
    setFixedSize(component, height);
    component.setAlignmentX(CENTER_ALIGNMENT);

    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    setFixedSize(wrapper, height);
    wrapper.setAlignmentX(CENTER_ALIGNMENT);
    wrapper.add(component, constraint);
    return wrapper;
  }

  private void setFixedSize(JComponent component, int height) {
    component.setPreferredSize(new Dimension(FORM_WIDTH, height));
    component.setMaximumSize(new Dimension(FORM_WIDTH, height));
    component.setMinimumSize(new Dimension(FORM_WIDTH, height));
  }

  private JLabel createFieldLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(ChatTheme.FONT_BODY_BOLD);
    label.setForeground(ChatTheme.TEXT_SECONDARY);
    label.setAlignmentX(CENTER_ALIGNMENT);
    return label;
  }

  private JButton createTextButton(String text) {
    JButton button = new JButton(text);
    button.setFont(ChatTheme.FONT_BODY_BOLD);
    button.setForeground(ChatTheme.PRIMARY);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    return button;
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }

  private boolean isBlank(char[] value) {
    for (char character : value) {
      if (!Character.isWhitespace(character)) {
        return false;
      }
    }
    return true;
  }
}
