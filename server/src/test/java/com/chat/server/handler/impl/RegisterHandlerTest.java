package com.chat.server.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * U1 REGISTER through {@link ActionDispatcher}, so error translation is covered too, against the
 * configured PostgreSQL database. Each test runs in a rolled-back transaction and is skipped, not
 * failed, when the database is unreachable.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = "spring.datasource.hikari.connection-timeout=3000")
@Import({ProtocolConfig.class, UserRepository.class, RegisterHandler.class})
class RegisterHandlerTest {
  private static final String PASSWORD = "Strong-Passw0rd";
  private static final AtomicInteger SEQUENCE = new AtomicInteger();
  private static Boolean databaseReachable;

  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired ObjectMapper mapper;
  @Autowired UserRepository users;
  @Autowired RegisterHandler registerHandler;

  private ActionDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    if (databaseReachable == null) {
      try {
        jdbc.queryForObject("SELECT 1", Integer.class);
        databaseReachable = true;
      } catch (DataAccessException e) {
        databaseReachable = false;
      }
    }
    assumeTrue(
        databaseReachable,
        "PostgreSQL is not reachable; start it with 'docker compose up -d' to run this test");
    dispatcher = new ActionDispatcher(mapper, List.of(registerHandler));
  }

  @Test
  void registersActiveUserWithBcryptHash() {
    inRollback(
        () -> {
          String username = uniqueUsername("u1New");

          ResponseEnvelope response = register(validRequest(username));

          assertTrue(response.success());
          assertEquals(username, response.data().path("user").path("username").asText());
          User stored = users.findByUsername(username).orElseThrow();
          assertEquals(UserRole.USER, stored.role());
          assertEquals(AccountStatus.ACTIVE, stored.accountStatus());
          assertEquals(Gender.FEMALE, stored.gender());
          assertNotEquals(PASSWORD, stored.passwordHash());
          assertTrue(BCrypt.checkpw(PASSWORD, stored.passwordHash()), "LOGIN must accept it");
        });
  }

  @Test
  void rejectsDuplicateUsernameOrEmailIgnoringCase() {
    inRollback(
        () -> {
          String username = uniqueUsername("u1Dup");
          assertTrue(register(validRequest(username)).success());

          ObjectNode sameUsername = validRequest(username.toUpperCase());
          sameUsername.put("email", "other_" + username + "@register.test");
          assertError(ErrorCode.ALREADY_EXISTS, register(sameUsername));

          ObjectNode sameEmail = validRequest(uniqueUsername("u1Mail"));
          sameEmail.put("email", (username + "@register.test").toUpperCase());
          assertError(ErrorCode.ALREADY_EXISTS, register(sameEmail));

          assertEquals(
              1,
              jdbc.queryForObject(
                  "SELECT count(*) FROM users WHERE lower(email) = lower(?)",
                  Integer.class,
                  username + "@register.test"));
        });
  }

  @Test
  void rejectsBlankOrMissingRequiredFields() {
    inRollback(
        () -> {
          for (String field : List.of("username", "password", "email", "fullName", "address")) {
            String username = uniqueUsername("u1Blank");
            ObjectNode request = validRequest(username);
            request.put(field, "   ");
            assertError(ErrorCode.VALIDATION_FAILED, register(request));

            ObjectNode missing = validRequest(username);
            missing.remove(field);
            assertError(ErrorCode.VALIDATION_FAILED, register(missing));
            assertFalse(users.existsByUsername(username), field);
          }
          ObjectNode noBirthDate = validRequest(uniqueUsername("u1NoDob"));
          noBirthDate.remove("dateOfBirth");
          assertError(ErrorCode.VALIDATION_FAILED, register(noBirthDate));
        });
  }

  @Test
  void rejectsInvalidValues() {
    inRollback(
        () -> {
          assertInvalid("username", "a b");
          assertInvalid("username", "ab");
          assertInvalid("password", "short");
          assertInvalid("email", "not-an-email");
          assertInvalid("fullName", "A");
          assertInvalid("dateOfBirth", "2999-01-01");
        });
  }

  private void assertInvalid(String field, String value) {
    String username = uniqueUsername("u1Bad");
    ObjectNode request = validRequest(username);
    request.put(field, value);
    assertError(ErrorCode.VALIDATION_FAILED, register(request));
    assertFalse(users.existsByUsername(username), field + "=" + value);
  }

  private ResponseEnvelope register(ObjectNode data) {
    return dispatcher.dispatch(
        new RequestEnvelope("register-" + SEQUENCE.incrementAndGet(), ActionType.REGISTER, data),
        new SessionContext("register-test"));
  }

  private ObjectNode validRequest(String username) {
    return mapper
        .createObjectNode()
        .put("username", username)
        .put("password", PASSWORD)
        .put("email", username + "@register.test")
        .put("fullName", "Register " + username)
        .put("address", "1 Test Street")
        .put("dateOfBirth", "2000-01-01")
        .put("gender", "FEMALE");
  }

  /** Letters, digits and underscores only, at most 30 characters, as REGISTER requires. */
  private static String uniqueUsername(String prefix) {
    return String.format(
        "%s_%d_%d", prefix, ProcessHandle.current().pid() % 10000, SEQUENCE.incrementAndGet());
  }

  private void inRollback(Runnable body) {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              status.setRollbackOnly();
              body.run();
            });
  }

  private static void assertError(ErrorCode expected, ResponseEnvelope response) {
    assertFalse(response.success());
    assertEquals(expected, response.errorCode(), response.message());
  }
}
