import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Icon } from '/@/components/Icon';
import { duplicateCheck } from '../user/user.api';
import { ajaxGetDictItems ,checkPermDuplication } from './menu.api';
import { render } from '/@/utils/common/renderUtils';

const isDir = (type) => type === 0;
const isMenu = (type) => type === 1;
const isButton = (type) => type === 2;

// Định nghĩa loại component có thể chọn
export enum ComponentTypes {
  Default = 'layouts/default/index',
  IFrame = 'sys/iframe/FrameBlank',
}

export const columns: BasicColumn[] = [
  {
    title: 'Tên menu',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'Loại menu',
    dataIndex: 'menuType',
    width: 150,
    customRender: ({ text }) => {
      return render.renderDict(text, 'menu_type');
    },
  },
  {
    title: 'Biểu tượng',
    dataIndex: 'icon',
    width: 50,
    customRender: ({ record }) => {
      return h(Icon, { icon: record.icon });
    },
  },
  {
    title: 'Component',
    dataIndex: 'component',
    align: 'left',
    width: 150,
  },
  {
    title: 'Đường dẫn',
    dataIndex: 'url',
    align: 'left',
    width: 150,
  },
  {
    title: 'Sắp xếp',
    dataIndex: 'sortNo',
    width: 50,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tên menu',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'menuType',
    label: 'Loại menu',
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: [
          { label: 'Menu cấp một', value: 0 },
          { label: 'Menu con', value: 1 },
          { label: 'Nút/Quyền hạn', value: 2 },
        ],
        onChange: (e) => {
          const { updateSchema, clearValidate } = formActionType;
          const label = isButton(e) ? 'Nút/Quyền hạn' : 'Tên menu';
          //Xóa kiểm tra
          clearValidate();
          updateSchema([
            {
              field: 'name',
              label: label,
            },
            {
              field: 'url',
              required: !isButton(e),
            },
          ]);
          // Giải thích logic mã: [VUEN-1834]Chỉ menu cấp một mới có giá trị mặc định, khi là menu con thì xóa trống------------
          if (isMenu(e) && !formModel.id && (formModel.component=='layouts/default/index' || formModel.component=='layouts/RouteView')) {
            formModel.component = '';
          }
        },
      };
    },
  },
  {
    field: 'name',
    label: 'Tên menu',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentId',
    label: 'Menu cấp trên',
    component: 'TreeSelect',
    required: true,
    componentProps: {
      // Giải thích logic mã: replaceFields đã lỗi thời, dùng fieldNames thay thế------------
      fieldNames: {
        label: 'name',
        key: 'id',
        value: 'id',
      },
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: (node) => node?.parentNode,
    },
    ifShow: ({ values }) => !isDir(values.menuType),
  },
  {
    field: 'url',
    label: 'Đường dẫn truy cập',
    component: 'Input',
    required: true,
    // Giải thích logic mã: [issues/5008]Cài đặt quyền dữ liệu của bảng con không có hiệu lực
    ifShow: ({ values }) => !(values.component === ComponentTypes.IFrame && values.internalOrExternal),
    // Giải thích logic mã: Định tuyến tổng hợp cho phép đường dẫn trùng lặp
     dynamicRules: ({ model, schema,values }) => {
       return checkPermDuplication(model, schema,  values.menuType !== 2?true:false);
    },
  },
  {
    field: 'component',
    label: 'Component frontend',
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập component frontend',
    },
    defaultValue:'layouts/default/index',
    required: true,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'componentName',
    label: 'Tên component',
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập tên component',
    },
    helpMessage: [
      'Tên ở đây phải giữ nhất quán với thuộc tính name của component Vue.',
      'Tên component không được trùng lặp, chủ yếu dùng cho chức năng bộ nhớ đệm định tuyến.',
      'Nếu tên component và thuộc tính name của component Vue không nhất quán thì sẽ khiến bộ nhớ đệm định tuyến mất hiệu lực.',
      'Không bắt buộc, để trống sẽ tự động tạo theo đường dẫn truy cập.',
    ],
    defaultValue: '',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'frameSrc',
    label: 'IframeĐịa chỉ',
    component: 'Input',
    rules: [
      { required: true, message: 'Vui lòng nhập địa chỉ Iframe' },
      { type: 'url', message: 'Vui lòng nhập địa chỉ URL đúng' },
    ],
    ifShow: ({ values }) => !isButton(values.menuType) && values.component === ComponentTypes.IFrame,
  },
  {
    field: 'redirect',
    label: 'Địa chỉ chuyển hướng mặc định',
    component: 'Input',
    ifShow: ({ values }) => isDir(values.menuType),
  },
  {
    field: 'perms',
    label: 'Dấu hiệu cấp quyền',
    component: 'Input',
    ifShow: ({ values }) => isButton(values.menuType),
    // dynamicRules: ({ model }) => {
    //   return [
    //     {
    //       required: false,
    //       validator: (_, value) => {
    //         return new Promise((resolve, reject) => {
    //           let params = {
    //             tableName: 'sys_permission',
    //             fieldName: 'perms',
    //             fieldVal: value,
    //             dataId: model.id,
    //           };
    //           duplicateCheck(params)
    //             .then((res) => {
    //               res.success ? resolve() : reject(res.message || 'Kiểm tra không hợp lệ');
    //             })
    //             .catch((err) => {
    //               reject(err.message || 'Kiểm tra không hợp lệ');
    //             });
    //         });
    //       },
    //     },
    //   ];
    // },
  },
  {
    field: 'permsType',
    label: 'Chiến lược cấp quyền',
    component: 'RadioGroup',
    defaultValue: '1',
    helpMessage: ['Hiển thị/Có thể truy cập(Sau khi cấp quyền mới hiển thị/có thể truy cập)', 'Có thể chỉnh sửa(Vô hiệu hóa khi chưa cấp quyền)'],
    componentProps: {
      options: [
        { label: 'Hiển thị/Có thể truy cập', value: '1' },
        { label: 'Có thể chỉnh sửa', value: '2' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'RadioGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: 'Hợp lệ', value: '1' },
        { label: 'Không hợp lệ', value: '0' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'icon',
    label: 'Biểu tượng menu',
    component: 'IconPicker',
    ifShow: ({ values }) => !isButton(values.menuType),
    componentProps: {
      allowClear: true
    },
  },
  {
    field: 'sortNo',
    label: 'Sắp xếp',
    component: 'InputNumber',
    defaultValue: 1,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'route',
    label: 'Có phải menu định tuyến không',
    component: 'Switch',
    defaultValue: true,
    componentProps: {
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hidden',
    label: 'Ẩn định tuyến',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hideTab',
    label: 'ẨnTab',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'keepAlive',
    label: 'Có lưu đệm định tuyến không',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'alwaysShow',
    label: 'Định tuyến tổng hợp',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'internalOrExternal',
    label: 'Cách mở',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'Bên ngoài',
      unCheckedChildren: 'Nội bộ',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
];

export const dataRuleColumns: BasicColumn[] = [
  {
    title: 'Tên quy tắc',
    dataIndex: 'ruleName',
    width: 150,
  },
  {
    title: 'Trường quy tắc',
    dataIndex: 'ruleColumn',
    width: 100,
  },
  {
    title: 'Giá trị quy tắc',
    dataIndex: 'ruleValue',
    width: 100,
  },
];

export const dataRuleSearchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: 'Tên quy tắc',
    component: 'JInput',
    // colProps: { span: 6 },
  },
  {
    field: 'ruleValue',
    label: 'Giá trị quy tắc',
    component: 'JInput',
    // colProps: { span: 6 },
  },
];

export const dataRuleFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'ruleName',
    label: 'Tên quy tắc',
    component: 'Input',
    required: true,
  },
  {
    field: 'ruleColumn',
    label: 'Trường quy tắc',
    component: 'Input',
    ifShow: ({ values }) => {
      const ruleConditions = Array.isArray(values.ruleConditions) ? values.ruleConditions[0] : values.ruleConditions;
      return ruleConditions !== 'USE_SQL_RULES';
    },
  },
  {
    field: 'ruleConditions',
    label: 'Quy tắc điều kiện',
    required: true,
    component: 'ApiSelect',
    componentProps: {
      api: ajaxGetDictItems,
      params: { code: 'rule_conditions' },
      labelField: 'text',
      valueField: 'value',
      getPopupContainer: (node) => document.body,
    },
  },
  // Giải thích logic mã: 【TV360X-1864】Thêm biến hệ thống
  {
    field: 'ruleValue',
    component: 'JInputSelect',
    label: 'Giá trị quy tắc',
    required: true,
    componentProps: {
      selectPlaceholder: 'Có thể chọn biến hệ thống',
      inputPlaceholder: 'Vui lòng nhập',
      getPopupContainer: () => document.body,
      selectWidth: '200px',
      options: [
        {
          label: 'Tài khoản của người dùng đăng nhập',
          value: '#{sys_user_code}',
        },
        {
          label: 'Tên người dùng đăng nhập',
          value: '#{sys_user_name}',
        },
        {
          label: 'Ngày hiện tại',
          value: '#{sys_date}',
        },
        {
          label: 'Thời gian hiện tại',
          value: '#{sys_time}',
        },
        {
          label: 'Phòng ban của người dùng đăng nhập',
          value: '#{sys_org_code}',
        },
        {
          label: 'Phòng ban người dùng sở hữu',
          value: '#{sys_multi_org_code}',
        },
        {
          label: 'Đơn vị thuê của người dùng đăng nhập',
          value: '#{tenant_id}',
        },
      ],
    },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: 'Không hợp lệ', value: '0' },
        { label: 'Hợp lệ', value: '1' },
      ],
    },
  },
];
