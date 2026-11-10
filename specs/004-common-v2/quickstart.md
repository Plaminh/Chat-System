# Quickstart: Common V2 Integration and Verification

## Prerequisites

- JDK 25
- Maven 3.9.9 or `.tools/apache-maven-3.9.9/bin/mvn.cmd`
- `feature/common-v2` checked out

## Import Common

The existing Admin, User, and Server modules already depend on the reactor's `com.chat:common` artifact. An external Maven consumer would use the same project version:

```xml
<dependency>
    <groupId>com.chat</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Common supplies wire DTOs, enums, envelopes, framing, and context-free validation. It does not open sockets, authenticate users, authorize resources, query PostgreSQL, send email, call an LLM, or perform cryptography.

## Configure Jackson once

Use the Java Time module so `LocalDate` and `OffsetDateTime` remain ISO-8601 text. Do not enable unknown-enum fallbacks; unknown protocol identifiers must fail.

```java
ObjectMapper mapper =
    JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
```

## Construct and decode requests

Payloads are JSON objects, including payload-free actions such as `PING`, which use `{}`.

```java
LoginRequest login = new LoginRequest("alice", "request-only-password");
RequestEnvelope outbound =
    new RequestEnvelope(
        UUID.randomUUID().toString(), ActionType.LOGIN, mapper.valueToTree(login));
String ndjsonLine = mapper.writeValueAsString(outbound) + "\n";

RequestEnvelope inbound = ProtocolValidator.parseRequest(mapper, ndjsonLine.stripTrailing());
LoginRequest decoded = ProtocolValidator.decodePayload(mapper, inbound, LoginRequest.class);
```

`ProtocolValidator.parseRequest` reports malformed JSON, invalid envelope shape, missing/blank correlation IDs, missing or unknown actions, and missing/non-object data with stable `ErrorCode` values. `decodePayload` reports JSON shape/type mismatches as `INVALID_PAYLOAD` and DTO invariant failures as `VALIDATION_FAILED` without exposing the rejected values or internal exception details.

## Decode responses and errors

```java
ResponseEnvelope response = mapper.readValue(jsonLine, ResponseEnvelope.class);
if (!response.success()) {
  ErrorCode code = response.errorCode();
  String safeMessage = response.message();
  // Branch on code. Treat message as display text, not machine control.
} else {
  AuthenticationResult result =
      mapper.treeToValue(response.data(), AuthenticationResult.class);
}
```

A successful response has a non-blank request ID, null `errorCode`, and null error message. A failed response has an `errorCode`, safe non-blank message, and null data. A failure for malformed input whose request ID cannot be read may have a null request ID. Server V2 owns the mapping from Common validation exceptions to response envelopes.

## Decode events

Events are not responses: they carry their own `eventId` and `MessageType` and therefore never consume a pending request correlation.

```java
EventEnvelope event = ProtocolValidator.parseEvent(mapper, jsonLine);
switch (event.type()) {
  case PRESENCE_CHANGED -> {
    PresenceUpdate presence = mapper.treeToValue(event.data(), PresenceUpdate.class);
    // Update presentation state in the consuming client.
  }
  case GROUP_MEMBERSHIP_CHANGED -> {
    GroupMembershipEvent membership =
        mapper.treeToValue(event.data(), GroupMembershipEvent.class);
  }
  default -> {
    // Route other documented MessageType values explicitly.
  }
}
```

Event publication, authorization, offline delivery, retry behavior, and UI updates are deferred integrations.

## Pagination and time conventions

- Pages are zero-based.
- `PageRequest.size` is 1 through 100 inclusive.
- Empty results use `items: []`, never `items: null`, and retain complete non-negative metadata.
- Domain IDs are positive JSON integers; display IDs are never domain IDs.
- Instants are ISO-8601 offset date-times. Calendar-only values are ISO dates.

```java
PageRequest firstPage = new PageRequest(0, 20);
PageResult<UserSummary> empty = new PageResult<>(List.of(), 0, 20, 0, 0);
DateRange range =
    new DateRange(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"));
```

## Realtime message and recall examples

Normal messages contain exactly one of plaintext `body` or `encryptedContent`. A recalled message contains neither and preserves identity, creation time, recall time, and recalling user ID.

```java
SendMessageRequest plaintext = new SendMessageRequest(20L, "Hello", null);

MessageInfo recalled =
    new MessageInfo(
        10L,
        20L,
        sender,
        null,
        null,
        OffsetDateTime.parse("2026-11-10T09:15:00+07:00"),
        OffsetDateTime.parse("2026-11-10T09:16:00+07:00"),
        sender.userId());
```

Delete-for-me, global recall, and history clearing remain separate actions and Server persistence operations.

## Encryption metadata

Common validates public/opaque metadata only. The User Client owns private keys and AES/HPKE operations; the Server stores and forwards public keys, recipient envelopes, epochs, and ciphertext without private or plaintext group keys.

```java
EncryptedMessageContent content =
    new EncryptedMessageContent(
        3,
        ciphertext,
        freshNonce12Bytes,
        authenticationTag16Bytes);
SendMessageRequest encrypted = new SendMessageRequest(20L, null, content);

RecipientKeyEnvelope recipient =
    new RecipientKeyEnvelope(
        recipientKeyId,
        EncryptionAlgorithms.KEY_ENVELOPE,
        encapsulatedKey32Bytes,
        encryptedGroupKey48Bytes);
GroupKeyRotationRequest rotation =
    new GroupKeyRotationRequest(
        20L, 3, EncryptionAlgorithms.MESSAGE, List.of(recipient));
```

Binary arrays are defensively copied. Membership/key replacement requires a future epoch; new or rejoining members receive no old epoch by default. Authenticity, distribution completion, send pausing, encryption/decryption, and key storage are deferred.

## Shared UI theme exception

Admin and User continue importing the existing shared theme:

```java
import com.chat.common.ui.ChatTheme;
```

`common/src/main/java/com/chat/common/ui/ChatTheme.java`, its test, client imports, FlatLaf defaults, and initialization behavior remain unchanged. Do not create client-local copies or new Common UI infrastructure. Server must not import `com.chat.common.ui`; DTO, enum, protocol, validation, and exception packages must not import UI APIs.

The immutable legacy theme and its immutable test are excluded from Spotless because Google Java Format would rewrite their approved V1 baseline content.

## Verification commands

On a machine whose default `JAVA_HOME` is not JDK 25, set it for the current PowerShell process first.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-25'
$mvnTool = '.tools/apache-maven-3.9.9/bin/mvn.cmd'

& $mvnTool -pl common -am test
& $mvnTool test
& $mvnTool clean verify
& $mvnTool spotless:check
git diff --check
```

Report only commands actually run. Spotless is check-only; do not run `spotless:apply` or format unrelated files.

## Deferred handover

Common V2 does not implement business workflows. The following remain outside this feature:

- Server V2 former T031: map Common malformed, unknown, invalid, and unsupported outcomes in `RequestHandler`.
- Server V2 former T032: add request-handler protocol regression tests.
- Server authentication, authorization, persistence, socket/session lifecycle, event publication, offline delivery, email, and writing-provider integration.
- User/Admin request routing, response/event consumption, UI behavior, and client-side encryption/key lifecycle.

## Theme-preservation verification

Compare the feature branch with V1 baseline `67107ac` and confirm:

1. `common/ui/ChatTheme.java` and `common/ui/ChatThemeTest.java` have identical Git blob hashes.
2. No other source file named `ChatTheme.java` exists.
3. `admin-client/src`, `user-client/src`, and `server/src` have no source delta for this decision.
4. Server has no `com.chat.common.ui` import.
5. Common V2 DTO/protocol/validation packages have no Swing or AWT imports.
6. Admin and User compile against the shared Common theme.

## References

- [Protocol contract](contracts/protocol-contract.md)
- [Action matrix](contracts/action-matrix.md)
- [Wire model](data-model.md)
