import { isDevMode } from '/@/utils/env';

// Thời gian hết hạn mặc định của cache
export const DEFAULT_CACHE_TIME = 60 * 60 * 24 * 7;

// Sau khi bật mã hóa cache, khóa mã hóa. Dùng mã hóa aes
export const cacheCipher = {
  key: '_11111000001111@',
  iv: '@11111000001111_',
};

// Có mã hóa cache không, mặc định mã hóa trong môi trường production
export const enableStorageEncryption = false;
