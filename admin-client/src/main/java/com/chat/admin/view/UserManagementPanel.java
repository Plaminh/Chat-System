package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * User Account Directory & Management Panel (Task 3 / Screen 3).
 *
 * <p>Covers Requirement 1.2.1: View user list, filter by name/username/status, sort by name or
 * creation date, Add/Edit/Delete accounts, Lock/Unlock accounts, Update password, and trigger Login
 * History & Friends inspection.
 */
public final class UserManagementPanel extends JPanel {
  private static final String[] COLUMNS = {
    "USER ID",
    "USERNAME",
    "FULL NAME",
    "EMAIL ADDRESS",
    "GENDER",
    "DATE OF BIRTH",
    "ADDRESS",
    "CREATED DATE",
    "STATUS"
  };

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField searchField =
      AdminUIStyle.createSearchField("Search by username, full name, or email...", 18);
  private final JComboBox<String> statusCombo =
      AdminUIStyle.createComboBox(new String[] {"All Statuses", "Active", "Locked"});
  private final JComboBox<String> sortCombo =
      AdminUIStyle.createComboBox(
          new String[] {
            "Sort: Created Date (Newest)",
            "Sort: Created Date (Oldest)",
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
  private final JTable userTable = new JTable(tableModel);
  private final JLabel countSummaryLabel = new JLabel("Showing 0 accounts");
  private final JLabel selectedUserLabel = new JLabel("SELECTED: None");
  private final JButton lockToggleBtn = AdminUIStyle.createSecondaryButton("Lock / Unlock");
  private Consumer<AdminUser> historyInspectorCallback;

  public UserManagementPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(AdminUIStyle.CANVAS_BG);

    add(buildTopFilterToolbar(), BorderLayout.NORTH);
    add(buildTableCenterCard(), BorderLayout.CENTER);
    add(buildBottomActionBar(), BorderLayout.SOUTH);

    wireEvents();
    dataStore.addChangeListener(this::refreshTableData);
    refreshTableData();
  }

  public void setHistoryInspectorCallback(Consumer<AdminUser> callback) {
    this.historyInspectorCallback = callback;
  }

  private JPanel buildTopFilterToolbar() {
    JPanel card = AdminUIStyle.createCardPanel(new BorderLayout(12, 6));

    JPanel leftFilters = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.LEFT, 10, 2));
    leftFilters.setOpaque(false);

    JLabel searchLbl = new JLabel("Filter:");
    searchLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    searchLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    leftFilters.add(searchLbl);
    leftFilters.add(searchField);
    leftFilters.add(statusCombo);
    leftFilters.add(sortCombo);

    JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightActions.setOpaque(false);

    JButton resetBtn = AdminUIStyle.createSecondaryButton("Reset");
    resetBtn.addActionListener(
        e -> {
          searchField.setText("");
          statusCombo.setSelectedIndex(0);
          sortCombo.setSelectedIndex(0);
          refreshTableData();
        });

    JButton addUserBtn = AdminUIStyle.createPrimaryButton("+ Add New User");
    addUserBtn.addActionListener(e -> showAddOrEditUserDialog(null));

    rightActions.add(resetBtn);
    rightActions.add(addUserBtn);

    card.add(leftFilters, BorderLayout.CENTER);
    card.add(rightActions, BorderLayout.EAST);
    return card;
  }

  private JPanel buildTableCenterCard() {
    JPanel card = new JPanel(new BorderLayout(0, 0));
    card.setBackground(AdminUIStyle.SURFACE_WHITE);

    AdminUIStyle.styleTable(userTable);
    userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    userTable.getColumnModel().getColumn(0).setPreferredWidth(95);
    userTable.getColumnModel().getColumn(1).setPreferredWidth(125);
    userTable.getColumnModel().getColumn(2).setPreferredWidth(140);
    userTable.getColumnModel().getColumn(3).setPreferredWidth(180);
    userTable.getColumnModel().getColumn(4).setPreferredWidth(75);
    userTable.getColumnModel().getColumn(5).setPreferredWidth(105);
    userTable.getColumnModel().getColumn(6).setPreferredWidth(145);
    userTable.getColumnModel().getColumn(7).setPreferredWidth(135);
    userTable.getColumnModel().getColumn(8).setPreferredWidth(105);
    userTable
        .getColumnModel()
        .getColumn(8)
        .setCellRenderer(AdminUIStyle.createStatusBadgeRenderer());

    card.add(AdminUIStyle.wrapTableInScrollPane(userTable), BorderLayout.CENTER);

    JPanel tableFooter = new JPanel(new BorderLayout());
    tableFooter.setBackground(AdminUIStyle.SURFACE_WHITE);
    tableFooter.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, AdminUIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
    countSummaryLabel.setFont(AdminUIStyle.FONT_SMALL);
    countSummaryLabel.setForeground(AdminUIStyle.TEXT_SECONDARY);
    tableFooter.add(countSummaryLabel, BorderLayout.WEST);

    card.add(tableFooter, BorderLayout.SOUTH);
    return card;
  }

  private JPanel buildBottomActionBar() {
    JPanel bar = new JPanel(new BorderLayout(12, 6));
    bar.setBackground(AdminUIStyle.SIDEBAR_BG);
    bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

    selectedUserLabel.setFont(AdminUIStyle.FONT_BODY_BOLD);
    selectedUserLabel.setForeground(Color.WHITE);
    selectedUserLabel.setMinimumSize(new Dimension(120, 24));

    JPanel actionButtons = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.RIGHT, 8, 2));
    actionButtons.setOpaque(false);

    JButton editBtn = AdminUIStyle.createSecondaryButton("Edit Profile");
    editBtn.addActionListener(
        e -> getSelectedUser().ifPresent(this::showAddOrEditUserDialog));

    lockToggleBtn.addActionListener(e -> handleLockToggle());

    JButton resetPassBtn = AdminUIStyle.createSecondaryButton("Reset Password");
    resetPassBtn.addActionListener(e -> handlePasswordReset());

    JButton deleteBtn = AdminUIStyle.createDangerButton("Delete User");
    deleteBtn.addActionListener(e -> handleDeleteUser());

    JButton historyBtn = AdminUIStyle.createPrimaryButton("View History & Network");
    historyBtn.addActionListener(e -> handleViewHistoryAndFriends());

    actionButtons.add(editBtn);
    actionButtons.add(lockToggleBtn);
    actionButtons.add(resetPassBtn);
    actionButtons.add(deleteBtn);
    actionButtons.add(historyBtn);

    bar.add(selectedUserLabel, BorderLayout.CENTER);
    bar.add(actionButtons, BorderLayout.EAST);
    return bar;
  }

  private void wireEvents() {
    DocumentListener docListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            refreshTableData();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            refreshTableData();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            refreshTableData();
          }
        };
    searchField.getDocument().addDocumentListener(docListener);
    statusCombo.addActionListener(e -> refreshTableData());
    sortCombo.addActionListener(e -> refreshTableData());

    userTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                updateSelectedUserBanner();
              }
            });
  }

  public void refreshTableData() {
    String selectedUsername =
        getSelectedUserWithoutPrompt().map(AdminUser::getUsername).orElse(null);

    String query =
        searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
    String statusFilter = (String) statusCombo.getSelectedItem();
    int sortIdx = sortCombo.getSelectedIndex();

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
        switch (sortIdx) {
          case 1 -> Comparator.comparing(AdminUser::getCreatedAt);
          case 2 -> Comparator.comparing(u -> u.getFullName().toLowerCase());
          case 3 ->
              Comparator.comparing((AdminUser u) -> u.getFullName().toLowerCase()).reversed();
          default -> Comparator.comparing(AdminUser::getCreatedAt).reversed();
        };
    filtered.sort(comparator);

    tableModel.setRowCount(0);
    int rowToSelect = -1;
    for (int i = 0; i < filtered.size(); i++) {
      AdminUser u = filtered.get(i);
      tableModel.addRow(
          new Object[] {
            u.getId(),
            "@" + u.getUsername(),
            u.getFullName(),
            u.getEmail(),
            u.getGender(),
            u.getDateOfBirth().format(AdminUIStyle.DATE_FMT),
            u.getAddress(),
            u.getCreatedAt().format(AdminUIStyle.DATETIME_FMT),
            u.getStatusText()
          });
      if (selectedUsername != null && u.getUsername().equalsIgnoreCase(selectedUsername)) {
        rowToSelect = i;
      }
    }

    countSummaryLabel.setText(
        "Showing "
            + filtered.size()
            + " of "
            + dataStore.getUsers().size()
            + " total user accounts");

    if (rowToSelect >= 0) {
      userTable.setRowSelectionInterval(rowToSelect, rowToSelect);
    } else if (tableModel.getRowCount() > 0) {
      userTable.setRowSelectionInterval(0, 0);
    }
    updateSelectedUserBanner();
  }

  private void updateSelectedUserBanner() {
    Optional<AdminUser> selected = getSelectedUserWithoutPrompt();
    if (selected.isPresent()) {
      AdminUser u = selected.get();
      selectedUserLabel.setText(
          "SELECTED: @"
              + u.getUsername()
              + " ("
              + u.getFullName()
              + " • "
              + u.getId()
              + " • "
              + u.getStatusText()
              + ")");
      lockToggleBtn.setText(u.isLocked() ? "Unlock Account" : "Lock Account");
    } else {
      selectedUserLabel.setText("SELECTED: None (Select a row to manage account)");
      lockToggleBtn.setText("Lock / Unlock");
    }
  }

  private Optional<AdminUser> getSelectedUserWithoutPrompt() {
    int row = userTable.getSelectedRow();
    if (row < 0) {
      return Optional.empty();
    }
    String rawUsername = String.valueOf(tableModel.getValueAt(row, 1));
    return dataStore.findUserByUsername(rawUsername);
  }

  private Optional<AdminUser> getSelectedUser() {
    Optional<AdminUser> selected = getSelectedUserWithoutPrompt();
    if (selected.isEmpty()) {
      JOptionPane.showMessageDialog(
          this,
          "Please select a user account from the table first.",
          "No User Selected",
          JOptionPane.WARNING_MESSAGE);
    }
    return selected;
  }

  private void showAddOrEditUserDialog(AdminUser existingUser) {
    boolean isEdit = existingUser != null;
    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(owner, isEdit ? "Edit User Profile" : "Add New User Account", true);
    dialog.setSize(460, 440);
    dialog.setLocationRelativeTo(this);

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(AdminUIStyle.SURFACE_WHITE);
    form.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

    JTextField usernameInput =
        new JTextField(isEdit ? existingUser.getUsername() : "", 20);
    usernameInput.setEnabled(!isEdit);
    JTextField fullNameInput =
        new JTextField(isEdit ? existingUser.getFullName() : "", 20);
    JTextField emailInput = new JTextField(isEdit ? existingUser.getEmail() : "", 20);
    JTextField addressInput =
        new JTextField(isEdit ? existingUser.getAddress() : "TP.HCM", 20);
    JTextField dobInput =
        AdminUIStyle.createDateField(
            isEdit
                ? existingUser.getDateOfBirth().format(AdminUIStyle.DATE_FMT)
                : "2002/01/15");
    JComboBox<String> genderInput =
        new JComboBox<>(new String[] {"Male", "Female", "Other"});
    if (isEdit) {
      genderInput.setSelectedItem(existingUser.getGender());
    }

    String[] labels = {
      "Username (*):",
      "Full Name (*):",
      "Email Address (*):",
      "Address (*):",
      "Date of Birth (YYYY/MM/DD):",
      "Gender:"
    };
    java.awt.Component[] fields = {
      usernameInput, fullNameInput, emailInput, addressInput, dobInput, genderInput
    };

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    for (int i = 0; i < labels.length; i++) {
      gbc.gridx = 0;
      gbc.gridy = i;
      gbc.weightx = 0.35;
      JLabel lbl = new JLabel(labels[i]);
      lbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
      form.add(lbl, gbc);

      gbc.gridx = 1;
      gbc.weightx = 0.65;
      if (fields[i] != dobInput) {
        fields[i].setFont(AdminUIStyle.FONT_BODY);
      }
      form.add(fields[i], gbc);
    }

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
    footer.setBackground(AdminUIStyle.CANVAS_BG);
    footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AdminUIStyle.BORDER_COLOR));

    JButton cancelBtn = AdminUIStyle.createSecondaryButton("Cancel");
    cancelBtn.addActionListener(e -> dialog.dispose());

    JButton saveBtn =
        AdminUIStyle.createPrimaryButton(isEdit ? "Save Changes" : "Create Account");
    saveBtn.addActionListener(
        e -> {
          String username = usernameInput.getText().trim();
          String fullName = fullNameInput.getText().trim();
          String email = emailInput.getText().trim();
          String address = addressInput.getText().trim();
          String dobStr = dobInput.getText().trim();
          String gender = String.valueOf(genderInput.getSelectedItem());

          if (username.isEmpty()
              || fullName.isEmpty()
              || email.isEmpty()
              || address.isEmpty()
              || AdminUIStyle.isDateMaskBlank(dobStr)) {
            JOptionPane.showMessageDialog(
                dialog,
                "All fields marked with (*) are required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          if (!email.contains("@")) {
            JOptionPane.showMessageDialog(
                dialog,
                "Please enter a valid email address containing '@'.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          LocalDate dob;
          try {
            if (!AdminUIStyle.isDateMaskComplete(dobStr)) {
              throw new DateTimeParseException("Incomplete date mask", dobStr, 0);
            }
            dob = LocalDate.parse(dobStr, AdminUIStyle.DATE_INPUT_FMT);
            if (!dob.isBefore(LocalDate.now())) {
              AdminUIStyle.setDateFieldError(
                  dobInput, true, "Date of birth must be in the past");
              JOptionPane.showMessageDialog(
                  dialog,
                  "Date of birth must be in the past.",
                  "Validation Error",
                  JOptionPane.ERROR_MESSAGE);
              return;
            }
            AdminUIStyle.setDateFieldError(dobInput, false, null);
          } catch (DateTimeParseException ex) {
            AdminUIStyle.setDateFieldError(
                dobInput, true, "Invalid calendar date: " + dobStr);
            JOptionPane.showMessageDialog(
                dialog,
                "Invalid Date of Birth ('"
                    + dobStr
                    + "'). Please enter a valid calendar date in YYYY/MM/DD format.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (!isEdit && dataStore.findUserByUsername(username).isPresent()) {
            JOptionPane.showMessageDialog(
                dialog,
                "Username '@" + username + "' already exists.",
                "Duplicate Username",
                JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (isEdit) {
            existingUser.setFullName(fullName);
            existingUser.setEmail(email);
            existingUser.setAddress(address);
            existingUser.setDateOfBirth(dob);
            existingUser.setGender(gender);
            dataStore.notifyListeners();
          } else {
            dataStore.addUser(username, fullName, email, address, dob, gender);
          }
          dialog.dispose();
        });

    footer.add(cancelBtn);
    footer.add(saveBtn);

    dialog.setLayout(new BorderLayout());
    dialog.add(form, BorderLayout.CENTER);
    dialog.add(footer, BorderLayout.SOUTH);
    dialog.setVisible(true);
  }

  private void handleLockToggle() {
    getSelectedUser()
        .ifPresent(
            user -> {
              boolean nowLocked = dataStore.toggleUserLock(user.getUsername());
              JOptionPane.showMessageDialog(
                  this,
                  "Account @"
                      + user.getUsername()
                      + " is now "
                      + (nowLocked ? "LOCKED" : "ACTIVE")
                      + ".",
                  "Account Status Updated",
                  JOptionPane.INFORMATION_MESSAGE);
            });
  }

  private void handlePasswordReset() {
    getSelectedUser()
        .ifPresent(
            user -> {
              JPasswordField newPassField = new JPasswordField(18);
              int result =
                  JOptionPane.showConfirmDialog(
                      this,
                      new Object[] {
                        "Enter new password for @"
                            + user.getUsername()
                            + " ("
                            + user.getFullName()
                            + "):",
                        newPassField
                      },
                      "Update / Reset User Password",
                      JOptionPane.OK_CANCEL_OPTION,
                      JOptionPane.PLAIN_MESSAGE);
              if (result == JOptionPane.OK_OPTION) {
                String newPass = new String(newPassField.getPassword()).trim();
                if (newPass.length() < 6) {
                  JOptionPane.showMessageDialog(
                      this,
                      "Password must be at least 6 characters long.",
                      "Invalid Password",
                      JOptionPane.ERROR_MESSAGE);
                  return;
                }
                user.setPasswordHash("bcrypt$updated$" + newPass.hashCode());
                JOptionPane.showMessageDialog(
                    this,
                    "Password for @" + user.getUsername() + " has been updated successfully.",
                    "Password Updated",
                    JOptionPane.INFORMATION_MESSAGE);
              }
            });
  }

  private void handleDeleteUser() {
    getSelectedUser()
        .ifPresent(
            user -> {
              int confirm =
                  JOptionPane.showConfirmDialog(
                      this,
                      "Are you sure you want to permanently delete user account @"
                          + user.getUsername()
                          + " ("
                          + user.getFullName()
                          + ")?",
                      "Confirm Delete Account",
                      JOptionPane.YES_NO_OPTION,
                      JOptionPane.WARNING_MESSAGE);
              if (confirm == JOptionPane.YES_OPTION) {
                dataStore.deleteUser(user.getUsername());
              }
            });
  }

  private void handleViewHistoryAndFriends() {
    getSelectedUser()
        .ifPresent(
            user -> {
              if (historyInspectorCallback != null) {
                historyInspectorCallback.accept(user);
              } else {
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                UserDetailHistoryDialog dialog = new UserDetailHistoryDialog(owner, user);
                dialog.setVisible(true);
              }
            });
  }
}
