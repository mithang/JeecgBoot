<!--
  Bộ chọn xếp tầng: Ô nhập chỉ đọc + Menu dropdown nhiều cột được mount vào body
  Khi chọn nút lá thì đồng bộ value và đóng dropdown; nút không phải lá chỉ mở rộng cột tiếp theo
-->
<template>
  <div class="cascader" ref="rootRef">
    <AInput
      :value="displayText"
      :placeholder="inputPlaceholder"
      readonly
      :disabled="inputDisabled"
      v-bind="inputAttrs"
      :class="{ 'cascader-input-open': visible }"
      @click="toggle"
    >
      <template #suffix>
        <span v-if="displayText && !inputDisabled" class="ant-input-clear-icon ant-input-clear-icon-has-suffix"
          role="button" tabindex="-1" @click.stop="handleClear" @mousedown.prevent>
          <CloseCircleFilled />
        </span>
        <DownOutlined class="cascader-arrow" :class="{ 'is-open': visible }" />
      </template>
    </AInput>
    <!-- Dropdown gắn vào body, dùng fixed + đã tính left/top Định vị -->
    <Teleport to="body">
      <Transition name="cascader-dropdown">
        <div v-show="visible" ref="dropdownRef" class="cascader-dropdown cascader-dropdown-placement"
          :style="dropdownPlaceStyle">
          <div class="cascader-menus">
            <div v-for="(column, colIndex) in columns" :key="colIndex" class="cascader-menu">
              <div v-for="opt in column" :key="opt.value" class="cascader-menu-item"
                :class="{ 'is-active': isActive(opt, colIndex), 'is-selected': isSelected(opt, colIndex) }"
                @click="onSelectOption(opt, colIndex)">
                <span class="cascader-menu-item-content">{{ opt.label }}</span>
                <RightOutlined v-if="hasChildren(opt)" class="cascader-menu-item-arrow" />
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount, nextTick, unref } from 'vue';
import { Input as AInput } from 'ant-design-vue';
import { DownOutlined, RightOutlined, CloseCircleFilled } from '@ant-design/icons-vue';
import { useAttrs } from '/@/hooks/core/useAttrs';

/** Tùy chọn liên cấp:value/label + Tùy chọn children */
export interface CascaderOption {
  value: string | number;
  label: string;
  children?: CascaderOption[];
}

/** Đường dẫn đã chọn: mảng value mỗi cấp */
type PathValue = (string | number)[];

const DISPLAY_SEPARATOR = ' / ';
const DROPDOWN_GAP = 4;

defineOptions({
  name: 'JCascader',
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    /** Đường dẫn đang chọn (mảng value mỗi cấp) */
    value?: PathValue;
    /** Dữ liệu cây liên cấp */
    options?: CascaderOption[];
    /** Có chỉ hiển thị cấp cuối cùng trong input hay không */
    showLastLevelOnly?: boolean;
  }>(),
  {
    value: () => [],
    options: () => [],
    showLastLevelOnly: false,
  }
);

const emit = defineEmits<{
  (e: 'change', value: PathValue): void;
  (e: 'update:value', value: PathValue): void;
}>();

const attrs = useAttrs();
const inputPlaceholder = computed(() => String(unref(attrs as Record<string, unknown>).placeholder ?? 'Vui lòng chọn'));
const inputDisabled = computed(() => Boolean(unref(attrs as Record<string, unknown>).disabled));
// update-begin--author:liaozhiyang---date:20260804---for：【LHZP-707] Sửa lỗi Tỉnh/Thành/Quận huyện ở trạng thái vô hiệu hóa vẫn có thể bấm x để xóa
/** Loại trừ allowClear/disabled, nút xóa do suffix tự định nghĩa */
const inputAttrs = computed(() => {
  const { allowClear: _allowClear, disabled: _disabled, ...rest } = unref(attrs as Record<string, unknown>);
  return rest;
});
// update-end--author:liaozhiyang---date:20260804---for：【LHZP-707] Sửa lỗi Tỉnh/Thành/Quận huyện ở trạng thái vô hiệu hóa vẫn có thể bấm x để xóa
const rootRef = ref<HTMLElement>();
const dropdownRef = ref<HTMLElement>();
/** Dropdown có mở rộng hay không */
const visible = ref(false);
/** Đường dẫn đang chỉnh sửa khi mở rộng (trước khi xác nhận); dùng cho trạng thái tô sáng hiện tại và dữ liệu cột con của menu nhiều cột */
const editingPath = ref<PathValue>([]);
/** Định vị fixed khi dropdown gắn vào body: left, top (chuỗi px) */
const dropdownPlaceStyle = ref<{ left: string; top: string }>({ left: '0', top: '0' });

/** Chuẩn hóa value thành mảng đường dẫn */
function normalizePath(val: PathValue | undefined): PathValue {
  return Array.isArray(val) ? val : [];
}

/** Có nút con không (không phải nút lá) */
function hasChildren(opt: CascaderOption): boolean {
  return !!(opt.children?.length);
}

/** Dựa theo đường dẫn value để tìm từng cấp trong cây options, trả về mảng label tương ứng */
function getPathLabels(opts: CascaderOption[], pathValues: PathValue): string[] {
  const labels: string[] = [];
  let current = opts;
  for (const v of pathValues) {
    const opt = current.find((o) => o.value === v);
    if (!opt) break;
    labels.push(opt.label);
    current = opt.children ?? [];
  }
  return labels;
}

/** Văn bản hiển thị trong ô nhập: khi showLastLevelOnly chỉ hiển thị label của cấp cuối cùng, nếu không thì dùng dấu phân cách để nối toàn bộ đường dẫn */
const displayText = computed(() => {
  const path = normalizePath(props.value);
  if (!path.length) return '';
  const labels = getPathLabels(props.options ?? [], path);
  return props.showLastLevelOnly ? (labels.at(-1) ?? '') : labels.join(DISPLAY_SEPARATOR);
});

/** Dùng cho tô sáng menu và dữ liệu cột: khi mở rộng dùng editingPath, khi thu lại dùng props.value */
const pathForMenus = computed<PathValue>(() =>
  visible.value ? editingPath.value : normalizePath(props.value));

/** Các cột được mở rộng theo path hiện tại: cột 0 là gốc, cột k là path[k-1] children; số cột = path.length + 1, nhấp vào cấp nào chỉ hiển thị đến cấp tiếp theo */
const columns = computed(() => {
  const path = pathForMenus.value;
  const opts = props.options ?? [];
  const cols: CascaderOption[][] = [];
  let current: CascaderOption[] = opts;
  for (let i = 0; i <= path.length && current.length; i++) {
    cols.push(current);
    if (i < path.length && path[i] !== undefined) {
      const opt = current.find((o) => o.value === path[i]);
      current = opt?.children ?? [];
    } else {
      break;
    }
  }
  return cols;
});

/** Mục này trong cột hiện tại có phải là nút của "đường dẫn hiện tại" tại cột đó hay không (tô sáng và hiển thị cột con) */
function isActive(opt: CascaderOption, colIndex: number): boolean {
  const path = pathForMenus.value;
  return path[colIndex] === opt.value;
}

/** Mục này trong cột hiện tại có phải là nút lá đã chọn hay không (kiểu đã chọn) */
function isSelected(opt: CascaderOption, colIndex: number): boolean {
  const path = pathForMenus.value;
  if (colIndex < path.length - 1) return path[colIndex] === opt.value;
  return path[colIndex] === opt.value && !hasChildren(opt);
}

/** Click vào một tùy chọn của cột: cắt ngắn đến cột đó và thêm mục hiện tại; nếu là lá thì emit và đóng lại, nếu không thì cập nhật editingPath để mở rộng cột tiếp theo */
function onSelectOption(opt: CascaderOption, colIndex: number) {
  const path = visible.value ? [...editingPath.value] : normalizePath(props.value).slice();
  path.length = colIndex;
  path.push(opt.value);
  if (hasChildren(opt)) {
    editingPath.value = path;
  } else {
    emit('change', path);
    emit('update:value', path);
    visible.value = false;
  }
}

function toggle() {
  if (inputDisabled.value) return;
  visible.value = !visible.value;
}

/** Xóa lựa chọn: bù cho việc UI library không hiển thị nút xóa khi ở chế độ readonly, tự triển khai thủ công trong suffix */
function handleClear() {
  if (inputDisabled.value) return;
  emit('change', []);
  emit('update:value', []);
  visible.value = false;
}

/** Đóng dropdown khi nhấp vào vị trí không nằm trong ô nhập và không nằm trong dropdown (lắng nghe ở giai đoạn capture) */
function handleClickOutside(e: MouseEvent) {
  const target = e.target as Node;
  if (!target || !document.body.contains(target)) return;
  if (rootRef.value?.contains(target)) return;
  if (dropdownRef.value?.contains(target)) return;
  visible.value = false;
}

/** Dựa vào vị trí viewport của rootRef của trigger, đặt giá trị dropdown fixed left/top(Danh sách thả xuống nằm dưới body) */
function updatePopupPosition() {
  if (!rootRef.value) return;
  const rect = rootRef.value.getBoundingClientRect();
  // update-begin--author:liaozhiyang---date:20260714---for【LHZP-100] Tỉnh/Thành/Quận huyện khi ở mép trang, cấp cuối cùng không chọn được
  const margin = 8;
  const viewportWidth = document.documentElement.clientWidth;
  const viewportHeight = document.documentElement.clientHeight;
  // Chiều rộng và chiều cao của lớp nổi (khi chưa render ra sẽ thoái hóa thành chỉ định vị theo trigger)
  const dropdownWidth = dropdownRef.value?.offsetWidth ?? 0;
  const dropdownHeight = dropdownRef.value?.offsetHeight ?? 0;

  // Theo chiều ngang: mặc định căn trái với trigger; nếu phía phải không đủ chỗ thì dịch sang trái để hiển thị đầy đủ
  let left = rect.left;
  if (dropdownWidth && left + dropdownWidth > viewportWidth - margin) {
    left = viewportWidth - dropdownWidth - margin;
  }
  if (left < margin) left = margin;

  // Theo chiều dọc: mặc định ở phía dưới; nếu phía dưới không đủ chỗ và phía trên đủ không gian thì lật lên trên
  let top = rect.bottom + DROPDOWN_GAP;
  if (dropdownHeight && top + dropdownHeight > viewportHeight - margin) {
    const aboveTop = rect.top - DROPDOWN_GAP - dropdownHeight;
    top = aboveTop >= margin ? aboveTop : Math.max(margin, viewportHeight - dropdownHeight - margin);
  }

  dropdownPlaceStyle.value = { left: `${left}px`, top: `${top}px` };
  // update-end--author:liaozhiyang---date:20260714---for：【LHZP-100] Tỉnh/Thành/Quận huyện khi ở mép trang, cấp cuối cùng không chọn được
}

// update-begin--author:liaozhiyang---date:20260714---for：【LHZP-100] Tỉnh/Thành/Quận huyện khi ở mép trang, cấp cuối cùng không chọn được
watch(columns, () => {
  if (visible.value) {
    nextTick(updatePopupPosition);
  }
});
// update-end--author:liaozhiyang---date:20260714---for：【LHZP-100] Tỉnh/Thành/Quận huyện khi ở mép trang, cấp cuối cùng không chọn được

/** Khi mở rộng: dùng value hiện tại để khởi tạo editingPath, tính toán vị trí, đăng ký đóng khi click bên ngoài; khi thu gọn thì gỡ bỏ lắng nghe */
watch(visible, (v) => {
  if (v) {
    editingPath.value = normalizePath(props.value).slice();
    nextTick(() => {
      updatePopupPosition();
      setTimeout(() => document.addEventListener('click', handleClickOutside, true), 0);
    });
  } else {
    document.removeEventListener('click', handleClickOutside, true);
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside, true);
});
</script>

<!-- Dùng khi danh sách thả xuống gắn vào body .cascader-dropdown-placement ghi đè thành position: fixed -->
<style scoped lang="less">
@import (reference) '/@/design/config.less';

.cascader {
  position: relative;
  display: inline-block;
  width: 100%;
  min-width: 0;
}

.cascader .ant-input-affix-wrapper {
  cursor: pointer;

  .ant-input-clear-icon {
    display: none;
  }

  &:hover {
    .ant-input-clear-icon {
      display: block;
    }

    .cascader-arrow {
      opacity: 0;
    }
  }

  &.ant-input-affix-wrapper-disabled,
  &.ant-input-affix-wrapper-disabled:hover {
    cursor: not-allowed;

    .ant-input-clear-icon {
      display: none !important;
    }

    .cascader-arrow {
      opacity: 1;
    }
  }
}

.cascader-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.cascader-arrow.is-open {
  transform: rotate(180deg);
}

:deep(.ant-input-suffix) {
  position: relative;

  .ant-input-clear-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0;
    z-index: 1;
  }
}

.cascader-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1050;
  margin-top: 4px;
  background: @component-background;
  border-radius: 6px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
}

.cascader-dropdown-placement {
  position: fixed;
  margin-top: 0;
}

.cascader-menus {
  display: flex;
  min-height: 180px;
}

.cascader-menu {
  padding: 5px;
  min-width: 112px;
  max-height: 180px;
  overflow-y: auto;
  background: @component-background;

  &:not(:last-child) {
    border-right: 1px solid @border-color-split;
  }
}

.cascader-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 12px;
  font-size: 14px;
  line-height: 22px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.cascader-menu-item-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cascader-menu-item-arrow {
  margin-left: 8px;
  font-size: 10px;
  color: @text-color-secondary;
  flex-shrink: 0;
}

.cascader-menu-item:hover {
  background: @item-hover-bg;
}

.cascader-menu-item.is-active .cascader-menu-item-arrow,
.cascader-menu-item.is-selected .cascader-menu-item-arrow {
  color: var(--j-global-primary-color);
}

.cascader-menu-item.is-active {
  background: @primary-1;
  color: var(--j-global-primary-color);
}

@supports (background: color-mix(in srgb, red, blue)) {
  .cascader-menu-item.is-active {
    background: color-mix(in srgb, var(--j-global-primary-color) 10%, #fff);
  }
}

.cascader-menu-item.is-selected {
  font-weight: 500;
  color: var(--j-global-primary-color);
}

.cascader-dropdown-enter-active,
.cascader-dropdown-leave-active {
  transition: opacity 0.15s;
}

.cascader-dropdown-enter-from,
.cascader-dropdown-leave-to {
  opacity: 0;
}
</style>
