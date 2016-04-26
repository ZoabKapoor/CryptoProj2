package cryptography;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptographer {
	
	private final String ALGORITHM = "AES";
	// Add authentication with HMAC? Stretch goal.
	private final String CRYPTOMODE = "AES/CBC/PKCS5Padding";
	private SecretKey key;
	
	// 16 is a magic number & IV is 0 - neither good
	static IvParameterSpec iv = new IvParameterSpec(new byte[16]);
	
	byte[] bytes;
	
	public Cryptographer(String keyString, Path p) {
		key = generateKey(keyString);
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + " . Is the file path correct?", e);
		}
	}
	
	public SecretKey generateKey(String keyString) {
		try {		
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
			keyBytes = Arrays.copyOf(keyBytes, 16);
			// Have to trim the key to 16 bytes to use in AES
			SecretKey secret = new SecretKeySpec(keyBytes, ALGORITHM);
			return secret;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Key could not be generated!", e);
		}
	}
	
	public void changePath(Path p) {
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + ". Is the file path correct?", e);
		}
	}
	
	public void changeKey(String newKeyString) {
		key = generateKey(newKeyString);
	}
	
	public void doCrypto(Path out, int mode) {
			Cipher cipher = createCipher();
			try {
				if (mode == Cipher.ENCRYPT_MODE) {
					cipher.init(Cipher.ENCRYPT_MODE, key, iv);
				} else if (mode == Cipher.DECRYPT_MODE) {
					cipher.init(Cipher.DECRYPT_MODE, key, iv);
				} else {
					throw new IllegalArgumentException("The mode specified is not valid. Please use "
							+ "1 for encryption or 2 for decryption");
				}
			} catch (InvalidKeyException e) {
				throw new IllegalArgumentException("The key " + key.toString() + " is not valid!", e);
			} catch (InvalidAlgorithmParameterException e) {
				throw new IllegalArgumentException("A parameter passed into the algorithm was not valid!", e);
			}
			try {
				byte[] output = cipher.doFinal(bytes);
				Files.createFile(out);
				Files.write(out, output, StandardOpenOption.WRITE);
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new RuntimeException("Crypto failed!", e);
			} catch (IOException e) {
				throw new RuntimeException("Couldn't write the output file!", e);
			}
	}
	
	public Cipher createCipher() {
		try {
			return Cipher.getInstance(CRYPTOMODE);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("The algorithm requested does not exist!", e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("The padding schema requested is unavailable!", e);
		}
	}
	
	public static void main(String[] args) {
		String keystr = "password";
		Path inputPath = Paths.get(".", "input.txt");
		Path outputPath = Paths.get(".","output.txt");
		Cryptographer encoder = new Cryptographer(keystr, inputPath);
		encoder.doCrypto(outputPath, Cipher.ENCRYPT_MODE);
		System.out.println("Done!");
		try {
			int limit = Cipher.getMaxAllowedKeyLength("RC5");
			System.out.println(limit);
			System.out.println(Integer.MAX_VALUE);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
