import { Ref } from 'vue';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { findTree } from '/@/utils/common/compUtils';

// Thông tin người dùng columns
export const userInfoColumns: BasicColumn[] = [
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
    width: 150,
  },
  {
    title: 'Tên người dùng',
    dataIndex: 'realname',
    width: 180,
  },
  {
    title: 'Phòng ban',
    dataIndex: 'orgCode',
    width: 200,
  },
  {
    title: 'Giới tính',
    dataIndex: 'sex_dictText',
    width: 80,
  },
  {
    title: 'Điện thoại',
    dataIndex: 'phone',
    width: 120,
  },
];

// Biểu mẫu điều kiện truy vấn thông tin người dùng
export const userInfoSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'Tài khoản người dùng',
    component: 'Input',
  },
  {
    field: 'realname',
    label: 'Họ tên người dùng',
    component: 'Input',
  },
];

// Vai trò phòng ban columns
export const departRoleColumns: BasicColumn[] = [
  {
    title: 'Tên vai trò phòng ban',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: 'Mã vai trò phòng ban',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: 'Phòng ban',
    dataIndex: 'departId_dictText',
    width: 100,
  },
  {
    title: 'Ghi chú',
    dataIndex: 'description',
    width: 100,
  },
];

// Biểu mẫu điều kiện truy vấn vai trò phòng ban
export const departRoleSearchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: 'Tên vai trò phòng ban',
    component: 'JInput',
  },
];

// Biểu mẫu form trong hộp thoại vai trò phòng ban
export const departRoleModalFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'roleName',
    label: 'Tên vai trò phòng ban',
    component: 'Input',
    rules: [
      { required: true, message: 'Tên vai trò phòng ban không được để trống!' },
      { min: 2, max: 30, message: 'Độ dài từ 2 đến 30 ký tự', trigger: 'blur' },
    ],
  },
  {
    field: 'roleCode',
    label: 'Mã vai trò phòng ban',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        { required: true, message: 'Mã vai trò phòng ban không được để trống!' },
        { min: 0, max: 64, message: 'Độ dài không được vượt quá 64 ký tự', trigger: 'blur' },
        {
          validator: (_, value) => {
            if (/[\u4E00-\u9FA5]/g.test(value)) {
              return Promise.reject('Mã vai trò phòng ban không được nhập ký tự Hán!');
            }
            return new Promise((resolve, reject) => {
              let params = {
                tableName: 'sys_depart_role',
                fieldName: 'role_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || 'Kiểm tra không hợp lệ');
                })
                .catch((err) => {
                  reject(err.message || 'Xác thực thất bại');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'description',
    label: 'Mô tả',
    component: 'Input',
    rules: [{ min: 0, max: 126, message: 'Độ dài không được vượt quá 126 ký tự', trigger: 'blur' }],
  },
];

// Thông tin cơ bảnform
export function useBaseInfoForm(treeData: Ref<any[]>) {
  const descItems: DescItem[] = [
    {
      field: 'departName',
      label: 'Tên cơ quan',
    },
    {
      field: 'parentId',
      label: 'Phòng ban cấp trên',
      render(val) {
        if (val) {
          let data = findTree(treeData.value, (item) => item.key == val);
          return data?.title ?? val;
        }
        return val;
      },
    },
    {
      field: 'orgCode',
      label: 'Mã cơ quan',
    },
    {
      field: 'orgCategory',
      label: 'Loại cơ quan',
      render(val) {
        if (val === '1') {
          return 'Công ty';
        } else if (val === '2') {
          return 'Phòng ban';
        } else if (val === '3') {
          return 'Vị trí công tác';
        } else if(val === '4'){
          return 'Công ty con';
        }
        return val;
      },
    },
    {
      field: 'departOrder',
      label: 'Sắp xếp',
    },

    {
      field: 'mobile',
      label: 'Số điện thoại',
    },
    {
      field: 'address',
      label: 'Địa chỉ',
    },
    {
      field: 'memo',
      label: 'Ghi chú',
    },
  ];

  return { descItems };
}
