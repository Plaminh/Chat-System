package com.chat.admin.view;

import com.chat.common.ui.ChatTheme;
import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
 * Friend Network Statistics Panel (Task 8 / Screen 8).
 *
 * <p>Covers Requirement 1.2.7: View user list with direct friend counts and friends-of-friends
 * counts, sort by name or creation timestamp, filter by name, and filter by direct friend count
 * using comparison operators ({@code =}, {@code >}, {@code <}).
 */
public final class FriendStatsPanel extends JPanel {
  private static final String[] COLUMNS = {
    "USERNAME",
    "FULL NAME",
    "EMAIL ADDRESS",
    "CREATED DATE",
    "DIRECT FRIENDS",
    "FRIENDS OF FRIENDS (2ND-DEGREE)",
    "STATUS"
  };

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField nameSearchField =
      ChatTheme.createSearchField("Filter by full name or username...", 15);
  private final JComboBox<String> operatorCombo =
      ChatTheme.createComboBox(
          new String[] {
            "All Counts",
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
  private final JTable friendStatsTable = new JTable(tableModel);

  private final JLabel matchedUsersValueLabel = new JLabel("0 users");
  private final JLabel avgDirectFriendsValueLabel = new JLabel("0.0 friends");
  private final JLabel maxExtendedReachValueLabel = new JLabel("0 connections");
  private final JLabel footerSummaryLabel = new JLabel("Showing 0 user friend statistics records");

  public FriendStatsPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(ChatTheme.CANVAS_BG);

    JPanel topSection = new JPanel(new BorderLayout(0, 12));
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
            "Users Matching Filter",
            matchedUsersValueLabel,
            "Accounts matching name & friend threshold",
            ChatTheme.PRIMARY));
    row.add(
        AdminUIComponents.createKpiCard(
            "Avg. Direct Friends",
            avgDirectFriendsValueLabel,
            "Mean 1st-degree connections in result set",
            ChatTheme.SUCCESS_DOT));
    row.add(
        AdminUIComponents.createKpiCard(
            "Peak 2nd-Degree Network",
            maxExtendedReachValueLabel,
            "Highest friends-of-friends count in view",
            new Color(0x6366F1)));
    return row;
  }

  private JPanel buildFilterToolbar() {
    JPanel card = ChatTheme.createCardPanel(new BorderLayout(10, 6));

    JPanel leftControls = new JPanel(AdminUIComponents.createWrapFlowLayout(FlowLayout.LEFT, 8, 2));
    leftControls.setOpaque(false);

    JLabel nameLbl = new JLabel("Name:");
    nameLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    nameLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    JLabel friendFilterLbl = new JLabel("Direct Friends:");
    friendFilterLbl.setFont(ChatTheme.FONT_BODY_BOLD);
    friendFilterLbl.setForeground(ChatTheme.TEXT_SECONDARY);

    thresholdField.setFont(ChatTheme.FONT_MONO);
    thresholdField.putClientProperty("JTextField.placeholderText", "Count");
    thresholdField.setEnabled(false);

    leftControls.add(nameLbl);
    leftControls.add(nameSearchField);
    leftControls.add(friendFilterLbl);
    leftControls.add(operatorCombo);
    leftControls.add(thresholdField);

    JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightControls.setOpaque(false);

    JButton resetBtn = ChatTheme.createSecondaryButton("Reset");
    resetBtn.addActionListener(
        e -> {
          nameSearchField.setText("");
          operatorCombo.setSelectedIndex(0);
          thresholdField.setText("");
          thresholdField.setEnabled(false);
          sortCombo.setSelectedIndex(0);
          applyFiltersAndRefresh(false);
        });

    rightControls.add(sortCombo);
    rightControls.add(resetBtn);

    card.add(leftControls, BorderLayout.CENTER);
    card.add(rightControls, BorderLayout.EAST);
    return card;
  }

  private JPanel buildTableCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(ChatTheme.SURFACE_WHITE);

    AdminUIComponents.styleTable(friendStatsTable);
    friendStatsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    friendStatsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
    friendStatsTable.getColumnModel().getColumn(1).setPreferredWidth(155);
    friendStatsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
    friendStatsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    friendStatsTable.getColumnModel().getColumn(4).setPreferredWidth(135);
    friendStatsTable.getColumnModel().getColumn(5).setPreferredWidth(210);
    friendStatsTable.getColumnModel().getColumn(6).setPreferredWidth(100);

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
              cell.setForeground(
                  column == 4 ? ChatTheme.PRIMARY : new Color(0x4F46E5));
            }
            return cell;
          }
        };
    friendStatsTable.getColumnModel().getColumn(4).setCellRenderer(metricRenderer);
    friendStatsTable.getColumnModel().getColumn(5).setCellRenderer(metricRenderer);
    friendStatsTable
        .getColumnModel()
        .getColumn(6)
        .setCellRenderer(AdminUIComponents.createStatusBadgeRenderer());

    card.add(AdminUIComponents.wrapTableInScrollPane(friendStatsTable), BorderLayout.CENTER);
    return card;
  }

  private JPanel buildFooterBar() {
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
    thresholdField.getDocument().addDocumentListener(docListener);

    operatorCombo.addActionListener(
        e -> {
          boolean requiresValue = operatorCombo.getSelectedIndex() > 0;
          thresholdField.setEnabled(requiresValue);
          if (!requiresValue) {
            thresholdField.setText("");
          } else if (thresholdField.getText().isBlank()) {
            thresholdField.setText("15");
          }
          applyFiltersAndRefresh(false);
        });

    sortCombo.addActionListener(e -> applyFiltersAndRefresh(false));
  }

  private void applyFiltersAndRefresh(boolean showValidationAlert) {
    int opIdx = operatorCombo.getSelectedIndex();
    Integer threshold = null;

    if (opIdx > 0) {
      String raw = thresholdField.getText() == null ? "" : thresholdField.getText().trim();
      if (raw.isEmpty()) {
        if (showValidationAlert) {
          JOptionPane.showMessageDialog(
              this,
              "Please enter a non-negative integer value for the Direct Friends comparison filter.",
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
                "Friend count threshold must be a non-negative integer (>= 0).",
                "Invalid Comparison Value",
                JOptionPane.WARNING_MESSAGE);
          }
          return;
        }
      } catch (NumberFormatException ex) {
        if (showValidationAlert) {
          JOptionPane.showMessageDialog(
              this,
              "Invalid friend count threshold. Please enter a valid integer (e.g. 15).",
              "Invalid Number Format",
              JOptionPane.ERROR_MESSAGE);
        }
        return;
      }
    }

    String query =
        nameSearchField.getText() == null ? "" : nameSearchField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    List<AdminUser> allUsers = dataStore.getUsers();
    List<AdminUser> filtered = new ArrayList<>();

    for (AdminUser user : allUsers) {
      if (!query.isEmpty()
          && !user.getFullName().toLowerCase().contains(query)
          && !user.getUsername().toLowerCase().contains(query)) {
        continue;
      }
      if (opIdx > 0 && threshold != null) {
        int direct = user.getDirectFriendCount();
        boolean matches =
            switch (opIdx) {
              case 1 -> direct == threshold;
              case 2 -> direct > threshold;
              case 3 -> direct < threshold;
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
    int totalDirect = 0;
    int maxSecondDegree = 0;

    for (AdminUser user : filtered) {
      totalDirect += user.getDirectFriendCount();
      if (user.getFriendsOfFriendsCount() > maxSecondDegree) {
        maxSecondDegree = user.getFriendsOfFriendsCount();
      }
      tableModel.addRow(
          new Object[] {
            "@" + user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getCreatedAt().format(ChatTheme.DATETIME_FMT),
            user.getDirectFriendCount() + " friends",
            user.getFriendsOfFriendsCount() + " network reach",
            user.getStatusText()
          });
    }

    double avgDirect = filtered.isEmpty() ? 0.0 : (double) totalDirect / filtered.size();
    matchedUsersValueLabel.setText(filtered.size() + " users");
    avgDirectFriendsValueLabel.setText(String.format("%.1f friends", avgDirect));
    maxExtendedReachValueLabel.setText(maxSecondDegree + " connections");
    footerSummaryLabel.setText(
        "Showing "
            + filtered.size()
            + " of "
            + allUsers.size()
            + " accounts   |   Total Direct Friend Links: "
            + totalDirect);

    if (tableModel.getRowCount() > 0) {
      friendStatsTable.setRowSelectionInterval(0, 0);
    }
  }
}
