import { defHttp } from '/@/utils/http/axios';
import { ref, unref } from 'vue';
import { VALIDATE_FAILED, validateFormModelAndTables } from '/@/utils/common/vxeUtils';

export function useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm?) {
  const formRef = ref();
  /** Truy vấn một cái nào đótabdữ liệu */
  function requestSubTableData(url, params, tab, success) {
    tab.loading = true;
    defHttp
      .get({ url, params }, { isTransformResponse: false })
      .then((res) => {
        let { result } = res;
        if (res.success && result) {
          if (Array.isArray(result)) {
            tab.dataSource = result;
          } else if (Array.isArray(result.records)) {
            tab.dataSource = result.records;
          }
        }
        typeof success === 'function' ? success(res) : '';
      })
      .finally(() => {
        tab.loading = false;
      });
  }

  /* --- handle Sự kiện --- */

  /** ATab Sự kiện chuyển tab */
  function handleChangeTabs(key) {
    // Tự động đặt lạiscrollTopTrạng thái, tránh xuất hiện trang trắng
    tableRefs[key]?.value?.resetScrollTop(0);
  }

  /** Lấy tất cảeditableTableInstance*/
  function getAllTable() {
    let values = Object.values(tableRefs);
    return Promise.all(values);
  }
  /** Sự kiện nhấp nút xác nhận */
  function handleSubmit() {
    /** Kích hoạt kiểm tra biểu mẫu */
    getAllTable()
      .then((tables) => {
        let values = formRef.value.getFieldsValue();
        return validateFormModelAndTables(formRef.value.validate, values, tables, formRef.value.getProps, false);
      })
      .then((allValues) => {
        /** Kiểm tra một lần tất cả bảng con một-một */
        return validateSubForm && typeof validateSubForm === 'function' ? validateSubForm(allValues) : validateAllSubOne(allValues);
      })
      .then((allValues) => {
        if (typeof classifyIntoFormData !== 'function') {
          throw throwNotFunction('classifyIntoFormData');
        }
        let formData = classifyIntoFormData(allValues);
        // Gửi yêu cầu
        return requestAddOrEdit(formData);
      })
      .catch((e) => {
        if (e.error === VALIDATE_FAILED) {
          // Nếu có bảng con chưa qua kiểm tra biểu mẫu, sẽ tự động chuyển đến chỗ của nótab
          // Giải thích logic mã: VUEN-2866[Tạo mã]TabPhong cách Khi kiểm tra bảng con một-nhiều không đạt, nhấp gửi biểu mẫu thì trống, trang đính kèm quy trình cũng gặp vấn đề này
          if(e.paneKey){
            activeKey.value = e.paneKey
          }else{
            // Giải thích logic mã: TV360X-478 Một-nhiềutab, khi kiểm tra không đạt,tabKhông chuyển
            activeKey.value = e.subIndex == null ? (e.index == null ? unref(activeKey) : refKeys.value[e.index]) : Object.keys(tableRefs)[e.subIndex];
          }
          // Giải thích logic mã: 【TV360X-1064]Các mục kiểm tra cuộn khi submit biểu mẫu không phải native không đạt---
          if (e?.errorFields) {
            const firstField = e.errorFields[0];
            if (firstField) {
              formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'end' });
            }
          }
          return Promise.reject(e?.errorFields);
        } else {
          console.error(e);
        }
      });
  }
  //Kiểm tra tất cả biểu mẫu bảng con
  function validateAllSubOne(allValues) {
    return new Promise((resolve) => {
      resolve(allValues);
    });
  }
  /* --- throw --- */

  /** not a function */
  function throwNotFunction(name) {
    return `${name} Chưa định nghĩa hoặc không phải là một hàm`;
  }

  /** not a array */
  function throwNotArray(name) {
    return `${name} Chưa định nghĩa hoặc không phải là một mảng`;
  }
  return [handleChangeTabs, handleSubmit, requestSubTableData, formRef];
}

/**
 * Kiểm tra nhiều biểu mẫu và bảng contable, dùng cho nativeantd-vuebiểu mẫu
 * @param activeKey Biểu mẫu bảng con/vxe-table nơi ởtabscủa activeKey
 * @param refMap Biểu mẫu bảng con/vxe-tabletương ứngrefĐối tượng mapCấu trúc
 * Ví dụ:
 * useValidateAntFormAndTable(activeKey, {
 *   'tableA': tableARef,
 *   'formB': formBRef
 * })
 */
export function useValidateAntFormAndTable(activeKey, refMap) {
  /**
   * Lấy dữ liệu tất cả bảng con
   */
  async function getSubFormAndTableData() {
    let formData = {};
    let all = Object.keys(refMap);
    let key = '';
    for (let i = 0; i < all.length; i++) {
      key = all[i];
      let instance = refMap[key].value;
      if (instance.isForm) {
        let subFormData = await validateFormAndGetData(instance, key);
        if (subFormData) {
          formData[key + 'List'] = [subFormData];
        }
      } else {
        let arr = await validateTableAndGetData(instance, key);
        if (arr && arr.length > 0) {
          formData[key + 'List'] = arr;
        }
      }
    }
    return formData;
  }

  /**
   * Dùng để chuyển dữ liệu Nếu có mảng thì chuyển thành định dạng tách bằng dấu phẩy
   * @param data
   */
  function transformData(data) {
    if (data) {
      Object.keys(data).map((k) => {
        if (data[k] instanceof Array) {
          data[k] = data[k].join(',');
        }
      });
    }
    return data;
  }

  /**
   * Bảng contable
   * @param instance
   * @param key
   */
  async function validateTableAndGetData(instance, key) {
    const errors = await instance.validateTable();
    if (!errors) {
      return instance.getTableData();
    } else {
      activeKey.value = key;
      // Tự động đặt lạiscrollTopTrạng thái, tránh xuất hiện trang trắng
      instance.resetScrollTop(0);
      return Promise.reject(1);
    }
  }

  /**
   * Biểu mẫu bảng con
   * @param instance
   * @param key
   */
  async function validateFormAndGetData(instance, key) {
    try {
      let data = await instance.getFormData();
      transformData(data);
      return data;
    } catch (e) {
      activeKey.value = key;
      return Promise.reject(e);
    }
  }

  return {
    getSubFormAndTableData,
    transformData,
  };
}
