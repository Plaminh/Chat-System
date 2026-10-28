# Data Model: User Client UI Version 1

These are presentation/sample-data concepts for the User Client UI. They do not define or modify the server's database schema or shared protocol.

## User Profile

Represents the signed-in sample user or a person displayed in a list.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `username` | Unique account handle shown in the UI | Required for sample account identity. |
| `fullName` | Person's name | Required for registration/profile sample flows and display. |
| `email` | Account contact email | Required where requested; recovery input must be non-blank and plausibly formatted. |
| `address` | User address | Editable/displayed only where included in the approved project account fields. |
| `dateOfBirth` | User birth date | Editable/displayed only where included in the approved project account fields. |
| `gender` | User gender | Editable/displayed only where included in the approved project account fields. |
| `avatarResource` | Optional local illustration/avatar reference | Presentation-only; no external user file or real personal data is required. |

No plaintext password is stored in a presentation model. Phone number, employer, department, work role, password expiry, 2FA, and enterprise security state shown in generated mockup copy are not part of this feature model.

## Friend Entry

Represents a friend or searchable user.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `username` | Account handle | Used as a stable sample identity. |
| `fullName` | Display name | Used for name filtering. |
| `presence` | Online or offline presentation state | Display state only; no live presence connection. |
| `blockedByCurrentUser` | Whether the current sample user blocked this account | If true, omit the account from that user's search results. |

## Friend Request

Represents an incoming sample request.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `requester` | Requesting user profile | Required. |
| `status` | Pending, accepted, or declined | Only pending requests offer accept/decline actions; sample state changes locally. |

## Conversation

Represents a personal conversation or group conversation in the signed-in user's workspace.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `conversationId` | Sample-only identifier | Stable within the current UI session. |
| `kind` | Personal or group | Determines participant and header presentation. |
| `displayName` | Friend or group name | Used in conversation list and header. |
| `participants` | Sample user identities | Used for group/member summaries. |
| `messages` | Ordered message list | Used for conversation history and search. |

## Message

Represents one sample entry within a conversation.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `messageId` | Sample-only identifier | Stable within the current UI session. |
| `senderUsername` | Author identity | Used to distinguish own and received messages. |
| `content` | Displayed message text | Sample content only. |
| `sentAt` | Display timestamp | Used for chronological presentation/search context. |
| `removedForCurrentUser` | Local Delete for Me presentation state | Hides only in the current sample view; does not assert server behavior. |
| `unsent` | Local Unsend presentation state | Displays a neutral withdrawn-message placeholder; does not imply other clients were updated. |

## Group Membership

Represents one sample participant in a group.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `groupId` | Sample group identifier | References a group in the sample state. |
| `user` | Participant profile | Required. |
| `role` | Group administrator or regular member | Role label is display-only unless a sample UI action changes it locally. |

Group creation requires at least one participant other than the creator. No key material or encryption state is stored in the User UI model; an encryption badge is an explicit placeholder.

## Spam Report Draft

Represents a report form before sample submission.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `reportedUsername` | Account being reported | Must be associated with an existing sample conversation/prior interaction. |
| `reason` | User-entered explanation | Required before the sample confirmation action is enabled. |

## Message Draft Assistance

Represents the current draft and the proposed writing-assistance result.

| Field | Meaning | Validation/display rule |
|---|---|---|
| `draftText` | Current composer text | Preserved unless the user explicitly accepts a preview. |
| `mode` | Suggest or revise | Selects the displayed sample assistance flow. |
| `proposedText` | Locally generated fixture text | Preview only; no LLM/API request in Version 1. |

## Relationships and sample state

- A profile can appear in friend, request, search, conversation, group membership, and report views.
- A conversation owns an ordered list of messages; a group conversation also has participants and roles.
- A spam report draft references a username with a prior sample conversation.
- Draft assistance is opened from a composer and returns text only after explicit user acceptance.
- Sample changes remain in the current application session; no server or database persistence is implied.
