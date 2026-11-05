package org.stealthjournal; 
import org.stealthjournal.config.ConfigManager; 
import org.stealthjournal.util.Constants; 
import java.io.IOException; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.Files; 
import java.nio.file.Path; 
import java.nio.file.Paths; 
import java.util.Map; 
import java.util.Comparator; 
import java.util.stream.Stream; 
/** 
* Tests the StealthJournal ConfigManager. 
* 
* This is a standalone test class and does not require JUnit. 
* It can be run using its main() method. 
* 
* Tests include: 
* - Creating a configuration file with default settings. 
* - Loading existing configuration settings. 
* - Parsing key-value entries. 
* - Preserving values containing '=' characters. 
* - Updating and retrieving settings. 
 * - Removing settings. 
 * - Preserving unknown configuration keys. 
 * - Saving and reloading configuration. 
 * - Handling invalid input. 
 * - Supporting a custom configuration file path. 
 */ 
public final class ConfigManagerTest { 
 
    private static int testsPassed = 0; 
    private static int testsFailed = 0; 
 
    private static Path testRoot; 
 
    private ConfigManagerTest() { 
        throw new AssertionError( 
                "Test class must not be instantiated." 
        ); 
    } 
 
    public static void main(String[] args) { 
        System.out.println("========================================"); 
        System.out.println(" StealthJournal - ConfigManager Tests"); 
        System.out.println("========================================"); 
 
        try { 
            testRoot = Files.createTempDirectory( 
                    "stealthjournal-config-tests-" 
            ); 
 
            runTest( 
                    "Creates default configuration file", 
                    ConfigManagerTest::testCreatesDefaultConfiguration 
            ); 
 
            runTest( 
                    "Loads existing configuration", 
                    ConfigManagerTest::testLoadsExistingConfiguration 
            ); 
 
            runTest( 
                    "Parses values containing equals signs", 
                    ConfigManagerTest::testParsesEqualsInValue 
            ); 
 
            runTest( 
                    "Ignores blank lines and comments", 
                    ConfigManagerTest::testIgnoresComments 
            ); 
 
            runTest( 
                    "Updates and retrieves settings", 
                    ConfigManagerTest::testUpdateSetting 
            ); 
 
            runTest( 
                    "Preserves unknown configuration settings", 
                    ConfigManagerTest::testPreservesUnknownSettings 
            ); 
 
            runTest( 
                    "Removes configuration settings", 
                    ConfigManagerTest::testRemoveSetting 
            ); 
 
            runTest( 
                    "Reloads saved configuration", 
                    ConfigManagerTest::testReloadConfiguration 
            ); 
 
            runTest( 
                    "Returns default values for missing settings", 
                    ConfigManagerTest::testDefaultValues 
            ); 
 
            runTest( 
                    "Tracks setup completion", 
                    ConfigManagerTest::testSetupCompleted 
            ); 
 
            runTest( 
                    "Reads and updates theme", 
                    ConfigManagerTest::testTheme 
            ); 
 
            runTest( 
                    "Reads and updates decoy video path", 
                    ConfigManagerTest::testDecoyVideoPath 
            ); 
 
            runTest( 
                    "Reads and updates journal directory", 
                    ConfigManagerTest::testJournalDirectory 
            ); 
 
            runTest( 
                    "Returns an unmodifiable settings snapshot", 
                    ConfigManagerTest::testUnmodifiableSnapshot 
            ); 
 
            runTest( 
                    "Rejects invalid setting keys", 
                    ConfigManagerTest::testInvalidKeys 
            ); 
 
            runTest( 
                    "Rejects null configuration path", 
                    ConfigManagerTest::testNullPath 
            ); 
 
        } catch (IOException exception) { 
            testsFailed++; 
            System.err.println( 
                    "[FAIL] Unable to initialize test environment: " 
                    + exception.getMessage() 
            ); 
 
        } finally { 
            if (testRoot != null) { 
                try { 
                    deleteRecursively(testRoot); 
                } catch (IOException exception) { 
                    System.err.println( 
                            "Warning: unable to clean test files: " 
                            + exception.getMessage() 
                    ); 
                } 
            } 
        } 
 
        System.out.println(); 
        System.out.println("========================================"); 
        System.out.println(" Tests passed : " + testsPassed); 
        System.out.println(" Tests failed : " + testsFailed); 
        System.out.println("========================================"); 
 
        if (testsFailed > 0) { 
            throw new AssertionError( 
                    "ConfigManager tests failed: " + testsFailed 
            ); 
        } 
 
        System.out.println("All ConfigManager tests passed."); 
    } 
 
    private static void testCreatesDefaultConfiguration() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "default-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration initialization should succeed." 
        ); 
 
        assertTrue( 
                Files.exists(file), 
                "Configuration file should be created." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_SETUP_COMPLETED, 
                manager.getSetting( 
                        Constants.CONFIG_SETUP_COMPLETED 
                ), 
                "Default setup state should be stored." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_THEME, 
                manager.getTheme(), 
                "Default theme should be stored." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_DECOY_VIDEO, 
                manager.getDecoyVideoPath(), 
                "Default decoy video path should be stored." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_JOURNAL_DIRECTORY, 
                manager.getJournalDirectory(), 
                "Default journal directory should be stored." 
        ); 
    } 
 
    private static void testLoadsExistingConfiguration() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "existing-config/ui_settings.txt" 
        ); 
 
        Files.createDirectories(file.getParent()); 
 
        Files.write( 
                file, 
                ( 
                        "setupCompleted=true\n" 
                        + "theme=dark\n" 
                        + "customSetting=customValue\n" 
                ).getBytes(StandardCharsets.UTF_8) 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Existing configuration should load." 
        ); 
 
        assertEquals( 
                "true", 
                manager.getSetting("setupCompleted"), 
                "Setup state should be loaded." 
        ); 
 
        assertEquals( 
                "dark", 
                manager.getSetting("theme"), 
                "Theme should be loaded." 
        ); 
 
        assertEquals( 
                "customValue", 
                manager.getSetting("customSetting"), 
                "Custom setting should be loaded." 
        ); 
    } 
 
    private static void testParsesEqualsInValue() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "equals-config/ui_settings.txt" 
        ); 
 
        Files.createDirectories(file.getParent()); 
 
        Files.write( 
                file, 
                "customValue=part1=part2=part3\n" 
                        .getBytes(StandardCharsets.UTF_8) 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertEquals( 
                "part1=part2=part3", 
                manager.getSetting("customValue"), 
                "Additional equals signs must remain in the value." 
        ); 
    } 
 
    private static void testIgnoresComments() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "comments-config/ui_settings.txt" 
        ); 
 
        Files.createDirectories(file.getParent()); 
 
        Files.write( 
                file, 
                ( 
                        "# This is a comment\n" 
                        + "; This is also a comment\n" 
                        + "\n" 
                        + "theme=light\n" 
                        + "malformedLine\n" 
                        + "=missingKey\n" 
                ).getBytes(StandardCharsets.UTF_8) 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load despite malformed lines." 
        ); 
 
        assertEquals( 
                "light", 
                manager.getTheme(), 
                "Valid configuration values should be parsed." 
        ); 
 
        assertFalse( 
                manager.containsSetting("malformedLine"), 
                "Malformed lines should not become settings." 
        ); 
    } 
 
    private static void testUpdateSetting() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "update-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Initial configuration should load." 
        ); 
 
        assertTrue( 
                manager.setSetting("language", "English"), 
                "A new setting should be saved." 
        ); 
 
        assertEquals( 
                "English", 
                manager.getSetting("language"), 
                "The new value should be retrievable." 
        ); 
 
        assertTrue( 
                manager.setSetting("language", "Telugu"), 
                "An existing setting should be updated." 
        ); 
 
        assertEquals( 
                "Telugu", 
                manager.getSetting("language"), 
                "The updated value should be retrievable." 
        ); 
 
        String diskContent = new String( 
                Files.readAllBytes(file), 
                StandardCharsets.UTF_8 
        ); 
 
        assertTrue( 
                diskContent.contains("language=Telugu"), 
                "The updated value should be persisted." 
        ); 
    } 
 
    private static void testPreservesUnknownSettings() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "unknown-config/ui_settings.txt" 
        ); 
 
        Files.createDirectories(file.getParent()); 
 
        Files.write( 
                file, 
                ( 
                        "futureFeature=enabled\n" 
                        + "customPath=folder/value\n" 
                ).getBytes(StandardCharsets.UTF_8) 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertTrue( 
                manager.setSetting("theme", "dark"), 
                "Updating a known setting should succeed." 
        ); 
 
        ConfigManager reloaded = new ConfigManager(file); 
 
        assertTrue( 
                reloaded.loadConfiguration(), 
                "Updated configuration should reload." 
        ); 
 
        assertEquals( 
                "enabled", 
                reloaded.getSetting("futureFeature"), 
                "Unknown settings must be preserved." 
        ); 
 
        assertEquals( 
                "folder/value", 
                reloaded.getSetting("customPath"), 
                "Unknown path settings must be preserved." 
        ); 
    } 
 
    private static void testRemoveSetting() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "remove-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertTrue( 
                manager.setSetting("temporary", "value"), 
                "Test setting should be created." 
        ); 
 
        assertTrue( 
                manager.containsSetting("temporary"), 
                "Test setting should exist." 
        ); 
 
        assertTrue( 
                manager.removeSetting("temporary"), 
                "Test setting should be removed." 
        ); 
 
        assertFalse( 
                manager.containsSetting("temporary"), 
                "Removed setting should no longer exist." 
        ); 
 
        assertTrue( 
                manager.removeSetting("missingSetting"), 
                "Removing a missing setting should succeed." 
        ); 
    } 
 
    private static void testReloadConfiguration() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "reload-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Initial configuration should load." 
        ); 
 
        assertTrue( 
                manager.setSetting("theme", "dark"), 
                "Initial theme update should succeed." 
        ); 
 
        Files.write( 
                file, 
                ( 
                        "setupCompleted=false\n" 
                        + "theme=light\n" 
                ).getBytes(StandardCharsets.UTF_8) 
        ); 
 
        assertTrue( 
                manager.reloadConfiguration(), 
                "Configuration should reload." 
        ); 
 
        assertEquals( 
                "light", 
                manager.getTheme(), 
                "Reload should read the current disk value." 
        ); 
    } 
 
    private static void testDefaultValues() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "defaults-config/ui_settings.txt" 
        ); 
 
        Files.createDirectories(file.getParent()); 
 
        Files.write( 
                file, 
                "custom=value\n".getBytes(StandardCharsets.UTF_8) 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_THEME, 
                manager.getSetting( 
                        "missingTheme", 
                        Constants.DEFAULT_THEME 
                ), 
                "Caller-supplied default should be returned." 
        ); 
 
        assertEquals( 
                Constants.DEFAULT_THEME, 
                manager.getTheme(), 
                "Missing theme should receive the application default." 
        ); 
    } 
 
    private static void testSetupCompleted() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "setup-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertFalse( 
                manager.isSetupCompleted(), 
                "Fresh configuration should not indicate completed setup." 
        ); 
 
        assertTrue( 
                manager.setSetupCompleted(true), 
                "Setup state should be updated." 
        ); 
 
        assertTrue( 
                manager.isSetupCompleted(), 
                "Setup state should now be true." 
        ); 
 
        assertTrue( 
                manager.setSetupCompleted(false), 
                "Setup state should be resettable." 
        ); 
 
        assertFalse( 
                manager.isSetupCompleted(), 
                "Setup state should now be false." 
        ); 
    } 
 
    private static void testTheme() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "theme-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertTrue( 
                manager.setTheme("dark"), 
                "Theme should be updated." 
        ); 
 
        assertEquals( 
                "dark", 
                manager.getTheme(), 
                "Theme should match the saved value." 
        ); 
 
        assertFalse( 
                manager.setTheme(" "), 
                "Blank themes should be rejected." 
        ); 
    } 
 
    private static void testDecoyVideoPath() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "video-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        String customPath = "media/decoy-video.mp4"; 
 
        assertTrue( 
                manager.setDecoyVideoPath(customPath), 
                "Decoy video path should be saved." 
        ); 
 
        assertEquals( 
                customPath, 
                manager.getDecoyVideoPath(), 
                "Decoy video path should match." 
        ); 
 
        assertFalse( 
                manager.setDecoyVideoPath(" "), 
                "Blank video paths should be rejected." 
        ); 
    } 
 
    private static void testJournalDirectory() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "journal-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        String customDirectory = "private/entries"; 
 
        assertTrue( 
                manager.setJournalDirectory(customDirectory), 
                "Journal directory should be saved." 
        ); 
 
        assertEquals( 
                customDirectory, 
                manager.getJournalDirectory(), 
                "Journal directory should match." 
        ); 
 
        assertFalse( 
                manager.setJournalDirectory(" "), 
                "Blank journal directories should be rejected." 
        ); 
    } 
 
    private static void testUnmodifiableSnapshot() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "snapshot-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        Map<String, String> snapshot = 
                manager.getAllSettings(); 
 
        boolean rejected = false; 
 
        try { 
            snapshot.put("injected", "value"); 
        } catch (UnsupportedOperationException exception) { 
            rejected = true; 
        } 
 
        assertTrue( 
                rejected, 
                "Settings snapshot must be unmodifiable." 
        ); 
 
        assertFalse( 
                manager.containsSetting("injected"), 
                "Snapshot changes must not affect manager state." 
        ); 
    } 
 
    private static void testInvalidKeys() 
            throws Exception { 
 
        Path file = testRoot.resolve( 
                "invalid-key-config/ui_settings.txt" 
        ); 
 
        ConfigManager manager = new ConfigManager(file); 
 
        assertTrue( 
                manager.loadConfiguration(), 
                "Configuration should load." 
        ); 
 
        assertFalse( 
                manager.setSetting(null, "value"), 
                "Null keys should be rejected." 
        ); 
 
        assertFalse( 
                manager.setSetting("", "value"), 
                "Empty keys should be rejected." 
        ); 
 
        assertFalse( 
                manager.setSetting("invalid=key", "value"), 
                "Keys containing the delimiter should be rejected." 
        ); 
 
        assertFalse( 
                manager.setSetting("key", null), 
                "Null values should be rejected." 
        ); 
 
        assertFalse( 
                manager.removeSetting(null), 
                "Removing a null key should be rejected." 
        ); 
    } 
 
    private static void testNullPath() { 
        boolean rejected = false; 
 
        try { 
            new ConfigManager((Path) null); 
        } catch (IllegalArgumentException exception) { 
            rejected = true; 
        } 
 
        assertTrue( 
                rejected, 
                "Null configuration paths must be rejected." 
        ); 
    } 
 
    private static void runTest( 
            String testName, 
            CheckedRunnable test 
    ) { 
        try { 
            test.run(); 
 
            testsPassed++; 
 
            System.out.println("[PASS] " + testName); 
 
        } catch (Throwable exception) { 
            testsFailed++; 
 
            System.out.println("[FAIL] " + testName); 
 
            System.out.println( 
                    "       " 
                    + exception.getClass().getSimpleName() 
                    + ": " 
                    + exception.getMessage() 
            ); 
        } 
    } 
 
    private static void assertTrue( 
            boolean condition, 
            String message 
    ) { 
        if (!condition) { 
            throw new AssertionError(message); 
        } 
    } 
 
    private static void assertFalse( 
            boolean condition, 
            String message 
    ) { 
        assertTrue(!condition, message); 
    } 
 
    private static void assertEquals( 
            Object expected, 
            Object actual, 
            String message 
    ) { 
        if (expected == null) { 
            if (actual != null) { 
                throw new AssertionError( 
                        message 
                        + " Expected null but received: " 
                        + actual 
                ); 
            } 
 
            return; 
        } 
 
        if (!expected.equals(actual)) { 
            throw new AssertionError( 
                    message 
                    + " Expected: " 
                    + expected 
                    + ", actual: " 
                    + actual 
            ); 
        } 
    } 
 
    private static void deleteRecursively(Path root) 
            throws IOException { 
 
        if (root == null || Files.notExists(root)) { 
            return; 
        } 
 
        try (Stream<Path> paths = Files.walk(root)) { 
            paths.sorted(Comparator.reverseOrder()) 
                    .forEach(path -> { 
                        try { 
                            Files.deleteIfExists(path); 
                        } catch (IOException exception) { 
                            throw new CleanupException(exception); 
                        } 
                    }); 
 
        } catch (CleanupException exception) { 
            throw exception.getCause(); 
        } 
    } 
 
    @FunctionalInterface 
    private interface CheckedRunnable { 
        void run() throws Exception; 
    } 
 
    private static final class CleanupException 
            extends RuntimeException { 
 
        private CleanupException(IOException cause) { 
            super(cause); 
        } 
 
        @Override 
        public synchronized IOException getCause() { 
            return (IOException) super.getCause(); 
        } 
    } 
} 