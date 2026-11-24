package com.chat.server.service;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.social.PresenceUpdate;
import com.chat.common.enums.MessageType;
import com.chat.common.enums.PresenceStatus;
import com.chat.server.repository.FriendshipRepository;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.repository.UserActivityLogRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SessionLifecycle {
  private final LoginHistoryRepository loginHistory;
  private final UserActivityLogRepository activityLogs;
  private final FriendshipRepository friendships;
  private final OnlineSessionRegistry registry;
  private final ObjectMapper mapper;

  public SessionLifecycle(
      LoginHistoryRepository loginHistory,
      UserActivityLogRepository activityLogs,
      FriendshipRepository friendships,
      OnlineSessionRegistry registry,
      ObjectMapper mapper) {
    this.loginHistory = loginHistory;
    this.activityLogs = activityLogs;
    this.friendships = friendships;
    this.registry = registry;
    this.mapper = mapper;
  }

  public void begin(SessionContext session, long databaseUserId) {
    long loginHistoryId = loginHistory.recordLogin(databaseUserId);
    activityLogs.recordAppOpen(databaseUserId);
    session.setDatabaseSession(databaseUserId, loginHistoryId);
    registry.bindSession(session);
    broadcastPresence(databaseUserId, PresenceStatus.ONLINE);
  }

  public void end(SessionContext session) {
    Long loginHistoryId = session.getLoginHistoryId();
    if (loginHistoryId != null) {
      loginHistory.recordLogout(loginHistoryId);
    }

    Long databaseUserId = session.getDatabaseUserId();
    registry.unbindSession(session);
    session.clearDatabaseSession();
    session.clear();

    if (databaseUserId != null
        && !registry.isUserOnline(userUuid(databaseUserId))) {
      broadcastPresence(databaseUserId, PresenceStatus.OFFLINE);
    }
  }

  private void broadcastPresence(long databaseUserId, PresenceStatus status) {
    List<Long> friendIds = friendships.findFriendIds(databaseUserId);
    PresenceUpdate payload =
        new PresenceUpdate(databaseUserId, status, OffsetDateTime.now());
    EventEnvelope event =
        new EventEnvelope(UUID.randomUUID().toString(), MessageType.PRESENCE_CHANGED,
            mapper.valueToTree(payload));
    for (Long friendId : friendIds) {
      registry.sendToUser(userUuid(friendId), event);
    }
  }

  public static UUID userUuid(long databaseUserId) {
    return UUID.nameUUIDFromBytes(
        ("chat-user-" + databaseUserId).getBytes(StandardCharsets.UTF_8));
  }
}
