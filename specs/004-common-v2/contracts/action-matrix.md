# Action Matrix

`Auth` values: Public, User, or Admin. All authenticated actions require an active account. Expected errors are in addition to common INVALID_REQUEST/INVALID_PAYLOAD/VALIDATION_FAILED/INTERNAL_ERROR outcomes.

## Compatibility and authentication

| Action | Auth | Request | Success | Expected errors | Events/dependencies |
|---|---|---|---|---|---|
| PING | Public | Empty object `{}` | Object `{"message":"PONG"}` | none | compatibility |
| REGISTER | Public | `RegisterRequest` | `AuthenticationResult` | ALREADY_EXISTS | users; email validation/delivery policy deferred |
| LOGIN | Public | `LoginRequest` | `AuthenticationResult` | INVALID_CREDENTIALS, ACCOUNT_LOCKED, ACCOUNT_DELETED | users/login_history/session registry |
| LOGOUT | User/Admin | Empty object `{}` | Empty object `{}` | UNAUTHENTICATED | login_history/session registry |
| CHANGE_PASSWORD | User/Admin | `PasswordChangeRequest` | Empty object `{}` | INVALID_CREDENTIALS | users; does not recover encryption keys |
| REQUEST_PASSWORD_RESET | Public | `PasswordRecoveryRequest` (email only) | Empty object `{}`, non-enumerating | none | Server generates/hashes/persists a random password and sends email; failure/rollback deferred |
| RESET_PASSWORD | Public | Deprecated `PasswordResetRequest` | Empty object `{}` | UNSUPPORTED_OPERATION or legacy-compatible outcome | compatibility identifier only; not the assignment recovery flow |

## Profile and social

| Action | Auth | Request | Success | Expected errors | Events/dependencies |
|---|---|---|---|---|---|
| GET_PROFILE | User | Empty object `{}` | `UserAccountDetail` | NOT_FOUND | users |
| UPDATE_PROFILE | User | `UpdateProfileRequest` | `UserAccountDetail` | ALREADY_EXISTS | users |
| SEARCH_USERS | User | `UserSearchRequest` | `PageResult<UserSummary>` | none | excludes blocks both directions |
| LIST_FRIENDS | User | `FriendListRequest` | `PageResult<FriendInfo>` | none | optional username/full-name query; friendships; presence in UserSummary |
| LIST_ONLINE_FRIENDS | User | `FriendListRequest` | `PageResult<FriendInfo>` | none | optional username/full-name query; friendships/session presence |
| SEND_FRIEND_REQUEST | User | `FriendRequestTarget` | `FriendRequestInfo` | ALREADY_EXISTS, BLOCKED_RELATIONSHIP | FRIEND_REQUEST_CHANGED |
| LIST_FRIEND_REQUESTS | User | `FriendRequestListRequest` | `PageResult<FriendRequestInfo>` | none | optional username/full-name, status, and INCOMING/OUTGOING filters; friend_requests |
| RESPOND_FRIEND_REQUEST | User | `FriendRequestDecisionRequest` | `FriendRequestInfo` | NOT_FOUND, CONFLICT | FRIEND_REQUEST_CHANGED |
| CANCEL_FRIEND_REQUEST | User | `FriendRequestIdTarget` | `FriendRequestInfo` | NOT_FOUND, CONFLICT | FRIEND_REQUEST_CHANGED |
| REMOVE_FRIEND | User | `UserTarget` | Empty object `{}` | NOT_FOUND | friendships |
| BLOCK_USER | User | `UserTarget` | Empty object `{}` | ALREADY_EXISTS | user_blocks; pending/friend state transaction |
| UNBLOCK_USER | User | `UserTarget` | Empty object `{}` | NOT_FOUND | user_blocks |

## Chat and groups

| Action | Auth | Request | Success | Expected errors | Events/dependencies |
|---|---|---|---|---|---|
| LIST_CONVERSATIONS | User | `PageRequest` | `PageResult<ConversationSummary>` | none | membership |
| OPEN_DIRECT_CONVERSATION | User | `UserTarget` | `ConversationSummary` | BLOCKED_RELATIONSHIP | direct pair transaction |
| SEND_MESSAGE | User | `SendMessageRequest` | `MessageInfo` | BLOCKED_RELATIONSHIP, INVALID_MEMBERSHIP, ENCRYPTION_REQUIRED, KEY_ROTATION_REQUIRED | DIRECT/GROUP_MESSAGE_RECEIVED; messages |
| LIST_MESSAGES | User | `MessageHistoryRequest` | `PageResult<MessageInfo>` | INVALID_MEMBERSHIP | visibility boundaries |
| DELETE_MESSAGE_FOR_ME | User | `MessageTarget` | Empty object `{}` | NOT_FOUND | message_deletions |
| UNSEND_MESSAGE | User | `MessageTarget` | `MessageInfo` | NOT_FOUND, UNAUTHORIZED | atomic redaction; message event |
| CLEAR_CONVERSATION_HISTORY | User | `ConversationTarget` | Empty object `{}` | INVALID_MEMBERSHIP | history_cleared_at |
| SEARCH_CONVERSATION_MESSAGES | User | `MessageSearchRequest` | `PageResult<MessageSearchHit>` | INVALID_MEMBERSHIP | plaintext server search; encrypted client search |
| SEARCH_ALL_MESSAGES | User | `MessageSearchRequest` | `PageResult<MessageSearchHit>` | none | accessible plaintext only |
| CREATE_GROUP | User | `CreateGroupRequest` | `GroupInfo` | ENCRYPTION_REQUIRED | group/membership/key transaction |
| GET_GROUP | User | `GroupTarget` | `GroupInfo` | NOT_FOUND, INVALID_MEMBERSHIP | conversations/members |
| LIST_GROUPS | User | `PageRequest` | `PageResult<GroupInfo>` | none | active membership |
| RENAME_GROUP | User | `RenameGroupRequest` | `GroupInfo` | INVALID_GROUP_ROLE | group event |
| ADD_GROUP_MEMBERS | User | `GroupMembersRequest` | `GroupInfo` | INVALID_GROUP_ROLE, CONFLICT, KEY_ROTATION_REQUIRED | GROUP_MEMBERSHIP_CHANGED; rotate encrypted group |
| REMOVE_GROUP_MEMBER | User | `GroupMemberTarget` | `GroupInfo` | INVALID_GROUP_ROLE, NOT_FOUND, KEY_ROTATION_REQUIRED | GROUP_MEMBERSHIP_CHANGED |
| LEAVE_GROUP | User | `GroupTarget` | Empty object `{}` | CONFLICT, KEY_ROTATION_REQUIRED | GROUP_MEMBERSHIP_CHANGED |
| UPDATE_GROUP_MEMBER_ROLE | User | `GroupRoleUpdateRequest` | `GroupInfo` | INVALID_GROUP_ROLE, CONFLICT | GROUP_ROLE_CHANGED |

## Encryption, report, and writing assistance

| Action | Auth | Request | Success | Expected errors | Events/dependencies |
|---|---|---|---|---|---|
| REGISTER_PUBLIC_KEY | User | `RegisterPublicKeyRequest` | `PublicKeyInfo` | CONFLICT, KEY_ROTATION_REQUIRED | authenticity/rotation Server+Client work |
| GET_PUBLIC_KEY_DIRECTORY | User | `PublicKeyDirectoryRequest` | `List<PublicKeyInfo>` | NOT_FOUND | active public keys |
| ROTATE_GROUP_KEY | User | `GroupKeyRotationRequest` | `GroupKeyEpochInfo` | INVALID_GROUP_ROLE, INVALID_MEMBERSHIP, CONFLICT | GROUP_KEY_ROTATION_REQUIRED/completion policy |
| GET_GROUP_KEY_ENVELOPE | User | `GroupKeyEnvelopeRequest` | `RecipientKeyEnvelope` | NOT_FOUND, INVALID_MEMBERSHIP | recipient-only authorization |
| CREATE_SPAM_REPORT | User | `SpamReportCreateRequest` | `SpamReportInfo` | NOT_FOUND, UNAUTHORIZED | prior chat required |
| REQUEST_WRITING_ASSISTANCE | User | `WritingAssistRequest` | `WritingAssistResponse` | UNSUPPORTED_OPERATION | provider integration deferred |

## Administration

| Action | Auth | Request | Success | Expected errors | Dependencies |
|---|---|---|---|---|---|
| ADMIN_LIST_USERS | Admin | `AdminUserFilter` | `PageResult<AdminUserInfo>` | none | users; optional createdAtRange produces the newly registered user list |
| ADMIN_GET_USER | Admin | `UserTarget` | `AdminUserInfo` | NOT_FOUND | users |
| ADMIN_CREATE_USER | Admin | `AdminCreateUserRequest` | `AdminUserInfo` | ALREADY_EXISTS | users |
| ADMIN_UPDATE_USER | Admin | `AdminUpdateUserRequest` | `AdminUserInfo` | NOT_FOUND, ALREADY_EXISTS | users |
| ADMIN_DELETE_USER | Admin | `UserTarget` | Empty object `{}` | NOT_FOUND, CONFLICT | terminal soft deletion |
| ADMIN_SET_ACCOUNT_STATUS | Admin | `AccountStatusUpdate` | `AdminUserInfo` | NOT_FOUND, CONFLICT | lock/unlock only; may publish ACCOUNT_STATUS_CHANGED |
| ADMIN_RESET_PASSWORD | Admin | `AdminPasswordResetRequest` | Empty object `{}` | NOT_FOUND | separate explicit Admin update; Server hashes/persists request-only new password |
| ADMIN_LIST_LOGIN_HISTORY | Admin | `LoginHistoryRequest` | `PageResult<LoginHistoryEntry>` | NOT_FOUND | global when userId omitted; otherwise one user; sort allow-list: loggedInAt; result includes username/full name |
| ADMIN_GET_FRIEND_NETWORK | Admin | `UserTarget` | `FriendNetworkInfo` | NOT_FOUND | friendships |
| ADMIN_LIST_GROUPS | Admin | `AdminGroupFilter` | `PageResult<GroupInspection>` | none | group conversations |
| ADMIN_GET_GROUP | Admin | `GroupTarget` | `GroupInspection` | NOT_FOUND | members/roles |
| ADMIN_LIST_SPAM_REPORTS | Admin | `SpamReportFilter` | `PageResult<SpamReportInfo>` | none | spam_reports |
| ADMIN_UPDATE_SPAM_REPORT | Admin | `SpamReportUpdate` | `SpamReportInfo` | NOT_FOUND, CONFLICT | review state only; sanctions explicit separately |
| ADMIN_GET_REGISTRATIONS | Admin | `DateRange` | `RegistrationCount` | none | aggregate range count only; use ADMIN_LIST_USERS.createdAtRange for the required user list |
| ADMIN_GET_MONTHLY_REGISTRATIONS | Admin | `YearRequest` | `List<MonthlyCount>` (12 months) | none | users.created_at |
| ADMIN_GET_FRIEND_STATISTICS | Admin | `FriendStatisticsRequest` | `PageResult<FriendStatistic>` | none | optional name/numeric filters; name/createdAt sort; friendships; distinct second degree |
| ADMIN_GET_USER_ACTIVITY | Admin | `UserActivityStatisticsRequest` | `PageResult<UserActivityStatistic>` | none | range, name/numeric filters, name/createdAt sort; app opens and distinct chatted users/groups |
| ADMIN_GET_MONTHLY_ACTIVE_USERS | Admin | `YearRequest` | `List<MonthlyCount>` (12 months) | none | distinct active users/month |
