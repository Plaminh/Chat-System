package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.list;
import static com.chat.common.protocol.ContractChecks.positive;

import com.chat.common.dto.social.FriendInfo;
import java.util.List;

public record FriendNetworkInfo(
    long userId, List<FriendInfo> directFriends, int directCount, int friendsOfFriendsCount) {
  public FriendNetworkInfo {
    userId = positive(userId, "userId");
    directFriends = list(directFriends, "directFriends");
    if (directCount < 0 || friendsOfFriendsCount < 0 || directCount != directFriends.size()) {
      throw new IllegalArgumentException("friend counts are inconsistent");
    }
  }
}
