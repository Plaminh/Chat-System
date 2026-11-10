package com.chat.common.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.chat.SendMessageRequest;
import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.encryption.EncryptedMessageContent;
import com.chat.common.dto.social.FriendListRequest;
import com.chat.common.dto.social.UserTarget;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.MessageType;
import com.chat.common.exception.ProtocolValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProtocolValidatorTest {
  private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

  @Test
  void parsesValidPingRequest() throws Exception {
    RequestEnvelope request =
        ProtocolValidator.parseRequest(
            mapper, "{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":{}}");

    assertEquals("r1", request.requestId());
    assertEquals(ActionType.PING, request.action());
  }

  @ParameterizedTest
  @MethodSource("invalidRequests")
  void classifiesInvalidRequestBoundaries(ErrorCode expected, String json) {
    ProtocolValidationException exception =
        assertThrows(
            ProtocolValidationException.class, () -> ProtocolValidator.parseRequest(mapper, json));

    assertEquals(expected, exception.errorCode());
  }

  @Test
  void parsesEventsAndRejectsUnknownEventTypes() throws Exception {
    EventEnvelope event =
        ProtocolValidator.parseEvent(
            mapper, "{\"eventId\":\"e1\",\"type\":\"SERVER_EVENT\",\"data\":{}}");
    assertEquals("e1", event.eventId());
    assertEquals(MessageType.SERVER_EVENT, event.type());

    ProtocolValidationException exception =
        assertThrows(
            ProtocolValidationException.class,
            () ->
                ProtocolValidator.parseEvent(
                    mapper, "{\"eventId\":\"e1\",\"type\":\"NOPE\",\"data\":{}}"));
    assertEquals(ErrorCode.INVALID_REQUEST, exception.errorCode());
  }

  @Test
  void decodesValidPayloadAndClassifiesWrongJsonTypes() throws Exception {
    RequestEnvelope valid =
        request(ActionType.LOGIN, "{\"username\":\"alice\",\"password\":\"pw\"}");
    assertEquals(
        "alice", ProtocolValidator.decodePayload(mapper, valid, LoginRequest.class).username());

    RequestEnvelope wrongType =
        request(ActionType.LOGIN, "{\"username\":[\"alice\"],\"password\":\"pw\"}");
    assertPayloadCode(ErrorCode.INVALID_PAYLOAD, wrongType, LoginRequest.class);
  }

  @Test
  void delegatesDomainInvariantValidationToPayloadConstructors() throws Exception {
    assertPayloadCode(
        ErrorCode.VALIDATION_FAILED,
        request(ActionType.REMOVE_FRIEND, "{\"targetUserId\":0}"),
        UserTarget.class);
    assertPayloadCode(
        ErrorCode.VALIDATION_FAILED,
        request(ActionType.LIST_FRIENDS, "{\"query\":null,\"page\":{\"page\":-1,\"size\":20}}"),
        FriendListRequest.class);
    assertPayloadCode(
        ErrorCode.VALIDATION_FAILED,
        request(
            ActionType.ADMIN_GET_REGISTRATIONS, "{\"from\":\"2026-12-01\",\"to\":\"2026-11-01\"}"),
        DateRange.class);
    assertPayloadCode(
        ErrorCode.VALIDATION_FAILED,
        request(
            ActionType.SEND_MESSAGE,
            "{\"conversationId\":1,\"body\":\"plain\",\"encryptedContent\":{\"keyVersion\":1,\"ciphertext\":\"AQ==\",\"nonce\":\"AAAAAAAAAAAAAAAA\",\"authenticationTag\":\"AAAAAAAAAAAAAAAAAAAAAA==\"}}"),
        SendMessageRequest.class);
    assertPayloadCode(
        ErrorCode.VALIDATION_FAILED,
        request(
            ActionType.SEND_MESSAGE,
            "{\"keyVersion\":1,\"ciphertext\":\"AQ==\",\"nonce\":\"AA==\",\"authenticationTag\":\"AAAAAAAAAAAAAAAAAAAAAA==\"}"),
        EncryptedMessageContent.class);
  }

  @Test
  void validationMessagesDoNotExposePayloadValuesOrExceptionInternals() throws Exception {
    String secret = "private-secret-value";
    RequestEnvelope invalid =
        request(ActionType.LOGIN, "{\"username\":null,\"password\":\"" + secret + "\"}");

    ProtocolValidationException exception =
        assertThrows(
            ProtocolValidationException.class,
            () -> ProtocolValidator.decodePayload(mapper, invalid, LoginRequest.class));

    assertFalse(exception.getMessage().contains(secret));
    assertFalse(exception.getMessage().contains("com.fasterxml"));
  }

  private RequestEnvelope request(ActionType action, String data) throws Exception {
    return new RequestEnvelope("r1", action, mapper.readTree(data));
  }

  private <T> void assertPayloadCode(
      ErrorCode expected, RequestEnvelope request, Class<T> payloadType) {
    ProtocolValidationException exception =
        assertThrows(
            ProtocolValidationException.class,
            () -> ProtocolValidator.decodePayload(mapper, request, payloadType));
    assertEquals(expected, exception.errorCode());
    assertEquals("r1", exception.requestId());
  }

  private static Stream<Arguments> invalidRequests() {
    return Stream.of(
        Arguments.of(ErrorCode.INVALID_JSON, "{not-json"),
        Arguments.of(ErrorCode.INVALID_REQUEST, "null"),
        Arguments.of(ErrorCode.INVALID_REQUEST, "[]"),
        Arguments.of(ErrorCode.INVALID_REQUEST, "{}"),
        Arguments.of(
            ErrorCode.INVALID_REQUEST, "{\"requestId\":\" \",\"action\":\"PING\",\"data\":{}}"),
        Arguments.of(ErrorCode.INVALID_REQUEST, "{\"requestId\":\"r1\",\"data\":{}}"),
        Arguments.of(
            ErrorCode.UNKNOWN_ACTION, "{\"requestId\":\"r1\",\"action\":\"NOPE\",\"data\":{}}"),
        Arguments.of(ErrorCode.INVALID_PAYLOAD, "{\"requestId\":\"r1\",\"action\":\"PING\"}"),
        Arguments.of(
            ErrorCode.INVALID_PAYLOAD, "{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":null}"),
        Arguments.of(
            ErrorCode.INVALID_PAYLOAD, "{\"requestId\":\"r1\",\"action\":\"PING\",\"data\":[]}"));
  }
}
