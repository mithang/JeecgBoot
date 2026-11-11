package org.jeecg.common.util.encryption;


import lombok.Data;

/**
 * @Description: EncryptedString
 * @author: jeecg-boot
 */
@Data
public class  EncryptedString {

    /**
     * Độ dài là 16 ký tự
     */
    public static  String key = "1234567890adbcde";

    /**
     * Độ dài là 16 ký tự
     */
    public static  String iv  = "1234567890hjlkew";
}
