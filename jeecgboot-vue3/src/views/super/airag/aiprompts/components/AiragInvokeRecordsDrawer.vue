<template>
  <BasicDrawer v-bind="getProps">
    <BasicTable @register="registerTable">
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" /> </template
    ></BasicTable>
  </BasicDrawer>
  <AiragTrackDetailModal @register="registerTrackDetailModal" />
</template>

<script lang="ts" setup>
  import type { DrawerProps } from '/@/components/Drawer';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { computed, unref, ref } from 'vue';
  import { useAttrs } from '@/hooks/core/useAttrs';
  import { BasicTable, TableAction } from '@/components/Table';
  import { useModal } from '@/components/Modal';
  import { getTrackList, deleteOne } from '@/views/super/airag/aiprompts/AiragExtData.api';
  import {useListPage} from "@/hooks/system/useListPage";
  import AiragTrackDetailModal from './AiragTrackDetailModal.vue';
  // Khai báo Emits
  const attrs = useAttrs();
  const [registerTrackDetailModal, { openModal }] = useModal();
  //Cấu hình cột
  const trackColumns = ref([
    { title: 'Câu hỏi', dataIndex: 'userQuery', key: 'userQuery' },
    { title: 'Đáp án prompt', dataIndex: 'promptAnswer', key: 'promptAnswer' },
    { title: 'Điểm đánh giá', dataIndex: 'answerScore', key: 'answerScore' },
    { title: 'Số phiên bản', dataIndex: 'version', key: 'version' },
  ]);
  // Đăng ký ngăn kéo
  const [registerDrawer, { closeDrawer }] = useDrawerInner(open);
  // Định nghĩa bảng
  const { tableContext } = useListPage({
    designScope: 'agent-config',
    tableProps: {
      title: 'Bản ghi gọi',
      api: getTrackList,
      columns: trackColumns.value,
      canResize: false,
      rowSelection: {
        columnWidth: 20,
      },
      immediate: false,
      beforeFetch: async (params) => {
        return Object.assign(params, { metadata: dataId.value });
      },
      afterFetch: async (res) => {
        if (res.length > 0) {
          res.forEach((item) => {
            if (item.dataValue) {
              let dataValue = JSON.parse(item.dataValue);
              item.answerScore = dataValue.answerScore;
              item.userQuery = dataValue.userQuery;
              item.promptAnswer = dataValue.promptAnswer;
            }
          });
        }
        return res;
      },
    },
  });
  const [registerTable, { reload }] = tableContext;
  // props cuối cùng của ngăn kéo
  const getProps = computed(() => {
    let drawerProps: Partial<DrawerProps> = {
      width: 1000,
      title: 'Bản ghi gọi',
      destroyOnClose: true,
    };
    let finalProps: Recordable = {
      ...unref(attrs),
      ...drawerProps,
      onCancel: closeDrawer,
      onRegister: registerDrawer,
    };
    return finalProps;
  });
  /** dataId */
  const dataId = ref<any>('');
  /** Ngăn kéo mở */
  function open(data) {
    dataId.value = data.record?.id || '';
    reload();
  }
  /**
   * Xóa
   */
  function handleDelete(record) {
    console.log(record, 'record');
    deleteOne({ id: record.id},reload)
  }

  /**
   * Chi tiết
   * @param record
   */
  function handleDetail(record) {
    openModal(true, { dataValue: record.dataValue });
  }
  /*
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
      },
    ];
  }
</script>

<style scoped lang="less">
  .dynamic-column-config {
    max-width: 1200px;
    margin: 0 auto;
    max-height: calc(100vh - 100px); /* Đặt chiều cao tối đa là 70 của chiều cao viewport% */
    overflow-y: auto; /* Khi vượt quá hiển thị thanh cuộn dọc */

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-card-body) {
      padding: 12px;
    }

    :deep(.ant-card-extra) {
      padding: 0;
    }
  }

  .column-list {
    margin-bottom: 24px;
  }

  .column-item {
    position: relative; /* Cung cấp tham chiếu cho khu vực thao tác định vị tuyệt đối */
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    margin-bottom: 16px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    transition: all 0.3s;

    &:hover {
      border-color: #1890ff;
      background: #f6fbff;
    }

    /* Kiểu khu vực thao tác */
    .action-area {
      position: absolute;
      top: 8px;
      right: 8px;

      :deep(.ant-btn) {
        width: 24px;
        height: 24px;
        min-width: 24px;
        padding: 0;

        .anticon {
          font-size: 14px;
        }
      }
    }
  }

  .column-form {
    flex: 1;
    width: 100%; /* Đảm bảo biểu mẫu chiếm hết độ rộng */
  }

  .form-row {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .form-item {
    flex: 1;

    &.full-width {
      flex: 3;
    }
  }

  .form-label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);

    &::after {
      content: '：';
    }
  }

  .error-text {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
  }

  .has-error {
    :deep(.ant-input) {
      border-color: #ff4d4f;
    }
  }

  .form-actions {
    display: none; /* Ẩn khu vực nút xóa ban đầu */
  }

  .fixed-columns {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px dashed #d9d9d9;

    .tip-text {
      margin-bottom: 16px;
      color: #666;
      font-size: 14px;
    }
  }

  .fixed-column-item {
    margin-bottom: 16px;
    padding: 16px;
    background: #f5f5f5;
    border: 1px solid #e8e8e8;
    border-radius: 6px;

    &:last-child {
      margin-bottom: 0;
    }

    :deep(.ant-input-group-addon) {
      background: #e6f7ff;
      color: #1890ff;
      font-weight: 500;
    }
  }

  .footer-actions {
    display: flex;
    justify-content: center;
    margin-top: 32px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    position: sticky;
    bottom: 0;
    background: #fff;
    padding-bottom: 8px;
  }
</style>
