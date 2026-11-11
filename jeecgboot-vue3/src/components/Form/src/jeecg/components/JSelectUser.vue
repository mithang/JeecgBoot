<!--Component chọn người dùng-->
<template>
  <div class="JselectUser">
    <JSelectBiz @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <a-form-item>
      <UserSelectModal
        :rowKey="rowKey"
        @register="regModal"
        @getSelectResult="setValue"
        v-bind="getBindValue"
        :excludeUserIdList="excludeUserIdList"
        @close="handleClose"
      />
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectModal from './modal/UserSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, computed } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'JSelectUser',
    components: {
      UserSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'realname',
      },
      rowKey: {
        type: String,
        default: 'username',
      },
      params: {
        type: Object,
        default: () => {},
      },
      //Tập hợp id người dùng bị loại trừ
      excludeUserIdList:{
        type: Array,
        default: () => [],
      }
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit }) {
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
        change: false,
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
        // Khi đặt lại điều kiện truy vấn, giao diện hiển thị chưa được xóa
        if (!props.value) {
          selectValues.value = [];
        }
      });

      /**
       * Lắng nghe sự thay đổi của selectValues
       */
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });

      // Giải thích logic mã: 【QQYUN-5685] 5. Nhân viên đã nghỉ việc có thể chọn chính mình------------
      const excludeUserIdList = ref<any>([]);
      
      /**
       * Cần lắng nghe excludeUserIdList, nếu không modal sẽ không lấy được
       */ 
      watch(()=>props.excludeUserIdList,(data)=>{
        excludeUserIdList.value = data;
      },{ immediate: true })
      
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
      // [Sửa lỗi] Cách viết cũ là snapshot một lần trong giai đoạn setup Object.assign ảnh chụp nhanh,
      // Component cha sau khi updateSchema thay đổi props v-bind="getBindValue" Sẽ không tính lại
      // Sau khi đổi thành computed,props/attrs Bất kỳ thay đổi nào cũng sẽ hợp nhất lại rồi truyền tiếp cho UserSelectModal
      const getBindValue = computed(() => Object.assign({}, unref(props), unref(attrs)));
      // Giải thích logic mã: 【QQYUN-9366]Component chọn người dùng khi hủy và đóng sẽ mang dữ liệu đã chọn vào
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
        let result = typeof props.value == "string" ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
      };
      return {
        // state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        excludeUserIdList,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-9260] Ở chế độ bắt buộc sẽ ảnh hưởng đến kiểu của component antd trong popup
  .JselectUser {
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
