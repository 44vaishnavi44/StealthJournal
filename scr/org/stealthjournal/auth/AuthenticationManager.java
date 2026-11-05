package src.org.stealthjournal.util; 

/** 

Centralized constants used throughout the StealthJournal application. 

This class contains: 

-File and configuration delimiters 

Crypto validation token 

-Default application paths 

-File extensions  

-Configuration keys  

-UI/security-related constant values  

All members are static because the class is intended to provide application-wide constants rather than object state. 

*/ 

Public final class Constants { 

/** 

*Private constructor prevents instantiation. 

*/ 

private Constants () { 

     throw new AssertionError(“Constants class must not be instantiated.”); 

} 

/* 

===== 

 APPLICATION INFORMATORY 

===== 

*/ 

public static final String APPLICATION_NAME = ”StealthJournal”; 

public static final String APPLICATION_VERSION = “1.0”; 

/* 

===== 

 PROJECT DIRECTORIES 

===== 

 */ 

/** 

*Root directory of the application. 

*/ 

public static final String ROOT_DIRECTORY = “StealthJournal”; 

/** 

*Runtime configuration directory. 

*/ 

public static final String CONFIG_DIRECTORY = “config”; 

/** 

*Runtime diary data directory . 

*/ 

public static final String DATA_DIRECTORY  = “data”; 

/**  

*Directory containing journal files. 

*/  

public static final String JOURNALS_DIRECTORY = “data/journals”; 

/** 

*Resources directory. 

*/ 

public static final String RESOURCES_DIRECTORY = “resources”; 

/** 

*Optional UI icon directory. 

*/ 

public  static final String ICONS_DIRECTORY = “resources“; 

/* 

===== 

 CONFIGURATIONS FILES 

===== 

 */ 

/** 

*UI configuration file generated at runtime. 

*/ 

public static final String UI_SETTINGS_FILES = “config/ui_settings.txt”; 

/** 

*Configuration key/value separator. 

*/ 

public static final String CONFIG_KEY_VALUE_DELIMITER = “=”; 

/* 

===== 

JOURNAL FILE SETTINGS  

===== 

*/ 

/** 

*Extension used for encrypted diary files. 

*/ 

public static final String JOURNAL_FILE_EXTENSION = “.txt”; 

/** 

*Default directory used for encrypted journal files. 

*/ 

public static final String DEFAULT_JOURNAL_DIRECTORY = “data/journals”; 

/* 

===== 

TEXT/FILE DELIMITERS 

===== 

*/ 

/** 

*Delimiter used when multiple logical values are stored 

*within a single text record . 

*/ 

public static final String FIELD_DELIMETER = “::”; 

/** 

*Delimiter used for key/value-style records. 

*/ 

public static final String KEY_VALUE_DELIMITER = “=”; 

/** 

*Line separator used when writing text files. 

*/ 

public static final String LINE_SEPARATOR = System.lineSeparator(); 

/* 

===== 

CRYPTOGRAPHY 

===== 

*/ 

/** 

*Validation token inserted by CryptoEngine into encrypted 

*journal content. 

*  

*CryptoEngine uses this token to determine whether the 

*supplied decryption key is correct. 

*/ 

public static final String AUTH_VERIFIED_TOKEN = “AUTH_VERIFIED_TOKEN::”; 

/** 

*ASCII character range used by the Caesar cipher. 

*/ 

public static final int ASCII_MAN = 0; 

public static final int ASCII_MAX = 127; 

/** 

*Default Caesar cipher shift. 

* 

*The actual encryption/decryption key supplied by the user 

*is expected to determine the effective shift. 

*/ 

 public static final int DEFAULT_CAESAR_SHIFT = 3; 

/* 

===== 

CONFIGURATION KEYS  

===== 

*/ 

/** 

*Configuration key for the first-run/setup state. 

*/ 

public static final String CONFIG_SETUP_COMPLETED = “setupCompleted”; 

/** 

*Configuration key for the application’s UI theme. 

*/ 

public static final String CONFIG_THEME = “theme “; 

/** 

*Configuration key for the decoy video path. 

*/ 

public static final String CONFIG_DECOY_VIDEO = “decoyVideo”; 

/** 

*Configuration key for the journal directory . 

*/ 

public static final String CONFIG_JOURNAL_DIRECTORY = “journalDirectory “; 

/* 

===== 

DEFAULT CONFIGURATION VALUES 

===== 

*/ 

/** 

*Default setup state for a fresh installation . 

*/ 

public static final String DEFAULT_SETUP_COMPLETED = “false”; 

/** 

*Default UI theme. 

*/ 

public static final String DEFAULT_THEME = “system”; 

/** 

*Default external decoy video filename. 

* 

*PanicSystemManager may use the configured value instead 

*when a custom decoy video location has been saved. 

*/ 

public static final String DEFAULT_DEECOY_VIDEO = “resources/decoy.mp4”; 

/* 

===== 

UI CONSTANTS  

===== 

*/ 

/** 

*Default diary window title. 

*/ 

public static final String DIARY_WINNDOW_TITLE = “StealthJournal”; 

/** 

*Default dialog title used by authentication prompts. 

*/ 

public static final String AUTH_DIALOG_TITLE = “Authentication”; 

/** 

*Default setup wizard title. 

*/ 

public static final String SETUP_DIALOG_TITLE = “StealthJournal Setup”; 

/* 

===== 

SECURITY / PANIC SYSTEM  

===== 

*/ 

/** 

*Tittle displayed by the trap/error handler. 

*/ 

public static final String CORRUPTION_ERROR_TITLE = “System Error”; 

/** 

*Message displayed by the fake corruption dialog. 

*/ 

public static final String CORRUPTION_ERROR_MESSAGE = “A critical system error has occurred.” 

+ ”The application will now close.”; 

/** 

*Text associated with the deceptive action that triggers 

*the trap handler. 

*/ 

public static final String DECEPTIVE_BUTTON_TEXT = “Click to View”; 

/* 

===== 

FILE ENCODING  

===== 

*/ 

/** 

*Character encoding used for text-file persistence. 

*/ 

public static final String TEXT_ENCODING = “UTF-8”; 

}  

  

 

 

 

 

 

 
