package com.chat.admin.view;

import com.chat.admin.controller.AdminAuthController;
import com.chat.admin.view.AdminUIComponents.NavIconType;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Administrative Authentication Modal Dialog (Task 2 / Screen 1).
 *
 * <p>Validates administrator credentials before granting access to {@link AdminMainFrame}.
 */
public final class AdminLoginDialog extends JDialog {
  private final AdminAuthController authController;
  private final JTextField usernameField = new JTextField();
  private final JPasswordField passwordField = new JPasswordField();
  private final JLabel errorLabel = new JLabel(" ");
  private JButton signInBtn;
  private boolean authenticated = false;
  private String authenticatedUsername;
  private AdminUserInfo authenticatedAdmin = null;
  private char defaultEchoChar;

  public AdminLoginDialog(Frame owner) {
    this(owner, new AdminAuthController());
  }

  public AdminLoginDialog(Frame owner, AdminAuthController authController) {
    super(owner, "Chat Admin Console — Authentication", true);
    this.authController = authController != null ? authController : new AdminAuthController();
    ChatTheme.applyGlobalDefaults();
    initDialogLayout();
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public String getAuthenticatedUsername() {
    return authenticatedUsername;
  }

  public AdminUserInfo getAuthenticatedAdmin() {
    return authenticatedAdmin;
  }

  private void initDialogLayout() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setMinimumSize(new Dimension(500, 490));
    setSize(500, 490);
    setResizable(false);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(ChatTheme.SURFACE_WHITE);
    root.setBorder(BorderFactory.createLineBorder(ChatTheme.BORDER, 1));

    root.add(buildTopHeaderBanner(), BorderLayout.NORTH);
    root.add(buildFormCenter(), BorderLayout.CENTER);
    root.add(buildSecurityFooter(), BorderLayout.SOUTH);

    setContentPane(root);
    getRootPane().setDefaultButton(findSignInButton(root));
    pack();
    if (getWidth() < 500) {
      setSize(500, Math.max(getHeight(), 490));
    }
    setLocationRelativeTo(getOwner());
  }

  private JPanel buildTopHeaderBanner() {
    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setBackground(ChatTheme.SIDEBAR_BG);
    topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

    JLabel barTitle = new JLabel("Chat Admin Console — Gateway Authentication");
    barTitle.setFont(ChatTheme.FONT_SMALL);
    barTitle.setForeground(new Color(0xE2E8F0));
    barTitle.setIcon(
        AdminUIComponents.createVectorIcon(NavIconType.SHIELD, new Color(0x60A5FA), 14));
    barTitle.setIconTextGap(8);

    JLabel versionTag = new JLabel("v1.0-FlatLaf");
    versionTag.setFont(ChatTheme.FONT_MONO);
    versionTag.setForeground(new Color(0x93C5FD));

    topBar.add(barTitle, BorderLayout.WEST);
    topBar.add(versionTag, BorderLayout.EAST);
    return topBar;
  }

  private JPanel buildFormCenter() {
    JPanel center = new JPanel();
    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
    center.setBackground(ChatTheme.SURFACE_WHITE);
    center.setBorder(BorderFactory.createEmptyBorder(22, 32, 16, 32));

    // Shield Hero Icon Badge
    JPanel iconBadge = new JPanel(new BorderLayout());
    iconBadge.setPreferredSize(new Dimension(52, 52));
    iconBadge.setMaximumSize(new Dimension(52, 52));
    iconBadge.setBackground(ChatTheme.PRIMARY_SOFT_BG);
    iconBadge.setBorder(BorderFactory.createLineBorder(new Color(0xBFDBFE), 1, true));
    JLabel shieldIcon =
        new JLabel(
            AdminUIComponents.createVectorIcon(NavIconType.SHIELD, ChatTheme.PRIMARY, 24),
            SwingConstants.CENTER);
    iconBadge.add(shieldIcon, BorderLayout.CENTER);
    iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel titleLabel = new JLabel("Chat Admin Console", SwingConstants.CENTER);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(ChatTheme.TEXT_PRIMARY);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel subtitleLabel =
        new JLabel(
            "Sign in with administrator credentials to manage the system", SwingConstants.CENTER);
    subtitleLabel.setFont(ChatTheme.FONT_SMALL);
    subtitleLabel.setForeground(ChatTheme.TEXT_MUTED);
    subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    center.add(iconBadge);
    center.add(Box.createVerticalStrut(10));
    center.add(titleLabel);
    center.add(Box.createVerticalStrut(4));
    center.add(subtitleLabel);
    center.add(Box.createVerticalStrut(18));

    // Form Fields Panel
    JPanel formGrid = new JPanel(new GridBagLayout());
    formGrid.setBackground(ChatTheme.SURFACE_WHITE);
    formGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    // Username Label
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 5, 0);
    JLabel userLabel = new JLabel("Administrator Username");
    userLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    userLabel.setForeground(ChatTheme.TEXT_PRIMARY);
    formGrid.add(userLabel, gbc);

    // Username Input
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 0, 14, 0);
    usernameField.setFont(ChatTheme.FONT_BODY);
    usernameField.setPreferredSize(new Dimension(360, 36));
    usernameField.putClientProperty("JTextField.placeholderText", "Enter admin username");
    formGrid.add(usernameField, gbc);

    // Password Label
    gbc.gridy = 2;
    gbc.insets = new Insets(0, 0, 5, 0);
    JLabel passLabel = new JLabel("Password");
    passLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    passLabel.setForeground(ChatTheme.TEXT_PRIMARY);
    formGrid.add(passLabel, gbc);

    // Password Input + Show/Hide Toggle
    gbc.gridy = 3;
    gbc.insets = new Insets(0, 0, 10, 0);
    JPanel passRow = new JPanel(new BorderLayout(6, 0));
    passRow.setBackground(ChatTheme.SURFACE_WHITE);
    passwordField.setFont(ChatTheme.FONT_BODY);
    passwordField.setPreferredSize(new Dimension(290, 36));
    passwordField.putClientProperty("JTextField.placeholderText", "Enter password");
    defaultEchoChar = passwordField.getEchoChar();

    JButton toggleEchoBtn = ChatTheme.createSecondaryButton("Show");
    toggleEchoBtn.setPreferredSize(new Dimension(68, 36));
    toggleEchoBtn.setFont(ChatTheme.FONT_SMALL);
    toggleEchoBtn.addActionListener(
        e -> {
          if (passwordField.getEchoChar() != (char) 0) {
            passwordField.setEchoChar((char) 0);
            toggleEchoBtn.setText("Hide");
          } else {
            passwordField.setEchoChar(defaultEchoChar);
            toggleEchoBtn.setText("Show");
          }
        });
    passRow.add(passwordField, BorderLayout.CENTER);
    passRow.add(toggleEchoBtn, BorderLayout.EAST);
    formGrid.add(passRow, gbc);

    // Error / Validation Label
    gbc.gridy = 4;
    gbc.insets = new Insets(0, 0, 10, 0);
    errorLabel.setFont(ChatTheme.FONT_SMALL);
    errorLabel.setForeground(ChatTheme.DANGER_TEXT);
    formGrid.add(errorLabel, gbc);

    // Primary Sign In Button
    gbc.gridy = 5;
    gbc.insets = new Insets(2, 0, 0, 0);
    signInBtn = ChatTheme.createPrimaryButton("Sign In to Admin Console  →");
    signInBtn.setName("signInButton");
    signInBtn.setPreferredSize(new Dimension(360, 40));
    signInBtn.addActionListener(e -> attemptLogin());
    formGrid.add(signInBtn, gbc);

    center.add(formGrid);
    return center;
  }

  private JPanel buildSecurityFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(ChatTheme.PRIMARY_SOFT_BG);
    footer.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDBEAFE)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

    JLabel lockNote = new JLabel("Restricted Access  •  Chat Admin Console", SwingConstants.CENTER);
    lockNote.setFont(ChatTheme.FONT_SMALL);
    lockNote.setForeground(ChatTheme.TEXT_SECONDARY);
    footer.add(lockNote, BorderLayout.CENTER);
    return footer;
  }

  private JButton findSignInButton(JPanel root) {
    if (signInBtn != null) {
      return signInBtn;
    }
    for (Component c : root.getComponents()) {
      if (c instanceof JPanel panel) {
        for (Component sub : panel.getComponents()) {
          if (sub instanceof JPanel grid) {
            for (Component item : grid.getComponents()) {
              if (item instanceof JButton btn && "signInButton".equals(btn.getName())) {
                return btn;
              }
            }
          }
        }
      }
    }
    return null;
  }

  private void attemptLogin() {
    String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
    // Do not trim: leading/trailing spaces may be part of the password.
    String password = new String(passwordField.getPassword());

    if (username.isEmpty()) {
      errorLabel.setForeground(ChatTheme.DANGER_TEXT);
      errorLabel.setText("Vui lòng nhập tên đăng nhập quản trị viên.");
      usernameField.requestFocusInWindow();
      return;
    }
    if (password.isEmpty()) {
      errorLabel.setForeground(ChatTheme.DANGER_TEXT);
      errorLabel.setText("Vui lòng nhập mật khẩu.");
      passwordField.requestFocusInWindow();
      return;
    }

    setFormEnabled(false);
    errorLabel.setForeground(ChatTheme.PRIMARY);
    errorLabel.setText("Đang kết nối đến máy chủ...");

    authController
        .authenticateAsync(username, password)
        .whenComplete(
            (result, ex) ->
                SwingUtilities.invokeLater(
                    () -> {
                      setFormEnabled(true);
                      if (ex != null) {
                        errorLabel.setForeground(ChatTheme.DANGER_TEXT);
                        errorLabel.setText("Không thể kết nối đến máy chủ: " + ex.getMessage());
                        return;
                      }

                      if (!result.success()) {
                        errorLabel.setForeground(ChatTheme.DANGER_TEXT);
                        errorLabel.setText(result.errorMessage());
                        if (AdminAuthController.NOT_ADMIN_MESSAGE.equals(result.errorMessage())) {
                          JOptionPane.showMessageDialog(
                              this,
                              result.errorMessage(),
                              "Truy cập bị từ chối",
                              JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                      }

                      this.authenticated = true;
                      this.authenticatedAdmin = result.adminUser();
                      this.authenticatedUsername =
                          result.adminUser() != null
                              ? result.adminUser().account().summary().username()
                              : username;
                      dispose();
                    }));
  }

  private void setFormEnabled(boolean enabled) {
    usernameField.setEnabled(enabled);
    passwordField.setEnabled(enabled);
    if (signInBtn != null) {
      signInBtn.setEnabled(enabled);
      signInBtn.setText(enabled ? "Sign In to Admin Console  →" : "Signing In...");
    }
  }
}
