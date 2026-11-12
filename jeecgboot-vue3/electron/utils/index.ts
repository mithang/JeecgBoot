import fs from 'fs';
import path from 'path'
import {shell, dialog} from 'electron'
import {_PATHS} from "../paths";
import {isDev} from "../env";

// Mở liên kết bằng trình duyệt
export function openInBrowser(url: string) {
  return shell.openExternal(url);
}


export function getAppInfo(): any {
  try {
    const yamlPath = isDev ? path.join(_PATHS.publicRoot, '../electron-builder.yaml') : path.join(_PATHS.electronRoot, 'env.yaml');
    const yamlContent = fs.readFileSync(yamlPath, 'utf-8');
    // Khớp appId và productName bằng biểu thức chính quy
    const appIdMatch = yamlContent.match(/appId:\s*['"]([^'"]+)['"]/);
    const productNameMatch = yamlContent.match(/productName:\s*['"]([^'"]+)['"]/);
    const appId = appIdMatch ? appIdMatch[1] : '';
    const productName = productNameMatch ? productNameMatch[1] : '';
    return {appId, productName}
  } catch (e) {
    dialog.showMessageBoxSync(null, {
      type: 'error',
      title: 'Lỗi',
      message: 'Khởi động ứng dụng thất bại, vui lòng tải gói cài đặt phiên bản mới nhất từ trang web chính thức rồi cài đặt lại!',
    });
    process.exit(-1);
  }
}
