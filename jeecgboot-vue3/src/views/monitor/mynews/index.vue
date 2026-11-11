<template>
  <div>
    <BasicTable @register="registerTable" :searchInfo="searchInfo" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handlerReadAllMsg">Đánh dấu tất cả đã đọc</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            Thao tác hàng loạt
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <DetailModal @register="register" />
    <keep-alive>
      <component v-if="currentModal" v-bind="bindParams" :key="currentModal" :is="currentModal" @register="modalRegCache[currentModal].register" />
    </keep-alive>
  </div>
</template>
<script lang="ts" name="monitor-mynews" setup>
import {ref, onMounted, unref} from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import DetailModal from './DetailModal.vue';
  import { getMyNewsList, editCementSend, syncNotic, readAllMsg, getOne, deleteAnnSend, deleteBatchAnnSend } from './mynews.api';
  import { columns, searchFormSchema } from './mynews.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getToken } from '/@/utils/auth';
  import { useModal } from '/@/components/Modal';
  import { useGlobSetting } from '/@/hooks/setting';
  const glob = useGlobSetting();
  const { createMessage } = useMessage();
  const checkedKeys = ref<Array<string | number>>([]);
  const content = ref({});
  const searchInfo = { logType: '1' };
  const [register, { openModal: openDetail }] = useModal();
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getLogList } from '/@/views/monitor/log/log.api';
  import { useRouter } from 'vue-router';
  import { useAppStore } from '/@/store/modules/app';
  import { useMessageHref } from '/@/views/system/message/components/useSysMessage';
  const appStore = useAppStore();
  const router = useRouter();
  const { currentRoute } = useRouter();
  const { goPage, currentModal, modalRegCache, bindParams } = useMessageHref();
  // Giải thích logic mã: 【QQYUN-13058】Tin nhắn của tôi được phân loại theo loại và hỗ trợ truy vấn loại theo tham số url
  const querystring = currentRoute.value.query;
  const findItem: any = searchFormSchema.find((item: any) => item.field === 'msgCategory');
  if (findItem) {
    if (querystring?.msgCategory) {
      findItem.componentProps.defaultValue = querystring.msgCategory
    } else if (querystring.noticeType) {
      findItem.componentProps.defaultValue = querystring.noticeType;
    } else {
      findItem.componentProps.defaultValue = null
    }
  }
  const { prefixCls, tableContext } = useListPage({
    designScope: 'mynews-list',
    tableProps: {
      title: 'Tin nhắn của tôi',
      api: getMyNewsList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        // Giải thích logic mã: 【TV360X-545】Danh sách tin nhắn của tôi không thể truy vấn theo khoảng thời gian---
        fieldMapToTime: [['sendTime', ['sendTimeBegin', 'sendTimeEnd'], 'YYYY-MM-DD']],
      },
      beforeFetch: (params) => {
        // Giải thích logic mã: 【QQYUN-13058】Tin nhắn của tôi được phân loại theo loại và hỗ trợ truy vấn loại theo tham số url
        if (params.msgCategory) {
          if (['1', '2'].includes(params.msgCategory)) {
            params.msgCategory = params.msgCategory;
          } else {
            params.noticeType = params.msgCategory;
            delete params.msgCategory;
          }
        } else {
          if (querystring?.msgCategory) {
            params.msgCategory = querystring.msgCategory;
          } else if (querystring.noticeType) {
            params.noticeType = querystring.noticeType;
          }
        }
        return params;
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;
  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Xem',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa không?',
          confirm: handleDelete.bind(null, record.id),
        },
        ifShow: record.readFlag === 1
      }
    ];
  }

  /**
   * Xem
   */
  function handleDetail(record) {
    let anntId = record.anntId;
    editCementSend({ anntId: anntId }).then((res) => {
      reload();
      syncNotic({ anntId: anntId });
    });
    const openModalFun = ()=>{
      openDetail(true, {
        record,
        isUpdate: true,
      });
    }
    goPage(record, openModalFun);

  }
  // Loại nhật ký
  function callback(key) {
    searchInfo.logType = key;
    reload();
  }

  // Đánh dấu tất cả đã đọc
  function handlerReadAllMsg() {
    readAllMsg({}, reload);
  }

  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  // Giải thích logic mã: tin nhắn chuyển hướng, mở biểu mẫu chi tiết
  onMounted(()=>{
    initHrefModal();
  });
  function initHrefModal(){
    let params = appStore.getMessageHrefParams;
    if(params){
      let anntId = params.id;
      if(anntId){
        editCementSend({ anntId: anntId }).then(() => {
          reload();
          syncNotic({ anntId: anntId });
        });
      }
      let detailId = params.detailId;
      if(detailId){
        getOne(detailId).then(data=>{
          console.log('getOne', data)
          openDetail(true, {
            record: data,
            isUpdate: true,
          });
          appStore.setMessageHrefParams('')
        })
      }
    }
  }

  function handleSuccess() {
    selectedRowKeys.value = [];
    reload();
  }
  
  /**
   * Xóa tin nhắn của tôi
   * 
   * @param id
   */
  async function handleDelete(id) {
    await deleteAnnSend({ id: id }, handleSuccess);
  }

  /**
   * Xóa hàng loạt tin nhắn của tôi
   */
  async function batchHandleDelete() {
    let unRead = unref(selectedRows).filter((item) => item.readFlag == 0);
    if (unref(unRead).length > 0) {
      createMessage.warning('Không được xóa tin nhắn chưa đọc!');
      return;
    }
    await deleteBatchAnnSend({ ids: selectedRowKeys.value }, handleSuccess);
  }
</script>
