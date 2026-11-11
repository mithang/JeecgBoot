import {Button, Space} from 'ant-design-vue'
import {useMessage} from "@/hooks/web/useMessage";

const {createConfirm} = useMessage()

/**
 * Tạo popup nhắc nhở khi xóa danh sách
 * @param deleteFn phương thức xóa
 * @param removeFn phương thức loại bỏ
 */
export function showListDeleteConfirm(deleteFn, removeFn) {
  const {destroy} = createConfirm({
    title: 'Xác nhận xóa biểu mẫu chứ?',
    content: () => (
      <div style="font-size: 13px; color: #666; line-height: 1.8;">
        <div>Loại bỏ: chỉ xóa cấu hình, giữ lại bảng cơ sở dữ liệu và dữ liệu</div>
        <div>Xóa: đồng thời xóa bảng cơ sở dữ liệu và dữ liệu (không thể khôi phục)</div>
      </div>
    ),
    iconType: 'info',
    closable: true,
    maskClosable: true,
    width: 400,
    footer: () => (
      <div style="text-align: right; margin-top: 12px;">
        <Space>
          <Button onClick={() => destroy()}>Hủy</Button>
          <Button type="primary" ghost onClick={getFn(removeFn)}>Loại bỏ biểu mẫu</Button>
          <Button type="primary" danger onClick={getFn(deleteFn)}>Xóa biểu mẫu</Button>
        </Space>
      </div>
    ),
  });

  function getFn(func) {
    return async () => {
      await func()
      destroy()
    }
  }

}
