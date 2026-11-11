<template>
  <div class="JAuthRuleValue">
    <a-input-group compact>
      <!-- Bộ chọn biến hệ thống: dùng Dropdown thay vì Select, tránh bị FormItem bên ngoài thu thập lần hai -->
      <a-dropdown class="sys-var-dropdown" :trigger="['click']" :getPopupContainer="getPopupContainer" v-model:open="dropdownVisible">
        <a-button type="default" size="middle" :class="isSysVarMode ? 'sys-var-button-active' : 'sys-var-button-inactive'">
          <span class="sys-var-label">{{ selectedSysVarLabel || selectPlaceholder }}</span>
          <DownOutlined />
        </a-button>
        <template #overlay>
          <a-menu @click="handleMenuClick">
            <a-menu-item v-for="item in options" :key="item.value">{{ item.label }}</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>

      <!-- Ô nhập văn bản mặc định (chưa cấu hình component chuyên dụng hoặc đang ở chế độ biến hệ thống) -->
      <a-input
        v-if="!innerComponent || isSysVarMode"
        allow-clear
        :placeholder="inputPlaceholder"
        v-model:value="inputVal"
        :getPopupContainer="getPopupContainer"
        @change="handleInputChange"
      />

      <!-- Chọn từ điển -->
      <JDictSelectTag v-else-if="innerComponent === 'JDictSelectTag'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />

      <!-- Bản ghi liên kết -->
      <div v-else-if="innerComponent === 'JLinkTableCard'" class="link-table-rule-value">
        <JLinkTableCard v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />
      </div>

      <!-- Popup Từ điển -->
      <JPopupDict v-else-if="innerComponent === 'JPopupDict'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />

      <!-- Chọn người dùng -->
      <JSelectUser v-else-if="innerComponent === 'JSelectUser'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />

      <!-- Chọn phòng ban -->
      <JSelectDept v-else-if="innerComponent === 'JSelectDept'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />

      <!-- Tỉnh/Thành/Quận huyện -->
      <JAreaLinkage v-else-if="innerComponent === 'JAreaLinkage'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />

      <!-- Ngày -->
      <DatePicker
        v-else-if="innerComponent === 'DatePicker'"
        v-bind="innerProps"
        :value="datePickerValue"
        v-model:open="datePickerOpen"
        @change="handleDatePickerChange"
      />

      <!-- Thời gian -->
      <TimePicker v-else-if="innerComponent === 'TimePicker'" v-bind="innerProps" :value="inputVal" @change="handleCustomChange" />
    </a-input-group>
  </div>
</template>

<script setup name="JAuthRuleValue" lang="ts">
  import { ref, watchEffect, computed } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { DownOutlined } from '@ant-design/icons-vue';
  import JDictSelectTag from './JDictSelectTag.vue';
  import JLinkTableCard from './JLinkTableCard/JLinkTableCard.vue';
  import JPopupDict from './JPopupDict.vue';
  import JSelectUser from './JSelectUser.vue';
  import JSelectDept from './JSelectDept.vue';
  import JAreaLinkage from './JAreaLinkage.vue';
  import { DatePicker, TimePicker } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import weekOfYear from 'dayjs/plugin/weekOfYear';
  import quarterOfYear from 'dayjs/plugin/quarterOfYear';

  dayjs.extend(weekOfYear);
  dayjs.extend(quarterOfYear);

  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
    // Cấu hình component thực tế bên trong { component: 'JSelectUser', componentProps: {...} }
    customComponent: propTypes.object.def(null),
    // Tùy chọn biến hệ thống
    options: propTypes.array.def([]),
    selectPlaceholder: propTypes.string.def('Chọn biến hệ thống'),
    inputPlaceholder: propTypes.string.def('Vui lòng nhập'),
    getPopupContainer: {
      type: Function,
      default: () => document.body,
    },
  });
  const emit = defineEmits(['update:value', 'change']);

  const inputVal = ref<any>();
  const dropdownVisible = ref(false);
  const datePickerOpen = ref(false);

  const innerComponent = computed(() => props.customComponent?.component);
  const innerProps = computed(() => props.customComponent?.componentProps || {});
  // Tương thích với giá trị quy tắc đã lưu trước đây theo năm/tháng/Tuần/Giá trị quy tắc được lưu theo định dạng hiển thị theo quý, chuyển đổi thành định dạng ổn định trước khi truyền cho DatePicker YYYY-MM-DD
  const datePickerValue = computed(() => {
    const value = inputVal.value;
    const picker = innerProps.value?.picker;
    if (!picker || typeof value !== 'string' || /^\d{4}-\d{2}-\d{2}$/.test(value)) return value;
    if (picker === 'year' && /^\d{4}$/.test(value)) return `${value}-01-01`;
    if (picker === 'month' && /^\d{4}-\d{2}$/.test(value)) return `${value}-01`;
    const quarterMatch = value.match(/^(\d{4})-[Qq]([1-4])$/);
    if (picker === 'quarter' && quarterMatch) {
      return dayjs(`${quarterMatch[1]}-01-01`).quarter(Number(quarterMatch[2])).startOf('quarter').format('YYYY-MM-DD');
    }
    const weekMatch = value.match(/^(\d{4})-(\d+)/);
    if (picker === 'week' && weekMatch) {
      return dayjs(`${weekMatch[1]}-01-01`).week(Number(weekMatch[2])).startOf('week').format('YYYY-MM-DD');
    }
    return value;
  });

  const isSysVar = (val) => val != null && props.options.some((item) => item.value === val);
  // Khi giá trị hiện tại là biến hệ thống, phía bên phải thống nhất quay về ô nhập thông thường, tránh component chuyên dụng phân tích cú pháp #{...} Chuỗi
  const isSysVarMode = computed(() => isSysVar(inputVal.value));

  const selectedSysVarLabel = computed(() => {
    const found = props.options.find((item) => item.value === inputVal.value);
    return found ? found.label : '';
  });

  const handleInputChange = (e) => {
    emits(e.target.value);
  };
  const handleMenuClick = ({ key }) => {
    inputVal.value = key;
    emits(key);
    dropdownVisible.value = false;
  };
  const handleCustomChange = (val) => {
    inputVal.value = val;
    emits(val);
  };
  const handleDatePickerChange = (val) => {
    handleCustomChange(val);
    datePickerOpen.value = false;
  };
  const emits = (val) => {
    emit('update:value', val);
    emit('change', val);
  };

  watchEffect(() => {
    inputVal.value = props.value;
  });
</script>

<style lang="less" scoped>
  .JAuthRuleValue {
    .ant-input-group {
      display: flex;
      width: 100%;
    }
    // a-dropdown Không sinh nút bao bọc, class sẽ được hợp nhất trực tiếp vào nút bên trong, cần dùng bộ chọn class cùng cấp
    .sys-var-dropdown.ant-btn {
      flex: 0 0 auto;
      border-top-right-radius: 0;
      border-bottom-right-radius: 0;
      box-shadow: none;
      transition:
        background-color 0.2s,
        border-color 0.2s,
        color 0.2s;
    }
    .sys-var-dropdown.sys-var-button-inactive {
      border-color: @border-color-base !important;
      background: fade(@text-color, 2%) !important;
      color: fade(@text-color, 35%) !important;
      font-weight: 400;

      &:hover,
      &:focus,
      &:active {
        border-color: @border-color-base !important;
        background: fade(@text-color, 5%) !important;
        color: fade(@text-color, 50%) !important;
      }
    }
    .sys-var-dropdown.sys-var-button-active {
      border-color: @primary-color !important;
      background: fade(@primary-color, 8%) !important;
      color: @primary-color !important;

      &:hover,
      &:focus,
      &:active {
        background: fade(@primary-color, 12%) !important;
        color: @primary-color !important;
      }
    }
    .sys-var-label {
      display: inline-block;
      max-width: 140px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      vertical-align: middle;
    }
    .ant-input-group > .ant-input,
    .ant-input-group > :deep(.ant-select),
    .ant-input-group > :deep(.ant-radio-group),
    .ant-input-group > .JselectUser,
    .ant-input-group > .JSelectDept,
    .ant-input-group > .JAreaLinkage,
    .ant-input-group > :deep(.JPopupDict),
    .ant-input-group > .link-table-rule-value,
    .ant-input-group > .ant-picker {
      flex: 1;
      min-width: 0;
    }
    :deep(.JPopupDict > .ant-select),
    :deep(.JPopupDict > .ant-input) {
      width: 100%;
    }
    :deep(.JPopupDict > .ant-select .ant-select-selector),
    :deep(.JPopupDict > .ant-input) {
      border-top-left-radius: 0;
      border-bottom-left-radius: 0;
    }
    .link-table-rule-value {
      padding-left: 8px;
    }
  }
</style>
