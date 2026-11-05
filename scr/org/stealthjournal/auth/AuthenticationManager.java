package org.stealthjournal.auth;  
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import org.stealthjournal.config.ConfigManager;  
/**  
* Manages StealthJournal's secondary password and security 
question.  
*  
* Authentication credentials are stored in ConfigManager as 
salted  
* SHA-256 hashes. Plain-text passwords and security answers 
are never  
* written to the configuration file.  
*/  
public final class AuthenticationManager {  
private static final String KEY_PASSWORD_HASH = 
"auth.passwordHash";  
private static final String KEY_PASSWORD_SALT = 
"auth.passwordSalt";  
private static final String KEY_SECURITY_QUESTION = 
"auth.securityQuestion";  
private static final String KEY_ANSWER_HASH = 
"auth.securityAnswerHash";  
private static final String KEY_ANSWER_SALT = 
"auth.securityAnswerSalt";       
private static final int SALT_LENGTH = 16;       
private final ConfigManager configManager;      
private final SecureRandom secureRandom;       
/**       
* Creates an AuthenticationManager using the default 
configuration file.       
*/      
public AuthenticationManager() {          
this(new ConfigManager());      
}       
/**       
* Creates an AuthenticationManager using the supplied 
configuration manager.       
*       
* @param configManager configuration manager to use       
*/      
public AuthenticationManager(ConfigManager configManager) {         
if (configManager == null) {              
throw new IllegalArgumentException(                     
"ConfigManager must not be null."              
);          
}           
this.configManager = configManager;          
this.secureRandom = new SecureRandom();      
}       
/**       
* Configures the secondary password and security question.      
*       
* @param password        
secondary password       
* @param securityQuestion security question displayed during 
recovery       
* @param securityAnswer   answer to the security question      
* @return true if the credentials were saved successfully       
*/      
public synchronized boolean setCredentials(              
String password,              
String securityQuestion,              
String securityAnswer) {           
if (!isValidPassword(password)                  
|| !isValidQuestion(securityQuestion)                  
|| !isValidAnswer(securityAnswer)) {              
return false;          
}           
String passwordSalt = generateSalt();          
String answerSalt = generateSalt();           
String passwordHash = hashCredential(                 
passwordSalt,                  
"password",                  
password          
);           
String answerHash = hashCredential(                  
answerSalt,                  
"answer",                  
normalizeAnswer(securityAnswer)          
);           
if (passwordHash == null || answerHash == null) {              
return false;          
}           
/*           
* Save hashes and salts rather than the original credentials.          
* ConfigManager persists each setting individually.           
*/          
boolean saved = configManager.setSetting(                 
KEY_PASSWORD_SALT, passwordSalt          
);           
saved = configManager.setSetting(                 
KEY_PASSWORD_HASH, passwordHash          
) && saved;           
saved = configManager.setSetting(                 
KEY_ANSWER_SALT, answerSalt          
) && saved;           
saved = configManager.setSetting(                 
KEY_ANSWER_HASH, answerHash          
) && saved;           
saved = configManager.setSetting(                 
KEY_SECURITY_QUESTION, securityQuestion.trim()          
) && saved;           
if (!saved) {              
removeCredentialSettings();              
return false;          
}           
return hasCredentials();      
}       
/**       
* Verifies the supplied secondary password.       
*       
* @param password password entered by the user       
* @return true if the password matches       
*/      
public synchronized boolean verifyPassword(String password) {         
if (password == null || !hasCredentials()) {              
return false;          
}           
String salt =  
configManager.getSetting(KEY_PASSWORD_SALT);         
String storedHash = 
configManager.getSetting(KEY_PASSWORD_HASH);           
if (salt == null || storedHash == null) {              
return false;          
}           
String suppliedHash = hashCredential(                  
salt,                  
"password",                  
password          
);           
return suppliedHash != null                  
&& constantTimeEquals(storedHash, suppliedHash);      
}       
/**       
* Verifies the supplied security answer.       
* Answers are case-insensitive and leading/trailing spaces are 
ignored.       
*       
* @param answer answer entered by the user       
* @return true if the answer matches       
*/      
public synchronized boolean verifySecurityAnswer(String 
answer) {          
if (answer == null || !hasCredentials()) {              
return false;          
}           
String salt = configManager.getSetting(KEY_ANSWER_SALT);         
String storedHash = 
configManager.getSetting(KEY_ANSWER_HASH);           
if (salt == null || storedHash == null) {              
return false;          
}           
String suppliedHash = hashCredential(                  
salt,                  
"answer",                  
normalizeAnswer(answer)          
);           
return suppliedHash != null                  
&& constantTimeEquals(storedHash, suppliedHash);      
}       
/**       
* Authenticates using both the secondary password and 
security answer.       
*       
* @param password password entered by the user       
* @param answer security answer entered by the user       
* @return true only if both credentials match       
*/      
public synchronized boolean authenticate(              
String password,              
String answer) {           
boolean passwordMatches = verifyPassword(password);         
boolean answerMatches = verifySecurityAnswer(answer);          
return passwordMatches && answerMatches;      
}       
/**       
* Changes the secondary password after verifying the current 
password.       
*       
* @param currentPassword current password       
* @param newPassword    new password       
* @return true if the password was changed successfully       
*/      
public synchronized boolean changePassword(              
String currentPassword,              
String newPassword) {           
if (!verifyPassword(currentPassword)                  
|| !isValidPassword(newPassword)) {              
return false;          
}           
String newSalt = generateSalt();          
String newHash = hashCredential(                  
newSalt,                  
"password",                  
newPassword          
);           
if (newHash == null) {              
return false;          
}           
String oldSalt = 
configManager.getSetting(KEY_PASSWORD_SALT);         
String oldHash = 
configManager.getSetting(KEY_PASSWORD_HASH);           
if (!configManager.setSetting(KEY_PASSWORD_SALT, 
newSalt)) {              
return false;          
}           
if (!configManager.setSetting(KEY_PASSWORD_HASH, 
newHash)) {              
if (oldSalt != null) {                 
configManager.setSetting(KEY_PASSWORD_SALT, oldSalt);             
}               
if (oldHash != null) {                 
configManager.setSetting(KEY_PASSWORD_HASH, oldHash);             
} 
return false;          
}           
return verifyPassword(newPassword);      
}       
/**       
* Changes the security question and answer after verifying the 
password.       
*       
* @param currentPassword current secondary password       
* @param newQuestion    replacement security question       
* @param newAnswer    replacement security answer       
* @return true if the security details were updated successfully      
*/      
public synchronized boolean changeSecurityQuestion(             
String currentPassword,              
String newQuestion,              
String newAnswer) {           
if (!verifyPassword(currentPassword)                  
|| !isValidQuestion(newQuestion)                  
|| !isValidAnswer(newAnswer)) {              
return false;          
}           
String newSalt = generateSalt();          
String newHash = hashCredential(                  
newSalt,                  
"answer",                  
normalizeAnswer(newAnswer)          
);           
if (newHash == null) {              
return false;          
}           
String oldQuestion = configManager.getSetting(                 
KEY_SECURITY_QUESTION          
);          
String oldSalt = 
configManager.getSetting(KEY_ANSWER_SALT);          
String oldHash = 
configManager.getSetting(KEY_ANSWER_HASH);           
if (!configManager.setSetting(KEY_ANSWER_SALT, newSalt)) 
{              
return false;          
}           
if (!configManager.setSetting(KEY_ANSWER_HASH, 
newHash)) {              
restoreSetting(KEY_ANSWER_SALT, oldSalt);             
restoreSetting(KEY_ANSWER_HASH, oldHash);              
return false;          
}           
if (!configManager.setSetting(                 
KEY_SECURITY_QUESTION, newQuestion.trim())) {             
restoreSetting(KEY_ANSWER_SALT, oldSalt);             
restoreSetting(KEY_ANSWER_HASH, oldHash);             
restoreSetting(KEY_SECURITY_QUESTION, oldQuestion);             
return false;          
}           
return verifySecurityAnswer(newAnswer);      
}       
/**       
* Returns the configured security question.       
*       
* @return security question, or null if credentials are not 
configured       
*/      
public synchronized String getSecurityQuestion() {          
if (!hasCredentials()) {              
return null;          
}           
return 
configManager.getSetting(KEY_SECURITY_QUESTION);     }      
/**       
* Checks whether all required authentication settings exist.       
*       
* @return true if credentials are configured       
*/      
public synchronized boolean hasCredentials() {          
String passwordHash = 
configManager.getSetting(KEY_PASSWORD_HASH);         
String passwordSalt = 
configManager.getSetting(KEY_PASSWORD_SALT);         
String question = 
configManager.getSetting(KEY_SECURITY_QUESTION);         
String answerHash = 
configManager.getSetting(KEY_ANSWER_HASH);          
String answerSalt = 
configManager.getSetting(KEY_ANSWER_SALT);           
return isNonEmpty(passwordHash)                  
&& isNonEmpty(passwordSalt)                  
&& isNonEmpty(question)                  
&& isNonEmpty(answerHash)                  
&& isNonEmpty(answerSalt);      
}       
/**       
* Removes authentication settings from the configuration.       
*       
* @return true if all credential settings were removed 
successfully       
*/      
public synchronized boolean clearCredentials() {          
boolean result = true;           
result = 
configManager.removeSetting(KEY_PASSWORD_HASH) && 
result;          
result = 
configManager.removeSetting(KEY_PASSWORD_SALT) && 
result;          
result = 
configManager.removeSetting(KEY_SECURITY_QUESTION) 
&& result;          
result = configManager.removeSetting(KEY_ANSWER_HASH) 
&& result;          
result = configManager.removeSetting(KEY_ANSWER_SALT) 
&& result;           
return result && !hasCredentials();      
}       
private void removeCredentialSettings() {         
configManager.removeSetting(KEY_PASSWORD_HASH);         
configManager.removeSetting(KEY_PASSWORD_SALT);         
configManager.removeSetting(KEY_SECURITY_QUESTION);         
configManager.removeSetting(KEY_ANSWER_HASH);         
configManager.removeSetting(KEY_ANSWER_SALT);      
}       
private void restoreSetting(String key, String value) {          
if (value == null) {              
configManager.removeSetting(key);          
} else {              
configManager.setSetting(key, value);          
}      
}       
private String generateSalt() {          
byte[] salt = new byte[SALT_LENGTH];         
secureRandom.nextBytes(salt);           
return Base64.getEncoder().encodeToString(salt);      
}       
private String hashCredential(              
String salt,              
String credentialType,              
String credential) {           
if (salt == null || credentialType == null || credential == null) {             
return null;          
}           
try {              
MessageDigest digest = 
MessageDigest.getInstance("SHA-256");               
String value = salt + ":" + credentialType + ":" + credential;             
byte[] hash = digest.digest(                     
value.getBytes(StandardCharsets.UTF_8)              
);               
return Base64.getEncoder().encodeToString(hash);           
} catch (NoSuchAlgorithmException exception) {              
// SHA-256 is required by Java; return failure if unavailable.             
return null;          
}      
}       
private boolean constantTimeEquals(String first, String second) 
{          
if (first == null || second == null) {              
return false;          
}           
byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);         
byte[] secondBytes = 
second.getBytes(StandardCharsets.UTF_8);           
try {              
return MessageDigest.isEqual(firstBytes, secondBytes);          
} finally {              
Arrays.fill(firstBytes, (byte) 0);              
Arrays.fill(secondBytes, (byte) 0);          
}      
}       
private String normalizeAnswer(String answer) {          
return answer.trim().toLowerCase(Locale.ROOT);      
}       
private boolean isValidPassword(String password) {          
return password != null && !password.trim().isEmpty();      
}       
private boolean isValidQuestion(String question) {          
return question != null && !question.trim().isEmpty();      
}       
private boolean isValidAnswer(String answer) {          
return answer != null && !answer.trim().isEmpty();      
}       
private boolean isNonEmpty(String value) {          
return value != null && !value.trim().isEmpty();      
}  
}  