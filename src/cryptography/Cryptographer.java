package cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptographer {
	
	private final String ALGORITHM = "DES";
	private final String CRYPTOMODE = "DES/CBC/PKCS5Padding";
	// TODO - Change crypto mode to more secure mode, figure out how to construct key for AES
//	private final int KEYLENGTH = 256;
//	private final int ITERATIONS = 65536;
	private SecretKey key;
//	private final byte[] SALT = "0".getBytes();
	
	// static SecureRandom rnd = new SecureRandom();

	// 8 is a magic number - fix
	static IvParameterSpec iv = new IvParameterSpec(new byte[8]);
	
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
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			KeySpec spec = new DESKeySpec(keyString.getBytes());
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "DES");
			return secret;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
			throw new RuntimeException("Key could not be generated!", e);
		}
	}
	
	public void changePath(Path p) {
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + " . Is the file path correct?", e);
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
					throw new IllegalArgumentException("The mode specified is not valid");
				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
				throw new RuntimeException("The key " + key + " is not valid!", e);
			}
			try {
				byte[] output = cipher.doFinal(bytes);
				Files.createFile(out);
				Files.write(out, output, StandardOpenOption.WRITE);
			} catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
				throw new RuntimeException("Crypto failed!", e);
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
		Path inputPath;
		Path outputPath;
		inputPath = Paths.get(".", "output.txt");
		outputPath = Paths.get(".","decrypted.txt");
		Cryptographer encoder = new Cryptographer(keystr, inputPath);
		encoder.doCrypto(outputPath, Cipher.DECRYPT_MODE);
		System.out.println("Done!");
	}
}
