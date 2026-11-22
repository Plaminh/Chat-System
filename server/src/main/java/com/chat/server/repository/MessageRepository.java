package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.rangeEndExclusive;
import static com.chat.server.repository.SqlSupport.rangeStart;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.server.model.EncryptedPayload;
import com.chat.server.model.Message;
import com.chat.server.model.MessageView;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code messages} and {@code message_deletions}.
 *
 * <p>A message is visible to a viewer when the viewer is (or was) a member of its conversation, it
 * was sent inside the viewer's membership interval and after the viewer's cleared history, and the
 * viewer has not deleted it for themselves. Unsent messages stay visible with the placeholder body.
 */
@Repository
public class MessageRepository {
  private static final String VISIBLE_FROM =
      " FROM messages m"
          + " JOIN conversation_members cm"
          + " ON cm.conversation_id = m.conversation_id AND cm.user_id = :viewer"
          + " LEFT JOIN users s ON s.user_id = m.sender_user_id"
          + " WHERE m.created_at >= cm.joined_at"
          + " AND (cm.left_at IS NULL OR m.created_at <= cm.left_at)"
          + " AND (cm.history_cleared_at IS NULL OR m.created_at > cm.history_cleared_at)"
          + " AND NOT EXISTS (SELECT 1 FROM message_deletions md"
          + " WHERE md.message_id = m.message_id AND md.user_id = :viewer)";

  private static final String VIEW_COLUMNS =
      Rows.MESSAGE_COLUMNS + ", " + Rows.userBriefColumns("s", "s_");

  private static final RowMapper<Message> MESSAGE = (rs, n) -> Rows.message(rs);

  private static final RowMapper<MessageView> VIEW =
      (rs, n) -> new MessageView(Rows.message(rs), Rows.userBrief(rs, "s_"));

  private final NamedParameterJdbcTemplate jdbc;

  public MessageRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Stores a plaintext message and returns its ID. */
  public long insertPlain(long conversationId, long senderUserId, String body) {
    return jdbc.queryForObject(
        "INSERT INTO messages (conversation_id, sender_user_id, body)"
            + " VALUES (:conversation, :sender, :body) RETURNING message_id",
        Map.of("conversation", conversationId, "sender", senderUserId, "body", body),
        Long.class);
  }

  /** Stores client-encrypted content for an encrypted group and returns its ID. */
  public long insertEncrypted(long conversationId, long senderUserId, EncryptedPayload payload) {
    return jdbc.queryForObject(
        "INSERT INTO messages (conversation_id, sender_user_id, content_encrypted, ciphertext,"
            + " encryption_key_version, encryption_nonce, authentication_tag)"
            + " VALUES (:conversation, :sender, TRUE, :ciphertext, :version, :nonce, :tag)"
            + " RETURNING message_id",
        new MapSqlParameterSource()
            .addValue("conversation", conversationId)
            .addValue("sender", senderUserId)
            .addValue("ciphertext", payload.ciphertext())
            .addValue("version", payload.keyVersion())
            .addValue("nonce", payload.nonce())
            .addValue("tag", payload.authenticationTag()),
        Long.class);
  }

  public Optional<Message> findById(long messageId) {
    return jdbc
        .query(
            "SELECT " + Rows.MESSAGE_COLUMNS + " FROM messages m WHERE m.message_id = :id",
            Map.of("id", messageId),
            MESSAGE)
        .stream()
        .findFirst();
  }

  /** The message with its sender, only when it is visible to {@code viewerId}. */
  public Optional<MessageView> findVisible(long messageId, long viewerId) {
    return jdbc
        .query(
            "SELECT " + VIEW_COLUMNS + VISIBLE_FROM + " AND m.message_id = :id",
            Map.of("viewer", viewerId, "id", messageId),
            VIEW)
        .stream()
        .findFirst();
  }

  /**
   * Visible history of one conversation, newest first: page 0 holds the latest messages. Reverse a
   * page to display it in chronological order.
   */
  public PageResult<MessageView> findHistory(long conversationId, long viewerId, PageRequest page) {
    Map<String, Long> params = Map.of("viewer", viewerId, "conversation", conversationId);
    String condition = " AND m.conversation_id = :conversation";
    long total =
        jdbc.queryForObject("SELECT count(*)" + VISIBLE_FROM + condition, params, Long.class);
    List<MessageView> items =
        jdbc.query(
            "SELECT "
                + VIEW_COLUMNS
                + VISIBLE_FROM
                + condition
                + " ORDER BY m.created_at DESC, m.message_id DESC"
                + limitOffset(page),
            params,
            VIEW);
    return SqlSupport.page(items, page, total);
  }

  /** The newest message of the conversation visible to the viewer. */
  public Optional<MessageView> findLatestVisible(long conversationId, long viewerId) {
    return jdbc
        .query(
            "SELECT "
                + VIEW_COLUMNS
                + VISIBLE_FROM
                + " AND m.conversation_id = :conversation"
                + " ORDER BY m.created_at DESC, m.message_id DESC LIMIT 1",
            Map.of("viewer", viewerId, "conversation", conversationId),
            VIEW)
        .stream()
        .findFirst();
  }

  /**
   * Literal, case-insensitive search over visible plaintext messages, newest first. {@code
   * conversationId} null searches every conversation of the viewer. Encrypted and unsent messages
   * are never matched; encrypted groups are searched on the client.
   */
  public PageResult<MessageView> search(
      long viewerId, Long conversationId, String text, DateRange dateRange, PageRequest page) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("viewer", viewerId)
            .addValue("pattern", containsPattern(text));
    String condition =
        " AND NOT m.content_encrypted AND m.unsent_at IS NULL"
            + " AND lower(m.body) LIKE :pattern ESCAPE '\\'";
    if (conversationId != null) {
      condition += " AND m.conversation_id = :conversation";
      params.addValue("conversation", conversationId);
    }
    if (dateRange != null) {
      condition += " AND m.created_at >= :from AND m.created_at < :to";
      params.addValue("from", rangeStart(dateRange));
      params.addValue("to", rangeEndExclusive(dateRange));
    }
    long total =
        jdbc.queryForObject("SELECT count(*)" + VISIBLE_FROM + condition, params, Long.class);
    List<MessageView> items =
        jdbc.query(
            "SELECT "
                + VIEW_COLUMNS
                + VISIBLE_FROM
                + condition
                + " ORDER BY m.created_at DESC, m.message_id DESC"
                + limitOffset(page),
            params,
            VIEW);
    return SqlSupport.page(items, page, total);
  }

  /**
   * Replaces the content with the unsent placeholder for everyone. Returns false when the message
   * is missing or already unsent.
   */
  public boolean unsend(long messageId, long byUserId) {
    return jdbc.update(
            // content_encrypted is kept: it must keep matching the conversation's mode (FK).
            "UPDATE messages SET body = :placeholder, ciphertext = NULL,"
                + " encryption_key_version = NULL, encryption_nonce = NULL,"
                + " authentication_tag = NULL, unsent_at = CURRENT_TIMESTAMP, unsent_by = :by"
                + " WHERE message_id = :id AND unsent_at IS NULL",
            Map.of("id", messageId, "by", byUserId, "placeholder", Message.UNSENT_BODY))
        == 1;
  }

  /**
   * Hides the messages from {@code userId} only. Already hidden messages are skipped; returns the
   * number newly hidden.
   */
  public int deleteForUser(long userId, List<Long> messageIds) {
    if (messageIds.isEmpty()) {
      return 0;
    }
    return jdbc.update(
        "INSERT INTO message_deletions (message_id, user_id)"
            + " SELECT id, :user FROM unnest(CAST(:ids AS BIGINT[])) AS id"
            + " ON CONFLICT ON CONSTRAINT uq_message_deletions_message_user DO NOTHING",
        new MapSqlParameterSource()
            .addValue("user", userId)
            .addValue("ids", messageIds.toArray(Long[]::new)));
  }

  /**
   * True when {@code senderId} has sent at least one message that {@code receiverId} could see,
   * i.e. the sender chatted with the receiver directly or in a shared group.
   */
  public boolean hasReceivedMessageFrom(long receiverId, long senderId) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM messages m JOIN conversation_members cm"
                + " ON cm.conversation_id = m.conversation_id AND cm.user_id = :receiver"
                + " WHERE m.sender_user_id = :sender AND m.created_at >= cm.joined_at"
                + " AND (cm.left_at IS NULL OR m.created_at <= cm.left_at))",
            Map.of("receiver", receiverId, "sender", senderId),
            Boolean.class));
  }
}
