package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestHandler {
    private final ObjectMapper mapper;
    private final ActionDispatcher dispatcher;

    public RequestHandler(ObjectMapper mapper) {
        this(mapper, new ActionDispatcher(mapper, java.util.List.of(new com.chat.server.handler.impl.PingHandler(mapper))));
    }

    public RequestHandler(ObjectMapper mapper, ActionDispatcher dispatcher) {
        this.mapper = mapper;
        this.dispatcher = dispatcher;
    }

    public String handle(String jsonLine) {
        return handle(jsonLine, new SessionContext("anonymous-" + UUID.randomUUID()));
    }

    public String handle(String jsonLine, SessionContext session) {
        try {
            RequestEnvelope request = ProtocolValidator.parseRequest(mapper, jsonLine);
            ResponseEnvelope response = dispatcher.dispatch(request, session);
            return write(response);
        } catch (ProtocolValidationException exception) {
            return write(new ResponseEnvelope(
                    exception.requestId(),
                    false,
                    null,
                    exception.errorCode(),
                    exception.getMessage()
            ));
        } catch (Exception exception) {
            return write(new ResponseEnvelope(
                    null,
                    false,
                    null,
                    ErrorCode.INVALID_JSON,
                    "Malformed JSON protocol message"
            ));
        }
    }

    public String messageTooLarge() {
        return write(new ResponseEnvelope(null, false, null, ErrorCode.MESSAGE_TOO_LARGE, "Protocol message is too large"));
    }

    private String write(ResponseEnvelope response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize protocol response", exception);
        }
    }
}
