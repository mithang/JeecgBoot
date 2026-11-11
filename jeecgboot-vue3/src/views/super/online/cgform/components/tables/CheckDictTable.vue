<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    keyboardEdit
    :class="[prefixCls]"
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    v-bind="tableProps"
  >
    <template #fieldValidType="props">
      <a-row type="flex" :class="['row-valid-type', { full: !isCustomRegexp(props.value) }]">
        <a-col :class="['left']" @click="isCustomRegexp(props.value) ? onChangeCustomRegexp(props) : undefined">
          <a-select :value="props.value" :options="validTypeOptions" placeholder="Trống" style="width: 100%" @change="props.triggerChange" :virtual="false" :open="isCustomRegexp(props.value) ? false : undefined">
            <template #dropdownRender="{ menuNode }">
              <div class="menu">
                <VNodes :vnodes="menuNode" />
              </div>
              <div
                v-show="!isCustomRegexp(props.value)"
                class="custom-option-list rc-virtual-list-holder-inner"
                style="border-top: 1px solid #dfdfdf"
              >
                <div class="ant-select-item ant-select-item-option" title="Sử dụng biểu thức chính quy tùy chỉnh làm quy tắc kiểm chứng" @click="onAddCustomRegexp(props)" @mousedown="e => e.preventDefault()">
                  Biểu thức chính quy
                </div>
              </div>
            </template>
          </a-select>
        </a-col>
        <a-col class="right" title="Sửa biểu thức chính quy tùy chỉnh">
          <a-button preIcon="ant-design:edit" @click="() => onChangeCustomRegexp(props)" />
        </a-col>
      </a-row>
    </template>
  </JVxeTable>
</template>

<script lang="ts">
  import { ref, defineComponent, computed } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useJPrompt } from '/@/components/jeecg/JPrompt';
  import { useDesign } from '/@/hooks/web/useDesign';

  export default defineComponent({
    name: 'CheckDictTable',
    components: {
      VNodes: (_, { attrs }) => attrs.vnodes,
    },
    setup() {
      const { prefixCls } = useDesign('cgform-check-dict-table');
      const { createMessage: $message } = useMessage();
      // Định nghĩa thông tin cột
      const columns = ref<JVxeColumn[]>([
        { title: 'Tên trường', key: 'dbFieldName', width: 100 },
        { title: 'Ghi chú trường', key: 'dbFieldTxt', width: 100 },
        {
          title: 'trườngHref',
          key: 'fieldHref',
          width: 130,
          type: JVxeTypes.textarea,
          defaultValue: '',
        },
        {
          title: 'Quy tắc xác thực',
          key: 'fieldValidType',
          width: 170,
          type: JVxeTypes.slot,
          slotName: 'fieldValidType',
          allowInput: true,
          defaultValue: '',
          placeholder: 'Trống',
        },
        {
          title: 'Kiểm chứng mục bắt buộc điền',
          key: 'fieldMustInput',
          width: 80,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
        },
        {
          title: 'Định nghĩa bộ chuyển đổi',
          key: 'converter',
          width: 350,
          type: JVxeTypes.input,
          defaultValue: '',
        },
      ]);
      
      const setup = useTableSync(columns);
      const { tableRef } = setup;
      
      const validTypeOptions = ref([
        { label: 'Trống', value: '' },
        { label: 'Kiểm chứng tính duy nhất', value: 'only' },
        { label: '6tới16chữ số', value: 'n6-16' },
        { label: '6tới18chữ cái', value: 's6-18' },
        { label: '6tới16ký tự bất kỳ', value: '*6-16' },
        { label: 'Địa chỉ web', value: 'url' },
        { label: 'Thư điện tử (email)', value: 'e' },
        { label: 'Số điện thoại di động', value: 'm' },
        { label: 'Mã bưu chính', value: 'p' },
        { label: 'Chữ cái', value: 's' },
        { label: 'Chữ số', value: 'n' },
        { label: 'Số nguyên', value: 'z' },
        { label: 'Không rỗng', value: '*' },
        { label: 'Số tiền', value: 'money' },
      ]);
      const validTypeValues = computed(() => {
        return validTypeOptions.value.map((opt) => opt.value);
      });

      const { createJPrompt } = useJPrompt();

      /**
       * Phán đoán có phải biểu thức chính quy tùy chỉnh hay không
       * @param value
       */
      function isCustomRegexp(value) {
        return value != null && !validTypeValues.value.includes(value);
      }

      // Thêm biểu thức chính quy tùy chỉnh
      function onAddCustomRegexp(props) {
        createJPrompt({
          title: 'Biểu thức chính quy tùy chỉnh',
          placeholder: 'Vui lòng nhập biểu thức chính quy',
          rules: [{ required: true, message: 'Biểu thức chính quy không được để trống!' }, { validator: validatorCustomRegexp }],
          onOk: (value) => {
            props.triggerChange(value);
            $message.success('Thêm thành công');
          },
        });
      }

      // Thay đổi biểu thức chính quy tùy chỉnh
      function onChangeCustomRegexp(props) {
        createJPrompt({
          title: 'Sửa biểu thức chính quy tùy chỉnh',
          defaultValue: props.value,
          placeholder: 'Vui lòng nhập biểu thức chính quy',
          rules: [{ required: true, message: 'Biểu thức chính quy không được để trống!' }, { validator: validatorCustomRegexp }],
          onOk: (value) => {
            props.triggerChange(value);
            if (value !== props.value) {
              $message.success('Sửa thành công');
            }
          },
        });
      }

      // Kiểm chứng biểu thức chính quy tùy chỉnh
      function validatorCustomRegexp(_, value) {
        if (isCustomRegexp(value)) {
          return Promise.resolve();
        } else {
          return Promise.reject('Kiểm chứng hiện tại đã tồn tại');
        }
      }
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng
      function syncFieldMustInput(row) {
        if (row.dbIsNull === '0') {
          tableRef.value!.setValues([
            {
              rowKey: row.id,
              values: { fieldMustInput: '1' },
            },
          ]);
        }
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng
      return {
        ...setup,
        prefixCls,
        columns,
        isCustomRegexp,
        validTypeOptions,
        validTypeValues,

        onAddCustomRegexp,
        onChangeCustomRegexp,
        syncFieldMustInput,
      };
    },
  });
</script>
<style lang="less" scoped>
  //noinspection LessUnresolvedVariable
  // --------------------- begin Kiểu dáng slot của biểu thức chính quy tùy chỉnh ------------------
  .row-valid-type {
    .left,
    .right {
      &:hover {
        z-index: 3;
      }
    }

    &.full {
      .right {
        display: none;
      }
    }

    .left {
      flex: 1;
      z-index: 2;
      width: calc(100% - 33px);

      :deep(.ant-select){
        .ant-select-selector {
          border-width: 0 !important;
          border-color: transparent !important;
          box-shadow: none !important;
        }
        .ant-select-arrow {
          display: none;
        }
      }

      :deep(.ant-select:not(.ant-select-open)){
        .ant-select-selector {
          background-color: transparent !important;
        }

        .ant-select-arrow {
          display: none;
        }
      }
    }

    .right {
      width: 33px;
      z-index: 1;

      :deep(.ant-btn){
        padding: 4px 8px;
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
        border: none;
        background-color: transparent;
        box-shadow: none !important;
      }
    }
  }
  // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-497】Chọn mục cuối cùng của loại kiểm chứng biểu mẫu, mở lần thứ hai thỉnh thoảng hiển thị không đầy đủ
  .menu {
    :deep(.rc-virtual-list-holder) {
      overflow-y: hidden !important;
      &:hover {
        overflow-y: auto !important;
      }
      &::-webkit-scrollbar-track {
        background-color: transparent;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240807---for：【TV360X-497】Chọn mục cuối cùng của loại kiểm chứng biểu mẫu, mở lần thứ hai thỉnh thoảng hiển thị không đầy đủ
  // --------------------- end Kiểu dáng slot của biểu thức chính quy tùy chỉnh ------------------
</style>
