package com.chat.user;

import com.chat.common.ui.ChatTheme;
import com.chat.user.model.UserSessionState;
import com.chat.user.network.SocketClient;
import com.chat.user.view.LoginPanel;
import com.chat.user.view.PasswordRecoveryDialog;
import com.chat.user.view.RegistrationDialog;
import com.chat.user.view.UserMainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public final class UserApplication {
  private UserApplication() {}

  public static void main(String[] args) {
    FlatLightLaf.setup();
    ChatTheme.applyGlobalDefaults();
    SwingUtilities.invokeLater(UserApplication::showWindow);
  }

  private static void showWindow() {
    UserSessionState sessionState = UserSessionState.createSample();
    SocketClient socketClient = new SocketClient();
    try {
      socketClient.connect();
    } catch (Exception exception) {
      // The registration dialog reports connection failures.
    }
    JFrame frame = new JFrame("Chat System - User Client");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(1120, 720));
    frame.setSize(1280, 800);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent event) {
            if (socketClient.isConnected()) {
              socketClient
                  .sendAsync(com.chat.common.enums.ActionType.LOGOUT, null)
                  // Exit even if the server never answers; closing the socket still makes the
                  // server record the logout time through its disconnect path.
                  .orTimeout(2, TimeUnit.SECONDS)
                  .whenComplete(
                      (response, error) -> {
                        socketClient.close();
                        SwingUtilities.invokeLater(() -> {
                          frame.dispose();
                          System.exit(0);
                        });
                      });
            } else {
              frame.dispose();
              System.exit(0);
            }
          }
        });

    LoginPanel loginPanel =
        new LoginPanel(
            sessionState,
            () -> showWorkspace(frame, sessionState, socketClient),
            () -> showRegistrationDialog(frame, socketClient),
            () -> showRecoveryDialog(frame),
            socketClient);
    frame.setContentPane(loginPanel);
    frame.setVisible(true);
  }

  private static void showWorkspace(
      JFrame frame, UserSessionState sessionState, SocketClient socketClient) {
    frame.setContentPane(
        new UserMainFrame(sessionState, () -> showLogin(frame, sessionState, socketClient)));
    frame.setTitle("Chat System - User Client");
    frame.revalidate();
    frame.repaint();
  }

  private static void showLogin(
      JFrame frame, UserSessionState sessionState, SocketClient socketClient) {
    if (socketClient.isConnected()) {
      socketClient.sendAsync(com.chat.common.enums.ActionType.LOGOUT, null);
    }
    sessionState.signOut();
    LoginPanel loginPanel =
        new LoginPanel(
            sessionState,
            () -> showWorkspace(frame, sessionState, socketClient),
            () -> showRegistrationDialog(frame, socketClient),
            () -> showRecoveryDialog(frame),
            socketClient);
    frame.setContentPane(loginPanel);
    frame.setTitle("Chat System - User Client");
    frame.revalidate();
    frame.repaint();
  }

  private static void showRegistrationDialog(JFrame frame, SocketClient socketClient) {
    new RegistrationDialog(socketClient).createDialog(frame).setVisible(true);
  }

  private static void showRecoveryDialog(JFrame frame) {
    new PasswordRecoveryDialog().createDialog(frame).setVisible(true);
  }
}
