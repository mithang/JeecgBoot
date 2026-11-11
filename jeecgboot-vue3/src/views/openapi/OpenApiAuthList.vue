<template>
  <div>
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot: Tiêu đề bảng-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'openapi:open_api_auth:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới</a-button>
        <a-button type="primary" v-auth="'openapi:open_api_auth:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất</a-button>
        <j-upload-button type="primary" v-auth="'openapi:open_api_auth:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'openapi:open_api_auth:deleteBatch'">Thao tác hàng loạt
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
        <!-- Tìm kiếm nâng cao -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--Slot hiển thị lại trường-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- Vùng biểu mẫu -->
    <OpenApiAuthDrawer @register="registerAuthDrawer" @success="handleSuccess" />
    <AuthDrawer @register="registerPermDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="openapi-openApiAuth" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { columns, searchFormSchema, superQuerySchema } from './OpenApiAuth.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, getGenAKSK, saveOrUpdate } from './OpenApiAuth.api';
  import OpenApiAuthDrawer from './components/OpenApiAuthDrawer.vue';
  import AuthDrawer from './components/AuthDrawer.vue';

  const queryParam = reactive<any>({});
  const { createMessage } = useMessage();
  const [registerAuthDrawer, { openDrawer: openAuthDrawer }] = useDrawer();
  const [registerPermDrawer, { openDrawer: openPermDrawer }] = useDrawer();

  //Đăng ký dữ liệu bảng
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Quản lý ủy quyền',
      api: list,
      columns,
      canResize: false,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      actionColumn: {
        width: 220,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: 'Quản lý ủy quyền',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  // Cấu hình tìm kiếm nâng cao
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * Sự kiện tìm kiếm nâng cao
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openAuthDrawer(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record: Recordable) {
    openAuthDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * Sự kiện ủy quyền
   */
  function handleAuth(record: Recordable) {
    openPermDrawer(true, { record });
  }

  /**
   * Đặt lại AK/SK
   */
  async function handleReset(record: Recordable) {
    const AKSKObj = await getGenAKSK({});
    record.ak = AKSKObj[0];
    record.sk = AKSKObj[1];
    await saveOrUpdate(record, true);
    reload();
  }

  /**
   * Chi tiết
   */
  function handleDetail(record: Recordable) {
    openAuthDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  /**
   * Callback thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Thanh thao tác
   */
  /**
   * Sao chép khóa bí mật
   */
  function handleCopyKeys(record: Recordable) {
    const text = `Khóa truy cập (AK): ${record.ak}\nKhóa ký (SK): ${record.sk}`;
    const isSuccess = copyTextToClipboard(text);
    if (isSuccess) {
      createMessage.success('Khóa bí mật đã sao chép vào clipboard');
    } else {
      createMessage.error('Sao chép thất bại, vui lòng sao chép thủ công');
    }
  }

  function getTableAction(record) {
    return [
      {
        label: 'Sao chép khóa bí mật',
        onClick: handleCopyKeys.bind(null, record),
      },
      {
        label: 'Phân bổ API',
        onClick: handleAuth.bind(null, record),
        auth: 'openapi:open_api_auth:edit',
      },
    ];
  }

  /**
   * Thanh thao tác dropdown
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Sửa đối tượng',
        onClick: handleEdit.bind(null, record),
        auth: 'openapi:open_api_auth:edit',
      },
      {
        label: 'Đặt lại khóa',
        popConfirm: {
          title: 'Khóa bí mật gốc sẽ hết hiệu lực, xác nhận đặt lại?',
          confirm: handleReset.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:edit',
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Bạn có xác nhận xóa không?',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number) {
    width: 100%;
  }
</style>
