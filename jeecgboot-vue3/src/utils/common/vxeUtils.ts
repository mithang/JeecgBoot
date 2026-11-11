import { getValueType } from '/@/utils';

export const VALIDATE_FAILED = Symbol();
/**
 * Kiểm tra một lần biểu mẫu chính và tất cả biểu mẫu phụ(Phiên bản mới)
 * @param form Biểu mẫu chính form Đối tượng
 * @param cases Nhận một mảng, mỗi mục đều là mộtJEditableTableInstance
 * @returns {Promise<any>}
 */
export async function validateFormModelAndTables(validate, formData, cases, props, autoJumpTab?) {
  if (!(validate && typeof validate === 'function')) {
    throw `validate Tham số cần là một phương thức, nhưng truyền vào lại là${typeof validate}`;
  }
  let dataMap = {};
  let values = await new Promise((resolve, reject) => {
    // Kiểm tra biểu mẫu bảng chính
    validate()
      .then(() => {
        // Giải thích logic mã: [VUEN-912]Component người dùng một-nhiều (mọi kiểu, bảng đơn và cây không có vấn đề) báo lỗi khi lưu------------
        for (let data in formData) {
          //Nếu dữ liệu này là mảng
          if (formData[data] instanceof Array) {
            let valueType = getValueType(props, data);
            //Nếu là kiểu chuỗi thì cần đổi thành chuỗi tách bằng dấu phẩy
            if (valueType === 'string') {
              formData[data] = formData[data].join(',');
            }
          }
        }
        resolve(formData);
      })
      // Giải thích logic mã: 【TV360X-1064]Các mục kiểm tra cuộn khi submit biểu mẫu không phải native không đạt---
      .catch(({ errorFields }) => {
        reject({ error: VALIDATE_FAILED, index: 0, errorFields: errorFields });
      });
  });
  Object.assign(dataMap, { formValue: values });
  // Kiểm tra biểu mẫu tất cả bảng con
  let subData = await validateTables(cases, autoJumpTab);
  // Hợp nhất dữ liệu cuối cùng
  dataMap = Object.assign(dataMap, { tablesValue: subData });
  return dataMap;
}
/**
 * Kiểm tra và lấy tất cả giá trị của một hoặc nhiều bảng
 * @param cases Nhận một mảng, mỗi mục đều là mộtJEditableTableInstance
 * @param autoJumpTab Có tự động chuyển đến chỗ báo lỗi khôngtab
 */
export function validateTables(cases, autoJumpTab = true) {
  if (!(cases instanceof Array)) {
    throw `'validateTables'của hàm'cases'Tham số cần là một mảng, nhưng truyền vào lại là${typeof cases}`;
  }
  return new Promise((resolve, reject) => {
    let tablesData: any = [];
    let index = 0;
    if (!cases || cases.length === 0) {
      resolve(tablesData);
    }
    (function next() {
      let vm = cases[index];
      vm.value.validateTable().then((errMap) => {
        // Kiểm tra đạt
        if (!errMap) {
          tablesData[index] = { tableData: vm.value.getTableData() };
          // Kiểm tra việc kiểm chứng đã hoàn thành hết chưa, hoàn thành thì trả về thành công, nếu không thì tiếp tục bước kiểm chứng tiếp theo
          if (++index === cases.length) {
            resolve(tablesData);
          } else next();
        } else {
          // Thử lấytabKey, nếu ởATabTrong component là có thể lấy
          let paneKey;
          let tabPane = getVmParentByName(vm.value, 'ATabPane');
          if (tabPane) {
            paneKey = tabPane.$.vnode.key;
            // Tự động chuyển đến bảng đó
            if (autoJumpTab) {
              let tabs = getVmParentByName(tabPane, 'Tabs');
              tabs && tabs.setActiveKey && tabs.setActiveKey(paneKey);
            }
          }
          // Xuất hiện biểu mẫu chưa qua kiểm tra, không thực hiện bước kiểm tra tiếp theo nữa, trả về thất bại trực tiếp
          // Giải thích logic mã: TV360X-478 Một-nhiềutab, khi kiểm tra không đạt,tabKhông chuyển
          reject({ error: VALIDATE_FAILED, index, paneKey, errMap, subIndex: index });
        }
      });
    })();
  });
}

export function getVmParentByName(vm, name) {
  let parent = vm.$parent;
  if (parent && parent.$options) {
    if (parent.$options.name === name) {
      return parent;
    } else {
      let res = getVmParentByName(parent, name);
      if (res) {
        return res;
      }
    }
  }
  return null;
}
