import type { Component, Ref, ComputedRef, ExtractPropTypes } from 'vue';
import type { VxeColumnProps } from 'vxe-table/types/column';
import type { JVxeComponent } from './JVxeComponent';
import type { VxeGridInstance, VxeTablePropTypes } from 'vxe-table';
import { JVxeTypes } from './JVxeTypes';
import { vxeProps } from '../vxe.data';
import { useMethods } from '../hooks/useMethods';
import { getJVxeAuths } from '../utils/authUtils';

export type JVxeTableProps = Partial<ExtractPropTypes<ReturnType<typeof vxeProps>>>;
export type JVxeTableMethods = ReturnType<typeof useMethods>['methods'];

export type JVxeVueComponent = {
  enhanced?: JVxeComponent.EnhancedPartial;
} & Component;

type statisticsTypes = 'sum' | 'average';

export type JVxeColumn = IJVxeColumn & Recordable;

/**
 * JVxe Mục cấu hình cột
 */
export interface IJVxeColumn extends VxeColumnProps {
  type?: any;
  // Định danh duy nhất của hàng
  key: string;
  // Thông tin nhắc nhở về giá trị kỳ vọng của biểu mẫu, có thể dùng${...}Văn bản thay thế biến
  placeholder?: string;
  // Giá trị mặc định
  defaultValue?: any;
  // Có vô hiệu hóa cột hiện tại không, mặc địnhfalse
  disabled?: boolean;
  // Quy tắc kiểm chứng TODO, kiểu dữ liệu chờ định nghĩa
  validateRules?: any;
  // Trường liên động cấp tiếp theokey
  linkageKey?: string;
  // Các thuộc tính khác của component được truyền vào tùy chỉnh
  props?: Recordable;
  allowClear?: boolean; // Cho phép xóa
  // Có phải là cột thống kê không, chỉ inputNumber mới có thể đặt cột thống kê. Cột thống kê: sum là tính tổng; average là giá trị trung bình
  statistics?: boolean | [statisticsTypes, statisticsTypes?];
  // 【select】
  dictCode?: string; // Từ điển code
  options?: { title?: string; label?: string; text?: string; value: any; disabled?: boolean }[]; // Danh sách tùy chọn dropdown
  allowInput?: boolean; // Cho phép nhập
  allowSearch?: boolean; // Cho phép tìm kiếm
  // 【slot】
  slotName?: string; // Tên slot
  // 【checkbox】
  customValue?: [any, any]; // Giá trị tùy chỉnh
  defaultChecked?: boolean; // Được chọn mặc định
  // 【upload】 upload
  btnText?: string; // Văn bản nút tải lên
  token?: boolean; // Có truyền hay không token
  responseName?: string; // Tên giá trị trả về
  action?: string; // Địa chỉ tải lên
  allowRemove?: boolean; // Có cho phép xóa không
  allowDownload?: boolean; // Có cho phép tải xuống không
  // [Tìm kiếm thả xuống theo từ điển]
  dict?: string; // Thông tin cấu hình bảng từ điển: Tên bảng cơ sở dữ liệu,Tên trường hiển thị,Tên trường lưu trữ
  async?: boolean; // Có phải chế độ đồng bộ không
  tipsContent?: string;
  // 【popup】
  popupCode?: string;
  field?: string;
  orgFields?: string;
  destFields?: string;
}

export interface JVxeRefs {
  gridRef: Ref<VxeGridInstance | undefined>;
  subPopoverRef: Ref<any>;
  detailsModalRef: Ref<any>;
}

export interface JVxeDataProps {
  prefixCls: string;
  // vxe InstanceID
  caseId: string;
  // vxe Cuối cùng columns
  vxeColumns?: ComputedRef;
  // vxe Cuối cùng dataSource
  vxeDataSource: Ref<Recordable[]>;
  // Ghi lại vị trí thanh cuộn
  scroll: { top: number; left: number };
  // Hiện có đang cuộn không
  scrolling: Ref<boolean>;
  // vxe Cấu hình mặc định
  defaultVxeProps: object;
  // Liên kết hộp chọn bên trái
  selectedRows: Ref<any[]>;
  // Gắn các ID đã chọn của hộp chọn bên tráiid
  selectedRowIds: Ref<string[]>;
  disabledRowIds: string[];
  // Cấu hình cột thống kê
  statistics: {
    has: boolean;
    sum: string[];
    average: string[];
  };
  // Tất cả thông tin ủy quyền liên quan đến bảng hiện tại
  authsMap: Ref<Nullable<ReturnType<typeof getJVxeAuths>>>;
  // Tích hợp sẵn EditRules
  innerEditRules: Recordable<VxeTablePropTypes.EditRules[]>;
  // Tùy chọn dropdown liên động (dùng để tách riêng các tùy chọn dropdown khác nhau)
  // Cấu hình liên động nội bộ,map
  innerLinkageConfig: Map<string, any>;
  // Hàng đã bật hiệu ứng làm mới dữ liệu
  reloadEffectRowKeysMap: Recordable;
}

export interface JVxeLinkageConfig {
  // của cấp đầu tiên được liên động key
  key: string;
  // Phương thức lấy dữ liệu
  requestData: (parent: string) => Promise<any>;
}

export { JVxeTypes };
