<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable @register="registerTable">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-upload name="file" :showUploadList="false" :action="ossAction" :headers="tokenHeader" :beforeUpload="beforeUpload" @change="handleChange">
          <a-button type="primary" preIcon="ant-design:upload-outlined">OSSTải tệp lên</a-button>
        </a-upload>
        <a-upload
          name="file"
          :showUploadList="false"
          :action="minioAction"
          :headers="tokenHeader"
          :beforeUpload="beforeUpload"
          @change="handleChange"
        >
          <a-button type="primary" preIcon="ant-design:upload-outlined">MINIOTải tệp lên</a-button>
        </a-upload>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="system-ossfile" setup>
  //tsCú pháp
  import { ref, computed, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './ossfile.data';
  import { list, deleteFile, getOssUrl, getMinioUrl } from './ossfile.api';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';
  import {encryptByBase64} from "@/utils/cipher";
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';

  const { createMessage } = useMessage();
  const glob = useGlobSetting();
  const tokenHeader = { 'X-Access-Token': getToken() };
  //Đăng ký dữ liệu table
  const [registerTable, { reload }] = useTable({
    api: list,
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    tableSetting: { fullScreen: true },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
    actionColumn: {
      width: 80,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  /**
   * Tải lênurl
   */
  const ossAction = computed(() => `${glob.uploadUrl}${getOssUrl}`);
  const minioAction = computed(() => `${glob.uploadUrl}${getMinioUrl}`);

  /**
   * Xem trước
   */
  function handleView(record) {
    if (record && record.url) {
      console.log('glob.onlineUrl', glob.viewUrl);
      // let filePath = encodeURIComponent(record.url);
      //update-begin-author:scott---date:2026-04-16--for: 【Github #8855】Sửa lỗi xử lý đường dẫn xem trước tệp, url cần nối thành URL đầy đủ trước rồi mới mã hóa
      let url = encodeURIComponent(encryptByBase64(getFileAccessHttpUrl(record.url)));
      //update-end-author:scott---date:2026-04-16--for: 【Github #8855】Sửa lỗi xử lý đường dẫn xem trước tệp, url cần nối thành URL đầy đủ trước rồi mới mã hóa
      // //Tài liệu dùng chế độ nâng cao xem trước PDF
      // if(filePath.endsWith(".pdf") || filePath.endsWith(".doc") || filePath.endsWith(".docx")){
      //   filePath = filePath
      // }
      let previewUrl = `${glob.viewUrl}?url=` + url;
      
      window.open(previewUrl, '_blank');
    }
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteFile({ id: record.id }, reload);
  }

  /**
   * Sự kiện trước khi tải lên
   */
  function beforeUpload(file) {
    var fileType = file.type;
    if (fileType === 'image') {
      if (fileType.indexOf('image') < 0) {
        createMessage.warning('Vui lòng tải lên hình ảnh');
        return false;
      }
    } else if (fileType === 'file') {
      if (fileType.indexOf('image') >= 0) {
        createMessage.warning('Vui lòng tải lên tệp');
        return false;
      }
    }
    return true;
  }

  /**
   * Sự kiện tải tệp lên
   */
  function handleChange(info) {
    if (info.file.status === 'done') {
      if (info.file.response.success) {
        reload();
        createMessage.success(`${info.file.name} Tải lên thành công!`);
      } else {
        createMessage.error(`${info.file.response.message}`);
      }
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.response.message}`);
    }
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Xem trước',
        onClick: handleView.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
