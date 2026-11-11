<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection" v-bind="$attrs">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">Nhập liệu (ghi vào)</a-button>
        <a-button preIcon="bxs:bot" type="primary" style="margin-right: 5px" @click="onCreateByAi">AITạo báo cáo</a-button>
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
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- Vùng biểu mẫu -->
    <CgreportModal @register="registerModal" @success="handleSuccess"></CgreportModal>
    <!-- Thông quaAiTạo báo cáo -->
    <CgreportAigcModal @register="registerCgreportAigcModal" @generate="onAigcGenerate" />
  </div>
</template>

<script lang="ts" name="online-cgreport" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './cgreport.data';
  import { list, deleteOne, batchDelete, getReportParam, doCopy } from './cgreport.api';
  import { useModal } from '/@/components/Modal';
  import CgreportModal from './components/CgreportModal.vue';
  import CgreportAigcModal from "./components/CgreportAigcModal.vue";
  import Clipboard from 'clipboard';
  import { useRouter } from 'vue-router';
  import { buildUUID } from '/@/utils/uuid';
  let router = useRouter();
  const [registerModal, { openModal }] = useModal();

  // Tham số, phương thức dùng chung của trang danh sách
  const {
    prefixCls,
    tableContext,
    createMessage: $message,
    createConfirm: $confirm,
  } = useListPage({
    designScope: 'online-cgreport-list',
    pagination: true,
    tableProps: {
      title: 'OnlineBáo cáo',
      api: list,
      rowKey: 'id',
      columns: columns,
      formConfig: {
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        schemas: searchFormSchema,
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  // Đăng kýAigcPopup
  const [registerCgreportAigcModal, cgreportAigcModal] = useModal();
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

  function onCreateByAi() {
    cgreportAigcModal.openModal(true, {});
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, reload);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, () => {
      reload();
      selectedRowKeys.value = [];
    });
  }

  /**
   * Callback thành công
   */
  function handleSuccess() {
    reload();
  }

  function onAigcGenerate(aigcResult: Recordable) {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
      aigc: aigcResult
    });
  }

  /**
   * Thanh thao tác
   */
  function getAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }
  /**
   * Thanh thao tác dropdown
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Kiểm thử chức năng',
        class: ['low-app-hide'],
        onClick: () => onShowList(record.id),
      },
      {
        label: 'Địa chỉ cấu hình',
        class: ['low-app-hide'],
        onClick: () => onShowOnlineUrl(record),
      },
      {
        label: 'Sao chép',
        onClick: () => onCopyReport(record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Kiểm thử chức năng
   * @param id
   */
  function onShowList(id) {
    router.push({ path: '/online/cgreport/' + id });
  }

  /**
   * Địa chỉ cấu hình
   * @param record
   */
  function onShowOnlineUrl(record) {
    let id = record.id;
    let baseUrl = `/online/cgreport/${id}`;
    // Tạo menu chènsql
    let insertMenuSql = `INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external) 
                         VALUES ('${buildUUID()}', NULL, '${record.name}', '${baseUrl}', '1', 'OnlineAutoList', NULL, 0, NULL, '1', 0.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', null, NULL, NULL, 0)`;
    
    getReportParam(id)
      .then((arr) => {
        // Xử lý tham số
        let urlParam = '';
        if (arr && arr.length > 0) {
          for (let i of arr) {
            urlParam += i.paramName + '=${' + i.paramName + '}&';
          }
        }
        if (urlParam.length > 0) {
          urlParam = urlParam.substring(0, urlParam.length - 1);
          baseUrl += '?' + urlParam;
        }
      })
      .catch(() => {
        $message.warning('Lấy tham số thất bại!');
      })
      .finally(() => {
        // Bất kể việc lấy có thất bại hay không Đều bật popup hiển thị địa chỉ chuyển hướng
        $confirm({
          iconType: 'info',
          width: 500,
          title: `Liên kết menu 【${record.name}】`,
          content: baseUrl,
          closable: true,
          maskClosable: true,
          cancelText: 'Sao chépSQL',
          okText: 'Sao chépURL',
          cancelButtonProps: {
            class: 'copy-this-sql',
            'data-clipboard-text': insertMenuSql,
          } as any,
          okButtonProps: {
            class: 'copy-this-text',
            'data-clipboard-text': baseUrl,
          } as any,
          onOk() {
            return new Promise((resolve: any) => {
              const clipboard = new Clipboard('.copy-this-text');
              clipboard.on('success', () => {
                clipboard.destroy();
                $message.success('Sao chépURLThành công');
                resolve();
              });
              clipboard.on('error', () => {
                $message.error('Trình duyệt này không hỗ trợ sao chép tự động');
                clipboard.destroy();
                resolve();
              });
            });
          },
          onCancel() {
            return new Promise((resolve: any) => {
              const clipboard = new Clipboard('.copy-this-sql');
              clipboard.on('success', () => {
                clipboard.destroy();
                $message.success('Sao chép và chèn menuSQLThành công');
                resolve();
              });
              clipboard.on('error', () => {
                $message.error('Trình duyệt này không hỗ trợ sao chép tự động');
                clipboard.destroy();
                resolve();
              });
            });
          },
        });
      });
  }
  
  /**
   * Sao chép báo cáo
   * @param record
   */
  function onCopyReport(record) {
    $confirm({
      title: 'Sao chép báo cáo',
      content: 'Có xác nhận sao chép báo cáo này không?',
      iconType: 'warning',
      closable: true,
      onOk() {
        return doCopy(record.id)
          .then(() => {
            // Nhắc nhở thành công do defHttp Tự động bật ra (backend Result của message) , tại đây không bật thủ công nữa, tránh nhắc trùng lặp
            // Bắt buộc nhảy tới thứ 1 trang rồi làm mới: sắp xếp mặc định là createTime desc, báo cáo mới sao chép rơi vào thứ 1 trang,
            // Nếu người dùng hiện đang ở thứ N trang, thông thường reload() sẽ dùng lại theo current, dẫn đến không nhìn thấy dữ liệu mới.
            reload({ page: 1 });
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      },
    });
  }

  // Ném phương thức ra, để bên ngoài có thể gọi
  // Hiện tại 【lowApp】Trang dùng đến
  defineExpose({
    handleAdd,
  })

</script>
