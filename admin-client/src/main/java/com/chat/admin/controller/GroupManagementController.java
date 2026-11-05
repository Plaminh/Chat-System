package com.chat.admin.controller;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.GroupChatInfo;
import com.chat.admin.model.GroupChatInfo.GroupMemberInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý giám sát nhóm chat và danh sách thành viên cho GroupManagementPanel.
 */
public final class GroupManagementController {
  private final AdminMockDataStore dataStore;

  public GroupManagementController() {
    this(AdminMockDataStore.getInstance());
  }

  public GroupManagementController(AdminMockDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public List<GroupChatInfo> filterAndSortGroups(String searchGroupQuery, int sortIndex) {
    String query = searchGroupQuery == null ? "" : searchGroupQuery.trim().toLowerCase();

    List<GroupChatInfo> filtered = new ArrayList<>();
    for (GroupChatInfo g : dataStore.getGroups()) {
      if (query.isEmpty()
          || g.groupName().toLowerCase().contains(query)
          || g.groupId().toLowerCase().contains(query)) {
        filtered.add(g);
      }
    }

    Comparator<GroupChatInfo> comparator =
        switch (sortIndex) {
          case 1 -> Comparator.comparing(GroupChatInfo::createdAt);
          case 2 -> Comparator.comparing(g -> g.groupName().toLowerCase());
          case 3 ->
              Comparator.comparing((GroupChatInfo g) -> g.groupName().toLowerCase()).reversed();
          default -> Comparator.comparing(GroupChatInfo::createdAt).reversed();
        };
    filtered.sort(comparator);
    return filtered;
  }

  public List<GroupMemberInfo> filterMembers(
      GroupChatInfo group, boolean adminsOnly, String memberSearchQuery) {
    if (group == null) {
      return List.of();
    }
    String memberQuery = memberSearchQuery == null ? "" : memberSearchQuery.trim().toLowerCase();
    List<GroupMemberInfo> result = new ArrayList<>();
    for (GroupMemberInfo m : group.members()) {
      if (adminsOnly && !m.groupAdmin()) {
        continue;
      }
      if (!memberQuery.isEmpty()
          && !m.username().toLowerCase().contains(memberQuery)
          && !m.fullName().toLowerCase().contains(memberQuery)) {
        continue;
      }
      result.add(m);
    }
    return result;
  }

  public int getTotalGroupCount() {
    return dataStore.getGroups().size();
  }

  public Optional<GroupChatInfo> findGroupById(String groupId) {
    if (groupId == null) {
      return Optional.empty();
    }
    String cleanId = groupId.startsWith("#") ? groupId.substring(1) : groupId;
    return dataStore.getGroups().stream()
        .filter(g -> g.groupId().equalsIgnoreCase(cleanId))
        .findFirst();
  }

  public void addDataChangeListener(Runnable listener) {
    dataStore.addChangeListener(listener);
  }
}
