package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.model.SpamReportRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField usernameFilterField =
      AdminUIStyle.createSearchField("Filter by reported or reporting username...", 15);
  private final JTextField fromDateField = AdminUIStyle.createDateField("2025-01-01");
  private final JTextField toDateField = AdminUIStyle.createDateField("2025-12-31");
  private final JComboBox<String> sortCombo =
      AdminUIStyle.createComboBox(
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
      AdminUIStyle.createDangerButton("Lock Offending Account");

  public SpamReportPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(AdminUIStyle.CANVAS_BG);

    JPanel topSection = new JPanel(new BorderLayout(0, 12));
    topSection.setOpaque(false);
    topSection.add(buildKpiSummaryRow(), BorderLayout.NORTH);
    topSection.add(buildFilterToolbar(), BorderLayout.SOUTH);

    add(topSection, BorderLayout.NORTH);
    add(buildReportsTableCard(), BorderLayout.CENTER);
    add(buildBottomModerationBar(), BorderLayout.SOUTH);

    wireEvents();
    dataStore.addChangeListener(() -> applyFiltersAndRefresh(false));
    applyFiltersAndRefresh(false);
  }

  private JPanel buildKpiSummaryRow() {
    JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
    row.setOpaque(false);

    row.add(
        AdminUIStyle.createKpiCard(
            "Total Spam Reports",
            new JLabel(dataStore.getSpamReports().size() + " reports"),
            "User-submitted abuse & spam reports",
            AdminUIStyle.PRIMARY));
    row.add(
        AdminUIStyle.createKpiCard(
            "Pending Review",
            openReportsValueLabel,
            "Awaiting administrator action",
            new Color(0xF59E0B)));
    row.add(
        AdminUIStyle.createKpiCard(
            "Sanctioned / Locked Accounts",
            lockedOffendersValueLabel,
            "Offending accounts locked by admin",
            AdminUIStyle.DANGER_BTN));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = AdminUIStyle.createCardPanel(new BorderLayout(10, 6));

    JPanel leftFilters = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftFilters.setOpaque(false);

    JLabel userLbl = new JLabel("Username:");
    userLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    userLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    JLabel fromLbl = new JLabel("From:");
    fromLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    fromLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    JLabel toLbl = new JLabel("To:");
    toLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    toLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    leftFilters.add(userLbl);
    leftFilters.add(usernameFilterField);
    leftFilters.add(fromLbl);
    leftFilters.add(fromDateField);
    leftFilters.add(toLbl);
    leftFilters.add(toDateField);

    JPanel rightSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightSort.setOpaque(false);

    JButton resetBtn = AdminUIStyle.createSecondaryButton("Reset");
    resetBtn.addActionListener(
        e -> {
          usernameFilterField.setText("");
          fromDateField.setText("2025-01-01");
          toDateField.setText("2025-12-31");
          sortCombo.setSelectedIndex(0);
          applyFiltersAndRefresh(false);
        });

    rightSort.add(sortCombo);
    rightSort.add(resetBtn);

    card.add(leftFilters, BorderLayout.CENTER);
    card.add(rightSort, BorderLayout.EAST);
    return card;
  }

  private JPanel buildReportsTableCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(AdminUIStyle.SURFACE_WHITE);

    AdminUIStyle.styleTable(reportTable);
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
        .setCellRenderer(AdminUIStyle.createStatusBadgeRenderer());

    card.add(AdminUIStyle.wrapTableInScrollPane(reportTable), BorderLayout.CENTER);
    return card;
  }

  private JPanel buildBottomModerationBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 6));
    bar.setBackground(AdminUIStyle.SIDEBAR_BG);
    bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

    selectedIncidentLabel.setFont(AdminUIStyle.FONT_BODY_BOLD);
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
    fromDateField.getDocument().addDocumentListener(docListener);
    toDateField.getDocument().addDocumentListener(docListener);
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
    LocalDate fromDate = null;
    LocalDate toDate = null;
    String fromText = fromDateField.getText() == null ? "" : fromDateField.getText().trim();
    String toText = toDateField.getText() == null ? "" : toDateField.getText().trim();

    if ((!fromText.isEmpty() && fromText.length() != 10)
        || (!toText.isEmpty() && toText.length() != 10)) {
      return;
    }

    try {
      if (!fromText.isEmpty()) {
        fromDate = LocalDate.parse(fromText, AdminUIStyle.DATE_FMT);
      }
      if (!toText.isEmpty()) {
        toDate = LocalDate.parse(toText, AdminUIStyle.DATE_FMT);
      }
      if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
        if (showValidationAlert) {
          JOptionPane.showMessageDialog(
              this,
              "Start Date ('From') cannot be later than End Date ('To').",
              "Invalid Date Range",
              JOptionPane.WARNING_MESSAGE);
        }
        return;
      }
    } catch (DateTimeParseException ex) {
      if (showValidationAlert) {
        JOptionPane.showMessageDialog(
            this,
            "Invalid date format. Please use YYYY-MM-DD (e.g. 2025-05-01).",
            "Invalid Date Format",
            JOptionPane.ERROR_MESSAGE);
      }
      return;
    }

    String query =
        usernameFilterField.getText() == null
            ? ""
            : usernameFilterField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    List<SpamReportRecord> all = dataStore.getSpamReports();
    long pendingCount =
        all.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
    long lockedCount = all.size() - pendingCount;
    openReportsValueLabel.setText(pendingCount + " pending");
    lockedOffendersValueLabel.setText(lockedCount + " locked");

    List<SpamReportRecord> filtered = new ArrayList<>();
    for (SpamReportRecord r : all) {
      LocalDate reportDate = r.getReportedAt().toLocalDate();
      if (fromDate != null && reportDate.isBefore(fromDate)) {
        continue;
      }
      if (toDate != null && reportDate.isAfter(toDate)) {
        continue;
      }
      if (!query.isEmpty()
          && !r.getReportedUsername().toLowerCase().contains(query)
          && !r.getReporterUsername().toLowerCase().contains(query)) {
        continue;
      }
      filtered.add(r);
    }

    Comparator<SpamReportRecord> comparator =
        switch (sortIdx) {
          case 1 -> Comparator.comparing(SpamReportRecord::getReportedAt);
          case 2 -> Comparator.comparing(r -> r.getReportedUsername().toLowerCase());
          case 3 ->
              Comparator.comparing((SpamReportRecord r) -> r.getReportedUsername().toLowerCase())
                  .reversed();
          default -> Comparator.comparing(SpamReportRecord::getReportedAt).reversed();
        };
    filtered.sort(comparator);

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
            r.getReportedAt().format(AdminUIStyle.FULL_DATETIME_FMT),
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
      dataStore.lockUserAccount(offenderUsername);
      JOptionPane.showMessageDialog(
          this,
          "Account " + offenderUsername + " has been locked and updated across the system.",
          "Offending Account Locked",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }
}
