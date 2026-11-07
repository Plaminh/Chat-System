package com.chat.user.controller;

import com.chat.user.model.Conversation;
import com.chat.user.model.FriendEntry;
import com.chat.user.model.FriendRequest;
import com.chat.user.model.UserProfile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Controller xử lý lọc danh sách bạn bè, tìm kiếm danh bạ và cập nhật lời mời kết bạn.
 * Tách biệt nghiệp vụ lọc quan hệ và chặn (blocking) khỏi FriendsPanel và FriendRequestsSearchPanel.
 */
public final class FriendsController {

  public List<FriendEntry> filterFriends(
      List<FriendEntry> friends, Set<String> blockedUsernames, String query, boolean onlyOnline) {
    if (friends == null) {
      return List.of();
    }
    String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    return friends.stream()
        .filter(friend -> !isBlocked(friend.username(), blockedUsernames))
        .filter(friend -> filter.isEmpty() || friend.fullName().toLowerCase(Locale.ROOT).contains(filter))
        .filter(friend -> !onlyOnline || friend.online())
        .toList();
  }

  public List<FriendRequest> filterFriendRequests(List<FriendRequest> requests, String query) {
    return filterFriendRequests(requests, Set.of(), Set.of(), query);
  }

  public List<FriendRequest> filterFriendRequests(
      List<FriendRequest> requests,
      Set<String> blockedByMe,
      Set<String> blockedMe,
      String query) {
    if (requests == null) {
      return List.of();
    }
    String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    return requests.stream()
        .filter(request -> request.status() == FriendRequest.Status.PENDING)
        .filter(request -> !isBlocked(request.requester().username(), blockedByMe))
        .filter(request -> !isBlocked(request.requester().username(), blockedMe))
        .filter(request -> filter.isEmpty() || request.requester().fullName().toLowerCase(Locale.ROOT).contains(filter))
        .toList();
  }

  public List<UserProfile> searchUsers(
      List<UserProfile> directory, Set<String> blockedUsernames, String searchText) {
    return searchUsers(directory, blockedUsernames, Set.of(), searchText);
  }

  public List<UserProfile> searchUsers(
      List<UserProfile> directory,
      Set<String> blockedByMe,
      Set<String> blockedMe,
      String searchText) {
    if (directory == null || searchText == null) {
      return List.of();
    }
    String entered = searchText.trim().toLowerCase(Locale.ROOT);
    if (entered.isEmpty()) {
      return List.of();
    }
    String usernameQuery = entered.startsWith("@") ? entered.substring(1) : entered;
    return directory.stream()
        .filter(profile -> !isBlocked(profile.username(), blockedByMe))
        .filter(profile -> !isBlocked(profile.username(), blockedMe))
        .filter(
            profile ->
                profile.username().toLowerCase(Locale.ROOT).contains(usernameQuery)
                    || profile.fullName().toLowerCase(Locale.ROOT).contains(entered))
        .toList();
  }

  public boolean isEligibleForSpamReport(Conversation conversation) {
    return conversation != null && !conversation.messages().isEmpty();
  }

  public UserProfile resolveProfileForConversation(List<UserProfile> knownProfiles, Conversation conversation) {
    Objects.requireNonNull(conversation, "conversation");
    if (knownProfiles != null) {
      for (UserProfile profile : knownProfiles) {
        if (profile.username().equalsIgnoreCase(conversation.username())) {
          return profile;
        }
      }
    }
    return new UserProfile(
        conversation.username(),
        conversation.displayName(),
        conversation.username() + "@example.test",
        "Sample address",
        LocalDate.of(2000, 1, 1),
        "Not specified");
  }

  private boolean isBlocked(String username, Set<String> blockedUsernames) {
    if (username == null || blockedUsernames == null) {
      return false;
    }
    return blockedUsernames.stream().anyMatch(blocked -> blocked.equalsIgnoreCase(username));
  }
}