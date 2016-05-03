package cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;

public class Cryptographer {

	private static final String ALGORITHM = "AES";
	private static final String CRYPTOMODE = "AES/CBC/PKCS5Padding";
	private static final String HMACMODE = "HmacSHA256";
	private static final int IVLENGTH = 16;
	private static final int HMACLENGTH = 32;
	
	private SecretKey key;	
	byte[] bytes;
	
	public Cryptographer(Path keyPath, Path p) {
		try {
			byte[] encoded = Files.readAllBytes(keyPath);
			byte[] keyBytes = Base64.getDecoder().decode(encoded);
			key = new SecretKeySpec(keyBytes, ALGORITHM);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + keyPath + " . Is the file path correct?", e);
		}
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + " . Is the file path correct?", e);
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
	
	public void changeKey(Path newKeyPath) {
		try {
			key = new SecretKeySpec(Files.readAllBytes(newKeyPath), ALGORITHM);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + newKeyPath + " . Is the file path correct?", e);
		}
	}
	
	public void doCrypto(Path out, int mode) {
		Cipher cipher = createCipher();
		Mac mac;
		try {
			mac = Mac.getInstance(HMACMODE);
			mac.init(key);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("The HMAC mode specified " + HMACMODE + " doesn't exist! (maybe it's unsupported by this machine)", e);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("The key " + key.toString() + " is not valid!", e);
		}
		byte[] output;
		try {
			if (mode == Cipher.ENCRYPT_MODE) {
				IvParameterSpec iv = generateIV(IVLENGTH);
				cipher.init(mode, key, iv);
				byte[] ivBytes = cipher.getIV();
				byte[] message = cipher.doFinal(bytes);
				byte[] hmac = mac.doFinal(message);
				assertEquals("The IV generated has length: " + ivBytes.length + " but is required to have length: " + IVLENGTH, 
						ivBytes.length, IVLENGTH);
				assertEquals("The HMAC generated has length: " + hmac.length + " but is required to have length: " + HMACLENGTH,
						hmac.length, HMACLENGTH);
				output = new byte[ivBytes.length + message.length + hmac.length];
				System.arraycopy(ivBytes, 0, output, 0, ivBytes.length);
				System.arraycopy(message, 0, output, ivBytes.length, message.length);
				System.arraycopy(hmac, 0, output, ivBytes.length + message.length, hmac.length);
			} else if (mode == Cipher.DECRYPT_MODE) {
				byte[] ivBytes = Arrays.copyOfRange(bytes, 0, IVLENGTH);
				byte[] message = Arrays.copyOfRange(bytes, IVLENGTH, bytes.length - HMACLENGTH);
				byte[] messageHmac = Arrays.copyOfRange(bytes, bytes.length - HMACLENGTH, bytes.length);
				byte[] calcHmac = mac.doFinal(message);
				assertArrayEquals("Message HMAC is not valid!", messageHmac, calcHmac);
				cipher.init(mode, key, new IvParameterSpec(ivBytes));
				output = cipher.doFinal(message);
			} else {
				throw new IllegalArgumentException("The mode specified is not valid. Please use "
						+ "1 for encryption or 2 for decryption");
			}
			// Files.createFile(out);
			// If you don't want to overwrite the output file if it already exists,
			// add StandardOpenOption.CREATE as a parameter
			// This doesn't seem to work? 
			Files.write(out, output);
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
	
	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Path keyPath = Paths.get(".", "key.txt");
		Path inputPath = Paths.get(".", "input.txt");
		Path outputPath = Paths.get(".","output.txt");
		Cryptographer encoder = new Cryptographer(keyPath, inputPath);
		encoder.doCrypto(outputPath, Cipher.ENCRYPT_MODE);
		Cryptographer decoder = new Cryptographer(keyPath, outputPath);
		Path decryptedPath = Paths.get(".", "decrypted.txt");
		decoder.doCrypto(decryptedPath, Cipher.DECRYPT_MODE);
		System.out.println("Encryption/decryption complete!");
	}
}
