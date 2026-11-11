<template>
  <PageWrapper contentFullHeight>
    <a-card :bordered="false" title="Quản lý phiên bản">
      <!--Chế độ chỉnh sửa-->
      <a-spin v-if="active" :spinning="confirmLoading">
        <a-form ref="formRef" :model="model" :labelCol="labelCol" :wrapperCol="wrapperCol" :rules="validatorRules">
          <a-row>
            <a-col :span="24">
              <a-form-item label="Phiên bản" name="appVersion">
                <a-input v-model:value="model.appVersion" placeholder="Vui lòng nhập phiên bản" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="APPCài đặtapk" name="downloadUrl">
                <a-input placeholder="Đặt cài đặt APPapk" v-model:value="model.downloadUrl">
                  <template #addonAfter>
                    <Icon icon="ant-design:upload-outlined" style="cursor: pointer" @click="showUploadModal('apk')" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="APPTệp cập nhật nóng (hot update)" name="wgtUrl">
                <a-input placeholder="Đặt tệp cập nhật nóng của APP" v-model:value="model.wgtUrl">
                  <template #addonAfter>
                    <Icon icon="ant-design:upload-outlined" style="cursor: pointer" @click="showUploadModal('wgt')" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Ứng dụng desktop" name="webDownloadUrl">
                <a-input placeholder="Đường dẫn cài đặt ứng dụng desktop" v-model:value="model.webDownloadUrl">
                  <template #addonAfter>
                    <Icon icon="ant-design:upload-outlined" style="cursor: pointer" @click="showUploadModal('web')" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Nội dung cập nhật">
                <a-textarea :rows="4" v-model:value="model.updateNote" placeholder="Vui lòng nhập nội dung cập nhật" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
        <JUploadModal :value="modalValue" :bizPath="filePath" :maxCount="1" @register="registerModel" @change="uploadBack" />
      </a-spin>
      <!--Chế độ chi tiết-->
      <Description v-else class="desc" :column="1" :data="model" :schema="schema" />
      <!--Nút ở cuối-->
      <div class="anty-form-btn" v-if="hasPermission('app:edit:version')">
        <a-button v-if="active" @click="handleSubmit" type="primary" preIcon="ant-design:save-outlined">Lưu</a-button>
        <a-button v-else @click="active = true" type="primary" preIcon="ant-design:edit-outlined">Bật chế độ chỉnh sửa</a-button>
      </div>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup name="portalapp-sysAppVersion">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { usePermission } from '@/hooks/web/usePermission';
  import { JUploadModal } from '@/components/Form/src/jeecg/components/JUpload';
  import { useModal } from '@/components/Modal';
  import { reactive, ref, toRaw, unref, onMounted } from 'vue';
  import { PageWrapper } from '@/components/Page';
  import { queryAppVersion, saveAppVersion } from './appVersion.api';
  import { Description, DescItem } from '/@/components/Description/index';

  const { hasPermission } = usePermission();
  const { createMessage } = useMessage();

  const [registerModel, { openModal }] = useModal();
  const confirmLoading = ref(false);
  const active = ref(false);
  const formRef = ref<any>(null);
  const appKey = 'E0CC280';
  const filePath = 'appVersion';
  const uploadType = ref('');
  const modalValue = ref('');
  const labelCol = {
    xs: { span: 24 },
    sm: { span: 5 },
  };
  const wrapperCol = {
    xs: { span: 24 },
    sm: { span: 16 },
  };
  const model = reactive({
    id: 'E0CC280',
    appVersion: '',
    versionNum: 0,
    updateNote: '',
    downloadUrl: '',
    wgtUrl: '',
    webDownloadUrl: '',
  });

  /**
   * Khởi tạo dữ liệu biểu mẫu
   * @param record
   */
  async function initFormData() {
    const appVersion = await queryAppVersion({ key: appKey });
    if (appVersion) {
      Object.assign(model, appVersion);
    }
  }

  /**
   * Gửi và lưu thông tin phiên bản
   */
  function handleSubmit() {
    const form = unref(formRef);
    form.validate().then(async () => {
      let obj = toRaw(model);
      if (obj.appVersion.indexOf('.') != -1) {
        obj.versionNum = Number(obj.appVersion.replace(/\./g, ''));
      }
      obj.id = appKey;
      confirmLoading.value = true;
      await saveAppVersion(obj);
      createMessage.success('Lưu thành công');
      confirmLoading.value = false;
      active.value = false;
    }).finally(() => {
      confirmLoading.value = false;
    });
  }

  /**
   * Hộp thoại cài đặt hiển thị
   * @param type
   */
  function showUploadModal(type) {
    uploadType.value = type;
    modalValue.value = type == 'web'? model.webDownloadUrl :type == 'apk' ? model.downloadUrl : model.wgtUrl;
    openModal(true, {
      maxCount: 1,
      bizPath: filePath,
    });
  }

  /**
   *Kết quả trả về khi tải lên
   */
  function uploadBack(value) {
    if (unref(uploadType) == 'apk') {
      model.downloadUrl = value;
    } else if (unref(uploadType) == 'wgt') {
      model.wgtUrl = value;
    }else{
      model.webDownloadUrl = value;
    }
  }
  //Quy tắc kiểm tra biểu mẫu
  const validatorRules = {
    appVersion: [{ required: true, message: 'Phiên bản không được để trống', trigger: 'blur' }],
    downloadUrl: [{ required: true, message: 'APPCài đặt apk không được để trống', trigger: 'change' }],
    wgtUrl: [{ required: false, message: 'APPTệp cập nhật nóng không được để trống', trigger: 'change' }],
  };
  // Trường hiển thị
  const schema: DescItem[] = [
    {
      field: 'appVersion',
      label: 'Phiên bản',
    },
    {
      field: 'downloadUrl',
      label: 'APPCài đặtapk',
    },
    {
      field: 'wgtUrl',
      label: 'APPTệp cập nhật nóng (hot update)',
    },
    {
      field: 'webDownloadUrl',
      label: 'Địa chỉ tải xuống ứng dụng desktop',
    },
    {
      field: 'updateNote',
      label: 'Nội dung cập nhật',
    },
  ];

  onMounted(() => {
    initFormData();
  });
</script>

<style scoped>
  .anty-form-btn {
    width: 100%;
    text-align: center;
  }
  .anty-form-btn button {
    margin: 20px;
  }
  .approveDiv span {
    margin: 0 20px;
  }
  .desc {
    width: 80%;
    margin: 0 auto;
  }

  :deep(.ant-descriptions-item-label) {
    width: 30% !important;
    min-width: 150px !important;
  }
  :deep(.ant-descriptions-item-content) {
    padding: 16px !important;
    width: 60% !important;
  }
</style>
