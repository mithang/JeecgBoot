import { watch } from 'vue';
import { isFunction, isPromise, isArray } from '/@/utils/is';
import { JVxeColumn, JVxeDataProps, JVxeTableProps, JVxeLinkageConfig } from '../types';

/**
 * Liên động nhiều cấp
 */
export function useLinkage(props: JVxeTableProps, data: JVxeDataProps, methods) {
  // Sắp xếp cấu hình liên động nhiều cấp
  watch(
    () => props.linkageConfig,
    (linkageConfig: JVxeLinkageConfig[]) => {
      data.innerLinkageConfig.clear();
      if (isArray(linkageConfig) && linkageConfig.length > 0) {
        linkageConfig.forEach((config) => {
          let keys = getLinkageKeys(config.key, []);
          // Nhiều key dùng chung một địa chỉ tham chiếu
          let configItem = {
            ...config,
            keys,
            optionsMap: new Map(),
          };
          keys.forEach((k) => data.innerLinkageConfig.set(k, configItem));
        });
      }
    },
    { immediate: true }
  );

  // Lấy thứ tự key liên động
  function getLinkageKeys(key: string, keys: string[]): string[] {
    let col = props.columns?.find((col: JVxeColumn) => col.key === key) as JVxeColumn;
    if (col) {
      keys.push(col.key);
      // Tìm cấp dưới
      if (col.linkageKey) {
        return getLinkageKeys(col.linkageKey, keys);
      }
    }
    return keys;
  }

  // Xử lý dữ liệu hiển thị lại liên động
  function handleLinkageBackData(row) {
    if (data.innerLinkageConfig.size > 0) {
      for (let configItem of data.innerLinkageConfig.values()) {
        autoSetLinkageOptionsByData(row, '', configItem, 0);
      }
    }
  }

  /** [Liên động nhiều cấp] Lấy tùy chọn dropdown liên động cùng cấp */
  function getLinkageOptionsSibling(row, col, config, request) {
    // Nếu cột hiện tại không phải là cột cấp cao nhất
    let key = '';
    if (col.key !== config.key) {
      // Sẽ tìm ra cột cấp trên liên động
      let idx = config.keys.findIndex((k) => col.key === k);
      let parentKey = config.keys[idx - 1];
      key = row[parentKey];
      // Nếu cột cấp trên liên động chưa chọn dữ liệu thì trả về trực tiếp mảng rỗng
      if (key === '' || key == null) {
        return [];
      }
    } else {
      key = 'root';
    }
    let options = config.optionsMap.get(key);
    if (!Array.isArray(options)) {
      if (request) {
        let parent = key === 'root' ? '' : key;
        return getLinkageOptionsAsync(config, parent);
      } else {
        options = [];
      }
    }
    return options;
  }

  /** [Liên động nhiều cấp] Lấy tùy chọn dropdown liên động (bất đồng bộ) */
  function getLinkageOptionsAsync(config, parent) {
    return new Promise((resolve) => {
      let key = parent ? parent : 'root';
      let options;
      if (config.optionsMap.has(key)) {
        options = config.optionsMap.get(key);
        if (isPromise(options)) {
          options.then((opt) => {
            config.optionsMap.set(key, opt);
            resolve(opt);
          });
        } else {
          resolve(options);
        }
      } else if (isFunction(config.requestData)) {
        // Gọi phương thức requestData, lấy cấp con bằng cách truyền vào parent
        // noinspection JSVoidFunctionReturnValueUsed,TypeScriptValidateJSTypes
        let promise = config.requestData(parent);
        config.optionsMap.set(key, promise);
        promise.then((opt) => {
          config.optionsMap.set(key, opt);
          resolve(opt);
        });
      } else {
        resolve([]);
      }
    });
  }

  // [Liên động nhiều cấp] Dùng để hiển thị lại dữ liệu, tự động điền optionsMap
  function autoSetLinkageOptionsByData(data, parent, config, level) {
    if (level === 0) {
      getLinkageOptionsAsync(config, '');
    } else {
      getLinkageOptionsAsync(config, parent);
    }
    if (config.keys.length - 1 > level) {
      let value = data[config.keys[level]];
      if (value) {
        autoSetLinkageOptionsByData(data, value, config, level + 1);
      }
    }
  }

  // [Liên động nhiều cấp] Khi component liên động change, xóa component cấp dưới
  function handleLinkageSelectChange(row, col, config, value) {
    if (col.linkageKey) {
      getLinkageOptionsAsync(config, value);
      let idx = config.keys.findIndex((k) => k === col.key);
      let values = {};
      for (let i = idx; i < config.keys.length; i++) {
        values[config.keys[i]] = '';
      }
      // Xóa dữ liệu của các cột phía sau
      methods.setValues([{ rowKey: row.id, values }]);
    }
  }

  return {
    getLinkageOptionsAsync,
    getLinkageOptionsSibling,
    handleLinkageSelectChange,
    handleLinkageBackData,
  };
}
