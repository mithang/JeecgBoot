//Props chung của component hộp chọn thả xuốngprops
import { propTypes } from '/@/utils/propTypes';

export const selectProps = {
  //Có chọn nhiều hay không
  isRadioSelection: {
    type: Boolean,
    // Giải thích logic mã: Component người dùng phòng ban mặc định nên là chọn đơn, nếu không các nơi khác sẽ có vấn đề------------
    default: false,
  },
  //Trả về tên trường value
  rowKey: {
    type: String,
    default: 'id',
  },
  //Tên trường văn bản trả về
  labelKey: {
    type: String,
    default: 'name',
  },
  //Tham số truy vấn
  params: {
    type: Object,
    default: () => {},
  },
  //Có hiển thị nút chọn không
  showButton: propTypes.bool.def(true),
  //Có hiển thị danh sách đã chọn ở bên phải không
  showSelected: propTypes.bool.def(false),
  //Số lượng chọn tối đa
  maxSelectCount: {
    type: Number,
    default: 0,
  },
};

//Props chung của component chọn dạng câyprops
export const treeProps = {
  //Trả về tên trường value
  rowKey: {
    type: String,
    default: 'key',
  },
  //Tên trường văn bản trả về
  labelKey: {
    type: String,
    default: 'title',
  },
  //Cấp độ mở rộng ban đầu
  defaultExpandLevel: {
    type: [Number],
    default: 1,
  },
  //Giá trị pid gốc
  startPid: {
    type: [Number, String],
    default: '',
  },
  //Trường khóa chính
  primaryKey: {
    type: [String],
    default: 'id',
  },
  //Trường ID cha
  parentKey: {
    type: [String],
    default: 'parentId',
  },
  //titleTrường
  titleKey: {
    type: [String],
    default: 'title',
  },
  //Có bật chuyển đổi cấu trúc dữ liệu tree ở phía server hay không
  serverTreeData: propTypes.bool.def(true),
  //Có bật tải dữ liệu bất đồng bộ không
  sync: propTypes.bool.def(true),
  //Có hiển thị nút chọn không
  showButton: propTypes.bool.def(true),
  //Có chỉ hiển thị công ty không
  onlyShowCompany: propTypes.bool.def(false),
  //Có hiển thị hộp kiểm không
  checkable: propTypes.bool.def(true),
  //checkable Trong trạng thái checkable, việc chọn nút được kiểm soát hoàn toàn (trạng thái chọn của nút cha và nút con không còn liên kết với nhau)
  checkStrictly: propTypes.bool.def(false),
  // Có cho phép chọn nhiều không, mặc định true
  multiple: propTypes.bool.def(true),
  // Có chỉ chọn vị trí công tác không
  izOnlySelectDepartPost: propTypes.bool.def(false),
  // Có hiển thị tên viết tắt phòng ban không
  izShowDepartNameAbbr: propTypes.bool.def(false),
};
