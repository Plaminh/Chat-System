package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.AuthenticationResult;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.PresenceStatus;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class LoginHandler implements ActionHandler {
    private final ObjectMapper mapper;
    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHandler(ObjectMapper mapper, UserRepository userRepository, LoginHistoryRepository loginHistoryRepository) {
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @Override
    public ActionType action() {
        return ActionType.LOGIN;
    }

    @Override
    public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) throws Exception {
        String requestId = request.requestId();
        LoginRequest loginRequest = mapper.treeToValue(request.data(), LoginRequest.class);
        if (loginRequest == null || loginRequest.username() == null || loginRequest.password() == null) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.INVALID_REQUEST, "Invalid username or password request");
        }

        Optional<User> userOpt = userRepository.findByUsername(loginRequest.username());
        if (userOpt.isEmpty()) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
        }

        User user = userOpt.get();
        if (user.accountStatus() == AccountStatus.LOCKED) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.ACCOUNT_LOCKED, "Account is locked by an administrator");
        }
        if (user.accountStatus() == AccountStatus.DELETED) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.ACCOUNT_DELETED, "Account has been deactivated");
        }

        boolean passwordMatch;
        try {
            passwordMatch = BCrypt.checkpw(loginRequest.password(), user.passwordHash());
        } catch (Exception ex) {
            passwordMatch = false;
        }

        if (!passwordMatch) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
        }

        long historyId = loginHistoryRepository.recordLogin(user.userId());
        if (session != null) {
            session.authenticate(user.userId(), user.username(), user.role(), historyId);
        }

        UserSummary summary = new UserSummary(
                user.userId(),
                user.username(),
                user.fullName(),
                user.accountStatus(),
                PresenceStatus.ONLINE
        );
        AuthenticationResult authResult = new AuthenticationResult(summary, OffsetDateTime.now());

        return new ResponseEnvelope(requestId, true, mapper.valueToTree(authResult), null, null);
    }
}
