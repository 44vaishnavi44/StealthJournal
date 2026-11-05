package org.stealthjournal.crypto; 
import org.stealthjournal.util.Constants; 
/** 
* Provides encryption and decryption functionality for StealthJournal. 
* 
* The project uses a character-shifting Caesar Cipher at the ASCII level. 
* A validation token is inserted into encrypted content so that the  
* application can determine whether a supplied key successfully 
decrypted  
* the content. 
*/ 
public final class CryptoEngine{ 
/** 
* Private constructor because this class provides utility methods. 
*/ 
private CryptoEngine() { 
throw new AssertionError(“CryptoEngine must not be instantiated.”); 
} 
/** 
* Encrypts plain text using an ASCII-level Caesar shift. 
* 
* The validation token is placed at the beginning of the plaintext 
* before encryption.During decryption, the token is restored only  
* when the correct key is supplied. 
* 
* @param plainText text to encrypt 
* @param key Caesar shift key 
* @return encrypted text  
* @throws IllegalArgumentException if plainText is null or key is invalid  
*/ 
public static String encrypt(String plainText, int key) { 
validateText(plainText); 
validateKey(key); 
String contentWithToken = 
Constants.AUTH_VERIFIED_TOKEN + plainText; 
return shiftAscii(contentWithToken, key); 
} 
/** 
* Decrypts encrypted text using the supplied Caesar shift key. 
*  
* After decryption, the method checks for the validation token. 
* If the token is absent, the supplied key is considered incorrect. 
* 
* @param encryptedText encrypted journal content 
* @param key Caesar shift key 
* @return decrypted plaintext without the validation token 
* @throws IllegalArgumentException if encryptedText is null or key is 
invalid 
* @throws SecurityException if the supplied key is incorrect 
*/ 
public static String decrypt(String encryptedText, int key) { 
validateText(encryptedText); 
validateKey(key); 
String decryptedText = shiftAscii(encryptedText, -key); 
String token = Constants.AUTH_VERIFIED_TOKEN; 
if(!decryptedText.startsWith(token)) { 
throw new SecurityException(“Incorrect encryption key.”); 
} 
return decryptedText.substring(token.length()); 
} 
/** 
* Checks whether an encrypted string can be successfully decrypted 
* using the supplied key. 
*  
* This method does not expose the decrypted journal content. 
* 
* @param encryptedText encrypted text  
* @param key key to test  
* @return true if the key successfully validates the token; 
*        false otherwise  
*/ 
public static boolean isValidKey(String encryptedText, int key) { 
  if (encryptedText == null || encryptedText.isEmpty()) { 
    return false; 
  } 
  if (!isValidkeyValue(key)) { 
    return false; 
  } 
  try { 
    String decryptedText = shiftAscii(encryptedText, -key); 
    return decryptedText.startsWith( 
        Contents.AUTH_VERIFIED_TOKEN 
    ); 
  }catch (RuntimeException exception) { 
    return false; 
    } 
} 
/** 
* Performs the Caesar character shift at the ASCII level. 
* 
* Every character is treated according to its ASCII value. 
* Characters in the printable ASCII range (32-126) are wrapped 
* within that range so that encrypted text remains printable. 
* 
* Characters outside the printable ASCII range are preserved. 
* 
* @param text text to shift  
* @param shift positive or negative Caesar shift 
* @return shifted text  
*/ 
private static String shiftAscii(String text, int shift) { 
  StringBuilder result = new StringBuilder(text.length()); 
  for (int index = 0; index < text.length(); index++) { 
    char currentCharacter = text.charAt(index); 
    if (isPrintableAscii(currentCharacter)) { 
      int shiftedValue = shiftPrintableAscii( 
          currentCharacter, 
          shift 
     ); 
    result.append((char) shiftedValued); 
}else { 
/* 
* Newline, carriage return and other non-printable 
* characters are preserved so journal formatting remains  
* intact.  
*/ 
result.append(currentCharacter); 
} 
} 
return result.to.String(); 
} 
/** 
* Shifts a printable ASCII character and wraps around the printable 
* ASCII range. 
*  
* Printable ASCII characters occupy decimal values 32 through 126. 
* 
* @param character printable ASCII character  
* @param shift Caesar shift  
* @return shifted printable ASCII value  
*/ 
private static int shiftPrintableAscii(char character, int shift) { 
  final int printableMinimum = 32; 
  final int printableMaximum = 126; 
  final int printableRange =  
      printableMaximum - printableMinimum + 1; 
  int normalizedShift = shift % printableRange; 
  int shiftedValue =  
      character + nomalizedShift; 
  if (shiftedValue > printableMaximum) { 
    shiftedValue =  
        printableMinimum  
        + (shiftedValue - printableMaximum) 
        % printableRange; 
  }else if (shiftedValue < printableMinimum) { 
    shiftedValue =  
        printableMaximum  
        - (printableMinimum - shiftedValue - 1) 
        %printableRange; 
  } 
  return shiftedValue; 
} 
/** 
* Determines whether a character belongs to printable ASCII. 
*  
* @param character character to inspect  
* @return true when the character is printable ASCII  
*/ 
private static boolean isPrintableAscii(char character) { 
return character >= Constants.ASCII_MIN + 32 
&&character <= Constants.ASCII_MAX - 1; 
} 
/**  
* Validations text supplied to the encryption/decryption methods. 
* 
* @param text text to validate 
* @throws IllegalArgumentException when text is null   
*/ 
private static void validateText(String text) { 
if (text == null) { 
throw new IllegalArgumentException( 
“Text cannot be null.” 
); 
} 
} 
/** 
* Validates a Caesar cipher key. 
* 
* The key is required to remain within the ASCII range so that  
* the Caesar shift remains deterministic and compatible with the 
* project’s ASCII-level encryption design. 
* 
* @param key Caesar shift key 
* @throws IllegalArgumentException when the key is outside the  
*      supported range  
*/ 
private static void validateKey(int key) { 
  if (!isValidKeyValue(key)) { 
    throw new IllegalArgumentException( 
        “Encryption key must be between” 
         + Constants.ASCII_MIN 
         + “and” 
         + Constants.ASCII_MAX 
         + “.” 
    ); 
  } 
}  
/** 
* Determines whether a key is within the supported ASCII range. 
* 
* @param key key to validate 
* @return true when the key is valid 
*/ 
private static boolean isValidKeyValue(int key) { 
return key >= Constants.ASCII_MIN 
&& <= Constants.ASCII_MAX; 
} 
} 