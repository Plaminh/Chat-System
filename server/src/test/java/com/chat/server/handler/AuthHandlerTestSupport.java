package com.chat.server.handler;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.SortDirection;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.exception.InvalidSortFieldException;
import com.chat.server.model.LoginHistoryRecord;
import com.chat.server.model.User;
import com.chat.server.repository.FriendshipRepository;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.repository.UserActivityLogRepository;
import com.chat.server.repository.UserRepository;
import com.chat.server.service.SessionLifecycle;
import com.chat.server.session.OnlineSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test fixture helper providing Java 25-compatible repository and service stubs.
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

    public static SessionLifecycle stubSessionLifecycle(
            LoginHistoryRepository loginHistory,
            OnlineSessionRegistry registry,
            ObjectMapper mapper) {
        UserActivityLogRepository activityLogs = new UserActivityLogRepository(new JdbcTemplate()) {
            @Override
            public void recordAppOpen(long userId) {
                // stub: no-op for tests
            }
        };
        FriendshipRepository friendships = new FriendshipRepository(new JdbcTemplate()) {
            @Override
            public List<Long> findFriendIds(long userId) {
                return List.of();
            }
        };
        return new SessionLifecycle(loginHistory, activityLogs, friendships, registry, mapper);
    }

    public static class StubUserRepository extends UserRepository {
        private final Map<Long, User> usersById = new ConcurrentHashMap<>();
        private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();

        public StubUserRepository() {
            super(new JdbcTemplate());
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

        @Override
        public Optional<User> findByEmail(String email) {
            if (email == null) return Optional.empty();
            return usersById.values().stream()
                    .filter(u -> email.equalsIgnoreCase(u.email()))
                    .findFirst();
        }

        @Override
        public PageResult<User> findForAdmin(
                String query,
                AccountStatus status,
                com.chat.common.dto.common.DateRange createdAtRange,
                SortSpec sort,
                PageRequest page) {
            if (sort != null && !"name".equals(sort.field()) && !"createdAt".equals(sort.field())) {
                throw new InvalidSortFieldException(sort.field(), List.of("name", "createdAt"));
            }
            List<User> filtered = usersById.values().stream()
                    .filter(u -> {
                        if (query != null && !query.isBlank()) {
                            String q = query.trim().toLowerCase();
                            boolean matchName = u.fullName() != null && u.fullName().toLowerCase().contains(q);
                            boolean matchUser = u.username() != null && u.username().toLowerCase().contains(q);
                            if (!matchName && !matchUser) return false;
                        }
                        if (status != null && u.accountStatus() != status) {
                            return false;
                        }
                        if (createdAtRange != null) {
                            if (createdAtRange.from() != null && u.createdAt().toLocalDate().isBefore(createdAtRange.from())) {
                                return false;
                            }
                            if (createdAtRange.to() != null && u.createdAt().toLocalDate().isAfter(createdAtRange.to())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .sorted((a, b) -> {
                        if (sort == null) return 0;
                        int cmp;
                        if ("name".equals(sort.field())) {
                            cmp = a.fullName().compareToIgnoreCase(b.fullName());
                        } else {
                            cmp = a.createdAt().compareTo(b.createdAt());
                        }
                        return sort.direction() == SortDirection.DESC ? -cmp : cmp;
                    })
                    .toList();

            int fromIndex = Math.min(page.page() * page.size(), filtered.size());
            int toIndex = Math.min(fromIndex + page.size(), filtered.size());
            List<User> paged = filtered.subList(fromIndex, toIndex);
            int totalPages = (int) ((filtered.size() + page.size() - 1) / page.size());
            return new PageResult<>(paged, page.page(), page.size(), filtered.size(), totalPages);
        }
    }

    public static class StubLoginHistoryRepository extends LoginHistoryRepository {
        private final AtomicLong counter = new AtomicLong(100);
        private final Map<Long, Long> logins = new ConcurrentHashMap<>();
        private final Map<Long, Boolean> logouts = new ConcurrentHashMap<>();
        private final List<LoginHistoryRecord> historyRecords = new CopyOnWriteArrayList<>();

        public StubLoginHistoryRepository() {
            super(new JdbcTemplate());
        }

        public void addRecord(LoginHistoryRecord record) {
            historyRecords.add(record);
        }

        @Override
        public PageResult<LoginHistoryRecord> find(Long userId, SortSpec sort, PageRequest page) {
            if (!"loggedInAt".equals(sort.field())) {
                throw new InvalidSortFieldException(sort.field(), List.of("loggedInAt"));
            }
            List<LoginHistoryRecord> filtered = historyRecords.stream()
                    .filter(r -> userId == null || r.userId() == userId)
                    .sorted((a, b) -> {
                        int cmp = a.loggedInAt().compareTo(b.loggedInAt());
                        return sort.direction() == SortDirection.DESC ? -cmp : cmp;
                    })
                    .toList();
            int fromIndex = Math.min(page.page() * page.size(), filtered.size());
            int toIndex = Math.min(fromIndex + page.size(), filtered.size());
            List<LoginHistoryRecord> paged = filtered.subList(fromIndex, toIndex);
            int totalPages = (int) ((filtered.size() + page.size() - 1) / page.size());
            return new PageResult<>(paged, page.page(), page.size(), filtered.size(), totalPages);
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
