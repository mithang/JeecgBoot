import { computed, reactive, h } from 'vue';
import { JVxeTableMethods, JVxeTableProps } from '/@/components/jeecg/JVxeTable/src/types';
import { isEmpty } from '/@/utils/is';
import { Pagination } from 'ant-design-vue';

export function usePagination(props: JVxeTableProps, methods: JVxeTableMethods) {
  const innerPagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' Tổng cộng ' + total + ' mục';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 100,
  });

  const bindProps = computed(() => {
    return {
      ...innerPagination,
      ...props.pagination,
      size: props.size === 'tiny' ? 'small' : '',
    };
  });

  const boxClass = computed(() => {
    return {
      'j-vxe-pagination': true,
      'show-quick-jumper': !!bindProps.value.showQuickJumper,
    };
  });

  function handleChange(current, pageSize) {
    innerPagination.current = current;
    methods.trigger('pageChange', { current, pageSize });
  }

  function handleShowSizeChange(current, pageSize) {
    innerPagination.pageSize = pageSize;
    // -update-begin--author:liaozhiyang---date:20251209---for:【issues/9169]Khi chuyển trang, sự kiện pageChange đã tải hai lần
    // Vì handleShowSizeChange kích hoạt trước, ngay sau đó sẽ kích hoạt handleChange, nên có thể ghi chú lại.
    // methods.trigger('pageChange', { current, pageSize });
    // -update-end--author:liaozhiyang---date:20251209---for:【issues/9169]Khi chuyển trang, sự kiện pageChange đã tải hai lần
  }

  /** Kết xuất phân trang */
  function renderPagination() {
    if (props.pagination && !isEmpty(props.pagination)) {
      return h(
        'div',
        {
          class: boxClass.value,
        },
        [
          h(Pagination, {
            ...bindProps.value,
            // Giải thích logic mã: 【issues/8137] Bảng vxetable sau khi vô hiệu hóa thì phân trang bị ẩn
            disabled: false,
            onChange: handleChange,
            onShowSizeChange: handleShowSizeChange,
          }),
        ]
      );
    }
    return null;
  }

  return { renderPagination };
}
