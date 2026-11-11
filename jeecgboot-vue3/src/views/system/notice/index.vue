<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">Tạo mới</a-button>
<!--        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>-->
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>-->
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
            <Icon style="fontsize: 12px" icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <NoticeModal @register="registerModal" @success="handleSuccess" />
    <DetailModal @register="register" :frameSrc="iframeUrl" />
  </div>
</template>
<script lang="ts" name="system-notice" setup>
  import { ref, onMounted } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import NoticeModal from './NoticeModal.vue';
  import DetailModal from './DetailModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';
  import { columns, searchFormSchema } from './notice.data';
  import { getList, deleteNotice, batchDeleteNotice,editIzTop, getExportUrl, getImportUrl, doReleaseData, doReovkeData } from './notice.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useAppStore } from '/@/store/modules/app';

  const appStore = useAppStore();
  const glob = useGlobSetting();
  const [registerModal, { openModal }] = useModal();
  const [register, { openModal: openDetail }] = useModal();
  const iframeUrl = ref('');
  const { createMessage, createConfirm } = useMessage();
  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, onExportXls, onImportXls, tableContext, doRequest } = useListPage({
    designScope: 'notice-template',
    tableProps: {
      title: 'Thông báo tin nhắn',
      api: getList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['sendTime', ['sendTime_begin', 'sendTime_end'], 'YYYY-MM-DD']]
      }
    },
    exportConfig: {
      name: 'Danh sách thông báo tin nhắn',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext;
  //Mã quy trình
  const flowCode = 'dev_sys_announcement_001';
  /**
   * Thêm mới/Chỉnh sửa lưu thành công
   * Giải thích logic mã: Loại tin nhắn của điều kiện truy vấn mặc định là"Thông báo", nếu cái được lưu là"Tin nhắn hệ thống"và các loại khác,
   * Dữ liệu sẽ bị điều kiện truy vấn lọc mất, người dùng tưởng nhầm là chưa lưu thành công, tại đây tự động chuyển điều kiện truy vấn sang loại tin nhắn của bản ghi dữ liệu đó
   */
  async function handleSuccess(values) {
    const msgCategory = values?.msgCategory;
    if (msgCategory) {
      const searchForm = getForm();
      const searchMsgCategory = searchForm.getFieldsValue()?.msgCategory;
      // Điều kiện truy vấn trống nghĩa là truy vấn tất cả, không cần chuyển đổi
      if (searchMsgCategory && searchMsgCategory != msgCategory) {
        await searchForm.setFieldsValue({ msgCategory });
        reload({ page: 1 });
        return;
      }
    }
    reload();
  }
  /**
   * Sự kiện thêm mới
   */
  function handleAdd(record = {}) {
    openModal(true, {
      isUpdate: false,
      record,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteNotice({ id: record.id }, reload);
  }
  /**
   * Thao tác ghim đầu
   */
  async function handleTop(record, izTop) {
     await editIzTop({ id: record.id, izTop }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    doRequest(() => batchDeleteNotice({ ids: selectedRowKeys.value }));
  }
  /**
   * Phát hành
   */
  async function handleRelease(id) {
    await doReleaseData({ id });
    reload();
  }
  /**
   * Thu hồi
   */
  async function handleReovke(id) {
    await doReovkeData({ id });
    reload();
  }
  /**
   * Xem
   */
  function handleDetail(record) {
    iframeUrl.value = `${glob.uploadUrl}/sys/annountCement/show/${record.id}?token=${getToken()}`;
    openDetail(true, { record });
  }

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        ifShow: record.sendStatus == 0 || record.sendStatus == '2',
      },
      {
        label: 'Xem',
        onClick: handleDetail.bind(null, record),
        ifShow: record.sendStatus == 1,
      },
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Xóa',
        ifShow: record.sendStatus != 1,
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'Phát hành',
        ifShow: (!record?.izApproval || record.izApproval == '0') && record.sendStatus == 0,
        onClick: handleRelease.bind(null, record.id),
      },
      {
        label: 'Thu hồi',
        ifShow: record.sendStatus == 1,
        popConfirm: {
          title: 'Bạn có chắc muốn thu hồi không?',
          confirm: handleReovke.bind(null, record.id),
        },
      },
      {
        label: 'Phát hành',
        ifShow: record.sendStatus == '2',
        popConfirm: {
          title: 'Bạn có chắc muốn phát hành lại không?',
          confirm: handleRelease.bind(null, record.id),
        },
      },
      {
        label: 'Ghim đầu',
        onClick: handleTop.bind(null, record, 1),
        ifShow: record.sendStatus == 1 && record.izTop == 0,
      },
      {
        label: 'Bỏ ghim đầu',
        onClick: handleTop.bind(null, record, 0),
        ifShow: record.sendStatus == 1 && record.izTop == 1,
      },
    ];
  }

  onMounted(() => {
    // Giải thích logic mã: 【JHHB-128】Chuyển thành thông báo
    const params = appStore.getMessageHrefParams;
    if (params?.add) {
      delete params.add;
      handleAdd(params);
      appStore.setMessageHrefParams('');
    }
  });
</script>
