package com.chat.admin.controller;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý logic tìm kiếm, lọc, sắp xếp, kiểm tra hợp lệ
 * và cập nhật tài khoản người dùng cho UserManagementPanel.
 */
public final class UserManagementController {
  private final AdminMockDataStore dataStore;

  public UserManagementController() {
    this(AdminMockDataStore.getInstance());
  }

  public UserManagementController(AdminMockDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public List<AdminUser> filterAndSortUsers(
      String searchQuery, String statusFilter, int sortIndex) {
    String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase();

    List<AdminUser> filtered = new ArrayList<>();
    for (AdminUser u : dataStore.getUsers()) {
      boolean matchesQuery =
          query.isEmpty()
              || u.getUsername().toLowerCase().contains(query)
              || u.getFullName().toLowerCase().contains(query)
              || u.getEmail().toLowerCase().contains(query);
      boolean matchesStatus =
          statusFilter == null
              || "All Statuses".equals(statusFilter)
              || u.getStatusText().equalsIgnoreCase(statusFilter);
      if (matchesQuery && matchesStatus) {
        filtered.add(u);
      }
    }

    Comparator<AdminUser> comparator =
        switch (sortIndex) {
          case 1 -> Comparator.comparing(AdminUser::getCreatedAt);
          case 2 -> Comparator.comparing(u -> u.getFullName().toLowerCase());
          case 3 ->
              Comparator.comparing((AdminUser u) -> u.getFullName().toLowerCase()).reversed();
          default -> Comparator.comparing(AdminUser::getCreatedAt).reversed();
        };
    filtered.sort(comparator);
    return filtered;
  }

  public int getTotalUserCount() {
    return dataStore.getUsers().size();
  }

  public Optional<AdminUser> findUserByUsername(String username) {
    return dataStore.findUserByUsername(username);
  }

  public boolean toggleUserLock(String username) {
    return dataStore.toggleUserLock(username);
  }

  public boolean deleteUser(String username) {
    return dataStore.deleteUser(username);
  }

  public void resetPassword(AdminUser user, String newPassword) {
    if (newPassword == null || newPassword.trim().length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters long.");
    }
    user.setPasswordHash("bcrypt$updated$" + newPassword.trim().hashCode());
    dataStore.notifyListeners();
  }

  public void saveUserProfile(
      AdminUser existingUser,
      String username,
      String fullName,
      String email,
      String address,
      LocalDate dateOfBirth,
      String gender) {
    if (existingUser != null) {
      existingUser.setFullName(fullName);
      existingUser.setEmail(email);
      existingUser.setAddress(address);
      existingUser.setDateOfBirth(dateOfBirth);
      existingUser.setGender(gender);
      dataStore.notifyListeners();
    } else {
      dataStore.addUser(username, fullName, email, address, dateOfBirth, gender);
    }
  }

  public void addDataChangeListener(Runnable listener) {
    dataStore.addChangeListener(listener);
  }
}
