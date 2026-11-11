<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới menu</a-button>
        <a-button type="primary" preIcon="ic:round-expand" @click="expandAll">Mở rộng tất cả</a-button>
        <a-button type="primary" preIcon="ic:round-compress" @click="collapseAll">Thu gọn tất cả</a-button>

        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <MenuDrawer @register="registerDrawer" @success="handleSuccess" :showFooter="showFooter" />
    <DataRuleList @register="registerDrawer1" />
  </div>
</template>
<script lang="ts" name="system-menu" setup>
  import { nextTick, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import MenuDrawer from './MenuDrawer.vue';
  import DataRuleList from './DataRuleList.vue';
  import { columns,searchFormSchema } from './menu.data';
  import { list, deleteMenu, batchDeleteMenu } from './menu.api';
  import { useDefIndexStore } from "@/store/modules/defIndex";
  import { useI18n } from "/@/hooks/web/useI18n";

  const checkedKeys = ref<Array<string | number>>([]);
  const showFooter = ref(true);
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerDrawer1, { openDrawer: openDataRule }] = useDrawer();
  const { t } = useI18n();
  let collapseBeforeFetch = () => {};

  // Kết xuất cột tên menu tùy chỉnh
  columns[0].customRender = function ({ text, record }) {
    // date-begin--author:liaozhiyang---date:20250716---for：【issues/8317】Tên menu của trang chủ mặc định gây lỗi khi thích ứng quốc tế hóa
    let displayText = text;
    // Xử lý quốc tế hóa trước, tránh ảnh hưởng đến kiểm tra quốc tế hóa sau khi thêm dấu trang chủ mặc định
    if (displayText && displayText.includes("t('") && t) {
      try {
        displayText = new Function('t', `return ${displayText}`)(t);
      } catch (error) {
        console.warn('Xử lý quốc tế hóa thất bại:', error);
        // Nếu xử lý quốc tế hóa thất bại, dùng văn bản gốc
        displayText = text;
      }
    }
    // Sau khi hoàn thành xử lý quốc tế hóa, mới thêm dấu trang chủ mặc định
    const isDefIndex = checkDefIndex(record);
    if (isDefIndex) {
      displayText += `（${t('routes.basic.defaultHomePage')}）`;
    }
    return displayText;
    // date-end--author:liaozhiyang---date:20250716---for：【issues/8317】Tên menu của trang chủ mặc định gây lỗi khi thích ứng quốc tế hóa
  };

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      title: 'Danh sách menu',
      api: list,
      columns: columns,
      size: 'small',
      pagination: false,
      isTreeTable: true,
      striped: true,
      useSearchForm: true,
      showTableSetting: true,
      bordered: true,
      showIndexColumn: false,
      tableSetting: { fullScreen: true },
      beforeFetch: (params) => {
        collapseBeforeFetch();
        return params;
      },
      formConfig: {
        // Giải thích logic mã: 【QQYUN-5873】Nhãn (label) của khu vực truy vấn mặc định căn trái
        labelWidth: 74,
        rowProps: { gutter: 24 },
        schemas: searchFormSchema,
        autoAdvancedCol: 4,
        baseColProps: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
        actionColOptions: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
      },
      actionColumn: {
        width: 120,
      },
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload, expandAll, collapseAll, getDataSource, setProps }] = tableContext;
  collapseBeforeFetch = collapseAll;

  /**
   * Chỉ mở rộng nút gốc, tránh kết xuất toàn bộ nút menu và nút quyền cùng một lúc khiến trang bị giật lag
   */
  function expandFirstLevel() {
    const rootKeys = getDataSource().map((item) => item.id).filter(Boolean);
    setProps({ expandedRowKeys: rootKeys });
  }

  /**
   * Cấu hình cột chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 30,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * Thêm mới
   */
  function handleCreate() {
    showFooter.value = true;
    openDrawer(true, {
      isUpdate: false,
    });
  }

  /**
   * Chỉnh sửa
   */
  function handleEdit(record) {
    showFooter.value = true;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Chi tiết
   */
  function handleDetail(record) {
    showFooter.value = false;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Thêm cấp dưới
   */
  function handleAddSub(record) {
    openDrawer(true, {
      record: { parentId: record.id, menuType: 1 },
      isUpdate: false,
    });
  }
  /**
   * Hộp thoại quyền dữ liệu
   */
  function handleDataRule(record) {
    openDataRule(true, { id: record.id });
  }

  /**
   * Xóa
   */
  async function handleDelete(record) {
    await deleteMenu({ id: record.id }, reload);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteMenu({ ids: checkedKeys.value }, () => {
      // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1662】Quản lý menu, tác vụ hẹn giờ: khi xóa hàng loạt thì xóa danh sách đã chọn
      reload();
      checkedKeys.value = [];
      // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1662】Quản lý menu, tác vụ hẹn giờ: khi xóa hàng loạt thì xóa danh sách đã chọn
    });
  }
  /**
   * Callback thành công
   */
  function handleSuccess() {
    reload();
    reloadDefIndex();
  }

  function onFetchSuccess() {
    // Trình diễn mặc định mở rộng mọi mục bảng
    nextTick(expandAll);
  }

  // --------------- begin Cấu hình trang chủ mặc định ------------

  const defIndexStore = useDefIndexStore()

  // Đặt trang chủ mặc định
  async function handleSetDefIndex(record: Recordable) {
    defIndexStore.update(record.url, record.component, record.route)
  }

  /**
   * Kiểm tra có phải trang chủ mặc định hay không
   * @param record
   */
  function checkDefIndex(record: Recordable) {
    return defIndexStore.check(record.url)
  }

  // Tải lại cấu hình trang chủ mặc định
  function reloadDefIndex() {
    try {
      defIndexStore.query();
    } catch (e) {
      console.error(e)
    }
  }

  reloadDefIndex()

  // --------------- end Cấu hình trang chủ mặc định ------------

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      // {
      //   label: 'Chi tiết',
      //   onClick: handleDetail.bind(null, record),
      // },
      {
        label: 'Thêm cấp dưới',
        onClick: handleAddSub.bind(null, record),
      },
      {
        label: 'Quy tắc dữ liệu',
        onClick: handleDataRule.bind(null, record),
      },
      {
        label: 'Đặt làm trang chủ mặc định',
        onClick: handleSetDefIndex.bind(null, record),
        ifShow: () => !record.internalOrExternal && record.component && !checkDefIndex(record),
      },
      {
        label: 'Xóa',
        color: 'error',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
