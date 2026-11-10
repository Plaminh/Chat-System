package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

class GroupChatPanelTest {
  @Test
  void standardGroupDisplaysModeAndMemberInformation() {
    GroupConversation group = createGroup(false);
    GroupChatPanel panel = new GroupChatPanel(group, "creator");

    String memberText = collectLabelText(panel.getMemberList());
    assertEquals("Standard Group", panel.getEncryptionPlaceholder().getText());
    assertTrue(memberText.contains("Creator"));
    assertTrue(memberText.contains("@creator"));
    assertTrue(memberText.contains("Administrator"));
    assertTrue(memberText.contains("Member"));
  }

  @Test
  void encryptedGroupDisplaysPrototypeModeAndV2Disclaimer() {
    GroupConversation group = createGroup(true);
    GroupChatPanel panel = new GroupChatPanel(group, "creator");

    String panelText = collectLabelText(panel);
    assertEquals("Encrypted Group (Prototype)", panel.getEncryptionPlaceholder().getText());
    assertTrue(panelText.contains("V1 display only"));
    assertTrue(panelText.contains("encryption will be implemented in V2"));
  }

  @Test
  void sampleSendAddsMessageToLocalGroupConversation() {
    GroupConversation group = createGroup(false);
    GroupChatPanel panel = new GroupChatPanel(group, "creator");
    panel.getComposer().setText("Sample group message");

    panel.sendSampleMessage();

    assertEquals(1, group.messages().size());
    assertEquals("Sample group message", group.messages().get(0).content());
    assertTrue(panel.getFeedbackLabel().getText().contains("this session only"));
  }

  private GroupConversation createGroup(boolean encrypted) {
    UserProfile creator = profile("creator", "Creator");
    UserProfile member = profile("member", "Member");
    return new GroupConversation(
        "group-1",
        "Sample group",
        List.of(
            new GroupMember(creator, GroupMember.Role.ADMIN),
            new GroupMember(member, GroupMember.Role.MEMBER)),
        List.of(),
        encrypted);
  }

  private UserProfile profile(String username, String fullName) {
    return new UserProfile(
        username, fullName, username + "@example.test", "Sample", LocalDate.of(2000, 1, 1), "N/A");
  }

  private String collectLabelText(Container container) {
    StringBuilder text = new StringBuilder();
    for (Component component : container.getComponents()) {
      if (component instanceof JLabel label) {
        text.append(' ').append(label.getText());
      }
      if (component instanceof Container child) {
        text.append(collectLabelText(child));
      }
    }
    return text.toString();
  }
}
