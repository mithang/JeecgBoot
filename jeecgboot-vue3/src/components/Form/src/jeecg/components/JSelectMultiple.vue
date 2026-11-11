<!--Dropdown từ điển chọn nhiều-->
<template>
  <a-select
    :value="arrayValue"
    @change="onChange"
    mode="multiple"
    :filter-option="useLoadDict ? false : filterOption"
    :disabled="disabled"
    :placeholder="placeholder"
    allowClear
    showSearch
    :getPopupContainer="getParentContainer"
    :notFoundContent="loading ? undefined : null"
    @search="handleSearch"
    @dropdown-visible-change="handleDropdownVisibleChange"
    @popupScroll="handlePopupScroll"
  >
    <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template>
    <template v-for="item of getOptions" :key="item.key">
      <a-select-option :value="item.value" :getPopupContainer="getParentContainer">
        <span :class="item.class" :style="item.style">{{ item.text }}</span>
      </a-select-option>
    </template>
  </a-select>
</template>
<script lang="ts">
  import { computed, defineComponent, onMounted, ref, nextTick, watch } from 'vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';
  import { setPopContainer } from '/@/utils';
  import { useScrollLoadDict } from '../hooks/useSelectMultipleScrollLoad';

  export default defineComponent({
    name: 'JSelectMultiple',
    components: {},
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      placeholder: {
        type: String,
        default: 'Vui lòng chọn',
        required: false,
      },
      readOnly: {
        type: Boolean,
        required: false,
        default: false,
      },
      options: {
        type: Array,
        default: () => [],
        required: false,
      },
      triggerChange: {
        type: Boolean,
        required: false,
        default: true,
      },
      spliter: {
        type: String,
        required: false,
        default: ',',
      },
      popContainer: {
        type: String,
        default: '',
        required: false,
      },
      dictCode: {
        type: String,
        required: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      useDicColor: {
        type: Boolean,
        default: false,
      },
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
      // Số bản ghi mỗi trang khi phân trang, chỉ có hiệu lực khi scrollLoad là true và dictCode có định dạng bảng từ điển
      pageSize: { type: Number, default: 10 },
      // Có tải cuộn hay không, khi là true và dictCode có định dạng bảng từ điển(table,text,code)thì đi qua /sys/dict/loadDict/, nếu không sẽ dùng initDictOptions
      scrollLoad: { type: Boolean, default: false },
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
    },
    emits: ['options-change', 'change', 'input', 'update:value'],
    setup(props, { emit }) {
      //console.info(props);
      const emitData = ref<any[]>([]);
      const arrayValue = ref<any[]>(
        !props.value ? [] : Array.isArray(props.value) ? props.value : (props.value as string).split(props.spliter)
      );
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change', emitData);
      // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
      const scrollLoadApi = useScrollLoadDict(props, dictOptions, arrayValue);
      const { useLoadDict, loading, loadDictOptions: loadDictOptionsScroll, ensureValueInOptions, handleSearch, handleDropdownVisibleChange, handlePopupScroll, isDictTable } = scrollLoadApi;
      // update-end--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
      // Xử lý tùy chọn dropdown
      const getOptions = computed(() => {
        if (!Array.isArray(dictOptions.value)) {
          return [];
        }
        return dictOptions.value.map((item, index) => {
          const {useDicColor} = props;
          const text = item.text || item.label || item.label || '';
          const key = item.value + '_' + text + '_' + index;
          return {
            key: key,
            text: text,
            value: item.value,
            color: item.color,
            class: [useDicColor && item.color ? 'colorText' : ''],
            style: {backgroundColor: `${useDicColor && item.color}`},
          };
        });
      });

      onMounted(() => {
        if (props.dictCode) {
          loadDictOptions();
        } else {
          dictOptions.value = props.options;
        }
      });

      watch(
          () => props.dictCode,
          () => {
            if (props.dictCode) {
              loadDictOptions();
            } else {
              dictOptions.value = props.options;
            }
          }
      );

      watch(
        () => props.value,
        (val) => {
          if (!val) {
            arrayValue.value = [];
          } else {
            arrayValue.value = Array.isArray(props.value) ? props.value : (props.value as string).split(props.spliter);
          }
          if (useLoadDict.value) ensureValueInOptions();
        }
      );

      //Áp dụng cho thao tác thay đổi động các tùy chọn dropdown
      watch(()=>props.options, ()=>{
        if (props.dictCode) {
          // update-begin--author:liaozhiyang---date:20260325---for:【QQYUN-15021] Tăng cường JS online sửa dropdown không có hiệu lực
          // online jsThay đổi tăng cườngoptions
          dictOptions.value = props.options;
          // update-end--author:liaozhiyang---date:20260325---for:【QQYUN-15021] Tăng cường JS online sửa dropdown không có hiệu lực
        } else {
          dictOptions.value = props.options;
        }
      });

      function onChange(selectedValue) {
        if (props.triggerChange) {
          emit('change', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        } else {
          emit('input', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        }
        // Giải thích logic mã: 【QQYUN-9110] kiểm chứng vẫn không biến mất khi component có giá trị
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
      }

      function getParentContainer(node) {
        if (!props.popContainer) {
          return node?.parentNode;
        } else {
          // Giải thích logic mã: 【QQYUN-9339]Khi có nhiều popup modal đều chứa component chọn nhiều trong danh mục thả xuống và component tìm kiếm thả xuống, khi mở một modal khác thì options của component không hiển thị
          return setPopContainer(node, props.popContainer);
        }
      }

      async function loadDictOptions() {
        // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
        if (useLoadDict.value) {
          loadDictOptionsScroll();
        } else {
          const code = props.dictCode ?? '';
          try {
            const dictData = await initDictOptions(code);
            dictOptions.value = dictData;
          } catch (error) {
            console.error('initDictOptions error:', error);
            dictOptions.value = [];
          }
        }
        // update-end--author:liaozhiyang---date:20260204---for:【issues/9307] Dropdown online tải từ điển bảng cần tải cuộn
      }

      // Giải thích logic mã: VUEN-1145 Dropdown đa chọn, khi tìm kiếm không tra được dữ liệu
      function filterOption(input, option) {
        return option.children()[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        attrs,
        getOptions,
        dictOptions,
        onChange,
        arrayValue,
        getParentContainer,
        filterOption,
        isDictTable,
        useLoadDict,
        loading,
        handlePopupScroll,
        handleSearch,
        handleDropdownVisibleChange,
      };
    },
  });
</script>
<style scoped lang='less'>
.colorText{
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
