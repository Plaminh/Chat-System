package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.GroupChatInfo;
import com.chat.admin.model.GroupChatInfo.GroupMemberInfo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Group Chat Oversight & Member Roles Panel (Task 5 / Screen 5).
 *
 * <p>Covers Requirement 1.2.3: View list of group chats, filter by group name, sort by group name
 * or creation time, view members of a selected group, and view members with Group Admin privileges.
 */
public final class GroupManagementPanel extends JPanel {
  private static final String[] GROUP_COLUMNS = {
    "GROUP ID", "GROUP NAME", "CREATED DATE", "MEMBERS", "GROUP ADMINS"
  };
  private static final String[] MEMBER_COLUMNS = {"USERNAME", "FULL NAME", "ROLE IN GROUP"};

  private final AdminMockDataStore dataStore = AdminMockDataStore.getInstance();
  private final JTextField searchGroupField =
      AdminUIStyle.createSearchField("Filter group chats by group name...", 18);
  private final JComboBox<String> sortCombo =
      AdminUIStyle.createComboBox(
          new String[] {
            "Sort: Created Date (Newest)",
            "Sort: Created Date (Oldest)",
            "Sort: Group Name (A-Z)",
            "Sort: Group Name (Z-A)"
          });

  private final DefaultTableModel groupTableModel =
      new DefaultTableModel(GROUP_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable groupTable = new JTable(groupTableModel);
  private final JLabel groupCountLabel = new JLabel("Showing 0 group chats");

  // Right-side Roster Inspector Controls
  private final JLabel inspectorTitleLabel = new JLabel("Roster Inspector: Select a Group");
  private final JLabel inspectorMetaLabel = new JLabel("Created Date: —   |   Total Members: 0");
  private final JComboBox<String> roleFilterCombo =
      AdminUIStyle.createComboBox(
          new String[] {"View: All Group Members", "View: Group Admins Only"});
  private final JTextField memberSearchField =
      AdminUIStyle.createSearchField("Filter members by name or username...", 12);
  private final DefaultTableModel memberTableModel =
      new DefaultTableModel(MEMBER_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable memberTable = new JTable(memberTableModel);

  public GroupManagementPanel() {
    setLayout(new BorderLayout(0, 14));
    setBackground(AdminUIStyle.CANVAS_BG);

    add(buildTopFilterToolbar(), BorderLayout.NORTH);
    add(buildSplitWorkspace(), BorderLayout.CENTER);

    wireEvents();
    refreshGroupTable();
  }

  private JPanel buildTopFilterToolbar() {
    JPanel card = AdminUIStyle.createCardPanel(new BorderLayout(12, 6));

    JPanel leftControls = new JPanel(AdminUIStyle.createWrapFlowLayout(FlowLayout.LEFT, 10, 2));
    leftControls.setOpaque(false);

    JLabel filterLbl = new JLabel("Group Filter:");
    filterLbl.setFont(AdminUIStyle.FONT_BODY_BOLD);
    filterLbl.setForeground(AdminUIStyle.TEXT_SECONDARY);

    leftControls.add(filterLbl);
    leftControls.add(searchGroupField);
    leftControls.add(sortCombo);

    JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
    rightControls.setOpaque(false);

    JButton resetBtn = AdminUIStyle.createSecondaryButton("Reset Filter");
    resetBtn.addActionListener(
        e -> {
          searchGroupField.setText("");
          sortCombo.setSelectedIndex(0);
          roleFilterCombo.setSelectedIndex(0);
          memberSearchField.setText("");
          refreshGroupTable();
        });
    rightControls.add(resetBtn);

    card.add(leftControls, BorderLayout.CENTER);
    card.add(rightControls, BorderLayout.EAST);
    return card;
  }

  private JSplitPane buildSplitWorkspace() {
    JPanel leftPanel = buildLeftGroupListCard();
    JPanel rightPanel = buildRightMemberInspectorCard();
    leftPanel.setMinimumSize(new Dimension(320, 240));
    rightPanel.setMinimumSize(new Dimension(300, 240));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
    splitPane.setResizeWeight(0.56);
    splitPane.setContinuousLayout(true);
    splitPane.setDividerSize(10);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setOpaque(false);
    return splitPane;
  }

  private JPanel buildLeftGroupListCard() {
    JPanel panel = AdminUIStyle.createCardPanel(new BorderLayout(0, 10));
    panel.setPreferredSize(new Dimension(560, 500));

    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    JLabel title = new JLabel("Registered Group Chats");
    title.setFont(AdminUIStyle.FONT_SUBTITLE);
    title.setForeground(AdminUIStyle.TEXT_PRIMARY);
    groupCountLabel.setFont(AdminUIStyle.FONT_SMALL);
    groupCountLabel.setForeground(AdminUIStyle.TEXT_MUTED);
    header.add(title, BorderLayout.WEST);
    header.add(groupCountLabel, BorderLayout.EAST);

    AdminUIStyle.styleTable(groupTable);
    groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    groupTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    groupTable.getColumnModel().getColumn(1).setPreferredWidth(195);
    groupTable.getColumnModel().getColumn(2).setPreferredWidth(105);
    groupTable.getColumnModel().getColumn(3).setPreferredWidth(100);
    groupTable.getColumnModel().getColumn(4).setPreferredWidth(95);

    panel.add(header, BorderLayout.NORTH);
    panel.add(AdminUIStyle.wrapTableInScrollPane(groupTable), BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildRightMemberInspectorCard() {
    JPanel panel = AdminUIStyle.createCardPanel(new BorderLayout(0, 10));
    panel.setPreferredSize(new Dimension(410, 500));

    JPanel topSection = new JPanel(new BorderLayout(0, 8));
    topSection.setOpaque(false);

    JPanel metaBlock = new JPanel(new GridLayout(2, 1, 0, 3));
    metaBlock.setBackground(AdminUIStyle.SIDEBAR_BG);
    metaBlock.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    inspectorTitleLabel.setFont(AdminUIStyle.FONT_SUBTITLE);
    inspectorTitleLabel.setForeground(AdminUIStyle.SURFACE_WHITE);
    inspectorMetaLabel.setFont(AdminUIStyle.FONT_SMALL);
    inspectorMetaLabel.setForeground(new java.awt.Color(0x93C5FD));
    metaBlock.add(inspectorTitleLabel);
    metaBlock.add(inspectorMetaLabel);

    JPanel filterRow = new JPanel(new BorderLayout(8, 0));
    filterRow.setOpaque(false);
    filterRow.add(memberSearchField, BorderLayout.CENTER);
    filterRow.add(roleFilterCombo, BorderLayout.EAST);

    topSection.add(metaBlock, BorderLayout.NORTH);
    topSection.add(filterRow, BorderLayout.SOUTH);

    AdminUIStyle.styleTable(memberTable);
    memberTable.getColumnModel().getColumn(0).setPreferredWidth(115);
    memberTable.getColumnModel().getColumn(1).setPreferredWidth(145);
    memberTable.getColumnModel().getColumn(2).setPreferredWidth(125);
    memberTable
        .getColumnModel()
        .getColumn(2)
        .setCellRenderer(AdminUIStyle.createStatusBadgeRenderer());

    panel.add(topSection, BorderLayout.NORTH);
    panel.add(AdminUIStyle.wrapTableInScrollPane(memberTable), BorderLayout.CENTER);
    return panel;
  }

  private void wireEvents() {
    DocumentListener groupDocListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            refreshGroupTable();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            refreshGroupTable();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            refreshGroupTable();
          }
        };
    searchGroupField.getDocument().addDocumentListener(groupDocListener);
    sortCombo.addActionListener(e -> refreshGroupTable());

    groupTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                refreshMemberInspector();
              }
            });

    DocumentListener memberDocListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            refreshMemberInspector();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            refreshMemberInspector();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            refreshMemberInspector();
          }
        };
    memberSearchField.getDocument().addDocumentListener(memberDocListener);
    roleFilterCombo.addActionListener(e -> refreshMemberInspector());
  }

  private void refreshGroupTable() {
    String query =
        searchGroupField.getText() == null ? "" : searchGroupField.getText().trim().toLowerCase();
    int sortIdx = sortCombo.getSelectedIndex();

    List<GroupChatInfo> filtered = new ArrayList<>();
    for (GroupChatInfo g : dataStore.getGroups()) {
      if (query.isEmpty()
          || g.groupName().toLowerCase().contains(query)
          || g.groupId().toLowerCase().contains(query)) {
        filtered.add(g);
      }
    }

    Comparator<GroupChatInfo> comparator =
        switch (sortIdx) {
          case 1 -> Comparator.comparing(GroupChatInfo::createdAt);
          case 2 -> Comparator.comparing(g -> g.groupName().toLowerCase());
          case 3 ->
              Comparator.comparing((GroupChatInfo g) -> g.groupName().toLowerCase()).reversed();
          default -> Comparator.comparing(GroupChatInfo::createdAt).reversed();
        };
    filtered.sort(comparator);

    groupTableModel.setRowCount(0);
    for (GroupChatInfo g : filtered) {
      groupTableModel.addRow(
          new Object[] {
            "#" + g.groupId(),
            g.groupName(),
            g.createdAt().format(AdminUIStyle.DATE_FMT),
            g.getMemberCount() + " members",
            g.getAdminCount() + " admins"
          });
    }

    groupCountLabel.setText(
        "Showing " + filtered.size() + " of " + dataStore.getGroups().size() + " groups");

    if (groupTableModel.getRowCount() > 0) {
      groupTable.setRowSelectionInterval(0, 0);
    } else {
      refreshMemberInspector();
    }
  }

  private void refreshMemberInspector() {
    Optional<GroupChatInfo> selected = getSelectedGroup();
    memberTableModel.setRowCount(0);

    if (selected.isEmpty()) {
      inspectorTitleLabel.setText("Roster Inspector: No Group Selected");
      inspectorMetaLabel.setText("Select a group row on the left to inspect members and admins");
      return;
    }

    GroupChatInfo group = selected.get();
    inspectorTitleLabel.setText("Roster: #" + group.groupId() + " — " + group.groupName());
    inspectorMetaLabel.setText(
        "Created: "
            + group.createdAt().format(AdminUIStyle.DATE_FMT)
            + "   •   Members: "
            + group.getMemberCount()
            + "   •   Group Admins: "
            + group.getAdminCount());

    boolean adminsOnly = roleFilterCombo.getSelectedIndex() == 1;
    String memberQuery =
        memberSearchField.getText() == null
            ? ""
            : memberSearchField.getText().trim().toLowerCase();

    for (GroupMemberInfo m : group.members()) {
      if (adminsOnly && !m.groupAdmin()) {
        continue;
      }
      if (!memberQuery.isEmpty()
          && !m.username().toLowerCase().contains(memberQuery)
          && !m.fullName().toLowerCase().contains(memberQuery)) {
        continue;
      }
      memberTableModel.addRow(
          new Object[] {
            "@" + m.username(), m.fullName(), m.groupAdmin() ? "Group Admin" : "Member"
          });
    }
  }

  private Optional<GroupChatInfo> getSelectedGroup() {
    int row = groupTable.getSelectedRow();
    if (row < 0) {
      return Optional.empty();
    }
    String rawId = String.valueOf(groupTableModel.getValueAt(row, 0));
    String cleanId = rawId.startsWith("#") ? rawId.substring(1) : rawId;
    return dataStore.getGroups().stream()
        .filter(g -> g.groupId().equalsIgnoreCase(cleanId))
        .findFirst();
  }
}
