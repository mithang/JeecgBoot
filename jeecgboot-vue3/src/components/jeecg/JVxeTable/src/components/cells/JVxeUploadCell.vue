<template>
  <div>
    <template v-if="hasFile" v-for="(file, fileKey) of [innerFile || {}]" :key="fileKey">
      <a-input :readOnly="true" :value="file.name">
        <template #addonBefore style="width: 30px">
          <a-tooltip v-if="file.status === 'uploading'" :title="`Đang tải lên(${Math.floor(file.percent)}%)`">
            <LoadingOutlined />
          </a-tooltip>
          <a-tooltip v-else-if="file.status === 'done'" title="Tải lên hoàn tất">
            <Icon icon="ant-design:check-circle-outlined" style="color: #00db00" />
          </a-tooltip>
          <a-tooltip v-else :title="file.message || 'Tải lên thất bại'">
            <Icon icon="ant-design:exclamation-circle-outlined" style="color: red" />
          </a-tooltip>
        </template>
        <span v-if="file.status === 'uploading'" slot="addonAfter">{{ Math.floor(file.percent) }}%</span>
        <template v-if="originColumn.allowDownload !== false || originColumn.allowRemove !== false" #addonAfter>
          <Dropdown :trigger="['click']" placement="bottomRight">
            <a-tooltip title="Thao tác">
              <Icon icon="ant-design:setting-outlined" style="cursor: pointer" />
            </a-tooltip>
            <template #overlay>
              <a-menu>
                <a-menu-item v-if="originColumn.allowDownload !== false" @click="handleClickDownloadFile">
                  <span><Icon icon="ant-design:download-outlined" />&nbsp;Tải xuống</span>
                </a-menu-item>
                <a-menu-item v-if="originColumn.allowRemove !== false" @click="handleClickDeleteFile">
                  <span><Icon icon="ant-design:delete-outlined" />&nbsp;Xóa</span>
                </a-menu-item>
              </a-menu>
            </template>
          </Dropdown>
        </template>
      </a-input>
    </template>
    <a-upload
      v-if="!cellProps.disabledTable"
      v-show="!hasFile"
      name="file"
      :data="uploadData"
      :multiple="false"
      :action="originColumn.action"
      :headers="uploadHeaders"
      :showUploadList="false"
      v-bind="cellProps"
      @change="handleChangeUpload"
    >
      <a-button preIcon="ant-design:upload-outlined">{{ originColumn.btnText || 'Nhấn để tải lên' }}</a-button>
    </a-upload>
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { Dropdown } from 'ant-design-vue';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { useJVxeUploadCell, fileGetValue, fileSetValue } from '../../hooks/cells/useJVxeUploadCell';

  export default defineComponent({
    name: 'JVxeUploadCell',
    components: { Icon, Dropdown, LoadingOutlined },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const setup = useJVxeUploadCell(props);
      return { ...setup };
    },
    // [Tăng cường component] Xem chú thích chi tiết tại::JVxeComponent.Enhanced
    enhanced: {
      switches: { visible: true },
      getValue: (value) => fileGetValue(value),
      setValue: (value) => fileSetValue(value),
    } as JVxeComponent.EnhancedPartial,
  });
</script>
