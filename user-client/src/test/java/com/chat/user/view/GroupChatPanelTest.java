package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupChatPanelTest {
  @Test
  void displaysGroupRolesAndNonFunctionalEncryptionPlaceholder() {
    GroupConversation group = createGroup();
    GroupChatPanel panel = new GroupChatPanel(group, "creator");

    assertEquals(2, panel.getMemberList().getComponentCount());
    assertTrue(panel.getEncryptionPlaceholder().getText().contains("chưa được kích hoạt"));
  }

  @Test
  void sampleSendAddsMessageToLocalGroupConversation() {
    GroupConversation group = createGroup();
    GroupChatPanel panel = new GroupChatPanel(group, "creator");
    panel.getComposer().setText("Sample group message");

    panel.sendSampleMessage();

    assertEquals(1, group.messages().size());
    assertEquals("Sample group message", group.messages().get(0).content());
    assertTrue(panel.getFeedbackLabel().getText().contains("phiên này"));
  }

  private GroupConversation createGroup() {
    UserProfile creator = profile("creator", "Creator");
    UserProfile member = profile("member", "Member");
    return new GroupConversation(
        "group-1",
        "Sample group",
        List.of(
            new GroupMember(creator, GroupMember.Role.ADMIN),
            new GroupMember(member, GroupMember.Role.MEMBER)),
        List.of());
  }

  private UserProfile profile(String username, String fullName) {
    return new UserProfile(
        username, fullName, username + "@example.test", "Sample", LocalDate.of(2000, 1, 1), "N/A");
  }
}
