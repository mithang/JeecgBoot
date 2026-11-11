import { inject, reactive, ref, watch, unref, Ref, computed } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { isEmpty } from '@/utils/is';

export function useSelectBiz(getList, props, emit?) {
  //Nhận tùy chọn hộp thả xuống
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //Nhận giá trị đã chọn
  const selectValues = <object>inject('selectValues', reactive({ value: [], change: false }));
  // Có đang tải hiển thị lại không
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //Tập dữ liệu
  const dataSource = ref<Array<object>>([]);
  //Giá trị đã chọn
  const checkedKeys = ref<Array<string | number>>([]);
  //Bản ghi hàng đã chọn
  const selectRows = ref<Array<object>>([]);
  //Popup nhắc nhở
  const $message = useMessage();
  // Có phải lần đầu tải và hiển thị lại không, chỉ lần đầu tải mới hiển thị loading
  let isFirstLoadEcho = true;

  /**
   * Lắng nghe sự thay đổi của selectValues
   */
  watch(
    selectValues,
    () => {
      //if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
      if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
        // Giải thích logic mã: 【issues/7405]Chọn người dùng theo phòng ban đồng thời chọn tất cả người dùng của hai trang, hiển thị lại lên trang cha. Người dùng ở trang thứ hai hiển thị không phải tên thật
        let params = { isMultiTranslate: 'true', pageSize: selectValues.value?.length };
        params[props.rowKey] = selectValues['value'].join(',');
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        getDataSource(params, true)
          .then()
          .finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
      }
      //Đặt mục chọn mặc định của danh sách
      // Giải thích logic mã: 【QQYUN-12155] Tick trong popup, rồi bấm Hủy, giá trị vẫn bị chọn
      checkedKeys['value'] = [...selectValues['value']];
    },
    { immediate: true }
  );

  async function onSelectChange(selectedRowKeys: (string | number)[], selectRow) {
    checkedKeys.value = selectedRowKeys;
    //Vấn đề chọn tất cả: checkedKeys và selectRows phải nhất quán
    if (props.showSelected && unref(checkedKeys).length !== unref(selectRow).length) {
      let { records } = await getList({
        code: unref(checkedKeys).join(','),
        pageSize: unref(checkedKeys).length,
      });
      selectRows.value = records;
    } else {
      selectRows.value = selectRow;
    }
  }

  /**
   * Cấu hình cột chọn (phản ứng: khi prop isRadioSelection thay đổi, type tự động chuyển radio/checkbox）
   */
  const rowSelection = computed(() => ({
    type: props.isRadioSelection ? 'radio' : 'checkbox',
    columnWidth: 20,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
    //table4.4.0Thuộc tính mới: sau khi chọn có xóa dữ liệu trang trước/trang sau hay không, mặc địnhfalse
    preserveSelectedRowKeys:true,
  }));

  /**
   * Cấu hình cột số thứ tự
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: 50,
  };

  /**
   * Tải tập dữ liệu danh sách
   * @param params
   * @param flag Có phải tải ở chế độ hiển thị lại mặc định không
   */
  async function getDataSource(params, flag) {
    let { records } = await getList(params);
    dataSource.value = records;
    if (flag) {
      let options = <any[]>[];
      records.forEach((item) => {
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
      });
      selectOptions.value = options;
    }
  }
  async function initSelectRows() {
    let { records } = await getList({
      code: selectValues['value'].join(','),
      pageSize: selectValues['value'].length,
    });
    // Giải thích logic mã: 【QQYUN-12155] Tick trong popup, rồi bấm Hủy, giá trị vẫn bị chọn
    checkedKeys['value'] = [...selectValues['value']];
    selectRows['value'] = records;
  }

  /**
   * Sự kiện kích hoạt khi popup hiển thị/ẩn
   */
  async function visibleChange(visible) {
    if (visible) {
      // Giải thích logic mã: 【QQYUN-12179]Popup đã chọn giá trị, nhấn hủy rồi mở lại popup thì vẫn còn giá trị đã chọn lần trước
      checkedKeys['value'] = [...selectValues['value']];
      //Đặt mục chọn mặc định của danh sách
      props.showSelected && initSelectRows();
    } else {
      // Giải thích logic mã: 【QQYUN-9366]Component chọn người dùng khi hủy và đóng sẽ mang dữ liệu đã chọn vào
      emit?.('close');
    }
  }

  /**
   * Xác nhận lựa chọn
   */
  function getSelectResult(success) {
    let options = <any[]>[];
    let values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    if (props.maxSelectCount && values.length > props.maxSelectCount) {
      $message.createMessage.warning(`Tối đa chỉ được chọn${props.maxSelectCount}bản ghi dữ liệu`);
      return false;
    }
    success && success(options, values);
  }
  //Xóa thông tin đã chọn
  function handleDeleteSelected(record) {
    // Giải thích logic mã: 【issues/424]Sau khi bật danh sách bên phải, khi xóa người dùng trong danh sách bên phải thì logic có vấn đề------------
    checkedKeys.value = checkedKeys.value.filter((item) => item != record[props.rowKey]);
    selectRows.value = selectRows.value.filter((item) => item[props.rowKey] !== record[props.rowKey]);
  }
  //Xóa mục đã chọn
  function reset() {
    checkedKeys.value = [];
    selectRows.value = [];
  }
  return [
    {
      onSelectChange,
      getDataSource,
      visibleChange,
      selectOptions,
      selectValues,
      rowSelection,
      indexColumnProps,
      checkedKeys,
      selectRows,
      dataSource,
      getSelectResult,
      handleDeleteSelected,
      reset,
    },
  ];
}
