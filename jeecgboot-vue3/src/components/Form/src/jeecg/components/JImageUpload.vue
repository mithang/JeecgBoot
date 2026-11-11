<template>
  <div class="clearfix">
    <a-upload
      :listType="listType"
      accept="image/*"
      :multiple="multiple"
      :action="uploadUrl"
      :headers="headers"
      :data="{ biz: bizPath }"
      v-model:fileList="uploadFileList"
      :beforeUpload="beforeUpload"
      :disabled="disabled"
      @change="handleChange"
      @preview="handlePreview"
    >
      <div v-if="uploadVisible">
        <div v-if="listType == 'picture-card'">
          <LoadingOutlined v-if="loading" />
          <UploadOutlined v-else />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
        <a-button v-if="listType == 'picture'" :disabled="disabled">
          <UploadOutlined></UploadOutlined>
          {{ text }}
        </a-button>
      </div>
    </a-upload>
    <a-modal :width="previewWidth" :open="previewVisible" :footer="null" @cancel="handleCancel()">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { LoadingOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl, getHeaders, getRandom } from '/@/utils/common/compUtils';
  import { uploadUrl as systemUploadUrl } from '/@/api/common/api';
  import { getToken } from '/@/utils/auth';

  const { createMessage, createErrorModal } = useMessage();
  export default defineComponent({
    name: 'JImageUpload',
    components: { LoadingOutlined, UploadOutlined },
    inheritAttrs: false,
    props: {
      //Giá trị liên kết
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      //Văn bản nút
      listType: {
        type: String,
        required: false,
        default: 'picture-card',
      },
      //Văn bản nút
      text: {
        type: String,
        required: false,
        default: 'Tải lên',
      },
      //Thuộc tính này dùng để điều khiển đường dẫn nghiệp vụ tải lên tệp
      bizPath: {
        type: String,
        required: false,
        default: 'temp',
      },
      //Có vô hiệu hóa hay không
      disabled: {
        type: Boolean,
        required: false,
        default: false,
      },
      //Số lượng tải lên
      fileMax: {
        type: Number,
        required: false,
        default: 1,
      },
      uploadUrl: {
        type: String,
        default: systemUploadUrl,
      },
      previewWidth: {
        type: Number,
        required: false,
        default: 520,
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //Lấy tên tệp
      const getFileName = (path) => {
        if (path.lastIndexOf('\\') >= 0) {
          let reg = new RegExp('\\\\', 'g');
          path = path.replace(reg, '/');
        }
        return path.substring(path.lastIndexOf('/') + 1);
      };
      //token
      const headers = getHeaders();
      //Trạng thái tải lên
      const loading = ref<boolean>(false);
      //Có phải tải khởi tạo không
      const initTag = ref<boolean>(true);
      //Danh sách tệp
      let uploadFileList = ref<any[]>([]);
      //Ảnh xem trước
      const previewImage = ref<string | undefined>('');
      //Trạng thái khung xem trước
      const previewVisible = ref<boolean>(false);

      //Tính xem có bật tải lên nhiều hình ảnh không
      const multiple = computed(() => {
        return props['fileMax'] > 1 || props['fileMax'] === 0;
      });

      //Tính xem có thể tiếp tục tải lên không
      const uploadVisible = computed(() => {
        if (props['fileMax'] === 0) {
          return true;
        }
        return uploadFileList.value.length < props['fileMax'];
      });

      /**
       * Lắng nghe thay đổi của value
       */
      watch(
        () => props.value,
        (val, prevCount) => {
         // Giải thích logic mã: 【issues/556]Component JImageUpload gán giá trị ban đầu cho value nhưng không hiển thị hình ảnh------------
            if (val && val instanceof Array) {
            val = val.join(',');
          }
          if (initTag.value == true) {
            initFileList(val);
          }
        },
        { immediate: true }
      );

      /**
       * Khởi tạo danh sách tệp
       */
      function initFileList(paths) {
        if (!paths || paths.length == 0) {
          uploadFileList.value = [];
          return;
        }
        let files = [];
        let arr = paths.split(',');
        arr.forEach((value) => {
          let url = getFileAccessHttpUrl(value);
          files.push({
            uid: getRandom(10),
            name: getFileName(value),
            status: 'done',
            url: url,
            response: {
              status: 'history',
              message: value,
            },
          });
        });
        uploadFileList.value = files;
      }

      /**
       * Kiểm chứng trước khi tải lên
       */
      function beforeUpload(file) {
        let fileType = file.type;
        if (fileType.indexOf('image') < 0) {
          createMessage.info('Vui lòng tải lên hình ảnh');
          return false;
        }
      }
      /**
       * Callback kết quả tải tệp lên
       */
      function handleChange({ file, fileList, event }) {
        initTag.value = false;
        // uploadFileList.value = fileList;
        if (file.status === 'error') {
          createMessage.error(`${file.name} Tải lên thất bại.`);
        }
        // Giải thích logic mã: 【TV360X-1640] Tối ưu hiển thị khi kích thước hình ảnh tải lên vượt giới hạn
        if (file.status === 'done' && file.response.success === false) {
          const failIndex = uploadFileList.value.findIndex((item) => item.uid === file.uid);
          if (failIndex != -1) {
            uploadFileList.value.splice(failIndex, 1);
          }
          createMessage.warning(file.response.message);
          return;
        }
        let fileUrls = [];
        let noUploadingFileCount = 0;
        if (file.status != 'uploading') {
          fileList.forEach((file) => {
            if (file.status === 'done') {
              fileUrls.push(file.response.message);
            }
            if (file.status != 'uploading') {
              noUploadingFileCount++;
            }
          });
          if (file.status === 'removed') {
            handleDelete(file);
          }
          if (noUploadingFileCount == fileList.length) {
            state.value = fileUrls.join(',');
            emit('update:value', fileUrls.join(','));
            // Giải thích logic mã: [issues/248]Sử dụng component hình ảnh trong biểu mẫu gốc,Đóng popup thì giá trị của component hình ảnh không bị xóa------------
            nextTick(() => {
              initTag.value = true;
            });
          }
        }
      }

      /**
       * Xóa hình ảnh
       */
      function handleDelete(file) {
        //Nếu cần thêm mới, logic xóa
        console.log(file);
      }

      /**
       * Xem trước hình ảnh
       */
      function handlePreview(file) {
        previewImage.value = file.url || file.thumbUrl;
        previewVisible.value = true;
      }

      function getAvatarView() {
        if (uploadFileList.length > 0) {
          let url = uploadFileList[0].url;
          return getFileAccessHttpUrl(url, null);
        }
      }

      function handleCancel() {
        previewVisible.value = false;
      }

      return {
        state,
        attrs,
        previewImage,
        previewVisible,
        uploadFileList,
        multiple,
        headers,
        loading,
        beforeUpload,
        uploadVisible,
        handlePreview,
        handleCancel,
        handleChange,
      };
    },
  });
</script>
<style scoped>
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }
</style>
