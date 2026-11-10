# Protocol Requirements Checklist: Common V2 Foundation

**Purpose**: Reviewer gate for completeness, clarity, consistency, and security of the shared protocol requirements
**Created**: 2026-11-10
**Feature**: [spec.md](../spec.md)

**Review Ownership**: Mark `[x]` only when a reviewer determines the requirements-quality criterion is satisfied. It does not mean implementation is complete.

## Coverage and traceability

- [ ] CHK001 Are all 9 Admin functional groups mapped to actor, access, request, response, error, event, and dependency requirements? [Completeness, Spec §SC-001]
- [ ] CHK002 Are all 12 User functional groups mapped with the same contract dimensions? [Completeness, Spec §SC-001]
- [ ] CHK003 Are every action, error, and event identifier traceable to a user story or functional requirement? [Traceability, Spec §FR-003–FR-005]
- [ ] CHK004 Are deferred Server, User, and Admin responsibilities explicitly distinguished from Common deliverables? [Boundary, Spec §FR-027]

## Clarity and consistency

- [ ] CHK005 Are request, response, and event correlation rules unambiguous for concurrent traffic? [Clarity, Spec §FR-001]
- [ ] CHK006 Are identity, time, nullability, empty-list, paging, sort, and date-range conventions consistent across all contracts? [Consistency, Spec §FR-006–FR-008, FR-026]
- [ ] CHK007 Are delete-for-me, global recall, and history clearing specified as distinct lifecycle operations? [Clarity, Spec §FR-013]
- [ ] CHK008 Are Admin account status, spam review status, friendship state, and membership role kept as separate concepts? [Consistency, Spec §FR-004, FR-016]
- [ ] CHK009 Are PING compatibility expectations and permitted envelope evolution explicitly bounded? [Compatibility, Spec §FR-002, FR-028]

## Failure and edge-case coverage

- [ ] CHK010 Are malformed JSON, unknown action, unsupported action, invalid payload type, missing data, invalid enum, and over-limit input requirements distinguishable? [Coverage, Spec §FR-004, FR-023–FR-025]
- [ ] CHK011 Are immediate EOF, unterminated final line, LF, CRLF, exact-limit, and over-limit framing outcomes specified? [Edge Case, Spec §FR-024]
- [ ] CHK012 Are empty results, optional filters, invalid date ranges, and invalid paging bounds specified? [Edge Case, Spec §FR-008, FR-023, FR-026]
- [ ] CHK013 Are conflict, block, membership, role, and account-state errors defined without leaking backend details? [Security/Clarity, Spec §FR-004, FR-025]

## Security requirements

- [ ] CHK014 Are password, reset-code, authentication-secret, private-key, and plaintext-group-key exposure rules explicit for requests, responses, errors, and logs? [Security, Spec §FR-010, FR-021, FR-025]
- [ ] CHK015 Are encrypted-message mutual exclusivity, nonce/tag lengths, key version, algorithm, and ciphertext rules objectively testable? [Measurability, Spec §FR-020]
- [ ] CHK016 Are HPKE profile, envelope lengths, recipient identity, AAD context, and distribution completeness requirements documented? [Completeness, Spec §FR-021–FR-022]
- [ ] CHK017 Are membership/key-replacement rotation and encrypted-send pause requirements clear without claiming implementation? [Boundary, Spec §FR-022]
- [ ] CHK018 Is the plaintext-versus-encrypted search boundary explicit and consistent with no server decryption key? [Security, Assumption]

## Acceptance quality

- [ ] CHK019 Can each success criterion be objectively verified using the documented artifacts and commands? [Measurability, Spec §SC-001–SC-008]
- [ ] CHK020 Are baseline/environment failures required to be distinguished from regressions without weakening tests? [Acceptance, Spec §SC-005]

## Notes

- This checklist intentionally remains unchecked until human requirements review.
- The user's autonomous-execution instruction permits implementation to proceed without treating unchecked items as approval.
