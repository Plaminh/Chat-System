package com.chat.admin.model;

import com.chat.admin.model.GroupChatInfo.GroupMemberInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized in-memory mock data repository for the Admin Client UI (Version 1).
 *
 * <p>Provides realistic, interconnected datasets across all 9 administrative functional modules
 * without connecting to PostgreSQL or requiring live TCP business endpoints.
 */
public final class AdminMockDataStore {
  private static final AdminMockDataStore INSTANCE = new AdminMockDataStore();

  private final List<AdminUser> users = new ArrayList<>();
  private final List<LoginRecord> loginRecords = new ArrayList<>();
  private final Map<String, List<FriendConnection>> friendConnectionsByUser = new HashMap<>();
  private final List<GroupChatInfo> groups = new ArrayList<>();
  private final List<SpamReportRecord> spamReports = new ArrayList<>();
  private final Map<Integer, int[]> monthlyRegistrationsByYear = new HashMap<>();
  private final Map<Integer, int[]> monthlyActiveUsersByYear = new HashMap<>();
  private final List<Runnable> changeListeners = new ArrayList<>();
  private int nextUserSeq = 1013;

  private AdminMockDataStore() {
    seedUsers();
    seedLoginRecords();
    seedFriendConnections();
    seedGroups();
    seedSpamReports();
    seedAnnualChartMetrics();
  }

  public static AdminMockDataStore getInstance() {
    return INSTANCE;
  }

  public void addChangeListener(Runnable listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public void notifyListeners() {
    for (Runnable listener : changeListeners) {
      listener.run();
    }
  }

  public List<AdminUser> getUsers() {
    return new ArrayList<>(users);
  }

  public Optional<AdminUser> findUserByUsername(String username) {
    if (username == null) {
      return Optional.empty();
    }
    String normalized = username.startsWith("@") ? username.substring(1) : username;
    return users.stream()
        .filter(u -> u.getUsername().equalsIgnoreCase(normalized))
        .findFirst();
  }

  public AdminUser addUser(
      String username,
      String fullName,
      String email,
      String address,
      LocalDate dateOfBirth,
      String gender) {
    String id = "USR-" + (nextUserSeq++);
    AdminUser created =
        new AdminUser(
            id,
            username.trim(),
            fullName.trim(),
            email.trim(),
            address.trim(),
            dateOfBirth,
            gender,
            LocalDateTime.now().withSecond(0).withNano(0),
            false,
            0,
            0,
            1,
            0,
            0);
    users.add(0, created);
    loginRecords.add(0, new LoginRecord(LocalDateTime.now(), created.getUsername(), fullName));
    friendConnectionsByUser.put(created.getUsername(), new ArrayList<>());
    notifyListeners();
    return created;
  }

  public boolean deleteUser(String username) {
    boolean removed = users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    if (removed) {
      notifyListeners();
    }
    return removed;
  }

  public boolean toggleUserLock(String username) {
    Optional<AdminUser> target = findUserByUsername(username);
    if (target.isPresent()) {
      AdminUser user = target.get();
      user.setLocked(!user.isLocked());
      notifyListeners();
      return user.isLocked();
    }
    return false;
  }

  public void lockUserAccount(String username) {
    findUserByUsername(username).ifPresent(user -> user.setLocked(true));
    for (SpamReportRecord report : spamReports) {
      String reported =
          report.getReportedUsername().startsWith("@")
              ? report.getReportedUsername().substring(1)
              : report.getReportedUsername();
      String cleanTarget = username.startsWith("@") ? username.substring(1) : username;
      if (reported.equalsIgnoreCase(cleanTarget)) {
        report.setStatus("Account Locked");
      }
    }
    notifyListeners();
  }

  public List<LoginRecord> getAllLoginRecordsChronological() {
    List<LoginRecord> copy = new ArrayList<>(loginRecords);
    copy.sort(Comparator.comparing(LoginRecord::timestamp).reversed());
    return copy;
  }

  public List<LoginRecord> getLoginRecordsForUser(String username) {
    String clean = username.startsWith("@") ? username.substring(1) : username;
    return loginRecords.stream()
        .filter(r -> r.username().equalsIgnoreCase(clean))
        .sorted(Comparator.comparing(LoginRecord::timestamp).reversed())
        .toList();
  }

  public List<FriendConnection> getFriendsForUser(String username) {
    String clean = username.startsWith("@") ? username.substring(1) : username;
    return friendConnectionsByUser.getOrDefault(clean, List.of());
  }

  public List<GroupChatInfo> getGroups() {
    return new ArrayList<>(groups);
  }

  public List<SpamReportRecord> getSpamReports() {
    return new ArrayList<>(spamReports);
  }

  public int[] getMonthlyRegistrations(int year) {
    return monthlyRegistrationsByYear
        .getOrDefault(year, monthlyRegistrationsByYear.get(2025))
        .clone();
  }

  public int[] getMonthlyActiveUsers(int year) {
    return monthlyActiveUsersByYear
        .getOrDefault(year, monthlyActiveUsersByYear.get(2025))
        .clone();
  }

  private void seedUsers() {
    users.add(
        new AdminUser(
            "USR-1001",
            "alex_turner",
            "Alex Turner",
            "alex.turner@univ.edu",
            "Quận 1, TP.HCM",
            LocalDate.of(1999, 4, 12),
            "Male",
            LocalDateTime.of(2025, 1, 15, 9, 20),
            false,
            18,
            142,
            148,
            34,
            8));
    users.add(
        new AdminUser(
            "USR-1002",
            "sarah_c",
            "Sarah Connor",
            "sconnor@techlab.io",
            "Quận 3, TP.HCM",
            LocalDate.of(1998, 11, 23),
            "Female",
            LocalDateTime.of(2025, 1, 18, 14, 10),
            false,
            42,
            380,
            132,
            29,
            6));
    users.add(
        new AdminUser(
            "USR-1003",
            "marcus_v",
            "Marcus Vance",
            "mvance@cyber.org",
            "Cầu Giấy, Hà Nội",
            LocalDate.of(2001, 2, 9),
            "Male",
            LocalDateTime.of(2025, 2, 2, 11, 5),
            false,
            14,
            96,
            94,
            18,
            4));
    users.add(
        new AdminUser(
            "USR-1004",
            "spammer99",
            "Spammer Bot 99",
            "spam99@tempmail.xyz",
            "Hải Châu, Đà Nẵng",
            LocalDate.of(2005, 9, 30),
            "Other",
            LocalDateTime.of(2025, 2, 14, 16, 40),
            true,
            2,
            8,
            15,
            12,
            1));
    users.add(
        new AdminUser(
            "USR-1005",
            "elena_r",
            "Elena Rostova",
            "elena.r@design.net",
            "Thủ Đức, TP.HCM",
            LocalDate.of(2000, 7, 14),
            "Female",
            LocalDateTime.of(2025, 2, 20, 10, 15),
            false,
            16,
            128,
            88,
            16,
            3));
    users.add(
        new AdminUser(
            "USR-1006",
            "david_k",
            "David Kim",
            "dkim@student.edu",
            "Quận 7, TP.HCM",
            LocalDate.of(2002, 5, 19),
            "Male",
            LocalDateTime.of(2025, 3, 1, 8, 50),
            false,
            12,
            84,
            76,
            12,
            3));
    users.add(
        new AdminUser(
            "USR-1007",
            "bot_crypto",
            "Crypto Promo Bot",
            "bot@coinpump.biz",
            "Ninh Kiều, Cần Thơ",
            LocalDate.of(2004, 12, 1),
            "Other",
            LocalDateTime.of(2025, 3, 5, 19, 30),
            true,
            1,
            4,
            19,
            15,
            2));
    users.add(
        new AdminUser(
            "USR-1008",
            "clara_m",
            "Clara Morales",
            "cmorales@student.edu",
            "Bình Thạnh, TP.HCM",
            LocalDate.of(2003, 8, 25),
            "Female",
            LocalDateTime.of(2025, 5, 10, 13, 21),
            false,
            11,
            72,
            52,
            9,
            2));
    users.add(
        new AdminUser(
            "USR-1009",
            "kevin_z",
            "Kevin Zhao",
            "kzhao@polytech.edu",
            "Đống Đa, Hà Nội",
            LocalDate.of(2001, 6, 17),
            "Male",
            LocalDateTime.of(2025, 5, 12, 11, 50),
            false,
            9,
            54,
            41,
            7,
            2));
    users.add(
        new AdminUser(
            "USR-1010",
            "julian_b",
            "Julian Barnes",
            "jbarnes@campus.edu",
            "Quận 10, TP.HCM",
            LocalDate.of(2000, 1, 30),
            "Male",
            LocalDateTime.of(2025, 5, 15, 14, 2),
            false,
            26,
            210,
            64,
            14,
            5));
    users.add(
        new AdminUser(
            "USR-1011",
            "maya_lin",
            "Maya Lin",
            "mlin@design.univ.edu",
            "Tân Bình, TP.HCM",
            LocalDate.of(2002, 10, 8),
            "Female",
            LocalDateTime.of(2025, 5, 17, 22, 40),
            false,
            15,
            110,
            47,
            11,
            4));
    users.add(
        new AdminUser(
            "USR-1012",
            "prof_johnson",
            "Alan Johnson",
            "ajohnson@faculty.edu",
            "Quận 5, TP.HCM",
            LocalDate.of(1985, 3, 14),
            "Male",
            LocalDateTime.of(2025, 1, 5, 8, 0),
            false,
            38,
            340,
            115,
            25,
            7));
  }

  private void seedLoginRecords() {
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 18, 14, 18, 22), "alex_turner", "Alex Turner"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 18, 13, 55, 10), "sarah_c", "Sarah Connor"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 18, 12, 40, 5), "prof_johnson", "Alan Johnson"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 18, 11, 15, 44), "clara_m", "Clara Morales"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 17, 19, 11, 4), "alex_turner", "Alex Turner"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 17, 16, 30, 12), "marcus_v", "Marcus Vance"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 17, 9, 42, 15), "alex_turner", "Alex Turner"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 16, 20, 5, 33), "elena_r", "Elena Rostova"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 16, 15, 22, 19), "david_k", "David Kim"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 15, 11, 5, 48), "alex_turner", "Alex Turner"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 15, 8, 19, 0), "julian_b", "Julian Barnes"));
    loginRecords.add(
        new LoginRecord(LocalDateTime.of(2025, 5, 14, 21, 45, 11), "maya_lin", "Maya Lin"));
  }

  private void seedFriendConnections() {
    List<FriendConnection> alexFriends =
        List.of(
            new FriendConnection("sarah_c", "Sarah Connor", 8, true),
            new FriendConnection("elena_r", "Elena Rostova", 12, true),
            new FriendConnection("david_k", "David Kim", 5, false),
            new FriendConnection("prof_johnson", "Alan Johnson", 9, true),
            new FriendConnection("marcus_v", "Marcus Vance", 4, false),
            new FriendConnection("clara_m", "Clara Morales", 6, true));
    friendConnectionsByUser.put("alex_turner", new ArrayList<>(alexFriends));

    List<FriendConnection> sarahFriends =
        List.of(
            new FriendConnection("alex_turner", "Alex Turner", 8, true),
            new FriendConnection("prof_johnson", "Alan Johnson", 14, true),
            new FriendConnection("julian_b", "Julian Barnes", 7, true),
            new FriendConnection("maya_lin", "Maya Lin", 5, false));
    friendConnectionsByUser.put("sarah_c", new ArrayList<>(sarahFriends));

    for (AdminUser user : users) {
      friendConnectionsByUser.putIfAbsent(
          user.getUsername(),
          List.of(
              new FriendConnection("alex_turner", "Alex Turner", 3, true),
              new FriendConnection("sarah_c", "Sarah Connor", 5, true)));
    }
  }

  private void seedGroups() {
    groups.add(
        new GroupChatInfo(
            "GRP-001",
            "Algorithms & Data Structures",
            LocalDate.of(2025, 1, 10),
            List.of(
                new GroupMemberInfo("prof_johnson", "Alan Johnson", true),
                new GroupMemberInfo("sarah_c", "Sarah Connor", true),
                new GroupMemberInfo("alex_turner", "Alex Turner", true),
                new GroupMemberInfo("marcus_v", "Marcus Vance", false),
                new GroupMemberInfo("elena_r", "Elena Rostova", false),
                new GroupMemberInfo("david_k", "David Kim", false))));
    groups.add(
        new GroupChatInfo(
            "GRP-002",
            "Computer Science Club 2025",
            LocalDate.of(2025, 1, 12),
            List.of(
                new GroupMemberInfo("sarah_c", "Sarah Connor", true),
                new GroupMemberInfo("julian_b", "Julian Barnes", true),
                new GroupMemberInfo("clara_m", "Clara Morales", false),
                new GroupMemberInfo("kevin_z", "Kevin Zhao", false),
                new GroupMemberInfo("maya_lin", "Maya Lin", false))));
    groups.add(
        new GroupChatInfo(
            "GRP-003",
            "Java Swing & Desktop Devs",
            LocalDate.of(2025, 2, 1),
            List.of(
                new GroupMemberInfo("alex_turner", "Alex Turner", true),
                new GroupMemberInfo("david_k", "David Kim", true),
                new GroupMemberInfo("marcus_v", "Marcus Vance", false),
                new GroupMemberInfo("kevin_z", "Kevin Zhao", false))));
    groups.add(
        new GroupChatInfo(
            "GRP-004",
            "Campus Study & Exam Prep",
            LocalDate.of(2025, 2, 18),
            List.of(
                new GroupMemberInfo("elena_r", "Elena Rostova", true),
                new GroupMemberInfo("clara_m", "Clara Morales", false),
                new GroupMemberInfo("maya_lin", "Maya Lin", false))));
    groups.add(
        new GroupChatInfo(
            "GRP-005",
            "Senior Capstone Project Team A",
            LocalDate.of(2025, 3, 5),
            List.of(
                new GroupMemberInfo("prof_johnson", "Alan Johnson", true),
                new GroupMemberInfo("alex_turner", "Alex Turner", false),
                new GroupMemberInfo("sarah_c", "Sarah Connor", false),
                new GroupMemberInfo("david_k", "David Kim", false))));
  }

  private void seedSpamReports() {
    spamReports.add(
        new SpamReportRecord(
            "REP-8041",
            "alex_turner",
            "spammer99",
            "Bulk phishing links: 'Claim free $500 campus bookstore voucher'",
            LocalDateTime.of(2025, 5, 18, 13, 45, 10),
            "Account Locked"));
    spamReports.add(
        new SpamReportRecord(
            "REP-8039",
            "sarah_c",
            "bot_crypto",
            "Unsolicited promo: 'Join our 100x crypto pump signal group right now'",
            LocalDateTime.of(2025, 5, 18, 12, 20, 4),
            "Account Locked"));
    spamReports.add(
        new SpamReportRecord(
            "REP-8032",
            "david_k",
            "marcus_v",
            "Repeated advertising messages in study group conversation",
            LocalDateTime.of(2025, 5, 16, 10, 14, 55),
            "Pending"));
    spamReports.add(
        new SpamReportRecord(
            "REP-8015",
            "clara_m",
            "kevin_z",
            "Spamming external commercial links in direct chat",
            LocalDateTime.of(2025, 5, 12, 18, 2, 11),
            "Pending"));
  }

  private void seedAnnualChartMetrics() {
    monthlyRegistrationsByYear.put(
        2024, new int[] {42, 50, 64, 78, 95, 88, 110, 130, 105, 92, 84, 76});
    monthlyRegistrationsByYear.put(
        2025, new int[] {58, 72, 95, 110, 145, 130, 160, 180, 140, 125, 112, 98});
    monthlyRegistrationsByYear.put(
        2026, new int[] {80, 94, 118, 136, 162, 154, 175, 198, 165, 148, 132, 120});

    monthlyActiveUsersByYear.put(
        2024, new int[] {260, 310, 420, 510, 680, 640, 790, 890, 760, 690, 610, 540});
    monthlyActiveUsersByYear.put(
        2025, new int[] {340, 440, 580, 720, 1020, 940, 1140, 1240, 1080, 920, 800, 680});
    monthlyActiveUsersByYear.put(
        2026, new int[] {520, 610, 740, 890, 1180, 1120, 1310, 1420, 1260, 1150, 980, 860});
  }
}
