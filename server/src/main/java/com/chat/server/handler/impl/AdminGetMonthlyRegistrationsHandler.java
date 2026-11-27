package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.common.YearRequest;
import com.chat.common.dto.statistics.MonthlyCount;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Handles {@link ActionType#ADMIN_GET_MONTHLY_REGISTRATIONS} to return 12-month registration
 * distribution for a selected year (Requirement 1.2.6 / Issue #15 / [A6]).
 */
@Component
public class AdminGetMonthlyRegistrationsHandler implements ActionHandler {
  private final ObjectMapper mapper;
  private final UserRepository userRepository;

  public AdminGetMonthlyRegistrationsHandler(ObjectMapper mapper, UserRepository userRepository) {
    this.mapper = mapper;
    this.userRepository = userRepository;
  }

  @Override
  public ActionType action() {
    return ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    String requestId = request.requestId();
    YearRequest yearRequest;
    try {
      yearRequest = ProtocolValidator.decodePayload(mapper, request, YearRequest.class);
    } catch (IllegalArgumentException | ProtocolValidationException ex) {
      return new ResponseEnvelope(
          requestId, false, null, ErrorCode.VALIDATION_FAILED, ex.getMessage());
    }

    int year = yearRequest.year();
    long[] monthlyCounts = userRepository.countMonthlyRegistrations(year);

    List<MonthlyCount> result = new ArrayList<>(12);
    for (int month = 1; month <= 12; month++) {
      long count = (monthlyCounts != null && month <= monthlyCounts.length)
          ? monthlyCounts[month - 1]
          : 0L;
      result.add(new MonthlyCount(year, month, count));
    }

    return new ResponseEnvelope(requestId, true, mapper.valueToTree(result), null, null);
  }
}
