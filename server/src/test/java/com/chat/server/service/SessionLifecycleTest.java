package com.chat.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.chat.common.enums.UserRole;
import com.chat.server.handler.AuthHandlerTestSupport;
import com.chat.server.repository.FriendshipRepository;
import com.chat.server.repository.UserActivityLogRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class SessionLifecycleTest {
  private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
  private final List<Long> appOpens = new CopyOnWriteArrayList<>();
  private final SessionLifecycle lifecycle =
      new SessionLifecycle(
          AuthHandlerTestSupport.stubLoginHistoryRepository(),
          new UserActivityLogRepository(new JdbcTemplate()) {
            @Override
            public void recordAppOpen(long userId) {
              appOpens.add(userId);
            }
          },
          new FriendshipRepository(new JdbcTemplate()) {
            @Override
            public List<Long> findFriendIds(long userId) {
              return List.of();
            }
          },
          new OnlineSessionRegistry(mapper),
          mapper);

  @Test
  void recordsAppOpenWhenAUserSignsIn() {
    SessionContext session = signIn(7L, UserRole.USER);

    assertEquals(List.of(7L), appOpens);
    assertNotNull(session.getLoginHistoryId());
  }

  @Test
  void recordsLoginHistoryButNoAppOpenWhenAnAdminSignsIn() {
    SessionContext session = signIn(1L, UserRole.ADMIN);

    assertEquals(List.of(), appOpens);
    assertNotNull(session.getLoginHistoryId());
  }

  private SessionContext signIn(long userId, UserRole role) {
    SessionContext session = new SessionContext("conn-" + userId);
    session.authenticate(userId, "user" + userId, role);
    lifecycle.begin(session, userId);
    return session;
  }
}
