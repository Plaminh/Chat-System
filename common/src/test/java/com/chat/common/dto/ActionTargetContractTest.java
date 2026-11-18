package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.group.GroupTarget;
import com.chat.common.dto.social.FriendRequestIdTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ActionTargetContractTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void roundTripsFriendRequestIdentityWithoutUserIdentityAmbiguity() throws Exception {
    FriendRequestIdTarget target = new FriendRequestIdTarget(17);

    var json = mapper.valueToTree(target);
    FriendRequestIdTarget decoded = mapper.treeToValue(json, FriendRequestIdTarget.class);

    assertEquals(target, decoded);
    assertEquals(17, json.get("friendRequestId").asLong());
    assertFalse(json.has("targetUserId"));
  }

  @Test
  void roundTripsReusableGroupIdentity() throws Exception {
    GroupTarget target = new GroupTarget(23);

    var json = mapper.valueToTree(target);
    GroupTarget decoded = mapper.treeToValue(json, GroupTarget.class);

    assertEquals(target, decoded);
    assertEquals(23, json.get("groupId").asLong());
  }

  @Test
  void rejectsNonPositiveTargetIdentities() {
    assertThrows(IllegalArgumentException.class, () -> new FriendRequestIdTarget(0));
    assertThrows(IllegalArgumentException.class, () -> new GroupTarget(-1));
  }
}
