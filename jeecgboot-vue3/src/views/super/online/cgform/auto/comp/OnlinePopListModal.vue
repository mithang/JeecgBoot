<template>
    <BasicModal @register="registerModal" :width="popModalFixedWidth" :dialogStyle="{top: '70px'}" :bodyStyle="popBodyStyle" :title="modalTitle" wrapClassName="jeecg-online-pop-list-modal">
        <template #footer>
            <div style="display: inline-block;width: calc(100% - 140px);text-align: left;">
                <a-button v-if="addAuth" style="border-radius: 50px" type="primary" @click="handleAdd"><PlusOutlined/>Thêm bản ghi</a-button>
            </div>
            <a-button key="back" @click="handleCancel">Đóng</a-button>
            <a-button :disabled="submitDisabled" key="submit" type="primary" @click="handleSubmit" :loading="submitLoading">Xác nhận</a-button>
        </template>
        
        
        <BasicTable @register="registerTable" :rowSelection="rowSelection">
            
            <!-- update-begin-author:taoyan date:2023-7-11 for: issues/4992 onlinePhát triển biểu mẫu Loại control của trường là bản ghi liên kết Khi thêm mới, danh sách lựa chọn có thể thêm truy vấn không -->
            <template #tableTitle>
                <a-input-search v-model:value="searchText" @search="onSearch"  placeholder="Vui lòng nhập từ khóa, nhấn Enter để tìm kiếm"  style="width: 240px" />
            </template>
            <!-- update-end-author:taoyan date:2023-7-11 for: issues/4992 onlinePhát triển biểu mẫu Loại control của trường là bản ghi liên kết Khi thêm mới, danh sách lựa chọn có thể thêm truy vấn không -->
            
            <!--Thanh thao tác-->
            <template #action="{ record }">
                <TableAction :actions="getTableAction(record)">
                </TableAction>
            </template>

            <template #fileSlot="{ text }">
                <span v-if="!text" style="font-size: 12px; font-style: italic">Không có tệp</span>
                <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadRowFile(text)"> Tải xuống </a-button>
            </template>

            <template #imgSlot="{ text }">
                <span v-if="!text" style="font-size: 12px; font-style: italic">Không có hình ảnh</span>
                <img v-else :src="getImgView(text)" alt="Hình ảnh không tồn tại" class="online-cell-image" @click="viewOnlineCellImage(text)" />
            </template>

            <template #htmlSlot="{ text }">
                <div v-html="sanitizeRichText(text)"></div>
            </template>

            <template #pcaSlot="{ text, column }">
                <div :title="getPcaText(text, column)">{{ getPcaText(text, column) }}</div>
            </template>

            <template #dateSlot="{ text, column }">
                <span>{{ getFormatDate(text, column) }}</span>
            </template>
            
        </BasicTable>
    </BasicModal>

    <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa -->
    <online-pop-modal :id="id" @register="registerPopModal" @success="handleDataSave" topTip></online-pop-modal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref, toRaw, computed, inject, type Ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import OnlinePopModal from './OnlinePopModal.vue';
  import { useFixedHeightModal } from '../../hooks/auto/useAutoModal';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  
  export default defineComponent({
    name: 'OnlinePopListModal',
    props: {
      /**có thể là tên bảng có thể làID*/
      id: {
        type: String,
        default: '',
      },
      multi:{
        type: Boolean,
        default: false
      },
      addAuth:{
        type: Boolean,
        default: true
      }
    },
    components: {
      BasicModal,
      BasicTable,
      TableAction,
      PlusOutlined,
      OnlinePopModal
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      // Popup vẫn thuộc về hiện tại Online Ngữ cảnh biểu mẫu, phải mang theo mã biểu mẫu nguồn mới có thể đi qua ủy quyền chỉ đọc có kiểm soát của bản ghi liên kết.
      const sourceFormCode = inject<Ref<string | null>>('tableId');
      // Kiểm soát chiều cao popup
      const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
      const searchText = ref('');
      const modalWidth = ref(800);
      //useModalInner
      const [registerModal, {closeModal}] = useModalInner((data) => {
        searchText.value = '';
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
        selectedRowKeys.value = data.selectedRowKeys;
        selectedRows.value = data.selectedRows;
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
        setPagination({current:1})
        reload();
        resetBodyStyle();
      });

      // Dùng để onlineTrong biểu mẫu Bật ra biểu mẫu khác
      const [registerPopModal, { openModal: openPopModal }] = useModal();
      
      function handleCancel() {
        closeModal();
      }
      const submitDisabled = computed(()=>{
        const arr = selectedRowKeys.value;
        if(arr && arr.length>0){
          return false;
        }
        return true;
      })
      const submitLoading = ref(false);
      function handleSubmit(){
        submitLoading.value = true;
        let arr = toRaw(selectedRows.value);
        if(arr && arr.length>0){
          emit('success', arr)
          closeModal();
        }
        setTimeout(()=>{
          submitLoading.value = false
        }, 200);
      }
      
      //---------------------Danh sách------------------------
      /**
       * chỉ để Online Yêu cầu bản ghi liên kết trong biểu mẫu bổ sung mã biểu mẫu nguồn; khi không có ngữ cảnh, giữ nguyên hoàn toàn tham số gốc.
       */
      function getLinkTableParams(params = {}) {
        return sourceFormCode?.value ? { ...params, linkTableSourceCode: sourceFormCode.value } : params;
      }

      function queryTableData(params){
        const url = '/online/cgform/api/getData/'+props.id;
        return defHttp.get({ url, params: getLinkTableParams(params) });
      }
      
      function list(params){
        params['column'] = 'id';
        return new Promise(async (resolve, _reject) => {
          const aa = await queryTableData(params)
          resolve(aa);
        })
      }

      const onlineTableContext = {
        isPopList: true,
        reloadTable(){
          console.log('reloadTable')
        },
        isTree(){
          return false;
        }
      };
      const extConfigJson = ref<any>({});

      // Xử lý BasicTable cấu hình của
      const {
        columns,
        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,
        handleColumnResult,
        hrefComponent,
        viewOnlineCellImage,
      } = useTableColumns(onlineTableContext, extConfigJson);


      /**
       * Truy vấntableThông tin cột và các cấu hình khác
       */
      function getColumnList() {
        const url = '/online/cgform/api/getColumns/'+props.id;
        return new Promise((resolve, reject) => {
          defHttp.get({url, params: getLinkTableParams()}, { isTransformResponse: false }).then((res) => {
            if (res.success) {
              resolve(res.result);
            } else {
              $message.warning(res.message);
              reject();
            }
          })
        });
      }

      const modalTitle = ref('')
      watch(()=>props.id, async ()=>{
        let columnResult:any = await getColumnList();
        handleColumnResult(columnResult);
        modalTitle.value = columnResult.description;
      }, {immediate: true})

      const { tableContext } = useListPage({
        designScope: 'process-design',
        pagination: true,
        tableProps: {
          title: '',
          api: list,
          clickToRowSelect: true,
          columns: columns,
          showTableSetting: false,
          immediate:false,
          //showIndexColumn: true,
          canResize: false,
          showActionColumn: false,
          actionColumn: {
            dataIndex: 'action',
            slots: { customRender: 'action' },
          },
          useSearchForm: false,
          beforeFetch: (params) => {
            return addQueryParams(params);
          },
        },
      });
      const [registerTable, { reload, setPagination }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
      watch(()=>props.multi, (val)=>{
        if(val==true){
          rowSelection.type = 'checkbox'
        }else{
          rowSelection.type = 'radio'
        }
      }, {immediate: true});

      /**
       * Thanh thao tác
       */
      function getTableAction(record) {
        return [
          {
            label: 'Chỉnh sửa',
            onClick: handleUpdate.bind(null, record),
          }
        ];
      }
  
      function handleUpdate(record){
        console.log('handleUpdate', record)
      }
     
      function onSearch(){
        reload();
      }
      const eqConditonTypes = ['int', 'double', 'long', 'Date', 'Datetime', 'BigDecimal']
      function addQueryParams(params){
        let text = searchText.value;
        if(!text){
          params['superQueryMatchType'] = 'or';
          params['superQueryParams'] = ''
          return params;
        }
        let arr = columns.value;
        let conditions:any[] = []
        if(arr && arr.length>0){
          for(let item of arr){
            if(item.dbType){
              if(item.dbType == 'string'){
                conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'like', val: text})
              }else if(item.dbType == 'Date'){
                if(text.length=='2020-10-10'.length){
                  conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'eq', val: text})
                }
              }else if(item.dbType == 'Datetime'){
                if(text.length=='2020-10-10 10:10:10'.length){
                  conditions.push({field: item.dataIndex,type:item.dbType.toLowerCase(), rule: 'eq', val: text})
                }
              }else if(eqConditonTypes.indexOf(item.dbType)){
                conditions.push({field: item.dataIndex, type:item.dbType.toLowerCase(), rule: 'eq', val: text})
              }else{
                //text blobkhông xử lý
              }
            }
          }
        }
        params['superQueryMatchType'] = 'or';
        params['superQueryParams'] = encodeURI(JSON.stringify(conditions));
        return params;
      }
  
      function handleAdd(){
        openPopModal(true, {})
      }
      
      // modalThêm mới dữ liệu hoàn tất Đóng trực tiếplist, đưa dữ liệu thêm mới trở lại biểu mẫu
      function handleDataSave(data){
        console.log('handleDateSave' ,data)
        // update-begin--author:liaozhiyang---date:20250429---for：【issues/8163】Thêm mới bản ghi liên kết bị mất dữ liệu
        let arr = [data, ...selectedRows.value];
        // update-end--author:liaozhiyang---date:20250429---for：【issues/8163】Thêm mới bản ghi liên kết bị mất dữ liệu
        emit('success', arr);
        closeModal();
        //reload();
      }
      
      return {
        registerModal,
        modalWidth,
        handleCancel,
        submitDisabled,
        submitLoading,
        handleSubmit,

        registerTable,
        getTableAction,
        searchText,
        onSearch,

        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,
        sanitizeRichText,
        hrefComponent,
        viewOnlineCellImage,
        rowSelection,
        modalTitle,

        registerPopModal,
        handleAdd,
        reload,

        popModalFixedWidth,
        popBodyStyle,
        handleDataSave
      };
    },
  });
</script>

<style lang="less" scoped>
.online-cell-image {
  max-height: 30px;
  max-width: 50px;
  object-fit: contain;
  cursor: pointer;
}
</style>
