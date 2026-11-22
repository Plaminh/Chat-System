package com.chat.server.model;

import java.time.OffsetDateTime;

/**
 * A conversation in a user's list. {@code directPeer} is the other participant of a direct
 * conversation and null for groups. {@code lastMessageAt} is null when nothing was sent yet.
 */
public record ConversationOverview(
    Conversation conversation, UserBrief directPeer, OffsetDateTime lastMessageAt) {}
