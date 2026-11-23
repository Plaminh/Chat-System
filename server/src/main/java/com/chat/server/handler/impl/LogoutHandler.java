package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.server.handler.ActionHandler;
import com.chat.server.repository.LoginHistoryRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import org.springframework.stereotype.Component;

@Component
public class LogoutHandler implements ActionHandler {
    private final LoginHistoryRepository loginHistoryRepository;
    private final OnlineSessionRegistry sessionRegistry;

    public LogoutHandler(LoginHistoryRepository loginHistoryRepository, OnlineSessionRegistry sessionRegistry) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public ActionType action() {
        return ActionType.LOGOUT;
    }

    @Override
    public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
        String requestId = request.requestId();

        if (session != null) {
            Long historyId = session.getLoginHistoryId();
            if (historyId != null && loginHistoryRepository != null) {
                loginHistoryRepository.recordLogout(historyId);
            }
            session.clear();
        }

        return new ResponseEnvelope(requestId, true, null, null, null);
    }
}
