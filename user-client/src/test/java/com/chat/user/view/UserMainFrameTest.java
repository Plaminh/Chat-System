package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    assertInstanceOf(ChatWorkspacePanel.class, frame.getRegisteredView(UserMainFrame.Destination.CHATS));
    assertInstanceOf(FriendsPanel.class, frame.getRegisteredView(UserMainFrame.Destination.FRIENDS));
    assertInstanceOf(
        FriendRequestsSearchPanel.class,
        frame.getRegisteredView(UserMainFrame.Destination.REQUESTS));
    assertInstanceOf(GroupsPanel.class, frame.getRegisteredView(UserMainFrame.Destination.GROUPS));
    assertInstanceOf(
        ProfileSettingsPanel.class, frame.getRegisteredView(UserMainFrame.Destination.PROFILE));
  }

  @Test
  void navigationButtonsChangeActiveDestination() {
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample());

    frame.getNavigationButton(UserMainFrame.Destination.FRIENDS).doClick();

    assertEquals(UserMainFrame.Destination.FRIENDS, frame.getActiveDestination());
  }

  @Test
  void sidebarMatchesAdminChromeWithoutChangingNavigationBehavior() {
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample());

    assertEquals(245, frame.getNavigationSidebar().getPreferredSize().width);
    for (UserMainFrame.Destination destination : UserMainFrame.Destination.values()) {
      assertNotNull(frame.getNavigationButton(destination).getIcon());
    }
    assertEquals("Requests", frame.getNavigationButton(UserMainFrame.Destination.REQUESTS).getText());
    assertEquals("Sign Out", frame.getNavigationSidebar().getSignOutButton().getText());
    assertNotNull(frame.getNavigationSidebar().getSignOutButton().getIcon());
    assertEquals(1, frame.getNavigationSidebar().getSignOutButton().getActionListeners().length);

    frame.getNavigationSidebar().getSignOutButton().doClick();

    assertEquals(UserMainFrame.Destination.CHATS, frame.getActiveDestination());
  }

  @Test
  void signOutInvokesTheConfiguredHandler() {
    java.util.concurrent.atomic.AtomicInteger signOutCalls =
        new java.util.concurrent.atomic.AtomicInteger();
    UserMainFrame frame = new UserMainFrame(UserSessionState.createSample(), signOutCalls::incrementAndGet);

    frame.getNavigationSidebar().getSignOutButton().doClick();

    assertEquals(1, signOutCalls.get());
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
