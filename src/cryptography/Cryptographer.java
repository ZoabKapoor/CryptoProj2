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
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptographer {
	
	private static final String ALGORITHM = "AES";
	// Add authentication with HMAC? Stretch goal.
	private static final String CRYPTOMODE = "AES/CBC/PKCS5Padding";
	private static final int IVLENGTH = 16;
	
	private SecretKey key;	
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
	
	private IvParameterSpec generateIV(int lengthInBits) {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[lengthInBits];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
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
	
	public static String byteArrayToHex(byte[] a) {
		   StringBuilder sb = new StringBuilder(a.length * 2);
		   for(byte b: a)
		      sb.append(String.format("%02x", b & 0xff));
		   return sb.toString();
		}
	
	public void doCrypto(Path out, int mode) {
		Cipher cipher = createCipher();
		byte[] output;
		try {
			if (mode == Cipher.ENCRYPT_MODE) {
				IvParameterSpec iv = generateIV(IVLENGTH);
				cipher.init(mode, key, iv);
				byte[] ivBytes = cipher.getIV();
				byte[] message = cipher.doFinal(bytes);
				output = new byte[ivBytes.length + message.length];
				System.arraycopy(ivBytes, 0, output, 0, ivBytes.length);
				System.arraycopy(message, 0, output, ivBytes.length, message.length);
			} else if (mode == Cipher.DECRYPT_MODE) {
				byte[] ivBytes = Arrays.copyOfRange(bytes, 0, IVLENGTH);
				byte[] message = Arrays.copyOfRange(bytes, IVLENGTH,bytes.length);
				cipher.init(mode, key, new IvParameterSpec(ivBytes));
				output = cipher.doFinal(message);
			} else {
				throw new IllegalArgumentException("The mode specified is not valid. Please use "
						+ "1 for encryption or 2 for decryption");
			}
			Files.createFile(out);
			Files.write(out, output, StandardOpenOption.WRITE);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("The key " + key.toString() + " is not valid!", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalArgumentException("A parameter passed into the algorithm was not valid!", e);
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
		Cryptographer decoder = new Cryptographer(keystr, outputPath);
		Path decryptedPath = Paths.get(".", "decrypted.txt");
		decoder.doCrypto(decryptedPath, Cipher.DECRYPT_MODE);
		System.out.println("Done!");
	}
}
