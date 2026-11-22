package com.chat.common.dto.encryption;

public final class EncryptionAlgorithms {
  public static final String PUBLIC_KEY = "DHKEM-X25519-HKDF-SHA256";
  public static final String PUBLIC_KEY_FORMAT = "RFC9180-RAW";
  public static final String MESSAGE = "AES-256-GCM";
  public static final String KEY_ENVELOPE = "HPKE-BASE-X25519-HKDF-SHA256-AES-256-GCM";
  public static final String HPKE_INFO = "JavaChatSystem-V1-GroupKey";

  private EncryptionAlgorithms() {}
}
