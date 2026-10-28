package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.chat.user.model.UserSessionState;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

class UserMainFrameTest {
  @Test
  void providesAllWorkspaceDestinationsAndStartsOnChats() {
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample());

    assertEquals(UserMainFrame.Destination.CHATS, frame.getActiveDestination());
    for (UserMainFrame.Destination destination : UserMainFrame.Destination.values()) {
      assertNotNull(frame.getRegisteredView(destination));
    }
  }

  @Test
  void navigationButtonsChangeActiveDestination() {
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample());

    frame.getNavigationSidebar().getButton(UserMainFrame.Destination.FRIENDS).doClick();

    assertEquals(UserMainFrame.Destination.FRIENDS, frame.getActiveDestination());
  }

  @Test
  void registeredViewCanReplaceDestinationPlaceholder() {
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample());
    JLabel replacement = new JLabel("Friends");

    frame.registerView(UserMainFrame.Destination.FRIENDS, replacement);

    assertEquals(replacement, frame.getRegisteredView(UserMainFrame.Destination.FRIENDS));
    assertNotSame(replacement, frame.getRegisteredView(UserMainFrame.Destination.CHATS));
  }
}
