<template>
  <BasicModal
    v-bind="modalProps"
    :style="modalStyle"
    @register="registerModal"
    wrapClassName="jeecg-online-modal2"
    @ok="handleSubmit"
  >
    <template #footer>
      <a-button key="submit" type="primary" @click="handleSubmit"> {{modalProps.okText ?? 'Xác nhận'}}</a-button>
      <a-button key="back" @click="handleCancel">{{ modalProps.cancelText ?? 'Đóng' }}</a-button>
    </template>

    <a-spin :spinning="confirmLoading">
      <!-- onlineBiểu mẫu -->
      <online-form
        v-if="isOnlineForm"
        ref="onlineFormCompRef"
        :id="id"
        :form-template="formTemplate"
        @rendered="renderSuccess"
        @success="handleSuccess"
        modalClass="jeecg-online-modal2"
      >
      </online-form>

      <!-- Biểu mẫu tùy chỉnh -->
      <component
        v-else
        ref="customFormRef"
        :url="customFormComponent.url"
        :is="customFormComponent.is"
        :row="customFormComponent.row"
        @close="handleSuccess"
      >
      </component>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, reactive, ref, watch, nextTick, defineAsyncComponent, markRaw } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import { defHttp } from '/@/utils/http/axios';
  import OnlineForm from '../comp/OnlineForm.vue';
  import { importViewsFile } from '/@/utils';
  import { omit } from 'lodash-es';

  /**
   * openTham số của hàm
   */
  interface ModalConfig {
    row: any;
    hide?: string[];
    show?: string[];
    requestUrl?: string;
    tableType?: string;
    foreignKeys?: string;
    formComponent?: string;
    code?: string;
  }

  /**
   * Cấu hình mặc định
   */
  const DEF_CONFIG: ModalConfig = {
    code: '',
    row: {},
    hide: [],
    show: [],
    requestUrl: '',
    tableType: '',
    foreignKeys: '',
    formComponent: '',
  };
  /**
   * Popup tùy chỉnh
   */
  export default defineComponent({
    name: 'OnlineCustomModal',
    components: {
      OnlineForm,
      BasicModal,
    },
    setup(_props, { emit, attrs }) {
      const onlineFormCompRef = ref();
      const modalProps = ref({});
      // onlineCấu hình của biểu mẫuid
      const id = ref('');

      let onlineHideFields = [];
      let onlineShowFields = [];
      let onlineFormEditUrl = '';
      // Dữ liệu đang chỉnh sửa hiện tại
      let currentRowData = {};
      const url = {
        loadFormItems: '/online/cgform/api/getFormItem/',
        optPre: '/online/cgform/api/form/',
      };
      const modalStyle = { position: 'relative' };

      const confirmLoading = ref(false);
      // Biểu mẫu đã render xong hay chưa
      const formRendered = ref(false);
      // Render hoàn tất, thay đổi trạng thái
      function renderSuccess() {
        formRendered.value = true;
      }

      //Có phải làonlineBiểu mẫu
      const isOnlineForm = ref(true);

      // Hiển thị popup Kích hoạtonlineFormCompRef---show
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (params) => {
        setModalProps({ confirmLoading: false });
        resetParams(params);
        await nextTick(async () => {
          if (!params.formComponent) {
            //Không có component được khai báo Đi quaonlineBiểu mẫu
            await showOnlineForm();
          } else {
            showCustomForm(params.formComponent);
          }
        });
      });

      /**
       * Mở popup
       * @param params
       */
      function resetParams(params) {
        // code row formComponent hide show
        let options = Object.assign({}, DEF_CONFIG, params);
        id.value = options.code;
        // update-begin--author:liaozhiyang---date:20250818---for：【issues/8672】jsPopup tăng cường hỗ trợbasicModalcủaprops
        modalProps.value = {
          title: 'Popup tùy chỉnh',
          width: 600,
          ...attrs,
          ...omit(params, ['row', 'formComponent', 'hide', 'show', 'requestUrl']),
        };
        // update-end--author:liaozhiyang---date:20250818---for：【issues/8672】jsPopup tăng cường hỗ trợbasicModalcủaprops
        onlineHideFields = options.hide || [];
        onlineShowFields = options.show || [];
        onlineFormEditUrl = getOnlineFormEditUrl(options.requestUrl);
        currentRowData = options.row;
      }

      /**
       * Lấy địa chỉ chỉnh sửa dữ liệu biểu mẫu-Có hiệu lực với cả hai biểu mẫu
       */
      function getOnlineFormEditUrl(requestUrl) {
        if (requestUrl) {
          return requestUrl;
        } else {
          return url.optPre + id.value;
        }
      }

      const submitLoading = ref(false);
      function handleSubmit() {
        submitLoading.value = true;
        if (isOnlineForm.value === true) {
          onlineFormCompRef.value.handleSubmit();
        } else {
          customFormRef.value.handleSubmit();
        }
        setTimeout(() => {
          submitLoading.value = true;
        }, 3500);
      }

      function handleCancel() {
        closeModal();
      }

      function handleSuccess(formData) {
        emit('success', formData);
        closeModal();
      }

      /*-------------------------------------onlineBiểu mẫu-----------------------------------------------*/

      /**
       * Hiển thịonlineBiểu mẫu
       */
      async function showOnlineForm() {
        isOnlineForm.value = true;
        await getRefPromise(formRendered);
        //Xử lý việc hiển thị/ẩn trường
        onlineFormCompRef.value.handleCustomFormSh(onlineShowFields, onlineHideFields);
        //Hiển thị lại dữ liệu biểu mẫu
        onlineFormCompRef.value.handleCustomFormEdit(currentRowData, onlineFormEditUrl);
      }

      // Phong cách mẫu-Mặc định1cột
      const formTemplate = ref(1);
      // Lắng nghe (watch)idThay đổi Render lại biểu mẫu
      watch(id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!id.value) {
          return;
        }
        console.log('Render lại biểu mẫu 》》》》modal');
        let result: any = await loadFormItems();
        // modalTrang chỉ xử lý nút,JSTăng cường, độ rộng popup
        let dataFormTemplate = result.head.formTemplate;
        formTemplate.value = dataFormTemplate ? Number(dataFormTemplate) : 1;
        nextTick(async () => {
          let myForm = (await getRefPromise(onlineFormCompRef)) as any;
          myForm.createRootProperties(result);
        });
      }

      function loadFormItems() {
        let url = `/online/cgform/api/getFormItem/${id.value}`;
        return new Promise((resolve, reject) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              console.log('Kết quả biểu mẫu 》》modal:', res);
              if (res.success) {
                resolve(res.result);
              } else {
                reject(res.message);
              }
            })
            .catch(() => {
              reject();
            });
        });
      }
      /*-------------------------------------onlineBiểu mẫu-----------------------------------------------*/

      /*-------------------------------------Biểu mẫu tùy chỉnh-----------------------------------------------*/
      const customFormRef = ref();

      const customFormComponent = reactive({
        url: '',
        is: '',
        row: {},
      });

      /**
       * Hiển thị biểu mẫu tùy chỉnh
       */
      function showCustomForm(formComponent) {
        isOnlineForm.value = false;
        customFormComponent.url = onlineFormEditUrl;
        customFormComponent.row = currentRowData;
        customFormComponent.is = markRaw(defineAsyncComponent(() => importViewsFile(formComponent)));
      }
      /*-------------------------------------Biểu mẫu tùy chỉnh-----------------------------------------------*/

      return {
        //modal
        registerModal,
        modalProps,
        modalStyle,
        handleSubmit,
        handleCancel,

        // onlineBiểu mẫu
        id,
        onlineFormCompRef,
        formTemplate,
        renderSuccess,

        //Biểu mẫu tùy chỉnh
        customFormRef,
        customFormComponent,

        //Dùng chung
        open,
        isOnlineForm,
        confirmLoading,
        submitLoading,
        handleSuccess,
      };
    },
  });
</script>

<style scoped></style>
