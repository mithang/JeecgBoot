<template>
  <div>
    <!--Bảng tham chiếu-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection"  @expand="handleExpand">
      <!-- Khu vực bảng nhúng begin -->
<!--           <template #expandedRowRender="{record}">-->
<!--             <a-tabs tabPosition="top">-->
<!--               <a-tab-pane tab="Bảng header yêu cầu" key="openApiHeader" forceRender>-->
<!--                  <openApiHeaderSubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />-->
<!--               </a-tab-pane>-->
<!--               <a-tab-pane tab="Phần tham số yêu cầu" key="openApiParam" forceRender>-->
<!--                  <openApiParamSubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />-->
<!--               </a-tab-pane>-->
<!--             </a-tabs>-->
<!--           </template>-->
     <!-- Khu vực bảng nhúng end -->
     <!--Slot: Tiêu đề bảng-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'openapi:open_api:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới</a-button>
<!--          <a-button  type="primary" v-auth="'openapi:open_api:exportXls'"  preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất</a-button>-->
<!--          <j-upload-button  type="primary" v-auth="'openapi:open_api:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">Nhập</j-upload-button>-->
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    Xóa
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button  v-auth="'openapi:open_api:deleteBatch'">Thao tác hàng loạt
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
        <!-- Tìm kiếm nâng cao -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
       <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--Slot hiển thị lại trường-->
      <template v-slot:bodyCell="{ column, record, index, text }">
        <template v-if="column.dataIndex === 'requestUrl'">
          <a @click="handleCopyUrl(record)" title="Nhấp sao chép địa chỉ API đầy đủ">{{ text }}</a>
        </template>
      </template>
    </BasicTable>
    <!-- Vùng biểu mẫu -->
    <OpenApiDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="openapi-openApi" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import OpenApiDrawer from './components/OpenApiDrawer.vue';
  import { columns, searchFormSchema, superQuerySchema } from './OpenApi.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './OpenApi.api';
  const queryParam = reactive<any>({});
  const expandedRowKeys = ref<any[]>([]);
  const { createMessage } = useMessage();
  const API_DOMAIN = import.meta.env.VITE_GLOB_DOMAIN_URL;
  const [registerDrawer, { openDrawer }] = useDrawer();
   //Đăng ký dữ liệu bảng
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'Quản lý API',
           api: list,
           columns,
           canResize:false,
           formConfig: {
                //labelWidth: 120,
                schemas: searchFormSchema,
                autoSubmitOnEnter:true,
                showAdvancedButton:true,
                fieldMapToNumber: [
                ],
                fieldMapToTime: [
                ],
            },
           actionColumn: {
               width: 200,
               fixed:'right'
           },
           beforeFetch: (params) => {
             return Object.assign(params, queryParam);
           },
        },
        exportConfig: {
            name:"Quản lý API",
            url: getExportUrl,
            params: queryParam,
        },
        importConfig: {
            url: getImportUrl,
            success: handleSuccess
        },
    })

  const [registerTable, {reload},{ rowSelection, selectedRowKeys }] = tableContext

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
     * Sự kiện mở rộng
     * */
   function handleExpand(expanded, record){
        expandedRowKeys.value=[];
        if (expanded === true) {
           expandedRowKeys.value.push(record.id)
        }
    }
   /**
    * Sự kiện thêm mới
    */
  function handleAdd() {
     openDrawer(true, {
       isUpdate: false,
       showFooter: true,
     });
  }
   /**
    * Sự kiện chỉnh sửa
    */
  function handleEdit(record: Recordable) {
     openDrawer(true, {
       record,
       isUpdate: true,
       showFooter: true,
     });
   }
   /**
    * Chi tiết
   */
  function handleDetail(record: Recordable) {
     openDrawer(true, {
       record,
       isUpdate: true,
       showFooter: false,
     });
   }
   /**
    * Sự kiện xóa
    */
  async function handleDelete(record) {
     await deleteOne({id: record.id}, handleSuccess);
   }
   /**
    * Sự kiện xóa hàng loạt
    */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value},handleSuccess);
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
   * Sao chép địa chỉ API
   */
  async function handleCopyUrl(record: Recordable) {
    const url = API_DOMAIN + '/openapi/call/' + record.requestUrl;
    try {
      //update-begin---wangshuai---date:20260804  for: [LHZP-1353] openapi Quản lý API không sao chép được------------
      if (copyTextToClipboard(url)) {
        createMessage.success('Địa chỉ API đã sao chép');
      } else {
        createMessage.error('Sao chép thất bại, vui lòng sao chép thủ công');
      }
      //update-end---author:wangshuai ---date:20260804  for: [LHZP-1353]openapi Quản lý API không sao chép được------------
    } catch (_e) {
      createMessage.error('Sao chép thất bại, vui lòng sao chép thủ công');
    }
  }

  function getTableAction(record){
       return [
         {
           label: 'Chỉnh sửa',
           onClick: handleEdit.bind(null, record),
           auth: 'openapi:open_api:edit'
         },
       ]
   }


  /**
   * Thanh thao tác dropdown
   */
  function getDropDownAction(record){
    return [
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      }, {
        label: 'Xóa',
        popConfirm: {
          title: 'Bạn có xác nhận xóa không?',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft'
        },
        auth: 'openapi:open_api:delete'
      }
    ]
  }


</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>
