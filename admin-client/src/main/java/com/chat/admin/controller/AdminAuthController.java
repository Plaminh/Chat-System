package com.chat.admin.controller;

/**
 * Controller xử lý xác thực phiên đăng nhập cho Quản trị viên.
 * Tách biệt logic xác thực tài khoản khỏi AdminLoginDialog (View).
 */
public final class AdminAuthController {
  private static final String DEFAULT_ADMIN_USER = "admin";
  private static final String DEFAULT_ADMIN_PASS = "admin123";
  private static final String ALT_ADMIN_EMAIL = "sysadmin@campus.edu";

  public record AuthResult(boolean success, String username, String errorMessage) {}

  public AuthResult authenticate(String username, String password) {
    if (username == null || username.trim().isEmpty()) {
      return new AuthResult(false, null, "Please enter your administrator username.");
    }
    if (password == null || password.trim().isEmpty()) {
      return new AuthResult(false, null, "Please enter your password.");
    }
    String cleanUser = username.trim();
    String cleanPass = password.trim();

    if (cleanPass.length() < 4) {
      return new AuthResult(false, null, "Invalid credentials. Password must be at least 4 characters.");
    }

    boolean valid =
        (DEFAULT_ADMIN_USER.equalsIgnoreCase(cleanUser) || ALT_ADMIN_EMAIL.equalsIgnoreCase(cleanUser))
            && DEFAULT_ADMIN_PASS.equals(cleanPass);

    if (valid) {
      return new AuthResult(true, cleanUser, null);
    } else {
      return new AuthResult(
          false,
          null,
          "Authentication failed: Invalid administrator username or password.\nHint: Default mock credentials are 'admin' / 'admin123'.");
    }
  }
}
