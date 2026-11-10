package com.chat.admin.view;

import com.chat.common.ui.ChatTheme;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.model.LoginRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * User Engagement & Chat Activity Panel (Task 9 / Screen 9).
 *
 * <p>Covers Requirement 1.2.8: Select a date range (from date to date), view active users with
 * their app open counts, chatted individuals counts, and chatted groups counts, sort by name or
 * creation timestamp, filter by name, and filter by activity count using {@code =}, {@code >},
 * {@code <}.
 */
public final class UserActivityPanel extends JPanel {
  private static final String[] COLUMNS = {
    "USERNAME",
    "FULL NAME",
    "CREATED DATE",
    "APP OPENS",
    "CHATTED INDIVIDUALS",
    "CHATTED GROUPS",
    "STATUS"
  };

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField fromDateField = AdminUIComponents.createDateField("2025-01-01");
  private final JTextField toDateField = AdminUIComponents.createDateField("2025-12-31");
  private final JTextField nameSearchField =
      ChatTheme.createSearchField("Filter by full name or username...", 16);
  private final JComboBox<String> operatorCombo =
      ChatTheme.createComboBox(
          new String[] {
            "All Activity",
            "= (Equal to)",
            "> (Greater than)",
            "< (Less than)"
          });
  private final JTextField thresholdField = new JTextField("", 6);
  private final JComboBox<String> sortCombo =
      ChatTheme.createComboBox(
          new String[] {
            "Sort: Full Name (A-Z)",
            "Sort: Full Name (Z-A)",
            "Sort: Created Date (Newest First)",
            "Sort: Created Date (Oldest First)"
          });

  private final DefaultTableModel tableModel =
      new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable activityTable = new JTable(tableModel);

  private final JLabel activeUsersCountValueLabel = new JLabel("0 users");
  private final JLabel totalAppOpensValueLabel = new JLabel("0 opens");
  private final JLabel totalChatInteractionsValueLabel = new JLabel("0 chats / 0 groups");
  private final JLabel footerSummaryLabel = new JLabel("Showing 0 active user records");

  public UserActivityPanel() {
    setLayout(new BorderLayout(0, 8));
    setBackground(ChatTheme.CANVAS_BG);

    JPanel topSection = new JPanel(new BorderLayout(0, 8));
    topSection.setOpaque(false);
    topSection.add(buildKpiSummaryRow(), BorderLayout.NORTH);
    topSection.add(buildFilterToolbar(), BorderLayout.SOUTH);

    add(topSection, BorderLayout.NORTH);
    add(buildTableCard(), BorderLayout.CENTER);
    add(buildFooterBar(), BorderLayout.SOUTH);

    wireEvents();
    dataStore.addChangeListener(() -> applyFiltersAndRefresh(false));
    applyFiltersAndRefresh(false);
  }

  private JPanel buildKpiSummaryRow() {
    JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
    row.setOpaque(false);

    row.add(
        AdminUIComponents.createKpiCard(
            "Active Users in Period",
            activeUsersCountValueLabel,
            "Users matching date window & activity filter",
            ChatTheme.PRIMARY));
    row.add(
        AdminUIComponents.createKpiCard(
            "Total App Opens",
            totalAppOpensValueLabel,
            "Cumulative app launches across filtered users",
            ChatTheme.SUCCESS_DOT));
    row.add(
        AdminUIComponents.createKpiCard(
            "Chat Engagement Reach",
            totalChatInteractionsValueLabel,
            "Combined direct chat partners & group participations",
            new Color(0x6366F1)));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = ChatTheme.createCardPanel(new BorderLayout(0, 6));

    // Top row: Date range filter + quick presets
    JPanel dateRow = new JPanel(new BorderLayout(10, 4));
    dateRow.setOpaque(false);

    JPanel leftDateControls = new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftDateControls.setOpaque(false);

    JLabel fromLbl = new JLabel("From Date:");
    fromLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    fromLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    JLabel toLbl = new JLabel("To Date:");
    toLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    toLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    leftDateControls.add(fromLbl);
    leftDateControls.add(fromDateField);
    leftDateControls.add(toLbl);
    leftDateControls.add(toDateField);

    JPanel rightPresets = new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.RIGHT, 6, 2));
    rightPresets.setOpaque(false);

    JLabel presetLbl = new JLabel("Presets:");
    presetLbl.setFont(ChatTheme.FONT_SMALL);
    presetLbl.setForeground(ChatTheme.TEXT_MUTED);

    JButton mayPresetBtn = ChatTheme.createSecondaryButton("May 2025");
    mayPresetBtn.addActionListener(e -> setDateRange("2025-05-01", "2025-05-31"));

    JButton q1Q2PresetBtn = ChatTheme.createSecondaryButton("H1 2025");
    q1Q2PresetBtn.addActionListener(e -> setDateRange("2025-01-01", "2025-06-30"));

    JButton fullPeriodBtn = ChatTheme.createSecondaryButton("All 2025–2026");
    fullPeriodBtn.addActionListener(
        e -> setDateRange("2025-01-01", LocalDate.now().format(ChatTheme.DATE_FMT)));

    rightPresets.add(presetLbl);
    rightPresets.add(mayPresetBtn);
    rightPresets.add(q1Q2PresetBtn);
    rightPresets.add(fullPeriodBtn);

    dateRow.add(leftDateControls, BorderLayout.CENTER);
    dateRow.add(rightPresets, BorderLayout.EAST);

    // Bottom row: Name search + Activity comparison (=, >, <) + Sort + Reset
    JPanel bottomRow = new JPanel(new BorderLayout(10, 4));
    bottomRow.setOpaque(false);

    JPanel leftFilters = new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftFilters.setOpaque(false);

    JLabel nameLbl = new JLabel("Name:");
    nameLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    nameLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    JLabel activityLbl = new JLabel("App Opens:");
    activityLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    activityLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    thresholdField.setFont(ChatTheme.FONT_MONO);
    thresholdField.putClientProperty("JTextField.placeholderText", "Opens");
    thresholdField.setEnabled(false);

    leftFilters.add(nameLbl);
    leftFilters.add(nameSearchField);
    leftFilters.add(activityLbl);
    leftFilters.add(operatorCombo);
    leftFilters.add(thresholdField);

    JPanel rightSort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightSort.setOpaque(false);

    JButton resetBtn = ChatTheme.createSecondaryButton("Reset");
    resetBtn.addActionListener(
        e -> {
          fromDateField.setText("2025-01-01");
          toDateField.setText(LocalDate.now().format(ChatTheme.DATE_FMT));
          nameSearchField.setText("");
          operatorCombo.setSelectedIndex(0);
          thresholdField.setText("");
          thresholdField.setEnabled(false);
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

  private JPanel buildTableCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(ChatTheme.SURFACE_WHITE);

    AdminUIComponents.styleTable(activityTable);
    activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    activityTable.getColumnModel().getColumn(0).setPreferredWidth(125);
    activityTable.getColumnModel().getColumn(1).setPreferredWidth(155);
    activityTable.getColumnModel().getColumn(2).setPreferredWidth(145);
    activityTable.getColumnModel().getColumn(3).setPreferredWidth(125);
    activityTable.getColumnModel().getColumn(4).setPreferredWidth(160);
    activityTable.getColumnModel().getColumn(5).setPreferredWidth(145);
    activityTable.getColumnModel().getColumn(6).setPreferredWidth(95);

    DefaultTableCellRenderer metricRenderer =
        new DefaultTableCellRenderer() {
          @Override
          public java.awt.Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {
            JLabel cell =
                (JLabel)
                    super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
            cell.setHorizontalAlignment(SwingConstants.CENTER);
            cell.setFont(ChatTheme.FONT_BODY_BOLD);
            if (!isSelected) {
              if (column == 3) {
                cell.setForeground(ChatTheme.PRIMARY);
              } else if (column == 4) {
                cell.setForeground(new Color(0x0D9488));
              } else {
                cell.setForeground(new Color(0x4F46E5));
              }
            }
            return cell;
          }
        };
    activityTable.getColumnModel().getColumn(3).setCellRenderer(metricRenderer);
    activityTable.getColumnModel().getColumn(4).setCellRenderer(metricRenderer);
    activityTable.getColumnModel().getColumn(5).setCellRenderer(metricRenderer);
    activityTable
        .getColumnModel()
        .getColumn(6)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    card.add(AdminUIComponents.wrapTableInScrollPane(activityTable), BorderLayout.CENTER);
    return card;
  }

  private JPanel buildFooterBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 0));
    bar.setBackground(ChatTheme.SURFACE_WHITE);
    bar.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ChatTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)));

    footerSummaryLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    footerSummaryLabel.setForeground(ChatTheme.TEXT_PRIMARY);

    bar.add(footerSummaryLabel, BorderLayout.CENTER);
    return bar;
  }

  private void setDateRange(String from, String to) {
    fromDateField.setText(from);
    toDateField.setText(to);
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
    fromDateField.getDocument().addDocumentListener(docListener);
    toDateField.getDocument().addDocumentListener(docListener);
    AdminUIComponents.attachDateFieldValidationTriggers(
        fromDateField, () -> applyFiltersAndRefresh(true));
    AdminUIComponents.attachDateFieldValidationTriggers(
        toDateField, () -> applyFiltersAndRefresh(true));
    thresholdField.getDocument().addDocumentListener(docListener);

    operatorCombo.addActionListener(
        e -> {
          boolean requiresValue = operatorCombo.getSelectedIndex() > 0;
          thresholdField.setEnabled(requiresValue);
          if (!requiresValue) {
            thresholdField.setText("");
          } else if (thresholdField.getText().isBlank()) {
            thresholdField.setText("50");
          }
          applyFiltersAndRefresh(false);
        });

    sortCombo.addActionListener(e -> applyFiltersAndRefresh(false));
  }

  private void applyFiltersAndRefresh(boolean showValidationAlert) {
    LocalDate[] dateRange =
        AdminUIComponents.parseAndValidateDateRange(
            this, fromDateField, toDateField, showValidationAlert);
    if (dateRange == null) {
      return;
    }
    LocalDate fromDate = dateRange[0];
    LocalDate toDate = dateRange[1];
    String fromText = fromDate == null ? "" : fromDate.format(ChatTheme.DATE_FMT);
    String toText = toDate == null ? "" : toDate.format(ChatTheme.DATE_FMT);

    int opIdx = operatorCombo.getSelectedIndex();
    Integer threshold = null;
    if (opIdx > 0) {
      String raw = thresholdField.getText() == null ? "" : thresholdField.getText().trim();
      if (raw.isEmpty()) {
        if (showValidationAlert) {
          JOptionPane.showMessageDialog(
              this,
              "Please enter a non-negative integer value for the App Opens comparison filter.",
              "Missing Comparison Value",
              JOptionPane.WARNING_MESSAGE);
        }
        return;
      }
      try {
        threshold = Integer.parseInt(raw);
        if (threshold < 0) {
          if (showValidationAlert) {
            JOptionPane.showMessageDialog(
                this,
                "Activity count threshold must be a non-negative integer (>= 0).",
                "Invalid Comparison Value",
                JOptionPane.WARNING_MESSAGE);
          }
          return;
        }
      } catch (NumberFormatException ex) {
        if (showValidationAlert) {
          JOptionPane.showMessageDialog(
              this,
              "Invalid activity count threshold. Please enter a valid integer (e.g. 50).",
              "Invalid Number Format",
              JOptionPane.ERROR_MESSAGE);
        }
        return;
      }
    }

    Set<String> usersWithLoginInRange = new HashSet<>();
    for (LoginRecord record : dataStore.getAllLoginRecordsChronological()) {
      LocalDate loginDate = record.timestamp().toLocalDate();
      if (fromDate != null && loginDate.isBefore(fromDate)) {
        continue;
      }
      if (toDate != null && loginDate.isAfter(toDate)) {
        continue;
      }
      usersWithLoginInRange.add(record.username().toLowerCase());
    }

    String query =
        nameSearchField.getText() == null ? "" : nameSearchField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    List<AdminUser> allUsers = dataStore.getUsers();
    List<AdminUser> filtered = new ArrayList<>();

    for (AdminUser user : allUsers) {
      LocalDate createdDate = user.getCreatedAt().toLocalDate();
      boolean activeInWindow =
          usersWithLoginInRange.contains(user.getUsername().toLowerCase())
              || ((fromDate == null || !createdDate.isBefore(fromDate))
                  && (toDate == null || !createdDate.isAfter(toDate)));
      if (!activeInWindow) {
        continue;
      }
      if (!query.isEmpty()
          && !user.getFullName().toLowerCase().contains(query)
          && !user.getUsername().toLowerCase().contains(query)) {
        continue;
      }
      if (opIdx > 0 && threshold != null) {
        int opens = user.getAppOpenCount();
        boolean matches =
            switch (opIdx) {
              case 1 -> opens == threshold;
              case 2 -> opens > threshold;
              case 3 -> opens < threshold;
              default -> true;
            };
        if (!matches) {
          continue;
        }
      }
      filtered.add(user);
    }

    Comparator<AdminUser> comparator =
        switch (sortIdx) {
          case 1 ->
              Comparator.comparing((AdminUser u) -> u.getFullName().toLowerCase()).reversed();
          case 2 -> Comparator.comparing(AdminUser::getCreatedAt).reversed();
          case 3 -> Comparator.comparing(AdminUser::getCreatedAt);
          default -> Comparator.comparing(u -> u.getFullName().toLowerCase());
        };
    filtered.sort(comparator);

    tableModel.setRowCount(0);
    int sumOpens = 0;
    int sumIndividuals = 0;
    int sumGroups = 0;

    for (AdminUser user : filtered) {
      sumOpens += user.getAppOpenCount();
      sumIndividuals += user.getChattedUsersCount();
      sumGroups += user.getChattedGroupsCount();
      tableModel.addRow(
          new Object[] {
            "@" + user.getUsername(),
            user.getFullName(),
            user.getCreatedAt().format(ChatTheme.DATETIME_FMT),
            user.getAppOpenCount() + " opens",
            user.getChattedUsersCount() + " users",
            user.getChattedGroupsCount() + " groups",
            user.getStatusText()
          });
    }

    activeUsersCountValueLabel.setText(filtered.size() + " users");
    totalAppOpensValueLabel.setText(sumOpens + " opens");
    totalChatInteractionsValueLabel.setText(
        sumIndividuals + " users / " + sumGroups + " groups");
    footerSummaryLabel.setText(
        "Showing "
            + filtered.size()
            + " active user(s) in period ("
            + (fromText.isEmpty() ? "Any" : fromText)
            + " → "
            + (toText.isEmpty() ? "Any" : toText)
            + ")");

    if (tableModel.getRowCount() > 0) {
      activityTable.setRowSelectionInterval(0, 0);
    }
  }
}
