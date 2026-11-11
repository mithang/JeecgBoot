import { encrypt, decrypt } from 'crypto-js/aes';
import { parse } from 'crypto-js/enc-utf8';
import pkcs7 from 'crypto-js/pad-pkcs7';
import ECB from 'crypto-js/mode-ecb';
import md5 from 'crypto-js/md5';
import UTF8 from 'crypto-js/enc-utf8';
import Base64 from 'crypto-js/enc-base64';
import CryptoJS from 'crypto-js';

export interface EncryptionParams {
  key: string;
  iv: string;
}

export class AesEncryption {
  private key;
  private iv;

  constructor(opt: Partial<EncryptionParams> = {}) {
    const { key, iv } = opt;
    if (key) {
      this.key = parse(key);
    }
    if (iv) {
      this.iv = parse(iv);
    }
  }

  get getOptions() {
    return {
      mode: ECB,
      padding: pkcs7,
      iv: this.iv,
    };
  }

  encryptByAES(cipherText: string) {
    return encrypt(cipherText, this.key, this.getOptions).toString();
  }

  decryptByAES(cipherText: string) {
    return decrypt(cipherText, this.key, this.getOptions).toString(UTF8);
  }
}

export function encryptByBase64(cipherText: string) {
  return UTF8.parse(cipherText).toString(Base64);
}

export function decodeByBase64(cipherText: string) {
  return Base64.parse(cipherText).toString(UTF8);
}

export function encryptByMd5(password: string) {
  return md5(password).toString();
}

// ================== Liên quan đến mã hóa mật khẩu ==================
// Mã hóa mật khẩu thống nhất dùng AES CBC Chế độ, frontend và backend key và iv Phải giữ nhất quán
// AES_KEY và AES_IV Cần hoàn toàn nhất quán với cấu hình backend, nếu không mã hóa/Giải mã sẽ thất bại
// ================== Liên quan đến mã hóa mật khẩu ===========BEGIN=======

// AESMã hóakeyvàivHằng số
export const AES_KEY = '1234567890adbcde';
export const AES_IV = '1234567890hjlkew';

/**
 * AES CBC Mã hóa, dùng hằng số toàn cục AES_KEY và AES_IV
 * @param plainText Văn bản rõ
 * @returns Văn bản mã hóa sau khi mã hóa
 */
export function encryptAESCBC(plainText: string): string {
  const key = parse(AES_KEY);
  const iv = parse(AES_IV);
  return encrypt(plainText, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: pkcs7
  }).toString();
}
// ================== Liên quan đến mã hóa mật khẩu =============END=====
