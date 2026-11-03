package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.UserProfile;
import com.chat.user.model.UserSessionState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class ProfileSettingsPanel extends JPanel {
  private final UserSessionState sessionState;
  private final JTextField fullNameField = UserUiTheme.createTextField(24);
  private final JTextField usernameField = UserUiTheme.createTextField(24);
  private final JTextField emailField = UserUiTheme.createTextField(24);
  private final JTextField addressField = UserUiTheme.createTextField(24);
  private final JTextField dateOfBirthField = UserUiTheme.createTextField(24);
  private final JTextField genderField = UserUiTheme.createTextField(24);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.RIGHT);

  public ProfileSettingsPanel(UserSessionState sessionState) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    buildContent();
    loadProfile();
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

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  void saveProfile() {
    String fullName = fullNameField.getText().trim();
    String email = emailField.getText().trim();
    String address = addressField.getText().trim();
    String dateOfBirth = dateOfBirthField.getText().trim();
    String gender = genderField.getText().trim();

    if (fullName.isEmpty() || email.isEmpty() || address.isEmpty() || dateOfBirth.isEmpty()
        || gender.isEmpty()) {
      showFeedback("Complete all required fields.", UserUiTheme.DANGER);
      return;
    }
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      showFeedback("Enter a valid email address.", UserUiTheme.DANGER);
      return;
    }

    LocalDate parsedDate;
    try {
      parsedDate = LocalDate.parse(dateOfBirth);
    } catch (DateTimeParseException exception) {
      showFeedback("Date of birth must use the YYYY-MM-DD format.", UserUiTheme.DANGER);
      return;
    }

    UserProfile oldProfile = sessionState.getProfile();
    sessionState.updateProfile(
        new UserProfile(
            oldProfile.username(), fullName, email, address, parsedDate, gender));
    loadProfile();
    showFeedback("Sample profile updated for this session.", UserUiTheme.SUCCESS);
  }

  void cancelChanges() {
    loadProfile();
    showFeedback(" ", UserUiTheme.TEXT_MUTED);
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));

    JPanel profileCard = UserUiTheme.createCardPanel();
    profileCard.setLayout(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    JLabel title = new JLabel("Profile & Account");
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("View and edit your account details.");
    subtitle.setFont(UserUiTheme.FONT_BODY);
    subtitle.setForeground(UserUiTheme.TEXT_MUTED);
    JPanel heading = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(subtitle, BorderLayout.SOUTH);

    JPanel fields = new JPanel(new GridLayout(0, 2, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    fields.setOpaque(false);
    addField(fields, "Full name", fullNameField);
    usernameField.setEditable(false);
    addField(fields, "Username", usernameField);
    addField(fields, "Email", emailField);
    addField(fields, "Address", addressField);
    addField(fields, "Date of birth (YYYY-MM-DD)", dateOfBirthField);
    addField(fields, "Gender", genderField);

    JButton cancelButton = UserUiTheme.createSecondaryButton("Cancel");
    cancelButton.addActionListener(event -> cancelChanges());
    JButton saveButton = UserUiTheme.createPrimaryButton("Save Changes");
    saveButton.addActionListener(event -> saveProfile());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(feedbackLabel);
    actions.add(cancelButton);
    actions.add(saveButton);

    JPanel profileContent = new JPanel(new BorderLayout(UserUiTheme.SPACE_LG, UserUiTheme.SPACE_LG));
    profileContent.setOpaque(false);
    profileContent.add(heading, BorderLayout.NORTH);
    profileContent.add(fields, BorderLayout.CENTER);
    profileContent.add(actions, BorderLayout.SOUTH);
    profileCard.add(profileContent, BorderLayout.CENTER);

    JPanel passwordCard = UserUiTheme.createCardPanel();
    JLabel passwordInfo = new JLabel("<html><b>Account Password</b><br>Change your password using the sample form.</html>");
    passwordInfo.setFont(UserUiTheme.FONT_BODY);
    passwordInfo.setForeground(UserUiTheme.TEXT_SECONDARY);
    JButton changePasswordButton = UserUiTheme.createPrimaryButton("Change Password");
    changePasswordButton.addActionListener(event -> showChangePasswordDialog());
    passwordCard.add(passwordInfo, BorderLayout.CENTER);
    passwordCard.add(changePasswordButton, BorderLayout.EAST);

    add(profileCard, BorderLayout.CENTER);
    add(passwordCard, BorderLayout.SOUTH);
  }

  private void addField(JPanel parent, String labelText, JTextField field) {
    JPanel group = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
    group.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setFont(UserUiTheme.FONT_BODY_BOLD);
    label.setForeground(UserUiTheme.TEXT_SECONDARY);
    group.add(label, BorderLayout.NORTH);
    group.add(field, BorderLayout.CENTER);
    parent.add(group);
  }

  private void loadProfile() {
    UserProfile profile = sessionState.getProfile();
    fullNameField.setText(profile.fullName());
    usernameField.setText(profile.username());
    emailField.setText(profile.email());
    addressField.setText(profile.address());
    dateOfBirthField.setText(profile.dateOfBirth().toString());
    genderField.setText(profile.gender());
  }

  private void showChangePasswordDialog() {
    ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
    changePasswordDialog
        .createDialog(SwingUtilities.getWindowAncestor(this))
        .setVisible(true);
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
