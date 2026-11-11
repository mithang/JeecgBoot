import { propTypes } from '/@/utils/propTypes';

export const vxeProps = () => ({
  rowKey: propTypes.string.def('id'),
  // Thông tin cột
  columns: {
    type: Array,
    required: true,
  },
  // Nguồn dữ liệu
  dataSource: {
    type: Array,
    required: true,
  },
  authPre: {
    type: String,
    required: false,
    default: '',
  },
  // Có hiển thị thanh công cụ không
  toolbar: propTypes.bool.def(false),
  // Cấu hình thanh công cụ
  toolbarConfig: propTypes.object.def(() => ({
    // prefix Tiền tố; suffix Hậu tố;
    slots: ['prefix', 'suffix'],
    // add add là nút thêm mới; remove là nút xóa; clearSelection là nút xóa lựa chọn; collapse là mở rộng/thu gọn
    btns: ['add', 'remove', 'clearSelection'],
  })),
  // Có hiển thị số dòng không
  rowNumber: propTypes.bool.def(false),
  // Cố định vị trí số dòng hoặc không cố định [QQYUN-8405】
  rowNumberFixed: propTypes.oneOf(['left', 'none']).def('left'),
  // Giải thích logic mã: 【issues/1162]Khi cột JVxeTable quá dài (xuất hiện thanh cuộn ngang) thì không thể kéo thả sắp xếp
  dragSortFixed: propTypes.oneOf(['left', 'none']).def('left'),
  rowSelectionFixed: propTypes.oneOf(['left', 'none']).def('left'),
  // Có thể chọn hàng không
  rowSelection: propTypes.bool.def(false),
  // Loại hàng đã chọn
  rowSelectionType: propTypes.oneOf(['checkbox', 'radio']).def('checkbox'),
  // Có thể mở rộng hàng không
  rowExpand: propTypes.bool.def(false),
  // Cấu hình hàng mở rộng
  expandConfig: propTypes.object.def(() => ({})),
  // Có thể chèn hàng không
  insertRow: propTypes.bool.def(true),
  // Trang có đang tải không
  loading: propTypes.bool.def(false),
  // Chiều cao bảng
  height: propTypes.oneOfType([propTypes.number, propTypes.string]).def('auto'),
  // Chiều cao tối đa
  maxHeight: {
    type: Number,
    default: () => null,
  },
  // Hàng cần vô hiệu hóa
  disabledRows: propTypes.object.def(() => ({})),
  // Có vô hiệu hóa tất cả component không
  disabled: propTypes.bool.def(false),
  // Có thể kéo thả để sắp xếp hay không (khi có cột cố định thì không thể kéo thả sắp xếp, chỉ có thể sắp xếp lên/xuống)
  dragSort: propTypes.bool.def(false),
  // Key lưu trường sắp xếpKey
  sortKey: propTypes.string.def('orderNum'),
  // Giá trị bắt đầu của số thứ tự sắp xếp, mặc định là 0
  sortBegin: propTypes.number.def(0),
  // Kích thước, các giá trị tùy chọn: medium (trung bình), small (nhỏ), mini (rất nhỏ)
  size: propTypes.oneOf(['medium', 'small', 'mini']).def('medium'),
  // Có hiển thị đường viền không
  bordered: propTypes.bool.def(false),
  // Cấu hình cột tùy chỉnh, mặc định kế thừa setup.toolbar.custom
  custom: propTypes.bool.def(false),
  // Tham số phân trang, đặt là hiển thị phân trang
  pagination: propTypes.object.def(() => ({})),
  // Khi nhấn vào hàng có hiển thị biểu mẫu con không
  clickRowShowSubForm: propTypes.bool.def(false),
  // Khi nhấn vào hàng có hiển thị biểu mẫu chính không
  clickRowShowMainForm: propTypes.bool.def(false),
  // Có nhấp để chọn dòng hay không, mức ưu tiên thấp nhất
  clickSelectRow: propTypes.bool.def(false),
  // Có bật hiệu ứng reload dữ liệu hay không
  reloadEffect: propTypes.bool.def(false),
  // Quy tắc kiểm chứng
  editRules: propTypes.object.def(() => ({})),
  // Có xóa hàng không đồng bộ hay không, nếu bạn muốn triển khai xóa không đồng bộ thì cần bật tùy chọn này,
  // Chỉ khi gọi phương thức confirmRemove trong sự kiện remove mới thực sự xóa (trừ khi tất cả các hàng bị xóa đều là hàng mới thêm)
  asyncRemove: propTypes.bool.def(false),
  // Có luôn hiển thị component hay không, nếu là false thì chỉ khi nhấn vào mới xuất hiện component
  // Lưu ý: Tham số này không thể sửa động; nếu số trường hàng, cột nhiều thì tùy theo hiệu năng máy sẽ gây giật lag ở các mức độ khác nhau.
  // TODO Phiên bản mớivxe-tableĐã hủy tham số visible, dẫn đến không thể thực hiện chức năng này
  alwaysEdit: propTypes.bool.def(false),
  // Cấu hình liên động, dạng mảng, xem tài liệu để biết cấu hình chi tiết
  linkageConfig: propTypes.array.def(() => []),
  // Có bật dùng webSocket để làm mới không dấu vết không
  socketReload: propTypes.bool.def(false),
  // Khi socketKey giống nhau thay đổi sẽ làm mới lẫn nhau
  socketKey: propTypes.string.def('vxe-default'),
  // Khi thêm dòng mới, chuyển đổi trạng thái kích hoạt của dòng
  addSetActive: propTypes.bool.def(true),
  // Có bật chỉnh sửa bằng bàn phím không
  keyboardEdit: propTypes.bool.def(false),
  // Cấu hình cuộn ảo ngang (không hỗ trợ dòng mở rộng)
  // 【QQYUN-7676] Khi thanh cuộn x cuộn, từ điển biến thànhid
  scrollX: propTypes.object.def(() => ({ enabled: false })),
  // Cấu hình cuộn ảo dọc (không hỗ trợ dòng mở rộng)
  scrollY: propTypes.object.def(() => ({ enabled: true })),
  //【QQYUN-8566] Key của cài đặt cột cache (duy nhất trong trang route)
  cacheColumnsKey: propTypes.string.def(''),
  // Giải thích logic mã: 【QQYUN-8785]id vị trí cột của biểu mẫu online chưa được giới hạn, kéo cột khác lên trên cột id, khi đồng bộ cơ sở dữ liệu sẽ báo lỗi
  rowClassName: {
    type: [String, Function],
    default: null,
  },
  // Hàng không cho phép kéo thả [{'key':field,'value':value}]
  notAllowDrag: propTypes.array.def(() => []),

  // Cấu hình nút thêm mới
  addBtnCfg: propTypes.object,
  // Cấu hình nút xóa
  removeBtnCfg: propTypes.object,
});

export const vxeEmits = ['save', 'added', 'removed', 'inserted', 'dragged', 'selectRowChange', 'pageChange', 'valueChange', 'blur'];
