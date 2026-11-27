package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.server.handler.ActionHandler;
import com.chat.server.service.SessionLifecycle;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LogoutHandler implements ActionHandler {
  private final ObjectMapper mapper;
  private final SessionLifecycle sessionLifecycle;

  public LogoutHandler(ObjectMapper mapper, SessionLifecycle sessionLifecycle) {
    this.mapper = mapper;
    this.sessionLifecycle = sessionLifecycle;
  }

  @Override
  public ActionType action() {
    return ActionType.LOGOUT;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    sessionLifecycle.end(session);
    return new ResponseEnvelope(request.requestId(), true, mapper.createObjectNode(), null, null);
  }
}
