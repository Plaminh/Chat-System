# Database V1 Entity-Relationship Diagram

## Scope

This diagram is the approved twelve-table Version 1 model. It describes persistence structure only; authorization, coordinated cross-table updates, realtime behavior, and encryption key management remain Version 2 backend work.

```mermaid
erDiagram
    USERS ||--o{ LOGIN_HISTORY : records
    USERS ||--o{ FRIEND_REQUESTS : sends
    USERS ||--o{ FRIEND_REQUESTS : receives
    USERS ||--o{ FRIENDSHIPS : low_user
    USERS ||--o{ FRIENDSHIPS : high_user
    FRIEND_REQUESTS o|--o| FRIENDSHIPS : establishes
    USERS ||--o{ USER_BLOCKS : blocks
    USERS ||--o{ USER_BLOCKS : is_blocked

    USERS o|--o{ CONVERSATIONS : owns_group
    CONVERSATIONS ||--o| DIRECT_CONVERSATIONS : specializes
    USERS ||--o{ DIRECT_CONVERSATIONS : low_user
    USERS ||--o{ DIRECT_CONVERSATIONS : high_user
    CONVERSATIONS ||--o{ CONVERSATION_MEMBERS : has
    USERS ||--o{ CONVERSATION_MEMBERS : participates
    CONVERSATIONS ||--o{ MESSAGES : contains
    USERS o|--o{ MESSAGES : sends
    USERS o|--o{ MESSAGES : unsends
    MESSAGES ||--o{ MESSAGE_DELETIONS : hidden_by
    USERS ||--o{ MESSAGE_DELETIONS : hides

    USERS o|--o{ SPAM_REPORTS : reports
    USERS o|--o{ SPAM_REPORTS : reported
    USERS o|--o{ SPAM_REPORTS : reviews
    MESSAGES o|--o{ SPAM_REPORTS : evidence
    USERS ||--o{ USER_ACTIVITY_LOGS : performs
    CONVERSATIONS o|--o{ USER_ACTIVITY_LOGS : contextualizes

    USERS {
        bigint user_id PK
        varchar username UK
        varchar password_hash
        varchar email UK
        varchar full_name
        varchar address
        date date_of_birth
        varchar gender
        varchar role
        varchar account_status
        timestamptz locked_at
        timestamptz created_at
        timestamptz updated_at
    }
    LOGIN_HISTORY {
        bigint login_history_id PK
        bigint user_id FK
        timestamptz logged_in_at
        timestamptz logged_out_at
    }
    FRIEND_REQUESTS {
        bigint friend_request_id PK
        bigint sender_user_id FK
        bigint receiver_user_id FK
        varchar status
        timestamptz created_at
        timestamptz responded_at
    }
    FRIENDSHIPS {
        bigint friendship_id PK
        bigint user_low_id FK
        bigint user_high_id FK
        bigint established_by_request_id FK
        timestamptz created_at
    }
    USER_BLOCKS {
        bigint user_block_id PK
        bigint blocker_user_id FK
        bigint blocked_user_id FK
        timestamptz created_at
    }
    CONVERSATIONS {
        bigint conversation_id PK
        varchar conversation_type
        varchar name
        bigint owner_user_id FK
        boolean encryption_enabled
        timestamptz created_at
        timestamptz updated_at
    }
    DIRECT_CONVERSATIONS {
        bigint conversation_id PK,FK
        bigint user_low_id FK
        bigint user_high_id FK
    }
    CONVERSATION_MEMBERS {
        bigint conversation_member_id PK
        bigint conversation_id FK
        bigint user_id FK
        varchar member_role
        timestamptz joined_at
        timestamptz left_at
        timestamptz history_cleared_at
    }
    MESSAGES {
        bigint message_id PK
        bigint conversation_id FK
        bigint sender_user_id FK
        text body
        timestamptz created_at
        timestamptz unsent_at
        bigint unsent_by FK
    }
    MESSAGE_DELETIONS {
        bigint message_deletion_id PK
        bigint message_id FK
        bigint user_id FK
        timestamptz deleted_at
    }
    SPAM_REPORTS {
        bigint spam_report_id PK
        bigint reporter_user_id FK
        bigint reported_user_id FK
        bigint message_id FK
        text reason
        varchar review_status
        bigint reviewed_by FK
        text resolution_note
        timestamptz created_at
        timestamptz resolved_at
    }
    USER_ACTIVITY_LOGS {
        bigint user_activity_log_id PK
        bigint user_id FK
        varchar activity_type
        bigint conversation_id FK
        timestamptz occurred_at
    }
```

## Cardinality notes

- A user can have many relationship/history rows, but self-relationships are rejected.
- Friendship and direct-conversation pairs are canonical unordered pairs (`low < high`).
- Every direct conversation has exactly one direct specialization by contract; this cross-table requirement is created transactionally in Version 2.
- A conversation has many membership rows and messages. A direct conversation must have two active members; a group must have an owner who is an active admin.
- A recalled message retains its identity, conversation, sender, creation time, and recall metadata while `body` is replaced by the canonical `[Message unsent]` placeholder.
- A message can have multiple deletion rows, at most one per user, and can be referenced as optional evidence by reports.
- Reports retain their own workflow independently from user account status.
