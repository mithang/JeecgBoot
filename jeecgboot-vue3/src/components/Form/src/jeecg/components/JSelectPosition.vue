<!--Component chọn chức danh-->
<template>
  <div class="JSelectPosition">
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" @change="(changeValue) => $emit('update:value', changeValue)"></JSelectBiz>
    <a-form-item>
      <PositionSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></PositionSelectModal>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import PositionSelectModal from './modal/PositionSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, computed, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';

  export default defineComponent({
    name: 'JSelectPosition',
    components: {
      PositionSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'name',
      },
      rowKey: {
        type: String,
        default: 'id',
      },
      params: {
        type: Object,
        default: () => {},
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //Đăng kýmodel
      const [regModal, { openModal }] = useModal();
      //Giá trị biểu mẫu
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //Giá trị tùy chọn của hộp dropdown
      const selectOptions = ref<SelectValue>([]);
      //Giá trị đã chọn của hộp dropdown
      let selectValues = reactive<any>({
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

      const tag = ref(false);
      const attrs = useAttrs();

      /**
       * Lắng nghe giá trị component
       */
      // Giải thích logic mã: 【pull/8014]Trong popup dạng slot, hủy trạng thái chọn của checkbox dữ liệu này thì cần bấm lần thứ hai mới có hiệu lực.
      watch(
        () => props.value,
        () => {
          if (props.value) {
            initValue();
          } else {
            // Giải thích logic mã: 【issues/8233] Khi resetFields không thể đặt lại
            if (selectValues.value?.length) {
              selectValues.value = [];
            }
          }
        },
        { deep: true, immediate: true }
      );

      /**
       * Lắng nghe sự thay đổi của selectValues
       */
      watch(selectValues, () => {
        if (selectValues) {
          state.value = selectValues.value;
        }
      });

      /**
       * Hộp popup chấm công
       */
      function handleOpen() {
        tag.value = true;
        openModal(true, {
          isUpdate: false,
        });
      }

      /**
       * Chuyển giá trị chuỗi thành mảng
       */
      function initValue() {
        let value = props.value ? props.value : [];
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          state.value = value.split(',');
          selectValues.value = value.split(',');
        }
      }

      /**
       * Đặt giá trị của hộp dropdown
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        state.value = values;
        selectValues.value = values;
        // Giải thích logic mã: Component chọn chức danhv-modelGiá trị liên kết theo cách v-model không có hiệu lực
        emit('update:value', values.join(','));

      }

      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        getBindValue,
        attrs,
        selectOptions,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
      };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-9260] Ở chế độ bắt buộc sẽ ảnh hưởng đến kiểu của component antd trong popup
  .JSelectPosition {
    > .ant-form-item {
      display: none;
    }
  }
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
