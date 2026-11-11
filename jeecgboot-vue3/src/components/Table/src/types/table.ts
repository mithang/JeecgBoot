import type { VNodeChild, ComputedRef } from 'vue';
import type { PaginationProps } from './pagination';
import type { FormProps } from '/@/components/Form';
import type { TableRowSelection as ITableRowSelection } from 'ant-design-vue/lib/table/interface';
import type { ColumnProps } from 'ant-design-vue/lib/table';

import { ComponentType } from './componentType';
import { VueNode } from '/@/utils/propTypes';
import { RoleEnum } from '/@/enums/roleEnum';

export declare type SortOrder = 'ascend' | 'descend';

export interface TableCurrentDataSource<T = Recordable> {
  currentDataSource: T[];
}

export interface TableRowSelection<T = any> extends ITableRowSelection {
  /**
   * Callback executed when selected rows change
   * @type Function
   */
  onChange?: (selectedRowKeys: string[] | number[], selectedRows: T[]) => any;

  /**
   * Callback executed when select/deselect one row
   * @type Function
   */
  onSelect?: (record: T, selected: boolean, selectedRows: Object[]) => any;

  /**
   * Callback executed when select/deselect all rows
   * @type Function
   */
  onSelectAll?: (selected: boolean, selectedRows: T[], changeRows: T[]) => any;

  /**
   * Callback executed when row selection is inverted
   * @type Function
   */
  onSelectInvert?: (selectedRows: string[] | number[]) => any;
  //【issues/8163] bản ghi liên kết thêm mới bị mất
  selectedRows?: any[];
}

export interface TableCustomRecord<T> {
  record?: T;
  index?: number;
}

export interface ExpandedRowRenderRecord<T> extends TableCustomRecord<T> {
  indent?: number;
  expanded?: boolean;
}

export interface ColumnFilterItem {
  text?: string;
  value?: string;
  children?: any;
}

export interface TableCustomRecord<T = Recordable> {
  record?: T;
  index?: number;
}

export interface SorterResult {
  column: ColumnProps;
  order: SortOrder;
  field: string;
  columnKey: string;
}

export interface FetchParams {
  searchInfo?: Recordable;
  page?: number;
  sortInfo?: Recordable;
  filterInfo?: Recordable;
}

export interface GetColumnsParams {
  ignoreIndex?: boolean;
  ignoreAction?: boolean;
  // Giải thích logic mã: 【issues/8502] Giải quyết vấn đề cột quyền không hiển thị trong danh sách nhưng vẫn hiển thị trong cấu hình cột
  ignoreAuth?: boolean;
  ignoreIfShow?: boolean | ((column: BasicColumn) => boolean);
  sort?: boolean;
}

export type SizeType = 'middle' | 'small' | 'large';

export interface TableActionType {
  reload: (opt?: FetchParams) => Promise<void>;
  getSelectRows: <T = Recordable>() => T[];
  clearSelectedRowKeys: () => void;
  expandAll: () => void;
  collapseAll: () => void;
  getSelectRowKeys: () => string[];
  deleteSelectRowByKey: (key: string) => void;
  setPagination: (info: Partial<PaginationProps>) => void;
  setTableData: <T = Recordable>(values: T[]) => void;
  updateTableDataRecord: (rowKey: string | number, record: Recordable) => Recordable | void;
  deleteTableDataRecord: (rowKey: string | number | string[] | number[]) => void;
  insertTableDataRecord: (record: Recordable, index?: number) => Recordable | void;
  findTableDataRecord: (rowKey: string | number) => Recordable | void;
  getColumns: (opt?: GetColumnsParams) => BasicColumn[];
  setColumns: (columns: BasicColumn[] | string[]) => void;
  getDataSource: <T = Recordable>() => T[];
  getRawDataSource: <T = Recordable>() => T;
  setLoading: (loading: boolean) => void;
  setProps: (props: Partial<BasicTableProps>) => void;
  redoHeight: () => void;
  setSelectedRowKeys: (rowKeys: string[] | number[]) => void;
  getPaginationRef: () => PaginationProps | boolean;
  getSize: () => SizeType;
  getRowSelection: () => TableRowSelection<Recordable>;
  getCacheColumns: () => BasicColumn[];
  emit?: EmitType;
  updateTableData: (index: number, key: string, value: any) => Recordable;
  setShowPagination: (show: boolean) => Promise<void>;
  getShowPagination: () => boolean;
  setCacheColumnsByField?: (dataIndex: string | undefined, value: BasicColumn) => void;
  getColumnsRef: () => ComputedRef<BasicColumn[]>;
  getBindValuesRef: () => ComputedRef<any>;
}

export interface FetchSetting {
  // Số trang hiện tại của API yêu cầu
  pageField: string;
  // Mỗi trang hiển thị bao nhiêu dòng
  sizeField: string;
  // Trường danh sách kết quả yêu cầu, hỗ trợ a.b.c
  listField: string;
  // Trường tổng số kết quả yêu cầu, hỗ trợ a.b.c
  totalField: string;
}

export interface TableSetting {
  // Có hiển thị nút làm mới không
  redo?: boolean;
  // Có hiển thị nút điều chỉnh kích thước không
  size?: boolean;
  // Có hiển thị nút điều chỉnh trường không
  setting?: boolean;
  // Cache key cấu hình "Điều chỉnh trường", dùng cho trường hợp trên trang có nhiều bảng cần phân biệt
  cacheKey?: string;
  // Có hiển thị nút toàn màn hình không
  fullScreen?: boolean;
}

export interface BasicTableProps<T = any> {
  // Nhấp hàng để chọn
  clickToRowSelect?: boolean;
  isTreeTable?: boolean;
  // Phương thức sắp xếp tùy chỉnh
  sortFn?: (sortInfo: SorterResult) => any;
  // Phương thức sắp xếp
  filterFn?: (data: Partial<Recordable<string[]>>) => any;
  // Hủy padding mặc định của bảngpadding
  inset?: boolean;
  // Hiển thị cài đặt bảng
  showTableSetting?: boolean;
  // Cài đặt nút thao tác phía trên bảng
  tableSetting?: TableSetting;
  // Vằn ngựa vằn
  striped?: boolean;
  // Có tự động tạo khôngkey
  autoCreateKey?: boolean;
  // Phương thức tính hàng tổng cộng
  summaryFunc?: (...arg: any) => Recordable[];
  // Tùy chỉnh nội dung bảng tổng cộng
  summaryData?: Recordable[];
  // Có hiển thị hàng tổng cộng không
  showSummary?: boolean;
  // Có thể kéo thả cột không
  canColDrag?: boolean;
  // Đối tượng yêu cầu API
  api?: (...arg: any) => Promise<any>;
  // Xử lý tham số trước khi yêu cầu
  beforeFetch?: Fn;
  // Xử lý tùy chỉnh tham số trả về của API
  afterFetch?: Fn;
  // Xử lý trước khi gửi yêu cầu điều kiện truy vấn
  handleSearchInfoFn?: Fn;
  // Cấu hình API yêu cầu
  fetchSetting?: Partial<FetchSetting>;
  // Yêu cầu API ngay lập tức
  immediate?: boolean;
  // Khi mở biểu mẫu tìm kiếm, nếu không có dữ liệu thì có hiển thị bảng hay không
  emptyDataIsShowTable?: boolean;
  // Tham số yêu cầu bổ sung
  searchInfo?: Recordable;
  // Tham số sắp xếp mặc định
  defSort?: Recordable | Recordable[];
  // Sử dụng biểu mẫu tìm kiếm
  useSearchForm?: boolean;
  // Cấu hình biểu mẫu
  formConfig?: Partial<FormProps>;
  // Cấu hình cột
  columns: BasicColumn[];
  // Thống nhất đặt chiều rộng tối đa của cột
  maxColumnWidth?: number;
  // Có hiển thị cột số thứ tự không
  showIndexColumn?: boolean;
  // Cấu hình cột số thứ tự
  indexColumnProps?: BasicColumn;
  // Có hiển thị cột thao tác không
  showActionColumn?: boolean;
  // Cấu hình cột thao tác
  actionColumn?: Partial<BasicColumn>;
  // Văn bản vượt quá chiều rộng có hiển thị... hay không
  ellipsis?: boolean;
  // Có thể tự thích ứng chiều cao không
  canResize?: boolean;
  // Độ lệch chiều cao tự thích ứng, kết quả tính toán-Độ lệch
  resizeHeightOffset?: number;
  // Xóa tùy chọn khi phân trang thay đổi
  clearSelectOnPageChange?: boolean;
  //
  rowKey?: string | ((record: Recordable) => string);
  // Dữ liệu
  dataSource?: Recordable[];
  // Gợi ý ở phía bên phải tiêu đề
  titleHelpMessage?: string | string[];
  // Chiều cao tối thiểu bảng
  minHeight?: number;
  // Chiều cao tối đa khi cuộn bảng
  maxHeight?: number;
  // Có hiển thị viền không
  bordered?: boolean;
  // Chiều rộng cột mở rộng
  expandColumnWidth: number;
  // Cấu hình phân trang
  pagination?: PaginationProps | boolean;
  // loadingTải
  loading?: boolean;

  /**
   * The column contains children to display
   * @default 'children'
   * @type string | string[]
   */
  childrenColumnName?: string;

  /**
   * Override default table elements
   * @type object
   */
  components?: object;

  /**
   * Expand all rows initially
   * @default false
   * @type boolean
   */
  defaultExpandAllRows?: boolean;

  /**
   * Initial expanded row keys
   * @type string[]
   */
  defaultExpandedRowKeys?: string[];

  /**
   * Current expanded row keys
   * @type string[]
   */
  expandedRowKeys?: string[];

  /**
   * Expanded container render for each row
   * @type Function
   */
  expandedRowRender?: (record?: ExpandedRowRenderRecord<T>) => VNodeChild | JSX.Element;

  /**
   * Customize row expand Icon.
   * @type Function | VNodeChild
   */
  expandIcon?: Function | VNodeChild | JSX.Element;

  /**
   * Whether to expand row by clicking anywhere in the whole row
   * @default false
   * @type boolean
   */
  expandRowByClick?: boolean;

  /**
   * The index of `expandIcon` which column will be inserted when `expandIconAsCell` is false. default 0
   */
  expandIconColumnIndex?: number;

  /**
   * Table footer renderer
   * @type Function | VNodeChild
   */
  footer?: Function | VNodeChild | JSX.Element;

  /**
   * Indent size in pixels of tree data
   * @default 15
   * @type number
   */
  indentSize?: number;

  /**
   * i18n text including filter, sort, empty text, etc
   * @default { filterConfirm: 'Ok', filterReset: 'Reset', emptyText: 'No Data' }
   * @type object
   */
  locale?: object;

  /**
   * Row's className
   * @type Function
   */
  rowClassName?: (record: TableCustomRecord<T>, index: number) => string;

  /**
   * Row selection config
   * @type object
   */
  rowSelection?: TableRowSelection;

  /**
   * Set horizontal or vertical scrolling, can also be used to specify the width and height of the scroll area.
   * It is recommended to set a number for x, if you want to set it to true,
   * you need to add style .ant-table td { white-space: nowrap; }.
   * @type object
   */
  // Giải thích logic mã: 【issues/1188]BasicTable thêm định nghĩa kiểu scrollToFirstRowOnChange
  scroll?: { x?: number | true | 'max-content'; y?: number; scrollToFirstRowOnChange?: boolean };

  /**
   * Whether to show table header
   * @default true
   * @type boolean
   */
  showHeader?: boolean;

  /**
   * Size of table
   * @default 'default'
   * @type string
   */
  size?: SizeType;

  /**
   * Table title renderer
   * @type Function | ScopedSlot
   */
  title?: VNodeChild | JSX.Element | string | ((data: Recordable) => string);

  /**
   * Set props on per header row
   * @type Function
   */
  customHeaderRow?: (column: ColumnProps, index: number) => object;

  /**
   * Set props on per row
   * @type Function
   */
  customRow?: (record: T, index: number) => object;

  /**
   * `table-layout` attribute of table element
   * `fixed` when header/columns are fixed, or using `column.ellipsis`
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/CSS/table-layout
   * @version 1.5.0
   */
  tableLayout?: 'auto' | 'fixed' | string;

  /**
   * the render container of dropdowns in table
   * @param triggerNode
   * @version 1.5.0
   */
  getPopupContainer?: (triggerNode?: HTMLElement) => HTMLElement;

  /**
   * Data can be changed again before rendering.
   * The default configuration of general user empty data.
   * You can configured globally through [ConfigProvider](https://antdv.com/components/config-provider-cn/)
   *
   * @version 1.5.4
   */
  transformCellText?: Function;

  /**
   * Callback executed before editable cell submit value, not for row-editor
   *
   * The cell will not submit data while callback return false
   */
  beforeEditSubmit?: (data: { record: Recordable; index: number; key: string | number; value: any }) => Promise<any>;

  /**
   * Callback executed when pagination, filters or sorter is changed
   * @param pagination
   * @param filters
   * @param sorter
   * @param currentDataSource
   */
  onChange?: (pagination: any, filters: any, sorter: any, extra: any) => void;

  /**
   * Callback executed when the row expand icon is clicked
   *
   * @param expanded
   * @param record
   */
  onExpand?: (expande: boolean, record: T) => void;

  /**
   * Callback executed when the expanded rows change
   * @param expandedRows
   */
  onExpandedRowsChange?: (expandedRows: string[] | number[]) => void;

  onColumnsChange?: (data: ColumnChangeParam[]) => void;
}

export type CellFormat = string | ((text: string, record: Recordable, index: number) => string | number) | Map<string | number, any>;

// @ts-ignore
export interface BasicColumn extends ColumnProps<Recordable> {
  children?: BasicColumn[];
  filters?: {
    text: string;
    value: string;
    children?: unknown[] | (((props: Record<string, unknown>) => unknown[]) & (() => unknown[]) & (() => unknown[]));
  }[];

  //
  flag?: 'INDEX' | 'DEFAULT' | 'CHECKBOX' | 'RADIO' | 'ACTION';
  // Giải thích logic mã: 【issues/6908]Khi chuyển đổi đa ngôn ngữ không cần làm mới, giá trị trong BasicColumn và FormSchema không thể chuyển đổi bình thường
  title: string | Fn;
  customTitle?: VueNode;

  slots?: Recordable;
  // slotsBản sao lưu, tương thích với cách viết cũ, chuyển sang cách viết mới để tránh cảnh báo console
  slotsBak?: Recordable;

  // Whether to hide the column by default, it can be displayed in the column configuration
  defaultHidden?: boolean;

  // Help text for table column header
  helpMessage?: string | string[];

  format?: CellFormat;

  // Editable
  edit?: boolean;
  editRow?: boolean;
  editable?: boolean;
  editComponent?: ComponentType;
  // Giải thích logic mã: 【issues/8680]editComponentProps có thể nhận một hàm truyền vàorecord
  editComponentProps?: Recordable | ((record: Recordable) => Recordable);
  editRule?: boolean | ((text: string, record: Recordable) => Promise<string>);
  editValueMap?: (value: any) => string;
  onEditRow?: () => void;
  // Mã quyền điều khiển việc hiển thị
  auth?: RoleEnum | RoleEnum[] | string | string[];
  // Nghiệp vụ kiểm soát có hiển thị hay không
  ifShow?: boolean | ((column: BasicColumn) => boolean);
  //compType-Dùng để ghi lại loại
  compType?: string;
  // Giải thích logic mã: 【pull/1201]Thêm chức năng TableSummary của antd để tương thích với summary cũ (tổng cộng cuối bảng)
  customSummaryRender?: (opt: {
    value: any;
    text: any;
    record: Recordable;
    index: number;
    renderIndex?: number;
    column: BasicColumn;
  }) => any | VNodeChild | JSX.Element;
  // Thuộc tính bổ sung
  extraProps?: Recordable;
}

export type ColumnChangeParam = {
  dataIndex: string;
  fixed: boolean | 'left' | 'right' | undefined;
  visible: boolean;
};

export interface InnerHandlers {
  onColumnsChange: (data: ColumnChangeParam[]) => void;
}
