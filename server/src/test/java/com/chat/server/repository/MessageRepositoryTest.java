package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.common.DateRange;
import com.chat.common.enums.GroupRole;
import com.chat.server.model.ConversationKeyEnvelope;
import com.chat.server.model.EncryptedPayload;
import com.chat.server.model.Message;
import com.chat.server.model.MessageView;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class MessageRepositoryTest extends RepositoryTestSupport {
  @Autowired ConversationRepository conversations;
  @Autowired ConversationMemberRepository members;
  @Autowired MessageRepository messages;
  @Autowired ConversationKeyRepository keys;
  @Autowired UserPublicKeyRepository publicKeys;

  @Test
  void historyRespectsDeletionClearedHistoryAndOrdering() {
    inRollback(
        () -> {
          long a = newUser("mHisA");
          long b = newUser("mHisB");
          long chat = conversations.createDirect(a, b);
          moveJoinTimes(chat, -60);
          long old = sendAt(chat, a, "old message", -30);
          long hidden = sendAt(chat, b, "deleted for a", -20);
          long recent = sendAt(chat, a, "recent message", -10);

          assertEquals(
              List.of(recent, hidden, old), ids(messages.findHistory(chat, a, FIRST_PAGE).items()));

          assertEquals(1, messages.deleteForUser(a, List.of(hidden)));
          assertEquals(0, messages.deleteForUser(a, List.of(hidden)), "deleting twice is a no-op");
          assertEquals(
              List.of(recent, old), ids(messages.findHistory(chat, a, FIRST_PAGE).items()));
          assertEquals(
              3, messages.findHistory(chat, b, FIRST_PAGE).totalItems(), "b still sees it");
          assertTrue(messages.findVisible(hidden, a).isEmpty());

          jdbc.update(
              "UPDATE conversation_members SET history_cleared_at = CURRENT_TIMESTAMP"
                  + " + make_interval(mins => -15) WHERE conversation_id = ? AND user_id = ?",
              chat,
              a);
          assertEquals(List.of(recent), ids(messages.findHistory(chat, a, FIRST_PAGE).items()));
          assertEquals(
              recent, messages.findLatestVisible(chat, a).orElseThrow().message().messageId());

          MessageView view = messages.findVisible(recent, b).orElseThrow();
          assertEquals(a, view.sender().userId());
          assertEquals("recent message", view.message().body());
        });
  }

  @Test
  void formerMembersOnlySeeMessagesUntilTheyLeft() {
    inRollback(
        () -> {
          long owner = newUser("mLeftOwner");
          long leaver = newUser("mLeftLeaver");
          long group = conversations.createGroup("Left test", owner, false);
          members.addOrReactivate(group, owner, GroupRole.ADMIN);
          members.addOrReactivate(group, leaver, GroupRole.MEMBER);
          moveJoinTimes(group, -60);
          long before = sendAt(group, owner, "before leaving", -30);
          members.markLeft(group, leaver);
          jdbc.update(
              "UPDATE conversation_members SET left_at = CURRENT_TIMESTAMP"
                  + " + make_interval(mins => -20) WHERE conversation_id = ? AND user_id = ?",
              group,
              leaver);
          sendAt(group, owner, "after leaving", -10);

          assertEquals(
              List.of(before), ids(messages.findHistory(group, leaver, FIRST_PAGE).items()));
          assertEquals(2, messages.findHistory(group, owner, FIRST_PAGE).totalItems());
        });
  }

  @Test
  void searchIsLiteralCaseInsensitiveAndScopedToVisibleMessages() {
    inRollback(
        () -> {
          long a = newUser("mSrchA");
          long b = newUser("mSrchB");
          long c = newUser("mSrchC");
          long ab = conversations.createDirect(a, b);
          long bc = conversations.createDirect(b, c);
          long match = messages.insertPlain(ab, b, "Hen gap luc 7h, 100% nhe");
          messages.insertPlain(ab, b, "khong lien quan");
          messages.insertPlain(bc, b, "100% rieng tu cua b va c");
          long unsent = messages.insertPlain(ab, a, "100% se thu hoi");
          messages.unsend(unsent, a);

          assertEquals(
              List.of(match), ids(messages.search(a, null, "100%", null, FIRST_PAGE).items()));
          assertEquals(
              List.of(match), ids(messages.search(a, ab, "HEN GAP", null, FIRST_PAGE).items()));
          assertEquals(0, messages.search(a, null, "100_", null, FIRST_PAGE).totalItems());
          assertEquals(0, messages.search(a, bc, "100%", null, FIRST_PAGE).totalItems());

          LocalDate today = today();
          assertEquals(
              1,
              messages
                  .search(
                      a,
                      null,
                      "hen",
                      new DateRange(today.minusDays(1), today.plusDays(1)),
                      FIRST_PAGE)
                  .totalItems());
          assertEquals(
              0,
              messages
                  .search(
                      a,
                      null,
                      "hen",
                      new DateRange(today.minusDays(9), today.minusDays(2)),
                      FIRST_PAGE)
                  .totalItems());
        });
  }

  @Test
  void unsendReplacesContentOnce() {
    inRollback(
        () -> {
          long a = newUser("mUnsA");
          long b = newUser("mUnsB");
          long chat = conversations.createDirect(a, b);
          long id = messages.insertPlain(chat, a, "oops");

          assertTrue(messages.unsend(id, a));
          assertFalse(messages.unsend(id, a));
          Message message = messages.findById(id).orElseThrow();
          assertEquals(Message.UNSENT_BODY, message.body());
          assertEquals(a, message.unsentBy());
          assertEquals(
              1, messages.findHistory(chat, b, FIRST_PAGE).totalItems(), "placeholder stays");
        });
  }

  @Test
  void storesEncryptedGroupMessagesKeyEpochsAndEnvelopes() {
    inRollback(
        () -> {
          long owner = newUser("mEncOwner");
          long member = newUser("mEncMember");
          long ownerKey = publicKeys.insert(owner, 1, filled(32, 1));
          long memberKey = publicKeys.insert(member, 1, filled(32, 2));
          long group = conversations.createGroup("Encrypted", owner, true);
          members.addOrReactivate(group, owner, GroupRole.ADMIN);
          members.addOrReactivate(group, member, GroupRole.MEMBER);

          long epoch = keys.insertVersion(group, 1);
          keys.insertEnvelope(epoch, ownerKey, filled(32, 3), filled(48, 4));
          keys.insertEnvelope(epoch, memberKey, filled(32, 5), filled(48, 6));
          assertEquals(1, keys.findActiveVersion(group).orElseThrow().keyVersion());
          assertEquals(2, keys.findEnvelopes(epoch).size());
          ConversationKeyEnvelope forMember =
              keys.findEnvelopeForUser(group, 1, member).orElseThrow();
          assertEquals(memberKey, forMember.recipientPublicKeyId());
          assertArrayEquals(filled(48, 6), forMember.encryptedGroupKey());

          long encrypted =
              messages.insertEncrypted(
                  group,
                  owner,
                  new EncryptedPayload(1, filled(20, 7), filled(12, 8), filled(16, 9)));
          Message stored = messages.findById(encrypted).orElseThrow();
          assertTrue(stored.contentEncrypted());
          assertNull(stored.body());
          assertEquals(1, stored.encryptionKeyVersion());
          assertEquals(
              0,
              messages.search(member, group, "anything", null, FIRST_PAGE).totalItems(),
              "the server never searches ciphertext");
          assertFailsInSavepoint(
              DataIntegrityViolationException.class,
              () ->
                  messages.insertPlain(group, owner, "plaintext is rejected in encrypted groups"));

          assertTrue(messages.unsend(encrypted, owner), "unsend must keep the encryption flag");
          Message unsent = messages.findById(encrypted).orElseThrow();
          assertEquals(Message.UNSENT_BODY, unsent.body());
          assertNull(unsent.ciphertext());

          assertEquals(1, keys.retireActiveVersions(group));
          long next = keys.insertVersion(group, 2);
          assertEquals(
              next, keys.findActiveVersion(group).orElseThrow().conversationKeyVersionId());
          assertEquals(2, keys.findLatestVersionNumber(group));
          assertTrue(keys.findVersion(group, 1).orElseThrow().retiredAt() != null);
        });
  }

  @Test
  void detectsWhetherAUserReceivedMessagesFromAnother() {
    inRollback(
        () -> {
          long a = newUser("mRecvA");
          long b = newUser("mRecvB");
          long stranger = newUser("mRecvC");
          long chat = conversations.createDirect(a, b);

          assertFalse(messages.hasReceivedMessageFrom(a, b));
          messages.insertPlain(chat, b, "hello");
          assertTrue(messages.hasReceivedMessageFrom(a, b));
          assertFalse(messages.hasReceivedMessageFrom(b, a));
          assertFalse(messages.hasReceivedMessageFrom(stranger, b));
        });
  }

  /** Moves every member's join time of the conversation into the past. */
  private void moveJoinTimes(long conversationId, int minutes) {
    jdbc.update(
        "UPDATE conversation_members SET joined_at = CURRENT_TIMESTAMP"
            + " + make_interval(mins => ?) WHERE conversation_id = ?",
        minutes,
        conversationId);
  }

  private long sendAt(long conversationId, long senderId, String body, int minutesFromNow) {
    long id = messages.insertPlain(conversationId, senderId, body);
    shiftTime("messages", "created_at", "message_id", id, minutesFromNow);
    return id;
  }

  private static byte[] filled(int length, int value) {
    byte[] bytes = new byte[length];
    java.util.Arrays.fill(bytes, (byte) value);
    return bytes;
  }

  private static List<Long> ids(List<MessageView> views) {
    return views.stream().map(v -> v.message().messageId()).toList();
  }
}
