package org.jeecg.common.util.encryption;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.lang.codec.Base64;
import org.jeecg.common.util.oConvertUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * AES Công cụ (Tương thích với lịch sử NoPadding + Mới PKCS5Padding)
 */
@Slf4j
public class AesEncryptUtil {

    private static final String KEY = EncryptedString.key;
    private static final String IV  = EncryptedString.iv;

    /* -------- Phiên bản mới:CBC + PKCS5Padding (Tương thích với CryptoJS Pkcs7 của frontend) -------- */
    private static String decryptPkcs5(String cipherBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec ks = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, ks, ivSpec);
        byte[] plain = cipher.doFinal(Base64.decode(cipherBase64));
        return new String(plain, StandardCharsets.UTF_8);
    }

    /* -------- Phiên bản cũ:CBC + NoPadding (Bổ sung thủ công 0) -------- */
    private static String decryptLegacyNoPadding(String cipherBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec ks = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, ks, ivSpec);
        byte[] data = cipher.doFinal(Base64.decode(cipherBase64));
        return new String(data, StandardCharsets.UTF_8)
                .replace("\u0000",""); // Phần đệm cũ 0
    }

    /* -------- Điểm vào tương thích: dùng cho đăng nhập -------- */
    public static String resolvePassword(String input){
        if(oConvertUtils.isEmpty(input)){
            return input;
        }
        // 1. 1. Thử phiên bản mới trước
        try{
            String p = decryptPkcs5(input);
            return clean(p);
        }catch(Exception ignore){
            //log.debug("【Giải mã AES】Password not AES PKCS5 cipher, try legacy.");
        }

        // 2. 2. Quay lại phiên bản cũ
        try{
            String legacy = decryptLegacyNoPadding(input);
            return clean(legacy);
        }catch(Exception e){
            log.debug("【Giải mã AES】Password not AES cipher, raw used.");
        }

        // 3. 3. Xem như văn bản gốc
        return input;
    }

    /* -------- Tùy chọn: dọn dẹp thống nhất các ký tự điều khiển vô hình ở cuối -------- */
    private static String clean(String s){
        if(s==null) return null;
        // Loại bỏ ký tự điều khiển ở cuối/khoảng trắng (không ảnh hưởng đến khoảng trắng hợp lệ ở giữa)
        return s.replaceAll("[\\p{Cntrl}]+","").trim();
    }

    /* -------- Nếu vẫn cần API cũ, có thể giữ lại (Không khuyến nghị dùng cho frontend mới nữa) -------- */
    @Deprecated
    public static String desEncrypt(String data) throws Exception {
        return decryptLegacyNoPadding(data);
    }

    /* Mã hóa (nếu frontend không còn sử dụng, có thể bỏ qua; giữ lại triển khai cũ để tránh ảnh hưởng đến lịch sử) */
    @Deprecated
    public static String encrypt(String data) throws Exception {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength += (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return Base64.encodeToString(encrypted);
        }catch(Exception e){
            throw new IllegalStateException("legacy encrypt error", e);
        }
    }

//    public static void main(String[] args) throws Exception {
//        // Frontend CBC/Pkcs7 Kiểm tra mật mã
//        String frontCipher = encrypt("sa"); // Chỉ kiểm chứng xem pipeline có khả dụng hay không (cách cũ)
//        System.out.println(resolvePassword(frontCipher));
//    }
}