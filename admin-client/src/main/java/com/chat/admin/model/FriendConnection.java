package com.chat.admin.model;

/** Represents a direct friend entry for a user in the Admin inspection dialog. */
public record FriendConnection(
    String friendUsername, String friendFullName, int mutualFriendsCount, boolean online) {}
