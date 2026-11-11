<!--Nhập thủ công text-->
<template>
  <div class="p-2">
    <BasicModal destroyOnClose @register="registerModal" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <div v-show="currentStep === 0">
        <BasicForm @register="registerForm"></BasicForm>
        <div v-if="showWebContent" class="web-content-preview">
          <div class="web-content-label">Phân tích nội dung (chỉ đọc)</div>
          <div class="web-content-body">
            <pre>{{ webContentText }}</pre>
          </div>
        </div>
      </div>
      <div v-show="currentStep === 1">
        <!-- Khi kho tri thức có chiến lược phân đoạn mặc định, hiển thị chọn nguồn -->
        <div v-if="knowledgeDefaultSegment" style="margin-bottom: 16px">
          <div style="margin-bottom: 8px; font-weight: 500; color: rgba(0,0,0,0.85)">Nguồn chiến lược phân đoạn</div>
          <a-radio-group v-model:value="useKnowledgeDefault" button-style="solid">
            <a-radio-button value="default">Dùng mặc định của kho tri thức</a-radio-button>
            <a-radio-button value="custom">Tùy chỉnh</a-radio-button>
          </a-radio-group>
          <div style="margin-top: 6px; font-size: 12px; color: rgba(0,0,0,0.45)">
            <span v-if="useKnowledgeDefault === 'default'">Lưu trực tiếp, tài liệu sẽ dùng chiến lược phân đoạn do kho tri thức cấu hình</span>
            <span v-else>Bỏ qua chiến lược mặc định của kho tri thức, cấu hình riêng tham số phân đoạn cho tài liệu này</span>
          </div>
          <!-- Hiển thị chỉ đọc chiến lược mặc định của kho tri thức -->
          <div v-if="useKnowledgeDefault === 'default'" class="default-segment-info">
            <a-descriptions :column="2" size="small" bordered style="margin-top: 12px">
              <a-descriptions-item label="Chế độ phân đoạn">{{ knowledgeDefaultSegment.segmentStrategy === 'custom' ? 'Tùy chỉnh' : 'Tự động phân đoạn và làm sạch' }}</a-descriptions-item>
              <a-descriptions-item label="Độ dài tối đa">{{ knowledgeDefaultSegment.maxSegment }}</a-descriptions-item>
              <a-descriptions-item label="Mức chồng lấp%">{{ knowledgeDefaultSegment.overlap }}</a-descriptions-item>
              <a-descriptions-item v-if="knowledgeDefaultSegment.segmentStrategy === 'custom'" label="Định danh phân đoạn">{{ knowledgeDefaultSegment.separator === 'custom' ? knowledgeDefaultSegment.customSeparator : knowledgeDefaultSegment.separator }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
        <BasicForm v-show="!knowledgeDefaultSegment || useKnowledgeDefault === 'custom'" @register="registerSegmentForm"></BasicForm>
      </div>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref, computed } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { docSegmentSchema, docTextSchema } from '../AiKnowledgeBase.data';
  import { knowledgeSaveDoc } from '../AiKnowledgeBase.api';

  export default {
    name: 'AiragKnowledgeDocModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('Tạo kho tri thức');
      const currentStep = ref(0);
      const step1Values = ref({});
      //Dữ liệu phân từ tùy chỉnh
      const segmentMetadataRef = ref<any>({});
      // Chiến lược phân đoạn mặc định của kho tri thức (có giá trị biểu thị kho tri thức đã cấu hình phân đoạn mặc định)
      const knowledgeDefaultSegment = ref<any>(null);
      // Nguồn chiến lược phân đoạn:'default' Dùng mặc định của kho tri thức，'custom' Tùy chỉnh
      const useKnowledgeDefault = ref<'default' | 'custom'>('default');
      // Loại kho tri thức:'knowledge' | 'memory'
      const knowledgeType = ref<string>('knowledge');

      //Lưu hoặc sửa đổi
      const isUpdate = ref<boolean>(false);
      //id kho tri thức
      const knowledgeId = ref<string>();
      //Nội dung phân tích trang web (hiển thị chỉ đọc)
      const webContentText = ref<string>('');
      const docType = ref<string>('');
      const showWebContent = computed(() => docType.value === 'web' && isUpdate.value && webContentText.value);
      //Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
        schemas: docTextSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      const [registerSegmentForm, { resetFields: resetSegmentFields, validate: validateSegment, setFieldsValue: setSegmentFieldsValue }] = useForm({
        schemas: docSegmentSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
        labelCol: { span: 24 },
      });

      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        //Đặt lại biểu mẫu
        await resetFields();
        await resetSegmentFields();
        currentStep.value = 0;
        webContentText.value = '';
        docType.value = '';
        knowledgeType.value = data?.knowledgeType || 'knowledge';
        setModalProps({ confirmLoading: false, okText: knowledgeType.value === 'memory' ? 'Lưu' : 'Bước tiếp theo' });
        isUpdate.value = !!data?.isUpdate;
        title.value = isUpdate.value ? 'Chỉnh sửa tài liệu' : 'Tạo tài liệu';

        //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
        // Phân tích chiến lược phân đoạn mặc định của kho tri thức
        knowledgeDefaultSegment.value = null;
        useKnowledgeDefault.value = 'default';
        if (data?.knowledgeMetadata) {
          try {
            const kmeta = JSON.parse(data.knowledgeMetadata);
            if (kmeta.enableSegment) {
              knowledgeDefaultSegment.value = kmeta;
            }
          } catch (_e) {}
        }
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------

        if (unref(isUpdate)) {
          docType.value = data.record.type || '';
          if(data.record.type === 'file' && data.record.metadata){
            data.record.filePath = JSON.parse(data.record.metadata).filePath;
          }
          if(data.record.type === 'web' && data.record.metadata){
            data.record.website = JSON.parse(data.record.metadata).website;
          }
          if(data.record.type === 'web' && data.record.content){
            webContentText.value = data.record.content;
          }
          //Gán giá trị biểu mẫu
          await setFieldsValue({
            ...data.record,
          });

          // Phân tích metadata và chuẩn bị cho biểu mẫu bước hai
          if (data.record.metadata) {
            const meta = JSON.parse(data.record.metadata);
            //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
            // Nếu khi lưu tài liệu đã dùng chiến lược mặc định của kho tri thức, khi hiển thị lại khôi phục tùy chọn
            // Tương thích dữ liệu cũ: nếu kho tri thức không còn chiến lược phân đoạn mặc định, hạ xuống tùy chỉnh (hiển thị giá trị mặc định)
            if (meta.useKnowledgeDefault && knowledgeDefaultSegment.value) {
              useKnowledgeDefault.value = 'default';
            } else {
              useKnowledgeDefault.value = 'custom';
              // update-begin--author:wangshuai--date:2026-04-09--for:【issue/9418】Kho tri thức AI tải tệp lên quá lớn, vector hóa thất bại
              let strategy = meta.segmentStrategy || (meta.separator ? 'custom' : 'auto');
              // update-end--author:wangshuai--date:2026-04-09--for:【issue/9418】Kho tri thức AI tải tệp lên quá lớn, vector hóa thất bại
              segmentMetadataRef.value = {
                segmentStrategy: strategy,
                maxSegment: meta.maxSegment,
                overlap: meta.overlap,
                textRules: meta.textRules,
                separator: meta.separator,
                preprocessingRules: meta.preprocessingRules,
                customSeparator: strategy === 'custom' ? meta.customSeparator : '',
              };
            }
            //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
          } else {
            useKnowledgeDefault.value = knowledgeDefaultSegment.value ? 'default' : 'custom';
            segmentMetadataRef.value = { segmentStrategy: 'auto', separator: '\\n', maxSegment: 800, overlap: 10 };
          }
        } else {
          knowledgeId.value = data.knowledgeId;
          docType.value = data.type || '';
          // Khi tạo mới: nếu có chiến lược mặc định của kho tri thức thì mặc định chọn"Dùng mặc định của kho tri thức"
          useKnowledgeDefault.value = knowledgeDefaultSegment.value ? 'default' : 'custom';
          segmentMetadataRef.value = { segmentStrategy: 'auto', separator: '\\n', maxSegment: 800, overlap: 10 };
          await setFieldsValue({ type: data.type });
        }
        setModalProps({ bodyStyle: { padding: '10px' } });
      });

      /**
       * Lưu
       */
      async function handleOk() {
        try {
          if (currentStep.value === 0 && knowledgeType.value !== 'memory') {
            step1Values.value = await validate();
            currentStep.value = 1;
            setModalProps({ okText: 'Lưu', minHeight: 400 });
            if (segmentMetadataRef.value) {
              await setSegmentFieldsValue({ ...segmentMetadataRef.value });
            }
            return;
          }

          if (currentStep.value === 0 && knowledgeType.value === 'memory') {
            step1Values.value = await validate();
          }

          setModalProps({ confirmLoading: true });
          let values: any = { ...step1Values.value };

          let metadata: any = {};

          //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
          if (knowledgeType.value === 'memory') {
            // Kho bộ nhớ không cần chiến lược phân đoạn, metadata để trống
          } else if (useKnowledgeDefault.value === 'default' && knowledgeDefaultSegment.value) {
            // Dùng chiến lược phân đoạn mặc định của kho tri thức, chỉ cần đánh dấu, backend đọc metadata của kho tri thức
            metadata = { useKnowledgeDefault: true };
          } else {
            // update-begin--author:wangshuai--date:2026-04-09--for:【issue/9418】Kho tri thức AI tải tệp lên quá lớn, vector hóa thất bại
            const segmentFormValues = await validateSegment();
            metadata = {
              segmentStrategy: segmentFormValues.segmentStrategy,
              maxSegment: segmentFormValues.maxSegment,
              overlap: segmentFormValues.overlap,
            };
            if (segmentFormValues.segmentStrategy === 'custom') {
              metadata = {
                ...metadata,
                separator: segmentFormValues.separator,
                customSeparator: segmentFormValues.customSeparator,
                textRules: segmentFormValues.textRules,
              };
            }
            // update-end--author:wangshuai--date:2026-04-09--for:【issue/9418】Kho tri thức AI tải tệp lên quá lớn, vector hóa thất bại
          }
          //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
          if (!unref(isUpdate)) {
            values.knowledgeId = knowledgeId.value;
          }
          if(values.filePath){
            metadata.filePath = values.filePath;
            delete values.filePath;
          }
          if(values.website){
            metadata.website = values.website;
            delete values.website;
          }
          values.metadata = JSON.stringify(metadata);
          await knowledgeSaveDoc(values);
          //Đóng popup
          closeModal();
          //Làm mới danh sách
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        registerSegmentForm,
        currentStep,
        title,
        handleOk,
        handleCancel,
        showWebContent,
        webContentText,
        knowledgeDefaultSegment,
        useKnowledgeDefault,
        knowledgeType,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
  .web-content-preview {
    margin-top: 8px;
    .web-content-label {
      font-weight: 500;
      margin-bottom: 6px;
      color: rgba(0, 0, 0, 0.85);
    }
    .web-content-body {
      max-height: 300px;
      overflow-y: auto;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      padding: 12px;
      background: #f5f5f5;
      pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
        font-size: 13px;
        line-height: 1.6;
        color: #333;
      }
    }
  }
</style>
