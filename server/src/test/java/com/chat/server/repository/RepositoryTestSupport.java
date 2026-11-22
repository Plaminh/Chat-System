package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.model.NewUser;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class for repository tests against the real PostgreSQL database configured for the server
 * (the repository {@code .env}, normally the Docker Compose database).
 *
 * <p>Each test body runs in {@link #inRollback} so it never changes stored data. When the database
 * is unreachable the tests are skipped, not failed, so {@code mvn verify} still works without
 * Docker.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = "spring.datasource.hikari.connection-timeout=3000")
@Import({
  UserRepository.class,
  UserPublicKeyRepository.class,
  LoginHistoryRepository.class,
  FriendRequestRepository.class,
  FriendshipRepository.class,
  UserBlockRepository.class,
  ConversationRepository.class,
  ConversationMemberRepository.class,
  ConversationKeyRepository.class,
  MessageRepository.class,
  SpamReportRepository.class,
  UserActivityLogRepository.class
})
abstract class RepositoryTestSupport {
  static final PageRequest FIRST_PAGE = new PageRequest(0, 100);

  /** A syntactically valid BCrypt hash; its password is irrelevant for persistence tests. */
  static final String HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWX4Z2J3dI3pZ5FQe5mVzH6Y7a1K";

  private static final AtomicInteger SEQUENCE = new AtomicInteger();
  private static Boolean databaseReachable;

  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired UserRepository users;

  @BeforeEach
  void requireDatabase() {
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
  }

  /** Runs the body in a transaction that is always rolled back. */
  void inRollback(Runnable body) {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              status.setRollbackOnly();
              body.run();
            });
  }

  /**
   * Asserts that the body fails with {@code type}. The body runs in a savepoint, because a failed
   * statement otherwise aborts the whole PostgreSQL transaction of the test.
   */
  <T extends Throwable> T assertFailsInSavepoint(Class<T> type, Runnable body) {
    TransactionTemplate savepoint = new TransactionTemplate(transactionManager);
    savepoint.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
    return assertThrows(type, () -> savepoint.executeWithoutResult(status -> body.run()));
  }

  /** Today's date in Vietnam time, the zone repositories use for date filters. */
  static LocalDate today() {
    return LocalDate.now(SqlSupport.ZONE);
  }

  /** Inserts a regular active user whose username and full name start with {@code name}. */
  long newUser(String name) {
    return newUser(name, UserRole.USER);
  }

  long newUser(String name, UserRole role) {
    // Zero-padded so usernames created in one test sort in creation order.
    String unique = String.format("%s_%05d", name, SEQUENCE.incrementAndGet());
    return users.insert(
        new NewUser(
            unique,
            HASH,
            unique + "@test.example",
            "Test " + unique,
            "1 Test Street",
            LocalDate.of(2000, 1, 1),
            Gender.OTHER,
            role));
  }

  /** Moves a timestamp column of one row by {@code minutes} relative to the transaction time. */
  void shiftTime(String table, String column, String idColumn, long id, int minutes) {
    jdbc.update(
        "UPDATE "
            + table
            + " SET "
            + column
            + " = CURRENT_TIMESTAMP + make_interval(mins => ?) WHERE "
            + idColumn
            + " = ?",
        minutes,
        id);
  }
}
