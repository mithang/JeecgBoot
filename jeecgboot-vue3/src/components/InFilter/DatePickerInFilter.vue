<!-- Bộ chọn ngày dùng trong bộ lọc (có thể chọn phạm vi như hôm nay, tuần này...) -->
<template>
  <a-space :id="formItemId" :class="[prefixCls]" direction="vertical">
    <a-space-compact block>
      <!-- Danh sách thả xuống phạm vi ngày -->
      <a-select v-if="isRange" v-model:value="innerValue">
        <a-select-option v-for="opt of RANGE_OPTIONS" :key="opt.key" :value="opt.key">
          {{ opt.label }}
        </a-select-option>
        <a-select-option key="custom" value="custom">
          Ngày tùy chỉnh
        </a-select-option>
      </a-select>
      <!-- Tùy chỉnh bộ chọn ngày -->
      <DatePicker v-else v-model:value="innerValue" v-model:open="datePickerIsOpen" v-bind="attrs"/>
      <!-- Menu dropdown bên phải -->
      <a-dropdown v-if="allowSelectRange" :trigger="['click']">
        <a-button preIcon="ant-design:menu-unfold"/>
        <template #overlay>
          <a-menu @click="onMenuClick">
            <a-menu-item v-for="opt of RANGE_OPTIONS" :key="opt.key">
              {{ opt.label }}
            </a-menu-item>
            <a-menu-item key="custom">
              Ngày tùy chỉnh
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </a-space-compact>
  </a-space>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: 'DatePickerInFilter',
  inheritAttrs: false,
})
</script>

<script lang="ts" setup>
import {ref, watch, computed, nextTick, useAttrs} from 'vue'
import {DatePicker} from 'ant-design-vue'
import {useDesign} from '/@/hooks/web/useDesign';
import { Form } from 'ant-design-vue';
import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import quarterOfYear from 'dayjs/plugin/quarterOfYear';

dayjs.extend(isoWeek);
dayjs.extend(quarterOfYear);
const formItemContext = Form.useInjectFormItemContext();

// Tùy chọn phạm vi ngày
const RANGE_OPTIONS = [
  {key: 'TODAY', label: 'Hôm nay'},
  {key: 'YESTERDAY', label: 'Hôm qua'},
  {key: 'TOMORROW', label: 'Ngày mai'},
  {key: 'THIS_WEEK', label: 'Tuần này'},
  {key: 'LAST_WEEK', label: 'Tuần trước'},
  {key: 'NEXT_WEEK', label: 'Tuần sau'},
  {key: 'LAST_7_DAYS', label: '7 ngày qua'},
  {key: 'THIS_MONTH', label: 'Tháng này'},
  {key: 'LAST_MONTH', label: 'Tháng trước'},
  {key: 'NEXT_MONTH', label: 'Tháng sau'},
];
const RANGE_OPTION_KEYS = RANGE_OPTIONS.map(item => item.key)

const {prefixCls} = useDesign('j-data-picker-in-filter');
const props = defineProps({
  value: {
    type: [String],
    default: ''
  },
  // Có cho phép chọn phạm vi đặt trước không
  allowSelectRange: {
    type: Boolean,
    default: true,
  }
})
const emit = defineEmits(['change', 'update:value'])
const attrs = useAttrs()

// Componentid
const formItemId  = computed(() => formItemContext.id.value)

const innerValue = ref(props.value)
// Có mở bộ chọn ngày không
const datePickerIsOpen = ref(false)

// Kiểm tra có phải tùy chọn khoảng không
const isRange = computed(() => RANGE_OPTION_KEYS.includes(innerValue.value));

// Đồng bộvalue
watch(() => props.value, (val) => {
  innerValue.value = val
})

// emit Thay đổi
watch(innerValue, (val) => {
  if (val === 'custom') {
    val = ''
    openDatePicker()
  }
  // update-begin--author:wangshuai---date:20260819---for：【LHZP-322][Trình thiết kế biểu mẫu] online tuần, quý chưa chuyển đổi thành công
  const normalizedValue = normalizePickerValue(val)
  emit('change', normalizedValue)
  emit('update:value', normalizedValue)
  // update-end--author:wangshuai---date:20260819---for：【LHZP-322][Trình thiết kế biểu mẫu] online tuần, quý chưa chuyển đổi thành công
  // Giải thích logic mã: 【QQYUN-9227] kiểm tra ngày chưa được xóa
  formItemContext?.onFieldChange();
})

/** Quý lấy ngày đầu tiên của quý, tuần lấy thứ Hai của tuần ISO. */
function normalizePickerValue(value) {
  if (!value || RANGE_OPTION_KEYS.includes(value)) {
    return value
  }
  const picker = attrs.picker
  if (picker === 'quarter') {
    return dayjs(value).startOf('quarter').format('YYYY-MM-DD')
  }
  if (picker === 'week') {
    return dayjs(value).startOf('isoWeek').format('YYYY-MM-DD')
  }
  return value
}

watch(() => props.allowSelectRange, (allow) => {
  // Nếu không cho phép chọn phạm vi, và giá trị hiện tại là tùy chọn phạm vi, thì xóa giá trị
  if (!allow && isRange.value) {
    innerValue.value = ''
  }
}, {immediate: true});

// Nhấn menu thả xuống bên phải
function onMenuClick(event: Recordable) {
  if (event.key === 'custom') {
    if (isRange.value) {
      innerValue.value = '';
    }
    openDatePicker()
  } else {
    innerValue.value = event.key
  }
}

// Mở danh sách thả xuống của bộ chọn ngày
async function openDatePicker() {
  await nextTick()
  datePickerIsOpen.value = true
}

</script>

<style lang="less">
//noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-j-data-picker-in-filter';

.@{prefix-cls} {
  width: 100%;
}
</style>
