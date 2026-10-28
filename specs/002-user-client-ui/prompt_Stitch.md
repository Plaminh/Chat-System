# Stitch/Figma Design Prompts — User Client UI Version 1

Use the shared design system below for each prompt. Generate desktop application mockups only; do not generate a web page or mobile layout. Keep labels and sample copy in Vietnamese. The mockups are visual design assets and must not imply that backend services are already active.

## Shared design system

- Platform and canvas: desktop chat application, 1440 × 900 reference frame, responsive to common Windows desktop window sizes.
- Visual direction: warm, welcoming, polished chat product; clean information hierarchy and restrained decoration.
- Color palette: light neutral canvas (#F5F7FB), white surfaces (#FFFFFF), primary indigo/blue (#4F46E5), dark slate text (#1F2937), muted slate (#64748B), subtle borders (#E2E8F0), online green (#16A34A), pending amber (#D97706), destructive red (#DC2626).
- Typography: clear modern sans-serif; strong page titles, readable body text, compact secondary labels.
- Components: consistent 8 px spacing rhythm, moderate corner radius, subtle shadows, accessible contrast, clear focus/hover/selected states, Lucide-style line icons.
- Use believable but fictional Vietnamese names and sample messages. Do not include real personal data, credentials, API keys, or passwords.
- Common states to design where appropriate: loading placeholder, empty list, no search results, validation message, confirmation dialog, success feedback.
- Use one shared visual language across account entry, main workspace, friends, conversations, groups, and dialogs.

## Prompt 1 — Account access and profile

Design the account entry and account settings screens for a Vietnamese desktop chat app. Include:

1. Sign-in screen with app identity, username field, password field with visibility toggle, primary sign-in button, and links to registration and password recovery.
2. Registration form with clear required fields, inline validation, and cancel/submit actions.
3. Password recovery form asking for an email address and a neutral confirmation state. Never display a generated password.
4. Profile settings with a sample avatar, editable personal information, save/cancel controls, and a separate change-password dialog.

Keep the forms calm and focused. Provide clear validation and success/error states. Show sample-only interactions, not real authentication or email delivery.

## Prompt 2 — Main workspace and personal chat

Design the primary signed-in desktop workspace for a chat application at 1440 × 900. Use a compact left navigation rail/sidebar for Chats, Friends, Friend Requests, Search, Groups, and Profile; show the current user's sample avatar and a sign-out action. The main chat workspace has a conversation list with search/filter, sample contacts and last-message previews, and a selected personal conversation on the right.

The conversation view includes contact identity and online/offline status, readable message bubbles with timestamps, a scrollable history, a composer with send/attachment affordances, and visible actions for in-conversation search, report, and conversation details. Include a global conversation-history search state that groups matching snippets by conversation and lets the user jump to the selected message.

Show separate menu actions for “Xóa với tôi” (delete for me), “Thu hồi tin nhắn” (unsend), and “Xóa lịch sử trò chuyện”. Use confirmation dialogs and neutral copy; do not imply shared deletion behavior or a live server. Include empty and no-search-results states.

## Prompt 3 — Friends, friend requests, and people search

Create coordinated desktop panels for:

- Friends: searchable list, avatar, full name, username, online/offline status, and actions to open chat, unfriend, or block.
- Friend requests: incoming request list with name filtering and clear accept/decline actions.
- People search: search by username or full name, result cards, and actions to start a conversation or select a person for group creation.
- Online friends: a filterable subset of friends showing only online contacts, with open-chat and start-group actions.

Include realistic empty states, confirmation before block/unfriend, and a sample blocked user who does not appear in the searcher's results. Use Vietnamese labels and consistent navigation.

## Prompt 4 — Group list, group setup, and group chat

Design the group-chat experience as coordinated desktop screens/dialogs. Include a group list; a create-group dialog requiring a group name and at least one other username; a group conversation view; and a group-details/member dialog.

The member list must visually distinguish group administrators from regular members. Provide controls for rename, add member, remove member, and assign group administrator. Include a message history and composer in group chat. Show a restrained lock/encryption badge clearly marked as a visual placeholder for the design phase, not proof of active encryption. Include empty state and confirmation for member-removal or other destructive actions.

## Prompt 5 — Spam report and writing assistance

Design two compact dialogs integrated into the personal chat workspace:

1. Spam report: opened from a prior-interaction conversation, show the reported account identity, a reason text area, cancel/submit actions, inline validation, and a sample confirmation state.
2. Writing assistance: opened from the message composer, offer “Gợi ý tin nhắn” and “Chỉnh sửa bản nháp”, show a draft and a proposed result preview, and provide “Chèn vào ô soạn”, “Thử lại” (visual only), and “Đóng” actions.

Do not display API/provider settings or claim that a report was delivered or an AI service was contacted. Keep user control over whether a suggested message replaces or supplements their draft.
