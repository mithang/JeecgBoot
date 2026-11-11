import { FormSchema } from '/@/components/Form/index';
import { rules } from '/@/utils/helper/validator';

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
    name: 'Cài đặt cơ bản',
    component: 'BaseSetting',
  },
  {
    key: '2',
    name: 'Cài đặt bảo mật',
    component: 'SecureSetting',
  },
  /* {
    key: '3',
    name: 'Liên kết tài khoản',
    component: 'AccountBind',
  },
  {
    key: '4',
    name: 'Thông báo tin nhắn mới',
    component: 'MsgNotify',
  },*/
];

// Cài đặt cơ sở form
export const baseSetschemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'realname',
    component: 'Input',
    label: 'Biệt danh',
    colProps: { span: 18 },
  },
  {
    field: 'sex',
    label: 'Giới tính',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
      stringToNumber: true,
    },
    colProps: { span: 18 },
  },
  {
    label: 'Ngày sinh',
    field: 'birthday',
    component: 'DatePicker',
    colProps: { span: 18 },
  },
  {
    field: 'email',
    component: 'Input',
    label: 'Email',
    colProps: { span: 18 },
  },
  {
    field: 'phone',
    component: 'Input',
    label: 'Số điện thoại liên hệ',
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, false)[0] },
        { pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại sai' },
      ];
    },
    colProps: { span: 18 },
  },
];

// Cài đặt bảo mật list
export const secureSettingList: ListItem[] = [
  {
    key: '1',
    title: 'Mật khẩu tài khoản',
    description: 'Độ mạnh mật khẩu hiện tại: Mạnh',
    extra: 'Sửa',
  },
  {
    key: '2',
    title: 'Điện thoại bảo mật',
    description: 'Đã liên kết điện thoại::138****8293',
    extra: 'Sửa',
  },
  {
    key: '3',
    title: 'Câu hỏi bảo mật',
    description: 'Chưa đặt câu hỏi bảo mật, câu hỏi bảo mật có thể bảo vệ hiệu quả an toàn tài khoản',
    extra: 'Sửa',
  },
  {
    key: '4',
    title: 'Email dự phòng',
    description: 'Đã liên kết email::ant***sign.com',
    extra: 'Sửa',
  },
  {
    key: '5',
    title: 'MFA Thiết bị',
    description: 'Chưa liên kết thiết bị MFA, sau khi liên kết có thể xác nhận lần hai',
    extra: 'Sửa',
  },
];

// Liên kết tài khoản list
export const accountBindList: ListItem[] = [
  {
    key: '1',
    title: 'Liên kết Taobao',
    description: 'Hiện chưa liên kết tài khoản Taobao',
    extra: 'Liên kết',
    avatar: 'ri:taobao-fill',
    color: '#ff4000',
  },
  {
    key: '2',
    title: 'Liên kết Alipay',
    description: 'Hiện chưa liên kết tài khoản Alipay',
    extra: 'Liên kết',
    avatar: 'fa-brands:alipay',
    color: '#2eabff',
  },
  {
    key: '3',
    title: 'Liên kết DingTalk',
    description: 'Hiện chưa liên kết tài khoản DingTalk',
    extra: 'Liên kết',
    avatar: 'ri:dingding-fill',
    color: '#2eabff',
  },
];

// Thông báo tin nhắn mới list
export const msgNotifyList: ListItem[] = [
  {
    key: '1',
    title: 'Mật khẩu tài khoản',
    description: 'Tin nhắn của người dùng khác sẽ được thông báo dưới dạng thư nội bộ',
  },
  {
    key: '2',
    title: 'Tin nhắn hệ thống',
    description: 'Tin nhắn hệ thống sẽ được thông báo dưới dạng tin nhắn nội bộ',
  },
  {
    key: '3',
    title: 'Nhiệm vụ chờ xử lý',
    description: 'Nhiệm vụ chờ xử lý sẽ được thông báo dưới dạng tin nhắn nội bộ',
  },
];
