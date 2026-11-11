<!--Component chọn phòng ban-->
<template>
  <div class="JSelectDept">
    <JSelectBiz  @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" :isCustomRenderTag="isCustomRenderTag" :rowKey="getBindValue?.rowKey"/>
    <a-form-item>
      <DeptSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue" :multiple="multiple" @close="handleClose"/>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import DeptSelectModal from './modal/DeptSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, unref, toRaw } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';

  export default defineComponent({
    name: 'JSelectDept',
    components: {
      DeptSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      // Có cho phép chọn nhiều không, mặc định true
      multiple: propTypes.bool.def(true),
      // Kết xuất tùy chỉnhtag
      isCustomRenderTag: propTypes.bool.def(true),
    },
    emits: ['options-change', 'change', 'select', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //Đăng kýmodel
      const [regModal, { openModal }] = useModal();
      //Giá trị biểu mẫu
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //Giá trị tùy chọn của hộp dropdown
      const selectOptions = ref<SelectValue>([]);
      //Giá trị đã chọn của hộp dropdown
      let selectValues = reactive<Recordable>({
        value: [],
      });
      let tempSave: any = [];

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
        // Giải thích logic mã: [TV360X-840]Phân quyền người dùng, không chọn gì, nhấn Hủy cũng sẽ hiển thị lại một người dùng đã chọn trước đó
        tempSave = [];
        props.value && initValue();
      });

      // Giải thích logic mã: Để giải quyết vấn đề gán giá trị khởi tạo của form trong popup ---
      watch(
        () => props.value,
        () => {
          initValue();
        }
      );
      /**
       * Lắng nghe sự thay đổi của selectValues
       */
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });
      /**
       * Lắng nghe sự thay đổi của selectOptions
       */
      watch(selectOptions, () => {
        if (selectOptions) {
          emit('select', toRaw(unref(selectOptions)), toRaw(unref(selectValues)));
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
        if (value && typeof value === 'string') {
          // state.value = value.split(',');
          selectValues.value = value.split(',');
          tempSave = value.split(',');
        } else {
          // 【VUEN-857] Tương thích mảng (vấn đề cách dùng chỉnh sửa dòng)
          selectValues.value = value;
          tempSave = cloneDeep(value);
        }
      }

      /**
       * Đặt giá trị của hộp dropdown
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        // state.value = values;
        selectValues.value = values;
        send(values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));

       // Giải thích logic mã: 【TV360X-414] Phòng ban đã đặt giá trị mặc định, đặt lại truy vấn thì thành trống(Đồng bộ cách sửa của component JSelectUser)
      const handleClose = () => {
        if (tempSave.length) {
          selectValues.value = cloneDeep(tempSave);
        } else {
          send(tempSave);
        }
      };
      const handleSelectChange = (values) => {
        tempSave = cloneDeep(values);
        send(tempSave);
      };
      const send = (values) => {
        let result = typeof props.value == 'string' ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
        // Giải thích logic mã: 【TV360X-1648]Giao diện chỉnh sửa người dùng, liên động giữa "Phòng ban trực thuộc" và "Phòng ban phụ trách" bị lỗi (mã bị mất trước khi đồng bộ)
        if (!values || values.length == 0) {
          emit('select', null, null);
        }
      };
      
      return {
        // state,
        attrs,
        selectOptions,
        selectValues,
        loadingEcho,
        getBindValue,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-9260] Ở chế độ bắt buộc sẽ ảnh hưởng đến kiểu của component antd trong popup
  .JSelectDept {
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
