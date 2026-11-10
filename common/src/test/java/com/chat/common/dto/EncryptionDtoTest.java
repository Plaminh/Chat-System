package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.encryption.EncryptedMessageContent;
import com.chat.common.dto.encryption.EncryptionAlgorithms;
import com.chat.common.dto.encryption.GroupKeyRotationRequest;
import com.chat.common.dto.encryption.RecipientKeyEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class EncryptionDtoTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void roundTripsEncryptedMessageMetadata() throws Exception {
    EncryptedMessageContent content =
        new EncryptedMessageContent(3, new byte[] {1, 2, 3}, new byte[12], new byte[16]);
    EncryptedMessageContent decoded =
        mapper.readValue(mapper.writeValueAsString(content), EncryptedMessageContent.class);

    assertEquals(3, decoded.keyVersion());
    assertArrayEquals(new byte[] {1, 2, 3}, decoded.ciphertext());
    assertEquals(12, decoded.nonce().length);
    assertEquals(16, decoded.authenticationTag().length);
  }

  @Test
  void validatesEncryptionLengthsAndAlgorithms() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EncryptedMessageContent(1, new byte[] {1}, new byte[11], new byte[16]));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RecipientKeyEnvelope(
                1, EncryptionAlgorithms.KEY_ENVELOPE, new byte[31], new byte[48]));
  }

  @Test
  void rejectsDuplicateRecipientEnvelopes() {
    RecipientKeyEnvelope envelope =
        new RecipientKeyEnvelope(
            5, EncryptionAlgorithms.KEY_ENVELOPE, new byte[32], new byte[48]);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new GroupKeyRotationRequest(
                10, 2, EncryptionAlgorithms.MESSAGE, List.of(envelope, envelope)));
  }

  @Test
  void protectsBinaryValuesFromMutation() {
    byte[] ciphertext = new byte[] {1, 2};
    EncryptedMessageContent content =
        new EncryptedMessageContent(1, ciphertext, new byte[12], new byte[16]);
    ciphertext[0] = 9;
    byte[] returned = content.ciphertext();
    returned[1] = 9;

    assertArrayEquals(new byte[] {1, 2}, content.ciphertext());
  }
}
