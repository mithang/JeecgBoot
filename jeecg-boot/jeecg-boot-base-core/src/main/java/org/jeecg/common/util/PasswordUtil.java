package org.jeecg.common.util;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @Description: Lớp công cụ mật khẩu
 * @author: jeecg-boot
 */
public class PasswordUtil {

	/**
	 * JAVA6 Hỗ trợ bất kỳ thuật toán nào sau đây PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
	 * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
	 * */

    /**
     * Thuật toán được định nghĩa sử dụng là:PBEWITHMD5andDES Thuật toán
     * Thuật toán mã hóa
     */
	public static final String ALGORITHM = "PBEWithMD5AndDES";

    /**
     * Thuật toán được định nghĩa sử dụng là:PBEWITHMD5andDES Thuật toán
     * Khóa
     */
	public static final String SALT = "63293188";

	/**
	 * Định nghĩa số lần lặp là 1000 lần
	 */
	private static final int ITERATIONCOUNT = 1000;

	/**
	 * Lấy giá trị salt được sử dụng trong thuật toán mã hóa,Giá trị salt dùng khi giải mã phải giống với giá trị salt dùng khi mã hóa thì mới hoàn tất được thao tác. Độ dài salt phải là 8 byte
	 * 
	 * @return byte[] Giá trị salt
	 * */
	public static byte[] getSalt() throws Exception {
		// Khởi tạo số ngẫu nhiên an toàn
		SecureRandom random = new SecureRandom();
		// Tạo salt
		return random.generateSeed(8);
	}

	public static byte[] getStaticSalt() {
		// Tạo salt
		return SALT.getBytes();
	}

	/**
	 * Tạo một khóa dựa trên mật khẩu PBE
	 * 
	 * @param password
	 *            Mật khẩu được sử dụng khi tạo khóa
	 * @return Key PBE Khóa thuật toán
	 * */
	private static Key getPbeKey(String password) {
		// Khởi tạo thuật toán được sử dụng
		SecretKeyFactory keyFactory;
		SecretKey secretKey = null;
		try {
			keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			// Thiết lập tham số khóa PBE
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
			// Tạo khóa
			secretKey = keyFactory.generateSecret(keySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return secretKey;
	}

	/**
	 * Mã hóa chuỗi văn bản gốc
	 * 
	 * @param plaintext
	 *            Chuỗi văn bản gốc cần mã hóa
	 * @param password
	 *            Mật khẩu được sử dụng khi tạo khóa
	 * @param salt
	 *            Giá trị salt
	 * @return Chuỗi mật mã sau khi mã hóa
	 * @throws Exception
	 */
	public static String encrypt(String plaintext, String password, String salt) {

		Key key = getPbeKey(password);
		byte[] encipheredData = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
			// Giải thích logic mã: Khi tên người dùng là tiếng Trung, mật khẩu đã mã hóa trên Windows và Linux sẽ cho kết quả khác nhau gitee/issues/IZUD7
			encipheredData = cipher.doFinal(plaintext.getBytes("utf-8"));
		} catch (Exception e) {
		}
		return bytesToHexString(encipheredData);
	}

	/**
	 * Giải mã chuỗi mật mã
	 * 
	 * @param ciphertext
	 *            Chuỗi mật mã cần giải mã
	 * @param password
	 *            Mật khẩu được sử dụng khi tạo khóa(Nếu cần giải mã,Tham số này cần nhất quán với tham số đã dùng khi mã hóa)
	 * @param salt
	 *            Giá trị salt(Nếu cần giải mã,Tham số này cần nhất quán với tham số đã dùng khi mã hóa)
	 * @return Chuỗi văn bản gốc sau khi giải mã
	 * @throws Exception
	 */
	public static String decrypt(String ciphertext, String password, String salt) {

		Key key = getPbeKey(password);
		byte[] passDec = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

			passDec = cipher.doFinal(hexStringToBytes(ciphertext));
		}

		catch (Exception e) {
			// TODO: handle exception
		}
		return new String(passDec);
	}

	/**
	 * Chuyển đổi mảng byte thành chuỗi thập lục phân
	 * 
	 * @param src
	 *            Mảng byte
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Chuyển đổi chuỗi thập lục phân thành mảng byte
	 * 
	 * @param hexString
	 *            Chuỗi thập lục phân
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || "".equals(hexString)) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}


}