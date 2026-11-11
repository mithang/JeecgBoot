<template>
  <div>
    <a-upload
      :headers="headers"
      :action="uploadAction"
      :showUploadList="false"
      :multiple="false"
      :maxCount="1"
      :data="uploadData"
      accept=".xls,.xlsx"
      @change="onFileChange"
      :beforeUpload="beforeUpload"
      ref="uploadRef"
      style="width: 100%"
    >
      <a-button type="primary" preIcon="ant-design:import-outlined" v-if="percent === 0 || percent === 100">{{ btnText }}</a-button>
      <a-button @click.prevent.stop="progressOpen = true" type="primary" v-else>
        <LoadingOutlined />
        Đang nhập
      </a-button>
    </a-upload>
    <a-modal v-model:open="progressOpen" title="Tải tệp lên" :body-style="{ padding: '20px' }" :destroyOnClose="true" :footer="null">
      <div>
        <div style="display: flex">
          <div class="label">Tệp đang tải lên:</div>
          <div class="file-name">{{ fileName }}</div>
        </div>
        <div style="margin-top: 10px; display: flex">
          <div class="label">Tiến độ tải lên của tệp hiện tại:</div>
          <a-progress :percent="percent" style="width: 300px; margin-left: 10px" :format="(percent) => `${percent}%`"></a-progress>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onUnmounted, ref, computed, watch } from 'vue';
  import { getTenantId, getToken } from '@/utils/auth';
  import { randomString } from '@/utils/common/compUtils';
  import { useMessage } from '@/hooks/web/useMessage';
  import { useGlobSetting } from '@/hooks/setting';
  import { defHttp } from '@/utils/http/axios';
  import { LoadingOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'ImportExcelProgress',
    components: { LoadingOutlined },
    props: {
      //Đường dẫn tải lên
      uploadUrl: {
        type: String,
        required: true,
      },
      //Chữ trên nút
      btnText: {
        type: String,
        default: 'Nhập (import)',
      },
      //Loại tải lên
      type: {
        type: String,
        default: 'user',
      },
    },
    emit: ['success', 'register'],
    setup(props, { emit }) {
      const fileName = ref<string>('');
      //Tải lên dữ liệu
      const uploadData = ref({});
      //Header của yêu cầu tải lên
      const headers = ref({});
      const { createMessage, createWarningModal } = useMessage();
      //Tác vụ hẹn giờ tải tệp lên
      const fileUploadInterval = ref<any>();
      const glob = useGlobSetting();
      const progressOpen = ref(false);
      //Tiến độ tải lên
      const percent = ref<number>(0);
      const uploadRef = ref();
      //Số ngẫu nhiên
      const uuid = ref();
      const baseApiUrl = glob.domainUrl;
      const uploadAction = computed(() => {
        return baseApiUrl + props.uploadUrl;
      });
      //Callback khi tệp thay đổi
      function onFileChange(info) {
        if (info.file.name) {
          fileName.value = info.file.name;
        }
        if (info.file.status === 'done') {
          if (info.file.response.success) {
            if (info.file.response && info.file.response.code === 201) {
              let {
                message,
                result: { msg, fileUrl, fileName },
              } = info.file.response;
              let href = glob.uploadUrl + fileUrl;
              createWarningModal({
                title: message,
                centered: false,
                content: `<div>
                        <span>${msg}</span><br/> 
                        <span>Chi tiết cụ thể vui lòng<a href = ${href} download = ${fileName}> Nhấn tải xuống </a> </span> 
                      </div>`,
              });
              percent.value = 100;
              emit('success');
            } else {
              createMessage.success('Nhập tệp thành công!');
              percent.value = 100;
              emit('success');
            }
          } else {
            createMessage.error(info.file.response.message || `${info.file.name} Nhập thất bại.`);
          }
          clear();
        } else if (info.file.status === 'error' || (info.file.response && info.file.response.code === 500)) {
          clear();
          createMessage.error(info.file.response?.message || `Nhập thất bại, nguyên nhân có thể là tệp vượt quá dung lượng tải lên, vui lòng xem console hoặc nhật ký phía máy chủ`);
        }
      }

      /**
       * Xóa bộ hẹn giờ
       */
      function clear() {
        //Trạng thái xem: đổi mục đã nhập thành nhập thất bại
        clearInterval(fileUploadInterval.value);
      }

      /**
       * Callback trước khi tải lên
       */
      function beforeUpload() {
        percent.value = 1;
        headers.value = {
          'X-Access-Token': getToken(),
          'X-Tenant-Id': getTenantId() ? getTenantId() : '0',
        };
        uuid.value = randomString(16);
        uploadData.value['fileKey'] = uuid.value;
        progressOpen.value = true;
        fileUploadInterval.value = setInterval(() => {
          defHttp
            .get({ url: '/sys/user/getUploadFileProgress', params: { fileKey: uuid.value, type: props.type } }, { isTransformResponse: false })
            .then((res) => {
              if (res.success) {
                if (res.result) {
                  percent.value = Math.round(res.result);
                  if (res.result == 100) {
                    clear();
                  }
                }
              }
            })
            .catch(() => {
              clear();
            });
        }, 2000);
      }

      onUnmounted(() => {
        clear();
      });

      return {
        fileName,
        uploadData,
        headers,
        onFileChange,
        beforeUpload,
        progressOpen,
        percent,
        uploadAction,
        uploadRef,
      };
    },
  });
</script>

<style scoped lang="less">
  .label {
    font-size: 14px;
    color: #666;
  }
  .file-name {
    color: #151515;
    font-size: 12px;
    margin-top: 2px;
  }
</style>
