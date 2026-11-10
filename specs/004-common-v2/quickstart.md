# Quickstart: Common V2 Integration and Verification

## Prerequisites

- JDK 25
- Maven 3.9.9 or `.tools/apache-maven-3.9.9/bin/mvn.cmd`
- `feature/common-v2` checked out

## Module usage

Consumer modules already depend on `com.chat:common`. Construct a domain request record, convert it to `JsonNode`, and place it in `RequestEnvelope`. Deserialize the envelope first and map `data` to the action's documented type. Apply the same pattern to response and event data.

## Shared UI theme exception

- Admin and User continue importing the existing `com.chat.common.ui.ChatTheme` from the Common module.
- Keep `common/src/main/java/com/chat/common/ui/ChatTheme.java`, its Swing helpers, FlatLaf defaults, and both clients' initialization behavior unchanged.
- Do not create client-local theme copies or add new shared UI infrastructure.
- Server code must not import `com.chat.common.ui` or call Swing theme utilities.
- DTO, enum, protocol, validation, and exception packages must not import `java.awt`, `javax.swing`, FlatLaf, or other UI APIs.

Example existing client import:

```java
import com.chat.common.ui.ChatTheme;
```

## Routing rules

1. Requests contain a unique non-blank `requestId` and `ActionType`.
2. Responses reuse that `requestId`.
3. Events have their own `eventId` and `MessageType`; they are not responses.
4. On failure, branch on `errorCode`; `message` is safe explanatory text, not machine control.
5. Unknown enum names are protocol incompatibilities and are never silently ignored.

## Encryption boundary

- User Client owns private keys and encryption/decryption.
- Server stores/forwards public keys, HPKE envelopes, epochs, and ciphertext.
- Common validates metadata only.
- Never log passwords, reset codes, private keys, plaintext group keys, or decrypted encrypted-group content.

## Validation scenarios

### Common PING contract regression

Round-trip `{"requestId":"req-1","action":"PING","data":{}}` through the existing Common envelope and confirm the `PING` action and correlation fields remain compatible. Server dispatch behavior is outside Common V2 scope.

### Invalid requests

Exercise missing requestId, unknown action, scalar payload where an object is required, and malformed JSON through Common validation. Each produces its documented Common validation category without internal exception text.

### NDJSON boundary

Exercise LF, CRLF, immediate EOF, unterminated final content, 65,535, 65,536, and 65,537 characters.

### Encryption

Round-trip non-empty ciphertext with a 12-byte nonce, 16-byte tag, and positive version. Mixed plaintext/ciphertext and invalid lengths must fail.

### Empty paging

Round-trip `items: []` with zero totals. `items: null` is invalid.

## Commands

```powershell
$mvnTool = '.tools/apache-maven-3.9.9/bin/mvn.cmd'
& $mvnTool -pl common -am test
& $mvnTool test
& $mvnTool clean verify
& $mvnTool spotless:check
```

Report only commands actually run. Spotless is check-only; do not format unrelated files.

## Deferred Server integration

Common V2 does not modify Server source or tests. `feature/server-v2` owns:

- Former T031: map Common malformed, unknown, invalid, and unsupported outcomes in `RequestHandler`.
- Former T032: add Server request-handler protocol regression tests.

Admin and User source integration is likewise outside Common V2; reactor commands above are compilation/regression checks, not authorization to edit consumer modules.

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
