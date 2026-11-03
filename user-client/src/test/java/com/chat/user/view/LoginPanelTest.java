package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.UserSessionState;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
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
    assertTrue(panel.getFeedbackLabel().getText().contains("Enter your username"));
  }

  @Test
  void loginFieldsUseFixedAlignedDimensionsAndReadableText() {
    LoginPanel panel = newPanel(UserSessionState.createSample(), new int[] {0});

    assertTrue(panel.getUsernameField().getPreferredSize().width <= 360);
    assertEquals(36, panel.getUsernameField().getPreferredSize().height);
    assertEquals(new Dimension(360, 36), panel.getUsernameField().getMaximumSize());
    assertEquals(new Dimension(360, 36), panel.getUsernameField().getMinimumSize());
    assertTrue(panel.getPasswordField().getPreferredSize().width <= 360);
    assertEquals(36, panel.getPasswordField().getPreferredSize().height);
    assertEquals(new Dimension(360, 36), panel.getPasswordField().getMaximumSize());
    assertEquals(new Dimension(360, 36), panel.getPasswordField().getMinimumSize());
    assertEquals(14, panel.getUsernameField().getFont().getSize());
    assertEquals(14, panel.getPasswordField().getFont().getSize());

    JPanel centered = (JPanel) panel.getComponent(0);
    assertTrue(centered.getLayout() instanceof GridBagLayout);
    JPanel card = (JPanel) centered.getComponent(0);
    assertEquals(new Dimension(480, 560), card.getPreferredSize());
    assertEquals(new Dimension(480, 560), card.getMinimumSize());

    JPanel form =
        (JPanel) ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
    for (Component component : form.getComponents()) {
      if (component instanceof JPanel wrapper) {
        assertEquals(360, wrapper.getPreferredSize().width);
        assertEquals(360, wrapper.getMaximumSize().width);
        assertEquals(360, wrapper.getMinimumSize().width);
        assertTrue(wrapper.getLayout() instanceof BorderLayout);
      }
    }
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
    assertTrue(panel.getFeedbackLabel().getText().contains("not authenticated"));
  }

  private LoginPanel newPanel(UserSessionState session, int[] signInCalls) {
    return new LoginPanel(session, () -> signInCalls[0]++, () -> {}, () -> {});
  }
}
