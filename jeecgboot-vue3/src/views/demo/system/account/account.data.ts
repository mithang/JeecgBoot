import { getAllRoleList, isAccountExist } from '/@/api/demo/system';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tên người dùng',
    dataIndex: 'account',
    width: 120,
  },
  {
    title: 'Biệt danh',
    dataIndex: 'nickname',
    width: 120,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    width: 120,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: 'Vai trò',
    dataIndex: 'role',
    width: 200,
  },
  {
    title: 'Ghi chú',
    dataIndex: 'remark',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'account',
    label: 'Tên người dùng',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'nickname',
    label: 'Biệt danh',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const accountFormSchema: FormSchema[] = [
  {
    field: 'account',
    label: 'Tên người dùng',
    component: 'Input',
    helpMessage: ['Trường này minh họa kiểm tra không đồng bộ', 'Không thể nhập tên người dùng chứa admin'],
    rules: [
      {
        required: true,
        message: 'Vui lòng nhập tên người dùng',
      },
      {
        validator(_, value) {
          return new Promise((resolve, reject) => {
            isAccountExist(value)
              .then(() => resolve())
              .catch((err) => {
                reject(err.message || 'Kiểm tra thất bại');
              });
          });
        },
      },
    ],
  },
  {
    field: 'pwd',
    label: 'Mật khẩu',
    component: 'InputPassword',
    required: true,
    ifShow: false,
  },
  {
    label: 'Vai trò',
    field: 'role',
    component: 'ApiSelect',
    componentProps: {
      api: getAllRoleList,
      labelField: 'roleName',
      valueField: 'roleValue',
    },
    required: true,
  },
  {
    field: 'dept',
    label: 'Phòng ban trực thuộc',
    component: 'TreeSelect',
    componentProps: {
      fieldNames: {
        label: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
    required: true,
  },
  {
    field: 'nickname',
    label: 'Biệt danh',
    component: 'Input',
    required: true,
  },

  {
    label: 'Email',
    field: 'email',
    component: 'Input',
    required: true,
  },

  {
    label: 'Ghi chú',
    field: 'remark',
    component: 'InputTextArea',
  },
];
