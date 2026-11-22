package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.orderBy;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.server.model.Conversation;
import com.chat.server.model.ConversationOverview;
import com.chat.server.model.GroupOverview;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code conversations} and {@code direct_conversations}. */
@Repository
public class ConversationRepository {
  /** Sort fields accepted by {@link #findGroupsForAdmin}. */
  static final Map<String, String> GROUP_SORT =
      Map.of("name", "lower(c.name)", "createdAt", "c.created_at");

  private static final RowMapper<Conversation> CONVERSATION = (rs, n) -> Rows.conversation(rs);

  private static final RowMapper<GroupOverview> GROUP =
      (rs, n) ->
          new GroupOverview(
              rs.getLong("conversation_id"),
              rs.getString("name"),
              rs.getLong("owner_user_id"),
              rs.getBoolean("encryption_enabled"),
              timestamp(rs, "created_at"),
              timestamp(rs, "updated_at"),
              rs.getLong("member_count"));

  private static final RowMapper<ConversationOverview> OVERVIEW =
      (rs, n) ->
          new ConversationOverview(
              Rows.conversation(rs), Rows.userBrief(rs, "p_"), timestamp(rs, "last_message_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public ConversationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Creates a direct conversation, its canonical pair row and both memberships in one statement.
   * Fails with a unique violation when the pair already has a direct conversation.
   */
  public long createDirect(long userA, long userB) {
    if (userA == userB) {
      throw new IllegalArgumentException("A direct conversation needs two different users");
    }
    return jdbc.queryForObject(
        "WITH c AS (INSERT INTO conversations (conversation_type) VALUES ('direct')"
            + " RETURNING conversation_id),"
            + " d AS (INSERT INTO direct_conversations (conversation_id, user_low_id, user_high_id)"
            + " SELECT conversation_id, :low, :high FROM c),"
            + " m AS (INSERT INTO conversation_members (conversation_id, user_id)"
            + " SELECT conversation_id, :low FROM c UNION ALL SELECT conversation_id, :high FROM c)"
            + " SELECT conversation_id FROM c",
        new MapSqlParameterSource()
            .addValue("low", Math.min(userA, userB))
            .addValue("high", Math.max(userA, userB)),
        Long.class);
  }

  /**
   * Creates a group conversation row only and returns its ID. Add the members, including the owner
   * as admin, in the same transaction. The encryption mode cannot change later.
   */
  public long createGroup(String name, long ownerUserId, boolean encryptionEnabled) {
    return jdbc.queryForObject(
        "INSERT INTO conversations (conversation_type, name, owner_user_id, encryption_enabled)"
            + " VALUES ('group', :name, :owner, :encrypted) RETURNING conversation_id",
        Map.of("name", name, "owner", ownerUserId, "encrypted", encryptionEnabled),
        Long.class);
  }

  public Optional<Conversation> findById(long conversationId) {
    return jdbc
        .query(
            "SELECT "
                + Rows.CONVERSATION_COLUMNS
                + " FROM conversations c"
                + " WHERE c.conversation_id = :id",
            Map.of("id", conversationId),
            CONVERSATION)
        .stream()
        .findFirst();
  }

  /** The direct conversation between the two users, if one exists. */
  public Optional<Conversation> findDirectBetween(long userA, long userB) {
    return jdbc
        .query(
            "SELECT "
                + Rows.CONVERSATION_COLUMNS
                + " FROM direct_conversations d"
                + " JOIN conversations c ON c.conversation_id = d.conversation_id"
                + " WHERE d.user_low_id = :low AND d.user_high_id = :high",
            new MapSqlParameterSource()
                .addValue("low", Math.min(userA, userB))
                .addValue("high", Math.max(userA, userB)),
            CONVERSATION)
        .stream()
        .findFirst();
  }

  /** Renames a group; false when the conversation is missing or not a group. */
  public boolean renameGroup(long conversationId, String name) {
    return jdbc.update(
            "UPDATE conversations SET name = :name, updated_at = CURRENT_TIMESTAMP"
                + " WHERE conversation_id = :id AND conversation_type = 'group'",
            Map.of("id", conversationId, "name", name))
        == 1;
  }

  /** Updates {@code updated_at}, e.g. after a membership change. */
  public boolean touch(long conversationId) {
    return jdbc.update(
            "UPDATE conversations SET updated_at = CURRENT_TIMESTAMP"
                + " WHERE conversation_id = :id",
            Map.of("id", conversationId))
        == 1;
  }

  /**
   * Conversations where the user is an active member, most recent activity first. For direct
   * conversations the other participant is included. The last message time respects the user's join
   * time and cleared history.
   */
  public PageResult<ConversationOverview> findForUser(long userId, PageRequest page) {
    Map<String, Long> params = Map.of("me", userId);
    long total =
        jdbc.queryForObject(
            "SELECT count(*) FROM conversation_members"
                + " WHERE user_id = :me AND left_at IS NULL",
            params,
            Long.class);
    List<ConversationOverview> items =
        jdbc.query(
            "SELECT "
                + Rows.CONVERSATION_COLUMNS
                + ", "
                + Rows.userBriefColumns("p", "p_")
                + ", last.last_message_at FROM conversation_members cm JOIN conversations c ON"
                + " c.conversation_id = cm.conversation_id LEFT JOIN direct_conversations d ON"
                + " d.conversation_id = c.conversation_id LEFT JOIN users p ON p.user_id = CASE"
                + " WHEN d.user_low_id = :me THEN d.user_high_id ELSE d.user_low_id END LEFT JOIN"
                + " LATERAL (SELECT max(m.created_at) AS last_message_at FROM messages m WHERE"
                + " m.conversation_id = c.conversation_id AND m.created_at >= cm.joined_at AND"
                + " (cm.history_cleared_at IS NULL OR m.created_at > cm.history_cleared_at)) last"
                + " ON TRUE WHERE cm.user_id = :me AND cm.left_at IS NULL ORDER BY"
                + " coalesce(last.last_message_at, c.updated_at) DESC, c.conversation_id DESC"
                + limitOffset(page),
            params,
            OVERVIEW);
    return SqlSupport.page(items, page, total);
  }

  /** Groups where the user is an active member, by name. */
  public PageResult<Conversation> findGroupsForUser(long userId, PageRequest page) {
    String from =
        " FROM conversation_members cm JOIN conversations c"
            + " ON c.conversation_id = cm.conversation_id"
            + " WHERE cm.user_id = :me AND cm.left_at IS NULL AND c.conversation_type = 'group'";
    Map<String, Long> params = Map.of("me", userId);
    long total = jdbc.queryForObject("SELECT count(*)" + from, params, Long.class);
    List<Conversation> items =
        jdbc.query(
            "SELECT "
                + Rows.CONVERSATION_COLUMNS
                + from
                + " ORDER BY lower(c.name), c.conversation_id"
                + limitOffset(page),
            params,
            CONVERSATION);
    return SqlSupport.page(items, page, total);
  }

  /**
   * Admin group list with active member counts, optionally filtered by name. Sort fields: {@code
   * name}, {@code createdAt}.
   */
  public PageResult<GroupOverview> findGroupsForAdmin(
      String nameQuery, SortSpec sort, PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    String where = " WHERE c.conversation_type = 'group'";
    if (nameQuery != null) {
      where += " AND lower(c.name) LIKE :pattern ESCAPE '\\'";
      params.addValue("pattern", containsPattern(nameQuery));
    }
    long total =
        jdbc.queryForObject("SELECT count(*) FROM conversations c" + where, params, Long.class);
    List<GroupOverview> items =
        jdbc.query(
            "SELECT c.conversation_id, c.name, c.owner_user_id, c.encryption_enabled,"
                + " c.created_at, c.updated_at,"
                + " (SELECT count(*) FROM conversation_members cm"
                + " WHERE cm.conversation_id = c.conversation_id AND cm.left_at IS NULL)"
                + " AS member_count"
                + " FROM conversations c"
                + where
                + orderBy(sort, GROUP_SORT, "c.conversation_id")
                + limitOffset(page),
            params,
            GROUP);
    return SqlSupport.page(items, page, total);
  }
}
