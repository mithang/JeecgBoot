import { computed, ref, watch, defineComponent, h } from 'vue';
import { cloneDeep, debounce } from 'lodash-es';
import { defHttp } from '/@/utils/http/axios';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { ajaxGetDictItems, getDictItemsByCode } from '/@/utils/dict';
import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
import { useMessage } from '/@/hooks/web/useMessage';
import { Select, SelectOption, Spin } from 'ant-design-vue'

/** value - label map, tránh truy vấn lặp lại (làm mới sẽ xóa cache) */
const LabelMap = new Map<string, any>();
// Yêu cầuid
let requestId = 0;

/** Component hiển thị, tự kèm dịch */
export const DictSearchSpanCell = defineComponent({
  name: 'JVxeSelectSearchSpanCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { innerOptions, innerSelectValue, innerValue } = useSelectDictSearch(props);
    return () => {
      return h('span', {}, [filterDictText(innerOptions.value, innerSelectValue.value || innerValue.value)]);
    };
  },
});

// Component chọn nhập
export const DictSearchInputCell = defineComponent({
  name: 'JVxeSelectSearchInputCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { createMessage } = useMessage();
    const { dict, loading, isAsync, options, innerOptions, originColumn, cellProps, innerSelectValue, handleChangeCommon } =
      useSelectDictSearch(props);
    const hasRequest = ref(false);
    // Thông tin nhắc nhở
    const tipsContent = computed(() => {
      return originColumn.value.tipsContent || 'Vui lòng nhập nội dung tìm kiếm';
    });
    // Hàm lọc
    const filterOption = computed(() => {
      if (isAsync.value) {
        //【jeecgboot-vue3/issues/I5QRT8】JVxeTypes.selectDictSearch syncVấn đề
        return ()=>true;
      }
      return (input, option) => option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    });

    /** Tải dữ liệu */
    const loadData = debounce((value) => {
      const currentRequestId = ++requestId;
      loading.value = true;
      innerOptions.value = [];
      if (value == null || value.trim() === '') {
        loading.value = false;
        hasRequest.value = false;
        return;
      }
      // Định dạng code từ điển:table,text,code
      hasRequest.value = true;
      loadDictByKeyword(dict.value, value)
        .then((res) => {
          if (currentRequestId !== requestId) {
            return;
          }
          let { success, result, message } = res;
          if (success) {
            innerOptions.value = result;
            result.forEach((item) => {
              LabelMap.set(item.value, [item]);
            });
          } else {
            createMessage.warning(message || 'Tìm kiếm thất bại');
          }
        })
        .finally(() => {
          loading.value = false;
        });
    }, 300);

    function handleChange(selectedValue) {
      innerSelectValue.value = selectedValue;
      handleChangeCommon(innerSelectValue.value);
    }

    function handleSearch(value) {
      if (isAsync.value) {
        // Khi nhập cũng nên bật trạng thái tải, vì loadData đã thêm debounce, nên sẽ có thời gian 800ms mà người dùng chủ quan cho là bị kẹt
        loading.value = true;
        if (innerOptions.value.length > 0) {
          innerOptions.value = [];
        }
        loadData(value);
      }
    }

    function renderOptionItem() {
      let optionItems: any[] = [];
      options.value.forEach(({ value, text, label, title, disabled }) => {
        optionItems.push(
          h(
            SelectOption,
            {
              key: value,
              value: value,
              disabled: disabled,
            },
            {
              default: () => text || label || title,
            }
          )
        );
      });
      return optionItems;
    }

    return () => {
      return h(
        Select,
        {
          ...cellProps.value,
          value: innerSelectValue.value,
          filterOption: filterOption.value,
          showSearch: true,
          allowClear: true,
          autofocus: true,
          defaultOpen: true,
          style: 'width: 100%',
          onSearch: handleSearch,
          onChange: handleChange,
        },
        {
          default: () => renderOptionItem(),
          notFoundContent: () => {
            if (loading.value) {
              return h(Spin, { size: 'small' });
            } else if (hasRequest.value) {
              return h('div', 'Không truy vấn được dữ liệu nào');
            } else {
              return h('div', [tipsContent.value]);
            }
          },
        }
      );
    };
  },
  // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
  enhanced: {
    aopEvents: {
      editActived({ $event }) {
        dispatchEvent({
          $event,
          props: this.props,
          className: '.ant-select .ant-select-selection-search-input',
          isClick: false,
          handler: (el) => el.focus(),
        });
      },
    },
  } as JVxeComponent.EnhancedPartial,
});

function useSelectDictSearch(props) {
  const setup = useJVxeComponent(props);
  const { innerValue, originColumn } = setup;

  // Trạng thái tải
  const loading = ref(false);
  // Giá trị chọn nội bộ
  const innerSelectValue = ref(null);
  // Nội bộ options
  const innerOptions = ref<any[]>([]);

  const dict = computed(() => originColumn.value.dict);
  // Có phải chế độ bất đồng bộ không
  const isAsync = computed(() => {
    let isAsync = originColumn.value.async;
    return isAsync != null && isAsync !== '' ? !!isAsync : true;
  });
  const options = computed(() => {
    if (isAsync.value) {
      return innerOptions.value;
    } else {
      return originColumn.value.options || [];
    }
  });

  /** Lắng nghe thuộc tính dùng chung */
  watch(
    innerValue,
    (value: string) => {
      if (value == null || value === '') {
        innerSelectValue.value = null;
      } else {
        loadDataByValue(value);
      }
    },
    { immediate: true }
  );
  watch(dict, () => loadDataByDict());

  // Truy vấn dữ liệu theo value, dùng để hiển thị lại
  async function loadDataByValue(value) {
    if (isAsync.value) {
      if (innerSelectValue.value !== value) {
        if (LabelMap.has(value)) {
          innerOptions.value = cloneDeep(LabelMap.get(value));
        } else {
          let result = await loadDictItem(dict.value, value);
          if (result && result.length > 0) {
            innerOptions.value = [{ value: value, text: result[0] }];
            LabelMap.set(value, cloneDeep(innerOptions.value));
          }
        }
      }
    }
    innerSelectValue.value = (value || '').toString();
  }

  // Khởi tạo danh mục
  async function loadDataByDict() {
    if (!isAsync.value) {
      // Nếu tập hợp mục từ điển có dữ liệu
      if (!originColumn.value.options || originColumn.value.options.length === 0) {
        // Theo từ điểnCode, Khởi tạo mảng từ điển
        let dictStr = '';
        if (dict.value) {
          let arr = dict.value.split(',');
          if (arr[0].indexOf('where') > 0) {
            let tbInfo = arr[0].split('where');
            dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
          } else {
            dictStr = dict.value;
          }
          if (dict.value.indexOf(',') === -1) {
            //Ưu tiên đọc cấu hình từ điển từ cache
            let cache = getDictItemsByCode(dict.value);
            if (cache) {
              innerOptions.value = cache;
              return;
            }
          }
          let { success, result } = await ajaxGetDictItems(dictStr, null);
          if (success) {
            innerOptions.value = result;
          }
        }
      }
    }
  }

  return {
    ...setup,
    loading,
    innerOptions,
    innerSelectValue,
    dict,
    isAsync,
    options,
  };
}

/** Lấy mục danh mục */
function loadDictItem(dict: string, key: string) {
  return defHttp.get({
    url: `/sys/dict/loadDictItem/${dict}`,
    params: {
      key: key,
    },
  });
}

/** Lấy mục từ điển theo từ khóa (tìm kiếm) */
function loadDictByKeyword(dict: string, keyword: string) {
  return defHttp.get(
    {
      url: `/sys/dict/loadDict/${dict}`,
      params: {
        keyword: keyword,
      },
    },
    {
      isTransformResponse: false,
    }
  );
}
