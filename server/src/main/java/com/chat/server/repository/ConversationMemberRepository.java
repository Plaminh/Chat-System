package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.dbValue;
import static com.chat.server.repository.SqlSupport.enumValue;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.enums.GroupRole;
import com.chat.server.model.ConversationMember;
import com.chat.server.model.MemberView;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code conversation_members}. A user has one row per conversation; leaving sets
 * {@code left_at} and rejoining reactivates the same row with a new {@code joined_at}.
 */
@Repository
public class ConversationMemberRepository {
  private static final RowMapper<ConversationMember> MEMBER = (rs, n) -> Rows.member(rs);

  private static final RowMapper<MemberView> VIEW =
      (rs, n) ->
          new MemberView(
              Rows.userBrief(rs, "u_"),
              enumValue(GroupRole.class, rs.getString("member_role")),
              timestamp(rs, "joined_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public ConversationMemberRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Adds a member, or reactivates a member who left: the join time restarts, cleared history is
   * reset and the role is replaced. Returns false when the user is already an active member.
   */
  public boolean addOrReactivate(long conversationId, long userId, GroupRole role) {
    return jdbc.update(
            "INSERT INTO conversation_members (conversation_id, user_id, member_role)"
                + " VALUES (:conversation, :user, :role)"
                + " ON CONFLICT ON CONSTRAINT uq_conversation_members_pair DO UPDATE"
                + " SET member_role = EXCLUDED.member_role, joined_at = CURRENT_TIMESTAMP,"
                + " left_at = NULL, history_cleared_at = NULL"
                + " WHERE conversation_members.left_at IS NOT NULL",
            Map.of("conversation", conversationId, "user", userId, "role", dbValue(role)))
        == 1;
  }

  /** The membership row in any state. */
  public Optional<ConversationMember> find(long conversationId, long userId) {
    return jdbc
        .query(
            "SELECT "
                + Rows.MEMBER_COLUMNS
                + " FROM conversation_members cm"
                + " WHERE cm.conversation_id = :conversation AND cm.user_id = :user",
            Map.of("conversation", conversationId, "user", userId),
            MEMBER)
        .stream()
        .findFirst();
  }

  /** The membership row only while the user is an active member. */
  public Optional<ConversationMember> findActive(long conversationId, long userId) {
    return find(conversationId, userId).filter(ConversationMember::active);
  }

  /** Active members with their users, admins first, then by full name. */
  public List<MemberView> findActiveMembers(long conversationId) {
    return findMembers(conversationId, "");
  }

  /** Active group admins with their users, by full name. */
  public List<MemberView> findActiveAdmins(long conversationId) {
    return findMembers(conversationId, " AND cm.member_role = 'admin'");
  }

  /** IDs of active members, e.g. to deliver events. */
  public List<Long> findActiveMemberIds(long conversationId) {
    return jdbc.queryForList(
        "SELECT user_id FROM conversation_members"
            + " WHERE conversation_id = :conversation AND left_at IS NULL ORDER BY user_id",
        Map.of("conversation", conversationId),
        Long.class);
  }

  public long countActiveAdmins(long conversationId) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM conversation_members WHERE conversation_id = :conversation"
            + " AND left_at IS NULL AND member_role = 'admin'",
        Map.of("conversation", conversationId),
        Long.class);
  }

  /** Changes an active member's role; false when the user is not an active member. */
  public boolean updateRole(long conversationId, long userId, GroupRole role) {
    return jdbc.update(
            "UPDATE conversation_members SET member_role = :role"
                + " WHERE conversation_id = :conversation AND user_id = :user AND left_at IS NULL",
            Map.of("conversation", conversationId, "user", userId, "role", dbValue(role)))
        == 1;
  }

  /** Marks an active member as left (removed or leaving); false when not an active member. */
  public boolean markLeft(long conversationId, long userId) {
    return jdbc.update(
            "UPDATE conversation_members SET left_at = CURRENT_TIMESTAMP"
                + " WHERE conversation_id = :conversation AND user_id = :user AND left_at IS NULL",
            Map.of("conversation", conversationId, "user", userId))
        == 1;
  }

  /**
   * Hides every message sent until now from this member only. Returns false when the user is not an
   * active member.
   */
  public boolean clearHistory(long conversationId, long userId) {
    return jdbc.update(
            "UPDATE conversation_members SET history_cleared_at = CURRENT_TIMESTAMP"
                + " WHERE conversation_id = :conversation AND user_id = :user AND left_at IS NULL",
            Map.of("conversation", conversationId, "user", userId))
        == 1;
  }

  private List<MemberView> findMembers(long conversationId, String extraCondition) {
    return jdbc.query(
        "SELECT cm.member_role, cm.joined_at, "
            + Rows.userBriefColumns("u", "u_")
            + " FROM conversation_members cm JOIN users u ON u.user_id = cm.user_id"
            + " WHERE cm.conversation_id = :conversation AND cm.left_at IS NULL"
            + extraCondition
            + " ORDER BY CASE cm.member_role WHEN 'admin' THEN 0 ELSE 1 END,"
            + " lower(u.full_name), u.user_id",
        Map.of("conversation", conversationId),
        VIEW);
  }
}
