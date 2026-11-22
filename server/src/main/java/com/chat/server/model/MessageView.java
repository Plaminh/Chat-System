package com.chat.server.model;

/** A message joined with its sender; {@code sender} is null when the sender row was removed. */
public record MessageView(Message message, UserBrief sender) {}
