package com.chat.user.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserNavigationSidebarTest {
  @Test
  void profileCardMatchesSidebarMenuWidthAndKeepsFullTooltip() {
    UserNavigationSidebar sidebar =
        new UserNavigationSidebar(
            "Nguyen Van An Extremely Long Display Name",
            "nguyen.van.an",
            destination -> {});

    assertEquals(245, sidebar.getPreferredSize().width);
    assertEquals(220, sidebar.getProfileCard().getPreferredSize().width);
    assertTrue(sidebar.getProfileNameLabel().getToolTipText().contains("Nguyen Van An"));
  }

  @Test
  void shortProfileNameIsNotFittedToAnEllipsis() {
    UserNavigationSidebar sidebar =
        new UserNavigationSidebar("Alex Morgan", "alex.morgan", destination -> {});
    assertEquals("Alex Morgan", sidebar.getProfileNameLabel().getText());
  }

  @Test
  void profileCardHasFixedHeightAndFullSidebarWidth() {
    UserNavigationSidebar sidebar =
        new UserNavigationSidebar("A", "a", destination -> {});

    assertEquals(220, sidebar.getProfileCard().getPreferredSize().width);
    assertEquals(54, sidebar.getProfileCard().getPreferredSize().height);
  }
}
