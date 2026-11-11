<!--Component chọn người dùng-->
<template>
  <div>
    <JSelectBiz @change="handleChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <UserSelectByDepPostModal :rowKey="rowKey" @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></UserSelectByDepPostModal>
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectByDepPostModal from './modal/UserSelectByDepPostModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  export default defineComponent({
    name: 'JSelectUserByDeptPost',
    components: {
      UserSelectByDepPostModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      rowKey: {
        type: String,
        default: 'username',
      },
      labelKey: {
        type: String,
        default: 'realname',
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
      let selectValues = reactive<object>({
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
        initValue();
      });

      /**
       * Lắng nghe sự thay đổi của selectValues
       */
      watch(selectValues, () => {
        if (selectValues) {
          // Giải thích logic mã: 【QQYUN-12869]Qua component chọn người dùng theo phòng ban, ở trạng thái bắt buộc sau khi chọn người dùng rồi bấm đặt lại thì sẽ xuất hiện thông tin kiểm chứng
          if (props.value === undefined && selectValues.value?.length == 0) {
            return;
          }
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
        emit('update:value', values);
        emit('options-change', options);
      }

      function handleChange(values) {
        emit('update:value', values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleChange,
      };
    },
  });
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
