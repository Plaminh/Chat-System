package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.server.handler.ActionHandler;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PingHandler implements ActionHandler {
    private final ObjectMapper mapper;

    public PingHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ActionType action() {
        return ActionType.PING;
    }

    @Override
    public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
        return new ResponseEnvelope(
                request.requestId(),
                true,
                mapper.createObjectNode().put("message", "PONG"),
                null,
                null
        );
    }
}
