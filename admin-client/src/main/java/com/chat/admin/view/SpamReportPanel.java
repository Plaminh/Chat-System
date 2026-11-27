package com.chat.admin.view;

import com.chat.common.ui.ChatTheme;
import com.chat.admin.controller.SpamModerationController;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.model.SpamReportRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Spam Reports Moderation Panel (Task 6 / Screen 6).
 *
 * <p>Covers Requirement 1.2.4: View spam reports list, sort by report time or username, filter by
 * date range, filter by username, and lock the offending user account directly.
 */
public final class SpamReportPanel extends JPanel {
  private static final String[] COLUMNS = {
    "REPORT ID",
    "REPORTING USER",
    "REPORTED USER",
    "REASON / MESSAGE SNIPPET",
    "REPORT TIMESTAMP",
    "MODERATION STATUS"
  };

  private final SpamModerationController controller = new SpamModerationController();
  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField usernameFilterField =
      ChatTheme.createSearchField("Filter by reported or reporting username...", 15);
  private final DateRangePickerBar dateRangePickerBar =
      new DateRangePickerBar(
          LocalDate.of(2025, 1, 1),
          LocalDate.now(),
          (s, e) -> applyFiltersAndRefresh(false));
  private final JComboBox<String> sortCombo =
      ChatTheme.createComboBox(
          new String[] {
            "Sort: Time (Newest First)",
            "Sort: Time (Oldest First)",
            "Sort: Reported Username (A-Z)",
            "Sort: Reported Username (Z-A)"
          });

  private final DefaultTableModel tableModel =
      new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable reportTable = new JTable(tableModel);

  private final JLabel openReportsValueLabel = new JLabel("0");
  private final JLabel lockedOffendersValueLabel = new JLabel("0");
  private final JLabel selectedIncidentLabel =
      new JLabel("SELECTED INCIDENT: None");
  private final JButton lockOffenderButton =
      ChatTheme.createDangerButton("Lock Offending Account");

  public SpamReportPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(ChatTheme.CANVAS_BG);

    JPanel topSection = new JPanel(new BorderLayout(0, 12));
    topSection.setOpaque(false);
    topSection.add(buildKpiSummaryRow(), BorderLayout.NORTH);
    topSection.add(buildFilterToolbar(), BorderLayout.SOUTH);

    add(topSection, BorderLayout.NORTH);
    add(buildReportsTableCard(), BorderLayout.CENTER);
    add(buildBottomModerationBar(), BorderLayout.SOUTH);

    wireEvents();
    controller.addDataChangeListener(() -> applyFiltersAndRefresh(false));
    applyFiltersAndRefresh(false);
  }

  private JPanel buildKpiSummaryRow() {
    JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
    row.setOpaque(false);

    row.add(
        AdminUIComponents.createKpiCard(
            "Total Spam Reports",
            new JLabel(dataStore.getSpamReports().size() + " reports"),
            "User-submitted abuse & spam reports",
            ChatTheme.PRIMARY));
    row.add(
        AdminUIComponents.createKpiCard(
            "Pending Review",
            openReportsValueLabel,
            "Awaiting administrator action",
            new Color(0xF59E0B)));
    row.add(
        AdminUIComponents.createKpiCard(
            "Sanctioned / Locked Accounts",
            lockedOffendersValueLabel,
            "Offending accounts locked by admin",
            ChatTheme.DANGER));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = ChatTheme.createCardPanel(new BorderLayout(0, 10));

    // Top row: Unified Date Range Picker Bar (Bar + Presets)
    JPanel dateRow = new JPanel(new BorderLayout(10, 4));
    dateRow.setOpaque(false);
    dateRow.add(dateRangePickerBar, BorderLayout.CENTER);

    // Bottom row: Username filter + Sort + Reset
    JPanel bottomRow = new JPanel(new BorderLayout(10, 4));
    bottomRow.setOpaque(false);

    JPanel leftFilters = new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftFilters.setOpaque(false);

    JLabel userLbl = new JLabel("Username:");
    userLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    userLbl.setForeground(ChatTheme.TEXT_SECONDARY);
    leftFilters.add(userLbl);
    leftFilters.add(usernameFilterField);

    JPanel rightSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightSort.setOpaque(false);

    JButton resetBtn = ChatTheme.createSecondaryButton("Reset All");
    resetBtn.addActionListener(
        e -> {
          usernameFilterField.setText("");
          dateRangePickerBar.setDateRange(LocalDate.of(2025, 1, 1), LocalDate.now(), false);
          sortCombo.setSelectedIndex(0);
          applyFiltersAndRefresh(false);
        });

    rightSort.add(sortCombo);
    rightSort.add(resetBtn);

    bottomRow.add(leftFilters, BorderLayout.CENTER);
    bottomRow.add(rightSort, BorderLayout.EAST);

    card.add(dateRow, BorderLayout.NORTH);
    card.add(bottomRow, BorderLayout.SOUTH);
    return card;
  }

  private JPanel buildReportsTableCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(ChatTheme.SURFACE_WHITE);

    AdminUIComponents.styleTable(reportTable);
    reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    reportTable.getColumnModel().getColumn(0).setPreferredWidth(90);
    reportTable.getColumnModel().getColumn(1).setPreferredWidth(125);
    reportTable.getColumnModel().getColumn(2).setPreferredWidth(125);
    reportTable.getColumnModel().getColumn(3).setPreferredWidth(310);
    reportTable.getColumnModel().getColumn(4).setPreferredWidth(150);
    reportTable.getColumnModel().getColumn(5).setPreferredWidth(135);
    reportTable
        .getColumnModel()
        .getColumn(5)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    card.add(AdminUIComponents.wrapTableInScrollPane(reportTable), BorderLayout.CENTER);
    reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    return card;
  }

  private JPanel buildBottomModerationBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 6));
    bar.setBackground(ChatTheme.SIDEBAR_BG);
    bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

    selectedIncidentLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    selectedIncidentLabel.setForeground(Color.WHITE);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    actions.setOpaque(false);

    lockOffenderButton.addActionListener(e -> handleLockOffendingAccount());
    actions.add(lockOffenderButton);

    bar.add(selectedIncidentLabel, BorderLayout.CENTER);
    bar.add(actions, BorderLayout.EAST);
    return bar;
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
    usernameFilterField.getDocument().addDocumentListener(docListener);
    sortCombo.addActionListener(e -> applyFiltersAndRefresh(false));

    reportTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                updateSelectedReportBanner();
              }
            });
  }

  private void applyFiltersAndRefresh(boolean showValidationAlert) {
    LocalDate fromDate = dateRangePickerBar.getStartDate();
    LocalDate toDate = dateRangePickerBar.getEndDate();
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      LocalDate swap = fromDate;
      fromDate = toDate;
      toDate = swap;
    }

    String query =
        usernameFilterField.getText() == null
            ? ""
            : usernameFilterField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    SpamModerationController.SpamKpiStats stats = controller.getKpiStats();
    openReportsValueLabel.setText(stats.pendingCount() + " pending");
    lockedOffendersValueLabel.setText(stats.lockedCount() + " locked");

    List<SpamReportRecord> filtered =
        controller.filterAndSortReports(query, fromDate, toDate, sortIdx);

    tableModel.setRowCount(0);
    for (SpamReportRecord r : filtered) {
      Optional<AdminUser> offender = dataStore.findUserByUsername(r.getReportedUsername());
      boolean isLocked = offender.map(AdminUser::isLocked).orElse(false);
      String statusBadge = isLocked ? "Account Locked" : r.getStatus();

      tableModel.addRow(
          new Object[] {
            "#" + r.getReportId(),
            "@" + r.getReporterUsername(),
            "@" + r.getReportedUsername(),
            r.getReason(),
            r.getReportedAt().format(ChatTheme.FULL_DATETIME_FMT),
            statusBadge
          });
    }

    if (tableModel.getRowCount() > 0) {
      reportTable.setRowSelectionInterval(0, 0);
    }
    updateSelectedReportBanner();
  }

  private void updateSelectedReportBanner() {
    int row = reportTable.getSelectedRow();
    if (row < 0) {
      selectedIncidentLabel.setText("SELECTED INCIDENT: None (Select a report row)");
      lockOffenderButton.setEnabled(false);
      return;
    }

    String reportId = String.valueOf(tableModel.getValueAt(row, 0));
    String offenderUsername = String.valueOf(tableModel.getValueAt(row, 2));
    Optional<AdminUser> offender = dataStore.findUserByUsername(offenderUsername);
    boolean alreadyLocked = offender.map(AdminUser::isLocked).orElse(false);

    selectedIncidentLabel.setText(
        "INCIDENT "
            + reportId
            + "   •   Offending User: "
            + offenderUsername
            + (alreadyLocked ? " (Already Locked)" : " (Active — Action Required)"));
    lockOffenderButton.setEnabled(!alreadyLocked);
    lockOffenderButton.setText(
        alreadyLocked ? "Account Already Locked" : "Lock Offending Account (" + offenderUsername + ")");
  }

  private void handleLockOffendingAccount() {
    int row = reportTable.getSelectedRow();
    if (row < 0) {
      return;
    }
    String offenderUsername = String.valueOf(tableModel.getValueAt(row, 2));
    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to lock the reported account "
                + offenderUsername
                + " for spam violations?",
            "Confirm Account Lock",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION) {
      controller.lockUserAccount(offenderUsername);
      JOptionPane.showMessageDialog(
          this,
          "Account " + offenderUsername + " has been locked and updated across the system.",
          "Offending Account Locked",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }
}
