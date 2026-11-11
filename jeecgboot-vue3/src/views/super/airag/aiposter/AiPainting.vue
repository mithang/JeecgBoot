<template>
  <div class="content-wrapper">
    <!-- Cấu hình tham số ở giữa -->
    <div class="config-panel">
      <div class="config-tabs">
        <a-tabs v-model:activeKey="configTab" :tabBarStyle="{ margin: 0 }">
          <a-tab-pane key="draw" tab="Vẽ" />
          <a-tab-pane key="face" tab="Thay khuôn mặt" />
          <a-tab-pane key="mix" tab="Trộn hình" />
        </a-tabs>
      </div>

      <!-- Khu vực nút ví dụ -->
      <div class="example-buttons" v-if="configTab === 'mix'">
        <a-tooltip title="Làm thẻ công tác">
          <a-button class="example-btn" size="small" @click="applyExample('work_card')">Ví dụ một</a-button>
        </a-tooltip>
        <a-tooltip title="Thay trang phục">
          <a-button class="example-btn" size="small" @click="applyExample('change_clothes')">Ví dụ hai</a-button>
        </a-tooltip>
      </div>

      <div class="form-container">
        <BasicForm @register="registerForm" />

        <div class="instructions" v-if="configTab === 'face'">
          <div class="title">Giải thích:</div>
          <p>1 Mọi hình ảnh đều phải chứa khuôn mặt, nếu không sẽ không ra được ảnh</p>
          <p>2 "Hình ngôi sao"Có thể dùng mj vẽ tạo ra trước</p>
          <p>3 "Hình ngôi sao"Thực ra hình anime cũng được</p>
          <p>4 "Ảnh đại diện của bạn"Khuyên dùng ảnh cá nhân một inch</p>
        </div>

        <div class="instructions" v-if="configTab === 'mix'">
          <div class="title">Giải thích:</div>
          <p>1 Tổng hợp ít nhất 2 hình ảnh</p>
          <p>2 Tối đa có thể truyền 3 hình</p>
        </div>
      </div>
      <div class="action-container">
        <a-button type="primary" size="large" block @click="handleGenerate" :loading="loading">
          <Icon icon="ant-design:thunderbolt-outlined" />
          Tạo ngay
        </a-button>
      </div>
    </div>

    <!-- Kết quả tạo hình ảnh bên phải -->
    <div class="preview-panel">
      <div class="panel-title">Kết quả tạo</div>
      <div class="preview-content">
        <div v-if="!generatedImage && !loading" class="empty-state">
          <Icon icon="ant-design:picture-outlined" size="64" color="#ccc" />
          <p>Cấu hình tham số ở bên trái và nhấp tạo</p>
        </div>

        <div v-if="loading && !generatedImage" class="loading-state">
          <a-spin size="large" tip="Đang vẽ hình ảnh, vui lòng chờ..." />
        </div>

        <div v-if="generatedImage" class="result-image-wrapper group">
          <img :src="generatedImage" class="result-image" alt="Generated Image" />
          <div class="image-actions">
            <a-button class="preview-button" @click="handlePreview">
              <Icon icon="ant-design:eye-outlined" />
              Xem trước
            </a-button>
            <a-button type="primary" @click="handleDownload">
              <Icon icon="ant-design:download-outlined" />
              Tải xuống
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ImageViewer v-if="previewVisible" :imageUrl="generatedImage" @hide="previewVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { drawFormSchema, faceSwapFormSchema, mixFormSchema } from './AiPoster.data';
  import ImageViewer from '../aiapp/chat/components/ImageViewer.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Icon } from '@/components/Icon';
  import { defHttp } from '@/utils/http/axios';
  import { useGlobSetting } from '@/hooks/setting';

  const { createMessage } = useMessage();
  const loading = ref(false);
  //update-begin---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Vẽ AI đổi thành thăm dò bất đồng bộ, hỗ trợ tiếp tục truy vấn sau khi chuyển menu
  const PAINTING_TASK_ID_KEY = 'ai_painting_task_id';
  let pollTimer: ReturnType<typeof setTimeout> | null = null;
  //update-end---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Vẽ AI đổi thành thăm dò bất đồng bộ, hỗ trợ tiếp tục truy vấn sau khi chuyển menu
  const generatedImage = ref('');
  const previewVisible = ref(false);
  const configTab = ref('draw');
  const FACE_SWAP_PROMPT =
    'Thay khuôn mặt của hình 2 vào vùng khuôn mặt của hình 1, giữ lại đặc trưng khuôn mặt và chi tiết ngũ quan của hình 2, giữ nguyên tư thế cơ thể, kiểu tóc, trang phục, bố cục và nền của hình 1, khuôn mặt hòa trộn tự nhiên, ánh sáng và tông da hài hòa, độ phân giải cao, phong cách tả thực';
  const MIX_CHANGE_CLOTHES_PROMPT =
    'Dùng hình 2 làm hình nền chủ thể duy nhất và hình ảnh cuối cùng, chỉ chuyển trang phục mà nhân vật trong hình 1 đang mặc sang nhân vật trong hình 2. Phải giữ nguyên hoàn toàn danh tính, khuôn mặt, ngũ quan, kiểu tóc, thể hình, tư thế cơ thể của nhân vật hình 2 cùng bố cục, môi trường và nền của hình 2, không được dùng hoặc sao chép khuôn mặt, đầu, tư thế cơ thể, danh tính nhân vật và nền của hình 1. Hình 1 chỉ làm tham khảo trang phục, tái hiện chính xác kiểu dáng, màu sắc, họa tiết và chất liệu trang phục của nó, để trang phục ôm tự nhiên vào nhân vật hình 2, giữ nguyên ánh sáng và phong cách hình ảnh vốn có của hình 2. Kết quả cuối cùng phải là người trong hình 2 mặc quần áo của hình 1, chứ không phải tạo ra nhân vật trong hình 1. Độ phân giải cao, phong cách tả thực';

  const { domainUrl } = useGlobSetting();
  const [registerForm, { validate, resetSchema, setFieldsValue }] = useForm({
    schemas: drawFormSchema,
    labelWidth: 100,
    actionColOptions: { span: 24 },
    showActionButtonGroup: false,
  });

  watch(configTab, (val) => {
    if (val === 'draw') {
      resetSchema(drawFormSchema);
    } else if (val === 'face') {
      resetSchema(faceSwapFormSchema).then(() => {
        setFieldsValue({ content: FACE_SWAP_PROMPT });
      });
    } else if (val === 'mix') {
      resetSchema(mixFormSchema).then(() => {
        setFieldsValue({ content: MIX_CHANGE_CLOTHES_PROMPT });
      });
    } else {
      // Default to draw or empty for mix for now
      resetSchema(drawFormSchema);
    }
  });

  //update-begin---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Vẽ AI đổi thành thăm dò bất đồng bộ, hỗ trợ tiếp tục truy vấn sau khi chuyển menu
  /** Thăm dò truy vấn kết quả nhiệm vụ */
  function startPolling(taskId: string) {
    const poll = () => {
      defHttp
        .get({ url: `/airag/chat/getAiPosterResult/${taskId}` }, { isTransformResponse: false })
        .then((res) => {
          if (res.success) {
            if (res.result === 'pending' || res.result === null) {
              pollTimer = setTimeout(poll, 3000);
            } else {
              let imageUrl = res.result as string;
              const reg = /#\s*{\s*domainURL\s*}/g;
              imageUrl = imageUrl.replace(reg, domainUrl + '/sys/common/static');
              generatedImage.value = imageUrl;
              loading.value = false;
              localStorage.removeItem(PAINTING_TASK_ID_KEY);
              createMessage.success('Tạo hình ảnh thành công!');
            }
          } else {
            loading.value = false;
            localStorage.removeItem(PAINTING_TASK_ID_KEY);
            createMessage.warning(res.message || 'Tạo hình ảnh thất bại!');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
  }

  onMounted(() => {
    const savedTaskId = localStorage.getItem(PAINTING_TASK_ID_KEY);
    if (savedTaskId) {
      loading.value = true;
      generatedImage.value = '';
      startPolling(savedTaskId);
    }
  });

  onUnmounted(() => {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = null;
    }
  });
  //update-end---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Vẽ AI đổi thành thăm dò bất đồng bộ, hỗ trợ tiếp tục truy vấn sau khi chuyển menu

  async function handleGenerate() {
    try {
      const values = await validate();
      loading.value = true;
      generatedImage.value = '';
      values.type = configTab.value;

      if (configTab.value === 'face') {
        if (values.sourceImage && values.targetImage) {
          values.imageUrl = values.sourceImage + ',' + values.targetImage;
          delete values.sourceImage;
          delete values.targetImage;
        }
        values.content =
          values.content ||
          FACE_SWAP_PROMPT;
      }

      //update-begin---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Đổi thành gửi bất đồng bộ, lấy được taskId rồi bắt đầu thăm dò
      const res = await defHttp.post(
        { url: '/airag/chat/genAiPosterAsync', params: values },
        { isTransformResponse: false },
      );
      if (res.success && res.result) {
        const taskId = res.result as string;
        localStorage.setItem(PAINTING_TASK_ID_KEY, taskId);
        startPolling(taskId);
      } else {
        loading.value = false;
        createMessage.warning('Gửi nhiệm vụ thất bại!');
      }
      //update-end---author:wangshuai---date:2026-04-15---for:【QQYUN-14944】Đổi thành gửi bất đồng bộ, lấy được taskId rồi bắt đầu thăm dò
    } catch (error) {
      console.error('Validation failed:', error);
      loading.value = false;
    }
  }

  function handlePreview() {
    previewVisible.value = true;
  }

  function applyExample(type: string) {
    if (type === 'work_card') {
      setFieldsValue({
        imageUrl:
          'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/afdad9ea-077f-44a4-9d85-c26cde7aceed_1770703400282.png,https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/4e4d1886-fb3b-4c01-abf6-25e546a1253e_1770703403479.png',
        content:
          'Lấy[Hình 1]Dùng bố cục và chất cảm kính mờ của bản thiết kế danh thiếp làm mẫu, cho[Hình 2]Tạo thẻ công tác dọc cho nhân vật. Mặt thẻ bo góc bán trong suốt, điểm sáng mềm và bóng đổ nông; ảnh bán thân nhân vật đặt ở vùng giữa trên; góc dưới trái trình bày họ tên/Chức vụ/Công ty/điện thoại, phông không chân tối giản, khoảng trắng cân bằng. Góc trên phải đặt[Hình 1]hình tượng hoạt hình 3D dễ thương của nhân vật, phá vỡ ranh giới nửa nổi ra khỏi thẻ và đổ bóng nhẹ, tạo thành lớp và điểm nhấn thị giác. Ánh sáng tự nhiên sáng tổng thể, chi tiết chất liệu chân thực, không thêm họa tiết và yếu tố thừa. Nhân vật cần rõ nét, không được mờ.',
      });
    } else if (type === 'change_clothes') {
      setFieldsValue({
        imageUrl:
          'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/4e4d1886-fb3b-4c01-abf6-25e546a1253e_1770703403479.png,https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/63706787-4072-4cba-ad88-385e0584b020_1770703456766.png',
        content: MIX_CHANGE_CLOTHES_PROMPT,
      });
    }
  }

  /**
   * Xuất hình ảnh
   */
  function handleDownload() {
    if (!generatedImage.value) {
      return;
    }
    const a = document.createElement('a');
    a.href = generatedImage.value;
    a.download = `ai-painting-${Date.now()}.jpg`;
    a.target = '_blank';
    a.click();
  }
</script>

<style lang="less" scoped>
  .content-wrapper {
    flex: 1;
    display: flex;
    gap: 16px;
    overflow: hidden;
    height: 100%; /* Ensure it takes full height of parent */
  }

  .config-panel {
    width: 550px;
    min-width: 350px;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    padding: 20px;
    overflow: hidden;
    position: relative;

    .config-tabs {
      margin-bottom: 20px;

      :deep(.ant-tabs-nav::before) {
        border-bottom: none;
      }

      :deep(.ant-tabs-tab) {
        padding: 8px 0;
        margin: 0 32px 0 0;
        font-size: 16px;

        &.ant-tabs-tab-active .ant-tabs-tab-btn {
          color: #00b96b;
          font-weight: 500;
        }
      }

      :deep(.ant-tabs-ink-bar) {
        background: #00b96b;
      }
    }

    .example-buttons {
      position: absolute;
      top: 28px;
      right: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      z-index: 10;

      .example-btn {
        color: #666;
        border-color: #d9d9d9;

        &:hover {
          color: #00b96b;
          border-color: #00b96b;
        }
      }
    }

    .form-container {
      flex: 1;
      overflow-y: auto;

      .instructions {
        margin-top: 20px;
        padding: 0 10px;
        color: #666;
        font-size: 14px;
        line-height: 1.8;

        .title {
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
        }

        p {
          margin: 0;
        }
      }
    }

    .action-container {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
    }
  }

  .preview-panel {
    flex: 1;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    padding: 20px;
    overflow: hidden;

    .preview-content {
      flex: 1;
      background: #f7f8fc;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
    }
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin-bottom: 20px;
    padding-left: 8px;
    border-left: 4px solid #1890ff;
    line-height: 1;
  }

  .empty-state {
    text-align: center;
    color: #8f959e;

    p {
      margin-top: 16px;
    }
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .result-image-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;

    .result-image {
      width: 100%;
      height: 100%;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      object-fit: contain;
    }

    .image-actions {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 10px;
      border: 1px solid rgba(255, 255, 255, 0.5);
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.92);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
      opacity: 0;
      transition: opacity 0.2s ease;
      backdrop-filter: blur(6px);

      .preview-button {
        color: #1677ff;
        border-color: #1677ff;
      }
    }

    &:hover .image-actions {
      opacity: 1;
    }
  }
</style>
