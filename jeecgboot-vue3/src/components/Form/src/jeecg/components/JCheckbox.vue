<template>
  <a-checkbox-group v-bind="attrs" v-model:value="checkboxArray" :options="checkOptions" @change="handleChange">
    <template #label="{label, value}">
      <span :class="[useDicColor && getDicColor(value) ? 'colorText' : '']" :style="{ backgroundColor: `${getDicColor(value)}` }">{{ label }}</span>
    </template>
  </a-checkbox-group>
</template>

<script lang="ts">
  import { defineComponent, computed, watch, watchEffect, ref, unref } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import {getDictItems} from "@/api/common/api";

  export default defineComponent({
    name: 'JCheckbox',
    props: {
      value:propTypes.oneOfType([propTypes.string, propTypes.number]),
      dictCode: propTypes.string,
      useDicColor: propTypes.bool.def(false),
      options: {
        type: Array,
        default: () => [],
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //checkboxTùy chọn
      const checkOptions = ref<any[]>([]);
      //checkboxGiá trị số
      const checkboxArray = ref<any[]>([]);
      /**
       * Lắng nghevalue
       */
      watchEffect(() => {
        // Giải thích logic mã: issues/I5E7YX AUTOBiểu mẫu online sau khi vào kiểm thử chức năng thì luôn bị kẹt ở giao diện kiểm thử chức năng
        let temp = props.value;
        if(!temp && temp!==0){
          checkboxArray.value = []
        }else{
          temp = temp + '';
          checkboxArray.value = temp.split(',')
        }
        // Giải thích logic mã: Gọi resetFields của biểu mẫu không xóa thông tin hiện tại, giao diện hiển thị dữ liệu lần trước
        if (props.value === '' || props.value === undefined) {
          checkboxArray.value = [];
        }
      });
      /**
       * Lắng nghe danh mụccode
       */
      watchEffect(() => {
        props && initOptions();
      });

      /**
       * Khởi tạo tùy chọn
       */
      async function initOptions() {
        //Theooptions, Khởi tạo tùy chọn
        if (props.options && props.options.length > 0) {
          checkOptions.value = props.options;
          return;
        }
        //Theo từ điểnCode, Khởi tạo tùy chọn
        if (props.dictCode) {
          loadDictOptions()
        }
      }

      // Truy vấn mục từ điển theo code từ điển
      function loadDictOptions() {
        // Giải thích logic mã: Trước khi yêu cầu dữ liệu từ điển, mã hóa tham số, nhưng không thể mã hóa trực tiếp vì có thể trước đây đã mã hóa rồi
        let temp = props.dictCode || '';
        if (temp.indexOf(',') > 0 && temp.indexOf(' ') > 0) {
          // Sau khi mã hóa không chứa khoảng trắng
          temp = encodeURI(temp);
        }
        getDictItems(temp).then((res) => {
          if (res) {
            checkOptions.value = res.map((item) => ({value: item.value, label: item.text, color: item.color}));
            //console.info('res', dictOptions.value);
          } else {
            console.error('getDictItems error: : ', res);
            checkOptions.value = [];
          }
        });
      }

      /**
       * changeSự kiện
       * @param $event
       */
      function handleChange($event) {
        emit('update:value', $event.join(','));
        emit('change', $event.join(','));
      }
      const getDicColor = (value) => {
        if (props.useDicColor) {
          const findItem = checkOptions.value.find((item) => item.value == value);
          if (findItem) {
            return findItem.color;
          }
        }
        return null;
      };
      return { checkboxArray, checkOptions, attrs, handleChange, getDicColor };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-7799] Component từ điển (trừ component gốc) thêm cấu hình màu sắc
  .colorText {
    display: inline-block;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    background-color: red;
    color: #fff;
    font-size: 12px;
  }
</style>
