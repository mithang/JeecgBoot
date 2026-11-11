<template>
  <a-radio-group v-if="compType === CompTypeEnum.Radio" v-bind="attrs" v-model:value="state" @change="handleChangeRadio">
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio :value="item.value">
        <span :class="[useDicColor && item.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && item.color}` }">
          {{ item.label }}
        </span>
      </a-radio>
    </template>
  </a-radio-group>

  <a-radio-group
    v-else-if="compType === CompTypeEnum.RadioButton"
    v-bind="attrs"
    v-model:value="state"
    buttonStyle="solid"
    @change="handleChangeRadio"
  >
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio-button :value="item.value">
        {{ item.label }}
      </a-radio-button>
    </template>
  </a-radio-group>

  <template v-else-if="compType === CompTypeEnum.Select">
    <!-- Hiển thị hiệu ứng tải -->
    <a-input v-if="loadingEcho" readOnly placeholder="Đang tải...">
      <template #prefix>
        <LoadingOutlined />
      </template>
    </a-input>
    <a-select
      v-else
      :placeholder="placeholder"
      v-bind="attrs"
      v-model:value="state"
      :filterOption="handleFilterOption"
      :getPopupContainer="getPopupContainer"
      :style="style"
      @change="handleChange"
    >
      <a-select-option v-if="showChooseOption" :value="null">Vui lòng chọn...</a-select-option>
      <template v-for="item in dictOptions" :key="`${item.value}`">
        <a-select-option :value="item.value">
          <span
            :class="[useDicColor && item.color ? 'colorText' : '']"
            :style="{ backgroundColor: `${useDicColor && item.color}` }"
            :title="item.label"
          >
            {{ item.label }}
          </span>
        </a-select-option>
      </template>
    </a-select>
  </template>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { Form } from 'ant-design-vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict';
  import { get, omit } from 'lodash-es';
  import { CompTypeEnum } from '/@/enums/CompTypeEnum';
  import { LoadingOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'JDictSelectTag',
    inheritAttrs: false,
    components: { LoadingOutlined },
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
      dictCode: propTypes.string,
      type: propTypes.string,
      placeholder: propTypes.string,
      stringToNumber: propTypes.bool,
      useDicColor: propTypes.bool.def(false),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      // Có hiển thị tùy chọn [Vui lòng chọn] không
      showChooseOption: propTypes.bool.def(true),
      // Mục dropdown-onlineSử dụng
      options: {
        type: Array,
        default: () => [],
        required: false,
      },
      style: propTypes.any,
      // Khi tìm kiếm có chỉ tìm kiếmlabel
      onlySearchByLabel: propTypes.bool.def(false),
    },
    emits: ['options-change', 'change','update:value'],
    setup(props, { emit, refs }) {
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const formItemContext = Form.useInjectFormItemContext();
      const state = ref<any>(props.value);
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      // update-begin-author:liaozhiyang date:2026-05-12 for:【issues/9420] Khi hiển thị lại đa chọn chức danh của người dùng hệ thống sẽ hiển thị1,2,3
      watch(
        () => props.value,
        (val) => {
          const { mode } = unref<Recordable>(getBindValue);
          if ((mode === 'multiple' || mode === 'tags') && typeof val === 'string') {
            const arr = val ? val.split(',') : [];
            state.value = props.stringToNumber ? arr.map((v) => Number(v)) : arr;
          } else {
            const isMulti = mode === 'multiple' || mode === 'tags';
            state.value = isMulti && val == null ? [] : val;
          }
        },
        { immediate: true }
      );
      // update-end-author:liaozhiyang date:2026-05-12 for:【issues/9420] Khi hiển thị lại đa chọn chức danh của người dùng hệ thống sẽ hiển thị1,2,3
      // Có đang tải dữ liệu hiển thị lại không
      const loadingEcho = ref<boolean>(false);
      // Có phải lần đầu tải và hiển thị lại không, chỉ lần đầu tải mới hiển thị loading
      let isFirstLoadEcho = true;

      //Loại component
      const compType = computed(() => {
        return !props.type || props.type === 'list' ? 'select' : props.type;
      });
      /**
       * Lắng nghe danh mụccode
       */
      watchEffect(() => {
        if (props.dictCode) {
          loadingEcho.value = isFirstLoadEcho;
          isFirstLoadEcho = false;
          initDictData().finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
        }
        //update-begin-author:taoyan date: Nếu không cung cấp dictCode thì có thể dùng cấu hình của options--
        if (!props.dictCode) {
          dictOptions.value = props.options;
        }
        //update-end-author:taoyan date: Nếu không cung cấp dictCode thì có thể dùng cấu hình của options--
      });

      // Thêm sự kiện change cho giá trị rỗng tại đây,tức là khi mã gọi component đặt value thành''cũng có thể kích hoạt sự kiện change
      watch(
        () => props.value,
        () => {
          if (props.value === '') {
            emit('change', '');
            nextTick(() => formItemContext.onFieldChange());
          }
        }
      );

      async function initDictData() {
        let { dictCode, stringToNumber } = props;
        //Theo từ điểnCode, Khởi tạo mảng từ điển
        const dictData = await initDictOptions(dictCode);
        dictOptions.value = dictData.reduce((prev, next) => {
          if (next) {
            const value = next['value'];
            prev.push({
              label: next['text'] || next['label'],
              value: stringToNumber ? +value : value,
              color: next['color'],
              ...omit(next, ['text', 'value', 'color']),
            });
          }
          return prev;
        }, []);
      }

      function handleChange(e) {
        const { mode } = unref<Recordable>(getBindValue);
        let changeValue:any;
        // Tương thích chế độ chọn nhiều
        
        //Dùng một giá trị, nếu không khi giá trị state thay đổi sẽ kích hoạt nhiềuchange
        if (mode === 'multiple') {
          changeValue = e?.target?.value ?? e;
          // Lọc bỏ giá trị rỗng
          if (changeValue == null || changeValue === '') {
            changeValue = [];
          }
          if (Array.isArray(changeValue)) {
            changeValue = changeValue.filter((item) => item != null && item !== '');
          }
        } else {
          changeValue = e?.target?.value ?? e;
        }
        state.value = changeValue;

        // Giải thích logic mã: 【issues/4507]Khi dùng component JDictSelectTag, trình duyệt đưa ra cảnh báo:Expected Function, got Array------------
        emit('update:value', changeValue);
        // Không còn phụ thuộc vào setter của useRuleFormItem để tự động emit('change'), tại đây kích hoạt một cách tường minh, đảm bảo wrapper onChange của BasicForm có thể ghi lại vào formModel và kiểm chứng
        emit('change', changeValue);
        nextTick(() => formItemContext.onFieldChange());
      }

      /** Sự kiện thay đổi giá trị của radio chọn đơn */
      function handleChangeRadio(e) {
        const radioValue = e?.target?.value ?? e;
        state.value = radioValue;
        // Giải thích logic mã: 【issues/506] Component JDictSelectTag type="radio" Không có giá trị trả về------------
        emit('update:value', radioValue);
        emit('change', radioValue);
        nextTick(() => formItemContext.onFieldChange());
      }

      /** Dùng để tìm kiếm nội dung trong hộp thả xuống */
      function handleFilterOption(input, option) {
        // Giải thích logic mã: 【QQYUN-6514]Khi cấu hình, trục Y không thể nhập nhiều trường, console báo lỗi
        if (typeof option.children === 'function') {
          // Tìm kiếm trong label
          let labelIf = option.children()[0]?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          if (labelIf) {
            return true;
          }
        }
        if (props.onlySearchByLabel) {
          // Nếu đã bật chỉ tìm kiếm trong label thì không tiếp tục tìm kiếm value ở phía dưới nữa
          return false;
        }
        // Tìm kiếm trong value
        return (option.value || '').toString().toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        compType,
        attrs,
        loadingEcho,
        getBindValue,
        dictOptions,
        CompTypeEnum,
        handleChange,
        handleChangeRadio,
        handleFilterOption,
      };
    },
  });
</script>
<style scoped lang="less">
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
  // Giải thích logic mã: 【QQYUN-7799] Component từ điển (trừ component gốc) thêm cấu hình màu sắc
</style>
