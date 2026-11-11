<!--Component chọn phòng ban-->
<template>
  <div class="JSelectDepartPost">
    <JSelectBiz @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs" :isCustomRenderTag="isCustomRenderTag" :rowKey="getBindValue?.rowKey"/>
    <a-form-item>
      <DeptSelectModal @register="regModal" @getSelectResult="setValue" modalTitle="Chọn phòng ban và vị trí công tác" v-bind="getBindValue" :multiple="multiple" @close="handleClose" :izShowDepPath="izShowDepPath"/>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import DeptSelectModal from './modal/DeptSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, unref, toRaw } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';

  export default defineComponent({
    name: 'JSelectDepartPost',
    components: {
      DeptSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      // Có cho phép chọn nhiều không, mặc định true
      multiple: propTypes.bool.def(true),
      //Có chỉ chọn vị trí công tác không
      izOnlySelectDepartPost: propTypes.bool.def(true),
      // Kết xuất tùy chỉnhtag
      isCustomRenderTag: propTypes.bool.def(true),
      //Có hiển thị đường dẫn phòng ban hay không (khi người dùng chọn vị trí chính và vị trí kiêm nhiệm của phòng ban cần hiển thị đầy đủ đường dẫn)
      izShowDepPath: propTypes.bool.def(false),
    },
    emits: ['options-change', 'change', 'select', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //Đăng kýmodel
      const [regModal, { openModal }] = useModal();
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
        tempSave = [];
        props.value && initValue();
      });

      watch(
        () => props.value,
        () => {
          initValue();
        }
      );

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
          selectValues.value = value.split(',');
          tempSave = value.split(',');
        } else {
          selectValues.value = value;
          tempSave = cloneDeep(value);
        }
      }

      /**
       * Đặt giá trị của hộp dropdown
       */
      function setValue(options, values) {
        selectOptions.value = options;
        selectValues.value = values;
        send(values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));

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
  .JSelectDepartPost {
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
