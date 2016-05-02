package cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
	
	private static final int KEYLENGTH = 32;

	public static void generateKey(Path out) {
		SecureRandom rand = new SecureRandom();
		byte[] key = new byte[KEYLENGTH];
		rand.nextBytes(key);
		byte[] encoded = Base64.getEncoder().encode(key);
		try {
			Files.write(out, encoded);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write to the path: " + out.toString() + "!", e);
		}
	}
	
	public static void main(String[] args) {
		Path keyPath = Paths.get(".", "key.txt");
		generateKey(keyPath);
		System.out.println("Key generated!");
	}
}
