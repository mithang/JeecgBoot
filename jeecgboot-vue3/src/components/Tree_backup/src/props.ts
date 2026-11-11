import type { PropType } from 'vue';
import type { ReplaceFields, ActionItem, Keys, CheckKeys, ContextMenuOptions, TreeItem } from './typing';
import type { ContextMenuItem } from '/@/hooks/web/useContextMenu';
import type { TreeDataItem } from 'ant-design-vue/es/tree/Tree';
import { propTypes } from '/@/utils/propTypes';

export const basicProps = {
  value: {
    type: [Object, Array] as PropType<Keys | CheckKeys>,
  },
  renderIcon: {
    type: Function as PropType<(params: Recordable) => string>,
  },

  helpMessage: {
    type: [String, Array] as PropType<string | string[]>,
    default: '',
  },

  title: propTypes.string,
  toolbar: propTypes.bool,
  search: propTypes.bool,
  searchValue: propTypes.string,
  checkStrictly: propTypes.bool,
  clickRowToExpand: propTypes.bool.def(true),
  checkable: propTypes.bool.def(false),
  defaultExpandLevel: {
    type: [String, Number] as PropType<string | number>,
    default: '',
  },
  // Tô sáng giá trị tìm kiếm, chỉ tô sáng giá trị khớp cụ thể (thông qua title); khi giá trị là true thì dùng màu mặc định, khi giá trị là#xxxthì dùng giá trị này thay thế và bật tô sáng
  highlight: {
    type: [Boolean, String] as PropType<Boolean | String>,
    default: false,
  },
  defaultExpandAll: propTypes.bool.def(false),

  replaceFields: {
    type: Object as PropType<ReplaceFields>,
  },

  treeData: {
    type: Array as PropType<TreeDataItem[]>,
  },

  actionList: {
    type: Array as PropType<ActionItem[]>,
    default: () => [],
  },

  expandedKeys: {
    type: Array as PropType<Keys>,
    default: () => [],
  },

  selectedKeys: {
    type: Array as PropType<Keys>,
    default: () => [],
  },

  checkedKeys: {
    type: Array as PropType<CheckKeys>,
    default: () => [],
  },

  beforeRightClick: {
    type: Function as PropType<(...arg: any) => ContextMenuItem[] | ContextMenuOptions>,
    default: null,
  },

  rightMenuList: {
    type: Array as PropType<ContextMenuItem[]>,
  },
  // Phương thức phán đoán lọc dữ liệu tùy chỉnh(Lưu ý: Không phải toàn bộ phương thức lọc, mà là phương thức phán đoán lọc nội bộ, dùng để tăng cường cách thức ban đầu chỉ có thể lọc qua title)
  filterFn: {
    type: Function as PropType<(searchValue: any, node: TreeItem, replaceFields: ReplaceFields) => boolean>,
    default: null,
  },
  // Tự động mở rộng kết quả khi tìm kiếm hoàn tất
  expandOnSearch: propTypes.bool.def(false),
  // Sau khi tìm kiếm xong, tự động chọn tất cả kết quả,khi và chỉ khi checkable===true khi có hiệu lực
  checkOnSearch: propTypes.bool.def(false),
  // Sau khi tìm kiếm xong tự động select tất cả kết quả
  selectedOnSearch: propTypes.bool.def(false),
};

export const treeNodeProps = {
  actionList: {
    type: Array as PropType<ActionItem[]>,
    default: () => [],
  },
  replaceFields: {
    type: Object as PropType<ReplaceFields>,
  },
  treeData: {
    type: Array as PropType<TreeDataItem[]>,
    default: () => [],
  },
};
