package com.chat.admin.view;

import com.chat.admin.controller.AdminAuthController;
import com.chat.admin.network.SocketClient;
import com.chat.common.dto.ResponseEnvelope;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
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

  /** Registration times are shown in Vietnam time, the zone the server uses for date filters. */
  static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  static final int PAGE_SIZE = 50;
  private static final long RESPONSE_TIMEOUT_SECONDS = 5;

  private final SocketClient socketClient;
  private final ObjectMapper mapper;
  private int currentPage = 0;
  private int totalPages = 1;
  private int requestSeq = 0;
  private final JLabel pageInfoLabel = new JLabel("Page 1 of 1");
  private final JButton prevPageBtn = ChatTheme.createSecondaryButton("◀ Prev");
  private final JButton nextPageBtn = ChatTheme.createSecondaryButton("Next ▶");
  private final JButton refreshBtn = ChatTheme.createSecondaryButton("Refresh");
  private final JLabel statusLabel = new JLabel(" ");

  private final JTextField nameSearchField =
      ChatTheme.createSearchField("Filter by full name or username...", 18);
  private final DateRangePickerBar dateRangePickerBar =
      new DateRangePickerBar(
          LocalDate.of(2025, 1, 1), LocalDate.now(), (s, e) -> reloadFromFirstPage());

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
          reloadFromFirstPage();
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
    statusLabel.setFont(ChatTheme.FONT_SMALL);
    statusLabel.setForeground(ChatTheme.DANGER_TEXT);

    JPanel summary = new JPanel(new BorderLayout(0, 2));
    summary.setOpaque(false);
    summary.add(footerSummaryLabel, BorderLayout.NORTH);
    summary.add(statusLabel, BorderLayout.SOUTH);

    pageInfoLabel.setFont(ChatTheme.FONT_SMALL);
    pageInfoLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    prevPageBtn.setEnabled(false);
    nextPageBtn.setEnabled(false);
    JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    pageControls.setOpaque(false);
    pageControls.add(refreshBtn);
    pageControls.add(pageInfoLabel);
    pageControls.add(prevPageBtn);
    pageControls.add(nextPageBtn);

    bar.add(summary, BorderLayout.CENTER);
    bar.add(pageControls, BorderLayout.EAST);
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
                reloadFromFirstPage();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                reloadFromFirstPage();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                reloadFromFirstPage();
              }
            });
    sortCombo.addActionListener(e -> reloadFromFirstPage());
    refreshBtn.addActionListener(e -> applyFiltersAndRefresh(false));
    prevPageBtn.addActionListener(
        e -> {
          if (currentPage > 0) {
            currentPage--;
            applyFiltersAndRefresh(false);
          }
        });
    nextPageBtn.addActionListener(
        e -> {
          if (currentPage < totalPages - 1) {
            currentPage++;
            applyFiltersAndRefresh(false);
          }
        });
  }

  /** A filter, sort or date change starts again from the first page. */
  private void reloadFromFirstPage() {
    currentPage = 0;
    applyFiltersAndRefresh(false);
  }

  public void applyFiltersAndRefresh(boolean showValidationAlert) {
    final LocalDate startDate = dateRangePickerBar.getStartDate();
    final LocalDate endDate = dateRangePickerBar.getEndDate();
    int seq = ++requestSeq; // a late response to an older request is ignored
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      showError("The start date must not be after the end date.");
      return;
    }
    if (socketClient == null || !socketClient.isConnected()) {
      showError("Not connected to the server.");
      return;
    }

    String query = nameSearchField.getText() == null ? "" : nameSearchField.getText().trim();
    SortSpec sortSpec =
        switch (sortCombo.getSelectedIndex()) {
          case 1 -> new SortSpec("createdAt", SortDirection.ASC);
          case 2 -> new SortSpec("name", SortDirection.ASC);
          case 3 -> new SortSpec("name", SortDirection.DESC);
          default -> new SortSpec("createdAt", SortDirection.DESC);
        };
    DateRange range =
        (startDate != null && endDate != null) ? new DateRange(startDate, endDate) : null;
    String nameQuery = query.isEmpty() ? null : query;
    AdminUserFilter filter =
        new AdminUserFilter(
            nameQuery, null, range, sortSpec, new PageRequest(currentPage, PAGE_SIZE));
    // The active count covers the whole period, not only the page shown.
    AdminUserFilter activeFilter =
        new AdminUserFilter(
            nameQuery, AccountStatus.ACTIVE, range, sortSpec, new PageRequest(0, 1));

    socketClient
        .sendAsync(ActionType.ADMIN_LIST_USERS, filter)
        .orTimeout(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .whenComplete(
            (response, ex) ->
                SwingUtilities.invokeLater(
                    () -> {
                      if (seq != requestSeq) {
                        return;
                      }
                      if (ex != null) {
                        showError("The server did not respond. Please try again.");
                        return;
                      }
                      PageResult<AdminUserInfo> pageResult = readPage(response);
                      if (pageResult == null) {
                        showError("Could not load new registrations: " + failureReason(response));
                        return;
                      }
                      populateFromPageResult(pageResult, startDate, endDate);
                    }));

    socketClient
        .sendAsync(ActionType.ADMIN_LIST_USERS, activeFilter)
        .orTimeout(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .whenComplete(
            (response, ex) ->
                SwingUtilities.invokeLater(
                    () -> {
                      if (seq != requestSeq) {
                        return;
                      }
                      PageResult<AdminUserInfo> activePage = ex == null ? readPage(response) : null;
                      activeSignupsValueLabel.setText(
                          activePage == null ? "—" : activePage.totalItems() + " active");
                    }));
  }

  private PageResult<AdminUserInfo> readPage(ResponseEnvelope response) {
    if (response == null || !response.success() || response.data() == null) {
      return null;
    }
    try {
      return mapper.readValue(mapper.treeAsTokens(response.data()), new TypeReference<>() {});
    } catch (Exception ex) {
      return null;
    }
  }

  private static String failureReason(ResponseEnvelope response) {
    if (response == null) {
      return "no response";
    }
    if (response.success()) {
      return "the server returned data that could not be read";
    }
    return response.message() != null ? response.message() : String.valueOf(response.errorCode());
  }

  private void populateFromPageResult(
      PageResult<AdminUserInfo> pageResult, LocalDate startDate, LocalDate endDate) {
    tableModel.setRowCount(0);
    for (AdminUserInfo info : pageResult.items()) {
      var acc = info.account();
      var summary = acc.summary();
      String genderText = acc.gender() != null ? acc.gender().name() : "N/A";
      String dobText =
          acc.dateOfBirth() != null ? acc.dateOfBirth().format(ChatTheme.DATE_FMT) : "N/A";
      tableModel.addRow(
          new Object[] {
            "@" + summary.username(),
            summary.fullName(),
            acc.email(),
            genderText,
            dobText,
            acc.address(),
            formatRegisteredAt(acc.createdAt()),
            statusText(summary.accountStatus())
          });
    }

    matchingRegistrationsValueLabel.setText(pageResult.totalItems() + " accounts");
    activeDateWindowValueLabel.setText(windowText(startDate, endDate));
    activeDateWindowValueLabel.setForeground(new Color(0x6366F1));

    totalPages = Math.max(1, pageResult.totalPages());
    footerSummaryLabel.setText(
        "Showing "
            + pageResult.items().size()
            + " of "
            + pageResult.totalItems()
            + " newly registered account(s) in the period");
    pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
    prevPageBtn.setEnabled(currentPage > 0);
    nextPageBtn.setEnabled(currentPage < totalPages - 1);
    statusLabel.setText(" ");

    if (tableModel.getRowCount() > 0) {
      registrationTable.setRowSelectionInterval(0, 0);
    }
  }

  /** Shows an error instead of data; no sample accounts are shown in place of real ones. */
  private void showError(String message) {
    tableModel.setRowCount(0);
    matchingRegistrationsValueLabel.setText("0 accounts");
    activeSignupsValueLabel.setText("0 active");
    activeDateWindowValueLabel.setText(
        windowText(dateRangePickerBar.getStartDate(), dateRangePickerBar.getEndDate()));
    footerSummaryLabel.setText("Showing 0 newly registered accounts");
    currentPage = 0;
    totalPages = 1;
    pageInfoLabel.setText("Page 1 of 1");
    prevPageBtn.setEnabled(false);
    nextPageBtn.setEnabled(false);
    statusLabel.setText(message);
  }

  private static String windowText(LocalDate startDate, LocalDate endDate) {
    String durationText = "";
    if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
      long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
      durationText = " (" + days + " ngày)";
    }
    return (startDate == null ? "Any" : startDate.format(ChatTheme.DATE_FMT))
        + " → "
        + (endDate == null ? "Any" : endDate.format(ChatTheme.DATE_FMT))
        + durationText;
  }

  /** Formats a registration time in {@link #DISPLAY_ZONE}, whatever offset the server sent. */
  static String formatRegisteredAt(OffsetDateTime time) {
    return time == null
        ? "N/A"
        : time.atZoneSameInstant(DISPLAY_ZONE).format(ChatTheme.DATETIME_FMT);
  }

  static String statusText(AccountStatus status) {
    if (status == null) {
      return "N/A";
    }
    return switch (status) {
      case ACTIVE -> "Active";
      case LOCKED -> "Locked";
      case DELETED -> "Deleted";
    };
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

  String getStatusText() {
    return statusLabel.getText();
  }

  String getPageInfoText() {
    return pageInfoLabel.getText();
  }
}
