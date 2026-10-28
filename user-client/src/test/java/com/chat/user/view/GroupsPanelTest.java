package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import com.chat.user.model.UserProfile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupsPanelTest {
  @Test
  void groupCreationRequiresAnotherParticipant() {
    UserProfile creator = profile("creator", "Creator");
    CreateGroupDialog dialog =
        new CreateGroupDialog(creator, List.of(creator, profile("member", "Member")), group -> {});
    dialog.getGroupNameField().setText("Sample group");

    dialog.submitCreation();

    assertTrue(dialog.getCreatedGroup() == null);
    assertTrue(dialog.getFeedbackLabel().getText().contains("ít nhất một"));
  }

  @Test
  void groupCreationAddsCreatorAndSelectedMember() {
    UserProfile creator = profile("creator", "Creator");
    UserProfile other = profile("member", "Member");
    List<GroupConversation> created = new ArrayList<>();
    CreateGroupDialog dialog = new CreateGroupDialog(creator, List.of(creator, other), created::add);
    dialog.getGroupNameField().setText("Sample group");
    dialog.getParticipantList().setSelectedValue(other, true);

    dialog.submitCreation();

    assertEquals(2, created.get(0).members().size());
    assertEquals(GroupMember.Role.ADMIN, created.get(0).members().get(0).role());
  }

  @Test
  void memberRoleAndRemovalActionsUpdateLocalGroupOnlyAfterConfirmation() {
    UserProfile creator = profile("creator", "Creator");
    UserProfile other = profile("member", "Member");
    GroupConversation group =
        new GroupConversation(
            "group-1",
            "Sample group",
            List.of(
                new GroupMember(creator, GroupMember.Role.ADMIN),
                new GroupMember(other, GroupMember.Role.MEMBER)),
            List.of());
    GroupMembersDialog dialog =
        new GroupMembersDialog(group, List.of(creator, other), (member, action) -> false);

    assertTrue(dialog.performMemberAction("member", GroupMembersDialog.MemberAction.TOGGLE_ADMIN));
    assertEquals(GroupMember.Role.ADMIN, group.members().get(1).role());
    assertFalse(dialog.performMemberAction("member", GroupMembersDialog.MemberAction.REMOVE));
    assertEquals(2, group.members().size());
  }

  @Test
  void membersCanBeAddedAndConfirmedRemovalChangesSampleGroup() {
    UserProfile creator = profile("creator", "Creator");
    UserProfile other = profile("member", "Member");
    UserProfile added = profile("added", "Added");
    GroupConversation group =
        new GroupConversation(
            "group-1",
            "Sample group",
            List.of(
                new GroupMember(creator, GroupMember.Role.ADMIN),
                new GroupMember(other, GroupMember.Role.MEMBER)),
            List.of());
    GroupMembersDialog dialog =
        new GroupMembersDialog(group, List.of(creator, other, added), (member, action) -> true);
    dialog.getAddMemberSelector().setSelectedItem(added);

    dialog.addSelectedMember();

    assertTrue(group.containsMember("added"));
    assertTrue(dialog.performMemberAction("member", GroupMembersDialog.MemberAction.REMOVE));
    assertFalse(group.containsMember("member"));
  }

  @Test
  void groupPanelAddsCreatedGroupsAndAcceptsBlockedSet() {
    UserProfile creator = profile("creator", "Creator");
    UserProfile other = profile("member", "Member");
    UserProfile another = profile("another", "Another");
    GroupConversation initial =
        new GroupConversation(
            "group-1",
            "Initial",
            List.of(
                new GroupMember(creator, GroupMember.Role.ADMIN),
                new GroupMember(other, GroupMember.Role.MEMBER)),
            List.of());
    GroupsPanel panel =
        new GroupsPanel(
            creator,
            List.of(initial),
            List.of(creator, other, another),
            new HashSet<>(),
            (group, action) -> true,
            group -> {});

    GroupConversation added =
        new GroupConversation(
            "group-2",
            "Added group",
            List.of(
                new GroupMember(creator, GroupMember.Role.ADMIN),
                new GroupMember(another, GroupMember.Role.MEMBER)),
            List.of());
    panel.addGroup(added);
    panel.renameGroup(added, "Renamed group");

    assertEquals(2, panel.getGroups().size());
    assertEquals("Renamed group", panel.getGroups().get(1).name());
  }

  private UserProfile profile(String username, String fullName) {
    return new UserProfile(
        username, fullName, username + "@example.test", "Sample", LocalDate.of(2000, 1, 1), "N/A");
  }
}
