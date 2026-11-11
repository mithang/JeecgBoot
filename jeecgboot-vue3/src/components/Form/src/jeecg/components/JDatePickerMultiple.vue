<!-- Chọn nhiều ngày -->
<template>
  <div class="j-date-multiple" ref="wrapperRef">
    <!-- Hiển thị mục đã chọn: dùng chế độ nhãn chọn nhiều của ASelect, cấm tự mở dropdown, chỉ để hiển thị và xóa/Lối vào xóa -->
    <a-select
      :value="displayValues"
      mode="multiple"
      :placeholder="placeholder"
      :disabled="disabled"
      allowClear
      :open="false"
      :getPopupContainer="getParentContainer"
      v-bind="attrs"
      @change="onSelectChange"
      @click="openPanel"
    />

    <!-- Trigger ẩn, chỉ dùng để chứa lớp bật lên và chuyển đổi tháng, bảng được mở có kiểm soát -->
    <a-date-picker
      ref="pickerRef"
      :value="void 0"
      :open="open"
      :getPopupContainer="getParentContainer"
      :inputReadOnly="true"
      :allowClear="false"
      :disabledDate="disabledDate"
      :format="format"
      :showToday="false"
      @openChange="onOpenChange"
      class="hidden-picker"
      popupClassName="j-date-multiple-popup"
    >
      <template #dateRender="{ current }">
        <div
          class="ant-picker-cell-inner jdm-cell"
          :class="{ 'is-selected': isSelected(current) }"
          @click.stop.prevent="onCellClick(current)"
        >
          {{ current.date() }}
        </div>
      </template>
    </a-date-picker>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from 'vue';
import dayjs, {Dayjs} from 'dayjs';
import {propTypes} from '/@/utils/propTypes';
import {useAttrs} from '/@/hooks/core/useAttrs';
import {useRuleFormItem} from '/@/hooks/component/useFormItem';
import {setPopContainer} from '/@/utils';

const props = defineProps({
  value: propTypes.string.def(''), // Nhiều ngày được nối bằng ký tự phân tách
  placeholder: propTypes.string.def('Vui lòng chọn ngày'),
  separator: propTypes.string.def(','),
  format: propTypes.string.def('YYYY-MM-DD'), // Định dạng hiển thị
  valueFormat: propTypes.string.def('YYYY-MM-DD'), // Định dạng lưu trữ
  disabled: propTypes.bool.def(false),
  popContainer: propTypes.string.def(''),
  // Quy tắc vô hiệu hóa truyền thẳng cho DatePicker
  disabledDate: {
    type: Function as unknown as () => (current: Dayjs) => boolean,
    default: undefined,
  },
});

const emit = defineEmits(['change', 'update:value']);

// attrs Truyền xuyên thấu sang ASelect để tái sử dụng kích thước/kiểu dáng, v.v.
const attrs = useAttrs();

// Liên động với kiểm chứng biểu mẫu
const emitData = ref<any[]>([]);
const [, , , formItemContext] = useRuleFormItem(props as any, 'value', 'change', emitData);

const wrapperRef = ref<HTMLElement | null>(null);
const pickerRef = ref();
const open = ref(false);

// Danh sách chọn nội bộ (mảng chuỗi định dạng lưu trữ)
const selectedValueStrings = ref<string[]>([]);

// Khởi tạo từ value bên ngoài/Đồng bộ lựa chọn nội bộ
watch(
  () => props.value,
  (val) => {
    if (!val) {
      selectedValueStrings.value = [];
    } else {
      const arr = String(val)
        .split(props.separator)
        .map((s) => s && s.trim())
        .filter((s) => !!s) as string[];
      // Khử trùng lặp
      const set = new Set(arr);
      selectedValueStrings.value = Array.from(set);
    }
  },
  {immediate: true}
);

// Giá trị hiển thị (định dạng thành format)
const displayValues = computed<string[]>(() => {
  return selectedValueStrings.value.map((sv) => {
    const d = dayjs(sv, props.valueFormat);
    if (d.isValid()) {
      return d.format(props.format);
    } else {
      return sv;
    }
  });
});

// Container của bộ chọn: ưu tiên popContainer, tiếp theo dùng phần tử bao bọc của component này
function getParentContainer(node: HTMLElement) {
  if (!props.popContainer) {
    return wrapperRef.value ?? node?.parentNode;
  } else {
    return setPopContainer(node, props.popContainer);
  }
}

function openPanel() {
  if (props.disabled) {
    return;
  }
  open.value = true;
}

function onOpenChange(val: boolean) {
  // Chỉ đóng khi nhấn bên ngoài; nhấn ngày do chúng ta chặn, sẽ không kích hoạt đóng mặc định
  open.value = val;
}

function isSelected(current: Dayjs) {
  const key = current.format(props.valueFormat);
  return selectedValueStrings.value.includes(key);
}

function onCellClick(current: Dayjs) {
  if (props.disabled) {
    return;
  }
  if (props.disabledDate && props.disabledDate(current)) {
    return;
  }
  const key = current.format(props.valueFormat);
  const idx = selectedValueStrings.value.indexOf(key);
  if (idx >= 0) {
    selectedValueStrings.value.splice(idx, 1);
  } else {
    selectedValueStrings.value.push(key);
  }
  triggerChange();
  // Giữ bảng mở
  nextTick(() => {
    open.value = true;
  });
}

function onSelectChange(nextDisplayValues: string[]) {
  // Khôi phục từ giá trị hiển thị về định dạng lưu trữ
  const nextStoreValues: string[] = [];
  for (const dv of nextDisplayValues) {
    const d = dayjs(dv, props.format);
    if (d.isValid()) {
      nextStoreValues.push(d.format(props.valueFormat));
    }
  }
  // Khử trùng lặp
  const set = new Set(nextStoreValues);
  selectedValueStrings.value = Array.from(set);
  triggerChange();
}

function triggerChange() {
  const joined = selectedValueStrings.value.join(props.separator);
  emit('change', joined);
  emit('update:value', joined);
  nextTick(() => {
    if (formItemContext && formItemContext.onFieldChange) {
      formItemContext.onFieldChange();
    }
  });
}

onMounted(() => {
  // nothing now
});

</script>

<style lang="less">
.j-date-multiple {
  position: relative;
  width: 100%;

  .hidden-picker {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 32px;
    opacity: 0;
    pointer-events: none;
  }
}

.j-date-multiple-popup.ant-picker-dropdown {
  .ant-picker-cell {
    .ant-picker-cell-inner.jdm-cell {
      &.is-selected {
        color: #fff !important;
        // noinspection LessUnresolvedVariable
        background-color: @primary-color !important;
      }
    }
  }
}
</style>
