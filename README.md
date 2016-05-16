# CryptoProj2
Second project for Applied Cryptography at AIT Budapest, Spring 2016.
Blob Storage - File upload and download to the Blob using CBC encryption  5/17/2016

General Usage Notes
————————————

-File Storage on the BLOB 

-Encryption done with CBC Mode, using PKCS 5 padding and signed using HMAC, IV implemented through a securerandom  

-To run a project, Microsoft Azure SDK and associated jar files must be implemented on your computer. These files allow you to use the blob class and these classes have various methods. We use these methods and feed a file path in order to upload and download 

-Upon running the program, the user will be asked what operation they want to do. They can either upload, download, encrypt, decrypt, encrypt and upload, download and decrypt, list, or exit the program 

-The program is hardcoded with a blob storage key for ease of use, alternate methods would be encrypting the blob key in a separate file 
