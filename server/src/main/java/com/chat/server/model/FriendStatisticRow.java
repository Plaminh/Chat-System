package com.chat.server.model;

/** Direct friend count and distinct friends-of-friends count for one user. */
public record FriendStatisticRow(UserBrief user, long directFriends, long friendsOfFriends) {}
