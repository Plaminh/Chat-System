package com.chat.user.view;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.Gender;
import com.chat.common.ui.ChatTheme;
import com.chat.user.model.UserProfile;
import com.chat.user.model.UserSessionState;
import com.chat.user.network.SocketClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class ProfileSettingsPanel extends JPanel {
  private final UserSessionState sessionState;
  private final SocketClient socketClient;
  private final BiFunction<ActionType, Object, CompletableFuture<ResponseEnvelope>> requestSender;
  private final JTextField fullNameField = ChatTheme.createTextField(24);
  private final JTextField usernameField = ChatTheme.createTextField(24);
  private final JTextField emailField = ChatTheme.createTextField(24);
  private final JTextField addressField = ChatTheme.createTextField(24);
  private final JTextField dateOfBirthField = ChatTheme.createTextField(24);
  private final JTextField genderField = ChatTheme.createTextField(24);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.RIGHT);
  private final JButton cancelButton = ChatTheme.createSecondaryButton("Cancel");
  private final JButton saveButton = ChatTheme.createPrimaryButton("Save Changes");
  private boolean profileLoaded;
  private boolean requestInProgress;

  public ProfileSettingsPanel(UserSessionState sessionState) {
    this(sessionState, null);
  }

  public ProfileSettingsPanel(UserSessionState sessionState, SocketClient socketClient) {
    this(
        sessionState,
        socketClient,
        socketClient == null
            ? (action, payload) ->
                CompletableFuture.failedFuture(
                    new IllegalStateException("Socket client is not configured"))
            : socketClient::sendAsync);
  }

  ProfileSettingsPanel(
      UserSessionState sessionState,
      SocketClient socketClient,
      BiFunction<ActionType, Object, CompletableFuture<ResponseEnvelope>> requestSender) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    this.sessionState = Objects.requireNonNull(sessionState, "sessionState");
    this.socketClient = socketClient;
    this.requestSender = Objects.requireNonNull(requestSender, "requestSender");
    buildContent();
    setEditableFieldsEnabled(false);
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

  void refreshProfile() {
    if (requestInProgress) {
      return;
    }
    requestInProgress = true;
    profileLoaded = false;
    clearFields();
    setEditableFieldsEnabled(false);
    showFeedback("Loading profile...", ChatTheme.TEXT_MUTED);
    requestSender
        .apply(ActionType.GET_PROFILE, null)
        .whenComplete(
            (response, error) ->
                SwingUtilities.invokeLater(() -> handleProfileResponse(response, error, true)));
  }

  void saveProfile() {
    if (!profileLoaded || requestInProgress) {
      return;
    }
    String fullName = fullNameField.getText().trim();
    String email = emailField.getText().trim();
    String address = addressField.getText().trim();
    String dateOfBirth = dateOfBirthField.getText().trim();
    String genderText = genderField.getText().trim();

    if (fullName.isEmpty() || email.isEmpty() || address.isEmpty() || dateOfBirth.isEmpty()) {
      showFeedback("Complete all required fields.", ChatTheme.DANGER);
      return;
    }
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      showFeedback("Enter a valid email address.", ChatTheme.DANGER);
      return;
    }

    LocalDate parsedDate;
    try {
      parsedDate = LocalDate.parse(dateOfBirth);
    } catch (DateTimeParseException exception) {
      showFeedback("Date of birth must use the YYYY-MM-DD format.", ChatTheme.DANGER);
      return;
    }
    if (!parsedDate.isBefore(LocalDate.now())) {
      showFeedback("Date of birth must be in the past.", ChatTheme.DANGER);
      return;
    }

    Gender gender;
    try {
      gender =
          genderText.isEmpty() || genderText.equalsIgnoreCase("Not specified")
              ? null
              : Gender.valueOf(genderText.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      showFeedback("Gender must be MALE, FEMALE, OTHER, or Not specified.", ChatTheme.DANGER);
      return;
    }

    UpdateProfileRequest payload =
        new UpdateProfileRequest(fullName, email, address, parsedDate, gender);
    requestInProgress = true;
    setEditableFieldsEnabled(false);
    showFeedback("Saving profile...", ChatTheme.TEXT_MUTED);
    requestSender
        .apply(ActionType.UPDATE_PROFILE, payload)
        .whenComplete(
            (response, error) ->
                SwingUtilities.invokeLater(() -> handleProfileResponse(response, error, false)));
  }

  void cancelChanges() {
    if (profileLoaded && !requestInProgress) {
      displayProfile(sessionState.getProfile());
      showFeedback("Changes discarded.", ChatTheme.TEXT_MUTED);
    }
  }

  private void handleProfileResponse(ResponseEnvelope response, Throwable error, boolean loading) {
    requestInProgress = false;
    if (error != null) {
      setEditableFieldsEnabled(!loading && profileLoaded);
      showFeedback(
          loading ? "Could not load profile from the server." : "Could not save profile.",
          ChatTheme.DANGER);
      return;
    }
    if (response == null || !response.success()) {
      setEditableFieldsEnabled(!loading && profileLoaded);
      showFeedback(
          response != null && response.message() != null
              ? response.message()
              : loading ? "Could not load profile from the server." : "Could not save profile.",
          ChatTheme.DANGER);
      return;
    }
    if (socketClient == null || response.data() == null) {
      setEditableFieldsEnabled(!loading && profileLoaded);
      showFeedback("The server returned an invalid profile response.", ChatTheme.DANGER);
      return;
    }
    try {
      UserAccountDetail detail = socketClient.decodeData(response.data(), UserAccountDetail.class);
      if (detail == null) {
        setEditableFieldsEnabled(!loading && profileLoaded);
        showFeedback("The server returned an invalid profile response.", ChatTheme.DANGER);
        return;
      }
      UserProfile profile = toUserProfile(detail);
      sessionState.updateProfile(profile);
      displayProfile(profile);
      profileLoaded = true;
      setEditableFieldsEnabled(true);
      showFeedback(
          loading ? "Profile loaded from server." : "Profile updated successfully.",
          ChatTheme.SUCCESS);
    } catch (JsonProcessingException exception) {
      setEditableFieldsEnabled(!loading && profileLoaded);
      showFeedback("The server returned an invalid profile response.", ChatTheme.DANGER);
    }
  }

  private UserProfile toUserProfile(UserAccountDetail detail) {
    return new UserProfile(
        detail.summary().username(),
        detail.summary().fullName(),
        detail.email(),
        detail.address(),
        detail.dateOfBirth(),
        detail.gender() == null ? "Not specified" : detail.gender().name());
  }

  private void buildContent() {
    setBackground(ChatTheme.CANVAS_BG);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));

    JPanel profileCard = ChatTheme.createCardPanel();
    profileCard.setLayout(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    JLabel title = new JLabel("Profile & Account");
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("View and edit your account details.");
    subtitle.setFont(ChatTheme.FONT_BODY);
    subtitle.setForeground(ChatTheme.TEXT_MUTED);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_XS, ChatTheme.SPACE_XS));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(subtitle, BorderLayout.SOUTH);

    JPanel fields = new JPanel(new GridLayout(0, 2, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    fields.setOpaque(false);
    addField(fields, "Full name", fullNameField);
    usernameField.setEditable(false);
    addField(fields, "Username", usernameField);
    addField(fields, "Email", emailField);
    addField(fields, "Address", addressField);
    addField(fields, "Date of birth (YYYY-MM-DD)", dateOfBirthField);
    addField(fields, "Gender", genderField);

    cancelButton.addActionListener(event -> cancelChanges());
    saveButton.addActionListener(event -> saveProfile());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(feedbackLabel);
    actions.add(cancelButton);
    actions.add(saveButton);

    JPanel profileContent = new JPanel(new BorderLayout(ChatTheme.SPACE_LG, ChatTheme.SPACE_LG));
    profileContent.setOpaque(false);
    profileContent.add(heading, BorderLayout.NORTH);
    profileContent.add(fields, BorderLayout.CENTER);
    profileContent.add(actions, BorderLayout.SOUTH);
    profileCard.add(profileContent, BorderLayout.CENTER);

    JPanel passwordCard = ChatTheme.createCardPanel();
    JLabel passwordInfo =
        new JLabel(
            "<html><b>Account Password</b><br>Change your password using the sample form.</html>");
    passwordInfo.setFont(ChatTheme.FONT_BODY);
    passwordInfo.setForeground(ChatTheme.TEXT_SECONDARY);
    JButton changePasswordButton = ChatTheme.createPrimaryButton("Change Password");
    changePasswordButton.addActionListener(event -> showChangePasswordDialog());
    passwordCard.add(passwordInfo, BorderLayout.CENTER);
    passwordCard.add(changePasswordButton, BorderLayout.EAST);

    add(profileCard, BorderLayout.CENTER);
    add(passwordCard, BorderLayout.SOUTH);
  }

  private void addField(JPanel parent, String labelText, JTextField field) {
    JPanel group = new JPanel(new BorderLayout(ChatTheme.SPACE_XS, ChatTheme.SPACE_XS));
    group.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setFont(ChatTheme.FONT_BODY_BOLD);
    label.setForeground(ChatTheme.TEXT_SECONDARY);
    group.add(label, BorderLayout.NORTH);
    group.add(field, BorderLayout.CENTER);
    parent.add(group);
  }

  private void displayProfile(UserProfile profile) {
    fullNameField.setText(profile.fullName());
    usernameField.setText(profile.username());
    emailField.setText(profile.email());
    addressField.setText(profile.address());
    dateOfBirthField.setText(profile.dateOfBirth().toString());
    genderField.setText(profile.gender());
  }

  private void clearFields() {
    fullNameField.setText("");
    usernameField.setText("");
    emailField.setText("");
    addressField.setText("");
    dateOfBirthField.setText("");
    genderField.setText("");
  }

  private void setEditableFieldsEnabled(boolean enabled) {
    fullNameField.setEnabled(enabled);
    emailField.setEnabled(enabled);
    addressField.setEnabled(enabled);
    dateOfBirthField.setEnabled(enabled);
    genderField.setEnabled(enabled);
    usernameField.setEnabled(false);
    saveButton.setEnabled(enabled);
    cancelButton.setEnabled(enabled);
  }

  private void showChangePasswordDialog() {
    ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(socketClient);
    changePasswordDialog.createDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
