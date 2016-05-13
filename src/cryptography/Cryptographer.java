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

public class Cryptographer {

	private static final String ALGORITHM = "AES";
	private static final String CRYPTOMODE = "AES/CBC/PKCS5Padding";
	private static final String HMACMODE = "HmacSHA256";
	private static final int IVLENGTH = 16;
	private static final int HMACLENGTH = 32;
	
	private SecretKey key;	
	byte[] bytes;
	
	/**
	 * 
	 * @param keyPath  The path to the encryption/decryption key. Key must be stored in Base64 encoding and be the appropriate length
	 * 					for the cryptography & MAC algorithms used.
	 * @param p		The path to the data you want encrypted/decrypted. Data to encrypt should have the format {plaintext} while
	 * 				data to decrypt should have the format {IV | ciphertext | HMAC}, where the IV is IVLENGTH bytes long 
	 * 				& HMAC is HMACLENGTH bytes long
	 */
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
	
	/**
	 * Uses a SecureRandom to generate an n-byte initialization vector (for algorithms that need it)
	 * 
	 * @param length The length of IV to generate, in bytes
	 * @return An IvParameterSpec initialized with length bytes of IV 
	 */
	private IvParameterSpec generateIV(int length) {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[length];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	/**
	 * Changes the data that the Cryptographer is to encrypt/decrypt 
	 * @param p  Path to the new data
	 */
	public void changeData(Path p) {
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + ". Is the file path correct?", e);
		}
	}
	
	/**
	 * Changes the encryption/decryption key
	 * @param newKeyPath   The path to the file with the new key
	 */
	public void changeKey(Path newKeyPath) {
		try {
			key = new SecretKeySpec(Files.readAllBytes(newKeyPath), ALGORITHM);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + newKeyPath + " . Is the file path correct?", e);
		}
	}
	
	/**
	 * Encrypts/decrypts the contents of bytes, adds or checks a MAC, and writes encrypted/decrypted bytes out to a file
	 * @param out    The output path to save the encrypted/decrypted file to 
	 * @param mode   The encryption/decryption mode. 1 is to encrypt, 2 is to decrypt. 
	 * @throws IOException 
	 * @throws IntegrityException 
	 */
	public void doCrypto(Path out, int mode) throws IOException, IntegrityException {
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
				output = encrypt(cipher, mode, mac);
			} else if (mode == Cipher.DECRYPT_MODE) {
				output = decrypt(cipher, mode, mac);
			} else {
				throw new IllegalArgumentException("The mode specified is not valid. Please use "
						+ "1 for encryption or 2 for decryption");
			}
			// This overwrites any file with path out that already exists.
			// If you don't want this to happen, there are OpenOptions that
			// you can add to the call to prevent this. 
			Files.write(out, output);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("The key " + key.toString() + " is not valid!", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalArgumentException("A parameter passed into the algorithm was not valid!", e);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException("Crypto failed!", e);
		} catch (IOException e) {
			throw new IOException("Couldn't write the output file!", e);
		} catch (IntegrityException e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param cipher   The cipher to encrypt the data with 
	 * @param mode	   Should be Cipher.ENCRYPT_MODE
	 * @param mac      The Mac used to create the message's MAC 
	 * @return     	   A byte array with the format { IV | ciphertext | HMAC }
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IntegrityException
	 */
	private byte[] encrypt(Cipher cipher, int mode, Mac mac) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IntegrityException {
		IvParameterSpec iv = generateIV(IVLENGTH);
		cipher.init(mode, key, iv);
		byte[] ivBytes = cipher.getIV();
		byte[] message = cipher.doFinal(bytes);
		// Note that the MAC is calculated on the encrypted text, not the plaintext. This protects against padding oracle attacks
		byte[] hmac = mac.doFinal(message);
		if (IVLENGTH != ivBytes.length){
			throw new IntegrityException("The IV generated has length: " + ivBytes.length + " but is required to have length: " + IVLENGTH);
		}
		if (hmac.length != HMACLENGTH){
			throw new IntegrityException("The HMAC generated has length: " + hmac.length + " but is required to have length: " + HMACLENGTH);
		}
		byte[] output = new byte[ivBytes.length + message.length + hmac.length];
		System.arraycopy(ivBytes, 0, output, 0, ivBytes.length);
		System.arraycopy(message, 0, output, ivBytes.length, message.length);
		System.arraycopy(hmac, 0, output, ivBytes.length + message.length, hmac.length);
		return output;
	}
	
	/**
	 * 
	 * @param cipher   The cipher to decrypt the data with 
	 * @param mode     Should be Cipher.DECRYPT_MODE
	 * @param mac      The Mac used to check the message's MAC
	 * @return 		   A plaintext byte array corresponding to the data in bytes decrypted
	 * @throws InvalidKeyException  
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private byte[] decrypt(Cipher cipher, int mode, Mac mac) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IntegrityException {
		byte[] ivBytes = Arrays.copyOfRange(bytes, 0, IVLENGTH);
		byte[] message = Arrays.copyOfRange(bytes, IVLENGTH, bytes.length - HMACLENGTH);
		byte[] messageHmac = Arrays.copyOfRange(bytes, bytes.length - HMACLENGTH, bytes.length);
		byte[] calcHmac = mac.doFinal(message);
		if (!Arrays.equals(messageHmac, calcHmac)){
			throw new IntegrityException("Message HMAC is not valid!");
		}
		cipher.init(mode, key, new IvParameterSpec(ivBytes));
		return cipher.doFinal(message);
	}
	
	/**
	 * @return A cipher with mode CRYPTOMODE, if it exists. Throws an exception if the algorithm or padding schema requested 
	 * isn't available
	 */
	public Cipher createCipher() {
		try {
			return Cipher.getInstance(CRYPTOMODE);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("The algorithm requested is unavailable!", e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("The padding schema requested is unavailable!", e);
		}
	}
	
	/**
	 * Helper function to print a byte array as a hex string. 
	 * 
	 * @param a    The byte array to translate to a hex string
	 * @return    A string whose contents are the hex of the input byte array. 
	 */
	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		try{
			Path keyPath = Paths.get(".", "key.txt");
			Path inputPath = Paths.get(".", "input.txt");
			Path outputPath = Paths.get(".","output.txt");
			Cryptographer encoder = new Cryptographer(keyPath, inputPath);
			encoder.doCrypto(outputPath, Cipher.ENCRYPT_MODE);
			Cryptographer decoder = new Cryptographer(keyPath, outputPath);
			Path decryptedPath = Paths.get(".", "decrypted.txt");
			decoder.doCrypto(decryptedPath, Cipher.DECRYPT_MODE);
			System.out.println("Encryption/decryption complete!");
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong!", e);
		}
	}
}