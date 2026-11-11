package org.jeecg.modules.system.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * @Description: Mã hóa và giải mã mật khẩu
 * @author: lsq
 * @date: 2020 năm 09 tháng 07 ngày 14:26
 */
public class SecurityUtil {
    /**Mã hóa key*/
    private static String key = "JEECGBOOT1423670";

    //---AES Mã hóa---------begin---------
    /**Mã hóa
     * @param content
     * @return
     */
    public static String jiami(String content) {
            SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
            String encryptResultStr = aes.encryptHex(content);
            return encryptResultStr;
    }

    /**Giải mã
     * @param encryptResultStr
     * @return
     */
    public static String jiemi(String encryptResultStr){
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
        //Giải mã thành chuỗi
        String decryptResult = aes.decryptStr(encryptResultStr, CharsetUtil.CHARSET_UTF_8);
        return  decryptResult;
    }
    //---AES Mã hóa---------end---------
    /**
     * Hàm chính
     */
    public static void main(String[] args) {
        String content="test1111";
        String encrypt = jiami(content);
        System.out.println(encrypt);
        //Xây dựng
        String decrypt = jiemi(encrypt);
        //Giải mã thành chuỗi
        System.out.println(decrypt);
    }
}
