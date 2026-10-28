package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.FriendEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class FriendsPanelTest {
  @Test
  void filtersByNameAndOnlineStatus() {
    FriendsPanel panel = createPanel(new ArrayList<>(FriendsPanel.createSampleFriends()));

    panel.getSearchField().setText("mai");
    assertEquals(1, panel.getVisibleFriends().size());
    assertEquals("mai.anh", panel.getVisibleFriends().get(0).username());

    panel.getSearchField().setText("");
    panel.getOnlineOnlyToggle().doClick();
    assertEquals(2, panel.getVisibleFriends().size());
    assertTrue(panel.getVisibleFriends().stream().allMatch(FriendEntry::online));
  }

  @Test
  void cancelledRelationshipActionDoesNotChangeFriendOrBlockState() {
    List<FriendEntry> friends = new ArrayList<>(FriendsPanel.createSampleFriends());
    HashSet<String> blocked = new HashSet<>();
    FriendsPanel panel =
        new FriendsPanel(friends, blocked, (friend, action) -> false, friend -> {});

    findButton(panel, "Chặn").doClick();

    assertEquals(3, panel.getVisibleFriends().size());
    assertTrue(blocked.isEmpty());
  }

  @Test
  void confirmedBlockRemovesFriendAndAddsBlockedUsername() {
    List<FriendEntry> friends = new ArrayList<>(FriendsPanel.createSampleFriends());
    HashSet<String> blocked = new HashSet<>();
    FriendsPanel panel =
        new FriendsPanel(friends, blocked, (friend, action) -> true, friend -> {});
    panel.getSearchField().setText("mai");
    javax.swing.JButton blockButton = findButton(panel, "Chặn");
    blockButton.doClick();

    assertFalse(blocked.isEmpty());
    assertTrue(blocked.contains("mai.anh"));
    assertTrue(panel.getVisibleFriends().isEmpty());
  }

  private FriendsPanel createPanel(List<FriendEntry> friends) {
    return new FriendsPanel(friends, new HashSet<>(), (friend, action) -> false, friend -> {});
  }

  private javax.swing.JButton findButton(FriendsPanel panel, String text) {
    javax.swing.JButton button = findButtonRecursively(panel, text);
    assertNotNull(button);
    return button;
  }

  private javax.swing.JButton findButtonRecursively(java.awt.Container container, String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JButton button && button.getText().equals(text)) {
        return button;
      }
      if (component instanceof java.awt.Container nested) {
        javax.swing.JButton found = findButtonRecursively(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
