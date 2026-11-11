<template>
  <BasicModal @register="registerModal" :width="1200" :defaultFullscreen="false" :canFullscreen="false">
    <template #title> <info-circle-two-tone /> Kết quả sinh mã </template>
    <div :style="divStyle">
      <p>
        <template v-for="item in codeList"> {{ item }}<br /> </template>
      </p>
    </div>
    <template #footer>
      <a-button @click="handleClose">Đóng</a-button>
      <a-button type="primary" ghost @click="handleView">Xem trước trực tuyến</a-button>
      <a-button type="primary" @click="onDownloadGenerateCode" :loading="loading">Tải xuống máy cục bộ</a-button>
    </template>
  </BasicModal>
  <code-file-view-modal @register="registerCodeViewModal" @download="onDownloadGenerateCode" @close="handleClose"></code-file-view-modal>
</template>

<script>
  /**
   * onlinebật ra sau khi sinh mãmodal Danh sách tệp
   */
  import { ref, reactive, computed, nextTick, defineComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { InfoCircleTwoTone } from '@ant-design/icons-vue';
  import CodeFileViewModal from './CodeFileViewModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  
  export default {
    name: 'CodeFileListModal',
    components: {
      BasicModal,
      InfoCircleTwoTone,
      CodeFileViewModal,
    },
    emits: ['register'],
    setup() {
      const { createMessage: $message } = useMessage();
      const codeList = ref([]);
      const height = window.innerHeight - 150;
      const divStyle = reactive({
        overflowY: 'auto',
        maxHeight: height + 'px',
      });
      const loading = ref(false);
      const tableName = ref('')
      const pathKey = ref('')

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        codeList.value = data.codeList;
        tableName.value = data.tableName;
        pathKey.value = data.pathKey;
      });

      function handleClose() {
        closeModal();
      }
      function onDownloadGenerateCode() {
        //update-begin-author:taoyan date:2022-6-27 for:  VUEN-1433【vue3】Sinh mã một-nhiều, nhấp tải xuống thất bại
        let codeFileList = codeList.value;
        if (!codeFileList || codeFileList.length == 0) {
          $message.warning('Không có mã!');
          return;
        }
        let temp = codeFileList.join(',');
        //console.log(temp);
        //update-end-author:taoyan date:2022-6-27 for:  VUEN-1433【vue3】Sinh mã một-nhiều, nhấp tải xuống thất bại
        return defHttp
          .post(
            {
              url: '/online/cgform/api/downGenerateCode',
              params: {
                fileList: encodeURI(temp),
                pathKey: pathKey.value
              },
              responseType: 'blob',
            },
            { isTransformResponse: false }
          )
          .then((data) => {
            if (!data || data.size == 0) {
              $message.warning('Xuất mã thất bại!');
              return;
            }
            let fileName = 'Dẫn tới mã được tạo ra_' + tableName.value + '_' + new Date().getTime() + '.zip';
            downloadByData(data, fileName, 'application/zip');
          });
      }

      const [registerCodeViewModal, { openModal }] = useModal();
      function handleView() {
        let temp = codeList.value;
        openModal(true, {
          codeList: temp,
          pathKey: pathKey.value
        });
      }

      return {
        registerModal,
        registerCodeViewModal,
        divStyle,
        codeList,
        onDownloadGenerateCode,
        handleClose,
        handleView,
        loading,
      };
    },
  };
</script>

<style scoped></style>
