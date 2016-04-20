package cryptography;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.nio.file.Files;

public class Cryptographer {
	
	private final String ALGORITHM = "AES";
	private final String CRYPTOMODE = "AES/CBC/PKCS5Padding";
	private Key KEY;
	
	byte[] bytes;
	
	public Cryptographer(String key, Path p) {
			KEY = new SecretKeySpec(key.getBytes(), ALGORITHM);
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + " . Is the file path correct?");
		}
	}
	
	public void changePath(Path p) {
		try {
			bytes = Files.readAllBytes(p);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read the file with path " + p + " . Is the file path correct?");
		}
	}
	
	public void changeKey(String newKey) {
		KEY = new SecretKeySpec(newKey.getBytes(), ALGORITHM);
	}
	
	public void doCrypto(Path out, int mode) {
			Cipher cipher = createCipher();
			try {
				if (mode == Cipher.ENCRYPT_MODE) {
					cipher.init(Cipher.ENCRYPT_MODE, KEY);
				} else if (mode == Cipher.DECRYPT_MODE) {
					cipher.init(Cipher.DECRYPT_MODE, KEY);
				} else {
					throw new IllegalArgumentException("The mode specified is not valid");
				}
			} catch (InvalidKeyException e) {
				throw new RuntimeException("The key " + KEY + "is not valid! Check that it's the correct length");
			}
			try {
				byte[] output = cipher.doFinal(bytes);
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
}
