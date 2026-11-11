<template>
  <div>
    <template v-if="hasFile" v-for="(file, fileKey) of [innerFile || {}]" :key="fileKey">
      <div class="j-vxe-image-list">
        <template v-if="!file || !(file['url'] || file['path'] || file['message'])">
          <a-tooltip :title="'Vui lòng chờ: ' + JSON.stringify(file) + (file['url'] || file['path'] || file['message'])">
            <LoadingOutlined />
          </a-tooltip>
        </template>
        <template v-else-if="file['path']">
          <template v-for="src of imgList">
            <img class="j-vxe-image" :src="src" alt="Lỗi hình ảnh" @[clickEvent]="handleMoreOperation" @click="handlePreview" />
          </template>
        </template>
        <a-tooltip v-else :title="file.message || 'Tải lên thất bại'" @[clickEvent]="handleClickShowImageError">
          <Icon icon="ant-design:exclamation-circle" style="color: red" />
        </a-tooltip>
      </div>
    </template>
    <div class="j-vxe-image-upload" v-if="!cellProps.disabledTable">
      <a-upload
        :accept="acceptFileType"
        name="file"
        :data="uploadData"
        :multiple="false"
        :action="uploadAction"
        :headers="uploadHeaders"
        :showUploadList="false"
        v-bind="cellProps"
        @change="handleChangeUpload"
      >
        <a-button v-if="!hasFile" class="j-vxe-upload-trigger" size="small" preIcon="ant-design:upload">{{ originColumn.btnText || 'Hình ảnh' }}</a-button>
        <div v-if="hasFile && imgList.length < maxCount" class="j-vxe-plus" @click="">
          <Icon icon="ant-design:plus" />
        </div>
      </a-upload>
    </div>
    <JUploadModal :value="modalValue" @register="registerModel" @change="onModalChange" />
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent ,unref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { components, enhanced, useFileCell } from '../hooks/useFileCell';
  import { createImgPreview } from '/@/components/Preview/index';

  export default defineComponent({
    name: 'JVxeImageCell',
    components: components,
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      
      const { createErrorModal } = useMessage();
      const setup = useFileCell(props, UploadTypeEnum.image, { multiple: true });
      // Giải thích logic mã: 【TV360X-470]jVxetable giới hạn loại của component tải lên hình ảnh
      const acceptFileType = 'image/*';
      // Giải thích logic mã: 【issues/953] bảng con onlinevxe-tableHình ảnh tải lên trong chi tiết dạng hiển thị có thể nhấp được
      const clickEvent = computed(() => {
        return unref(setup.cellProps).disabled ? null : 'click';
      });
      const { innerFile, maxCount } = setup;

      const imgList = computed(() => {
        if (innerFile.value) {
          if (innerFile.value['url']) {
            return [innerFile.value['url']];
          } else if (innerFile.value['path']) {
            let paths = innerFile.value['path'].split(',');
            return paths.map((p) => getFileAccessHttpUrl(p));
          }
        }
        return [];
      });

      // Hiển thị chi tiết lỗi tải lên
      function handleClickShowImageError() {
        let file = innerFile.value || null;
        if (file && file['message']) {
          createErrorModal({
            title: 'Tải lên bị lỗi',
            content: 'Thông tin lỗi:' + file['message'],
            maskClosable: true,
          });
        }
      }
      // Giải thích logic mã: 【TV360X-121] Trạng thái vô hiệu hóa của component tệp jvxetable(Chi tiết)có thể tải xuống
      const handlePreview = () => {
        if (unref(setup.cellProps).disabled) {
          createImgPreview({ imageList: imgList.value });
        }
      };
      return {
        ...setup,
        imgList,
        maxCount,
        handleClickShowImageError,
        clickEvent,
        handlePreview,
        acceptFileType,
      };
    },
    // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
    enhanced: enhanced,
  });
</script>

<style scoped lang="less">
  .j-vxe-image {
    height: 32px;
    max-width: 100px !important;
    cursor: pointer;
    display: inline-block;
    margin-right: 4px;
    vertical-align: top;

    &:hover {
      opacity: 0.8;
    }

    &:active {
      opacity: 0.6;
    }
  }

  .j-vxe-plus {
    width: 32px;
    height: 32px;
    line-height: 32px;
    text-align: center;
    background-color: #fafafa;
    border: 1px dashed #d9d9d9;
    border-radius: 2px;
    cursor: pointer;
  }

  .j-vxe-image-list,
  .j-vxe-image-upload {
    display: inline-block;
    vertical-align: top;
  }
</style>
