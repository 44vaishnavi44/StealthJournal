package org.stealthjournal;
import org.stealthjournal.auth.AuthenticationManager;
import org.stealthjournal.config.ConfigManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
/**
* Standalone tests for StealthJournal's AuthenticationManager.
*
* Run this class using its main method. No external testing library 
* is required.
*/
public final class AuthenticationManagerTest {
private static int passedTests = 0;    
private static int failedTests = 0;
private AuthenticationManagerTest() {        
throw new AssertionError("Utility class must not be instantiated.");
}
public static void main(String[] args) {
System.out.println("=====");
System.out.println(" AuthenticationManager Tests");
System.out.println("=====");
runTest("No credentials initially configured",
AuthenticationManagerTest::testNoCredentialsInitially);
runTest("Configure valid credentials",
AuthenticationManagerTest::testSetCredentials);
runTest("Reject invalid credentials during setup",
AuthenticationManagerTest::testRejectInvalidCredentials);
runTest("Verify correct password",
AuthenticationManagerTest::testCorrectPassword);
runTest("Reject incorrect password",
AuthenticationManagerTest::testIncorrectPassword);
runTest("Verify correct security answer",
AuthenticationManagerTest::testCorrectSecurityAnswer);
runTest("Reject incorrect security answer",
AuthenticationManagerTest::testIncorrectSecurityAnswer);        
runTest("Security answer ignores case and outer spaces",
AuthenticationManagerTest::testNormalizedSecurityAnswer);
runTest("Authenticate with both correct credentials",
AuthenticationManagerTest::testSuccessfulAuthentication);
runTest("Reject authentication with incorrect credentials",
AuthenticationManagerTest::testFailedAuthentication);
runTest("Retrieve configured security question”,
AuthenticationManagerTest::testGetSecurityQuestion);
runTest("Change password successfully",
AuthenticationManagerTest::testChangePassword);
runTest("Reject password change with incorrect current password",
AuthenticationManagerTest::testRejectPasswordChange);
runTest("Change security question and answer",
AuthenticationManagerTest::testChangeSecurityQuestion);
runTest("Reject security question change with incorrect password",
AuthenticationManagerTest::testRejectSecurityQuestionChange);
runTest("Persist credentials across manager instances",
 AuthenticationManagerTest::testCredentialPersistence);        
runTest("Clear configured credentials",
AuthenticationManagerTest::testClearCredentials);
runTest("Reject null ConfigManager",
AuthenticationManagerTest::testNullConfigManager);
printSummary();
}
private static void testNoCredentialsInitially() throws Exception {withFreshManager((manager, configPath) -> {            assertFalse(manager.hasCredentials(),
"Credentials should not exist initially.");
assertFalse(manager.verifyPassword("secret"),
"Password verification should fail without credentials.");
assertFalse(manager.verifySecurityAnswer("answer"),
"Answer verification should fail without credentials.");
assertFalse(manager.authenticate("secret", "answer"),
"Authentication should fail without credentials.");
assertNull(manager.getSecurityQuestion(),
"Security question should be null before setup.");
});
}
private static void testSetCredentials() throws Exception {        
withFreshManager((manager, configPath) -> {            
boolean result = manager.setCredentials(
"JournalPass123",
"What is your favourite city?",
"Varanasi"
);
assertTrue(result, "Valid credentials should be saved.");            
assertTrue(manager.hasCredentials(),
"Credentials should be configured.");
assertEquals(
"What is your favourite city?",                    
manager.getSecurityQuestion(),
"The security question should be stored."
);
String configText = Files.readString(configPath);
assertFalse(configText.contains("JournalPass123"),
"The plain-text password must not be stored.");            
assertFalse(configText.contains("Varanasi"),
"The plain-text security answer must not be stored.");
});
}
private static void testRejectInvalidCredentials() throws Exception {        
withFreshManager((manager, configPath) -> {
assertFalse(                    
manager.setCredentials(
"",
"Favourite city?",
 "Varanasi"
),
"An empty password should be rejected."
);
assertFalse(                    
manager.setCredentials(
"JournalPass123",
"",
"Varanasi"
),
"An empty security question should be rejected."
);
assertFalse(
manager.setCredentials(
"JournalPass123"
"Favourite city?",
" "
),
"An empty security answer should be rejected."
);
assertFalse(                    
manager.setCredentials(
null,
"Favourite city?",
"Varanasi"
),
"A null password should be rejected."
);
assertFalse(                    
manager.setCredentials(                            
"JournalPass123",
null,
"Varanasi"
),
"A null security question should be rejected."
);
assertFalse(                    
manager.setCredentials(                            
"JournalPass123",                            
"Favourite city?",                            
null
),
"A null security answer should be rejected."
);
assertFalse(manager.hasCredentials(),
"Invalid setup should not create credentials.");
});
}
private static void testCorrectPassword() throws Exception {        
withConfiguredManager((manager, configPath) -> {assertTrue(                    
manager.verifyPassword("JournalPass123"),
"The correct password should be accepted."
);
});
}
private static void testIncorrectPassword() throws Exception {        
withConfiguredManager((manager, configPath) -> {assertFalse(
manager.verifyPassword("WrongPassword"),
"An incorrect password should be rejected."
);
assertFalse(                    
manager.verifyPassword(""),
"An empty password should be rejected."
);
assertFalse(                    
manager.verifyPassword(null),
"A null password should be rejected."
);
});
}
private static void testCorrectSecurityAnswer() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(                    
manager.verifySecurityAnswer("Varanasi"),
"The correct security answer should be accepted."
);
});
}
private static void testIncorrectSecurityAnswer() throws Exception {        
withConfiguredManager((manager, configPath) -> {             
assertFalse(                    
manager.verifySecurityAnswer("Delhi"),
"An incorrect security answer should be rejected."
);
assertFalse(                    
manager.verifySecurityAnswer(""),
"An empty security answer should be rejected."
);
assertFalse(                   
manager.verifySecurityAnswer(null),
"A null security answer should be rejected."
);
});
}
private static void testNormalizedSecurityAnswer() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(                    
manager.verifySecurityAnswer("varanasi"),
"Security answer matching should ignore letter case."
);
assertTrue(
manager.verifySecurityAnswer("  Varanasi  "),
"Security answer matching should ignore outer spaces."
);
});
}
private static void testSuccessfulAuthentication() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(                    
manager.authenticate("JournalPass123", "Varanasi"),
"Both correct credentials should authenticate."
);
});
}
private static void testFailedAuthentication() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertFalse(                    
manager.authenticate("WrongPassword", "Varanasi"),
"A wrong password should fail authentication."
);
assertFalse(                    
manager.authenticate("JournalPass123", "Delhi"),
"A wrong security answer should fail authentication."
);
assertFalse(                    
manager.authenticate("WrongPassword", "Delhi"),
"Two incorrect credentials should fail authentication."
);
assertFalse(                    
manager.authenticate(null, "Varanasi"),
"A null password should fail authentication."
);
assertFalse(manager.authenticate("JournalPass123", null),
"A null security answer should fail authentication."
);
});
}
private static void testGetSecurityQuestion() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertEquals(
"What is your favourite city?",                    
manager.getSecurityQuestion(),
"The configured security question should be returned."
);
});
}
private static void testChangePassword() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(                    
manager.changePassword(
"JournalPass123",
"NewJournalPass456"
),
"Password change should succeed with the correct old password."
);
assertTrue(manager.verifyPassword("NewJournalPass456"),
"The new password should be accepted."
);
assertFalse(                    
manager.verifyPassword("JournalPass123"),
"The old password should no longer be accepted."
);
});
}
private static void testRejectPasswordChange() throws Exception {        
withConfiguredManager((manager, configPath) -> {
assertFalse(manager.changePassword(
"WrongPassword",
"NewJournalPass456"
),
"A wrong current password should prevent password changes."
);
assertFalse(                    
manager.changePassword(
"JournalPass123",
""
),
"An empty new password should be rejected."
);
assertTrue(                    
manager.verifyPassword("JournalPass123"),
"The original password should remain valid."
);
});
}
private static void testChangeSecurityQuestion() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(
manager.changeSecurityQuestion(
"JournalPass123",
"What is your hometown?",
"Hyderabad"
),
"Security question change should succeed."
);
assertEquals(
"What is your hometown?",                    
manager.getSecurityQuestion(),
"The new security question should be stored."
);
assertTrue(                    
manager.verifySecurityAnswer("Hyderabad"),
"The new security answer should be accepted."
);
assertFalse(                    
manager.verifySecurityAnswer("Varanasi"),
"The old security answer should no longer be accepted."
);
});
}
private static void testRejectSecurityQuestionChange() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertFalse(                    
manager.changeSecurityQuestion(
"WrongPassword",
"What is your hometown?",
"Hyderabad"
),
"A wrong password should prevent security changes."
);
assertFalse(                    
manager.changeSecurityQuestion(
"JournalPass123",
"",
"Hyderabad"
),
"An empty question should be rejected."
);
assertFalse(                    
manager.changeSecurityQuestion(
"JournalPass123",
"What is your hometown?",
""
),
"An empty answer should be rejected."
);
assertEquals(
"What is your favourite city?",                    
manager.getSecurityQuestion(),
"The original question should remain unchanged."
);
assertTrue(                    
manager.verifySecurityAnswer("Varanasi"),
"The original answer should remain valid."
);
});
}
private static void testCredentialPersistence() throws Exception {        
withFreshManager((manager, configPath) -> {
assertTrue(                    
manager.setCredentials(
"JournalPass123",
"What is your favourite city?",
"Varanasi"
),
"Initial credential setup should succeed."
);
ConfigManager reloadedConfig = new ConfigManager(configPath);            
assertTrue(                    
reloadedConfig.loadConfiguration(),
"Configuration should reload successfully."
);
AuthenticationManager reloadedManager =                    
new AuthenticationManager(reloadedConfig);
assertTrue(                    
reloadedManager.hasCredentials(),
"Credentials should persist in the configuration."
);
assertTrue(                    
reloadedManager.verifyPassword("JournalPass123"),
"The password should remain verifiable after reload."
);
assertTrue(                    
reloadedManager.verifySecurityAnswer("Varanasi"),
"The answer should remain verifiable after reload."
);
});
}
private static void testClearCredentials() throws Exception {        
withConfiguredManager((manager, configPath) -> {            
assertTrue(manager.hasCredentials(),
"Credentials should exist before clearing.");
assertTrue(                    
manager.clearCredentials(),
"Clearing credentials should succeed."
);
assertFalse(manager.hasCredentials(),
"Credentials should no longer exist.");
assertFalse(                    
manager.verifyPassword("JournalPass123"),
"Password verification should fail after clearing."
);
assertFalse(                   
manager.verifySecurityAnswer("Varanasi"),
 "Answer verification should fail after clearing."
);
assertNull(manager.getSecurityQuestion(),                    
"The security question should be unavailable after clearing."
);
});
}
private static void testNullConfigManager() {        
assertThrows(
IllegalArgumentException.class,
() -> new AuthenticationManager(null),
 "A null ConfigManager should be rejected."
);
}
/**
* Creates an isolated temporary configuration and runs one test.
*/
private static void withFreshManager(
ManagerTestAction action) throws Exception {
Path tempDirectory = Files.createTempDirectory(
"stealthjournal-auth-tests-“
);        
try {
Path configPath = tempDirectory.resolve("ui_settings.txt");            
ConfigManager configManager = new ConfigManager(configPath);
assertTrue(                    
configManager.loadConfiguration(),
"Temporary configuration should load successfully."
);
AuthenticationManager manager =                    
new AuthenticationManager(configManager);
action.run(manager, configPath);
} finally {
deleteRecursively(tempDirectory);
}
}
/**
* Creates an isolated manager and configures known test credentials.
*/
private static void withConfiguredManager(
ManagerTestAction action) throws Exception {
withFreshManager((manager, configPath) -> {            
assertTrue(                    
manager.setCredentials(                            
"JournalPass123",
"What is your favourite city?",
 "Varanasi"
),
"Test credentials should be configured successfully."
);
action.run(manager, configPath);
});
}
private static void deleteRecursively(Path root) throws IOException {        
if (root == null || !Files.exists(root)) {            
return;
}
try (Stream<Path> paths = Files.walk(root)) {
Path[] entries = paths
.sorted(Comparator.reverseOrder())
.toArray(Path[]::new);
for (Path entry : entries) {
Files.deleteIfExists(entry);
}
}
}
private static void runTest(            
String testName,
CheckedRunnable test) {
try {            
test.run();            
passedTests++;
System.out.println("[PASS] " + testName);
} catch (Throwable failure) {            
failedTests++;
System.err.println("[FAIL] " + testName
+ " — " + failure.getMessage());
}
}
private static void printSummary() {
System.out.println("----------------------------------------");
System.out.println("Tests passed: " + passedTests);
System.out.println("Tests failed: " + failedTests);
System.out.println("Total tests:  " + (passedTests + failedTests));
System.out.println("----------------------------------------");
if (failedTests > 0) {
System.out.println("RESULT: SOME TESTS FAILED");            System.exit(1);
}
System.out.println("RESULT: ALL TESTS PASSED");
}
private static void assertTrue(boolean condition, String message) {        
if (!condition) {            
throw new AssertionError(message);
}
}
private static void assertFalse(boolean condition, String message) {        
assertTrue(!condition, message);
}
private static void assertNull(Object value, String message) {        
if (value != null) {            
throw new AssertionError(                    
message + " Actual value: " + value
);
}
}
private static void assertEquals(
Object expected,
Object actual,
String message) {
if (expected == null ? actual != null : !expected.equals(actual)) {            
throw new AssertionError(                    
message
+ " Expected: [" + expected + "]"
+ ", actual: [" + actual + "]"
);
}
}
private static void assertThrows(
Class<? extends Throwable> expectedType,
CheckedRunnable action,
String message) {
try {           
action.run();
} catch (Throwable actual) {            
if (expectedType.isInstance(actual)) {                
return;
}
throw new AssertionError(                    
message + " Expected " + expectedType.getSimpleName()                            
+ " but received "
+ actual.getClass().getSimpleName(),
actual
);
}
throw new AssertionError(                
message + " Expected exception: "
+ expectedType.getSimpleName()
);
}
@FunctionalInterface    
private interface CheckedRunnable {        
void run() throws Exception;
}
@FunctionalInterface    private interface ManagerTestAction {
void run(
AuthenticationManager manager,
Path configPath) throws Exception;
}
}
