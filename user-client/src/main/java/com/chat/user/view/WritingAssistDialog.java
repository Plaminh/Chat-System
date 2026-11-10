package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class WritingAssistDialog extends JPanel {
  public enum Mode {
    SUGGEST("Suggest a new message"),
    REVISE("Revise the draft");

    private final String label;

    Mode(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private final String originalDraft;
  private final Consumer<String> acceptHandler;
  private final JComboBox<Mode> modeSelector = new JComboBox<>(Mode.values());
  private final JTextArea originalDraftView = new JTextArea(4, 36);
  private final JTextArea preview = new JTextArea(5, 36);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private JDialog dialog;
  private boolean accepted;

  public WritingAssistDialog(String draft, Consumer<String> acceptHandler) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    this.originalDraft = Objects.requireNonNull(draft, "draft");
    this.acceptHandler = Objects.requireNonNull(acceptHandler, "acceptHandler");
    buildContent();
    updatePreview();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Writing Assistant", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(620, 560));
    dialog.setSize(680, 600);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JComboBox<Mode> getModeSelector() {
    return modeSelector;
  }

  JTextArea getPreview() {
    return preview;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isAccepted() {
    return accepted;
  }

  void acceptPreview() {
    String proposed = preview.getText().trim();
    if (proposed.isEmpty()) {
      showFeedback("There is no preview content to insert.", ChatTheme.DANGER);
      return;
    }
    acceptHandler.accept(proposed);
    accepted = true;
    showFeedback("Sample preview accepted in the composer.", ChatTheme.SUCCESS);
    closeDialog();
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));
    originalDraftView.setText(originalDraft);
    originalDraftView.setEditable(false);
    originalDraftView.setLineWrap(true);
    originalDraftView.setWrapStyleWord(true);
    originalDraftView.setFont(ChatTheme.FONT_BODY);
    preview.setEditable(false);
    preview.setLineWrap(true);
    preview.setWrapStyleWord(true);
    preview.setFont(ChatTheme.FONT_BODY);
    preview.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ChatTheme.PRIMARY),
        BorderFactory.createEmptyBorder(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM,
            ChatTheme.SPACE_SM, ChatTheme.SPACE_SM)));

    JLabel title = new JLabel("Message Writing Assistant", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel modeLabel = new JLabel("Preview mode:");
    modeLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    modeLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    modeSelector.setFont(ChatTheme.FONT_BODY);
    modeSelector.addActionListener(event -> updatePreview());

    JPanel modeRow = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, 0));
    modeRow.setOpaque(false);
    modeRow.add(modeLabel, BorderLayout.WEST);
    modeRow.add(modeSelector, BorderLayout.CENTER);

    JPanel currentBlock = createTextBlock("Current Draft", originalDraftView);
    JPanel previewBlock = createTextBlock("Sample Suggestion", preview);
    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(modeRow, BorderLayout.SOUTH);
    content.add(heading, BorderLayout.NORTH);
    JPanel textBlocks = new JPanel(new GridLayout(1, 2, ChatTheme.SPACE_MD, 0));
    textBlocks.setOpaque(false);
    textBlocks.add(currentBlock);
    textBlocks.add(previewBlock);
    content.add(textBlocks, BorderLayout.CENTER);

    JLabel disclaimer =
        new JLabel("Local sample only; no language model or external service is called.");
    disclaimer.setFont(ChatTheme.FONT_SMALL);
    disclaimer.setForeground(ChatTheme.TEXT_MUTED);
    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.TEXT_MUTED);
    JPanel footerInfo = new JPanel(new BorderLayout(ChatTheme.SPACE_XS, ChatTheme.SPACE_XS));
    footerInfo.setOpaque(false);
    footerInfo.add(feedbackLabel, BorderLayout.NORTH);
    footerInfo.add(disclaimer, BorderLayout.SOUTH);

    JButton discard = ChatTheme.createSecondaryButton("Discard");
    discard.addActionListener(event -> closeDialog());
    JButton accept = ChatTheme.createPrimaryButton("Insert into Composer");
    accept.addActionListener(event -> acceptPreview());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(discard);
    actions.add(accept);
    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(footerInfo, BorderLayout.CENTER);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private JPanel createTextBlock(String titleText, JTextArea textArea) {
    JPanel block = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    block.setBackground(ChatTheme.SURFACE_WHITE);
    block.setBorder(ChatTheme.createCardBorder());
    JLabel title = new JLabel(titleText);
    title.setFont(ChatTheme.FONT_BODY_BOLD);
    title.setForeground(ChatTheme.TEXT_SECONDARY);
    block.add(title, BorderLayout.NORTH);
    block.add(new JScrollPane(textArea), BorderLayout.CENTER);
    return block;
  }

  private void updatePreview() {
    Mode mode = (Mode) modeSelector.getSelectedItem();
    String draft = originalDraft.trim().replaceAll("\\s+", " ");
    String proposed;
    if (mode == Mode.SUGGEST) {
      proposed =
          draft.isEmpty()
              ? "Could you share a little more detail so everyone has the full context?"
              : draft + " Thanks for sharing.";
    } else {
      proposed =
          draft.isEmpty()
              ? "Hi, could you share a little more information?"
              : "I would like to share: " + draft;
    }
    preview.setText(proposed);
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
