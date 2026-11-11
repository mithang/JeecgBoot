<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">
          Thêm mới
        </a-button>
<!--        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">-->
<!--          Xuất (export)-->
<!--        </a-button>-->
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">-->
<!--          Nhập (import)-->
<!--        </j-upload-button>-->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            Thao tác hàng loạt
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction
          :actions="getTableAction(record)"
          :dropDownActions="getDropDownAction(record)"
        />
      </template>
    </BasicTable>

    <!-- Khu vực biểu mẫu -->
    <SysTableWhiteListModal @register="registerModal" @success="handleSuccess"/>
  </div>
</template>

<script lang="ts" name="ahh-sysTableWhiteList" setup>
import {BasicTable, TableAction} from '/@/components/Table';
import {useModal} from '/@/components/Modal';
import {useListPage} from '/@/hooks/system/useListPage'
import SysTableWhiteListModal from './modules/SysTableWhiteListModal.vue'
import {columns, searchFormSchema} from './SysTableWhiteList.data';
import {batchDelete, deleteOne, getExportUrl, getImportUrl, list} from './SysTableWhiteList.api';

//Đăng kýmodel
const [registerModal, {openModal}] = useModal();
//Đăng ký dữ liệu table
const {prefixCls, tableContext, onExportXls, onImportXls} = useListPage({
  tableProps: {
    title: 'Danh sách trắng bảng hệ thống',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    actionColumn: {
      width: 120,
    },
  },
  exportConfig: {
    name: "Danh sách trắng bảng hệ thống",
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl
  },
})

const [registerTable, {reload}, {rowSelection, selectedRowKeys}] = tableContext

/**
 * Sự kiện thêm mới
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

/**
 * Sự kiện chỉnh sửa
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

/**
 * Chi tiết
 */
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

/**
 * Sự kiện xóa
 */
async function handleDelete(record) {
  await deleteOne({id: record.id}, reload);
}

/**
 * Sự kiện xóa hàng loạt
 */
async function batchHandleDelete() {
  await batchDelete({ids: selectedRowKeys.value}, reload);
}

/**
 * Callback thành công
 */
function handleSuccess({isUpdate, values}) {
  reload();
}

/**
 * Thanh thao tác
 */
function getTableAction(record) {
  return [
    {
      label: 'Chỉnh sửa',
      onClick: handleEdit.bind(null, record),
    }
  ]
}

/**
 * Thanh thao tác thả xuống
 */
function getDropDownAction(record) {
  return [
    {
      label: 'Chi tiết',
      onClick: handleDetail.bind(null, record),
    }, {
      label: 'Xóa',
      popConfirm: {
        title: 'Xác nhận xóa?',
        confirm: handleDelete.bind(null, record),
      }
    }
  ]
}
</script>
<style scoped>

</style>
