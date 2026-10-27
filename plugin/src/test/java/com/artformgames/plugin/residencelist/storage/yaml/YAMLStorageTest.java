package com.artformgames.plugin.residencelist.storage.yaml;

import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.residence.ResidenceVisitor;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class YAMLStorageTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void userRoundTripAndResetRemovesStaleSettings() throws Exception {
        Path file = folder.newFile("user.yaml").toPath();
        UUID key = UUID.randomUUID();
        YAMLStorage.writeUserFile(file, new UserSnapshot(key, List.of("Home"), SortFunctions.RATINGS, true));
        UserSnapshot loaded = YAMLStorage.readUserFile(file, key);
        assertEquals(List.of("Home"), loaded.pinned());
        assertEquals(SortFunctions.RATINGS, loaded.sortFunction());
        assertTrue(loaded.reversed());

        YAMLStorage.writeUserFile(file, new UserSnapshot(key, List.of("Home"), SortFunctions.NAME, false));
        loaded = YAMLStorage.readUserFile(file, key);
        assertEquals(SortFunctions.NAME, loaded.sortFunction());
        assertFalse(loaded.reversed());

        YAMLStorage.writeUserFile(file, new UserSnapshot(key, List.of(), SortFunctions.NAME, false));
        assertFalse(Files.exists(file));
    }

    @Test
    public void residenceRatesRoundTripAndRemoval() throws Exception {
        Path file = folder.newFile("home.yaml").toPath();
        UUID author = UUID.randomUUID();
        ResidenceRate rate = new ResidenceRate(author, "Good", true, LocalDateTime.of(2024, 1, 2, 3, 4));
        YAMLStorage.writeResidenceFile(file, new ResidenceSnapshot(null, -1, "Home", List.of("Line one"), Map.of(author, rate)));
        ResidenceSnapshot loaded = YAMLStorage.readResidence(file);
        assertEquals("Home", loaded.aliasName());
        assertEquals(List.of("Line one"), loaded.description());
        assertEquals(rate, loaded.rates().get(author));

        YAMLStorage.writeResidenceFile(file, new ResidenceSnapshot(null, -1, null, List.of(), Map.of()));
        loaded = YAMLStorage.readResidence(file);
        assertNull(loaded.aliasName());
        assertTrue(loaded.rates().isEmpty());
        assertFalse(Files.readString(file).contains(author.toString()));
    }

    @Test
    public void visitorsRoundTripAndOlderFileWithoutVisitorsLoads() throws Exception {
        Path file = folder.newFile("visitors.yaml").toPath();
        assertTrue(YAMLStorage.readResidence(file).visitors().isEmpty());

        UUID uuid = UUID.randomUUID();
        ResidenceVisitor visitor = new ResidenceVisitor(uuid, "Guest", LocalDateTime.of(2025, 2, 3, 4, 5, 6, 123456789));
        YAMLStorage.writeResidenceFile(file, new ResidenceSnapshot(null, -1, null, List.of(), Map.of(), List.of(visitor)));
        assertEquals(List.of(visitor), YAMLStorage.readResidence(file).visitors());

        YAMLStorage.writeResidenceFile(file, ResidenceSnapshot.empty());
        assertTrue(YAMLStorage.readResidence(file).visitors().isEmpty());
        assertFalse(Files.readString(file).contains(uuid.toString()));
    }

    @Test
    public void malformedVisitorsDoNotLoadAsEmpty() throws Exception {
        Path file = folder.newFile("bad-visitors.yaml").toPath();
        Files.writeString(file, "visitors:\n  - uuid: not-a-uuid\n    name: Guest\n    last-time: 2025-01-01T00:00:00\n");
        assertThrows(Exception.class, () -> YAMLStorage.readResidence(file));
        Files.writeString(file, "visitors: Guest\n");
        assertThrows(Exception.class, () -> YAMLStorage.readResidence(file));
    }

    @Test
    public void malformedYamlIsNotOverwritten() throws Exception {
        Path file = folder.newFile("broken.yaml").toPath();
        String malformed = "rates: [unclosed";
        Files.writeString(file, malformed, StandardCharsets.UTF_8);
        try {
            YAMLStorage.writeResidenceFile(file, new ResidenceSnapshot(null, -1, null, List.of(), Map.of()));
            fail("Malformed source must fail before replacing the file");
        } catch (Exception expected) {
            assertEquals(malformed, Files.readString(file, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void wrongFieldTypeIsNotSilentlyReset() throws Exception {
        Path file = folder.newFile("wrong-type.yaml").toPath();
        String source = "pinned: Home\n";
        Files.writeString(file, source, StandardCharsets.UTF_8);
        try {
            YAMLStorage.readUserFile(file, UUID.randomUUID());
            fail("A scalar pinned field must not load as an empty list");
        } catch (Exception expected) {
            assertEquals(source, Files.readString(file, StandardCharsets.UTF_8));
        }
    }
}
