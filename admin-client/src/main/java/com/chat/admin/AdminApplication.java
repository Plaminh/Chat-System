package com.chat.admin;

import com.chat.common.ui.ChatTheme;
import com.chat.admin.view.AdminLoginDialog;
import com.chat.admin.view.AdminMainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

/** Entry point for the Admin Desktop Client application. */
public final class AdminApplication {
  private AdminApplication() {}

  public static void main(String[] args) {
    FlatLightLaf.setup();
    ChatTheme.applyGlobalDefaults();
    SwingUtilities.invokeLater(AdminApplication::launchAdminFlow);
  }

  private static void launchAdminFlow() {
    AdminLoginDialog loginDialog = new AdminLoginDialog(null);
    loginDialog.setVisible(true);

    if (loginDialog.isAuthenticated()) {
      AdminMainFrame mainFrame = new AdminMainFrame(loginDialog.getAuthenticatedUsername());
      mainFrame.setSignOutHandler(AdminApplication::launchAdminFlow);
      mainFrame.setVisible(true);
    } else {
      System.exit(0);
    }
  }
}
