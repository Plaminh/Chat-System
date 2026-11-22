package com.chat.server.model;

/** Activity counts for one user over a date range. */
public record ActivityStatisticRow(
    UserBrief user, long appOpens, long chattedUsers, long chattedGroups) {}
