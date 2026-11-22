package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.ConversationType;
import com.chat.common.enums.GroupRole;
import com.chat.common.enums.SortDirection;
import com.chat.server.model.Conversation;
import com.chat.server.model.ConversationMember;
import com.chat.server.model.ConversationOverview;
import com.chat.server.model.GroupOverview;
import com.chat.server.model.MemberView;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

class ConversationRepositoryTest extends RepositoryTestSupport {
  @Autowired ConversationRepository conversations;
  @Autowired ConversationMemberRepository members;
  @Autowired MessageRepository messages;

  @Test
  void createsOneDirectConversationPerPairWithBothMembers() {
    inRollback(
        () -> {
          long a = newUser("cDirA");
          long b = newUser("cDirB");
          long id = conversations.createDirect(b, a);

          Conversation direct = conversations.findDirectBetween(a, b).orElseThrow();
          assertEquals(id, direct.conversationId());
          assertEquals(ConversationType.DIRECT, direct.type());
          assertNull(direct.name());
          assertNull(direct.ownerUserId());
          assertEquals(List.of(Math.min(a, b), Math.max(a, b)), members.findActiveMemberIds(id));
          assertFailsInSavepoint(
              DuplicateKeyException.class, () -> conversations.createDirect(a, b));
          assertThrows(IllegalArgumentException.class, () -> conversations.createDirect(a, a));
        });
  }

  @Test
  void managesGroupMembershipRolesAndReactivation() {
    inRollback(
        () -> {
          long owner = newUser("cGrpOwner");
          long member = newUser("cGrpMember");
          long id = conversations.createGroup("Nhom test", owner, false);
          assertTrue(members.addOrReactivate(id, owner, GroupRole.ADMIN));
          assertTrue(members.addOrReactivate(id, member, GroupRole.MEMBER));
          assertFalse(
              members.addOrReactivate(id, member, GroupRole.ADMIN),
              "an active member is not re-added");

          List<MemberView> roster = members.findActiveMembers(id);
          assertEquals(
              List.of(owner, member), roster.stream().map(m -> m.user().userId()).toList());
          assertEquals(GroupRole.ADMIN, roster.get(0).role());
          assertEquals(1, members.countActiveAdmins(id));

          assertTrue(members.updateRole(id, member, GroupRole.ADMIN));
          assertEquals(2, members.findActiveAdmins(id).size());

          assertTrue(members.clearHistory(id, member));
          assertTrue(members.markLeft(id, member));
          assertFalse(members.markLeft(id, member));
          assertTrue(members.findActive(id, member).isEmpty());
          assertFalse(members.updateRole(id, member, GroupRole.MEMBER));
          assertEquals(List.of(owner), members.findActiveMemberIds(id));

          assertTrue(members.addOrReactivate(id, member, GroupRole.MEMBER));
          ConversationMember back = members.findActive(id, member).orElseThrow();
          assertEquals(GroupRole.MEMBER, back.role());
          assertNull(back.leftAt());
          assertNull(back.historyClearedAt(), "rejoining resets cleared history");
        });
  }

  @Test
  void renamesOnlyGroups() {
    inRollback(
        () -> {
          long a = newUser("cRenA");
          long b = newUser("cRenB");
          long group = conversations.createGroup("Old name", a, true);
          long direct = conversations.createDirect(a, b);

          assertTrue(conversations.renameGroup(group, "New name"));
          assertFalse(conversations.renameGroup(direct, "Not allowed"));
          Conversation renamed = conversations.findById(group).orElseThrow();
          assertEquals("New name", renamed.name());
          assertTrue(renamed.encryptionEnabled());
          assertTrue(conversations.touch(group));
        });
  }

  @Test
  void listsUserConversationsWithDirectPeerAndLatestMessage() {
    inRollback(
        () -> {
          long me = newUser("cLstMe");
          long peer = newUser("cLstPeer");
          long direct = conversations.createDirect(me, peer);
          long group = conversations.createGroup("Lst group", me, false);
          members.addOrReactivate(group, me, GroupRole.ADMIN);
          long left = conversations.createGroup("Lst left", me, false);
          members.addOrReactivate(left, me, GroupRole.ADMIN);
          members.markLeft(left, me);

          long message = messages.insertPlain(direct, peer, "xin chao");
          shiftTime("messages", "created_at", "message_id", message, 1);

          List<ConversationOverview> list = conversations.findForUser(me, FIRST_PAGE).items();
          assertEquals(
              List.of(direct, group),
              list.stream().map(o -> o.conversation().conversationId()).toList(),
              "the conversation with the newest message comes first; left groups are hidden");
          assertEquals(peer, list.get(0).directPeer().userId());
          assertNotNull(list.get(0).lastMessageAt());
          assertNull(list.get(1).directPeer());
          assertNull(list.get(1).lastMessageAt());

          assertEquals(
              List.of(group),
              conversations.findGroupsForUser(me, FIRST_PAGE).items().stream()
                  .map(Conversation::conversationId)
                  .toList());
        });
  }

  @Test
  void listsGroupsForAdminWithActiveMemberCounts() {
    inRollback(
        () -> {
          long owner = newUser("cAdmOwner");
          long other = newUser("cAdmOther");
          long beta = conversations.createGroup("cAdmGroup Beta", owner, false);
          long alpha = conversations.createGroup("cAdmGroup Alpha", owner, false);
          members.addOrReactivate(alpha, owner, GroupRole.ADMIN);
          members.addOrReactivate(alpha, other, GroupRole.MEMBER);
          members.addOrReactivate(beta, owner, GroupRole.ADMIN);
          members.addOrReactivate(beta, other, GroupRole.MEMBER);
          members.markLeft(beta, other);

          List<GroupOverview> byName =
              conversations
                  .findGroupsForAdmin(
                      "cadmgroup", new SortSpec("name", SortDirection.ASC), FIRST_PAGE)
                  .items();
          assertEquals(
              List.of(alpha, beta), byName.stream().map(GroupOverview::conversationId).toList());
          assertEquals(2, byName.get(0).memberCount());
          assertEquals(1, byName.get(1).memberCount());
          assertEquals(
              1,
              conversations
                  .findGroupsForAdmin(
                      "cAdmGroup Beta", new SortSpec("createdAt", SortDirection.DESC), FIRST_PAGE)
                  .totalItems());
        });
  }
}
