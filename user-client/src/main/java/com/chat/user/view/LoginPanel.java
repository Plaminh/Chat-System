package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.UserSessionState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Objects;
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

public final class LoginPanel extends JPanel {
  private static final int FORM_WIDTH = 360;

  private final UserSessionState sessionState;
  private final Runnable signInHandler;
  private final Runnable registrationHandler;
  private final Runnable recoveryHandler;
  private final JTextField usernameField = UserUiTheme.createTextField(32);
  private final JPasswordField passwordField = new JPasswordField(32);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);

  public LoginPanel(
      UserSessionState sessionState,
      Runnable signInHandler,
      Runnable registrationHandler,
      Runnable recoveryHandler) {
    super(new BorderLayout());
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    this.signInHandler = Objects.requireNonNull(signInHandler, "signInHandler");
    this.registrationHandler = Objects.requireNonNull(registrationHandler, "registrationHandler");
    this.recoveryHandler = Objects.requireNonNull(recoveryHandler, "recoveryHandler");
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
    Arrays.fill(password, '\0');
    passwordField.setText("");

    if (username.isEmpty() || passwordBlank) {
      showFeedback("Vui lòng nhập tên đăng nhập/email và mật khẩu.", UserUiTheme.DANGER);
      return;
    }

    sessionState.signIn();
    showFeedback("Đăng nhập mẫu thành công; tài khoản chưa được xác thực.", UserUiTheme.SUCCESS);
    signInHandler.run();
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);

    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(UserUiTheme.SURFACE_WHITE);
    card.setBorder(BorderFactory.createLineBorder(UserUiTheme.BORDER));
    card.setPreferredSize(new Dimension(480, 560));
    card.setMinimumSize(new Dimension(480, 560));

    JLabel windowTitle = new JLabel("Trò Chuyện Việt  —  Cổng Đăng Nhập");
    windowTitle.setFont(UserUiTheme.FONT_BODY_BOLD);
    windowTitle.setForeground(Color.WHITE);
    windowTitle.setBorder(
        BorderFactory.createEmptyBorder(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG,
            UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG));
    JPanel titleBar = new JPanel(new BorderLayout());
    titleBar.setBackground(UserUiTheme.SIDEBAR_BG);
    titleBar.add(windowTitle, BorderLayout.CENTER);

    JPanel form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.setBackground(UserUiTheme.SURFACE_WHITE);
    form.setBorder(
        BorderFactory.createEmptyBorder(
            UserUiTheme.SPACE_LG, 34, UserUiTheme.SPACE_LG, 34));

    JLabel brand = new JLabel("Trò Chuyện Việt", SwingConstants.CENTER);
    brand.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
    brand.setForeground(UserUiTheme.TEXT_PRIMARY);
    brand.setAlignmentX(CENTER_ALIGNMENT);
    JLabel subtitle = new JLabel("Đăng nhập vào tài khoản cá nhân để tiếp tục trao đổi");
    subtitle.setFont(UserUiTheme.FONT_BODY);
    subtitle.setForeground(UserUiTheme.TEXT_MUTED);
    subtitle.setAlignmentX(CENTER_ALIGNMENT);

    form.add(Box.createVerticalGlue());
    form.add(brand);
    form.add(Box.createVerticalStrut(UserUiTheme.SPACE_SM));
    form.add(subtitle);
    form.add(Box.createVerticalStrut(20));
    usernameField.setFont(UserUiTheme.FONT_BODY.deriveFont(14f));
    form.add(
        createFormWrapper(
            createFieldLabel("Tên đăng nhập hoặc Email"), 20, BorderLayout.WEST));
    form.add(Box.createVerticalStrut(UserUiTheme.SPACE_XS));
    form.add(createFormWrapper(usernameField, 36, BorderLayout.CENTER));
    form.add(Box.createVerticalStrut(12));
    form.add(createFormWrapper(createFieldLabel("Mật khẩu"), 20, BorderLayout.WEST));
    form.add(Box.createVerticalStrut(UserUiTheme.SPACE_XS));
    passwordField.setFont(UserUiTheme.FONT_BODY.deriveFont(14f));
    form.add(createFormWrapper(passwordField, 36, BorderLayout.CENTER));

    JPanel links = new JPanel(new BorderLayout());
    links.setOpaque(false);
    JButton recoveryButton = createTextButton("Quên mật khẩu?");
    recoveryButton.setMargin(new Insets(0, 0, 0, 0));
    recoveryButton.setBorder(BorderFactory.createEmptyBorder());
    recoveryButton.setCursor(
        java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    recoveryButton.addActionListener(event -> recoveryHandler.run());
    links.add(recoveryButton, BorderLayout.EAST);
    form.add(createFormWrapper(links, 28, BorderLayout.CENTER));

    feedbackLabel.setFont(UserUiTheme.FONT_SMALL);
    feedbackLabel.setForeground(UserUiTheme.DANGER);
    form.add(Box.createVerticalStrut(UserUiTheme.SPACE_XS));
    form.add(createFormWrapper(feedbackLabel, 20, BorderLayout.CENTER));

    JButton signInButton = UserUiTheme.createPrimaryButton("Đăng nhập");
    signInButton.addActionListener(event -> submitSignIn());
    form.add(Box.createVerticalStrut(10));
    form.add(createFormWrapper(signInButton, 42, BorderLayout.CENTER));

    JPanel registrationRow = new JPanel(new FlowLayout(FlowLayout.CENTER, UserUiTheme.SPACE_XS, 0));
    registrationRow.setOpaque(false);
    registrationRow.add(new JLabel("Chưa có tài khoản?"));
    JButton registrationButton = createTextButton("Đăng ký ngay");
    registrationButton.addActionListener(event -> registrationHandler.run());
    registrationRow.add(registrationButton);
    form.add(Box.createVerticalStrut(UserUiTheme.SPACE_MD));
    form.add(createFormWrapper(registrationRow, 32, BorderLayout.CENTER));
    form.add(Box.createVerticalGlue());

    JPanel footer = new JPanel(new GridLayout(1, 1));
    footer.setBackground(UserUiTheme.PRIMARY_SOFT_BG);
    JLabel footerText =
        new JLabel(
            "Phiên bản giao diện mẫu v1.0  •  Không kết nối dịch vụ tài khoản",
            SwingConstants.CENTER);
    footerText.setFont(UserUiTheme.FONT_SMALL);
    footerText.setForeground(UserUiTheme.TEXT_SECONDARY);
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
    label.setFont(UserUiTheme.FONT_BODY_BOLD);
    label.setForeground(UserUiTheme.TEXT_SECONDARY);
    label.setAlignmentX(CENTER_ALIGNMENT);
    return label;
  }

  private JButton createTextButton(String text) {
    JButton button = new JButton(text);
    button.setFont(UserUiTheme.FONT_BODY_BOLD);
    button.setForeground(UserUiTheme.PRIMARY);
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
