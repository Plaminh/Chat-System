package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.SortDirection;
import com.chat.common.enums.UserRole;
import com.chat.server.exception.InvalidSortFieldException;
import com.chat.server.model.User;
import com.chat.server.model.UserBrief;
import com.chat.server.model.UserProfile;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

class UserRepositoryTest extends RepositoryTestSupport {
  @Autowired UserBlockRepository blocks;
  @Autowired UserPublicKeyRepository publicKeys;
  @Autowired LoginHistoryRepository loginHistory;

  @Test
  void insertsAndFindsUsersCaseInsensitively() {
    inRollback(
        () -> {
          long id = newUser("uRepoFind", UserRole.ADMIN);
          User user = users.findById(id).orElseThrow();

          assertEquals(UserRole.ADMIN, user.role());
          assertEquals(AccountStatus.ACTIVE, user.accountStatus());
          assertEquals(Gender.OTHER, user.gender());
          assertNotNull(user.createdAt());
          assertEquals(
              id, users.findByUsername(user.username().toUpperCase()).orElseThrow().userId());
          assertEquals(id, users.findByEmail(user.email().toUpperCase()).orElseThrow().userId());
          assertTrue(users.existsByUsername(user.username().toLowerCase()));
          assertTrue(users.existsByEmail(user.email(), 0));
          assertFalse(users.existsByEmail(user.email(), id));
          assertFalse(user.toString().contains(HASH), "toString must not expose the hash");
        });
  }

  @Test
  void rejectsDuplicateUsernameIgnoringCase() {
    inRollback(
        () -> {
          long id = newUser("uRepoDup");
          User user = users.findById(id).orElseThrow();
          assertFailsInSavepoint(
              DuplicateKeyException.class,
              () ->
                  users.insert(
                      new com.chat.server.model.NewUser(
                          user.username().toUpperCase(),
                          HASH,
                          "other_" + user.email(),
                          "Other Name",
                          "Street",
                          LocalDate.of(2001, 2, 3),
                          null,
                          UserRole.USER)));
        });
  }

  @Test
  void updatesProfileRoleAndPassword() {
    inRollback(
        () -> {
          long id = newUser("uRepoUpd");
          UserProfile profile =
              new UserProfile(
                  "Nguyen Van Updated",
                  "updated_" + id + "@test.example",
                  "New Address",
                  LocalDate.of(1999, 5, 6),
                  Gender.FEMALE);

          assertTrue(users.updateProfile(id, profile));
          assertTrue(users.updateProfileAndRole(id, profile, UserRole.ADMIN));
          assertTrue(users.updatePasswordHash(id, HASH.replace('K', 'L')));
          assertFalse(users.updateProfile(Long.MAX_VALUE, profile));

          User user = users.findById(id).orElseThrow();
          assertEquals("Nguyen Van Updated", user.fullName());
          assertEquals(Gender.FEMALE, user.gender());
          assertEquals(LocalDate.of(1999, 5, 6), user.dateOfBirth());
          assertEquals(UserRole.ADMIN, user.role());
          assertTrue(user.passwordHash().endsWith("L"));
        });
  }

  @Test
  void followsLockUnlockAndTerminalDeleteTransitions() {
    inRollback(
        () -> {
          long id = newUser("uRepoLock");

          assertFalse(users.unlock(id), "an active account cannot be unlocked");
          assertTrue(users.lock(id));
          assertFalse(users.lock(id), "a locked account cannot be locked again");
          User locked = users.findById(id).orElseThrow();
          assertEquals(AccountStatus.LOCKED, locked.accountStatus());
          assertNotNull(locked.lockedAt());

          assertTrue(users.unlock(id));
          assertNull(users.findById(id).orElseThrow().lockedAt());

          assertTrue(users.lock(id));
          assertTrue(users.softDelete(id), "a locked account can be deleted");
          User deleted = users.findById(id).orElseThrow();
          assertEquals(AccountStatus.DELETED, deleted.accountStatus());
          assertNull(deleted.lockedAt());
          assertNotNull(deleted.deletedAt());
          assertFalse(users.softDelete(id));
          assertFalse(users.lock(id));
          assertFalse(users.unlock(id));
        });
  }

  @Test
  void filtersSortsAndPagesAdminList() {
    inRollback(
        () -> {
          long a = newUser("uRepoAdm");
          long b = newUser("uRepoAdm");
          long c = newUser("uRepoAdm");
          users.lock(c);
          // Full names are "Test " + username, so name order equals creation order here.
          SortSpec byName = new SortSpec("name", SortDirection.ASC);

          PageResult<User> all = users.findForAdmin("urepoadm", null, null, byName, FIRST_PAGE);
          assertEquals(List.of(a, b, c), all.items().stream().map(User::userId).toList());

          PageResult<User> locked =
              users.findForAdmin("uRepoAdm", AccountStatus.LOCKED, null, byName, FIRST_PAGE);
          assertEquals(List.of(c), locked.items().stream().map(User::userId).toList());

          PageResult<User> secondPage =
              users.findForAdmin(
                  "uRepoAdm",
                  null,
                  null,
                  new SortSpec("name", SortDirection.DESC),
                  new PageRequest(1, 2));
          assertEquals(3, secondPage.totalItems());
          assertEquals(2, secondPage.totalPages());
          assertEquals(List.of(a), secondPage.items().stream().map(User::userId).toList());

          LocalDate today = today();
          assertEquals(
              3,
              users
                  .findForAdmin(
                      "uRepoAdm",
                      null,
                      new DateRange(today.minusDays(1), today.plusDays(1)),
                      byName,
                      FIRST_PAGE)
                  .totalItems());
          assertEquals(
              0,
              users
                  .findForAdmin(
                      "uRepoAdm",
                      null,
                      new DateRange(today.minusDays(10), today.minusDays(5)),
                      byName,
                      FIRST_PAGE)
                  .totalItems());
        });
  }

  @Test
  void rejectsSortFieldsOutsideTheAllowList() {
    inRollback(
        () -> {
          InvalidSortFieldException error =
              assertThrows(
                  InvalidSortFieldException.class,
                  () ->
                      users.findForAdmin(
                          null,
                          null,
                          null,
                          new SortSpec("password_hash; DROP TABLE users", SortDirection.ASC),
                          FIRST_PAGE));
          assertEquals(ErrorCode.VALIDATION_FAILED, error.errorCode());
          assertEquals(List.of("createdAt", "name"), error.allowedFields());
          assertThrows(
              InvalidSortFieldException.class,
              () ->
                  users.findForAdmin(
                      null, null, null, new SortSpec("username", SortDirection.ASC), FIRST_PAGE),
              "the assignment sorts the user list by name or creation time only");
        });
  }

  @Test
  void treatsLikeWildcardsAsLiteralText() {
    inRollback(
        () -> {
          newUser("uRepoWild");
          assertEquals(
              0,
              users
                  .findForAdmin(
                      "uRepoWild%", null, null, new SortSpec("name", SortDirection.ASC), FIRST_PAGE)
                  .totalItems());
          assertEquals(
              0,
              users
                  .findForAdmin(
                      "uRepo_ild", null, null, new SortSpec("name", SortDirection.ASC), FIRST_PAGE)
                  .totalItems());
        });
  }

  @Test
  void searchHidesSelfDeletedAndBlockedInEitherDirection() {
    inRollback(
        () -> {
          long me = newUser("uRepoSrch");
          long visible = newUser("uRepoSrch");
          long iBlocked = newUser("uRepoSrch");
          long blockedMe = newUser("uRepoSrch");
          long deleted = newUser("uRepoSrch");
          blocks.block(me, iBlocked);
          blocks.block(blockedMe, me);
          users.softDelete(deleted);

          List<Long> found =
              users.search(me, "urepOSRCH", FIRST_PAGE).items().stream()
                  .map(UserBrief::userId)
                  .toList();
          assertEquals(List.of(visible), found);
        });
  }

  @Test
  void countsRegistrationsInRangeAndPerMonth() {
    inRollback(
        () -> {
          int year = today().getYear();
          long before = users.countMonthlyRegistrations(year)[today().getMonthValue() - 1];
          LocalDate today = today();
          long rangeBefore = users.countRegistrations(new DateRange(today, today));

          newUser("uRepoReg");
          newUser("uRepoReg");

          long[] monthly = users.countMonthlyRegistrations(year);
          assertEquals(12, monthly.length);
          assertEquals(before + 2, monthly[today().getMonthValue() - 1]);
          assertEquals(rangeBefore + 2, users.countRegistrations(new DateRange(today, today)));
        });
  }

  @Test
  void leavesAdministratorsOutOfTheAdminListAndRegistrationCounts() {
    inRollback(
        () -> {
          LocalDate today = today();
          int month = today.getMonthValue() - 1;
          long monthlyBefore = users.countMonthlyRegistrations(today.getYear())[month];
          long rangeBefore = users.countRegistrations(new DateRange(today, today));

          long user = newUser("uRepoRole");
          newUser("uRepoRole", UserRole.ADMIN);

          PageResult<User> listed =
              users.findForAdmin(
                  "urepoRole",
                  null,
                  new DateRange(today, today),
                  new SortSpec("name", SortDirection.ASC),
                  FIRST_PAGE);
          assertEquals(List.of(user), listed.items().stream().map(User::userId).toList());
          assertEquals(1, listed.totalItems());
          assertEquals(monthlyBefore + 1, users.countMonthlyRegistrations(today.getYear())[month]);
          assertEquals(rangeBefore + 1, users.countRegistrations(new DateRange(today, today)));
        });
  }

  @Test
  void storesOneActivePublicKeyPerUser() {
    inRollback(
        () -> {
          long id = newUser("uRepoKey");
          byte[] first = new byte[32];
          byte[] second = new byte[32];
          second[0] = 1;

          assertEquals(0, publicKeys.findLatestVersion(id));
          long firstId = publicKeys.insert(id, 1, first);
          assertEquals(1, publicKeys.retireActive(id));
          long secondId = publicKeys.insert(id, 2, second);

          assertEquals(secondId, publicKeys.findActive(id).orElseThrow().userPublicKeyId());
          assertArrayEquals(second, publicKeys.findActive(id).orElseThrow().publicKey());
          assertNotNull(publicKeys.findById(firstId).orElseThrow().retiredAt());
          assertEquals(2, publicKeys.findLatestVersion(id));
          assertEquals(1, publicKeys.findActive(List.of(id, Long.MAX_VALUE)).size());
        });
  }

  @Test
  void recordsLoginHistoryWithNamesInChronologicalOrder() {
    inRollback(
        () -> {
          long id = newUser("uRepoLogin");
          long first = loginHistory.recordLogin(id);
          long second = loginHistory.recordLogin(id);
          shiftTime("login_history", "logged_in_at", "login_history_id", first, -5);

          assertTrue(loginHistory.recordLogout(first));
          assertFalse(loginHistory.recordLogout(first), "logout is recorded once");

          var descending =
              loginHistory.find(id, new SortSpec("loggedInAt", SortDirection.DESC), FIRST_PAGE);
          assertEquals(2, descending.totalItems());
          assertEquals(second, descending.items().get(0).loginHistoryId());
          assertEquals(
              users.findById(id).orElseThrow().username(), descending.items().get(0).username());
          assertNotNull(descending.items().get(1).loggedOutAt());

          var ascending =
              loginHistory.find(id, new SortSpec("loggedInAt", SortDirection.ASC), FIRST_PAGE);
          assertEquals(first, ascending.items().get(0).loginHistoryId());

          assertTrue(
              loginHistory
                      .find(null, new SortSpec("loggedInAt", SortDirection.DESC), FIRST_PAGE)
                      .totalItems()
                  >= 2);
          assertThrows(
              InvalidSortFieldException.class,
              () -> loginHistory.find(id, new SortSpec("username", SortDirection.ASC), FIRST_PAGE));
        });
  }
}
