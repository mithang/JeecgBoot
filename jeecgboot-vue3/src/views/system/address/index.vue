<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" type="flex" :gutter="10" style="max-height: 800px">
    <a-col :xl="6" :lg="24" :md="24" style="margin-bottom: 10px">
      <DepartLeftTree ref="leftTree" @select="onTreeSelect" />
    </a-col>
    <a-col :xl="18" :lg="24" :md="24" style="margin-bottom: 10px">
      <div style="height: 100%;" class="address-book">
        <!--Tham chiếu bảng-->
        <BasicTable @register="registerTable">
          <template #post="{ text }">
            {{
              (text || '')
                .split(',')
                .map((t) => (positionInfo[t] ? positionInfo[t] : t))
                .join(',')
            }}
          </template>
          <template #action="{ record }">
            <TableAction :actions="getTableAction(record)" />
          </template>
        </BasicTable>
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
  import { provide, ref, unref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DepartLeftTree from './components/DepartLeftTree.vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './address.data';
  import { list, positionList } from './address.api';
  import { getCacheByDynKey } from "@/utils/auth";
  import { JEECG_CHAT_UID } from "@/enums/cacheEnum";

  const { prefixCls } = useDesign('address-list');
  provide('prefixCls', prefixCls);

  // Định nghĩa một biến ref cho component con
  const leftTree = ref();

  // Phòng ban đang chọncode
  const orgCode = ref('');
  const positionInfo = ref({});

  // Tham số và phương thức dùng chung của trang danh sách
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns,
      // Giải thích logic mã: [VUEN-1485]Sau khi vào trang Quản lý hệ thống -- Danh bạ, dòng lệnh của trang web báo lỗi------------
      rowKey: 'id',
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
      canResize: false,
      showTableSetting: false,
      actionColumn:{
        width: 80
      },
      // Xử lý tham số trước khi gửi yêu cầu
      beforeFetch(params) {
        params.orgCode = orgCode.value;
      },
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload }] = tableContext;

  // Kích hoạt sau khi chọn cây bên trái
  function onTreeSelect(data) {
    orgCode.value = data.orgCode;
    reload();
  }

  /**
   * Thanh thao tác
   * 
   * @param record
   */
  function getTableAction(record) {
    return [
      {
        label: 'Gửi tin nhắn',
        onClick: handleSendChat.bind(null, record),
      },
    ];
  }
  
  // Truy vấn thông tin chức danh
  async function queryPositionInfo() {
    const result = await positionList({ pageSize: 99999 });
    if (result) {
      let obj = {};
      result.records.forEach((position) => {
        obj[position['id']] = position['name'];
      });
      positionInfo.value = obj;
    }
  }
  queryPositionInfo();
  
  /**
   * Trò chuyện
   *
   * @param record
   */
  function handleSendChat(record) {
    //LấymessageId
    let cacheByDynKey = getCacheByDynKey(JEECG_CHAT_UID);
    let iframes:any = document.getElementById("jChatOnline");
    let id = record.id;
    //update-begin---wangshuai---date:20260616  for：[JHHB-1436]【Danh bạ phía PC】Nhấn trực tiếp vào gửi tin nhắn trong danh sách danh bạ không có phản hồi, phải nhấn vào biểu tượng danh bạ trước mới nhấn gửi tin nhắn trong danh sách được------------
    const msgData = {
      type: "open-chat",
      messageId: cacheByDynKey,
      data: { id: id, type: "friend", groupName: "", avatar: record.avatar, username: record.realname }
    };
    // .btn Tồn tại nghĩa là cửa sổ trò chuyện chưa mở (v-if="showChatIcon", trước tiên kích hoạt mở rồi chờ iframe khởi tạo
    const chatBtn = document.querySelector<HTMLElement>('.j-chat-online .btn');
    if (chatBtn) {
      chatBtn.click();
      const onReady = (event: MessageEvent) => {
        //Sau khi mở, phía trò chuyện sẽ gửi get-system-unread-num hoặc get-dialog-height, thỏa mãn một trong hai nghĩa là đã mở, rồi mới gửi yêu cầu mở cửa sổ trò chuyện
        if (event.data?.type === 'get-system-unread-num' || event.data?.type === 'get-dialog-height') {
          window.removeEventListener('message', onReady);
          iframes.contentWindow.postMessage(msgData, '*');
        }
      };
      //Theo dõi tin nhắn
      window.addEventListener('message', onReady);
    } else {
      iframes.contentWindow.postMessage(msgData, '*');
    }
    //update-end---author:wangshuai ---date:20260616  for：[JHHB-1436]【Danh bạ phía PC】Nhấn trực tiếp vào gửi tin nhắn trong danh sách danh bạ không có phản hồi, phải nhấn vào biểu tượng danh bạ trước mới nhấn gửi tin nhắn trong danh sách được------------
  }
</script>

<style lang="less">
  @import './index.less';
</style>
