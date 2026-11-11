<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :width="800" title="Xem chi tiết" :showCancelBtn="false" :showOkBtn="false" :maxHeight="500">
    <template #title>
      <span class="basic-title">Xem chi tiết</span>
      <div class="print-btn" @click="onPrinter">
        <Icon icon="ant-design:printer-filled" />
        <span class="print-text">In</span>
      </div>
    </template>
    <iframe ref="iframeRef" :src="frameSrc" class="detail-iframe" @load="onIframeLoad"></iframe>
    <template v-if="noticeFiles && noticeFiles.length > 0">
      <div class="files-title">Tệp đính kèm liên quan:</div>
      <template v-for="(file, index) in noticeFiles" :key="index">
        <div class="files-area">
          <div class="files-area-text">
            <span>
              <paper-clip-outlined />
              <a
                target="_blank"
                rel="noopener noreferrer"
                :title="file.fileName"
                :href="getFileAccessHttpUrl(file.filePath)"
                class="ant-upload-list-item-name"
                >{{ file.fileName }}</a
              >
            </span>
          </div>
          <div class="files-area-operate">
            <download-outlined class="item-icon" @click="handleDownloadFile(file.filePath)" />
            <eye-outlined class="item-icon" @click="handleViewFile(file.filePath)" />
          </div>
        </div>
      </template>
      <a v-if="noticeFiles.length > 1" :href="downLoadFiles + '?id=' + noticeId + '&token=' + getToken()" target="_blank"  style="margin: 15px 6px;color: #5ac0fa;">
        <download-outlined class="item-icon" style="margin-right: 5px"  /><span>Tải xuống hàng loạt mọi tệp đính kèm</span>
      </a>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { ref } from 'vue';
  import { buildUUID } from '@/utils/uuid';
  import {getElectronFileUrl, getFileAccessHttpUrl} from '@/utils/common/compUtils';
  import { DownloadOutlined, EyeOutlined, PaperClipOutlined } from '@ant-design/icons-vue';
  import { encryptByBase64 } from '@/utils/cipher';
  import { useGlobSetting } from '@/hooks/setting';
  import { getToken } from "@/utils/auth";
  import {$electron} from "@/electron";
  const glob = useGlobSetting();
  // Lấyprops
  defineProps({
    frameSrc: propTypes.string.def(''),
  });
  /**
   * Đường dẫn tải tệp xuống
   */
  const downLoadFiles = `${glob.domainUrl}/sys/annountCement/downLoadFiles`;

  //Nội dung đính kèm
  const noticeFiles = ref([]);
  //Dữ liệuID
  const noticeId = ref('');
  //Gán giá trị cho biểu mẫu
  const [registerModal] = useModalInner((data) => {
    noticeFiles.value = [];
    noticeId.value = data.record.id;
    if (data.record?.files && data.record?.files.length > 0) {
      noticeFiles.value = data.record.files.split(',').map((item) => {
        return {
          fileName: item.split('/').pop(),
          filePath: item,
        };
      });
    }
  });
  // iframeTham chiếu
  const iframeRef = ref<HTMLIFrameElement>();
  // Lưu trữ phiên in hiện tạiID
  const printSessionId = ref<string>('');
  // iframeSau khi tải xong, khởi tạo giao tiếp
  const onIframeLoad = () => {
    printSessionId.value = buildUUID(); // Mỗi lần tải tạo phiên mớiID
  };
  //In
  function onPrinter() {
    if (!iframeRef.value) return;
    console.log('onPrinter', iframeRef.value);
    iframeRef.value?.contentWindow?.postMessage({ printSessionId: printSessionId.value, type: 'action:print' }, '*');
  }
  /**
   * Tải tệp xuống
   * @param filePath
   */
  function handleDownloadFile(filePath) {
    window.open(getFileAccessHttpUrl(filePath), '_blank');
  }
  /**
   * Xem trước tệp
   * @param filePath
   */
  function handleViewFile(filePath) {
    if (filePath) {
      //update-begin-author:scott---date:2026-04-16--for: 【Github #8855】Sửa lỗi xử lý đường dẫn xem trước tệp, filePath cần nối thành URL đầy đủ trước rồi mới mã hóa
      let url = encodeURIComponent(encryptByBase64(getFileAccessHttpUrl(filePath)));
      //update-end-author:scott---date:2026-04-16--for: 【Github #8855】Sửa lỗi xử lý đường dẫn xem trước tệp, filePath cần nối thành URL đầy đủ trước rồi mới mã hóa
      let previewUrl = `${glob.viewUrl}?url=` + url;
      //update-begin-author:liusq---date:2025-12-16--for: JHHB-1139Phía desktop: thống nhất sửa đổi xem trước tệp 
      if($electron.isElectron()){
        previewUrl = getElectronFileUrl(filePath);
      }
      //update-end-author:liusq---date:2025-12-16--for: JHHB-1139Phía desktop: thống nhất sửa đổi xem trước tệp
      window.open(previewUrl, '_blank');
    }
  }
</script>

<style scoped lang="less">
  .print-btn {
    position: absolute;
    top: 20px;
    right: 100px;
    cursor: pointer;
    color: #a3a3a5;
    z-index: 999;
    .print-text {
      margin-left: 5px;
    }
    &:hover {
      color: #40a9ff;
    }
  }
  .detail-iframe {
    border: 0;
    width: 100%;
    height: 100%;
    min-height: 500px;
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1685】Khi xem thông báo xuất hiện hai thanh cuộn
    display: block;
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1685】Khi xem thông báo xuất hiện hai thanh cuộn
  }
  .files-title {
    font-size: 16px;
    margin: 10px;
    font-weight: 600;
    color: #333;
  }
  .files-area {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    margin: 6px;
    &:hover {
      background-color: #f5f5f5;
    }
    .files-area-text {
      display: flex;
      .ant-upload-list-item-name {
        margin: 0 6px;
        color: #56befa;
      }
    }
    .files-area-operate {
      display: flex;
      margin-left: 10px;
      .item-icon {
        cursor: pointer;
        margin: 0 6px;
        &:hover {
          color: #56befa;
        }
      }
    }
  }
</style>
