package com.chat.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ProtocolEnumJsonTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void preservesExistingEnumNames() throws Exception {
    assertEquals("\"PING\"", mapper.writeValueAsString(ActionType.PING));
    assertEquals("\"INVALID_JSON\"", mapper.writeValueAsString(ErrorCode.INVALID_JSON));
    assertEquals("\"SERVER_EVENT\"", mapper.writeValueAsString(MessageType.SERVER_EVENT));
  }

  @Test
  void serializesRepresentativeV2Names() throws Exception {
    assertEquals("\"SEND_MESSAGE\"", mapper.writeValueAsString(ActionType.SEND_MESSAGE));
    assertEquals("\"UNAUTHORIZED\"", mapper.writeValueAsString(ErrorCode.UNAUTHORIZED));
    assertEquals(
        "\"GROUP_MEMBERSHIP_CHANGED\"",
        mapper.writeValueAsString(MessageType.GROUP_MEMBERSHIP_CHANGED));
    assertEquals("\"LOCKED\"", mapper.writeValueAsString(AccountStatus.LOCKED));
  }

  @Test
  void rejectsUnknownEnumNames() {
    assertThrows(
        JsonMappingException.class,
        () -> mapper.readValue("\"NOT_A_REAL_ACTION\"", ActionType.class));
    assertThrows(
        JsonMappingException.class,
        () -> mapper.readValue("\"NOT_A_REAL_ERROR\"", ErrorCode.class));
    assertThrows(
        JsonMappingException.class,
        () -> mapper.readValue("\"NOT_A_REAL_EVENT\"", MessageType.class));
  }
}
