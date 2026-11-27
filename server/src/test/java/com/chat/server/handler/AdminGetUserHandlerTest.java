package com.chat.server.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.social.UserTarget;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.server.handler.impl.AdminGetUserHandler;
import com.chat.server.model.User;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminGetUserHandlerTest {
  private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
  private AuthHandlerTestSupport.StubUserRepository userRepository;
  private OnlineSessionRegistry sessionRegistry;
  private AdminGetUserHandler handler;

  @BeforeEach
  void setUp() {
    userRepository = AuthHandlerTestSupport.stubUserRepository();
    sessionRegistry = new OnlineSessionRegistry(mapper);
    handler = new AdminGetUserHandler(mapper, userRepository, sessionRegistry);
  }

  @Test
  void actionIsAdminGetUser() {
    assertEquals(ActionType.ADMIN_GET_USER, handler.action());
  }

  @Test
  void returnsAdminUserInfoForExistingUser() throws Exception {
    User admin =
        AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
    userRepository.addUser(admin);

    UserTarget target = new UserTarget(1L);
    RequestEnvelope request =
        new RequestEnvelope("req-getuser-1", ActionType.ADMIN_GET_USER, mapper.valueToTree(target));
    SessionContext session = new SessionContext("conn-1");
    session.authenticate(1L, "admin", UserRole.ADMIN);

    ResponseEnvelope response = handler.handle(request, session);

    assertTrue(response.success());
    assertEquals("req-getuser-1", response.requestId());
    assertNotNull(response.data());

    AdminUserInfo userInfo = mapper.treeToValue(response.data(), AdminUserInfo.class);
    assertEquals(1L, userInfo.account().summary().userId());
    assertEquals("admin", userInfo.account().summary().username());
    assertEquals(UserRole.ADMIN, userInfo.account().role());
  }

  @Test
  void returnsNotFoundWhenTargetUserDoesNotExist() throws Exception {
    UserTarget target = new UserTarget(999L);
    RequestEnvelope request =
        new RequestEnvelope("req-getuser-2", ActionType.ADMIN_GET_USER, mapper.valueToTree(target));
    SessionContext session = new SessionContext("conn-1");
    session.authenticate(1L, "admin", UserRole.ADMIN);

    ResponseEnvelope response = handler.handle(request, session);

    assertFalse(response.success());
    assertEquals(ErrorCode.NOT_FOUND, response.errorCode());
  }

  @Test
  void rejectsNonPositiveTargetUserIdAsValidationFailure() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-getuser-3",
            ActionType.ADMIN_GET_USER,
            mapper.createObjectNode().put("targetUserId", 0));
    SessionContext session = new SessionContext("conn-1");
    session.authenticate(1L, "admin", UserRole.ADMIN);

    ProtocolValidationException exception =
        assertThrows(ProtocolValidationException.class, () -> handler.handle(request, session));
    assertEquals(ErrorCode.VALIDATION_FAILED, exception.errorCode());
  }

  @Test
  void rejectsMalformedTargetUserIdAsInvalidPayload() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-getuser-4",
            ActionType.ADMIN_GET_USER,
            mapper.createObjectNode().put("targetUserId", "abc"));
    SessionContext session = new SessionContext("conn-1");
    session.authenticate(1L, "admin", UserRole.ADMIN);

    ProtocolValidationException exception =
        assertThrows(ProtocolValidationException.class, () -> handler.handle(request, session));
    assertEquals(ErrorCode.INVALID_PAYLOAD, exception.errorCode());
  }
}
