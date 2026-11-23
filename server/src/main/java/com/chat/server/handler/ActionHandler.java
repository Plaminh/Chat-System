package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.server.session.SessionContext;

public interface ActionHandler {
    ActionType action();
    ResponseEnvelope handle(RequestEnvelope request, SessionContext session) throws Exception;
}
