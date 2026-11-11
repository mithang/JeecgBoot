import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

/**
 * Người đăng có phải là system không: tin nhắn hệ thống tự động gửi, người đăng được lưu là system, không phải tài khoản người dùng
 * @author wangshuai
 * @since 2026-11-02 【LHZP-1240】【Trung tâm tin nhắn】 Tin nhắn của tôi: người đăng chưa được dịch
 */
export function isSystemSender(sender) {
  return sender === 'system';
}

/**
 * Văn bản hiển thị của người đăng: system mặc định hiển thị là "Hệ thống", người đăng khác hiển thị text (tên hoặc giá trị gốc)
 * Xét theo giá trị gốc của người đăng, dù có tài khoản tên là system thì cũng không hiển thị thành tên của tài khoản đó; danh sách và popup chi tiết dùng chung
 * @author wangshuai
 * @since 2026-11-02 【LHZP-1240】【Trung tâm tin nhắn】 Tin nhắn của tôi: người đăng chưa được dịch
 */
export function getSenderText(sender, text) {
  if (isSystemSender(sender)) {
    return 'Hệ thống';
  }
  return text;
}

export const columns: BasicColumn[] = [
  {
    title: 'Tiêu đề',
    dataIndex: 'titile',
    width: 100,
    align: 'left',
  },
  {
    title: 'Loại tin nhắn',
    dataIndex: 'msgCategory',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: 'Thông báo', value: '1', color: 'blue' },
          { label: 'Tin nhắn hệ thống', value: '2' },
        ],
        true
      );
    },
  },
  {
    title: 'Người đăng',
    dataIndex: 'sender',
    width: 80,
    // Tài khoản hiển thị tên đã dịch từ backend, các giá trị không phải tài khoản như thông báo quy trình hiển thị nguyên văn; khi trang hiện tại không có người phát hành nào khớp tài khoản thì backend không trả về sender_dictText, fallback hiển thị giá trị gốc
    customRender: ({ record }) => getSenderText(record.sender, record.sender_dictText || record.sender),
  },
  {
    title: 'Thời gian đăng',
    dataIndex: 'sendTime',
    width: 80,
  },
  {
    title: 'Mức ưu tiên',
    dataIndex: 'priority',
    width: 80,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: 'Trạng thái đọc',
    dataIndex: 'readFlag',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: 'Chưa đọc', value: '0', color: 'red' },
          { label: 'Đã đọc', value: '1' },
        ],
        true
      );
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: 'Tiêu đề',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'sender',
    label: 'Người đăng',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'sendTime',
    label: 'Thời gian đăng',
    component: 'RangeDate',
    componentProps: {
      valueType: 'Date',
    },
    colProps: { span: 6 },
  },
  {
    field: 'msgCategory',
    label: 'Loại tin nhắn',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Thông báo', value: '1' },
        { label: 'Tin nhắn hệ thống', value: '2' },
        { label: 'Kế hoạch lịch trình', value: 'plan' },
        { label: 'Tin nhắn quy trình', value: 'flow' },
        { label: 'Cuộc họp', value: 'meeting' },
        { label: 'Kho tri thức', value: 'file' },
        { label: 'Thông báo phối hợp', value: 'collab' },
        { label: 'Thông báo giám sát', value: 'supe' },
      ],
    },
    colProps: { span: 6 },
  },
];
