import { defHttp } from '/@/utils/http/axios';
import { ref, watchEffect, computed, reactive } from 'vue';
import { pick } from 'lodash-es';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

function queryTableData(tableName, params) {
  const url = '/online/cgform/api/getData/' + tableName;
  return defHttp.get({ url, params });
}

function queryTableColumns(tableName, params) {
  const url = '/online/cgform/api/getColumns/' + tableName;
  return defHttp.get({ url, params });
}
//Thông tin cột bảng
const tableColumns = ref<any[]>([]);
// Tùy chọn
const selectOptions = ref<any[]>([]);
//Mục từ điển
const dictOptions = ref<any>({});
/**
 * table Chế độ: Gọi /sys/dict/queryTableDataForLinkRecord API (lấy dữ liệu nhiều trường trong một lần yêu cầu)
 */
function queryAllTableData(params: {
  tableName: string;
  showFields: string;
  valueField: string;
  keyword?: string;
  searchFields?: string;
  condition?: string;
  keyValues?: string;
  pageNo?: number;
  pageSize?: number;
}) {
  return defHttp.get({ url: '/sys/dict/queryTableDataForLinkRecord', params });
}

/**
 * table Chế độ: lấy tất cả các trường hiển thị cần truy vấn (textFields + imageField, loại bỏ trùng lặp)
 */
function getAllTableShowFields(props): string {
  const textFields = (props.textField || '').split(',').filter(Boolean);
  const imageField = props.imageField;
  const fieldSet = new Set<string>(textFields);
  if (imageField) fieldSet.add(imageField);
  return Array.from(fieldSet).join(',');
}

export function useLinkTable(props) {
  //TODO Hiện tại chỉ hỗ trợ truy vấn dữ liệu của trang đầu tiên, có thể nhập từ khóa để tìm kiếm
  const pageNo = ref('1');
  // Cột tìm kiếm
  const baseParam = ref<any>({});
  // Điều kiện tìm kiếm
  const searchParam = ref<any>({});
  // Cột văn bản đầu tiên
  const mainContentField = ref('');
  //Dữ liệu quyền hạn
  const auths = reactive({
    add: true,
    update: true,
  });

  //Cột hiển thị
  const textFieldArray = computed(() => {
    if (props.textField) {
      return props.textField.split(',');
    }
    return [];
  });
  const otherColumns = ref<any[]>([]);
  // Các cột hiển thị: đã cấu hình nhiều cột, nhưng chỉ hiển thị ba dòng
  const realShowColumns = computed(() => {
    const columns = otherColumns.value;
    if (props.multi == true) {
      return columns.slice(0, 3);
    } else {
      return columns.slice(0, 6);
    }
  });

  watchEffect(async () => {
    const table = props.tableName;
    if (table) {
      if (props.queryMode === 'table') {
        // table Chế độ: bỏ qua API Online, theo textField/imageField Cấu hình cột hiển thị khởi tạo
        const textFields = (props.textField || '').split(',').filter(Boolean);
        mainContentField.value = textFields[0] || '';
        // otherColumns: các trường sau trường đầu tiên trong textField, dùng để hiển thị nội dung phụ của thẻ
        otherColumns.value = textFields.slice(1).map((f) => ({ title: f, dataIndex: f }));
        // tableColumns Ghi lại tất cả các trường (dùng trong transData, ở chế độ allTable thực chất là no-op）
        tableColumns.value = textFields.map((f) => ({ title: f, dataIndex: f }));
        dictOptions.value = {};
        return;
      }

      const valueField = props.valueField || '';
      const textField = props.textField || '';
      const arr: any[] = [];
      if (valueField) {
        arr.push(valueField);
      }
      if (textField) {
        const temp = textField.split(',');
        mainContentField.value = temp[0];
        for (const field of temp) {
          arr.push(field);
        }
      }
      const imageField = props.imageField || '';
      if (imageField) {
        arr.push(imageField);
      }
      baseParam.value = {
        linkTableSelectFields: arr.join(','),
      };
      await resetTableColumns();
      await reloadTableLinkOptions();
    }
  });

  const otherFields = computed(() => {
    const textField = props.textField || '';
    const others: any[] = [];
    let labelField = '';
    if (textField) {
      const temp = textField.split(',');
      labelField = temp[0];
      for (let i = 0; i < temp.length; i++) {
        if (i > 0) {
          others.push(temp[i]);
        }
      }
    }
    return {
      others,
      labelField,
    };
  });


  async function resetTableColumns() {
    const params = baseParam.value;
    const data = await queryTableColumns(props.tableName, params);
    tableColumns.value = data.columns;
    if (data.columns) {
      const imageField = props.imageField;
      const arr = data.columns.filter((c) => c.dataIndex != mainContentField.value && c.dataIndex != imageField);
      otherColumns.value = arr;
    }
    dictOptions.value = data.dictOptions;
    // Dữ liệu quyền hạn
    console.log('Nút đã ẩn', data.hideColumns);
    if (data.hideColumns) {
      const hideCols = data.hideColumns;
      if (hideCols.indexOf('add') >= 0) {
        auths.add = false;
      } else {
        auths.add = true;
      }
      if (hideCols.indexOf('update') >= 0) {
        auths.update = false;
      } else {
        auths.update = true;
      }
    }
  }

  async function reloadTableLinkOptions() {
    const params = getLoadDataParams();
    const data = await queryTableData(props.tableName, params);
    const records = data.records;
    //tableTitle.value = data.head.tableTxt;
    const dataList: any[] = [];
    const { others, labelField } = otherFields.value;
    const imageField = props.imageField;
    if (records && records.length > 0) {
      for (const rd of records) {
        const temp = { ...rd };
        transData(temp);
        const result = Object.assign({}, pick(temp, others), { id: temp.id, label: temp[labelField], value: temp[props.valueField] });
        if (imageField) {
          result[imageField] = temp[imageField];
        }
        dataList.push(result);
      }
    }
    //Thêm một đối tượng trống làm chỗ cho thao tác add
    // Giải thích logic mã: 【TV360X-1095] Truy vấn nâng cao, bản ghi liên kết bỏ nút chỉnh sửa và bỏ nút bản ghi
    props.editBtnShow && dataList.push({});
    selectOptions.value = dataList;
  }

  /**
   * Dịch dữ liệu đơn giản-Từ điển
   * @param data
   */
  function transData(data) {
    const columns = tableColumns.value;
    const dictInfo = dictOptions.value;
    for (const c of columns) {
      const { dataIndex, customRender } = c;
      if (data[dataIndex] || data[dataIndex] === 0) {
        if (customRender && customRender == dataIndex) {
          //Như vậy là dữ liệu từ điển, có thể dịch trực tiếp
          if (dictInfo[customRender]) {
            data[dataIndex] = filterMultiDictText(dictInfo[customRender], data[dataIndex]);
            continue;
          }
        }
      }
      // Tương thích trường dịch của backend
      const dictText = data[dataIndex + '_dictText'];
      if (dictText) {
        data[dataIndex] = dictText;
      }
    }
  }

  //Lấy điều kiện truy vấn để tải dữ liệu
  function getLoadDataParams() {
    const params = Object.assign({ pageSize: 100, pageNo: pageNo.value }, baseParam.value, searchParam.value);
    return params;
  }

  //Đặt điều kiện truy vấn
  function addQueryParams(text) {
    if (!text) {
      searchParam.value = {};
    } else {
      const arr = textFieldArray.value;
      const params: any[] = [];
      const fields: any[] = [];
      for (let i = 0; i < arr.length; i++) {
        if (i <= 1) {
          fields.push(arr[i]);
          params.push({ field: arr[i], rule: 'like', val: text });
        }
      }
      // params[arr[i]] = `*${text}*`
      // params['selectConditionFields'] = fields.join(',')
      // searchParam.value = params;
      params['superQueryMatchType'] = 'or';
      params['superQueryParams'] = encodeURI(JSON.stringify(params));
      searchParam.value = params;
    }
  }

  async function loadOne(value) {
    if (!value) {
      return [];
    }

    // table Chế độ: gọi API mới, tra ngược tất cả các trường theo keyValues, trả về bản ghi đầy đủ trong một lần
    if (props.queryMode === 'table') {
      const showFields = getAllTableShowFields(props);
      if (!showFields) return [];
      try {
        const data = await queryAllTableData({
          tableName: props.tableName,
          showFields,
          valueField: props.valueField,
          keyValues: value,
          pageNo: 1,
          pageSize: 200,
        });
        const records: any[] = data?.records || [];
        if (records.length > 0) return records;
        // Khi API không có dữ liệu thì dùng value gốc để hiển thị dự phòng
        const firstTextField = (props.textField || '').split(',').filter(Boolean)[0] || props.valueField;
        return value
          .split(',')
          .filter(Boolean)
          .map((v: string) => ({ id: v, [props.valueField]: v, [firstTextField]: v }));
      } catch (e) {
        console.error('[useLinkTable] table loadOne Thất bại', e);
        return [];
      }
    }

    let valueFieldName = props.valueField;
    let params = {
      ...baseParam.value,
      pageSize: 100,
      pageNo: pageNo.value,
    };
    params['superQueryMatchType'] = 'and';
    let valueCondition = [{ field: valueFieldName, rule: 'in', val: value }];
    params['superQueryParams'] = encodeURI(JSON.stringify(valueCondition));
    const data = await queryTableData(props.tableName, params);
    let records = data.records;
    //tableTitle.value = data.head.tableTxt;
    let dataList: any[] = [];
    if (records && records.length > 0) {
      for (let item of records) {
        let temp = { ...item };
        transData(temp);
        dataList.push(temp);
      }
    }
    return dataList;
  }

  /**
   * true:Dữ liệu nhất quán;false:Dữ liệu không nhất quán
   * @param arr
   * @param value
   */
  function compareData(arr, value) {
    if (!arr || arr.length == 0) {
      return false;
    }
    const valueArray = value.split(',');
    if (valueArray.length != arr.length) {
      return false;
    }
    let flag = true;
    for (const item of arr) {
      const temp = item[props.valueField];
      if (valueArray.indexOf(temp) < 0) {
        flag = false;
      }
    }
    return flag;
  }

  function formatData(formData) {
    Object.keys(formData).map((k) => {
      if (formData[k] instanceof Array) {
        formData[k] = formData[k].join(',');
      }
    });
  }

  function initFormData(formData, linkFieldArray, record) {
    if (!record) {
      record = {};
    }
    if (linkFieldArray && linkFieldArray.length > 0) {
      for (const str of linkFieldArray) {
        const arr = str.split(',');
        //["Trường biểu mẫu,Trường từ điển bảng"]
        const field = arr[0];
        const dictField = arr[1];
        if (!formData[field]) {
          const value = record[dictField] || '';
          formData[field] = [value];
        } else {
          formData[field].push(record[dictField]);
        }
      }
    }
  }

  // Lấy địa chỉ hình ảnh
  function getImageSrc(item) {
    if (props.imageField) {
      let url = item[props.imageField];
      // Giải thích logic mã: 【TV360X-38]Không gian bản ghi liên kết, khi dữ liệu được liên kết có nhiều hình ảnh thì ảnh bìa không hiển thị
      if (typeof url === 'string') {
        // Khi có nhiều hình ảnh, mặc định lấy hình đầu tiên
        url = url.split(',')[0];
      }
      return getFileAccessHttpUrl(url);
    }
    return '';
  }
  const showImage = computed(() => {
    if (props.imageField) {
      return true;
    } else {
      return false;
    }
  });

  return {
    pageNo,
    otherColumns,
    realShowColumns,
    selectOptions,
    reloadTableLinkOptions,
    textFieldArray,
    addQueryParams,
    tableColumns,
    transData,
    mainContentField,
    loadOne,
    compareData,
    formatData,
    initFormData,
    getImageSrc,
    showImage,
    auths,
  };
}

/**
 * Sử dụng modal có chiều cao cố địnhmodal
 */
export function useFixedHeightModal() {
  const minWidth = 800;
  const popModalFixedWidth = ref(800);
  let tempWidth = window.innerWidth - 300;
  if (tempWidth < minWidth) {
    tempWidth = minWidth;
  }
  popModalFixedWidth.value = tempWidth;

  // Kiểm soát chiều cao popup
  const popBodyStyle = ref({});
  function resetBodyStyle() {
    const height = window.innerHeight - 210;
    popBodyStyle.value = {
      height: height + 'px',
      overflowY: 'auto',
    };
  }

  return {
    popModalFixedWidth,
    popBodyStyle,
    resetBodyStyle,
  };
}
