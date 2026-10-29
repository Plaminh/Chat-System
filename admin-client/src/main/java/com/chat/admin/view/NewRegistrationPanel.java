package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * New User Registrations Timeline Panel (Task 7 / Screen 7).
 *
 * <p>Covers Requirement 1.2.5: View newly registered users within a selected date range (from date
 * to date), sort by name or creation timestamp, and filter by name.
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

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField nameSearchField =
      AdminUIStyle.createSearchField("Filter by full name or username...", 18);
  private final JTextField startDateField = AdminUIStyle.createDateField("2025/01/01");
  private final JTextField endDateField = AdminUIStyle.createDateField("2025/12/31");
  private final JComboBox<String> sortCombo =
      AdminUIStyle.createComboBox(
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
  private final JLabel activeDateWindowValueLabel = new JLabel("2025/01/01 → 2025/12/31");
  private final JLabel footerSummaryLabel = new JLabel("Showing 0 newly registered accounts");

  public NewRegistrationPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(AdminUIStyle.CANVAS_BG);

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
        AdminUIStyle.createKpiCard(
            "Registrations in Period",
            matchingRegistrationsValueLabel,
            "New accounts matching selected date window",
            AdminUIStyle.PRIMARY));
    row.add(
        AdminUIStyle.createKpiCard(
            "Active New Accounts",
            activeSignupsValueLabel,
            "Unlocked accounts among filtered signups",
            AdminUIStyle.SUCCESS_DOT));
    row.add(
        AdminUIStyle.createKpiCard(
            "Selected Time Window",
            activeDateWindowValueLabel,
            "Inclusive Start Date → End Date filter",
            new Color(0x6366F1)));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = AdminUIStyle.createCardPanel(new BorderLayout(0, 10));

    // Top row: Date range inputs + quick range presets
    JPanel dateRow = new JPanel(new BorderLayout(10, 4));
    dateRow.setOpaque(false);

    JPanel leftDateControls = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftDateControls.setOpaque(false);

    JLabel startLbl = new JLabel("Start Date:");
    startLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    startLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    JLabel endLbl = new JLabel("End Date:");
    endLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    endLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    leftDateControls.add(startLbl);
    leftDateControls.add(startDateField);
    leftDateControls.add(endLbl);
    leftDateControls.add(endDateField);

    JPanel rightPresets = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.RIGHT, 6, 2));
    rightPresets.setOpaque(false);

    JLabel presetLbl = new JLabel("Quick Presets:");
    presetLbl.setFont(AdminUIStyle.FONT_SMALL);
    presetLbl.setForeground(AdminUIStyle.TEXT_MUTED);

    JButton q1Btn = AdminUIStyle.createSecondaryButton("Q1 2025");
    q1Btn.addActionListener(e -> setDateRange("2025/01/01", "2025/03/31"));

    JButton mayBtn = AdminUIStyle.createSecondaryButton("May 2025");
    mayBtn.addActionListener(e -> setDateRange("2025/05/01", "2025/05/31"));

    JButton fullYearBtn = AdminUIStyle.createSecondaryButton("All 2025–2026");
    fullYearBtn.addActionListener(
        e -> setDateRange("2025/01/01", LocalDate.now().format(AdminUIStyle.DATE_FMT)));

    rightPresets.add(presetLbl);
    rightPresets.add(q1Btn);
    rightPresets.add(mayBtn);
    rightPresets.add(fullYearBtn);

    dateRow.add(leftDateControls, BorderLayout.CENTER);
    dateRow.add(rightPresets, BorderLayout.EAST);

    // Bottom row: Name search + Sort selector + Reset
    JPanel searchSortRow = new JPanel(new BorderLayout(10, 4));
    searchSortRow.setOpaque(false);

    JPanel leftSearch = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftSearch.setOpaque(false);
    JLabel nameLbl = new JLabel("Filter by Name:");
    nameLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    nameLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);
    leftSearch.add(nameLbl);
    leftSearch.add(nameSearchField);

    JPanel rightSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightSort.setOpaque(false);

    JButton resetBtn = AdminUIStyle.createSecondaryButton("Reset All");
    resetBtn.addActionListener(
        e -> {
          nameSearchField.setText("");
          startDateField.setText("2025/01/01");
          endDateField.setText(LocalDate.now().format(AdminUIStyle.DATE_FMT));
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
    card.setBackground(AdminUIStyle.SURFACE_WHITE);

    AdminUIStyle.styleTable(registrationTable);
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
        .setCellRenderer(AdminUIStyle.createStatusBadgeRenderer());

    card.add(AdminUIStyle.wrapTableInScrollPane(registrationTable), BorderLayout.CENTER);
    return card;
  }

  private JPanel buildFooterSummaryBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 0));
    bar.setBackground(AdminUIStyle.SURFACE_WHITE);
    bar.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AdminUIStyle.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(9, 16, 9, 16)));

    footerSummaryLabel.setFont(AdminUIStyle.FONT_BODY_BOLD);
    footerSummaryLabel.setForeground(AdminUIStyle.TEXT_PRIMARY);

    bar.add(footerSummaryLabel, BorderLayout.CENTER);
    return bar;
  }

  private void setDateRange(String start, String end) {
    startDateField.setText(start);
    endDateField.setText(end);
    applyFiltersAndRefresh(false);
  }

  private void wireEvents() {
    DocumentListener docListener =
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
        };
    nameSearchField.getDocument().addDocumentListener(docListener);
    startDateField.getDocument().addDocumentListener(docListener);
    endDateField.getDocument().addDocumentListener(docListener);
    AdminUIStyle.attachDateFieldValidationTriggers(
        startDateField, () -> applyFiltersAndRefresh(true));
    AdminUIStyle.attachDateFieldValidationTriggers(
        endDateField, () -> applyFiltersAndRefresh(true));
    sortCombo.addActionListener(e -> applyFiltersAndRefresh(false));
  }

  private void applyFiltersAndRefresh(boolean showValidationAlert) {
    LocalDate[] dateRange =
        AdminUIStyle.parseAndValidateDateRange(
            this, startDateField, endDateField, showValidationAlert);
    if (dateRange == null) {
      return;
    }
    LocalDate startDate = dateRange[0];
    LocalDate endDate = dateRange[1];
    String startText = startDate == null ? "" : startDate.format(AdminUIStyle.DATE_FMT);
    String endText = endDate == null ? "" : endDate.format(AdminUIStyle.DATE_FMT);

    String query =
        nameSearchField.getText() == null ? "" : nameSearchField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    List<AdminUser> allUsers = dataStore.getUsers();
    List<AdminUser> filtered = new ArrayList<>();

    for (AdminUser user : allUsers) {
      LocalDate createdDate = user.getCreatedAt().toLocalDate();
      if (startDate != null && createdDate.isBefore(startDate)) {
        continue;
      }
      if (endDate != null && createdDate.isAfter(endDate)) {
        continue;
      }
      if (!query.isEmpty()
          && !user.getFullName().toLowerCase().contains(query)
          && !user.getUsername().toLowerCase().contains(query)) {
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
            user.getDateOfBirth().format(AdminUIStyle.DATE_FMT),
            user.getAddress(),
            user.getCreatedAt().format(AdminUIStyle.DATETIME_FMT),
            user.isLocked() ? "Locked" : "Active"
          });
    }

    matchingRegistrationsValueLabel.setText(filtered.size() + " accounts");
    activeSignupsValueLabel.setText(activeCount + " active");
    String windowDisplay =
        (startText.isEmpty() ? "Any" : startText)
            + " → "
            + (endText.isEmpty() ? "Any" : endText);
    activeDateWindowValueLabel.setText(windowDisplay);
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
}
