package com.chat.admin.view;

import com.chat.admin.controller.AdminAuthController;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.network.SocketClient;
import com.chat.common.dto.admin.AdminUserFilter;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.SortDirection;
import com.chat.common.ui.ChatTheme;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * New User Registrations Timeline Panel (Task 7 / Screen 7).
 *
 * <p>Covers Requirement 1.2.5 (Issue #14 [A5]): View newly registered users within a selected date
 * range (from date to date), sort by name or creation timestamp, and filter by name. Includes
 * enhanced UX with dynamic quick presets, active pill highlights, calendar picker popup, and
 * duration indicators.
 */
public final class NewRegistrationPanel extends JPanel {
  private static final String[] COLUMNS = {
    "USERNAME",
    "FULL NAME",
    "EMAIL ADDRESS",
    "GENDER",
    "DATE OF BIRTH",
    "ADDRESS",
    "REGISTERED AT",
    "STATUS"
  };

  private final SocketClient socketClient;
  private final ObjectMapper mapper;
  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();

  private final JTextField nameSearchField =
      ChatTheme.createSearchField("Filter by full name or username...", 18);
  private final DateRangePickerBar dateRangePickerBar =
      new DateRangePickerBar(
          LocalDate.of(2025, 1, 1),
          LocalDate.now(),
          (s, e) -> applyFiltersAndRefresh(false));

  private final JComboBox<String> sortCombo =
      ChatTheme.createComboBox(
          new String[] {
            "Sort: Created Time (Newest First)",
            "Sort: Created Time (Oldest First)",
            "Sort: Full Name (A-Z)",
            "Sort: Full Name (Z-A)"
          });

  private final DefaultTableModel tableModel =
      new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable registrationTable = new JTable(tableModel);

  private final JLabel matchingRegistrationsValueLabel = new JLabel("0 accounts");
  private final JLabel activeSignupsValueLabel = new JLabel("0 active");
  private final JLabel activeDateWindowValueLabel = new JLabel("All Time");
  private final JLabel footerSummaryLabel = new JLabel("Showing 0 newly registered accounts");

  public NewRegistrationPanel() {
    this(AdminAuthController.getSharedSocketClient());
  }

  public NewRegistrationPanel(SocketClient socketClient) {
    this.socketClient = socketClient;
    this.mapper =
        JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    setLayout(new BorderLayout(0, 14));
    setBackground(ChatTheme.CANVAS_BG);

    JPanel topSection = new JPanel(new BorderLayout(0, 12));
    topSection.setOpaque(false);
    topSection.add(buildKpiSummaryRow(), BorderLayout.NORTH);
    topSection.add(buildFilterToolbar(), BorderLayout.SOUTH);

    add(topSection, BorderLayout.NORTH);
    add(buildTableCard(), BorderLayout.CENTER);
    add(buildFooterSummaryBar(), BorderLayout.SOUTH);

    wireEvents();
    dataStore.addChangeListener(() -> applyFiltersAndRefresh(false));
    applyFiltersAndRefresh(false);
  }

  private JPanel buildKpiSummaryRow() {
    JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
    row.setOpaque(false);

    row.add(
        AdminUIComponents.createKpiCard(
            "Registrations in Period",
            matchingRegistrationsValueLabel,
            "New accounts matching selected date window",
            ChatTheme.PRIMARY));
    row.add(
        AdminUIComponents.createKpiCard(
            "Active New Accounts",
            activeSignupsValueLabel,
            "Unlocked accounts among filtered signups",
            ChatTheme.SUCCESS_DOT));
    row.add(
        AdminUIComponents.createKpiCard(
            "Selected Time Window",
            activeDateWindowValueLabel,
            "Inclusive Start Date → End Date filter",
            new Color(0x6366F1)));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = ChatTheme.createCardPanel(new BorderLayout(0, 10));

    // Top row: Unified Date Range Picker Bar (Bar + Presets)
    JPanel dateRow = new JPanel(new BorderLayout(10, 4));
    dateRow.setOpaque(false);
    dateRow.add(dateRangePickerBar, BorderLayout.CENTER);

    // Bottom row: Name search + Sort selector + Reset
    JPanel searchSortRow = new JPanel(new BorderLayout(10, 4));
    searchSortRow.setOpaque(false);

    JPanel leftSearch =
        new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftSearch.setOpaque(false);
    JLabel nameLbl = new JLabel("Filter by Name:");
    nameLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    nameLbl.setForeground(ChatTheme.TEXT_SECONDARY);
    leftSearch.add(nameLbl);
    leftSearch.add(nameSearchField);

    JPanel rightSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightSort.setOpaque(false);

    JButton resetBtn = ChatTheme.createSecondaryButton("Reset All");
    resetBtn.addActionListener(
        e -> {
          nameSearchField.setText("");
          dateRangePickerBar.setDateRange(LocalDate.of(2025, 1, 1), LocalDate.now(), false);
          sortCombo.setSelectedIndex(0);
          applyFiltersAndRefresh(false);
        });

    rightSort.add(sortCombo);
    rightSort.add(resetBtn);

    searchSortRow.add(leftSearch, BorderLayout.CENTER);
    searchSortRow.add(rightSort, BorderLayout.EAST);

    card.add(dateRow, BorderLayout.NORTH);
    card.add(searchSortRow, BorderLayout.SOUTH);
    return card;
  }

  private JPanel buildTableCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(ChatTheme.SURFACE_WHITE);

    AdminUIComponents.styleTable(registrationTable);
    registrationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    registrationTable.getColumnModel().getColumn(0).setPreferredWidth(115);
    registrationTable.getColumnModel().getColumn(1).setPreferredWidth(140);
    registrationTable.getColumnModel().getColumn(2).setPreferredWidth(185);
    registrationTable.getColumnModel().getColumn(3).setPreferredWidth(75);
    registrationTable.getColumnModel().getColumn(4).setPreferredWidth(105);
    registrationTable.getColumnModel().getColumn(5).setPreferredWidth(155);
    registrationTable.getColumnModel().getColumn(6).setPreferredWidth(145);
    registrationTable.getColumnModel().getColumn(7).setPreferredWidth(95);
    registrationTable
        .getColumnModel()
        .getColumn(7)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    card.add(AdminUIComponents.wrapTableInScrollPane(registrationTable), BorderLayout.CENTER);
    registrationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    return card;
  }

  private JPanel buildFooterSummaryBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 0));
    bar.setBackground(ChatTheme.SURFACE_WHITE);
    bar.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ChatTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(9, 16, 9, 16)));

    footerSummaryLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    footerSummaryLabel.setForeground(ChatTheme.TEXT_PRIMARY);

    bar.add(footerSummaryLabel, BorderLayout.CENTER);
    return bar;
  }

  public void setDateRange(String start, String end) {
    LocalDate s = parseDateOrNull(start);
    LocalDate e = parseDateOrNull(end);
    dateRangePickerBar.setDateRange(s, e, true);
  }

  private void wireEvents() {
    nameSearchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                applyFiltersAndRefresh(false);
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                applyFiltersAndRefresh(false);
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                applyFiltersAndRefresh(false);
              }
            });
    sortCombo.addActionListener(e -> applyFiltersAndRefresh(false));
  }

  public void applyFiltersAndRefresh(boolean showValidationAlert) {
    LocalDate rawStart = dateRangePickerBar.getStartDate();
    LocalDate rawEnd = dateRangePickerBar.getEndDate();
    final LocalDate startDate =
        (rawStart != null && rawEnd != null && rawStart.isAfter(rawEnd)) ? rawEnd : rawStart;
    final LocalDate endDate =
        (rawStart != null && rawEnd != null && rawStart.isAfter(rawEnd)) ? rawStart : rawEnd;
    String startText = startDate == null ? "" : startDate.format(ChatTheme.DATE_FMT);
    String endText = endDate == null ? "" : endDate.format(ChatTheme.DATE_FMT);

    String query =
        nameSearchField.getText() == null ? "" : nameSearchField.getText().trim();
    int sortIdx = sortCombo.getSelectedIndex();

    if (socketClient != null && socketClient.isConnected()) {
      SortSpec sortSpec =
          switch (sortIdx) {
            case 1 -> new SortSpec("createdAt", SortDirection.ASC);
            case 2 -> new SortSpec("name", SortDirection.ASC);
            case 3 -> new SortSpec("name", SortDirection.DESC);
            default -> new SortSpec("createdAt", SortDirection.DESC);
          };
      DateRange range =
          (startDate != null && endDate != null) ? new DateRange(startDate, endDate) : null;
      AdminUserFilter filter =
          new AdminUserFilter(
              query.isEmpty() ? null : query,
              null,
              range,
              sortSpec,
              new PageRequest(0, 100));

      socketClient
          .sendAsync(ActionType.ADMIN_LIST_USERS, filter)
          .thenAccept(
              response -> {
                SwingUtilities.invokeLater(
                    () -> {
                      if (response != null && response.success() && response.data() != null) {
                        try {
                          PageResult<AdminUserInfo> pageResult =
                              mapper.readValue(
                                  mapper.treeAsTokens(response.data()),
                                  new TypeReference<>() {});
                          populateFromPageResult(pageResult, startDate, endDate, startText, endText);
                        } catch (Exception ex) {
                          populateFromMockStore(
                              query, startDate, endDate, sortIdx, startText, endText);
                        }
                      } else {
                        populateFromMockStore(
                            query, startDate, endDate, sortIdx, startText, endText);
                      }
                    });
              })
          .exceptionally(
              ex -> {
                SwingUtilities.invokeLater(
                    () ->
                        populateFromMockStore(
                            query, startDate, endDate, sortIdx, startText, endText));
                return null;
              });
    } else {
      populateFromMockStore(query, startDate, endDate, sortIdx, startText, endText);
    }
  }

  private void populateFromPageResult(
      PageResult<AdminUserInfo> pageResult,
      LocalDate startDate,
      LocalDate endDate,
      String startText,
      String endText) {
    tableModel.setRowCount(0);
    long activeCount = 0;
    for (AdminUserInfo info : pageResult.items()) {
      var acc = info.account();
      var summary = acc.summary();
      boolean isActive = summary.accountStatus() == AccountStatus.ACTIVE;
      if (isActive) {
        activeCount++;
      }
      String genderText = acc.gender() != null ? acc.gender().name() : "N/A";
      String dobText =
          acc.dateOfBirth() != null ? acc.dateOfBirth().format(ChatTheme.DATE_FMT) : "N/A";
      String createdText =
          acc.createdAt() != null ? acc.createdAt().format(ChatTheme.DATETIME_FMT) : "N/A";
      tableModel.addRow(
          new Object[] {
            "@" + summary.username(),
            summary.fullName(),
            acc.email(),
            genderText,
            dobText,
            acc.address(),
            createdText,
            isActive ? "Active" : "Locked"
          });
    }

    matchingRegistrationsValueLabel.setText(pageResult.totalItems() + " accounts");
    activeSignupsValueLabel.setText(activeCount + " active");

    String durationText = "";
    if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
      long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
      durationText = " (" + days + " ngày)";
    }

    String windowDisplay =
        (startText.isEmpty() ? "Any" : startText)
            + " → "
            + (endText.isEmpty() ? "Any" : endText)
            + durationText;
    activeDateWindowValueLabel.setText(windowDisplay);
    activeDateWindowValueLabel.setForeground(new Color(0x6366F1));

    footerSummaryLabel.setText(
        "Showing "
            + pageResult.items().size()
            + " newly registered account(s) (total "
            + pageResult.totalItems()
            + " in period)");

    if (tableModel.getRowCount() > 0) {
      registrationTable.setRowSelectionInterval(0, 0);
    }
  }

  private void populateFromMockStore(
      String query,
      LocalDate startDate,
      LocalDate endDate,
      int sortIdx,
      String startText,
      String endText) {
    List<AdminUser> allUsers = dataStore.getUsers();
    List<AdminUser> filtered = new ArrayList<>();
    String lowerQuery = query == null ? "" : query.toLowerCase();

    for (AdminUser user : allUsers) {
      LocalDate createdDate = user.getCreatedAt().toLocalDate();
      if (startDate != null && createdDate.isBefore(startDate)) {
        continue;
      }
      if (endDate != null && createdDate.isAfter(endDate)) {
        continue;
      }
      if (!lowerQuery.isEmpty()
          && !user.getFullName().toLowerCase().contains(lowerQuery)
          && !user.getUsername().toLowerCase().contains(lowerQuery)) {
        continue;
      }
      filtered.add(user);
    }

    Comparator<AdminUser> comparator =
        switch (sortIdx) {
          case 1 -> Comparator.comparing(AdminUser::getCreatedAt);
          case 2 -> Comparator.comparing(u -> u.getFullName().toLowerCase());
          case 3 ->
              Comparator.comparing((AdminUser u) -> u.getFullName().toLowerCase()).reversed();
          default -> Comparator.comparing(AdminUser::getCreatedAt).reversed();
        };
    filtered.sort(comparator);

    tableModel.setRowCount(0);
    long activeCount = 0;
    for (AdminUser user : filtered) {
      if (!user.isLocked()) {
        activeCount++;
      }
      tableModel.addRow(
          new Object[] {
            "@" + user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getGender(),
            user.getDateOfBirth().format(ChatTheme.DATE_FMT),
            user.getAddress(),
            user.getCreatedAt().format(ChatTheme.DATETIME_FMT),
            user.isLocked() ? "Locked" : "Active"
          });
    }

    matchingRegistrationsValueLabel.setText(filtered.size() + " accounts");
    activeSignupsValueLabel.setText(activeCount + " active");

    String durationText = "";
    if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
      long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
      durationText = " (" + days + " ngày)";
    }

    String windowDisplay =
        (startText.isEmpty() ? "Any" : startText)
            + " → "
            + (endText.isEmpty() ? "Any" : endText)
            + durationText;
    activeDateWindowValueLabel.setText(windowDisplay);
    activeDateWindowValueLabel.setForeground(new Color(0x6366F1));

    footerSummaryLabel.setText(
        "Showing "
            + filtered.size()
            + " newly registered account(s) out of "
            + allUsers.size()
            + " total users");

    if (tableModel.getRowCount() > 0) {
      registrationTable.setRowSelectionInterval(0, 0);
    }
  }

  private static LocalDate parseDateOrNull(String text) {
    if (text == null || text.isBlank() || text.contains("_")) {
      return null;
    }
    try {
      return LocalDate.parse(text.trim(), ChatTheme.DATE_FMT);
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }

  public JTable getRegistrationTable() {
    return registrationTable;
  }

  public String getMatchingRegistrationsLabelText() {
    return matchingRegistrationsValueLabel.getText();
  }

  public String getActiveSignupsLabelText() {
    return activeSignupsValueLabel.getText();
  }
}
