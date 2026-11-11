<!--Component chọn vai trò-->
<template>
  <div class="JSelectRole">
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <a-form-item>
      <RoleSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></RoleSelectModal>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import RoleSelectModal from './modal/RoleSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, unref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';

  export default defineComponent({
    name: 'JSelectRole',
    components: {
      RoleSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'roleName',
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
      let selectValues = reactive<Recordable>({
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
      watchEffect(() => {
        props.value && initValue();
        // Khi đặt lại điều kiện truy vấn, xóa hiển thị giao diện
        if (!props.value) {
          selectValues.value = [];
        }
      });

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
        } else {
          selectValues.value = value;
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
        // Giải thích logic mã: 【issues/7948]Sửa lỗi component JselectRole không hỗ trợ binding hai chiều
        emit('update:value', values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        getBindValue,
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
  .JSelectRole {
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
