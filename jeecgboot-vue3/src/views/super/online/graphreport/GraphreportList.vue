<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" v-bind="$attrs">
      <template #tableTitle>
        <a-button @click="onAdd" type="primary" preIcon="ant-design:plus">Thêm mới</a-button>
        <a-button preIcon="bxs:bot" type="primary" style="margin-right: 5px" @click="onCreateByAi">AITạo biểu đồ</a-button>
        <ExcelButton :config="excelConfig" />
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onBatchDelete">
                <a-icon type="delete" />
                <span>Xóa</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Thao tác hàng loạt</span>
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
  </div>
  <GraphreportModal @register="registerFormModal" @success="reload" />
  <!-- Thông quaAiTạo biểu đồ -->
  <GraphreportAigcModal @register="registerAigcModal" @generate="onAigcGenerate" />
</template>

<script lang="ts">
  import { ref, defineComponent, h } from 'vue';
  import { router } from '/@/router';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { doBatchDelete, list, Api, queryParamsList, doCopy } from './graphreport.api';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import ExcelButton from '/@/components/jeecg/ExcelButton.vue';
  import { useModal } from '/@/components/Modal';
  import { useCopyModal } from '../cgform/hooks/useCopyCgformModal';
  import GraphreportModal from './components/GraphreportModal.vue';
  import GraphreportAigcModal from "./components/GraphreportAigcModal.vue";

  // noinspection JSUnusedGlobalSymbols
  export default defineComponent({
    name: 'GraphreportList',
    components: {GraphreportAigcModal, ExcelButton, BasicTable, TableAction, GraphreportModal },
    setup() {
      // Tham số, phương thức dùng chung của trang danh sách
      const { prefixCls, doRequest, doDeleteRecord, tableContext, createConfirm: $confirm } = useListPage({
        designScope: 'online-graphreport-list',
        tableProps: {
          api: list,
          columns: [
            { title: 'Tên biểu đồ', dataIndex: 'name' },
            { title: 'Mã (code)', dataIndex: 'code' },
            { title: 'Dữ liệu', dataIndex: 'cgrSql' },
            {
              title: 'Nguồn dữ liệu',
              dataIndex: 'dbSource',
              customRender: ({ text, record }) => {
                return record['dbSource_dictText'] ? record['dbSource_dictText'] : text;
              },
            },
            { title: 'YTrường trục', dataIndex: 'yaxisField' },
            { title: 'XTrường trục', dataIndex: 'xaxisField' },
          ],
          formConfig: {
            //labelWidth: 200,
            schemas: [
              { label: 'Tên biểu đồ', field: 'name', component: 'JInput' },
              { label: 'Mã (code)', field: 'code', component: 'JInput' },
            ],
          },
        },
      });
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
      /**
       * excelCấu hình nhập/xuất
       */
      const excelConfig = {
        export: {
          name: 'OnlineBiểu đồ',
          url: Api.exportXls,
        },
        import: {
          url: Api.importXls,
          success: reload,
        },
      };
      // Đăng ký popup chỉnh sửa
      const [registerFormModal, formModal] = useModal();
      // Đăng kýAigcPopup
      const [registerAigcModal, aigcModal] = useModal();

      function onAdd() {
        formModal.openModal(true, { isUpdate: false });
      }

      function onCreateByAi() {
        aigcModal.openModal(true, {});
      }

      function onAigcGenerate(aigcResult: Recordable) {
        formModal.openModal(true, { isUpdate: false, aigc: aigcResult });
      }

      function onEdit(record) {
        formModal.openModal(true, { isUpdate: true, record });
      }

      // Sự kiện xóa hàng loạt
      async function onBatchDelete() {
        doRequest(() => doBatchDelete(selectedRowKeys.value));
      }

      // Kiểm thử chức năng
      function onGoToTest(record) {
        router.push('/online/graphreport/chart/' + record.id);
      }

      const { createCopyModal } = useCopyModal();

      // Hiển thị popup địa chỉ cấu hình
      async function onShowOnlineUrl(record) {
        let baseUrl = `/online/graphreport/chart/${record.id}`;
        let copyText = ref(baseUrl);
        createCopyModal({
          title: `Địa chỉ cấu hình 【${record.name}】`,
          content: () => h('div', {}, copyText.value),
          copyText: copyText,
          copyTitle: `${record.name}`,
          componentName: 'GraphreportAutoChart'
        });
        let result = await queryParamsList(record.id);
        if (result && result.length > 0) {
          let str = '?';
          result.forEach((item) => {
            str += item.paramName + '=${' + item.paramName + '}&';
          });
          str = str.substring(0, str.length - 1);
          copyText.value = `${baseUrl}${str}`;
        }
      }

      /**
       * Sao chép biểu đồ
       * @param record
       */
      function onCopyReport(record) {
        $confirm({
          title: 'Sao chép biểu đồ',
          content: 'Có xác nhận sao chép biểu đồ này không?',
          iconType: 'warning',
          closable: true,
          onOk() {
            return doCopy(record.id)
              .then(() => {
                // Nhắc nhở thành công do defHttp Tự động bật ra (backend Result của message) , tại đây không bật thủ công nữa, tránh nhắc trùng lặp
                // Bắt buộc nhảy tới thứ 1 trang rồi làm mới: biểu đồ mới sao chép rơi vào thứ 1 trang, thông thường reload() Dùng lại theo current sẽ không nhìn thấy dữ liệu mới
                reload({ page: 1 });
              })
              .catch((err) => {
                return Promise.reject(err);
              });
          },
        });
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
            label: 'Kiểm thử chức năng',
            class: ['low-app-hide'],
            onClick: () => onGoToTest(record),
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
              title: 'Xác nhận xóa chứ?',
              placement: 'left',
              confirm: () => doDeleteRecord(() => doBatchDelete([record.id])),
            },
          },
        ];
      }

      return {
        reload,
        prefixCls,
        rowSelection,
        selectedRowKeys,
        onAdd,
        getTableAction,
        getDropDownAction,
        onBatchDelete,
        registerTable,
        registerFormModal,
        excelConfig,
        registerAigcModal,
        onCreateByAi,
        onAigcGenerate,
      };
    },
  });
</script>
<style lang="less" scoped></style>
