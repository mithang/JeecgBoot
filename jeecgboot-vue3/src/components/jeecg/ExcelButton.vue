<template>
  <a-button type="primary" v-if="hasExportAuth() && config.export" preIcon="ant-design:export-outlined" @click="onExportXls()"> Xuất</a-button>
  <a-upload name="file" :showUploadList="false" v-if="hasImportAuth() && config.import" :customRequest="(file) => onImportXls(file)">
    <a-button type="primary" preIcon="ant-design:import-outlined">Import</a-button>
  </a-upload>
</template>

<script lang="ts" setup name="ExcelButton">
  import { PropType } from 'vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { useMessage } from '/@/hooks/web/useMessage';

  // Định nghĩa tham số cần thiết cho phương thức excel
  interface ExcelConfig {
    // Cấu hình xuất
    exportConfig: {
      url: string;
      // Tên tệp xuất
      name?: string | (() => string);
      //Quyền hạn nút
      auth?: string | string[];
    };
    // Cấu hình import
    importConfig: {
      url: string;
      // Callback sau khi xuất thành công
      success?: (fileInfo?: any) => void;
      //Quyền hạn nút
      auth?: string | string[];
    };
  }
  /**
   * Định nghĩa tham số component
   */
  const props = defineProps({
    config: {
      type: Object as PropType<ExcelConfig>,
      default: null,
    },
  });
  //Vấn đề quyền nút
  const { hasPermission } = usePermission();
  //Phương thức nhập/xuất
  const { handleExportXls, handleImportXls } = useMethods();

  const $message = useMessage();
  // Xuất excel
  function onExportXls() {
    let { url, name } = props.config?.export ?? {};
    if (url) {
      let title = typeof name === 'function' ? name() : name;
      return handleExportXls(title as string, url);
    } else {
      $message.createMessage.warn('Chưa truyền export.url Tham số');
      return Promise.reject();
    }
  }

  // Import excel
  function onImportXls(file) {
    let { url, success } = props.config?.import ?? {};
    if (url) {
      return handleImportXls(file, url, success);
    } else {
      $message.createMessage.warn('Chưa truyền import.url Tham số');
      return Promise.reject();
    }
  }

  // Quyền nút nhập
  function hasImportAuth() {
    let auth = props.config?.import?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }

  // Quyền nút xuất
  function hasExportAuth() {
    let auth = props.config?.export?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }
</script>

<style scoped lang="less"></style>
