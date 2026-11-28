package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.server.exception.InvalidSortFieldException;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ActionDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ActionDispatcher.class);

    private static final Set<ActionType> PUBLIC_ACTIONS = EnumSet.of(
            ActionType.PING,
            ActionType.LOGIN,
            ActionType.REGISTER,
            ActionType.REQUEST_PASSWORD_RESET,
            ActionType.RESET_PASSWORD
    );

    /**
     * Actions whose unique-constraint violation means "this resource already exists" per
     * action-matrix.md. Any other action reports a DuplicateKeyException as CONFLICT.
     */
    private static final Set<ActionType> ALREADY_EXISTS_ACTIONS = EnumSet.of(
            ActionType.REGISTER,
            ActionType.UPDATE_PROFILE,
            ActionType.SEND_FRIEND_REQUEST,
            ActionType.BLOCK_USER,
            ActionType.ADMIN_CREATE_USER,
            ActionType.ADMIN_UPDATE_USER
    );

    private final Map<ActionType, ActionHandler> handlers = new EnumMap<>(ActionType.class);
    private final ObjectMapper mapper;

    public ActionDispatcher(ObjectMapper mapper, List<ActionHandler> handlerList) {
        this.mapper = mapper;
        if (handlerList != null) {
            for (ActionHandler handler : handlerList) {
                if (handler.action() != null) {
                    handlers.put(handler.action(), handler);
                }
            }
        }
    }

    public ResponseEnvelope dispatch(RequestEnvelope request, SessionContext session) {
        String requestId = request.requestId();
        ActionType action = request.action();

        if (action == null) {
            return error(requestId, ErrorCode.INVALID_REQUEST, "action is required");
        }

        // 1. Centralized Authorization Check
        if (!PUBLIC_ACTIONS.contains(action)) {
            if (session == null || !session.isAuthenticated()) {
                return error(requestId, ErrorCode.UNAUTHENTICATED, "Authentication required");
            }
            if (action.name().startsWith("ADMIN_") && !session.hasRole(UserRole.ADMIN)) {
                return error(requestId, ErrorCode.UNAUTHORIZED, "Admin privileges required");
            }
        }

        // 2. Handler lookup
        ActionHandler handler = handlers.get(action);
        if (handler == null) {
            return error(requestId, ErrorCode.UNSUPPORTED_OPERATION, "Operation not implemented yet");
        }

        // 3. Execution & Exception Translation Boundary
        try {
            return handler.handle(request, session);
        } catch (ProtocolValidationException exception) {
            return error(requestId, exception.errorCode(), exception.getMessage());
        } catch (InvalidSortFieldException exception) {
            return error(requestId, ErrorCode.VALIDATION_FAILED, exception.getMessage());
        } catch (DuplicateKeyException exception) {
            if (ALREADY_EXISTS_ACTIONS.contains(action)) {
                String message = action == ActionType.REGISTER
                        ? "Username or email already exists"
                        : "Resource already exists";
                return error(requestId, ErrorCode.ALREADY_EXISTS, message);
            }
            return error(requestId, ErrorCode.CONFLICT, "Request conflicts with the current state");
        } catch (IllegalArgumentException exception) {
            return error(requestId, ErrorCode.VALIDATION_FAILED, exception.getMessage() != null ? exception.getMessage() : "Invalid request arguments");
        } catch (Exception exception) {
            log.error("Unhandled exception processing action {} for requestId {}", action, requestId, exception);
            return error(requestId, ErrorCode.INTERNAL_ERROR, "An internal server error occurred");
        }
    }

    private ResponseEnvelope error(String requestId, ErrorCode code, String message) {
        return new ResponseEnvelope(requestId, false, null, code, message);
    }
}
