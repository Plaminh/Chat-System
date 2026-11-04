package com.chat.user;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.UserSessionState;
import com.chat.user.view.LoginPanel;
import com.chat.user.view.PasswordRecoveryDialog;
import com.chat.user.view.RegistrationDialog;
import com.chat.user.view.UserMainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public final class UserApplication {
  private UserApplication() {}

  public static void main(String[] args) {
    FlatLightLaf.setup();
    UserUiTheme.applyGlobalDefaults();
    SwingUtilities.invokeLater(UserApplication::showWindow);
  }

  private static void showWindow() {
    UserSessionState sessionState = UserSessionState.createSample();
    JFrame frame = new JFrame("Chat System - User Client");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(1120, 720));
    frame.setSize(1280, 800);
    frame.setLocationRelativeTo(null);

    LoginPanel loginPanel =
        new LoginPanel(
            sessionState,
            () -> showWorkspace(frame, sessionState),
            () -> showRegistrationDialog(frame),
            () -> showRecoveryDialog(frame));
    frame.setContentPane(loginPanel);
    frame.setVisible(true);
  }

  private static void showWorkspace(JFrame frame, UserSessionState sessionState) {
    frame.setContentPane(new UserMainFrame(sessionState));
    frame.setTitle("Chat System - User Client");
    frame.revalidate();
    frame.repaint();
  }

  private static void showRegistrationDialog(JFrame frame) {
    new RegistrationDialog().createDialog(frame).setVisible(true);
  }

  private static void showRecoveryDialog(JFrame frame) {
    new PasswordRecoveryDialog().createDialog(frame).setVisible(true);
  }
}
