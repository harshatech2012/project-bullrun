# Project-Bullrun
A tool for data integrity verification based on checksum(s) and digital signature(s). This is
 named after a clandestine, highly classified program to crack encryption of online
  communications and data, which is run by the United States National Security Agency (NSA).

## Modules
The tool is divided into modules with each having a specific purpose. The modules and their
 purpose are listed below:
  - `checksum` - for verifying a file's integrity using cryptographic hash functions
  - `gpg` - for verifying a file's integrity using Gnu Privacy Guard cryptographic algorithm
  -  `gui` - a graphical user-interface for combining `checksum` and `gpg` functions
 
### Usage
> **Note:** You need to have JRE 1.8+ installed on the computer. You may download the same from the
  [this][jre_download] link.

Running a module:
 - Download the `.jar` file from this link.
 - Open the terminal or Powershell (if on windows) and navigate to the directory of the
  downloaded .jar file.
 - Run the following command, while substituting the module-name for one of the modules listed
  above.
    
    `java -jar bullrun.jar <module-name>`
   
   For example, to view the help for the checksum module you would run the following command:
    
    `java -jar bullrun.jar checksum --help`

Each of the has a different user-interface and hence vary in usage. You are recommended to view
 the help for each module using the commands shown above for more understanding. But for
  convenience, the following section shows the usage of each module if various scenarios.

>**Note:** At the moment the GUI not been implemented. So, you have to run the tool from the
> command-line.

#### Checksum module


[jre_download]: https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
