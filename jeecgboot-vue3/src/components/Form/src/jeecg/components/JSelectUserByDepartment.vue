<!--Component chọn người dùng-->
<template>
  <div>
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" @change="handleSelectChange"></JSelectBiz>
    <JSelectUserByDepartmentModal
      v-if="modalShow"
      :selectedUser="selectOptions"
      :modalTitle="modalTitle"
      :rowKey="rowKey"
      :labelKey="labelKey"
      :isRadioSelection="isRadioSelection"
      :params="params"
      @register="regModal"
      @change="setValue"
      @close="() => (modalShow = false)"
      v-bind="attrs"
    ></JSelectUserByDepartmentModal>
  </div>
</template>
<script lang="ts" setup>
  import { ref, reactive, watch, provide } from 'vue';
  import JSelectUserByDepartmentModal from './modal/JSelectUserByDepartmentModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { isArray, isString, isObject } from '/@/utils/is';
  import { getTableList as getTableListOrigin } from '/@/api/common/api';
  import { useMessage } from '/@/hooks/web/useMessage';

  defineOptions({ name: 'JSelectUserByDepartment' });
  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    modalTitle: {
      type: String,
      default: 'Chọn người dùng theo phòng ban',
    },
    rowKey: {
      type: String,
      default: 'username',
    },
    labelKey: {
      type: String,
      default: 'realname',
    },
    //Tham số truy vấn
    params: {
      type: Object,
      default: () => {},
    },
    isRadioSelection: {
      type: Boolean,
      default: false,
    },
  });
  const emit = defineEmits(['options-change', 'change', 'update:value']);
  const { createMessage } = useMessage();
  //Đăng kýmodel
  const [regModal, { openModal }] = useModal();
  // Có hiển thị popup không
  const modalShow = ref(false);
  //Giá trị tùy chọn của hộp dropdown
  const selectOptions: any = ref<SelectValue>([]);
  //Giá trị đã chọn của hộp dropdown
  let selectValues: any = reactive<object>({
    value: [],
    change: false,
  });
  // Có đang tải dữ liệu hiển thị lại không
  const loadingEcho = ref<boolean>(false);
  //Gửi xuống selectOptions,xxxBizComponent nhận
  provide('selectOptions', selectOptions);
  //Gửi xuống selectValues,xxxBizComponent nhận
  provide('selectValues', selectValues);
  //Gửi xuống loadingEcho,xxxBizComponent nhận
  provide('loadingEcho', loadingEcho);

  const attrs: any = useAttrs();

  // Mở popup
  function handleOpen() {
    modalShow.value = true;
    setTimeout(() => {
      openModal(true, {
        isUpdate: false,
      });
    }, 0);
  }
  const handleSelectChange = (data) => {
    selectOptions.value = selectOptions.value.filter((item) => data.includes(item[props.rowKey]));
    setValue(selectOptions.value);
  };
  // Đặt giá trị của hộp dropdown
  const setValue = (data) => {
    selectOptions.value = data.map((item) => {
      return {
        ...item,
        label: item[props.labelKey],
        value: item[props.rowKey],
      };
    });
    selectValues.value = data.map((item) => item[props.rowKey]);
    // Cập nhậtvalue
    emit('update:value', selectValues.value);
    // Kích hoạt sự kiện change (không chuyển đổi vì khi basicForm submit sẽ tự động chuyển chuỗi thành mảng)
    emit('change', selectValues.value);
    // Kích hoạtoptions-changeSự kiện
    emit('options-change', selectOptions.value);
  };
  // Dịch
  const transform = () => {
    let value = props.value;
    let len;
    if (isArray(value) || isString(value)) {
      if (isArray(value)) {
        len = value.length;
        value = value.join(',');
      } else {
        len = value.split(',').length;
      }
      value = value.trim();
      if (value) {
        // Nếu giá trị của value tồn tại trong selectedUser thì không yêu cầu dịch
        let isNotRequestTransform = false;
        isNotRequestTransform = value.split(',').every((value) => !!selectOptions.value.find((item) => item[props.rowKey] === value));
        if (isNotRequestTransform) {
          selectValues.value = value.split(',')
          return;
        }
        const params = { isMultiTranslate: true, pageSize: len, [props.rowKey]: value };
        if (isObject(attrs.params)) {
          Object.assign(params, attrs.params);
        }
        getTableListOrigin(params).then((result: any) => {
          const records = result.records ?? [];
          selectValues.value = records.map((item) => item[props.rowKey]);
          selectOptions.value = records.map((item) => {
            return {
              ...item,
              label: item[props.labelKey],
              value: item[props.rowKey],
            };
          });
        });
      }
    } else {
      selectValues.value = [];
    }
  };
  // Lắng nghe thay đổi của value
  watch(
    () => props.value,
    () => {
      transform();
    },
    { deep: true, immediate: true }
  );
</script>
<style lang="less" scoped>
  .j-select-row {
    @width: 82px;
    .left {
      width: calc(100% - @width - 8px);
    }
    .right {
      width: @width;
    }
    .full {
      width: 100%;
    }
    :deep(.ant-select-search__field) {
      display: none !important;
    }
  }
</style>
