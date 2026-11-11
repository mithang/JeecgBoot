import { FormSchema } from '/@/components/Table';
import { isRoleExist } from './role.api';
import { useDrawer, useDrawerInner } from '@/components/Drawer';
export const columns = [
  {
    title: 'Tên vai trò',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: 'Mã vai trò',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 100,
  },
];
/**
 * Người dùng của vai tròColumns
 */
export const userColumns = [
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
  },
  {
    title: 'Họ tên người dùng',
    dataIndex: 'realname',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status_dictText',
    width: 80,
  },
];
export const searchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: 'Tên vai trò',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'roleCode',
    label: 'Mã vai trò',
    component: 'Input',
    colProps: { span: 6 },
  },
];

/**
 * Truy vấn vai trò đơn vị thuê
 */
export const searchTenantRoleFormSchema: FormSchema[] = [
    {
        field: 'roleName',
        label: 'Tên vai trò',
        component: 'JInput',
        colProps: { span: 6 },
    },
    {
        field: 'roleCode',
        label: 'Mã vai trò',
        component: 'JInput',
        colProps: { span: 6 },
    },
];

/**
 * Tìm kiếm người dùng của vai tròform
 */
export const searchUserFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'Tài khoản người dùng',
    component: 'JInput',
    colProps: { span: 8 },
    labelWidth: 74,
  },
  {
    field: 'realname',
    label: 'Tên người dùng',
    component: 'JInput',
    colProps: { span: 8 },
    labelWidth: 74,
  },
];

/**
 * Tìm kiếm người dùng của vai trò
 */
export const roleUserSearchFormSchema: FormSchema[] = [
    {
        field: 'username',
        label: 'Tài khoản người dùng',
        component: 'Input',
        colProps: { span: 8 },
        labelWidth: 74,
    },
    {
        field: 'realname',
        label: 'Tên người dùng',
        component: 'Input',
        colProps: { span: 8 },
        labelWidth: 74,
    },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    field: 'roleName',
    label: 'Tên vai trò',
    required: true,
    component: 'Input',
  },
  {
    field: 'roleCode',
    label: 'Mã vai trò',
    required: true,
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ values, model }) => {
      console.log('values:', values);
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('Vui lòng nhập mã vai trò');
            }
            if (values) {
              return new Promise((resolve, reject) => {
                isRoleExist({ id: model.id, roleCode: value })
                  .then((res) => {
                    res.success ? resolve() : reject(res.message || 'Kiểm tra không hợp lệ');
                  })
                  .catch((err) => {
                    reject(err.message || 'Xác thực thất bại');
                  });
              });
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },
  {
    label: 'Ghi chú',
    field: 'description',
    component: 'InputTextArea',
  },
];

export const formDescSchema = [
  {
    field: 'roleName',
    label: 'Tên vai trò',
  },
  {
    field: 'roleCode',
    label: 'Mã vai trò',
  },
  {
    label: 'Ghi chú',
    field: 'description',
  },
];

export const roleIndexFormSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    label: 'Mã vai trò',
    field: 'roleCode',
    component: 'Input',
    dynamicDisabled: true,
  },
  {
    label: 'Định tuyến trang chủ',
    field: 'url',
    component: 'Input',
    required: true,
    helpMessage: 'Địa chỉ truy cập của định tuyến trang chủ',
  },
  {
    label: 'Địa chỉ component',
    field: 'component',
    component: 'Input',
    helpMessage: 'Địa chỉ component của định tuyến trang chủ',
    componentProps: {
      placeholder: 'Vui lòng nhập component frontend',
    },
    required: true,
  },
  {
    field: 'route',
    label: 'Có phải menu định tuyến không',
    helpMessage: 'Menu phi định tuyến được đặt làm trang chủ thì cần bật',
    component: 'Switch',
    defaultValue: true
  },
  {
    label: 'Mức ưu tiên',
    field: 'priority',
    component: 'InputNumber',
  },
  {
    label: 'Có bật không',
    field: 'status',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
