package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailHistoryDialogTest {
    private AdminUser testUser;

    @BeforeEach
    void setUp() {
        testUser = AdminMockDataStore.getInstance().getUsers().get(0);
    }

    @Test
    void instantiatesWithoutServerConnection() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skip GUI test in headless mode");

        UserDetailHistoryDialog dialog = new UserDetailHistoryDialog(null, testUser, null);
        assertNotNull(dialog);
        assertTrue(dialog.getTitle().contains(testUser.getUsername()));
        dialog.dispose();
    }

    @Test
    void formatsLoginTimeInVietnamTimeWithSeconds() {
        assertEquals("2026-11-27 16:41:30",
                UserDetailHistoryDialog.formatLoginTime(OffsetDateTime.parse("2026-11-27T09:41:30.494Z")));
        assertEquals("2026-11-28 06:59:59",
                UserDetailHistoryDialog.formatLoginTime(OffsetDateTime.parse("2026-11-27T23:59:59Z")));
        assertEquals("2026-11-27 16:41:30",
                UserDetailHistoryDialog.formatLoginTime(OffsetDateTime.parse("2026-11-27T16:41:30+07:00")));
        assertEquals("", UserDetailHistoryDialog.formatLoginTime(null));
    }
}
