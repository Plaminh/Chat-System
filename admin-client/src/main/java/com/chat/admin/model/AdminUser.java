package com.chat.admin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a user account and its statistical metrics in the Admin UI (Version 1 Mock Model).
 */
public final class AdminUser {
  private final String id;
  private String username;
  private String fullName;
  private String email;
  private String address;
  private LocalDate dateOfBirth;
  private String gender;
  private LocalDateTime createdAt;
  private boolean locked;
  private String passwordHash;
  private int directFriendCount;
  private int friendsOfFriendsCount;
  private int appOpenCount;
  private int chattedUsersCount;
  private int chattedGroupsCount;

  public AdminUser(
      String id,
      String username,
      String fullName,
      String email,
      String address,
      LocalDate dateOfBirth,
      String gender,
      LocalDateTime createdAt,
      boolean locked,
      int directFriendCount,
      int friendsOfFriendsCount,
      int appOpenCount,
      int chattedUsersCount,
      int chattedGroupsCount) {
    this.id = id;
    this.username = username;
    this.fullName = fullName;
    this.email = email;
    this.address = address;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.createdAt = createdAt;
    this.locked = locked;
    this.passwordHash = "bcrypt$mock$hash";
    this.directFriendCount = directFriendCount;
    this.friendsOfFriendsCount = friendsOfFriendsCount;
    this.appOpenCount = appOpenCount;
    this.chattedUsersCount = chattedUsersCount;
    this.chattedGroupsCount = chattedGroupsCount;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public String getStatusText() {
    return locked ? "Locked" : "Active";
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public int getDirectFriendCount() {
    return directFriendCount;
  }

  public void setDirectFriendCount(int directFriendCount) {
    this.directFriendCount = directFriendCount;
  }

  public int getFriendsOfFriendsCount() {
    return friendsOfFriendsCount;
  }

  public void setFriendsOfFriendsCount(int friendsOfFriendsCount) {
    this.friendsOfFriendsCount = friendsOfFriendsCount;
  }

  public int getAppOpenCount() {
    return appOpenCount;
  }

  public void setAppOpenCount(int appOpenCount) {
    this.appOpenCount = appOpenCount;
  }

  public int getChattedUsersCount() {
    return chattedUsersCount;
  }

  public void setChattedUsersCount(int chattedUsersCount) {
    this.chattedUsersCount = chattedUsersCount;
  }

  public int getChattedGroupsCount() {
    return chattedGroupsCount;
  }

  public void setChattedGroupsCount(int chattedGroupsCount) {
    this.chattedGroupsCount = chattedGroupsCount;
  }
}
