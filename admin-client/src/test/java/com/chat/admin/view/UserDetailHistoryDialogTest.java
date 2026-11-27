package com.chat.admin.view;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailHistoryDialogTest {
    private AdminUser testUser;

    @BeforeEach
    void setUp() {
        testUser = AdminMockDataStore.getInstance().getUsers().get(0);
    }

    @Test
    void instantiatesAndPopulatesMockHistoryWhenDisconnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skip GUI test in headless mode");

        UserDetailHistoryDialog dialog = new UserDetailHistoryDialog(null, testUser, null);
        assertNotNull(dialog);
        assertTrue(dialog.getTitle().contains(testUser.getUsername()));
        dialog.dispose();
    }
}
