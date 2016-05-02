package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import cryptography.Cryptographer;
import cryptography.KeyGenerator;
// create a random key and share the key offline, give the user four options
// separate the four steps, a) upload, b) download, c) encrypt, d) decrypt
public class Main {

	public static void main(String[] args) {
		
//		try {
//			int limit = Cipher.getMaxAllowedKeyLength("RC5");
//			System.out.println(limit);
//			System.out.println(Integer.MAX_VALUE);
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		Path inputPath = Paths.get(".", "input.txt");
		Path outputPath = Paths.get(".","output.txt");
		Path keyPath = Paths.get(".", "key.txt");
		byte[] encoded;
		byte[] keyBytes;
		try {
			encoded = Files.readAllBytes(keyPath);
			keyBytes = Base64.getDecoder().decode(encoded);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//key = new SecretKeySpec(keyBytes, ALGORITHM);
		Cryptographer encoder = new Cryptographer(keyPath, inputPath);
		
		encoder.doCrypto(outputPath, Cipher.ENCRYPT_MODE);
		Cryptographer decoder = new Cryptographer(keyPath, outputPath);
		Path decryptedPath = Paths.get(".", "decrypted.txt");
		decoder.doCrypto(decryptedPath, Cipher.DECRYPT_MODE);
		System.out.println("Done!");
		
//		Path keyPath = Paths.get(".", "key.txt");
//		KeyGenerator.generateKey(keyPath);
//		System.out.println("Done!");
	}
}
