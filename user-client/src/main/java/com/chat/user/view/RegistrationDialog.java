package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class RegistrationDialog extends JPanel {
  private final JTextField fullNameField = ChatTheme.createTextField(28);
  private final JTextField usernameField = ChatTheme.createTextField(28);
  private final JTextField emailField = ChatTheme.createTextField(28);
  private final JTextField addressField = ChatTheme.createTextField(28);
  private final JTextField dateOfBirthField = ChatTheme.createTextField(28);
  private final JTextField genderField = ChatTheme.createTextField(28);
  private final JPasswordField passwordField = new JPasswordField(28);
  private final JPasswordField confirmPasswordField = new JPasswordField(28);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private JDialog dialog;
  private UserProfile submittedProfile;

  public RegistrationDialog() {
    super(new BorderLayout());
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Create Account", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(560, 700));
    dialog.setSize(560, 720);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextField getFullNameField() {
    return fullNameField;
  }

  JTextField getUsernameField() {
    return usernameField;
  }

  JTextField getEmailField() {
    return emailField;
  }

  JTextField getAddressField() {
    return addressField;
  }

  JTextField getDateOfBirthField() {
    return dateOfBirthField;
  }

  JTextField getGenderField() {
    return genderField;
  }

  JPasswordField getPasswordField() {
    return passwordField;
  }

  JPasswordField getConfirmPasswordField() {
    return confirmPasswordField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  UserProfile getSubmittedProfile() {
    return submittedProfile;
  }

  void submitRegistration() {
    char[] password = passwordField.getPassword();
    char[] confirmation = confirmPasswordField.getPassword();
    boolean passwordsMatch = Arrays.equals(password, confirmation);
    boolean passwordBlank = isBlank(password);
    Arrays.fill(password, '\0');
    Arrays.fill(confirmation, '\0');
    passwordField.setText("");
    confirmPasswordField.setText("");

    String fullName = fullNameField.getText().trim();
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String address = addressField.getText().trim();
    String dateOfBirth = dateOfBirthField.getText().trim();
    String gender = genderField.getText().trim();

    if (fullName.isEmpty()
        || username.isEmpty()
        || email.isEmpty()
        || address.isEmpty()
        || dateOfBirth.isEmpty()
        || gender.isEmpty()
        || passwordBlank) {
      showFeedback("Complete all required fields.", ChatTheme.DANGER);
      return;
    }
    if (!isValidEmail(email)) {
      showFeedback("Enter a valid email address.", ChatTheme.DANGER);
      return;
    }
    if (!passwordsMatch) {
      showFeedback("The passwords do not match.", ChatTheme.DANGER);
      return;
    }

    LocalDate parsedDate;
    try {
      parsedDate = LocalDate.parse(dateOfBirth);
    } catch (DateTimeParseException exception) {
      showFeedback("Date of birth must use the YYYY-MM-DD format.", ChatTheme.DANGER);
      return;
    }

    submittedProfile =
        new UserProfile(username, fullName, email, address, parsedDate, gender);
    showFeedback("Sample registration complete; no account was created on the server.", ChatTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(BorderFactory.createEmptyBorder(ChatTheme.SPACE_MD, ChatTheme.SPACE_LG,
        ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));

    JPanel form = new JPanel();
    form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
    form.setBackground(ChatTheme.SURFACE_WHITE);
    addField(form, "Full name", fullNameField);
    addField(form, "Username", usernameField);
    addField(form, "Email address", emailField);
    addField(form, "Address", addressField);
    addField(form, "Date of birth (YYYY-MM-DD)", dateOfBirthField);
    addField(form, "Gender", genderField);
    addField(form, "Password", passwordField);
    addField(form, "Confirm password", confirmPasswordField);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);
    form.add(feedbackLabel);

    JButton submitButton = ChatTheme.createPrimaryButton("Create Sample Account");
    submitButton.addActionListener(event -> submitRegistration());
    JButton cancelButton = ChatTheme.createSecondaryButton("Cancel");
    cancelButton.addActionListener(event -> cancelRegistration());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JLabel title = new JLabel("Create a New Account", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel subtitle =
        new JLabel("Enter the required account details", SwingConstants.CENTER);
    subtitle.setFont(ChatTheme.FONT_BODY);
    subtitle.setForeground(ChatTheme.TEXT_MUTED);
    JPanel heading = new JPanel(new BorderLayout(0, ChatTheme.SPACE_XS));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(subtitle, BorderLayout.SOUTH);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setBackground(ChatTheme.SURFACE_WHITE);
    content.add(heading, BorderLayout.NORTH);
    content.add(new JScrollPane(form), BorderLayout.CENTER);
    content.add(actions, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void addField(JPanel form, String labelText, JTextField field) {
    JLabel label = new JLabel(labelText);
    label.setFont(ChatTheme.FONT_BODY_BOLD);
    label.setForeground(ChatTheme.TEXT_SECONDARY);
    form.add(label);
    form.add(field);
    form.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
  }

  private void cancelRegistration() {
    submittedProfile = null;
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  }

  private boolean isBlank(char[] value) {
    for (char character : value) {
      if (!Character.isWhitespace(character)) {
        return false;
      }
    }
    return true;
  }
}
