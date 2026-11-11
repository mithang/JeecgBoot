import { BasicColumn, FormSchema } from '/@/components/Table';
import { getAutoScrollContainer } from '/@/utils/common/compUtils';
import { render } from "/@/utils/common/renderUtils";
import { rules } from "/@/utils/helper/validator";

export const columns: BasicColumn[] = [
  {
    title: 'Tên đơn vị thuê',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'Mã đơn vị thuê(ID)',
    dataIndex: 'id',
    width: 180,
  },{
    title: 'Tổ chứcLOGO',
    dataIndex: 'companyLogo',
    width: 100,
    customRender: ({ text }) => {
      if(!text){
        return text;
      }
      return render.renderImage({text});
    },
  },
  {
    dataIndex: 'trade_dictText',
    title: 'Ngành nghề',
    width: 150
  },
  {
    dataIndex: 'companySize_dictText',
    title: 'Quy mô công ty',
    width: 100
  },
  {
    dataIndex: 'houseNumber',
    title: 'Số nhà',
    width: 100,
  },
  // {
  //   dataIndex: 'position_dictText',
  //   title: 'Cấp bậc',
  //   width: 150
  // },
  // {
  //   dataIndex: 'department_dictText',
  //   title: 'Phòng ban',
  //   width: 150
  // },
  {
    dataIndex: 'createBy_dictText',
    title: 'Người tạo(Sở hữu)',
    width: 150
  },
/*  {
    title: 'Thời gian bắt đầu',
    dataIndex: 'beginDate',
    sorter: true,
    width: 180,
  },
  {
    title: 'Thời gian kết thúc',
    dataIndex: 'endDate',
    sorter: true,
    width: 180,
  },*/
  {
    title: 'Trạng thái',
    dataIndex: 'status_dictText',
    width: 100,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tên đơn vị thuê',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Bình thường', value: 1 },
        { label: 'Đóng băng', value: 0 },
      ],
    },
    colProps: { span: 8 },
  },
  // {
  //   field: 'fieldTime',
  //   component: 'RangePicker',
  //   label: 'Trường thời gian',
  //   componentProps: {
  //     valueType: 'Date',
  //   },
  //   colProps: {
  //     span: 8,
  //   },
  // },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tên đơn vị thuê',
    component: 'Input',
    required: true,
  },
  {
    field: 'id',
    label: 'Mã đơn vị thuê(ID)',
    component: 'InputNumber',
    required: true,
    ifShow: ({ values }) => {
      return values.id!=null;
    },
  },
  {
    field: 'companyLogo',
    label: 'Tổ chứcLOGO',
    component: 'JImageUpload',
    componentProps:{
      text:'logo'
    }
  },
  {
    field: 'trade',
    label: 'Ngành nghề',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode:'trade',
    }
  }, {
    field: 'companySize',
    label: 'Quy mô công ty',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode:'company_size',
    }
  }, {
    field: 'companyAddress',
    label: 'Địa chỉ công ty',
    component: 'JAreaSelect',
    componentProps: {
      placeholder: 'Vui lòng nhập địa chỉ công ty',
      rows: 4,
    }
  },
  {
    field: 'workPlace',
    label: 'Địa điểm làm việc',
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Vui lòng nhập địa điểm làm việc',
      rows: 4,
    }
  },
/*  {
    field: 'beginDate',
    label: 'Thời gian bắt đầu',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: getAutoScrollContainer,
    },
  },
  {
    field: 'endDate',
    label: 'Thời gian kết thúc',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: getAutoScrollContainer,
    },
  },*/
  {
    field: 'houseNumber',
    label: 'Số nhà',
    component: 'Input',
    dynamicDisabled: true,
    ifShow: ({ values }) => {
      return values.id!=null;
    },
  },
  {
    field: 'position',
    label: 'Cấp bậc',
    component: 'JDictSelectTag',
    componentProps:{
      dictCode: 'company_rank'
    }
  },
  {
    field: 'department',
    label: 'Phòng ban',
    component: 'JDictSelectTag',
    componentProps:{
      dictCode:'company_department'
    }
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: 'Bình thường', value: 1 },
        { label: 'Đóng băng', value: 0 },
      ],
    },
  },
];

//Định nghĩa cột bảng người dùng
export const userColumns: BasicColumn[] =[
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
    width: 100,
    align: 'left',
  },
  {
    title: 'Họ tên người dùng',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'Giới tính',
    dataIndex: 'sex_dictText',
    width: 100,
  },
  {
    title: 'Số điện thoại',
    dataIndex: 'phone',
    width: 100,
  },
];

//Biểu mẫu tìm kiếm mời người dùng
export const userSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'Tài khoản',
    component: 'Input',
  },
  {
    field: 'realname',
    label: 'Họ tên',
    component: 'Input',
  },
];

//Danh sách gói dịch vụ
export const packColumns: BasicColumn[] = [
  {
    title: 'Tên gói dịch vụ',
    dataIndex: 'packName',
    width: 100,
    customRender: ( { record, text }) => {
      if(record.packCode && record.packCode.indexOf('default') != -1) {
        return text + '(Gói sản phẩm mặc định)';
      } else {
        return text;
      }
    }
  },
  {
    title: 'Có tự động phân bổ người dùng không',
    dataIndex: 'izSysn',
    width: 120,
    customRender: ( { text }) => {
      if(text === '1') {
        return 'Có';
      } else {
        return 'Không';
      }
    }
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'Bật';
      } else {
        return 'Đóng';
      }
    },
  },
  {
    title: 'Mô tả ghi chú',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Danh sách gói dịch vụ
export const tenantPackColumns: BasicColumn[] = [
  {
    title: 'Tên gói dịch vụ',
    dataIndex: 'packName',
    width: 100,
    customRender: ( { record, text }) => {
      if(record.packCode && record.packCode.indexOf('default') != -1) {
        return text + '(Gói sản phẩm mặc định)';
      } else {
        return text;
      }
    }
  },
  {
    title: 'Có tự động phân bổ người dùng không',
    dataIndex: 'izSysn',
    width: 100,
    customRender: ( { text }) => {
      if(text === '1') {
        return 'Có';
      } else {
        return 'Không';
      }
    }
  },
  {
    title: 'Mô tả ghi chú',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Danh sách gói dịch vụ
export const defalutPackColumns: BasicColumn[] = [
  {
    title: 'Tên gói dịch vụ mặc định',
    dataIndex: 'packName',
    width: 100,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'Bật';
      } else {
        return 'Đóng';
      }
    },
  },
  {
    title: 'Mô tả ghi chú',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Biểu mẫu tìm kiếm gói dịch vụ
export const packFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Tên gói dịch vụ',
    component: 'JInput',
    colProps: { xxl: 8 },
  },
];

//Biểu mẫu tìm kiếm gói dịch vụ
export const defaultPackFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Tên gói mặc định',
    component: 'JInput',
    colProps: { xxl: 8 },
  },
];

//Biểu mẫu gói dịch vụ
export const packMenuFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Tên gói dịch vụ',
    component: 'Input',
  },
/*  {
    field: 'permissionIds',
    label: 'Menu cấp quyền',
    component: 'JTreeSelect',
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField:'is_leaf',
      multiple: true,
      treeCheckAble:true,
      treeCheckStrictly: true,
      converIsLeafVal: 0,
      getPopupContainer: () => document.body,
    },
  },*/
  {
    field: 'remarks',
    label: 'Mô tả ghi chú',
    component: 'InputTextArea',
  },
  {
    field: 'izSysn',
    label: 'Tự động phân bổ người dùng',
    component: 'Switch',
    componentProps: {
      checkedValue: "1",
      checkedChildren: 'Có',
      unCheckedValue: "0",
      unCheckedChildren: 'Không',
    },
    defaultValue: "1",
    helpMessage: "Mặc định sẽ tự động phân bổ cho người dùng; gói nâng cao cá nhân hóa cần quản trị viên đơn vị thuê phân bổ nhân viên thủ công(Sở hữu kiểm soát quyền linh hoạt hơn)"
  },  
  {
    field: 'status',
    label: 'Trạng thái bật',
    component: 'Switch',
    componentProps: {
      checkedValue: '1',
      checkedChildren: 'Bật',
      unCheckedValue: '0',
      unCheckedChildren: 'Đóng',
    },
    defaultValue: '1',
  },
  {
    field: 'id',
    label: 'Trạng thái bật',
    component: 'Input',
    show: false
  },
  {
    field: 'packCode',
    label: 'Mã gói sản phẩm',
    component: 'Input',
    show: false
  },  
  {
    field: 'packType',
    label: 'Loại gói sản phẩm',
    component: 'Input',
    show: false
  },
];

//Danh sách thùng rác
export const recycleColumns : BasicColumn[] = [
  {
    title: 'Tên đơn vị thuê',
    dataIndex: 'name',
    width: 100,
    align: 'left',
  },
  {
    title: 'Mã đơn vị thuê(ID)',
    dataIndex: 'id',
    width: 100,
  },
  {
    title: 'Tổ chứcLOGO',
    dataIndex: 'companyLogo',
    width: 100,
    customRender: ({ text }) => {
      if(!text){
        return text;
      }
      return render.renderImage({text});
    },
  },
  {
    dataIndex: 'houseNumber',
    title: 'Số nhà',
    width: 100,
  }
]

//Biểu mẫu tìm kiếm thùng rác đơn vị thuê
export const searchRecycleFormSchema : FormSchema[] = [
  {
    field: 'name',
    label: 'Tên đơn vị thuê',
    component: 'Input',
  },
  {
    field: 'houseNumber',
    label: 'Số nhà',
    component: 'Input',
  },
]

//Danh sách người dùng của gói dịch vụ
export const tenantPackUserColumns: BasicColumn[] = [
  {
    title: 'Người dùng',
    dataIndex: 'realname',
    width: 200,
  },
  {
    title: 'Phòng ban',
    dataIndex: 'departNames',
    width: 200,
    ellipsis: true,
    slots: { customRender: 'departNames' }
  },
  {
    title: 'Chức vụ',
    dataIndex: 'positionNames',
    ellipsis: true,
    width: 200,
    slots: { customRender: 'positionNames' }
  }
]

/**
 * Biểu mẫu thêm mới/chỉnh sửa đơn vị thuê của người dùng
 */
export const tenantUserSchema: FormSchema[] = [
  { field: 'id', label: 'id', component: 'Input', show: false },
  { field: 'username', label: 'username', component: 'Input', show: false },
  {
    field: 'realname',
    label: 'Họ tên',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'phone',
    label: 'Điện thoại',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      if (model.id) {
        return [];
      }
      return [{ ...rules.phone(true)[0] }, { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, false)[0] }];
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'email',
    label: 'Email',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      if (model.id) {
        return [];
      }
      return [{ ...rules.email(true)[0] }, { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, false)[0] }];
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  { field: 'selecteddeparts', label: 'Phòng ban', component: 'JSelectDept', componentProps: { checkStrictly: true } },
 /* {
    field: 'post',
    label: 'Chức vụ',
    component: 'JSelectPosition',
  },
  {
    field: 'workNo',
    label: 'Mã nhân viên',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [{ required: false, message: 'Vui lòng nhập mã nhân viên' }, { ...rules.duplicateCheckRule('sys_user', 'work_no', model, schema, false)[0] }];
    },
  },*/
  { field: 'relTenantIds', label: 'Đơn vị thuê (tenant)', component: 'Input',show:false },
  { field: 'selectedroles', label: 'Vai trò', component: 'Input',show:false },
];

// Phân bổ gói cho người dùng
export const packUserAllotSchemas: FormSchema[] = [
  { 
    field: 'userId', 
    label: 'Người dùngid', 
    component: 'Input',
    show: false
  },
  {
    field: 'realname',
    label: 'Họ tên người dùng',
    component: 'Input',
    componentProps:{
      readonly : true
    },
  },
  {
    field: 'packId',
    label: 'Gói dịch vụ',
    component: 'Select',
    slot: 'packId'
  }
];