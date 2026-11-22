package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.enumValue;
import static com.chat.server.repository.SqlSupport.nullableInt;
import static com.chat.server.repository.SqlSupport.nullableLong;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ConversationType;
import com.chat.common.enums.GroupRole;
import com.chat.server.model.Conversation;
import com.chat.server.model.ConversationMember;
import com.chat.server.model.Message;
import com.chat.server.model.UserBrief;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Row mappers shared by several repositories. */
final class Rows {
  /**
   * Selects the {@link UserBrief} columns of a {@code users} alias, each renamed with {@code
   * prefix}.
   */
  static String userBriefColumns(String alias, String prefix) {
    return alias
        + ".user_id AS "
        + prefix
        + "user_id, "
        + alias
        + ".username AS "
        + prefix
        + "username, "
        + alias
        + ".full_name AS "
        + prefix
        + "full_name, "
        + alias
        + ".account_status AS "
        + prefix
        + "account_status, "
        + alias
        + ".created_at AS "
        + prefix
        + "created_at";
  }

  static final String CONVERSATION_COLUMNS =
      "c.conversation_id, c.conversation_type, c.name, c.owner_user_id, c.encryption_enabled,"
          + " c.created_at, c.updated_at";

  static final String MEMBER_COLUMNS =
      "cm.conversation_member_id, cm.conversation_id, cm.user_id, cm.member_role, cm.joined_at,"
          + " cm.left_at, cm.history_cleared_at";

  static final String MESSAGE_COLUMNS =
      "m.message_id, m.conversation_id, m.sender_user_id, m.body, m.content_encrypted,"
          + " m.ciphertext, m.encryption_key_version, m.encryption_nonce, m.authentication_tag,"
          + " m.created_at, m.unsent_at, m.unsent_by";

  private Rows() {}

  /**
   * Reads a user projection selected with {@link #userBriefColumns}; null when the user is absent.
   */
  static UserBrief userBrief(ResultSet rs, String prefix) throws SQLException {
    Long userId = nullableLong(rs, prefix + "user_id");
    if (userId == null) {
      return null;
    }
    return new UserBrief(
        userId,
        rs.getString(prefix + "username"),
        rs.getString(prefix + "full_name"),
        enumValue(AccountStatus.class, rs.getString(prefix + "account_status")),
        timestamp(rs, prefix + "created_at"));
  }

  static Conversation conversation(ResultSet rs) throws SQLException {
    return new Conversation(
        rs.getLong("conversation_id"),
        enumValue(ConversationType.class, rs.getString("conversation_type")),
        rs.getString("name"),
        nullableLong(rs, "owner_user_id"),
        rs.getBoolean("encryption_enabled"),
        timestamp(rs, "created_at"),
        timestamp(rs, "updated_at"));
  }

  static ConversationMember member(ResultSet rs) throws SQLException {
    return new ConversationMember(
        rs.getLong("conversation_member_id"),
        rs.getLong("conversation_id"),
        rs.getLong("user_id"),
        enumValue(GroupRole.class, rs.getString("member_role")),
        timestamp(rs, "joined_at"),
        timestamp(rs, "left_at"),
        timestamp(rs, "history_cleared_at"));
  }

  static Message message(ResultSet rs) throws SQLException {
    return new Message(
        rs.getLong("message_id"),
        rs.getLong("conversation_id"),
        nullableLong(rs, "sender_user_id"),
        rs.getString("body"),
        rs.getBoolean("content_encrypted"),
        rs.getBytes("ciphertext"),
        nullableInt(rs, "encryption_key_version"),
        rs.getBytes("encryption_nonce"),
        rs.getBytes("authentication_tag"),
        timestamp(rs, "created_at"),
        timestamp(rs, "unsent_at"),
        nullableLong(rs, "unsent_by"));
  }
}
