import { ComponentInternalInstance, ExtractPropTypes } from 'vue';
import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

export namespace JVxeComponent {
  export type Props = ExtractPropTypes<ReturnType<typeof useJVxeCompProps>>;

  interface EnhancedCtx {
    props?: JVxeComponent.Props;
    context?: any;
  }

  /** Loại tăng cường component */
  export interface Enhanced {
    // Tham số đăng ký (xem chi tiết:https://xuliangzhan_admin.gitee.io/vxe-table/v4/table/renderer/edit）
    installOptions: {
      // Tên class tự động focus
      autofocus?: string;
    } & Recordable;
    // Bộ chặn sự kiện (dùng để tương thích)
    interceptor: {
      // Đã triển khai:event.clearActived
      // Giải thích: ví dụ sau khi click vào bảng nổi (panel) của một component nào đó, lúc này ô đang được kích hoạt không nên tự động đóng lại, trả về false có thể ngăn hành vi mặc định.
      'event.clearActived'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
      // Tùy chỉnh:event.clearActived.className
      // Giải thích: nhiều hơn một tham số so với gốc: className, dùng để phán đoán tên style của phần tử được click (đệ quy đến cấp cao nhất)
      'event.clearActived.className'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
    };
    // [Công tắc chức năng]
    switches: {
      // Có sử dụng chế độ editRender hay không (chỉ component hiện tại, không phải toàn cục)
      // Nếu đặt thành true thì phía trên tiêu đề bảng sẽ xuất hiện một biểu tượng có thể chỉnh sửa
      editRender?: boolean;
      // false = Component hiển thị sau khi được kích hoạt);true = Component luôn hiển thị
      visible?: boolean;
    };
    // [Tăng cường Aspect] Xử lý sự kiện Aspect, thường thực thi đồng bộ sau khi một số phương thức thực thi
    aopEvents: {
      // Sự kiện này sẽ kích hoạt khi ô được kích hoạt chỉnh sửa
      editActived?: (this: ComponentInternalInstance, ...args) => any;
      // Sự kiện này sẽ kích hoạt khi ô đang ở trạng thái chỉnh sửa bị đóng
      editClosed?: (this: ComponentInternalInstance, ...args) => any;
      // Giá trị trả về quyết định ô có thể chỉnh sửa hay không
      activeMethod?: (this: ComponentInternalInstance, ...args) => boolean;
    };
    // Có thể thực hiện ví dụ như value mà component select lưu, nhưng ở chế độ span cần hiển thị thànhtext
    translate: {
      // Có bật dịch không
      enabled?: boolean;
      /**
       * [Phương pháp xử lý dịch] Nếu handler để trống thì dùng phương pháp dịch mặc định
       *
       * @param value Giá trị cần dịch
       * @returns{*} Trả về dữ liệu đã dịch
       */
      handler?: (value, ctx?: EnhancedCtx) => any;
    };
    /**
     * [Tăng cường lấy giá trị] Giá trị mà component đưa ra
     *
     * @param value Giá trị lưu vào cơ sở dữ liệu
     * @returns{*} Trả về giá trị đã xử lý
     */
    getValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * [Tăng cường giá trị cài đặt] Giá trị đặt cho component
     *
     * @param value Giá trị do component kích hoạt
     * @returns{*} Trả về giá trị đã xử lý
     */
    setValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * [Tăng cường thêm dòng] Sự kiện được kích hoạt khi người dùng nhấn thêm mới, trả về giá trị mặc định của dòng mới
     *
     * @param defaultValue Giá trị mặc định
     * @param row Dữ liệu hàng
     * @param column Cấu hình cột,.params Là tham số do người dùng cấu hình
     * @param $table vxe Instance
     * @param renderOptions Tùy chọn render
     * @param params Có thể lấy $table tại đây $table
     *
     * @returns Trả về giá trị mới
     */
    createValue: (defaultValue: any, ctx?: EnhancedCtx) => any;
  }

  export type EnhancedPartial = Partial<Enhanced>;
}
