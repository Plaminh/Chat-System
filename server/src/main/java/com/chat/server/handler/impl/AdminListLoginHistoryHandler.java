package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.LoginHistoryEntry;
import com.chat.common.dto.admin.LoginHistoryRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.LoginHistoryRecord;
import com.chat.server.model.User;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Handles {@link ActionType#ADMIN_LIST_LOGIN_HISTORY} requests.
 *
 * <p>Returns paginated login history sorted chronologically. Supports both global queries
 * ({@code userId == null}) and user-filtered queries.
 */
@Component
public class AdminListLoginHistoryHandler implements ActionHandler {
    private final ObjectMapper mapper;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    public AdminListLoginHistoryHandler(
            ObjectMapper mapper,
            LoginHistoryRepository loginHistoryRepository,
            UserRepository userRepository) {
        this.mapper = mapper;
        this.loginHistoryRepository = loginHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ActionType action() {
        return ActionType.ADMIN_LIST_LOGIN_HISTORY;
    }

    @Override
    public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) throws Exception {
        String requestId = request.requestId();
        if (request.data() == null || request.data().isNull()) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.INVALID_REQUEST, "request payload is required");
        }

        LoginHistoryRequest req;
        try {
            req = mapper.treeToValue(request.data(), LoginHistoryRequest.class);
        } catch (Exception ex) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.VALIDATION_FAILED, "Invalid request payload");
        }

        if (req == null) {
            return new ResponseEnvelope(requestId, false, null, ErrorCode.INVALID_REQUEST, "request payload is required");
        }

        if (req.userId() != null) {
            Optional<User> userOpt = userRepository.findById(req.userId());
            if (userOpt.isEmpty()) {
                return new ResponseEnvelope(requestId, false, null, ErrorCode.NOT_FOUND, "User not found");
            }
        }

        PageResult<LoginHistoryRecord> recordPage =
                loginHistoryRepository.find(req.userId(), req.sort(), req.page());

        List<LoginHistoryEntry> entries = recordPage.items().stream()
                .map(r -> new LoginHistoryEntry(
                        r.loginHistoryId(),
                        r.userId(),
                        r.username(),
                        r.fullName(),
                        r.loggedInAt(),
                        r.loggedOutAt()
                ))
                .toList();

        PageResult<LoginHistoryEntry> result = new PageResult<>(
                entries,
                recordPage.page(),
                recordPage.size(),
                recordPage.totalItems(),
                recordPage.totalPages()
        );

        return new ResponseEnvelope(requestId, true, mapper.valueToTree(result), null, null);
    }
}
