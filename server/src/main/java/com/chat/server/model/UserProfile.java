package com.chat.server.model;

import com.chat.common.enums.Gender;
import java.time.LocalDate;

/** Editable profile columns of {@code users}. */
public record UserProfile(
    String fullName, String email, String address, LocalDate dateOfBirth, Gender gender) {}
