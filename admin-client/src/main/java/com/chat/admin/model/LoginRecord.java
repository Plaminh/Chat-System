package com.chat.admin.model;

import java.time.LocalDateTime;

/** Represents a chronological user login record (Requirement 1.2.2). */
public record LoginRecord(LocalDateTime timestamp, String username, String fullName) {}
