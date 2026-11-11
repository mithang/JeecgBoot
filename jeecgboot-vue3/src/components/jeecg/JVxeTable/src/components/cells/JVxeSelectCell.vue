<template>
  <a-select :value="innerValue" v-bind="selectProps">
    <template v-if="loading" #notFoundContent>
      <LoadingOutlined />
      <span>&nbsp;Đang tải...</span>
    </template>
    <!-- Chế độ nhóm: khi options chứa trường group thì render theo nhóm -->
    <template v-if="hasGroup">
      <template v-for="(groupOptions, groupName) of groupedSelectOptions" :key="groupName">
        <a-select-opt-group :label="groupName">
          <a-select-option
            v-for="option of groupOptions"
            :key="option.value"
            :value="option.value"
            :title="option.text || option.label || option.title"
            :disabled="option.disabled"
          >
            <span>{{ option.text || option.label || option.title || option.value }}</span>
          </a-select-option>
        </a-select-opt-group>
      </template>
    </template>
    <!-- Chế độ không nhóm: render phẳng như ban đầu -->
    <template v-else>
      <template v-for="option of selectOptions" :key="option.value">
        <a-select-option :value="option.value" :title="option.text || option.label || option.title" :disabled="option.disabled">
          <span>{{ option.text || option.label || option.title || option.value }}</span>
        </a-select-option>
      </template>
    </template>
  </a-select>
</template>

<script lang="ts">
  import { ref, computed, defineComponent } from 'vue';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
  import { JVxeComponent, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { isPromise } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeSelectCell',
    components: { LoadingOutlined },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, row, originColumn, scrolling, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);
      const loading = ref(false);
      // options tải bất đồng bộ (dùng cho liên động nhiều cấp)
      const asyncOptions = ref<any[] | null>(null);
      // Dropdown props
      const selectProps = computed(() => {
        let selProps = {
          ...cellProps.value,
          allowClear: true,
          autofocus: true,
          defaultOpen: !scrolling.value,
          style: { width: '100%' },
          filterOption: handleSelectFilterOption,
          onBlur: handleBlur,
          onChange: handleChange,
        };
        // Kiểm tra select có cho phép nhập hay không
        let { allowSearch, allowInput } = originColumn.value;
        if (allowInput === true || allowSearch === true) {
          selProps['showSearch'] = true;
          selProps['onSearch'] = handleSearchSelect;
        }
        return selProps;
      });
      // Tùy chọn danh sách thả xuống
      const selectOptions = computed(() => {
        if (asyncOptions.value) {
          return asyncOptions.value;
        }
        let { linkage } = props.renderOptions;
        if (linkage) {
          let { getLinkageOptionsSibling, config } = linkage;
          let res = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
          // Khi trả về Promise, nghĩa là liên động nhiều cấp
          if (res instanceof Promise) {
            loading.value = true;
            res
              .then((opt) => {
                asyncOptions.value = opt;
                loading.value = false;
              })
              .catch((e) => {
                console.error(e);
                loading.value = false;
              });
          } else {
            asyncOptions.value = null;
            return res;
          }
        }
        return originColumn.value.options;
      });

      // Có chứa trường nhóm không
      const hasGroup = computed(() => {
        const opts = selectOptions.value;
        return opts && opts.length > 0 && opts.some((o) => o.group);
      });

      // Các tùy chọn sau khi nhóm theo trường group
      const groupedSelectOptions = computed(() => {
        const opts = selectOptions.value || [];
        const groups: Record<string, any[]> = {};
        for (const opt of opts) {
          const g = opt.group || '';
          if (!groups[g]) groups[g] = [];
          groups[g].push(opt);
        }
        return groups;
      });

      // --------- created ---------

      // Chọn nhiều, tìm kiếmtype
      let multipleTypes = [JVxeTypes.selectMultiple, 'list_multi'];
      let searchTypes = [JVxeTypes.selectSearch, 'sel_search'];
      if (multipleTypes.includes(props.type)) {
        // Xử lý chọn nhiều
        let props = originColumn.value.props || {};
        props['mode'] = 'multiple';
        props['maxTagCount'] = 1;
        // Giải thích logic mã: issues/271 OnlineDropdown đa chọn của biểu mẫu chính-con trong biểu mẫu Online không tìm kiếm được
        originColumn.value.allowSearch = true;
        originColumn.value.props = props;
      } else if (searchTypes.includes(props.type)) {
        // Xử lý tìm kiếm
        originColumn.value.allowSearch = true;
      }

      /** Xử lý sự kiện change */
      function handleChange(value) {
        // Xử lý liên động cấp dưới
        let linkage = props.renderOptions.linkage;
        if (linkage) {
          linkage.handleLinkageSelectChange(row.value, originColumn.value, linkage.config, value);
        }
        handleChangeCommon(value);
      }

      /** Xử lý sự kiện blur mất focus */
      function handleBlur(value) {
        let { allowInput, options } = originColumn.value;
        if (allowInput === true) {
          // Xóa các mục vô dụng được tạo do tìm kiếm (người dùng nhập)
          if (typeof value === 'string') {
            let indexes: number[] = [];
            options.forEach((option, index) => {
              if (option.value.toLocaleString() === value.toLocaleString()) {
                delete option.searchAdd;
              } else if (option.searchAdd === true) {
                indexes.push(index);
              }
            });
            // Đảo thứ tự rồi xóa mục trong mảng
            for (let index of indexes.reverse()) {
              options.splice(index, 1);
            }
          }
        }
        handleBlurCommon(value);
      }

      /** Dùng để tìm kiếm nội dung trong hộp thả xuống */
      function handleSelectFilterOption(input, option) {
      
        let { allowSearch, allowInput } = originColumn.value;
        if (allowSearch === true || allowInput === true) {
          // Giải thích logic mã: 【QQYUN-5806] JS tăng cường thay đổi tìm kiếm trong danh sách thả xuốngoptions (Ngăn chặnoption.titleBáo lỗi khi là null)
          if (option.title == null) return false;
          // Giải thích logic mã: 【issues/5305】JVxeTypes.select Không thể tìm kiếm như mong đợi
          return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
        }
        return true;
      }

      /** select Sự kiện khi tìm kiếm, dùng để thêm độngoptions */
      function handleSearchSelect(value) {
        let { allowSearch, allowInput, options } = originColumn.value;

        if (allowSearch !== true && allowInput === true) {
          // Đã tìm thấy mục tương ứng chưa, nếu không tìm thấy thì thêm mục này
          let flag = false;
          for (let option of options) {
            if (option.value.toLocaleString() === value.toLocaleString()) {
              flag = true;
              break;
            }
          }
          // !!value : không thêm giá trị rỗng
          if (!flag && !!value) {
            // searchAdd Có phải được thêm qua tìm kiếm không
            options.push({ title: value, value: value, searchAdd: true });
          }
        }
      }

      return {
        loading,
        innerValue,
        selectProps,
        selectOptions,
        hasGroup,
        groupedSelectOptions,
        handleChange,
        handleBlur,
      };
    },
    // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event, row, column }) {
          dispatchEvent({
            $event,
            row,
            column,
            props: this.props,
            instance: this,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: false,
            handler: (el) => el.focus(),
          });
        },
      },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          let { props, context } = ctx!;
          let { row, originColumn } = context;
          let options;
          let linkage = props?.renderOptions.linkage;
          // Kiểm tra xem có phải là liên động nhiều cấp hay không, nếu đúng thì dịch bất đồng bộ qua API
          if (linkage) {
            let { getLinkageOptionsSibling, config } = linkage;
            let linkageOptions = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
            options = isPromise(linkageOptions) ? await linkageOptions : linkageOptions;
          } else if (isPromise(originColumn.value.optionsPromise)) {
            options = await originColumn.value.optionsPromise;
          } else {
            options = originColumn.value.options;
          }
          return filterDictText(options, value);
        },
      },
      getValue(value) {
        if (Array.isArray(value)) {
          return value.join(',');
        } else {
          return value;
        }
      },
      setValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // Xác định có phải chọn nhiều không
        if ((originColumn.value.props || {})['mode'] === 'multiple') {
          originColumn.value.props['maxTagCount'] = 1;
        }
        if (value != null && value !== '') {
          if (typeof value === 'string') {
            return value === '' ? [] : value.split(',');
          }
          return value;
        } else {
          return undefined;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
