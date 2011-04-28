package uk.ac.open.kmi.iserve.sal.rest.auth.oauth;

import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import uk.ac.open.kmi.iserve.sal.util.MD5;

public class KeyGenerator {

	private final static String[] CHARACTERS = { "a", "b", "c", "d", "e", "f",
			"g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
			"t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9" };

	public static String[] generateRequestToken(String consumer_key) throws NoSuchAlgorithmException {
		String[] result = new String[2];
		// generate oauth_token and oauth_secret
		// generate token and secret based on consumer_key
		// for now use md5 of name + current time as token
		String token_data = consumer_key + System.nanoTime();
		String token = MD5.digest(token_data).toLowerCase();
		result[0] = token;

		// for now use md5 of name + current time + token as secret
		String secret_data = consumer_key + System.nanoTime() + token;
		String secret = MD5.digest(secret_data).toLowerCase();
		result[1] = secret;
		return result;
	}

	public static String[] generateAccessToken(String consumer_key) throws NoSuchAlgorithmException {
		String[] result = new String[2];
		// generate oauth_token and oauth_secret
		// generate token and secret based on consumer_key
		// for now use md5 of name + current time as token
		String token_data = consumer_key + System.nanoTime();
		String token = MD5.digest(token_data).toLowerCase();
		result[0] = token;

		// for now use md5 of name + current time + token as secret
		String secret_data = consumer_key + System.nanoTime() + token;
		String secret = MD5.digest(secret_data).toLowerCase();
		result[1] = secret;
		return result;
	}

	public static String generateApiKey() throws NoSuchAlgorithmException {
		String uuid =  UUID.randomUUID().toString();
		return MD5.digest(uuid).toLowerCase();
	}

	public static String generateApiSecret() throws NoSuchAlgorithmException {
		return generateKey(16);
	}

	/**
	 * Given a <code>int</code> num, generates and returns a random
	 * <code>String</code> key the length of num.
	 * 
	 * @param num
	 * @return String - key
	 */
	private static String generateKey(int num) {
		Random random = new Random();
		String key = "";

		for (int x = 0; x < num; x++) {
			key += CHARACTERS[random.nextInt(CHARACTERS.length)];
		}
		return key;
	}
//
//	public static void main(String[] args) throws NoSuchAlgorithmException {
//		String consumer_key = generateApiKey();
//		System.out.println("consumer_key: " + consumer_key);
//		String consumer_secret = generateApiSecret();
//		System.out.println("consumer_secret: " + consumer_secret);
//
//		String[] token_secret = generateRequestToken(consumer_key);
//		System.out.println(token_secret[0] + " : " + token_secret[1]);
//		String access_token = generateAccessToken(consumer_key);
//		System.out.println(access_token);
//	}

}
