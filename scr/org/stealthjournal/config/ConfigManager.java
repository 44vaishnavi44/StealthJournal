package org.stealthjournal.config;
import org.stealthjournal.util.Constants;
import org.stealthjournal.util.FileIOHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
/**
* Manages StealthJournal's application configuration.
*
* Configuration is stored in the local flat file:
* config/ui_settings.txt
*
* The file uses key=value entries. No database is required.
*
* Responsibilities:
* - Create the configuration file when it does not exist.
* - Load configuration settings.
* - Parse key-value entries.
* - Read and update individual settings.
* - Save settings to the configuration file.
* - Preserve unknown configuration entries.
* - Handle file-related errors.
*/
public final class ConfigManager {
private static final String CONFIG_FILE_PATH =
Constants.UI_SETTINGS_FILE;
private final Path configPath;
private final Map<String, String> settings =
new LinkedHashMap<>();
private boolean loaded;
/**
* Creates a ConfigManager using the default configuration
path.
*/
public ConfigManager() {
this(Paths.get(CONFIG_FILE_PATH));
}
/**
* Creates a ConfigManager using a custom configuration path.
*
* This constructor is useful for testing and for installations
* that store their configuration outside the default directory.
*
* @param configPath configuration file path
*/
public ConfigManager(Path configPath) {
if (configPath == null) {
throw new IllegalArgumentException(
"Configuration path cannot be null."
);
}
this.configPath = configPath.toAbsolutePath().normalize();
this.loaded = false;
}
/**
* Loads configuration from disk.
*
* If the file does not exist, the default settings are initialized
* and saved to a newly created configuration file.
*
* Unknown settings already present in the file are preserved.
*
* @return true if loading or initial configuration creation
succeeds
*/
public synchronized boolean loadConfiguration() {
settings.clear();
loaded = false;
try {
createParentDirectory();
if (Files.notExists(configPath)) {
initializeDefaultSettings();
if (!saveConfiguration()) {
settings.clear();
return false;
}
loaded = true;
return true;
}
if (!Files.isRegularFile(configPath)
|| !Files.isReadable(configPath)) {
System.err.println(
"Configuration file is not readable: "
+ configPath
);
return false;
}
readConfigurationFile();
/*
* Fill missing known settings with defaults while retaining
* every unrecognized setting already in the file.
*/
addMissingDefaultSettings();
loaded = true;
/*
* Persist defaults added during loading so that the file
* remains complete for the next application launch.
*/
if (!saveConfiguration()) {
System.err.println(
"Configuration loaded, but defaults "
+ "could not be saved."
);
}
return true;
} catch (IOException | SecurityException exception) {
settings.clear();
System.err.println(
"Unable to load configuration: "
+ configPath
);
System.err.println(
"Reason: "
+ exception.getMessage()
);
return false;
}
}
/**
* Saves all current settings to disk.
*
* The file is written using UTF-8 encoding. A temporary file is
* used first so that a failed write is less likely to leave a
* partially written configuration.
*
* @return true if saving succeeds
*/
public synchronized boolean saveConfiguration() {
try {
createParentDirectory();
Path temporaryPath = configPath.resolveSibling(
configPath.getFileName().toString() + ".tmp"
);
try {
writeSettingsToFile(temporaryPath);
try {
Files.move(
temporaryPath,
configPath,
StandardCopyOption.REPLACE_EXISTING,
StandardCopyOption.ATOMIC_MOVE
);
} catch (IOException atomicMoveException) {
/*
* Some file systems do not support atomic moves.
* Retry using a normal replacement move.
*/
Files.move(
temporaryPath,
configPath,
StandardCopyOption.REPLACE_EXISTING
);
}
loaded = true;
return true;
} finally {
/*
* Remove a leftover temporary file after either
* success or failure.
*/
Files.deleteIfExists(temporaryPath);
}
} catch (IOException | SecurityException exception) {
System.err.println(
"Unable to save configuration: "
+ configPath
);
System.err.println(
"Reason: "
+ exception.getMessage()
);
return false;
}
}
/**
* Reads a setting by key.
*
* @param key configuration key
* @return the setting value, or null when the key is absent
*/
public synchronized String getSetting(String key) {
if (!isValidKey(key)) {
return null;
}
ensureLoaded();
return settings.get(key);
}
/**
* Reads a setting and supplies a fallback value when it is
absent.
*
* @param key configuration key
* @param defaultValue fallback value
* @return stored value or defaultValue
*/
public synchronized String getSetting(
String key,
String defaultValue
) {
String value = getSetting(key);
return value != null ? value : defaultValue;
}
/**
* Adds or updates a configuration setting and saves the
change.
*
* If saving fails, the previous in-memory value is restored.
*
* @param key configuration key
* @param value setting value
* @return true if the update is saved successfully
*/
public synchronized boolean setSetting(
String key,
String value
) {
if (!isValidKey(key) || value == null) {
return false;
}
ensureLoaded();
boolean previouslyExisted = settings.containsKey(key);
String previousValue = settings.get(key);
settings.put(key, value);
if (saveConfiguration()) {
return true;
}
if (previouslyExisted) {
settings.put(key, previousValue);
} else {
settings.remove(key);
}
return false;
}
/**
* Removes a setting and saves the change.
*
* @param key configuration key
* @return true if the setting is absent after the operation
* and the change is saved successfully
*/
public synchronized boolean removeSetting(String key) {
if (!isValidKey(key)) {
return false;
}
ensureLoaded();
if (!settings.containsKey(key)) {
return true;
}
String previousValue = settings.remove(key);
if (saveConfiguration()) {
return true;
}
settings.put(key, previousValue);
return false;
}
/**
* Returns an unmodifiable snapshot of all configuration
settings.
*
* The returned map cannot modify the manager's internal state.
*
* @return configuration snapshot
*/
public synchronized Map<String, String> getAllSettings() {
ensureLoaded();
return Collections.unmodifiableMap(
new LinkedHashMap<>(settings)
);
}
/**
* Checks whether a setting exists.
*
* @param key configuration key
* @return true when the key exists
*/ public synchronized boolean containsSetting(String key) {
if (!isValidKey(key)) {
return false;
}
ensureLoaded();
return settings.containsKey(key);
}
/**
* Indicates whether configuration loading has succeeded.
*
* @return true if the configuration has been loaded
*/
public synchronized boolean isLoaded() {
return loaded;
}
/**
* Returns the absolute path of the configuration file.
*
* @return configuration path
*/
public Path getConfigPath() {
return configPath;
}
/**
* Returns the setup-completed state.
*
* @return true when the stored value is "true", ignoring case
*/
public synchronized boolean isSetupCompleted() {
String value = getSetting(
Constants.CONFIG_SETUP_COMPLETED,
Constants.DEFAULT_SETUP_COMPLETED
);
return Boolean.parseBoolean(value);
}
/**
* Updates the setup-completed state.
*
* @param completed true when initial setup is complete
* @return true if the change is saved
*/
public synchronized boolean setSetupCompleted(
boolean completed
) {
return setSetting(
Constants.CONFIG_SETUP_COMPLETED,
Boolean.toString(completed)
);
}
/**
* Returns the configured UI theme.
*
* @return theme name
*/
public synchronized String getTheme() {
return getSetting(
Constants.CONFIG_THEME,
Constants.DEFAULT_THEME
);
}
/**
* Updates the UI theme.
*
* @param theme theme name
* @return true if the change is saved
*/
public synchronized boolean setTheme(String theme) {
if (theme == null || theme.trim().isEmpty()) {
return false;
}
return setSetting(
Constants.CONFIG_THEME,
theme.trim()
);
}
/**
* Returns the configured decoy video path.
*
* @return stored path or the default decoy video path
*/
public synchronized String getDecoyVideoPath() {
return getSetting(
Constants.CONFIG_DECOY_VIDEO,
Constants.DEFAULT_DECOY_VIDEO
);
}
/**
* Updates the configured decoy video path.
*
* The path is stored as provided; this method does not require
* the video to exist yet.
*
* @param path video path
* @return true if the change is saved
*/
public synchronized boolean setDecoyVideoPath(String path) {
if (path == null || path.trim().isEmpty()) {
return false;
}
return setSetting(
Constants.CONFIG_DECOY_VIDEO,
path.trim()
);
}
/**
* Returns the configured journal directory.
*
* @return stored directory or the default journal directory
*/
public synchronized String getJournalDirectory() {
return getSetting(
Constants.CONFIG_JOURNAL_DIRECTORY,
Constants.DEFAULT_JOURNAL_DIRECTORY
);
}
/**
* Updates the configured journal directory.
*
* @param directory journal directory
* @return true if the change is saved
*/
public synchronized boolean setJournalDirectory(
String directory
) {
if (directory == null || directory.trim().isEmpty()) {
return false;
}
return setSetting(
Constants.CONFIG_JOURNAL_DIRECTORY,
directory.trim()
);
}
/**
* Reloads the configuration from disk, discarding unsaved
* in-memory changes.
*
* @return true if reloading succeeds
*/
public synchronized boolean reloadConfiguration() {
return loadConfiguration();
}
/**
* Parses the configuration file.
*
* Blank lines and lines beginning with '#' or ';' are ignored.
* The first '=' separates the key from the value, allowing
* values to contain additional '=' characters.
*
* @throws IOException if the file cannot be read
*/
private void readConfigurationFile() throws IOException {
try (BufferedReader reader = Files.newBufferedReader(
configPath,
StandardCharsets.UTF_8
)) {
String line;
while ((line = reader.readLine()) != null) {
String trimmedLine = line.trim();
if (trimmedLine.isEmpty()
|| trimmedLine.startsWith("#")
|| trimmedLine.startsWith(";")) {
continue;
}
int delimiterIndex = trimmedLine.indexOf(
Constants.CONFIG_KEY_VALUE_DELIMITER
);
if (delimiterIndex <= 0) {
System.err.println(
"Ignoring malformed configuration line."
);
continue;
}
String key = trimmedLine.substring(
0,
delimiterIndex
).trim();
String value = trimmedLine.substring(
delimiterIndex + Constants
.CONFIG_KEY_VALUE_DELIMITER.length()
).trim();
if (isValidKey(key)) {
settings.put(key, value);
}
}
}
}
/**
* Writes the in-memory configuration map to the given path.
*
* @param targetPath output path
* @throws IOException if writing fails
*/
private void writeSettingsToFile(
Path targetPath
) throws IOException {
try (BufferedWriter writer = Files.newBufferedWriter(
targetPath,
StandardCharsets.UTF_8
)) {
writer.write("# StealthJournal UI configuration");
writer.newLine();
/*
* Sorting makes the output stable and easier to inspect.
*/
Map<String, String> sortedSettings =
new TreeMap<>(settings);
for (Map.Entry<String, String> entry
: sortedSettings.entrySet()) {
writer.write(entry.getKey());
writer.write(
Constants.CONFIG_KEY_VALUE_DELIMITER
);
writer.write(entry.getValue());
writer.newLine();
}
writer.flush();
}
}
/**
* Initializes all default settings.
*/
private void initializeDefaultSettings() {
settings.clear();
settings.put(
Constants.CONFIG_SETUP_COMPLETED,
Constants.DEFAULT_SETUP_COMPLETED
);
settings.put(
Constants.CONFIG_THEME,
Constants.DEFAULT_THEME
);
settings.put(
Constants.CONFIG_DECOY_VIDEO,
Constants.DEFAULT_DECOY_VIDEO
);
settings.put(
Constants.CONFIG_JOURNAL_DIRECTORY,
Constants.DEFAULT_JOURNAL_DIRECTORY
);
}
/**
* Adds defaults only for settings that are missing.
*
* Existing values are never overwritten.
*/
private void addMissingDefaultSettings() {
settings.putIfAbsent(
Constants.CONFIG_SETUP_COMPLETED,
Constants.DEFAULT_SETUP_COMPLETED
);
settings.putIfAbsent(
Constants.CONFIG_THEME,
Constants.DEFAULT_THEME
);
settings.putIfAbsent(
Constants.CONFIG_DECOY_VIDEO,
Constants.DEFAULT_DECOY_VIDEO
);
settings.putIfAbsent(
Constants.CONFIG_JOURNAL_DIRECTORY,
Constants.DEFAULT_JOURNAL_DIRECTORY
);
}
/**
* Creates the parent directory for the configuration file.
*
* @throws IOException if the directory cannot be created
*/
private void createParentDirectory() throws IOException {
Path parent = configPath.getParent();
if (parent != null) {
Files.createDirectories(parent);
}
}
/**
* Ensures that settings have been loaded before access.
*
* If loading fails, an IllegalStateException is thrown rather than
* silently returning an incomplete configuration.
*/
private void ensureLoaded() {
if (!loaded && !loadConfiguration()) {
throw new IllegalStateException(
"StealthJournal configuration could not be loaded."
);
}
}
/**
* Validates a configuration key.
*
* Keys must be non-empty and cannot contain the key/value
delimiter
* or line breaks.
*
* @param key key to validate
* @return true if valid
*/
private boolean isValidKey(String key) {
return key != null
&& !key.trim().isEmpty()
&& !key.contains(
Constants.CONFIG_KEY_VALUE_DELIMITER
)
&& !key.contains("\n")
&& !key.contains("\r");
}
}