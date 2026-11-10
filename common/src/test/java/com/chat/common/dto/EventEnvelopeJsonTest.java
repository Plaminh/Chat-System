package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chat.common.dto.event.GroupMembershipEvent;
import com.chat.common.dto.social.PresenceUpdate;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.GroupMembershipChangeType;
import com.chat.common.enums.MessageType;
import com.chat.common.enums.PresenceStatus;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class EventEnvelopeJsonTest {
  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void roundTripsPresenceEventEnvelope() throws Exception {
    PresenceUpdate presence =
        new PresenceUpdate(
            7, PresenceStatus.ONLINE, OffsetDateTime.parse("2026-11-10T09:15:00+07:00"));
    EventEnvelope envelope =
        new EventEnvelope("evt-1", MessageType.PRESENCE_CHANGED, mapper.valueToTree(presence));

    EventEnvelope decoded =
        mapper.readValue(mapper.writeValueAsString(envelope), EventEnvelope.class);
    PresenceUpdate decodedPayload = mapper.treeToValue(decoded.data(), PresenceUpdate.class);

    assertEquals(envelope.eventId(), decoded.eventId());
    assertEquals(envelope.type(), decoded.type());
    assertEquals(presence, decodedPayload);
  }

  @Test
  void serializesTypedMembershipPayload() throws Exception {
    UserSummary user =
        new UserSummary(8, "bob", "Bob Tran", AccountStatus.ACTIVE, PresenceStatus.OFFLINE);
    GroupMembershipEvent event =
        new GroupMembershipEvent(
            20,
            user,
            GroupMembershipChangeType.ADDED,
            OffsetDateTime.parse("2026-11-10T09:30:00+07:00"));
    JsonNode json = mapper.valueToTree(event);

    assertEquals(20, json.get("groupId").asLong());
    assertEquals("ADDED", json.get("change").asText());
  }
}
