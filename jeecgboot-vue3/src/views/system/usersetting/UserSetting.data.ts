import { FormSchema } from '/@/components/Form/index';
import { rules } from '/@/utils/helper/validator';
import anquan1 from './icons/anquan1.png'
import anquan2 from './icons/anquan2.png'
import app1 from './icons/app1.png'
import app2 from './icons/app2.png'
import geren1 from './icons/geren1.png'
import geren2 from './icons/geren2.png'
import zuhu1 from './icons/zuhu1.png'
import zuhu2 from './icons/zuhu2.png'
import { calculateFileSize } from "/@/utils/common/compUtils";
import { BasicColumn } from "@/components/Table";

export interface ListItem {
  key: string;
  title: string;
  description: string;
  extra?: string;
  avatar?: string;
  color?: string;
}

// tabcủalist
export const settingList = [
  {
    key: '1',
    name: 'Thông tin cá nhân',
    component: 'BaseSetting',
    icon:'ant-design:user-outlined',
    img1: geren1,
    img2: geren2,
  },
  {
    key: '2',
    name: 'Tổ chức của tôi',
    component: 'TenantSetting',
    isSlot:false,
    icon:'ant-design:team-outlined',
    img1: zuhu1,
    img2: zuhu2,
  },
   {
    key: '3',
    name: 'An toàn tài khoản',
    component: 'AccountSetting',
    icon:'ant-design:lock-outlined',
    img1: anquan1,
    img2: anquan2,
  },
  {
    key: '4',
    name: 'Bên thứ baAPP',
    component: 'WeChatDingSetting',
    icon: 'ant-design:contacts-outlined',
    img1: app1,
    img2: app2,
  },
];


/**
 * Biểu mẫu người dùng
 */
export const formSchema: FormSchema[] = [
  {
    field: 'realname',
    component: 'Input',
    label: 'Họ tên',
    colProps: { span: 24 },
    required:true
  },
  {
    field: 'birthday',
    component: 'DatePicker',
    label: 'Ngày sinh',
    colProps: { span: 24 },
    componentProps:{
      showTime:false,
      valueFormat:"YYYY-MM-DD",
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'sex',
    component: 'RadioGroup',
    label: 'Giới tính',
    colProps: { span: 24 },
    componentProps:{
      options: [
        {
          label: 'Nam',
          value: 1,
        },
        {
          label: 'Nữ',
          value: 2,
        },
      ],
    }
  },
  {
    field: 'relTenantIds',
    component: 'JDictSelectTag',
    label: 'Đơn vị thuê (tenant)',
    colProps: { span: 24 },
    componentProps:{
      mode:'multiple',
      dictCode:'sys_tenant,name,id',
      disabled:true
    }
  },
  {
    field: 'post',
    component: 'JDictSelectTag',
    label: 'Chức vụ',
    colProps: { span: 24 },
    componentProps:{
      mode:'multiple',
      dictCode:'sys_position,name,id',
      disabled:true
    }
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
]

//Hộp thoại mật khẩu
export const formPasswordSchema: FormSchema[] = [
  {
    label: 'Tài khoản người dùng',
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Mật khẩu cũ',
    field: 'oldpassword',
    component: 'InputPassword',
    required: true,
  },
  {
    label: 'Mật khẩu mới',
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: 'Vui lòng nhập mật khẩu mới',
    },
    rules: [
      {
        required: true,
        message: 'Vui lòng nhập mật khẩu mới',
      },
    ],
  },
  {
    label: 'Xác nhận mật khẩu mới',
    field: 'confirmpassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];
