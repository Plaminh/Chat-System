# Quickstart: Common V2 Integration and Verification

## Prerequisites

- JDK 25
- Maven 3.9.9 or `.tools/apache-maven-3.9.9/bin/mvn.cmd`
- `feature/common-v2` checked out

## Module usage

Consumer modules already depend on `com.chat:common`. Construct a domain request record, convert it to `JsonNode`, and place it in `RequestEnvelope`. Deserialize the envelope first and map `data` to the action's documented type. Apply the same pattern to response and event data.

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

### PING regression

Send `{"requestId":"req-1","action":"PING","data":{}}`; expect success, the same request ID, and `{"message":"PONG"}`.

### Invalid requests

Exercise missing requestId, unknown action, scalar payload where an object is required, and malformed JSON. Each produces its documented error without internal exception text.

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

## References

- [Protocol contract](contracts/protocol-contract.md)
- [Action matrix](contracts/action-matrix.md)
- [Wire model](data-model.md)
