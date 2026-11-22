package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.ComparisonOperator;
import com.chat.common.enums.FriendRequestDirection;
import com.chat.common.enums.FriendRequestStatus;
import com.chat.common.enums.SortDirection;
import com.chat.server.model.FriendRequest;
import com.chat.server.model.FriendRequestView;
import com.chat.server.model.FriendStatisticRow;
import com.chat.server.model.UserBrief;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class SocialRepositoryTest extends RepositoryTestSupport {
  @Autowired FriendRequestRepository requests;
  @Autowired FriendshipRepository friendships;
  @Autowired UserBlockRepository blocks;

  @Test
  void allowsOnePendingRequestPerPairAndResolvesItOnce() {
    inRollback(
        () -> {
          long alice = newUser("sReqA");
          long bob = newUser("sReqB");
          long id = requests.insertPending(alice, bob);

          assertFailsInSavepoint(
              DataIntegrityViolationException.class, () -> requests.insertPending(bob, alice));
          assertEquals(id, requests.findPendingBetween(bob, alice).orElseThrow().friendRequestId());
          assertThrows(
              IllegalArgumentException.class,
              () -> requests.resolve(id, FriendRequestStatus.PENDING));

          assertTrue(requests.resolve(id, FriendRequestStatus.ACCEPTED));
          assertFalse(requests.resolve(id, FriendRequestStatus.DECLINED));
          FriendRequest stored = requests.findById(id).orElseThrow();
          assertEquals(FriendRequestStatus.ACCEPTED, stored.status());
          assertTrue(requests.findPendingBetween(alice, bob).isEmpty());

          long again = requests.insertPending(bob, alice);
          assertEquals(1, requests.cancelPendingBetween(alice, bob));
          assertEquals(
              FriendRequestStatus.CANCELED, requests.findById(again).orElseThrow().status());
        });
  }

  @Test
  void listsRequestsByDirectionStatusAndOtherUsersName() {
    inRollback(
        () -> {
          long me = newUser("sReqMe");
          long incoming = newUser("sReqIn");
          long outgoing = newUser("sReqOut");
          long inId = requests.insertPending(incoming, me);
          long outId = requests.insertPending(me, outgoing);

          assertEquals(
              List.of(inId),
              ids(
                  requests
                      .findForUser(me, FriendRequestDirection.INCOMING, null, null, FIRST_PAGE)
                      .items()));
          assertEquals(
              List.of(outId),
              ids(
                  requests
                      .findForUser(me, FriendRequestDirection.OUTGOING, null, null, FIRST_PAGE)
                      .items()));
          assertEquals(
              Set.of(inId, outId),
              Set.copyOf(ids(requests.findForUser(me, null, null, null, FIRST_PAGE).items())));
          assertEquals(
              List.of(outId),
              ids(requests.findForUser(me, null, null, "sreqout", FIRST_PAGE).items()),
              "the query matches the other user, not the current user");
          assertEquals(
              0,
              requests
                  .findForUser(me, null, FriendRequestStatus.ACCEPTED, null, FIRST_PAGE)
                  .totalItems());

          FriendRequestView view =
              requests
                  .findForUser(me, FriendRequestDirection.INCOMING, null, null, FIRST_PAGE)
                  .items()
                  .get(0);
          assertEquals(incoming, view.sender().userId());
          assertEquals(me, view.receiver().userId());
        });
  }

  @Test
  void storesFriendshipsCanonicallyInEitherOrder() {
    inRollback(
        () -> {
          long a = newUser("sFrA");
          long b = newUser("sFrB");

          assertTrue(friendships.insert(b, a, null));
          assertFalse(friendships.insert(a, b, null), "the pair is stored once");
          assertTrue(friendships.areFriends(a, b));
          assertEquals(
              List.of(Math.min(a, b)),
              jdbc.queryForList(
                  "SELECT user_low_id FROM friendships WHERE user_low_id = ? OR user_high_id = ?",
                  Long.class,
                  Math.min(a, b),
                  Math.min(a, b)));
          assertEquals(List.of(b), friendships.findFriendIds(a));
          assertThrows(IllegalArgumentException.class, () -> friendships.areFriends(a, a));

          assertTrue(friendships.delete(a, b));
          assertFalse(friendships.delete(b, a));
          assertFalse(friendships.areFriends(a, b));
        });
  }

  @Test
  void listsFriendsWithQueryAndHidesDeletedAccounts() {
    inRollback(
        () -> {
          long me = newUser("sFlMe");
          long anna = newUser("sFlAnna");
          long binh = newUser("sFlBinh");
          long gone = newUser("sFlGone");
          friendships.insert(me, anna, null);
          friendships.insert(me, binh, null);
          friendships.insert(me, gone, null);
          users.softDelete(gone);

          assertEquals(
              List.of(anna, binh), briefIds(friendships.findFriends(me, null, FIRST_PAGE).items()));
          assertEquals(
              List.of(binh), briefIds(friendships.findFriends(me, "sflbinh", FIRST_PAGE).items()));
          assertEquals(
              3, friendships.findAllFriends(me).size(), "Admin view keeps deleted friends");
        });
  }

  @Test
  void countsDirectFriendsAndDistinctFriendsOfFriends() {
    inRollback(
        () -> {
          // me - a, me - b, a - b, a - x, b - x, b - y : friends of friends of me are x and y.
          long me = newUser("sStMe");
          long a = newUser("sStA");
          long b = newUser("sStB");
          long x = newUser("sStX");
          long y = newUser("sStY");
          friendships.insert(me, a, null);
          friendships.insert(me, b, null);
          friendships.insert(a, b, null);
          friendships.insert(a, x, null);
          friendships.insert(b, x, null);
          friendships.insert(b, y, null);

          assertEquals(2, friendships.countFriendsOfFriends(me));
          users.softDelete(y);

          SortSpec byName = new SortSpec("name", SortDirection.ASC);
          FriendStatisticRow row =
              friendships.findStatistics("sStMe", null, null, byName, FIRST_PAGE).items().get(0);
          assertEquals(me, row.user().userId());
          assertEquals(2, row.directFriends());
          assertEquals(2, row.friendsOfFriends());

          List<Long> threeDirect =
              friendships
                  .findStatistics(
                      "sSt", new NumericFilter(ComparisonOperator.EQ, 3), null, byName, FIRST_PAGE)
                  .items()
                  .stream()
                  .map(r -> r.user().userId())
                  .toList();
          assertEquals(List.of(a), threeDirect, "a: me, b, x; b has four direct friends");

          assertEquals(
              List.of(y),
              friendships
                  .findStatistics(
                      "sSt",
                      new NumericFilter(ComparisonOperator.LT, 2),
                      new NumericFilter(ComparisonOperator.GT, 2),
                      byName,
                      FIRST_PAGE)
                  .items()
                  .stream()
                  .map(r -> r.user().userId())
                  .toList(),
              "soft-deleted y stays listed: 1 direct friend (b), 3 friends of friends (me, a, x)");
        });
  }

  @Test
  void blocksAreDirected() {
    inRollback(
        () -> {
          long a = newUser("sBlA");
          long b = newUser("sBlB");

          assertTrue(blocks.block(a, b));
          assertFalse(blocks.block(a, b));
          assertTrue(blocks.isBlocked(a, b));
          assertFalse(blocks.isBlocked(b, a));
          assertTrue(blocks.isBlockedEitherWay(b, a));
          assertEquals(List.of(b), briefIds(blocks.findBlockedBy(a)));

          assertTrue(blocks.unblock(a, b));
          assertFalse(blocks.unblock(a, b));
          assertFalse(blocks.isBlockedEitherWay(a, b));
        });
  }

  private static List<Long> ids(List<FriendRequestView> views) {
    return views.stream().map(FriendRequestView::friendRequestId).toList();
  }

  private static List<Long> briefIds(List<UserBrief> briefs) {
    return briefs.stream().map(UserBrief::userId).toList();
  }
}
