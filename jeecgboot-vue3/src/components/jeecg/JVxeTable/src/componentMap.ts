import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';
import { componentMap } from './componentMapStore';

import JVxeSlotCell from './components/cells/JVxeSlotCell';
import JVxeNormalCell from './components/cells/JVxeNormalCell.vue';
import JVxeDragSortCell from './components/cells/JVxeDragSortCell.vue';

import JVxeInputCell from './components/cells/JVxeInputCell.vue';
import JVxeDateCell from './components/cells/JVxeDateCell.vue';
import JVxeTimeCell from './components/cells/JVxeTimeCell.vue';
import JVxeSelectCell from './components/cells/JVxeSelectCell.vue';
import JVxeRadioCell from './components/cells/JVxeRadioCell.vue';
import JVxeCheckboxCell from './components/cells/JVxeCheckboxCell.vue';
import JVxeUploadCell from './components/cells/JVxeUploadCell.vue';
// import { TagsInputCell, TagsSpanCell } from './components/cells/JVxeTagsCell.vue'
import JVxeProgressCell from './components/cells/JVxeProgressCell.vue';
import JVxeTextareaCell from './components/cells/JVxeTextareaCell.vue';
// import JVxeDepartSelectCell from './components/cells/JVxeDepartSelectCell.vue'
// import JVxeUserSelectCell from './components/cells/JVxeUserSelectCell.vue'
import JVxeTreeSelectCell from './components/cells/JVxeTreeSelectCell.vue';
import JVxeCategorySelectCell from './components/cells/JVxeCategorySelectCell.vue';

// Giải thích logic mã: 【issues/860] Mã một-nhiều được tạo, sau khi cập nhật nóng nhấn thêm mới bị kẹt[Tạm thời xử lý trước]
const JVxeComponents = 'JVxeComponents__';
/** span Cuối component */
export const spanEnds: string = ':span';

/** Định nghĩa từ khóa không thể dùng để đăng ký */
export const excludeKeywords: Array<JVxeTypes> = [
  JVxeTypes.hidden,
  JVxeTypes.rowNumber,
  JVxeTypes.rowCheckbox,
  JVxeTypes.rowRadio,
  JVxeTypes.rowExpand,
];

/**
 * Đăng ký component
 *
 * @param type Component type
 * @param component VueComponent
 * @param spanComponent Component hiển thị, có thể để trống, mặc định là component JVxeNormalCell
 */
export function addComponent(type: JVxeTypes, component: JVxeVueComponent, spanComponent?: JVxeVueComponent) {
  if (excludeKeywords.includes(type)) {
    throw new Error(`[addComponent] Không thể sử dụng"${type}"làm name của component, vì đây là từ khóa.`);
  }
  if (componentMap.has(type)) {
    throw new Error(`[addComponent] Component"${type}"Đã tồn tại`);
  }
  componentMap.set(type, component);
  if (spanComponent) {
    componentMap.set(type + spanEnds, spanComponent);
  }
  // Giải thích logic mã: 【issues/860] Mã một-nhiều được tạo, sau khi cập nhật nóng nhấn thêm mới bị kẹt[Tạm thời xử lý trước]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}

export function deleteComponent(type: JVxeTypes) {
  componentMap.delete(type);
  componentMap.delete(type + spanEnds);
  // Giải thích logic mã: 【issues/860] Mã một-nhiều được tạo, sau khi cập nhật nóng nhấn thêm mới bị kẹt[Tạm thời xử lý trước]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}

/** Định nghĩa component tùy chỉnh tích hợp sẵn */
export function definedComponent() {
  addComponent(JVxeTypes.slot, JVxeSlotCell);
  addComponent(JVxeTypes.normal, JVxeNormalCell);
  addComponent(JVxeTypes.rowDragSort, JVxeDragSortCell);

  addComponent(JVxeTypes.input, JVxeInputCell);
  addComponent(JVxeTypes.inputNumber, JVxeInputCell);
  addComponent(JVxeTypes.radio, JVxeRadioCell);
  addComponent(JVxeTypes.checkbox, JVxeCheckboxCell);
  addComponent(JVxeTypes.select, JVxeSelectCell);
  addComponent(JVxeTypes.selectSearch, JVxeSelectCell); // Tìm kiếm trong dropdown
  addComponent(JVxeTypes.selectMultiple, JVxeSelectCell); // Danh sách thả xuống chọn nhiều
  addComponent(JVxeTypes.date, JVxeDateCell);
  addComponent(JVxeTypes.datetime, JVxeDateCell);
  addComponent(JVxeTypes.time, JVxeTimeCell);
  addComponent(JVxeTypes.upload, JVxeUploadCell);
  addComponent(JVxeTypes.textarea, JVxeTextareaCell);

  // addComponent(JVxeTypes.tags, TagsInputCell, TagsSpanCell)
  addComponent(JVxeTypes.progress, JVxeProgressCell);

  // addComponent(JVxeTypes.departSelect, JVxeDepartSelectCell)
  // addComponent(JVxeTypes.userSelect, JVxeUserSelectCell)
  // update-begin--author:liaozhiyang---date:20260413---for:【issues/7633] Bảng con online hỗ trợ cây danh mục phân loại, cây tùy chỉnh
  addComponent(JVxeTypes.treeSelect, JVxeTreeSelectCell);
  addComponent(JVxeTypes.catTreeSelect, JVxeCategorySelectCell);
  // update-end--author:liaozhiyang---date:20260413---for:【issues/7633] Bảng con online hỗ trợ cây danh mục phân loại, cây tùy chỉnh
}

export { componentMap, clearComponent } from './componentMapStore';
