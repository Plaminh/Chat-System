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
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Test fixture helper for authentication and administrative user handler tests. */
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

    public static UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    public static LoginHistoryRepository mockLoginHistoryRepository() {
        return Mockito.mock(LoginHistoryRepository.class);
    }
}
