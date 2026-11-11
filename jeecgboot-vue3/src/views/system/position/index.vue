<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">Thêm mới</a-button>
        <a-popover placement="bottomLeft" trigger="hover">
          <template #content>
            <div class="position-level-popover">
              <div class="position-level-popover__title">Biểu thức cấp bậc quy trình chỉ nhận dạng 6 cấp bậc định sẵn</div>
              <div class="position-level-popover__hint">Tên cấp bậc chức danh và số thứ tự cấp bậc phải khớp nghiêm ngặt, số càng nhỏ thì cấp bậc càng cao.</div>
              <div class="position-level-popover__warning">
                6 cấp bậc định sẵn này không được phép sửa hoặc xóa. Các chức danh khác có thể duy trì bình thường, nhưng không thể dùng cho biểu thức cấp bậc của quy trình, nếu không quy trình sẽ không khớp được với người phê duyệt.
              </div>
              <div class="position-level-popover__rules">
                <div><span>Cấu hình</span>Cấp bậc ứng viên chỉ có thể cấu hình trên phòng ban, không thể liên kết trực tiếp với người dùng.</div>
                <div><span>Khái niệm</span>'Chức danh' trong hồ sơ người dùng đến từ từ điển, khác với cấp bậc chức danh ở đây, là hai khái niệm độc lập.</div>
              </div>
              <div class="position-level-popover__grid">
                <div v-for="item in positionLevelTips" :key="item.level" class="position-level-popover__item">
                  <span class="position-level-popover__level">{{ item.level }}</span>
                  <span class="position-level-popover__name">{{ item.name }}</span>
                  <span :class="['position-level-popover__type', `is-${item.type}`]">{{ item.typeName }}</span>
                </div>
              </div>
            </div>
          </template>
          <a-button class="position-level-help" preIcon="ant-design:info-circle-outlined">Quy trình chỉ hỗ trợ 6 cấp bậc định sẵn</a-button>
        </a-popover>
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
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <PositionModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="system-position" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getPositionList, deletePosition, batchDeletePosition, customUpload, getExportUrl, getImportUrl } from './position.api';
  import { columns, searchFormSchema } from './position.data';
  import PositionModal from './PositionModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const positionLevelTips = [
    { id: '1958471111989067778', name: 'Chủ tịch', level: 1, type: 'leader', typeName: 'Cấp lãnh đạo' },
    { id: '1958471074953363458', name: 'Tổng giám đốc', level: 2, type: 'leader', typeName: 'Cấp lãnh đạo' },
    { id: '1958471030867034113', name: 'Phó tổng giám đốc', level: 3, type: 'leader', typeName: 'Cấp lãnh đạo' },
    { id: '1958470912214368258', name: 'Bộ trưởng', level: 4, type: 'staff', typeName: 'Cấp bậc nhân viên' },
    { id: '1958470865577902082', name: 'Phó bộ trưởng', level: 5, type: 'staff', typeName: 'Cấp bậc nhân viên' },
    { id: '1958470823064436737', name: 'Nhân viên', level: 6, type: 'staff', typeName: 'Cấp bậc nhân viên' },
  ];

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'position-template',
    tableProps: {
      title: 'Danh sách chức danh',
      api: getPositionList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        labelWidth: 80,
      },
      actionColumn: {
        width: 180,
      },
      showIndexColumn: true,
      //update-begin---author:scott ---date:20260806  for：【LHZP-1183】Hạn chế sửa và xóa cấp bậc định sẵn của quy trình-----------
      rowSelection: {
        type: 'checkbox',
        getCheckboxProps: (record) => ({
          disabled: isDefaultPosition(record),
        }),
      },
      //update-end---author:scott ---date:20260806  for：【LHZP-1183】Hạn chế sửa và xóa cấp bậc định sẵn của quy trình-----------
      defSort: {
        column: "",
        order: ""
      }
    },
    exportConfig: {
      name: 'Danh sách chức danh',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    //update-begin---author:scott ---date:20260806  for：【LHZP-1183】Hạn chế sửa và xóa cấp bậc định sẵn của quy trình-----------
    const isDefault = isDefaultPosition(record);
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        disabled: isDefault,
        tooltip: isDefault ? 'Cấp bậc định sẵn của quy trình không được phép sửa' : undefined,
      },
      {
        label: 'Xóa',
        disabled: isDefault,
        tooltip: isDefault ? 'Cấp bậc định sẵn của quy trình không được phép xóa' : undefined,
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
    //update-end---author:scott ---date:20260806  for：【LHZP-1183】Hạn chế sửa và xóa cấp bậc định sẵn của quy trình-----------
  }

  /**
   * Dựa vào ID dữ liệu khởi tạo hệ thống để xác định có phải cấp bậc tích hợp sẵn của quy trình hay không
   * @author scott
   * @since 2026-08-06 LHZP-1183
   */
  function isDefaultPosition(record) {
    return positionLevelTips.some((item) => item.id === record?.id);
  }

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
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
    await deletePosition({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeletePosition({ ids: selectedRowKeys.value }, () => {
      // Giải thích logic mã: 【QQYUN-8334】Sau khi xóa hàng loạt, nút chưa ẩn, bản ghi đã chọn vẫn còn
      selectedRowKeys.value = [];
      reload();
    });
  }
</script>
<style lang="less" scoped>
  .position-level-help {
    color: #ad6800;
    border-color: #ffd591;
    background: #fffaf0;

    &:hover,
    &:focus {
      color: #873800;
      border-color: #faad14;
      background: #fff7e6;
    }
  }

  .position-level-popover {
    width: 480px;
    max-width: calc(100vw - 48px);
    padding: 4px;

    &__title {
      color: #262626;
      font-weight: 600;
    }

    &__hint {
      margin-top: 4px;
      color: #8c8c8c;
      font-size: 12px;
    }

    &__grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 8px;
      margin-top: 12px;
    }

    &__warning {
      margin-top: 10px;
      padding: 7px 9px;
      color: #ad4e00;
      font-size: 12px;
      line-height: 1.6;
      border-left: 3px solid #faad14;
      border-radius: 0 4px 4px 0;
      background: #fffbe6;
    }

    &__rules {
      display: grid;
      gap: 6px;
      margin-top: 10px;
      color: #595959;
      font-size: 12px;
      line-height: 1.6;

      span {
        display: inline-block;
        margin-right: 7px;
        padding: 0 5px;
        color: #0958d9;
        line-height: 18px;
        border-radius: 3px;
        background: #e6f4ff;
      }
    }

    &__item {
      display: flex;
      align-items: center;
      min-width: 0;
      padding: 7px 8px;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
      background: #fafafa;
    }

    &__level {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex: 0 0 22px;
      height: 22px;
      margin-right: 8px;
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      border-radius: 50%;
      background: #1677ff;
    }

    &__name {
      flex: 1;
      color: #262626;
      white-space: nowrap;
    }

    &__type {
      margin-left: 6px;
      padding: 1px 5px;
      font-size: 11px;
      white-space: nowrap;
      border-radius: 4px;

      &.is-leader {
        color: #0958d9;
        background: #e6f4ff;
      }

      &.is-staff {
        color: #237804;
        background: #f6ffed;
      }
    }
  }
</style>
