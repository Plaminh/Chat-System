package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.admin.AccountStatusUpdate;
import com.chat.common.dto.event.AccountStatusEvent;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.MessageType;
import com.chat.common.protocol.ProtocolValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AccountStatusEventTest {
  private static final OffsetDateTime CHANGED_AT =
      OffsetDateTime.parse("2026-11-18T14:30:00+07:00");

  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @ParameterizedTest
  @EnumSource(AccountStatus.class)
  void roundTripsEverySupportedAccountStatus(AccountStatus status) throws Exception {
    AccountStatusEvent payload = new AccountStatusEvent(7, status, CHANGED_AT);
    EventEnvelope outbound =
        new EventEnvelope(
            "evt-account-7", MessageType.ACCOUNT_STATUS_CHANGED, mapper.valueToTree(payload));

    EventEnvelope inbound =
        ProtocolValidator.parseEvent(mapper, mapper.writeValueAsString(outbound));
    AccountStatusEvent decoded = mapper.treeToValue(inbound.data(), AccountStatusEvent.class);

    assertEquals(MessageType.ACCOUNT_STATUS_CHANGED, inbound.type());
    assertEquals(payload, decoded);
  }

  @Test
  void rejectsInvalidAccountStatusEventFields() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AccountStatusEvent(0, AccountStatus.ACTIVE, CHANGED_AT));
    assertThrows(NullPointerException.class, () -> new AccountStatusEvent(7, null, CHANGED_AT));
    assertThrows(
        NullPointerException.class, () -> new AccountStatusEvent(7, AccountStatus.LOCKED, null));
  }

  @Test
  void keepsTerminalEventStatusDistinctFromAdminStatusUpdate() {
    assertEquals(
        AccountStatus.DELETED,
        new AccountStatusEvent(7, AccountStatus.DELETED, CHANGED_AT).status());
    assertThrows(
        IllegalArgumentException.class, () -> new AccountStatusUpdate(7, AccountStatus.DELETED));
  }
}
