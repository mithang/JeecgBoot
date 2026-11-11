<template>
  <div :class="[prefixCls, { fullscreen }]">
    <Upload
      name="file"
      multiple
      @change="handleChange"
      :action="uploadUrl"
      :showUploadList="false"
      :data="getBizData()"
      :headers="getheader()"
      :before-upload="beforeUpload"
      accept=".jpg,.jpeg,.gif,.png,.webp"
    >
      <a-button type="primary" v-bind="{ ...getButtonProps }">
        {{ t('component.upload.imgUpload') }}
      </a-button>
    </Upload>
  </div>
</template>
<script lang="ts">
  import { defineComponent, computed, ref } from 'vue';

  import { Upload } from 'ant-design-vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getFileAccessHttpUrl, getHeaders } from '/@/utils/common/compUtils';

  export default defineComponent({
    name: 'TinymceImageUpload',
    components: { Upload },
    props: {
      fullscreen: {
        type: Boolean,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['uploading', 'done', 'error', 'loading'],
    setup(props, { emit }) {
      // Giải thích logic mã: Không hỗ trợ tải lên hình ảnh trong rich text
      function getheader() {
        return getHeaders();
      }

      function getBizData() {
        return {
          biz: 'jeditor',
          jeditor: '1',
        };
      }
      const { domainUrl } = useGlobSetting();
      const uploadUrl = domainUrl + '/sys/common/upload';
      //Danh sách tệp
      let uploadFileList = ref<any[]>([]);
      const { t } = useI18n();
      const { prefixCls } = useDesign('tinymce-img-upload');

      const getButtonProps = computed(() => {
        const { disabled } = props;
        return {
          disabled,
        };
      });

      let uploadLength = 0;
      function handleChange({ file, fileList }) {
        // Lọc bỏ các tệp đã tồn tại
        fileList = fileList.filter((file) => {
          const existFile = uploadFileList.value.find(({ uid }) => uid === file.uid);
          return existFile ? false : true;
        });
        uploadLength == 0 && (uploadLength = fileList.length);
        if (file.status != 'uploading') {
          emit('loading', uploadLength, true);
        }
        // Xử lý tệp đã tải lên
        if (file.status != 'uploading') {
          fileList.forEach((file) => {
            if (file.status === 'done' && file.response.success) {
              const name = file?.name;
              let realUrl = getFileAccessHttpUrl(file.response.message);
              uploadFileList.value.push(file);
              emit('done', name, realUrl);
            }
          });
        }
      }
      //Trước khi tải lên
      function beforeUpload() {
        uploadLength = 0;
        emit('loading', null, true);
        setTimeout(() => {
          emit('loading', null, false);
        }, 10000);
      }
      return {
        prefixCls,
        handleChange,
        uploadUrl,
        getheader,
        getBizData,
        t,
        getButtonProps,
        uploadFileList,
        beforeUpload,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-tinymce-img-upload';

  .@{prefix-cls} {
    background-color: @primary-color;
    margin: 0 3px;
    &.fullscreen {
      position: fixed;
      z-index: 10000;
    }
    // Giải thích logic mã: 【QQYUN-8647]Nút tải ảnh lên của component tinymce online che khuất nút toàn màn hình trên thanh công cụ
    .ant-btn {
      padding: 2px 4px;
      font-size: 12px;
      height: 24px;
      // Giải thích logic mã: 【TV360X-235]Ở trạng thái vô hiệu hóa của rich text, chữ trên nút tải lên hình ảnh nhìn không rõ
      &.is-disabled {
        color: rgba(255, 255, 255, 0.5);
      }
    }
  }
</style>
