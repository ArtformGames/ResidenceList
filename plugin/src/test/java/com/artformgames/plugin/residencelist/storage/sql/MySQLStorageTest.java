package com.artformgames.plugin.residencelist.storage.sql;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.residence.ResidenceVisitor;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import org.junit.Test;

import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MySQLStorageTest {

    @Test
    public void batchesReplaceSnapshotsAndScopeResidenceRowsByServer() throws Exception {
        SQLManager manager = EasySQL.createManager("org.h2.Driver",
                "jdbc:h2:mem:residencelist_sql_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try {
            EasyPlugin plugin = mock(EasyPlugin.class);
            MySQLStorage first = new MySQLStorage(plugin, manager, "server_a", "rl_test_");
            MySQLStorage second = new MySQLStorage(plugin, manager, "server_b", "rl_test_");

            UUID user = UUID.randomUUID();
            first.writeUsers(List.of(new UserSnapshot(user, List.of("home", "shop"), SortFunctions.SIZE, true)));
            assertEquals(List.of("home", "shop"), first.readUser(user).getPinned());
            assertEquals(SortFunctions.SIZE, first.readUser(user).getSortFunction());

            first.writeUsers(List.of(new UserSnapshot(user, List.of("new"), SortFunctions.NAME, false)));
            assertEquals(List.of("new"), first.readUser(user).getPinned());
            UUID secondUser = UUID.randomUUID();
            first.writeUsers(List.of(UserSnapshot.empty(user),
                    new UserSnapshot(secondUser, List.of("other"), SortFunctions.RATINGS, false)));
            assertTrue(first.readUser(user).getPinned().isEmpty());
            assertEquals(SortFunctions.NAME, first.readUser(user).getSortFunction());
            assertEquals(List.of("other"), first.readUser(secondUser).getPinned());

            UUID author = UUID.randomUUID();
            ResidenceRate rate = new ResidenceRate(author, "nice", true, LocalDateTime.of(2024, 1, 2, 3, 4));
            ResidenceSnapshot a = new ResidenceSnapshot(null, -1, "A", List.of("first"), Map.of(author, rate));
            ResidenceSnapshot b = new ResidenceSnapshot(null, -1, "B", List.of("second"), Map.of());
            first.writeResidences(new LinkedHashMap<>(Map.of("home", a)));
            second.writeResidences(Map.of("home", b, "shop", b));

            assertEquals("A", first.readResidences().get("home").aliasName());
            assertEquals(rate, first.readResidences().get("home").rates().get(author));
            assertEquals("B", second.readResidences().get("home").aliasName());
            assertEquals(2, second.readResidences().size());

            first.writeResidences(Map.of("home", new ResidenceSnapshot(null, -1, null, List.of(), Map.of())));
            assertTrue(first.readResidences().get("home").rates().isEmpty());
            assertNull(first.readResidences().get("home").aliasName());
            first.writeResidences(Map.of("home", a));

            assertThrows(Exception.class, () -> first.writeResidences(Map.of("x".repeat(256), a)));
            assertEquals("A", first.readResidences().get("home").aliasName());
            first.writeResidences(Map.of("home", a));

            try (Connection connection = manager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE rl_test_residences SET rates = ? WHERE server_id = ? AND name = ?")) {
                statement.setString(1, "{invalid-json");
                statement.setString(2, "server_a");
                statement.setString(3, "home");
                assertEquals(1, statement.executeUpdate());
            }
            assertThrows(Exception.class, first::readResidences);
            first.writeResidences(Map.of("home", a));

            first.deleteResidence("home");
            assertTrue(first.readResidences().isEmpty());
            assertEquals("B", second.readResidences().get("home").aliasName());
        } finally {
            EasySQL.shutdownManager(manager);
        }
    }

    @Test
    public void visitorsUseSeparateScopedTableAndFollowRenameAndDelete() throws Exception {
        SQLManager manager = EasySQL.createManager("org.h2.Driver",
                "jdbc:h2:mem:residencelist_visitor_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try {
            EasyPlugin plugin = mock(EasyPlugin.class);
            MySQLStorage first = new MySQLStorage(plugin, manager, "first", "rl_visit_");
            MySQLStorage second = new MySQLStorage(plugin, manager, "second", "rl_visit_");
            UUID uuid = UUID.randomUUID();
            ResidenceVisitor old = new ResidenceVisitor(uuid, "Guest", LocalDateTime.of(2025, 1, 1, 12, 0));
            ResidenceVisitor latest = new ResidenceVisitor(uuid, "RenamedGuest", LocalDateTime.of(2025, 2, 1, 12, 0));
            ResidenceSnapshot firstSnapshot = new ResidenceSnapshot(null, -1, null, List.of(), Map.of(), List.of(old));
            ResidenceSnapshot latestSnapshot = new ResidenceSnapshot(null, -1, null, List.of(), Map.of(), List.of(latest));
            first.writeResidences(Map.of("home", firstSnapshot));
            second.writeResidences(Map.of("home", firstSnapshot));
            assertEquals(List.of(old), first.readResidences().get("home").visitors());

            first.writeResidences(Map.of("home", latestSnapshot));
            assertEquals(List.of(latest), first.readResidences().get("home").visitors());
            assertEquals(List.of(old), second.readResidences().get("home").visitors());
            first.writeResidences(Map.of("new-home", latestSnapshot));
            first.deleteResidence("home");
            assertEquals(List.of(latest), first.readResidences().get("new-home").visitors());
            assertFalse(first.readResidences().containsKey("home"));
            assertEquals(List.of(old), second.readResidences().get("home").visitors());

            first.writeResidences(Map.of("new-home", ResidenceSnapshot.empty()));
            assertTrue(first.readResidences().get("new-home").visitors().isEmpty());
            first.deleteResidence("new-home");
            try (Connection connection = manager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM rl_visit_visitors WHERE server_id = ?")) {
                statement.setString(1, "first");
                try (var rows = statement.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(0, rows.getInt(1));
                }
            }
        } finally {
            EasySQL.shutdownManager(manager);
        }
    }
}
