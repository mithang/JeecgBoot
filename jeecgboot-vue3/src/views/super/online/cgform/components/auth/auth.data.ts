import { computed } from 'vue';
import dayjs from 'dayjs';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { getAreaTextByCodeAnyLevel } from '/@/components/Form/src/utils/Area';
import { provinceOptions, provinceAndCityData, regionData } from '/@/components/Form/src/utils/areaDataUtil';
import { useConditionFilter } from '/@/utils/index';

// Cấu hình cột quyền trường
export const authFieldColumns: BasicColumn[] = [
  {
    title: 'Kích hoạt',
    dataIndex: 'switch',
    width: 100,
    align: 'center',
    slots: { customRender: 'switch' },
  },
  {
    title: 'Tên trường',
    width: 200,
    dataIndex: 'code',
  },
  {
    title: 'Mô tả trường',
    // width: 200,
    dataIndex: 'title',
  },
  {
    title: 'Kiểm soát danh sách',
    dataIndex: 'list',
    width: 120,
    slots: { customRender: 'list' },
  },
  {
    title: 'Kiểm soát biểu mẫu',
    dataIndex: 'form',
    width: 180,
    slots: { customRender: 'form' },
  },
];

// Cấu hình cột quyền nút
export const authButtonColumns: BasicColumn[] = [
  {
    title: 'Kích hoạt',
    dataIndex: 'switch',
    width: 80,
    slots: { customRender: 'switch' },
  },
  {
    title: 'Tên',
    dataIndex: 'title',
  },
  {
    title: 'Mã (code)',
    dataIndex: 'code',
  },
  {
    title: 'Kiểm soát quyền',
    dataIndex: 'control',
    width: 180,
    slots: { customRender: 'control' },
  },
];

export const authButtonFixedList = [
  { code: 'add', title: 'Thêm mới', status: 0 },
  { code: 'addSub', title: 'Thêm cấp dưới', status: 0 },
  { code: 'edit', title: 'Chỉnh sửa', status: 0 },
  { code: 'detail', title: 'Chi tiết', status: 0 },
  { code: 'delete', title: 'Xóa', status: 0 },
  { code: 'batch_delete', title: 'Xóa hàng loạt', status: 0 },
  { code: 'export', title: 'Xuất (export)', status: 0 },
  { code: 'import', title: 'Nhập (import)', status: 0 },
  { code: 'print', title: 'In', status: 0 },
  { code: 'query', title: 'Truy vấn', status: 0 },
  { code: 'reset', title: 'Đặt lại', status: 0 },
  { code: 'aigc_mock_data', title: 'Tạo dữ liệu kiểm thử', status: 0 },
  { code: 'bpm', title: 'Gửi quy trình', status: 0 },
  { code: 'super_query', title: 'Truy vấn nâng cao', status: 0 },
  { code: 'form_confirm', title: 'Xác nhận', status: 0 },
  // update-begin--author:liusq---date:20250721---for：【LHZP-540】Kiểm soát quyền nút của bảng con một-nhiều
  { code: 'popup_add', title: 'Thêm mới bằng popup', status: 0 },
  { code: 'popup_update', title: 'Chỉnh sửa bằng popup', status: 0 },
  // update-end--author:liusq---date:20250721---for：【LHZP-540】Kiểm soát quyền nút của bảng con một-nhiều
];

export const USE_SQL_RULES = 'USE_SQL_RULES';

const FUZZY_RULE_OPERATORS = ['LIKE', 'RIGHT_LIKE', 'LEFT_LIKE'];
const FUZZY_RULE_VIEWS = [
  'list',
  'list_multi',
  'radio',
  'checkbox',
  'sel_search',
  'sel_user',
  'sel_depart',
  'link_table',
  'popup_dict',
  'cat_tree',
  'sel_tree',
  'pca',
];
const FUZZY_RULE_OPTIONS = [
  { label: 'Mờ (fuzzy)', value: 'LIKE' },
  { label: 'bằng..Bắt đầu', value: 'RIGHT_LIKE' },
  { label: 'bằng..Kết thúc (phần cuối)', value: 'LEFT_LIKE' },
];

export function isFuzzyRuleOperator(operator) {
  return FUZZY_RULE_OPERATORS.includes(operator);
}

/** là mã lưu trữ/Trường loại lựa chọn theo cặp khóa-giá trị bổ sung điều kiện khớp mờ, không ảnh hưởng quy tắc lọc điều kiện toàn cục. */
export function supplementAuthFuzzyConditions(view, options) {
  if (!FUZZY_RULE_VIEWS.includes(view)) return options;
  const missingOptions = FUZZY_RULE_OPTIONS.filter((option) => !options.some((item) => item.value === option.value));
  if (!missingOptions.length) return options;
  const equalIndex = options.findIndex((item) => item.value === '=');
  const insertIndex = equalIndex < 0 ? 0 : equalIndex + 1;
  return [...options.slice(0, insertIndex), ...missingOptions, ...options.slice(insertIndex)];
}

/** Trường quy tắc quyền dữ liệu chỉ hỗ trợ cái không phải của cơ sở dữ liệu đã đồng bộ popup trường. */
export function isAuthDataField(field) {
  return Number(field?.dbIsPersist) === 1 && String(field?.fieldShowType || '').toLowerCase() !== 'popup';
}

// update-begin--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc/Quy tắc điều kiện tự động thích ứng theo loại control của trường quy tắc, tham khảoFormSchemaFactoryphương thức ánh xạ control của
// Control do từ điển hệ thống điều khiển (dropdown, chọn đơn, chọn nhiều, dropdown chọn nhiều)
const DICT_CODE_VIEWS = ['list', 'list_multi', 'radio', 'checkbox'];
// Control do từ điển bảng điều khiển, của nó dictTable/dictText/dictField Ngữ nghĩa nhất quán với từ điển bảng hệ thống, có thể dùng lại trực tiếp table,text,code Định dạng
// (bản ghi liên kết và Popup Từ điển sử dụng component chuyên dụng riêng của mỗi loại;cat_tree định dạng cấu hình từ điển của khác nhau, tạm thời không đưa vào chuyển đổi tự động)
const DICT_TABLE_VIEWS = ['sel_search'];
// Control thuộc loại ngày giờ
const DATE_TIME_VIEWS = ['date', 'datetime', 'time', 'date_year', 'date_month', 'date_week', 'date_quarter'];

function buildDictCode(dictField, dictTable, dictText) {
  if (dictTable) {
    // Từ điển bảng, định dạng giống với SelectWidget/LinkTableWidget Giữ nhất quán:table,text,code
    return encodeURI(`${dictTable},${dictText || dictField},${dictField}`);
  }
  return dictField;
}

/** Lấy mã từ điển tương ứng với trường quy tắc, hiển thị danh sách và component giá trị quy tắc dùng chung cùng một cấu hình từ điển. */
export function getRuleValueDictCode(field) {
  if (!field) return '';
  const { view, dictField, dictTable, dictText } = field;
  if (DICT_CODE_VIEWS.includes(view) && dictField) {
    return buildDictCode(dictField, dictTable, dictText);
  }
  if (DICT_TABLE_VIEWS.includes(view) && dictTable && dictField) {
    return buildDictCode(dictField, dictTable, dictText);
  }
  if (view === 'link_table' && dictTable && dictField) {
    // Bản ghi liên kết có thể cấu hình nhiều trường hiển thị, việc dịch từ điển bảng chỉ lấy trường đầu tiên, đảm bảo tạo ra table,text,code mã hóa ba đoạn
    const firstTextField = (dictText || dictField).split(',')[0];
    return buildDictCode(dictField, dictTable, firstTextField);
  }
  return '';
}

function dictSelectWidget(dictCode, multiple = false) {
  return {
    component: 'JDictSelectTag',
    componentProps: {
      dictCode,
      allowClear: true,
      getPopupContainer: () => document.body,
      // IN Khi có điều kiện thì chọn nhiều, nhiều giá trị lưu trữ phân tách bằng dấu phẩy
      ...(multiple ? { mode: 'multiple' } : {}),
    },
  };
}

// Bản ghi liên kết, tham khảo JLinkTableCard của table Chế độ
function linkTableWidget(field, multiple = false) {
  const { dictTable, dictField, dictText, fieldExtendJson } = field;
  if (!dictTable || !dictField || !dictText) return null;
  let imageField = '';
  try {
    imageField = fieldExtendJson ? JSON.parse(fieldExtendJson).imageField || '' : '';
  } catch (_error) {}
  return {
    component: 'JLinkTableCard',
    componentProps: {
      queryMode: 'table',
      tableName: dictTable,
      valueField: dictField,
      textField: dictText,
      imageField,
      multi: multiple,
    },
  };
}

/** Lấy Popup Cấu hình từ điển, định dạng giống với PopupDictWidget Giữ nhất quán: mã báo cáo,Trường hiển thị,trường lưu trữ giá trị. */
export function getPopupDictConfig(field) {
  const { dictTable, dictField, dictText } = field;
  if (!dictTable || !dictField || !dictText) return null;
  return {
    code: dictTable,
    labelField: dictText,
    valueField: dictField,
    dictCode: `${dictTable},${dictText},${dictField}`,
  };
}

// Popup từ điển
function popupDictWidget(field, multiple = false) {
  const config = getPopupDictConfig(field);
  if (!config) return null;
  return {
    component: 'JPopupDict',
    componentProps: {
      dictCode: config.dictCode,
      multi: multiple,
      allowClear: true,
    },
  };
}

const AREA_DISPLAY_LEVELS = ['province', 'city', 'region', 'all'];

/** Đọc cấp hiển thị tỉnh/thành/quận huyện hợp lệ từ cấu hình mở rộng của trường, không hợp lệ JSON Xử lý theo chưa cấu hình. */
function getAreaDisplayLevel(fieldExtendJson) {
  try {
    const displayLevel = fieldExtendJson ? JSON.parse(fieldExtendJson).displayLevel : undefined;
    return AREA_DISPLAY_LEVELS.includes(displayLevel) ? displayLevel : undefined;
  } catch (_error) {
    return undefined;
  }
}

/** Mở rộng cây khu vực dạng thác đổ thành các mục dropdown chọn nhiều, đồng thời giữ nguyên tên cấp độ đầy đủ để tránh nhầm lẫn giữa các khu vực trùng tên. */
function flattenAreaOptions(options, parentLabels: string[] = []) {
  return options.flatMap((option) => {
    const labels = [...parentLabels, option.label];
    if (option.children?.length) {
      return flattenAreaOptions(option.children, labels);
    }
    return [{ value: option.value, label: labels.join('/') }];
  });
}

/** Căn cứ cấp hiển thị của trường, chọn nguồn dữ liệu tỉnh, thành phố hoặc quận/huyện. */
function getAreaOptions(displayLevel) {
  if (displayLevel === 'province') return provinceOptions;
  if (displayLevel === 'city') return flattenAreaOptions(provinceAndCityData);
  return flattenAreaOptions(regionData);
}

// Tỉnh/thành/quận huyện, tham khảo auto/comp/factory/impl/AreaLinkage.ts；IN Điều kiện sử dụng chọn nhiều dạng phẳng, giá trị vẫn lưu mã khu vực
function areaWidget(fieldExtendJson, multiple = false) {
  const displayLevel = getAreaDisplayLevel(fieldExtendJson);
  if (multiple) {
    return {
      component: 'JDictSelectTag',
      componentProps: {
        options: getAreaOptions(displayLevel),
        mode: 'multiple',
        allowClear: true,
        showSearch: true,
        onlySearchByLabel: true,
        getPopupContainer: () => document.body,
      },
    };
  }
  return {
    component: 'JAreaLinkage',
    componentProps: {
      saveCode: displayLevel && displayLevel !== 'all' ? displayLevel : 'region',
      ...(displayLevel ? { displayLevel } : {}),
      getPopupContainer: () => document.body,
    },
  };
}

// Ngày, thời gian (bao gồm năm/Tháng/Tuần/bộ chọn quý), tham khảo auto/comp/factory/impl/DateWidget.ts
function dateTimeWidget(view, fieldExtendJson) {
  const viewPicker = view.startsWith('date_') ? view.substring(5) : undefined;
  const normalizedView = viewPicker ? 'date' : view;
  if (normalizedView === 'time') {
    return {
      component: 'TimePicker',
      componentProps: {
        valueFormat: 'HH:mm:ss',
        style: { width: '100%' },
        getPopupContainer: () => document.body,
      },
    };
  }
  let picker = viewPicker;
  if (normalizedView === 'date' && !picker && fieldExtendJson) {
    try {
      const json = JSON.parse(fieldExtendJson);
      if (json.picker && json.picker != 'default') {
        picker = json.picker; // year、month、week、quarter
      }
    } catch (_error) {}
  }
  const displayFormatMap = { year: 'YYYY', month: 'YYYY-MM', week: 'YYYY-wo', quarter: 'YYYY-[Q]Q' };
  // picker Định dạng đặc biệt chỉ dùng để hiển thị; giá trị quy tắc thống nhất lưu thành ngày tiêu chuẩn, tránh antd Phân tích nội bộ Q/wo Khi định dạng thì ném lỗi
  const valueFormat = normalizedView === 'datetime' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD';
  return {
    component: 'DatePicker',
    componentProps: {
      picker,
      format: picker ? displayFormatMap[picker] : undefined,
      showTime: normalizedView === 'datetime',
      valueFormat,
      style: { width: '100%' },
      getPopupContainer: () => document.body,
    },
  };
}

//update-begin--author:wangshuai---date:20260723---for：【LHZP-1267】Năm/Khi lưu, giá trị quy tắc năm-tháng được chuẩn hóa thành năm hiện tại/Ngày đầu tiên của tháng hiện tại
/**
 * Năm/Chuẩn hóa giá trị quy tắc của control ngày năm-tháng: năm lưu thành năm hiện tại1Tháng1ngày, năm-tháng lưu thành tháng hiện tại1ngày.
 * Năm/Sau khi bộ chọn năm-tháng được chọn valueFormat Cái xuất ra là ngày trong ngày của panel, lưu trực tiếp sẽ khiến backend so sánh phạm vi bị lệch.
 */
export function normalizeDateRuleValue(field, value) {
  if (!field || !value || typeof value !== 'string' || value.startsWith('#{')) return value;
  const { view, fieldExtendJson } = field;
  let picker = view.startsWith('date_') ? view.substring(5) : '';
  if (!picker && view === 'date' && fieldExtendJson) {
    try {
      const json = JSON.parse(fieldExtendJson);
      if (json.picker && json.picker !== 'default') picker = json.picker;
    } catch (_error) {}
  }
  if (picker === 'year') return `${value.substring(0, 4)}-01-01`;
  if (picker === 'month') return `${value.substring(0, 7)}-01`;
  if (picker === 'week') {
    // Bộ chọn tuần lưu thành ngày đầu tiên của tuần hiện tại (Thứ Hai), tính theo độ lệch thứ trong tuần
    const date = dayjs(value);
    if (!date.isValid()) return value;
    return date.subtract((date.day() + 6) % 7, 'day').format('YYYY-MM-DD');
  }
  return value;
}
//update-end--author:wangshuai---date:20260723---for：【LHZP-1267】Năm/Khi lưu, giá trị quy tắc năm-tháng được chuẩn hóa thành năm hiện tại/Ngày đầu tiên của tháng hiện tại

// Phân tích người dùng/Control chọn phòng ban, trường lưu trữ và trường hiển thị được chỉ định trong cấu hình mở rộng của trường
export function getUserDepartExtend(field) {
  if (!field?.fieldExtendJson) return {};
  try {
    return typeof field.fieldExtendJson === 'string' ? JSON.parse(field.fieldExtendJson) : field.fieldExtendJson;
  } catch (_error) {
    return {};
  }
}

// Lựa chọn của người dùng, tham khảo auto/comp/factory/impl/SelectUserWidget.ts(giá trị quy tắc là trường lưu trữ phân tách bằng dấu phẩy;IN khi có điều kiện thì cho phép chọn nhiều, nếu không thì chọn đơn)
function userWidget(field, multiple = false) {
  const extend = getUserDepartExtend(field);
  const componentProps: any = {
    isRadioSelection: !multiple,
    showSelected: false,
    allowClear: true,
    showButton: true,
  };
  // Sử dụng trường lưu trữ trong cấu hình mở rộng của trường/Trường hiển thị, và Online Giữ giá trị lưu của biểu mẫu nhất quán
  if (extend.store) {
    componentProps.rowKey = extend.store;
  }
  if (extend.text) {
    componentProps.labelKey = extend.text;
  }
  return {
    component: 'JSelectUser',
    componentProps,
  };
}

// Chọn phòng ban, tham khảo auto/comp/factory/impl/SelectDepartWidget.ts(giá trị quy tắc là trường lưu trữ phân tách bằng dấu phẩy;IN khi có điều kiện thì cho phép chọn nhiều, nếu không thì chọn đơn)
function departWidget(field, multiple = false) {
  const extend = getUserDepartExtend(field);
  const componentProps: any = {
    checkStrictly: true,
    multiple,
    showButton: true,
  };
  // Sử dụng trường lưu trữ trong cấu hình mở rộng của trường/Trường hiển thị, và Online Giữ giá trị lưu của biểu mẫu nhất quán
  if (extend.store) {
    componentProps.rowKey = extend.store;
  }
  if (extend.text) {
    componentProps.labelKey = extend.text;
  }
  return {
    component: 'JSelectDept',
    componentProps,
  };
}

/**
 * Căn cứ theo"Trường quy tắc"loại control tương ứng, tính toán ra"Giá trị quy tắc"Component nên sử dụng vàcomponentProps
 * Control không thuộc loại từ điển (văn bản,popupv.v.) trả về null, dùng lại theo cái gốc JInputSelect Ô nhập
 */
export function getRuleValueWidget(field, ruleOperator?) {
  if (!field) return null;
  if (isFuzzyRuleOperator(ruleOperator)) return null;
  // Quy tắc điều kiện là"tại...trong"(IN)khi đó, từ điển/Người dùng/Control loại phòng ban chuyển sang chọn nhiều (nhiều giá trị lưu trữ phân tách bằng dấu phẩy, backend tách theo dấu phẩy rồi nối in điều kiện)
  const isIn = ruleOperator === 'IN';
  const { view, fieldExtendJson } = field;
  if (view === 'popup_dict') {
    return popupDictWidget(field, isIn);
  }
  if (view === 'link_table') {
    return linkTableWidget(field, isIn);
  }
  const dictCode = getRuleValueDictCode(field);
  if (dictCode) {
    return dictSelectWidget(dictCode, isIn);
  }
  if (view === 'pca') {
    return areaWidget(fieldExtendJson, isIn);
  }
  if (DATE_TIME_VIEWS.includes(view)) {
    return dateTimeWidget(view, fieldExtendJson);
  }
  if (view === 'sel_user') {
    return userWidget(field, isIn);
  }
  if (view === 'sel_depart') {
    return departWidget(field, isIn);
  }
  return null;
}

// Component nhập giá trị quy tắc mặc định (nhập văn bản + biến hệ thống tùy chọn)
const DEFAULT_SYS_VAR_OPTIONS = [
  { label: 'Tài khoản của người dùng hiện tại', value: '#{sys_user_code}' },
  { label: 'Tên người dùng hiện tại', value: '#{sys_user_name}' },
  { label: 'Ngày hiện tại', value: '#{sys_date}' },
  { label: 'Thời gian hiện tại', value: '#{sys_time}' },
  { label: 'Phòng ban của người dùng hiện tại', value: '#{sys_org_code}' },
  { label: 'Phòng ban của người dùng đăng nhậpID', value: '#{sys_org_id}' },
  { label: 'Tất cả phòng ban của người dùng hiện tại', value: '#{sys_multi_org_code}' },
  { label: 'Đơn vị thuê hiện tại', value: '#{tenant_id}' },
];

/** Lấy tên tiếng Trung của biến hệ thống, chỉ dùng để hiển thị giao diện. */
export function getSysVarLabel(value) {
  return DEFAULT_SYS_VAR_OPTIONS.find((item) => item.value === value)?.label;
}

/** Chuyển đổi văn bản giá trị quy tắc trong danh sách theo loại control của trường. */
export function getRuleValueDisplayText(field, value, ruleOperator?) {
  const rawValue = Array.isArray(value) ? value.join(',') : String(value ?? '');
  if (isFuzzyRuleOperator(ruleOperator)) return rawValue;
  if (field?.view === 'pca') {
    const displayLevel = getAreaDisplayLevel(field.fieldExtendJson);
    const levelMap = { province: 1, city: 2, region: 3 } as const;
    const level = levelMap[displayLevel];
    const values =
      Array.isArray(value) || ruleOperator === 'IN'
        ? rawValue
            .split(',')
            .map((item) => item.trim())
            .filter(Boolean)
        : [rawValue];
    return values.map((item) => getAreaTextByCodeAnyLevel(item, !level, level) || item).join('、');
  }
  return rawValue;
}

export const DEFAULT_RULE_VALUE_WIDGET = {
  component: 'JAuthRuleValue' as const,
  componentProps: {
    selectPlaceholder: 'Biến hệ thống',
    inputPlaceholder: 'Vui lòng nhập',
    getPopupContainer: () => document.body,
    options: DEFAULT_SYS_VAR_OPTIONS,
  },
};

/**
 * Đóng gói component giá trị quy tắc cụ thể vào JAuthRuleValue trong đó, khiến nó vẫn có thể chuyển đổi biến hệ thống
 * @param widget {component, componentProps}
 */
export function wrapRuleValueWidget(widget) {
  return {
    component: 'JAuthRuleValue' as const,
    componentProps: {
      selectPlaceholder: 'Biến hệ thống',
      inputPlaceholder: undefined,
      getPopupContainer: () => document.body,
      options: DEFAULT_SYS_VAR_OPTIONS,
      customComponent: widget,
    },
  };
}
// update-end--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc/Quy tắc điều kiện tự động thích ứng theo loại control của trường quy tắc, tham khảoFormSchemaFactoryphương thức ánh xạ control của
// Cấu hình cột quyền dữ liệu
export const authDataColumns: BasicColumn[] = [
  {
    title: 'Kích hoạt',
    dataIndex: 'switch',
    width: 80,
    slots: { customRender: 'switch' },
  },
  {
    title: 'Tên quy tắc',
    dataIndex: 'ruleName',
    width: 130,
  },
  {
    title: 'Mô tả quy tắc',
    dataIndex: 'description',
    align: 'left',
    slots: { customRender: 'description' },
  },
];

export function useAuthDataFormSchemas(props, methods) {
  const formSchemas = computed<FormSchema[]>(() => [
    {
      label: 'Tên quy tắc',
      field: 'ruleName',
      required: true,
      component: 'Input',
      componentProps: {
        onChange: methods.onRuleNameChange,
      },
    },
    {
      label: 'Trường quy tắc',
      field: 'ruleColumn',
      component: 'JSearchSelect',
      componentProps: {
        dictOptions: props.authFields,
        getPopupContainer: () => document.body,
        onChange: methods.onRuleColumnChange,
      },
      dynamicRules({ model }) {
        return [{ required: model.ruleOperator != USE_SQL_RULES, message: 'Vui lòng nhập trường quy tắc' }];
      },
      show: ({ model }) => model.ruleOperator != USE_SQL_RULES,
    },
    // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-201】Điều kiện quản lý quyền lọc theo control
    {
      label: 'Quy tắc điều kiện',
      field: 'ruleOperator',
      required: true,
      component: 'JDictSelectTag',
      componentProps: {
        options: [],
        onChange: methods.onRuleOperatorChange,
        getPopupContainer: () => document.body,
      },
      dynamicPropskey: 'options',
      dynamicPropsVal: ({ model }) => {
        const getFieldType = (type) => {
          if (['BigDecimal', 'double', 'int', 'long'].includes(type)) {
            return 'number';
          } else {
            return;
          }
        };
        const { filterCondition } = useConditionFilter();
        if (model.ruleColumn) {
          const findItem = props.authFields.find((item) => item.value === model.ruleColumn) ?? {};
          const filteredOptions = filterCondition({ view: findItem.view, fieldType: getFieldType(findItem.dbType) }).map((item) => ({
            label: item.label,
            value: item.val ?? item.value,
          }));
          const result = supplementAuthFuzzyConditions(findItem.view, filteredOptions);
          result.push({ value: 'USE_SQL_RULES', label: 'Tùy chỉnhSQL' });
          return result;
        } else {
          return [{ value: 'USE_SQL_RULES', label: 'Tùy chỉnhSQL' }];
        }
      },
    },
    // {
    //   label: 'Quy tắc điều kiện',
    //   field: 'ruleOperator',
    //   required: true,
    //   component: 'JDictSelectTag',
    //   componentProps: {
    //     dictCode: 'rule_conditions',
    //     onChange: methods.onRuleOperatorChange,
    //     getPopupContainer: () => document.body,
    //   },
    // },
    // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-201】Điều kiện quản lý quyền lọc theo control
    {
      label: 'Giá trị quy tắc',
      field: 'ruleValue',
      // -update-begin--author:liaozhiyang---date:20260713---for：【LHZP-600】Khi quy tắc điều kiện rỗng và không rỗng, giá trị quy tắc sẽ bị ẩn
      dynamicRules({ model }) {
        if (['EMPTY', 'NOT_EMPTY'].includes(model.ruleOperator)) {
          return [];
        }
        return [
          {
            required: true,
            validator: (_rule, value) => {
              // IN Trong điều kiện, giá trị của component chọn nhiều là mảng, cần tương thích việc xét mảng rỗng
              const isEmpty = value == null || value === '' || (Array.isArray(value) && value.length === 0);
              return isEmpty ? Promise.reject(new Error('Vui lòng nhập giá trị quy tắc')) : Promise.resolve();
            },
          },
        ];
      },
      show: ({ model }) => !['EMPTY', 'NOT_EMPTY'].includes(model.ruleOperator),
      // -update-end--author:liaozhiyang---date:20260713---for：【LHZP-600】Khi quy tắc điều kiện rỗng và không rỗng, giá trị quy tắc sẽ bị ẩn
      // -update-begin--author:liaozhiyang---date:20240607---for：【TV360X-536】Tối ưu cấu hình quyền dữ liệu và bổ sungJInputSelectComponent
      ...DEFAULT_RULE_VALUE_WIDGET,
      // -update-end--author:liaozhiyang---date:20240607---for：【TV360X-536】Tối ưu cấu hình quyền dữ liệu và bổ sungJInputSelectComponent
    },
    {
      label: 'Trạng thái',
      field: 'status',
      required: true,
      component: 'RadioButtonGroup',
      componentProps: {
        options: [
          { label: 'Kích hoạt', value: 1 },
          { label: 'Không kích hoạt', value: 0 },
        ],
      },
      defaultValue: 1,
    },
  ]);
  return { formSchemas };
}
