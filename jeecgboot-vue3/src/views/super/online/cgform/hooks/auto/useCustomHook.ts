import * as vue from 'vue';
import * as UTIL_CACHE from '/@/utils/cache';
import * as UTIL_AXIOS from '/@/utils/http/axios';
import * as HOOK_MESSAGE from '/@/hooks/web/useMessage';
import { randomString } from '/@/utils/common/compUtils';
import * as HOOK_USERINFO from '/@/store/modules/user';
import * as UTIL_AUTH from "/@/utils/auth";

// Định nghĩa tại đâyJSNội dung có thể sử dụng trong phần tăng cường
const $exports = {
  vue,
  '@': {
    hooks: {
      // Ví dụ gọi:@/hooks/useMessage
      useMessage: HOOK_MESSAGE,
      useUserStore: HOOK_USERINFO
    },
    utils: {
      // Ví dụ gọi:@/utils/axios
      axios: UTIL_AXIOS,
      cache: UTIL_CACHE,
      auth: UTIL_AUTH,
    },
  },
};

/**
 * Dùng để xử lýjstùy chỉnh trong phần tăng cườnghookMã (code)
 * Phương thức định nghĩa phần tăng cường:useCustomHook(), khuyến nghị không sử dụng dấu ngoặc kép
 * Đối tượng xuất khác
 * @param otherExports
 */
export function useCustomHook(otherExports?: Recordable, context?: any) {
  const assignExports = Object.assign({}, $exports, otherExports);

  /**
   * Tùy chỉnh import Phương thức
   * @param path Đường dẫn tham chiếu
   */
  function doImport(path: string) {
    if (path != null && path != '') {
      let paths = path.toString().split('/');
      let result = assignExports[paths[0]];
      for (let i = 1; i < paths.length; i++) {
        result = result[paths[i]];
      }
      return result;
    }
    return null;
  }

  function doExport() {}

  /**
   * Thực thiJSMã tăng cường
   * @param code Mã cần thực thi
   */
  function executeJsEnhanced(code: string, row?) {
    // Để tránh xung đột tên phương thức, vì vậy sử dụng tên phương thức ngẫu nhiên
    let randomKey = randomString(6);
    // let importKey = '__import_' + randomKey
    //let importKey = 'customImport'
    let exportKey = '__export_' + randomKey;
    // Thay thế import Từ khóa
    //code = replaceImportKey(code, importKey)
    
    //update-begin-author:taoyan date:2023-5-15 for: issues/516 Nút tùy chỉnh_hooktham số sau đórowVấn đề chưa xác định (xem#410） #516
    if(row){
      const executeCode = `return function (row, customImport, ${exportKey}) {"use strict"; ${code}}`;
      console.group('executeJsEnhanced');
      console.log(executeCode);
      console.groupEnd();
      const fun = new Function(executeCode)();
      fun.call(context, row, doImport, doExport);
    }else{
      const executeCode = `return function (customImport, ${exportKey}) {"use strict"; ${code}}`;
      console.group('executeJsEnhanced');
      console.log(executeCode);
      console.groupEnd();
      const fun = new Function(executeCode)();
      fun.call(context, doImport, doExport);
    }
    //update-end-author:taoyan date:2023-5-15 for: issues/516 Nút tùy chỉnh_hooktham số sau đórowVấn đề chưa xác định (xem#410） #516
 
  }

  /**
   * Thay thế import Từ khóa
   * @param code
   * @param fnKey import của phương thứckey
   */
  /*  function replaceImportKey(code: string, fnKey: string) {
    let lines = code.split('\n')
    for (let i = 0; i < lines.length; i++) {
      let line = lines[i].trim()
      if (line.startsWith('import ')) {
        let regexp = /import (.*) from (.*)/g
        lines[i] = line.replace(regexp, `const $1 = ${fnKey}($2)`)
      }
    }
    return lines.join('\n')
  }*/

  return {
    executeJsEnhanced,
  };
}

/**Lấy nội dung thân hàm làm chuỗi*/
export const GET_FUN_BODY_REG = /(?:\/\*[\s\S]*?\*\/|\/\/.*?\r?\n|[^{])+\{([\s\S]*)\}$/;
