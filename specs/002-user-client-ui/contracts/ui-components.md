# User UI Contracts

These contracts define how the approved User screens interact with one another in Version 1. They are presentation contracts only; they do not define TCP actions, server authorization, database behavior, or a shared protocol.

## Shared workspace shell

| Surface | Inputs | User action | Expected UI result |
|---|---|---|---|
| Sign-in | Username/email and password text | Submit, register, or recover | Validate required inputs; successful sample sign-in opens the workspace; registration/recovery opens its corresponding form. No live authentication is claimed. |
| User workspace | Current sample profile and active navigation destination | Select Chats, Friends, Requests/Search, Groups, or Profile | Display the corresponding view in the common User shell and preserve sample state while navigating. |
| Profile | Project-required account fields | Edit, save, cancel, open password change | Show local sample update or keep old values on cancel; password change is a mock form and never displays or persists a real password. |

## Friends and discovery

| Surface | Inputs | User action | Expected UI result |
|---|---|---|---|
| Friends | Friend entries, presence and optional name filter | Filter, open chat, unfriend, block, show online-only | Filter by display name; show online state; route to the selected conversation or request confirmation for sample relationship action. |
| Friend requests | Pending request entries and optional name filter | Accept or decline | Update the selected sample request state and visible list locally. |
| User search | Username/full-name query and current user's blocked set | Search, open conversation, select for group | Show matching non-blocked sample profiles; empty results are explicit; actions open chat or continue group selection. |

## Conversations

| Surface | Inputs | User action | Expected UI result |
|---|---|---|---|
| Personal chat | Selected conversation and ordered sample messages | Select conversation, compose, send in mock mode, open search/report/writing aid | Display the selected conversation and sample message state; compose/send interaction is labelled as mock; child action opens the matching surface. |
| History search | Search text, optional current conversation scope | Search and select a result | Show matching sample messages with conversation and time context; selecting a result navigates to its conversation/message position. |
| Message actions | Selected message IDs and action choice | Delete for Me, Unsend, or clear history | Keep action types distinct, confirm destructive action, apply only local sample state, and leave the prior state unchanged on cancel. |

## Groups, reporting, and writing assistance

| Surface | Inputs | User action | Expected UI result |
|---|---|---|---|
| Group management | Group name and selected other usernames | Create, rename, add/remove member, assign group admin | Reject creation with no additional participant; otherwise update sample group state locally. Confirm destructive membership changes. |
| Group chat | Selected group and sample members/messages | Open group, compose, inspect members/roles | Display group messages and membership; encryption marker remains explicitly a non-functional mock indicator. |
| Spam report | Account with prior sample chat interaction and report reason | Submit or cancel | Require a reason; show sample confirmation only on submit; never report from an unrelated search result. |
| Writing assistance | Current draft and suggestion/revision mode | Preview, accept, discard, close | Show fixture suggestion/revision; change the composer only on explicit accept; keep the draft on discard/cancel. |

## Shared invariants

- Every screen is reachable from the sign-in flow or shared workspace navigation.
- Lists and searches provide an explicit empty state.
- Cancel does not mutate sample state.
- Blocked users do not appear in the blocking user's search results.
- Report action is offered only for a prior-interaction conversation.
- Group creation requires at least one additional participant.
- Delete for Me, Unsend, and Clear Conversation History remain distinct and do not claim unspecified server semantics.
- All remote-service-dependent actions remain visibly sample-only in Version 1.
