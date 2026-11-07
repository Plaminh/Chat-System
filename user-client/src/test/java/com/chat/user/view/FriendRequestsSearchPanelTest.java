package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.FriendRequest;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class FriendRequestsSearchPanelTest {
  @Test
  void acceptingAndDecliningRequestsRemovesThemFromPendingList() {
    FriendRequestsSearchPanel panel = createPanel(new HashSet<>());

    clickButton(panel, "Accept");
    assertEquals(1, panel.getVisibleRequests().size());

    clickButton(panel, "Decline");
    assertTrue(panel.getVisibleRequests().isEmpty());
  }

  @Test
  void searchMatchesUsernameAndFullNameButExcludesBlockedUsers() {
    HashSet<String> blocked = new HashSet<>();
    blocked.add("blocked.user");
    FriendRequestsSearchPanel panel = createPanel(blocked);

    panel.getSearchField().setText("minh.tran");
    assertEquals(1, panel.getVisibleSearchResults().size());
    assertEquals("minh.tran", panel.getVisibleSearchResults().get(0).username());

    panel.getSearchField().setText("Võ Quang");
    assertEquals(1, panel.getVisibleSearchResults().size());

    panel.getSearchField().setText("blocked");
    assertTrue(panel.getVisibleSearchResults().isEmpty());
  }

  @Test
  void acceptingRequestUpdatesItsSampleStatus() {
    FriendRequestsSearchPanel panel = createPanel(new HashSet<>());
    clickButton(panel, "Accept");

    List<FriendRequest> requests = panel.getRequests();
    assertTrue(
        requests.stream().anyMatch(request -> request.status() == FriendRequest.Status.ACCEPTED));
    assertFalse(panel.getVisibleRequests().stream()
        .anyMatch(request -> request.status() == FriendRequest.Status.ACCEPTED));
  }

  @Test
  void sendsFriendRequestAndShowsExistingRelationshipState() {
    HashSet<String> outgoing = new HashSet<>();
    HashSet<String> friends = new HashSet<>();
    FriendRequestsSearchPanel panel =
        new FriendRequestsSearchPanel(
            FriendRequestsSearchPanel.createSampleRequests(),
            FriendRequestsSearchPanel.createSampleDirectory(),
            new HashSet<>(),
            new HashSet<>(),
            friends,
            outgoing,
            profile -> {},
            profile -> {});
    panel.getSearchField().setText("blocked.user");

    clickButton(panel, "Add Friend");

    assertTrue(outgoing.contains("blocked.user"));
    assertTrue(findButton(panel, "Request Sent") != null);

    panel.getSearchField().setText("quang.vo");
    javax.swing.JButton received = findButton(panel, "Request Received");
    assertTrue(received != null);
    assertFalse(received.isEnabled());

    friends.add("blocked.user");
    panel.getSearchField().setText("blocked.user");
    javax.swing.JButton alreadyFriends = findButton(panel, "Friends");
    assertTrue(alreadyFriends != null);
    assertFalse(alreadyFriends.isEnabled());
  }

  @Test
  void filtersPeopleWhoBlockedMeAsWellAsPeopleIBlocked() {
    HashSet<String> blockedByMe = new HashSet<>();
    HashSet<String> blockedMe = new HashSet<>();
    blockedByMe.add("blocked.user");
    blockedByMe.add("thu.nguyen");
    blockedMe.add("minh.tran");
    FriendRequestsSearchPanel panel =
        new FriendRequestsSearchPanel(
            FriendRequestsSearchPanel.createSampleRequests(),
            FriendRequestsSearchPanel.createSampleDirectory(),
            blockedByMe,
            blockedMe,
            new HashSet<>(),
            new HashSet<>(),
            profile -> {},
            profile -> {});

    panel.getSearchField().setText("blocked.user");
    assertTrue(panel.getVisibleSearchResults().isEmpty());
    panel.getSearchField().setText("minh.tran");
    assertTrue(panel.getVisibleSearchResults().isEmpty());
    assertEquals(1, panel.getVisibleRequests().size());
    assertEquals("quang.vo", panel.getVisibleRequests().get(0).requester().username());
  }

  private FriendRequestsSearchPanel createPanel(HashSet<String> blocked) {
    return new FriendRequestsSearchPanel(
        FriendRequestsSearchPanel.createSampleRequests(),
        FriendRequestsSearchPanel.createSampleDirectory(),
        blocked,
        profile -> {},
        profile -> {});
  }

  private void clickButton(FriendRequestsSearchPanel panel, String text) {
    javax.swing.JButton button = findButton(panel, text);
    if (button == null) {
      throw new AssertionError("Button not found: " + text);
    }
    button.doClick();
  }

  private javax.swing.JButton findButton(java.awt.Container container, String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JButton button && button.getText().equals(text)) {
        return button;
      }
      if (component instanceof java.awt.Container nested) {
        javax.swing.JButton found = findButton(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
