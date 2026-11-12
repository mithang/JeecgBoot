// Không thể dùng trực tiếp process.env, sẽ gây lỗi
export const $ps = process;

export const isDev = !!$ps.env.VITE_DEV_SERVER_URL;

export const $env = getEnv();

function getEnv() {
  if (isDev) {
    return $ps.env;
  }
  // Môi trường không phải dev: lấy biến môi trường từ tệp JSON
  const env = require('./env.json');
  return {
    ...$ps.env,
    ...env,
  };
}
