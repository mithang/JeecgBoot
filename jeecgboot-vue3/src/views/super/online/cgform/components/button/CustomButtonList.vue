<template>
  <BasicModal @register="registerModal" title="Nút tùy chỉnh" :width="1200" defaultFullscreen @cancel="onCancel">
    <template #footer>
      <a-button @click="onCancel">Đóng</a-button>
      <div v-if="aiTestMode" style="float: left">
        <a-button :loading="generating" @click="onGenButtons">Tạo dữ liệu kiểm thử</a-button>
      </div>
    </template>

    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button @click="onAdd" type="primary" preIcon="ant-design:plus">Thêm mới</a-button>
        <a-button @click="onOpenBIButton" preIcon="ant-design:setting">Quản lý nút tích hợp sẵn</a-button>

        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onBatchDelete">
                <a-icon type="delete" />
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button style="margin-left: 8px">
            Thao tác hàng loạt
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </template>

      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>

    <!-- Vùng biểu mẫu -->
    <BasicModal v-bind="formModalProps">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm @register="registerForm" />
      </a-spin>
    </BasicModal>
  </BasicModal>

  <!-- Quản lý nút tích hợp sẵn -->
  <BuiltInButtonList @register="registerBIButtonModal" :record="record"/>

</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent } from 'vue';

  import { useOnlineTest } from '../../hooks/aitest/useOnlineTest';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { list, doBatchDelete, saveOrUpdate } from './button.api';
  import { columns, formSchemas } from './button.data';
  import BuiltInButtonList from './BuiltInButtonList.vue';
  import {useMessage} from "@/hooks/web/useMessage";

  export default defineComponent({
    name: 'CustomButtonList',
    components: { BasicModal, BasicTable, TableAction, BasicForm, BuiltInButtonList },
    emits: ['register'],
    setup() {
      const {createMessage: $message} = useMessage();

      const code = ref('');
      const record = ref()
      // Tham số, phương thức dùng chung của trang danh sách
      const { doRequest, doDeleteRecord, tableContext } = useListPage({
        tableProps: {
          api: (params) => list(code.value, params),
          columns,
          canResize: false,
          useSearchForm: false,
          beforeFetch(params) {
            return Object.assign(params, { column: 'orderNum', order: 'asc' });
          },
        },
      });
      // Đăng kýtableDữ liệu
      const [registerTable, { reload, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
      // Popup đăng ký
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        code.value = data.row.id;
        record.value = data.row
        reload();
      });
      // useOnlineAiTest
      const { aiTestMode, genButtons } = useOnlineTest({}, { reload }, null);
      const generating = ref(false);
      // Đăng ký form Popup
      const [registerFormModal, formModal] = useModal();
      const isUpdate = ref(false);
      const formModalProps = reactive({
        onRegister: registerFormModal,
        title: computed(() => (isUpdate?.value ? 'Chỉnh sửa' : 'Thêm mới')),
        width: 800,
        confirmLoading: false,
        onOk: onSubmit,
        onCancel: formModal.closeModal,
      });
      let formRecord = {};
      // Đăng ký form
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        // update-begin--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
        schemas: formSchemas({ redoModalHeight: formModal.redoModalHeight }),
        // update-end--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
        showActionButtonGroup: false,
      });

      async function openFormModal(data) {
        isUpdate.value = data.isUpdate;
        formRecord = { ...(data.record ?? {}) };
        formModal.openModal();
        await nextTick();
        await resetFields();
        setFieldsValue(formRecord);
      }

      // Nút thêm mới
      function onAdd() {
        openFormModal({ isUpdate: false });
      }

      // Nút chỉnh sửa
      function onEdit(record) {
        openFormModal({ isUpdate: true, record });
      }

      function onCancel() {
        closeModal();
      }

      // update-begin--author:liaozhiyang---date:20260824---for：【LHZP-195】Lọc các nút tùy chỉnh đã thêm và ngăn gửi trùng lặp
      async function onGenButtons() {
        if (generating.value) return;
        generating.value = true;
        try {
          const existingButtonCodes = (getDataSource?.() || []).map((item) => item.buttonCode).filter(Boolean);
          await genButtons(code.value, existingButtonCodes);
        } finally {
          generating.value = false;
        }
      }
      // update-end--author:liaozhiyang---date:20260824---for：【LHZP-195】Lọc các nút tùy chỉnh đã thêm và ngăn gửi trùng lặp

      // Sự kiện xóa hàng loạt
      async function onBatchDelete() {
        doRequest(() => doBatchDelete(selectedRowKeys.value));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let values = await validate();
          values = Object.assign({ cgformHeadId: code.value }, formRecord, values);
          await saveOrUpdate(values, isUpdate.value);
          reload();
          formModal.closeModal();
        } finally {
          formModalProps.confirmLoading = false;
        }
      }

      // -------------------- [Begin] Popup nút tích hợp sẵn ------------------

      const [registerBIButtonModal, bIButtonModal] = useModal()

      /**
       * Mở popup quản lý nút tích hợp sẵn
       */
      function onOpenBIButton() {
        if (record.value?.tableType == 3) {
          $message.warn('Bảng phụ không hỗ trợ quản lý nút tích hợp sẵn, vui lòng chọn bảng chính tương ứng')
          return
        }
        bIButtonModal.openModal(true, {});
      }

      // ---------------------- [End] Popup nút tích hợp sẵn ------------------

      /**
       * Thanh thao tác
       */
      function getTableAction(record) {
        return [
          {
            label: 'Chỉnh sửa',
            onClick: () => onEdit(record),
          },
        ];
      }

      /**
       * Thanh thao tác dropdown
       */
      function getDropDownAction(record): ActionItem[] {
        return [
          {
            label: 'Xóa',
            popConfirm: {
              title: 'Xác nhận xóa chứ?',
              placement: 'left',
              confirm: () => doDeleteRecord(() => doBatchDelete([record.id])),
            },
          },
        ];
      }

      return {
        code,
        record,
        onAdd,
        onEdit,
        onBatchDelete,
        aiTestMode,
        generating,
        onGenButtons,
        registerModal,
        registerTable,
        selectedRowKeys,
        rowSelection,
        onCancel,
        getTableAction,
        getDropDownAction,
        registerForm,
        formModalProps,
        registerBIButtonModal,
        onOpenBIButton,
      };
    },
  });
</script>
<style lang="less" scoped></style>
