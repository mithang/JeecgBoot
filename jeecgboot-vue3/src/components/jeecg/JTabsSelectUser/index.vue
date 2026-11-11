<template>
  <div class="tabsSelectUser" v-bind="$attrs">
    <JSelectBiz @handleOpen="openSelect" allowClear :placeholder="placeholder" :loading="loadingEcho" v-bind="attrs" @change="handleSelectChange"></JSelectBiz>
  </div>
  <teleport to="body">
    <select-user-modal @register="registerModal" :rowKey="rowKey" @selected="onSelected" />
  </teleport>
</template>

<script lang="ts" setup>
  /**
   * Component chọn người dùng - Chọn người dùng bằng cách chuyển đổi nhiều tab
   */
  import { provide, reactive, ref, watch } from 'vue';
  import SelectUserModal from './SelectUserModal.vue';
  import { useModal } from '/src/components/Modal';
  import JSelectBiz from '@/components/Form/src/jeecg/components/base/JSelectBiz.vue';
  import { propTypes } from '@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray, isString, isObject } from '/@/utils/is';
  import { getTableList as getTableListOrigin } from '/@/api/common/api';

  defineOptions({ name: 'JTabsSelectUser' });
  //Định nghĩaprops
  const props = defineProps({
    placeholder:{
      type: String,
      default: 'Chọn nhân sự',
    },
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    rowKey: {
      type: String,
      default: 'username',
    },
    labelKey: {
      type: String,
      default: 'realname',
    },
  });
  //Định nghĩaemit
  const emit = defineEmits(['options-change', 'change', 'update:value']);
  const attrs: any = useAttrs();
  const [registerModal, { openModal }] = useModal();
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

  function openSelect() {
    let arr = getModalData();
    openModal(true, {
      selected: arr,
    });
  }

  function getModalData() {
    //Tìmrows
    let arr = selectOptions.value;
    //Tìmusername
    let arr2 = selectValues.value;
    let dataArray = arr.filter((item) => arr2.indexOf(item[props.rowKey]) >= 0);
    return dataArray;
  }

  function onSelected(data) {
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
    emit('change', selectValues.value,selectOptions.value);
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
    emit('change', selectValues.value,selectOptions.value);
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
          selectValues.value = value.split(',');
          return;
        }
        const params = { isMultiTranslate: true, pageSize: len, [props.rowKey]: value };
        if (isObject(attrs.params)) {
          Object.assign(params, attrs.params);
        }
        console.log('JTabsSelectUser params', params);
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
  // update-begin--author:liaozhiyang---date:20240605---for：【TV360X-1050]Trang chỉ định người xử lý tiếp theo trên trình duyệt Safari, control chưa căn chỉnh
  .ant-select {
    vertical-align: middle;
  }
  // update-end--author:liaozhiyang---date:20240605---for：【TV360X-1050]Trang chỉ định người xử lý tiếp theo trên trình duyệt Safari, control chưa căn chỉnh

  // Kiểu nút tùy chỉnh
  .custom-btn {
    height: 28px !important;
    padding: 0 12px !important;
    font-size: 13px !important;
  }
</style>
