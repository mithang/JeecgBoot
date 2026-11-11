import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { getDataSourceList } from './cgreport.api';
import {usePermissionStore} from "/@/store/modules/permission";
const permissionStore = usePermissionStore();
//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Tên báo cáo',
    align: 'center',
    dataIndex: 'name',
    width: 120,
  },
  {
    title: 'Mã báo cáo',
    align: 'center',
    dataIndex: 'code',
    width: 120,
  },
  {
    title: 'Báo cáoSQL',
    align: 'center',
    dataIndex: 'cgrSql',
    width: 360,
  },
  {
    title: 'Nguồn dữ liệu',
    align: 'center',
    dataIndex: 'dbSource',
    customRender: ({ text, record }) => {
      return record["dbSource_dictText"] ? record["dbSource_dictText"] : text
    },
    width: 120,
  },
  {
    title: 'Thời gian tạo',
    align: 'center',
    dataIndex: 'createTime',
    width: 120,
  },
];
//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên báo cáo',
    field: 'name',
    component: 'JInput',
  },
  {
    label: 'Mã báo cáo',
    field: 'code',
    component: 'JInput',
  },
];

// Kiểm chứng mã chỉonlineBáo cáo dùng
const codePattern = /^[a-z|A-Z][a-z|A-Z|\d|_|-]{0,}$/;
//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Mã báo cáo',
    field: 'code',
    component: 'Input',
    colProps: {
      sm: 24,
      xs: 24,
      md: 12,
      lg: 8,
      xl: 8,
      xxl: 8,
    },
    dynamicRules: ({ values, model }) => {
      console.log('values:', values);
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                return reject('Vui lòng nhập mã báo cáo!');
              }
              if (!codePattern.test(value)) {
                return reject('Mã phải bắt đầu bằng chữ cái, có thể chứa số, dấu gạch dưới, dấu gạch ngang!');
              }
              let params = {
                tableName: 'onl_cgreport_head',
                fieldName: 'code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject('Mã báo cáo đã tồn tại!');
                })
                .catch((err) => {
                  reject(err.message || 'Kiểm chứng thất bại');
                });
            });
          },
        },
      ];
    },
  },
  {
    label: 'Tên báo cáo',
    field: 'name',
    component: 'Input',
    colProps: {
      sm: 24,
      xs: 24,
      md: 12,
      lg: 8,
      xl: 8,
      xxl: 8,
    },
    dynamicRules: () => {
      return [{ required: true, message: 'Vui lòng nhập tên báo cáo!' }];
    },
  },
  {
    label: 'Nguồn dữ liệu động',
    field: 'dbSource',
    colProps: {
      sm: 24,
      xs: 24,
      md: 12,
      lg: 8,
      xl: 8,
      xxl: 8,
    },
    component: 'ApiSelect',
    rules: [{ required: permissionStore.sysSafeMode, message: 'Vui lòng chọn nguồn dữ liệu!' }],
    componentProps: {
      api: getDataSourceList,
    },
  },
  /*    {
        label: ' ',
        field: 'line1',
        component: 'Input',
        slot: 'line1',
        colProps: {
            span: 24
        },
        itemProps:{
            labelCol: { xs: 1, sm: 1 },
            wrapperCol: { xs: 23, sm: 23 },
            colon: false
        },
    },*/
  {
    label: 'Báo cáoSQL',
    field: 'cgrSql',
    component: 'JCodeEditor',
    rules: [{ required: true, message: 'Vui lòng điền báo cáoSQL' }],
    // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
    // itemProps: {
    //   labelCol: { xs: 24, sm: 4, md: 2, lg: 2, xl: 3, xxl: 2 },
    //   wrapperCol: { xs: { span: 24 }, sm: { span: 18 }, md: { span: 24 } },
    // },
    // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
    componentProps: {
      height: '200px',
      fullScreen: true,
    },
    colProps: {
      sm: 24,
      xs: 24,
      md: 18,
      lg: 16,
      xl: 16,
      xxl: 16,
    },
  },
  {
    label: ' ',
    field: 'analyseButton',
    component: 'Input',
    slot: 'analyseButton',
    colProps: {
      xs: 24,
      sm: 24,
      md: 6,
      lg: 8,
      xl: 8,
      xxl: 8,
    },
    itemProps: {
      labelCol: { xs: 1, sm: 1 },
      wrapperCol: { xs: 23, sm: 23 },
      colon: false,
    },
  },
];
//Cấu hình bảng dữ liệu của bảng con
export const onlCgreportParamColumns: JVxeColumn[] = [
  {
    title: 'Trường tham số',
    key: 'paramName',
    type: JVxeTypes.input,
    width: '150px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
    validateRules: [{ required: true, message: '${title}không được để trống' }],
  },
  {
    title: 'Văn bản tham số',
    key: 'paramTxt',
    type: JVxeTypes.input,
    width: '150px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
    validateRules: [{ required: true, message: '${title}không được để trống' }],
  },
  {
    title: 'Giá trị mặc định',
    key: 'paramValue',
    type: JVxeTypes.input,
    width: '150px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
];
export const onlCgreportItemColumns: JVxeColumn[] = [
  {
    title: 'Tên trường',
    key: 'fieldName',
    type: JVxeTypes.input,
    width: '160px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
    validateRules: [{ required: true, message: '${title}không được để trống' }],
  },
  {
    title: 'Văn bản trường',
    key: 'fieldTxt',
    type: JVxeTypes.input,
    width: '160px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
    validateRules: [{ required: true, message: '${title}không được để trống' }],
  },
  {
    title: 'Độ rộng',
    key: 'fieldWidth',
    type: JVxeTypes.input,
    width: '80px',
    defaultValue: '',
  },
  {
    title: 'Loại',
    key: 'fieldType',
    width: '120px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
    validateRules: [{ required: true, message: '${title}không được để trống' }],
    type: JVxeTypes.select,
    options: [
      { title: 'Loại giá trị số', value: 'Integer' },
      { title: 'Loại ký tự', value: 'String' },
      { title: 'Loại ngày', value: 'Date' },
      { title: 'Loại thời gian', value: 'Datetime' },
      { title: 'Số nguyên dài (long)', value: 'Long' },
      { title: 'Loại hình ảnh', value: 'Image' },
    ],
  },
  {
    title: 'Hiển thị cột',
    key: 'isShow',
    width: '80px',
    align: 'center',
    type: JVxeTypes.checkbox,
    customValue: [1, 0],
    defaultChecked: true,
  },
  {
    title: 'trườnghref',
    key: 'fieldHref',
    type: JVxeTypes.input,
    width: '120px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'Truy vấn',
    key: 'isSearch',
    type: JVxeTypes.checkbox,
    customValue: ['1', '0'],
    width: '80px',
    align: 'center',
    defaultChecked: false,
  },
  {
    title: 'Chế độ truy vấn',
    key: 'searchMode',
    type: JVxeTypes.select,
    width: '120px',
    placeholder: 'Vui lòng chọn${title}',
    options: [
      { title: 'Truy vấn giá trị đơn', value: 'single' },
      { title: 'Truy vấn theo phạm vi', value: 'group' },
    ],
  },
  {
    title: 'Biểu thức lấy giá trị',
    key: 'replaceVal',
    type: JVxeTypes.input,
    width: '120px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'từ điểncode',
    key: 'dictCode',
    type: JVxeTypes.input,
    width: '120px',
    titleSuffix: { content: 'Điền mã từ điển, hoặcSQL：SELECT trường AS value, trường AS text FROM bảng. KingbaseES,PostgreSQL dưới text là từ khóa, bí danh bắt buộc phải viết AS' },
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'Tiêu đề nhóm',
    key: 'groupTitle',
    type: JVxeTypes.input,
    width: '120px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'Cột tổng cộng',
    align: 'center',
    key: 'isTotal',
    type: JVxeTypes.checkbox,
    customValue: ['1', '0'],
    width: '80px',
    defaultChecked: false,
  },
];
