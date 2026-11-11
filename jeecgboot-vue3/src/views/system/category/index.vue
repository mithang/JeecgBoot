<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable
      @register="registerTable"
      :rowSelection="rowSelection"
      :expandedRowKeys="expandedRowKeys"
      @expand="handleExpand"
      @fetch-success="onFetchSuccess"
    >
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--Hộp thoại từ điển-->
    <CategoryModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="system-category" setup>
  //tsCú pháp
  import { ref, computed, unref, toRaw, nextTick } from 'vue';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import CategoryModal from './components/CategoryModal.vue';
  import { useModal } from '/src/components/Modal';
  import { useMethods } from '/src/hooks/system/useMethods';
  import { columns, searchFormSchema } from './category.data';
  import { list, deleteCategory, batchDeleteCategory, getExportUrl, getImportUrl, getChildList, getChildListBatch } from './category.api';
  import { useListPage } from '/@/hooks/system/useListPage';

  const expandedRowKeys = ref([]);
  const { handleExportXls, handleImportXls } = useMethods();
  //Từ điểnmodel
  const [registerModal, { openModal }] = useModal();
  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'category-template',
    tableProps: {
      title: 'Từ điển phân loại',
      api: list,
      columns: columns,
      actionColumn: {
        width: 180,
      },
      formConfig: {
        schemas: searchFormSchema,
      },
      isTreeTable: true,
    },
    exportConfig: {
      name: 'Danh sách từ điển phân loại',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //Đăng ký dữ liệu table
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
    tableContext;

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Chi tiết
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
      hideFooter: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteCategory({ id: record.id }, importSuccess);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    const ids = selectedRowKeys.value.filter((item) => !item.includes('loading'));
    await batchDeleteCategory({ ids: ids }, importSuccess);
  }
  /**
   * Nhập (import)
   */
  function importSuccess() {
    // Giải thích logic mã: [issues/54]Từ điển cây, đánh dấu chọn, sau đó xóa hàng loạt, lỗi hệ thống------------
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * Thêm cấp dưới
   */
  function handleAddSub(record) {
    openModal(true, {
      record,
      isUpdate: false,
    });
  }
  /**
   * Callback thành công
   */
  async function handleSuccess({ isUpdate,isSubAdd, values, expandedArr }) {
    if (isUpdate) {
      //Callback khi chỉnh sửa
      updateTableDataRecord(values.id, values);
    } else {
      if (!values['pid']) {
        //Thêm mới nút gốc
        reload();
      } else {
        //Thêm mới tập con
        // Giải thích logic mã: [issue/4550]Từ điển phân loại có lượng dữ liệu quá nhiều sẽ khiến thời gian truy vấn dữ liệu quá dài---
        if(isSubAdd){
          await expandTreeNode(values.pid);
        }else{
          //expandedRowKeys.value = [];
          for (let key of unref(expandedArr)) {
            await expandTreeNode(key);
          }
        }
      }
    }
  }

  /**
   * Callback sau khi yêu cầu API thành công
   */
  function onFetchSuccess(result) {
    getDataByResult(result.items) && loadDataByExpandedRows();
  }
  /**
   * Truy vấn dữ liệu theo các hàng đã mở rộng (dùng để tải không đồng bộ dữ liệu cấp con khi làm mới sau khi lưu)
   */
  async function loadDataByExpandedRows() {
    if (unref(expandedRowKeys).length > 0) {
      const res = await getChildListBatch({ parentIds: unref(expandedRowKeys).join(',') });
      if (res.success && res.result.records.length > 0) {
        //Các nút con hàng loạt của dữ liệu đã mở rộng
        let records = res.result.records;
        const listMap = new Map();
        for (let item of records) {
          let pid = item['pid'];
          if (unref(expandedRowKeys).includes(pid)) {
            let mapList = listMap.get(pid);
            if (mapList == null) {
              mapList = [];
            }
            mapList.push(item);
            listMap.set(pid, mapList);
          }
        }
        let childrenMap = listMap;
        let fn = (list) => {
          if (list) {
            list.forEach((data) => {
              if (unref(expandedRowKeys).includes(data.id)) {
                data.children = getDataByResult(childrenMap.get(data.id));
                fn(data.children);
              }
            });
          }
        };
        fn(getDataSource());
      }
    }
  }
  /**
   * Xử lý tập dữ liệu
   */
  function getDataByResult(result) {
    if (result && result.length > 0) {
      return result.map((item) => {
        //Kiểm tra có đánh dấu là có nút con hay không
        if (item['hasChild'] == '1') {
          let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true };
          item.children = [loadChild];
        }
        return item;
      });
    }
  }
  /**
   *Mở rộng/gộp nút cây
   * */
  async function handleExpand(expanded, record) {
    // Kiểm tra có phải trạng thái mở rộng hay không, trạng thái mở rộng(expanded)Và tồn tại tập con(children)Và chưa từng tải(isLoading)thì đi truy vấn dữ liệu nút con
    if (expanded) {
      expandedRowKeys.value.push(record.id);
      if (record.children.length > 0 && !!record.children[0].isLoading) {
        let result = await getChildList({ pid: record.id });
        if (result && result.length > 0) {
          record.children = getDataByResult(result);
        } else {
          record.children = null;
          record.hasChild = '0';
        }
      }
    } else {
      let keyIndex = expandedRowKeys.value.indexOf(record.id);
      if (keyIndex >= 0) {
        expandedRowKeys.value.splice(keyIndex, 1);
      }
    }
  }
  /**
   *Sau khi thao tác bảng, xử lý việc mở rộng/gộp nút cây
   * */
  async function expandTreeNode(key) {
    let record:any = findTableDataRecord(key);
    // Giải thích logic mã: [issue/4550]Từ điển phân loại có lượng dữ liệu quá nhiều sẽ khiến thời gian truy vấn dữ liệu quá dài, hiển thị 'Yêu cầu API quá thời gian chờ,Vui lòng làm mới trang và thử lại!”---
    if(!expandedRowKeys.value.includes(key)){
      expandedRowKeys.value.push(key);
    }
    let result = await getChildList({ pid: key });
    if (result && result.length > 0) {
      record.children = getDataByResult(result);
    } else {
      record.children = null;
      record.hasChild = '0';
    }
    updateTableDataRecord(key, record);
  }
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Bạn có chắc muốn xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'Thêm cấp dưới',
        onClick: handleAddSub.bind(null, { pid: record.id }),
      },
    ];
  }
</script>

<style scoped></style>
