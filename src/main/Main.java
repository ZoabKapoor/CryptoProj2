package main;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.crypto.Cipher;

import cryptography.KeyGenerator;

// create a random key and share the key offline, give the user four options
// separate the four steps, a) upload, b) download, c) encrypt, d) decrypt
public class Main {
	private static Path keyPath;
	private static String StorageName;
	private static String StorageKey;

	public static void main(String[] args) {
		StorageName = "juliamcarr";
		StorageKey = "SSpOZPJ5PJx+f/ehu58tf8jam+HRZo3Dpq1/+SvFT8mHBOWbXIN25e4lHadRR2Teq0i/JD4909PJNy30BEAfWA==";
		Scanner sc = new Scanner(System.in);

		getSessionKey(sc);
		while (true) {
			System.out.println("What operations do you want to:");
			System.out.println(
					"a) upload, b) download, c) encrypt, d) decrypt, e) encrypt & upload, f) download & decrypt, g) list, h) exit");
			String option = sc.nextLine();
			try {
				switch (option) {
				case "a":
					uploadDownload("upload", sc, "");
					break;
				case "b":
					uploadDownload("download", sc, "");
					break;
				case "c":
					encryptDecrypt(sc, "encrypt", "");
					break;
				case "d":
					encryptDecrypt(sc, "decrypt", "");
					break;
				case "e":
					String encryptedFile = encryptDecrypt(sc, "encrypt", "");
					uploadDownload("upload", sc, encryptedFile);
					break;
				case "f":
					String downloadedFile = uploadDownload("download", sc, "");
					encryptDecrypt(sc, "decrypt", downloadedFile);
					break;
				case "g":
					uploadDownload("list", sc, "");
					break;
				case "h":
					sc.close();
					System.exit(0);
					break;
				default:
					System.out.println("Invalid option: " + option + ". Please enter again");
					break;
				}

			} catch (IOException | IntegrityException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static String encryptDecrypt(Scanner sc, String action, String fileStr)
			throws IOException, IntegrityException {
		if (fileStr.equals("")) {
			fileStr = getFilePath(sc, action);
		}
		Path inputPath = FileSystems.getDefault().getPath(fileStr);
		String output;
		if (action.equals("encrypt")) {
			Cryptographer encoder = new Cryptographer(keyPath, inputPath);
			output = getFilePath(sc, "use to store the encrypted file");
			Path outputPath = FileSystems.getDefault().getPath(output);
			encoder.doCrypto(outputPath, Cipher.ENCRYPT_MODE);
		} else {
			Cryptographer decoder = new Cryptographer(keyPath, inputPath);
			output = getFilePath(sc, "use to store the decrypted file");
			Path decryptedPath = FileSystems.getDefault().getPath(output);
			decoder.doCrypto(decryptedPath, Cipher.DECRYPT_MODE);
		}
		System.out.println("File: "+fileStr+" "+action+"ion done!");
		return output;
	}

	public static String getFilePath(Scanner sc, String action) {
		System.out.println("What is the loaction of the file that you want to " + action + "?");
		String filePath = sc.nextLine();
		return filePath;
	}

	public static void getSessionKey(Scanner sc) {
		System.out.println("Do you have a key file for this session (yes/no)?");
		String ans = sc.nextLine();
		String key_str;
		if (ans.equals("yes")) {
			key_str = getFilePath(sc, "use as the key for this seesion");
			keyPath = FileSystems.getDefault().getPath(key_str);
		} else {
			if (!ans.equals("no")) {
				System.out.println("Invalid answer. Will create a key for this session by default.");
			}
			keyPath = Paths.get(".", "key.txt");
			KeyGenerator.generateKey(keyPath);
		}
	}

	public static String uploadDownload(String action, Scanner sc, String filePath) {
		UploaderDownloader.storageConnection(StorageName, StorageKey);
		System.out.println("What do you want to name your blob?");
		UploaderDownloader.blobName = sc.nextLine();
		String outputPath = "";
		if (!action.equals("list")) {
			if (filePath.equals("")) {
				filePath = getFilePath(sc, action);
			}
			if (action.equals("upload")) {
				UploaderDownloader.fileReference = filePath;
			} else {
				outputPath = getFilePath(sc, "download to");
				UploaderDownloader.fileDestination = outputPath;
			}
		}
		UploaderDownloader.blobAction(filePath, action);
		System.out.println("File: "+filePath+" "+action+"ed!");
		return outputPath;
	}
}
