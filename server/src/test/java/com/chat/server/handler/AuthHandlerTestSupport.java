package com.chat.server.handler;

import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.model.User;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test fixture helper providing Java 25-compatible repository stubs.
 */
public final class AuthHandlerTestSupport {
    public static final ObjectMapper MAPPER = new ProtocolConfig().objectMapper();
    public static final String DEFAULT_RAW_PASSWORD = "Password123!";
    public static final String DEFAULT_PASSWORD_HASH = BCrypt.hashpw(DEFAULT_RAW_PASSWORD, BCrypt.gensalt(4));

    private AuthHandlerTestSupport() {}

    public static User createTestUser(long userId, String username, UserRole role, AccountStatus status) {
        return new User(
                userId,
                username,
                DEFAULT_PASSWORD_HASH,
                username + "@campus.edu",
                "Full Name " + username,
                "Campus Office 101",
                LocalDate.of(1995, 5, 20),
                Gender.MALE,
                role,
                status,
                status == AccountStatus.LOCKED ? OffsetDateTime.now() : null,
                status == AccountStatus.DELETED ? OffsetDateTime.now() : null,
                null,
                OffsetDateTime.now().minusDays(30),
                OffsetDateTime.now().minusDays(1)
        );
    }

    public static StubUserRepository stubUserRepository() {
        return new StubUserRepository();
    }

    public static StubLoginHistoryRepository stubLoginHistoryRepository() {
        return new StubLoginHistoryRepository();
    }

    public static class StubUserRepository extends UserRepository {
        private final Map<Long, User> usersById = new ConcurrentHashMap<>();
        private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();

        public StubUserRepository() {
            super(new org.springframework.jdbc.core.JdbcTemplate());
        }

        public void addUser(User user) {
            usersById.put(user.userId(), user);
            usersByUsername.put(user.username().toLowerCase(), user);
        }

        @Override
        public Optional<User> findById(long userId) {
            return Optional.ofNullable(usersById.get(userId));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            if (username == null) return Optional.empty();
            return Optional.ofNullable(usersByUsername.get(username.toLowerCase()));
        }
    }

    public static class StubLoginHistoryRepository extends LoginHistoryRepository {
        private final AtomicLong counter = new AtomicLong(100);
        private final Map<Long, Long> logins = new ConcurrentHashMap<>();
        private final Map<Long, Boolean> logouts = new ConcurrentHashMap<>();

        public StubLoginHistoryRepository() {
            super(new org.springframework.jdbc.core.JdbcTemplate());
        }

        @Override
        public long recordLogin(long userId) {
            long id = counter.incrementAndGet();
            logins.put(id, userId);
            return id;
        }

        @Override
        public boolean recordLogout(long loginHistoryId) {
            logouts.put(loginHistoryId, true);
            return true;
        }

        public boolean isLoggedOut(long loginHistoryId) {
            return Boolean.TRUE.equals(logouts.get(loginHistoryId));
        }

        public Long getLoginUserId(long loginHistoryId) {
            return logins.get(loginHistoryId);
        }
    }
}
