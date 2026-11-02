package org.stealthjournal.util;  
import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.File;  
import java.io.FileReader;  
import java.io.FileWriter;  
import java.io.IOException;  
import java.nio.charset.StandardCharsets;  
import java.nio.file.Files;  
import java.nio.file.Path;  
import java.nio.file.Paths;  
import java.util.ArrayList;  
import java.util.Collections;  
import java.util.List;  
/**  
* Provides shared file input/output operations for 
StealthJournal.  
*  
* StealthJournal uses local flat text files rather than a database. 
* This helper centralizes the BufferedReader and 
BufferedWriter logic  
* used by configuration, journal, and other text-file operations.  
*/  
public final class FileIOHelper {  
/** 
* Private constructor prevents instantiation.       
*/      
private FileIOHelper() {          
throw new AssertionError(                  
"FileIOHelper must not be instantiated."          
);      
}       
/**       
* Writes text to a file.       
*       
* Existing content is replaced.       
*       
* @param filePath path of the file       
* @param content content to write       
* @return true when the operation succeeds       
*/      
public static boolean writeText(              
String filePath,              
String content      
) {          
if (!isValidPath(filePath) || content == null) {              
return false;          
}           
File file = new File(filePath);  
try {              
createParentDirectories(file);               
try (BufferedWriter writer =                           
new BufferedWriter(                                   
new FileWriter(                                           
file,                                           
StandardCharsets.UTF_8                                   
)                           
)) {                   
writer.write(content);                  
writer.flush();                   
return true;              
}           
} catch (IOException exception) {              
System.err.println(                      
"Unable to write file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()  
);               
return false;          
}      
}       
/**       
* Appends text to a file.       
*       
* The existing contents are preserved.       
*       
* @param filePath path of the file       
* @param content content to append       
* @return true when the operation succeeds       
*/      
public static boolean appendText(              
String filePath,              
String content      
) {          
if (!isValidPath(filePath) || content == null) {              
return false;          
}           
File file = new File(filePath);           
try { 
createParentDirectories(file);               
try (BufferedWriter writer =                           
new BufferedWriter(                                   
new FileWriter(                                           
file,                                           
StandardCharsets.UTF_8,                                           
true                                   
)                           
)) {                   
writer.write(content);                  
writer.flush();                   
return true;              
}           
} catch (IOException exception) {              
System.err.println(                      
"Unable to append to file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
); 
return false;          
}      
}       
/**       
* Reads the complete contents of a text file.       
*       
* @param filePath path of the file       
* @return file contents, or an empty string if the file cannot       
*    be read       
*/      
public static String readText(              
String filePath      
) {          
if (!isValidPath(filePath)) {              
return "";          
}           
File file = new File(filePath);           
if (!file.exists() || !file.isFile()) {              
return "";          
}           
StringBuilder content = new StringBuilder();   
try (BufferedReader reader =                       
new BufferedReader(                               
new FileReader(                                       
file,                                       
StandardCharsets.UTF_8                               
)                       
)) {               
String line;              
boolean firstLine = true;               
while ((line = reader.readLine()) != null) {                   
if (!firstLine) {                      
content.append(                             
Constants.LINE_SEPARATOR                      
);                  
}                   
content.append(line);                  
firstLine = false;              
}               
return content.toString();  
} catch (IOException exception) {              
System.err.println(                      
"Unable to read file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return "";          
}      
}       
/**       
* Reads a text file line by line.       
*       
* @param filePath path of the file       
* @return list containing all lines, or an empty list when       
*    the file cannot be read       
*/      
public static List<String> readLines(              
String filePath      
) {          
if (!isValidPath(filePath)) {              
return Collections.emptyList(); 
}           
File file = new File(filePath);           
if (!file.exists() || !file.isFile()) {              
return Collections.emptyList();          
}           
List<String> lines = new ArrayList<>();           
try (BufferedReader reader =                       
new BufferedReader(                               
new FileReader(                                       
file,                                       
StandardCharsets.UTF_8                               
)                       
)) {               
String line;               
while ((line = reader.readLine()) != null) {                 
lines.add(line);              
}               
return lines;  
} catch (IOException exception) {              
System.err.println(                      
"Unable to read lines from file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return Collections.emptyList();          
}      
}       
/**       
* Writes a list of lines to a text file.       
*       
* Each list element becomes one line in the resulting file.       
*       
* @param filePath path of the file       
* @param lines lines to write       
* @return true when the operation succeeds       
*/      
public static boolean writeLines(              
String filePath,              
List<String> lines  
) {          
if (!isValidPath(filePath) || lines == null) {              
return false;          
}           
File file = new File(filePath);           
try {              
createParentDirectories(file);               
try (BufferedWriter writer =                           
new BufferedWriter(                                   
new FileWriter(                                           
file,                                           
StandardCharsets.UTF_8                                   
)                           
)) {                   
for (int index = 0;                       
index < lines.size();                       
index++) {                       
String line = lines.get(index);                       
if (line != null) {                          
writer.write(line);  
}                       
if (index < lines.size() - 1) {                          
writer.newLine();                      
}                  
}                   
writer.flush();                   
return true;              
}           
} catch (IOException exception) {              
System.err.println(                      
"Unable to write lines to file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return false;          
}      
}  
/**       
* Determines whether a file exists.       
*       
* @param filePath path of the file       
* @return true when the path exists and points to a regular file      
*/      
public static boolean fileExists(              
String filePath      
) {          
if (!isValidPath(filePath)) {              
return false;          
}           
File file = new File(filePath);           
return file.exists() && file.isFile();      
}       
/**       
* Creates a directory and all missing parent directories.       
*       
* @param directoryPath directory path       
* @return true when the directory exists after the operation      
*/      
public static boolean createDirectory(              
String directoryPath  
) {          
if (!isValidPath(directoryPath)) {              
return false;          
}           
File directory = new File(directoryPath);           
if (directory.exists()) {              
return directory.isDirectory();          
}           
try {              
return directory.mkdirs();           
} catch (SecurityException exception) {             
System.err.println(                      
"Unable to create directory: "                      
+ directoryPath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return false;          
} 
}       
/**       
* Deletes a file if it exists.       
*       
* @param filePath path of the file       
* @return true if the file was deleted or did not exist       
*/      
public static boolean deleteFile(              
String filePath      
) {          
if (!isValidPath(filePath)) {              
return false;          
}           
File file = new File(filePath);           
if (!file.exists()) {              
return true;          
}           
if (!file.isFile()) {              
return false;          
}           
try {  
return file.delete();           
} catch (SecurityException exception) {             
System.err.println(                      
"Unable to delete file: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return false;          
}      
}       
/**       
* Resolves a path using the application's current working 
directory.       
*       
* @param filePath relative or absolute path       
* @return normalized Path object       
*/      
public static Path resolvePath(              
String filePath      
) {          
if (!isValidPath(filePath)) {  
return null;          
}           
try {              
return Paths.get(filePath).normalize();           
} catch (Exception exception) {              
System.err.println(                      
"Unable to resolve path: "                      
+ filePath              
);              
System.err.println(                      
"Reason: "                      
+ exception.getMessage()              
);               
return null;          
}      
}       
/**       
* Creates the parent directories of a file when they do not exist.      
*       
* @param file target file       
*/      
private static void createParentDirectories( 
File file      
) throws IOException {           
File parentDirectory =                 
file.getAbsoluteFile().getParentFile();           
if (parentDirectory != null                  
&& !parentDirectory.exists()) {               
if (!parentDirectory.mkdirs()                      
&& !parentDirectory.exists()) {                   
throw new IOException(                          
"Unable to create parent directory: "                          
+ parentDirectory.getAbsolutePath()                  
);              
}          
}      
}       
/**       
* Checks whether a supplied path is non-null and non-empty.      
*       
* @param filePath path to validate       
* @return true when the path is usable       
*/  
private static boolean isValidPath(  
String filePath  
) {  
return filePath != null  
&& !filePath.trim().isEmpty();   
} 
} 