package com.chat.server.config;

import com.chat.common.enums.ActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolConfigTest {
    private final ObjectMapper mapper = new ProtocolConfig().objectMapper();

    static class SampleDateObject {
        public LocalDate date;
        public OffsetDateTime timestamp;
        public ActionType action;
    }

    @Test
    void serializesDatesAsIso8601Strings() throws Exception {
        SampleDateObject obj = new SampleDateObject();
        obj.date = LocalDate.of(2026, 11, 23);
        obj.timestamp = OffsetDateTime.of(2026, 11, 23, 13, 0, 0, 0, ZoneOffset.UTC);
        obj.action = ActionType.PING;

        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("\"2026-11-23\""));
        assertTrue(json.contains("\"2026-11-23T13:00:00Z\""));
        assertTrue(json.contains("\"PING\""));
    }

    @Test
    void rejectsUnknownEnumsStrictly() {
        String json = "{\"action\":\"UNKNOWN_ACTION_TYPE\"}";
        assertThrows(InvalidFormatException.class, () -> mapper.readValue(json, SampleDateObject.class));
    }
}
