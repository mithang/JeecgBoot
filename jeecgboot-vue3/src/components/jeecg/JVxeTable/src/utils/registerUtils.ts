import type { Component } from 'vue';
import { h } from 'vue';
import VXETable from 'vxe-table';
import { definedComponent, addComponent, componentMap, spanEnds, excludeKeywords } from '../componentMap';
import { JVxeRenderType, JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { getEnhanced } from './enhancedUtils';
import { isFunction } from '/@/utils/is';

/**
 * Kiểm tra xem một component đã được đăng ký chưa
 * @param type
 */
export function isRegistered(type: JVxeTypes | string) {
  if (excludeKeywords.includes(<JVxeTypes>type)) {
    return true;
  }
  return componentMap.has(type);
}

/**
 * Đăng ký component tùy chỉnh vxe
 *
 * @param type
 * @param component Component hiển thị ở trạng thái chỉnh sửa
 * @param spanComponent Component hiển thị ở trạng thái không chỉnh sửa, có thể để trống
 */
export function registerComponent(type: JVxeTypes, component: Component, spanComponent?: Component) {
  addComponent(type, component, spanComponent);
  registerOneComponent(type);
}

/**
 * Đăng ký bất đồng bộ component tùy chỉnh vxe
 *
 * @param type
 * @param promise
 */
export async function registerAsyncComponent(type: JVxeTypes, promise: Promise<any>) {
  const result = await promise;
  if (isFunction(result.installJVxe)) {
    result.install((component: Component, spanComponent?: Component) => {
      addComponent(type, component, spanComponent);
      registerOneComponent(type);
    });
  } else {
    addComponent(type, result.default);
    registerOneComponent(type);
  }
}

/**
 * 2024-03-08
 * liaozhiyang
 * Đăng ký bất đồng bộ component tùy chỉnh vxe
 * 【QQYUN-8241】
 * @param type
 * @param promise
 */
export function registerASyncComponentReal(type: JVxeTypes, component) {
  addComponent(type, component);
  registerOneComponent(type);
}

/**
 * Cài đặt tất cả component vxe
 */
export function registerAllComponent() {
  definedComponent();
  // Duyệt tất cả component để đăng ký hàng loạt
  const components = [...componentMap.keys()];
  components.forEach((type) => {
    if (!type.endsWith(spanEnds)) {
      registerOneComponent(<JVxeTypes>type);
    }
  });
}

/**
 * Đăng ký một component vxe
 *
 * @param type Component type
 */
export function registerOneComponent(type: JVxeTypes) {
  const component = componentMap.get(type);
  if (component) {
    const switches = getEnhanced(type).switches;
    if (switches.editRender && !switches.visible) {
      createEditRender(type, component);
    } else {
      createCellRender(type, component);
    }
  } else {
    throw new Error(`【registerOneComponent】"${type}"không tồn tại trong componentMap`);
  }
}

/** Đăng ký component có thể chỉnh sửa */
function createEditRender(type: JVxeTypes, component: Component, spanComponent?: Component) {
  // Lấy phần tăng cường của component hiện tại
  const enhanced = getEnhanced(type);
  if (!spanComponent) {
    if (componentMap.has(type + spanEnds)) {
      spanComponent = componentMap.get(type + spanEnds);
    } else {
      // Component span mặc định là normal
      spanComponent = componentMap.get(JVxeTypes.normal);
    }
  }
  // Thêm render
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // Mẫu có thể chỉnh sửa
    renderEdit: createRender(type, component, JVxeRenderType.editer),
    // Hiển thị mẫu
    renderCell: createRender(type, spanComponent, JVxeRenderType.spaner),
    // Đăng ký tăng cường
    ...enhanced.installOptions,
  });
}

/** Đăng ký component thông thường */
function createCellRender(type: JVxeTypes, component: Component = <Component>componentMap.get(JVxeTypes.normal)) {
  // Lấy phần tăng cường của component hiện tại
  const enhanced = getEnhanced(type);
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // Mẫu hiển thị mặc định
    renderDefault: createRender(type, component, JVxeRenderType.default),
    // Đăng ký tăng cường
    ...enhanced.installOptions,
  });
}

function createRender(type, component, renderType) {
  return function (renderOptions, params) {
    // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
    // Tạo key duy nhất cho mỗi cell, tránh các cột kề nhau (giá trị giống nhau nhưng options khác nhau) bị tái sử dụng sai
    const rowId = params.row?.id ?? params.rowIndex ?? '';
    const colId = params.column?.property ?? params.column?.id ?? '';
    const cellKey = `cell-${rowId}-${colId}`;
    // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
    return [
      h(component, {
        key: cellKey,
        type: type,
        params: params,
        renderOptions: renderOptions,
        renderType: renderType,
      }),
    ];
  };
}
