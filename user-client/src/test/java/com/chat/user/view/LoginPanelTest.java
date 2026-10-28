package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.UserSessionState;
import org.junit.jupiter.api.Test;

class LoginPanelTest {
  @Test
  void missingFieldsKeepUserOutOfWorkspace() {
    UserSessionState session = UserSessionState.createSample();
    int[] signInCalls = {0};
    LoginPanel panel = newPanel(session, signInCalls);

    panel.submitSignIn();

    assertFalse(session.isSignedIn());
    assertEquals(0, signInCalls[0]);
    assertTrue(panel.getFeedbackLabel().getText().contains("Vui lòng nhập"));
  }

  @Test
  void nonBlankSampleCredentialsOpenWorkspaceWithoutRetainingPassword() {
    UserSessionState session = UserSessionState.createSample();
    int[] signInCalls = {0};
    LoginPanel panel = newPanel(session, signInCalls);
    panel.getUsernameField().setText("sample.user");
    panel.getPasswordField().setText("sample-password");

    panel.submitSignIn();

    assertTrue(session.isSignedIn());
    assertEquals(1, signInCalls[0]);
    assertEquals("", new String(panel.getPasswordField().getPassword()));
    assertTrue(panel.getFeedbackLabel().getText().contains("chưa được xác thực"));
  }

  private LoginPanel newPanel(UserSessionState session, int[] signInCalls) {
    return new LoginPanel(session, () -> signInCalls[0]++, () -> {}, () -> {});
  }
}
