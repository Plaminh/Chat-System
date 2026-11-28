package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.impl.GetProfileHandler;
import com.chat.server.handler.impl.UpdateProfileHandler;
import com.chat.server.model.User;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class UserProfileHandlerIntegrationTest extends RepositoryTestSupport {
  private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
  private final OnlineSessionRegistry sessions = new OnlineSessionRegistry(mapper);

  @Test
  void updateIsReadBackFromPostgresAfterTheProfileIsFetchedAgain() {
    inRollback(
        () -> {
          long userId = newUser("uProfilePersist");
          SessionContext session = authenticatedSession(userId);
          GetProfileHandler getProfile = new GetProfileHandler(mapper, users, sessions);
          UpdateProfileHandler updateProfile = new UpdateProfileHandler(mapper, users, sessions);

          ResponseEnvelope before =
              handle(
                  getProfile, request(ActionType.GET_PROFILE, mapper.createObjectNode()), session);
          assertTrue(before.success());
          assertEquals("1 Test Street", before.data().path("address").asText());

          UpdateProfileRequest payload =
              new UpdateProfileRequest(
                  "Updated Profile Name",
                  users.findById(userId).orElseThrow().email().toUpperCase(Locale.ROOT),
                  "Updated profile address",
                  LocalDate.of(1999, 2, 3),
                  Gender.FEMALE);
          ResponseEnvelope updateResponse =
              handle(updateProfile, request(ActionType.UPDATE_PROFILE, payload), session);

          assertTrue(updateResponse.success());
          UserAccountDetail updated = readDetail(updateResponse);
          assertEquals("Updated Profile Name", updated.summary().fullName());
          assertEquals(payload.email(), updated.email());
          assertEquals("Updated profile address", updated.address());
          assertEquals(Gender.FEMALE, updated.gender());

          ResponseEnvelope after =
              handle(
                  getProfile,
                  request(ActionType.GET_PROFILE, mapper.createObjectNode()),
                  authenticatedSession(userId));
          UserAccountDetail persisted = readDetail(after);
          User stored = users.findById(userId).orElseThrow();
          assertEquals("Updated Profile Name", persisted.summary().fullName());
          assertEquals("Updated profile address", persisted.address());
          assertEquals(LocalDate.of(1999, 2, 3), stored.dateOfBirth());
          assertEquals(Gender.FEMALE, stored.gender());
          assertEquals(AccountStatus.ACTIVE, persisted.summary().accountStatus());
        });
  }

  @Test
  void rejectsEmailOwnedByAnotherAccountAndKeepsTheOriginalProfile() {
    inRollback(
        () -> {
          long userId = newUser("uProfileOwner");
          long otherUserId = newUser("uProfileOther");
          User original = users.findById(userId).orElseThrow();
          String otherEmail =
              users.findById(otherUserId).orElseThrow().email().toUpperCase(Locale.ROOT);
          UpdateProfileHandler updateProfile = new UpdateProfileHandler(mapper, users, sessions);
          UpdateProfileRequest payload =
              new UpdateProfileRequest(
                  "Must Not Persist",
                  otherEmail,
                  "Must not persist",
                  LocalDate.of(1999, 2, 3),
                  Gender.OTHER);

          ResponseEnvelope response =
              handle(
                  updateProfile,
                  request(ActionType.UPDATE_PROFILE, payload),
                  authenticatedSession(userId));

          assertFalse(response.success());
          assertEquals(ErrorCode.ALREADY_EXISTS, response.errorCode());
          User unchanged = users.findById(userId).orElseThrow();
          assertEquals(original.email(), unchanged.email());
          assertEquals(original.fullName(), unchanged.fullName());
        });
  }

  private RequestEnvelope request(ActionType action, Object payload) {
    return new RequestEnvelope("profile-integration", action, mapper.valueToTree(payload));
  }

  private SessionContext authenticatedSession(long userId) {
    SessionContext session = new SessionContext("profile-" + userId);
    session.authenticate(userId, "integration-user", UserRole.USER);
    return session;
  }

  private ResponseEnvelope handle(
      com.chat.server.handler.ActionHandler handler,
      RequestEnvelope request,
      SessionContext session) {
    try {
      return handler.handle(request, session);
    } catch (Exception exception) {
      throw new AssertionError("Profile handler unexpectedly failed", exception);
    }
  }

  private UserAccountDetail readDetail(ResponseEnvelope response) {
    try {
      return mapper.treeToValue(response.data(), UserAccountDetail.class);
    } catch (JsonProcessingException exception) {
      throw new AssertionError("Profile response was not a UserAccountDetail", exception);
    }
  }
}
