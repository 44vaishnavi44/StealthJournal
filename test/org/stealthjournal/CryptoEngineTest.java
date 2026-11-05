package test.org.stealthjournal;  
import org.stealthjournal.crypto.CryptoEngine;  
/**  
* Test class for StealthJournal's CryptoEngine.  
* 
* Tests:  
* 1. Encryption produces different content from the original text.  
* 2. Correct-key decryption restores the original text.  
* 3. Incorrect-key detection works correctly.  
* 4. Empty text can be encrypted and decrypted.  
* 5. Multi-line diary content survives encryption/decryption.  
* 6. Special printable ASCII characters survive 
encryption/decryption.  
* 7. Invalid keys are rejected.  
* 
* This class uses Java assertions so it can be executed with:  
*  
* java -ea org.stealthjournal.CryptoEngineTest  
*  
* It also provides a main method that reports the result 
explicitly,  
* making it convenient to run without a test framework.  
*/  
public final class CryptoEngineTest { 
private static final int TEST_KEY = 7; 
private static final int WRONG_KEY = 19;  
private static int testsPassed = 0;  
private static int testsFailed = 0; 
/**  
* Private constructor prevents instantiation.  
*/  
private CryptoEngineTest() {  
throw new AssertionError("Test class must not be instantiated.");  
}  
/**  
* Runs all CryptoEngine tests.  
*  
* @param args command-line arguments  
*/  
public static void main(String[] args) {  
System.out.println("=====");  
System.out.println(" StealthJournal - CryptoEngine Tests"); 
System.out.println("=====");  
runTest(  
"Encryption changes plaintext", 
CryptoEngineTest::testEncryptionChangesPlaintext);  
runTest(  
"Correct-key decryption restores plaintext", 
CryptoEngineTest::testCorrectKeyDecryption);  
runTest(  
"Incorrect key is rejected",  
CryptoEngineTest::testIncorrectKey);  
runTest(  
"isValidKey accepts the correct key",  
CryptoEngineTest::testValidKey);  
runTest(  
"isValidKey rejects an incorrect key",  
CryptoEngineTest::testInvalidKey);  
runTest(  
"Empty text round-trip",  
CryptoEngineTest::testEmptyText);  
runTest(  
"Multi-line diary round-trip",  
CryptoEngineTest::testMultiLineText);  
runTest(  
"Printable ASCII round-trip", 
CryptoEngineTest::testPrintableAscii);  
runTest(  
"Invalid negative key is rejected", 
CryptoEngineTest::testNegativeKey);  
runTest(  
"Invalid key above ASCII range is rejected", 
CryptoEngineTest::testKeyAboveAsciiRange);  
runTest(  
"Null plaintext is rejected",  
CryptoEngineTest::testNullPlaintext);  
runTest(  
"Null encrypted text is rejected", 
CryptoEngineTest::testNullEncryptedText);  
System.out.println();  
System.out.println("=====");  
System.out.println(" Tests passed : " + testsPassed); 
System.out.println(" Tests failed : " + testsFailed); 
System.out.println("=====");  
if (testsFailed > 0) {  
throw new AssertionError(  
"CryptoEngine tests failed:  " + testsFailed  );  
} 
System.out.println("All CryptoEngine tests passed.");  
}  
/**  
* Verifies that encryption does not return the original plaintext.  
*/  
private static void testEncryptionChangesPlaintext() {  
String plainText =  "Today was a productive day.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
assertTrue(  
!plainText.equals(encryptedText),  
"Encrypted text must differ from plaintext." );  
}  
/**  
* Verifies a complete encryption/decryption round trip.  
*/  
private static void testCorrectKeyDecryption() {  
String plainText =  "This is my private diary entry.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
String decryptedText =  CryptoEngine.decrypt(encryptedText, TEST_KEY); 
assertEquals( plainText,  decryptedText,  "Decrypted text must match the original plaintext." );  
}  
/**  
* Verifies that decrypting with an incorrect key is rejected.  
*/  
private static void testIncorrectKey() {  
String plainText =  "This entry must remain protected.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
boolean rejected = false;  
try {  
CryptoEngine.decrypt( encryptedText,  WRONG_KEY  );  
} catch (SecurityException exception) {  
rejected = true;  
}  
assertTrue(  
rejected,  
"An incorrect encryption key must be rejected."  );  
}  
/**  
* Verifies that isValidKey() accepts the correct key.  
*/  
private static void testValidKey() {  
String plainText =  "Correct key validation test.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
assertTrue(  
CryptoEngine.isValidKey( encryptedText,  TEST_KEY ),  "The correct key must be reported as valid." );  
}  
/**  
* Verifies that isValidKey() rejects an incorrect key.  
*/  
private static void testInvalidKey() {  
String plainText =  "Incorrect key validation test.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
assertTrue( !CryptoEngine.isValidKey(encryptedText,  WRONG_KEY ),  "An incorrect key must be reported as invalid." );  
}  
/**  
* Verifies that an empty diary entry can be encrypted and 
decrypted.  
*/  
private static void testEmptyText() {  
String plainText = "";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY); 
String decryptedText =  CryptoEngine.decrypt(encryptedText, TEST_KEY); 
assertEquals(  plainText,  decryptedText,  "Empty text must survive encryption/decryption." );  
}  
/**  
* Verifies that multi-line diary content survives a round trip.  
*/  
private static void testMultiLineText() {  
String plainText =  "Dear Diary,\n"  + "Today I worked on StealthJournal.\n"  + "The project is progressing well.\n"  + "Good night.";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
String decryptedText =  CryptoEngine.decrypt(encryptedText, TEST_KEY); 
assertEquals( plainText,  decryptedText,  "Multi-line diary content must remain unchanged."  );  
}  
/**  
* Verifies that printable ASCII characters survive encryption  
* and decryption.  
*/  
private static void testPrintableAscii() {  
String plainText = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"  + "abcdefghijklmnopqrstuvwxyz"  + "0123456789"  + "!@#$%^&*()_+-=[]{}|;:',.<>/?`~"  + " \"\\";  
String encryptedText =  CryptoEngine.encrypt(plainText, TEST_KEY);  
String decryptedText =  CryptoEngine.decrypt(encryptedText, TEST_KEY); 
assertEquals( plainText,  decryptedText,  "Printable ASCII characters must survive "  + "encryption/decryption."  );  
}  
/**  
* Verifies that a negative key is rejected.  
*/  
private static void testNegativeKey() {  
boolean rejected = false;  
try {  
CryptoEngine.encrypt(  "Test",  -1  );  
} catch (IllegalArgumentException exception) {  
rejected = true;  
}  
assertTrue(  
rejected,  
"Negative encryption keys must be rejected."  
); 
}  
/**  
* Verifies that a key greater than the supported ASCII range  
* is rejected.  
*/  
private static void testKeyAboveAsciiRange() {  
boolean rejected = false;  
try {  
CryptoEngine.encrypt(  "Test",  128  );  
} catch (IllegalArgumentException exception) {  
rejected = true;  
}  
assertTrue(  rejected,  "Keys above the ASCII range must be rejected."  );  
}  
/**  
* Verifies that null plaintext is rejected.  
*/  
private static void testNullPlaintext() {  
boolean rejected = false;  
try {  
CryptoEngine.encrypt(  null,  TEST_KEY  );  
} catch (IllegalArgumentException exception) {  
rejected = true;  
}  
assertTrue(  rejected,  "Null plaintext must be rejected."  );  
}  
/**  
* Verifies that null encrypted text is rejected.  
*/  
private static void testNullEncryptedText() {  
boolean rejected = false;  
try {  
CryptoEngine.decrypt( null,  TEST_KEY  );  
} catch (IllegalArgumentException exception) {  
rejected = true;  
}  
assertTrue( rejected,  "Null encrypted text must be rejected."  );  
}  
/**  
* Executes one test and records its result.  
*  
* @param testName human-readable test name  
* @param test test operation  
*/  
private static void runTest(  String testName,  Runnable test  ) {  
try {  
test.run();  
testsPassed++;  
System.out.println(  "[PASS] " + testName  );  
} catch (AssertionError | RuntimeException exception) { 
testsFailed++;  
System.out.println( "[FAIL] " + testName  );  
System.out.println( "     " + exception.getMessage()); 
} 
} 
/** 
* Performs a boolean assertion.  
*  
* @param condition condition that must be true  
* @param message failure message  
*/  
private static void assertTrue(  boolean condition,  String message ) {  
if (!condition) {  
throw new AssertionError(message);  
}  
} 
/**  
* Performs an equality assertion for strings.  
*  
* @param expected expected value  
* @param actual actual value  
* @param message failure message  
*/  
private static void assertEquals(  
String expected,  
String actual,  
String message  
) {  
if (expected == null) {  
if (actual != null) {  
throw new AssertionError(message);  
}  
return;  
}  
if (!expected.equals(actual)) {  
throw new AssertionError(  
message  + " Expected: ["  + expected  + "] but received: ["  + actual  + "]"  );  
}  
}  
}  