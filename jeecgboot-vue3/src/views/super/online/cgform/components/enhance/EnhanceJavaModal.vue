<template>
  <BasicModal @register="registerModal" title="JAVATăng cường" :width="1200" defaultFullscreen @cancel="onCancel">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button @click="onAdd" type="primary" preIcon="ant-design:plus">Thêm mới</a-button>
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

    <template #footer>
      <a-button @click="onCancel">Đóng</a-button>
      <div v-if="aiTestMode" style="float: left">
        <a-button @click="onGenEnhanceJavaData">Tạo dữ liệu kiểm thử</a-button>
      </div>
    </template>

    <!-- Vùng biểu mẫu -->
    <BasicModal v-bind="formModalProps">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm @register="registerForm" />
      </a-spin>
    </BasicModal>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { TableAction, BasicTable, ActionItem } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useOnlineTest } from '../../hooks/aitest/useOnlineTest';
  import { useJavaColumns, useJavaFormSchemas } from './enhance.data';
  import { doEnhanceJavaBatchDelete, getEnhanceJavaByCode, saveEnhanceJava } from './enhance.api';

  export default defineComponent({
    name: 'EnhanceJavaModal',
    components: { BasicModal, BasicTable, BasicForm, TableAction },
    emits: ['register'],
    setup() {
      const code = ref('');
      const btnList = ref<any[]>([]);
      const { columns } = useJavaColumns(btnList);
      // Tham số, phương thức dùng chung của trang danh sách
      const { doRequest, doDeleteRecord, tableContext } = useListPage({
        tableProps: {
          api: async (params) => {
            let { dataSource, btnList: $btnList } = await getEnhanceJavaByCode(code.value, params);
            btnList.value = $btnList;
            return dataSource;
          },
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
        reload();
      });
      // useOnlineAiTest
      const { aiTestMode, genEnhanceJavaData } = useOnlineTest({}, { reload, getDataSource }, null);
      // Đăng ký form Popup
      const [registerFormModal, formModal] = useModal();
      const isUpdate = ref(false);
      const formModalProps = reactive({
        onRegister: registerFormModal,
        title: computed(() => (isUpdate?.value ? 'Chỉnh sửa' : 'Thêm mới')),
        width: 800,
        confirmLoading: false,
        bodyStyle: { height: '350px' },
        onOk: onSubmit,
        onCancel: formModal.closeModal,
      });
      let formRecord = {};
      const { formSchemas } = useJavaFormSchemas(btnList);
      // Đăng ký form
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        // update-begin--author:liaozhiyang---date:20231017---for：【issues/790】Vấn đề ô văn bản trong popup không căn giữa
        labelCol: { xs: 24, sm: 5 },
        wrapperCol: { xs: 24, sm: 16 },
        // update-end--author:liaozhiyang---date:20231017---for：【issues/790】Vấn đề ô văn bản trong popup không căn giữa
      });

      function onCancel() {
        closeModal();
      }

      async function openFormModal(data) {
        isUpdate.value = data.isUpdate;
        formRecord = { ...(data.record ?? {}) };
        formModal.openModal();
        await nextTick();
        await resetFields();
        setFieldsValue(formRecord);
      }

      function onAdd() {
        openFormModal({ isUpdate: false });
      }

      // Nút chỉnh sửa
      function onEdit(record) {
        openFormModal({ isUpdate: true, record });
      }

      function onGenEnhanceJavaData() {
        // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-250】Tạo dữ liệu kiểm thử cần lọc bỏ những mục đã thêm nút trong trangjavaTăng cường
        const existingButtonCodes = (getDataSource?.() || []).map((item) => item.buttonCode).filter(Boolean);
        // đưa btnList Chuyển thành {buttonCode: buttonName} map, khi nhắc nhở, dùng tên tiếng Trung thân thiện hơn
        const buttonNameMap = (btnList.value || []).reduce((acc, item) => {
          if (item.buttonCode) acc[item.buttonCode] = item.buttonName || item.buttonCode;
          return acc;
        }, {});
        genEnhanceJavaData(code.value, existingButtonCodes, buttonNameMap);
        // update-end--author:liaozhiyang---date:20260714---for：【LHZP-250】Tạo dữ liệu kiểm thử cần lọc bỏ những mục đã thêm nút trong trangjavaTăng cường
      }

      // Sự kiện xóa hàng loạt
      async function onBatchDelete() {
        doRequest(() => doEnhanceJavaBatchDelete(selectedRowKeys.value));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let values = await validate();
          values = Object.assign({}, formRecord, values);
          await saveEnhanceJava(code.value, values, isUpdate.value);
          reload();
          formModal.closeModal();
        } finally {
          formModalProps.confirmLoading = false;
        }
      }

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
              confirm: () => doDeleteRecord(() => doEnhanceJavaBatchDelete([record.id])),
            },
          },
        ];
      }

      return {
        rowSelection,
        selectedRowKeys,
        aiTestMode,
        onCancel,
        onAdd,
        onGenEnhanceJavaData,
        onBatchDelete,
        getTableAction,
        getDropDownAction,
        formModalProps,
        registerModal,
        registerTable,
        registerForm,
      };
    },
    computed: {
      tableScroll() {
        let height = window.innerHeight;
        return {
          y: height - 320,
        };
      },
    },
  });
</script>
<style lang="less" scoped></style>
