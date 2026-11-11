import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
// Độ dài tối đa của tên
export const NAME_MAX_LENGTH = 40;

//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Tên nhóm người dùng',
    align: 'center',
    dataIndex: 'groupName',
  },
  {
    title: 'Thời gian tạo',
    align: 'center',
    dataIndex: 'createTime',
  },
];
//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên nhóm người dùng',
    field: 'groupName',
    component: 'JInput',
  },
];

//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: 'Tên nhóm người dùng',
    field: 'groupName',
    component: 'Input',
    dynamicRules() {
      return [
        {required: true, message: 'Vui lòng nhập tên nhóm người dùng'},
        {
          max: NAME_MAX_LENGTH,
          message: `Độ dài tên không được vượt quá ${NAME_MAX_LENGTH} ký tự`,
        },
      ];
    }
  },
  {
    label: 'Mô tả',
    field: 'description',
    component: 'InputTextArea',
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

/**
 * Tìm kiếm người dùng của vai tròform
 */
export const searchUserFormSchema: FormSchema[] = [
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
