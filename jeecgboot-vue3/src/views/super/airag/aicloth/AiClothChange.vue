<template>
  <div class="ai-cloth-change-page">
    <!-- Khu vực tiêu đề trên cùng -->
    <div class="page-header">
      <h1 class="page-title">AI Thay trang phục</h1>
      <div class="header-desc">Tải lên ảnh người mẫu và ảnh trang phục, AI tự động tạo hiệu ứng thay trang phục</div>
    </div>

    <div class="cloth-change-wrapper">
      <!-- Panel cấu hình bên trái -->
      <div class="config-panel">
        <!-- Tab trên cùng: tạo hình ảnh / Nút tạo video -->
        <div class="config-tabs">
          <a-tabs v-model:activeKey="genType" :tabBarStyle="{ margin: 0 }">
            <a-tab-pane key="image" tab="Tạo hình ảnh" />
            <a-tab-pane key="video" tab="Tạo video" />
          </a-tabs>
        </div>

        <div class="form-scroll">
          <div class="examples-row" role="toolbar" aria-label="Ví dụ">
            <a-button class="example-btn" type="default" @click.prevent="useExample1">Ví dụ: thay toàn bộ trang phục</a-button>
            <a-button class="example-btn" type="default" @click.prevent="useExample2">Ví dụ: áo trên/Quần</a-button>
          </div>
          <!-- Chọn mô hình -->
          <BasicForm @register="registerForm">
            <!-- Khu vực tải lên (một component tải lên duy nhất hỗ trợ nhiều hình ảnh) -->
            <template #clothUpload>
              <div class="section-block">
                <JImageUpload v-model:value="clothUploads" :fileMax="2" text="Tải lên trang phục" />
              </div>
            </template>
          </BasicForm>

          <!-- Gợi ý video -->
          <div v-if="genType === 'video'" class="ai-notice">
            <a-alert message="Tạo video có thể cần thời gian khá dài, vui lòng kiên nhẫn chờ đợi~" type="info" show-icon :closable="false" />
          </div>
        </div>

        <!-- Nút tạo ở đáy -->
        <div class="action-bar">
          <a-button type="primary" size="large" block class="gen-btn" :loading="loading" @click="handleGenerate"> Tạo </a-button>
        </div>
      </div>

      <!-- Hiển thị kết quả bên phải -->
      <div class="preview-panel">
        <div class="preview-content">
          <div v-if="!generatedResult && !loading" class="empty-state">
            <Icon icon="ant-design:picture-outlined" size="64" color="#ccc" />
            <p>Sau khi cấu hình ở bên trái nhấp「Tạo」</p>
          </div>

          <div v-if="loading" class="loading-state">
            <a-spin size="large" :tip="`Đang${genType === 'image' ? 'Tạo hình ảnh' : 'Tạo video'}，vui lòng chờ...`" />
          </div>

          <!-- Kết quả hình ảnh -->
          <div v-if="genType === 'image' && generatedResult && !loading" class="result-image-wrapper group">
            <img :src="generatedResult" class="result-image" alt="Kết quả thay trang phục" />
            <div class="hover-actions">
              <a-button type="primary" ghost @click="previewVisible = true"> <Icon icon="ant-design:eye-outlined" /> Xem trước </a-button>
              <a-button type="primary" ghost @click="handleDownload"> <Icon icon="ant-design:download-outlined" /> Tải xuống </a-button>
            </div>
          </div>

          <!-- Kết quả video -->
          <div v-if="genType === 'video' && generatedResult && !loading" class="result-video-wrapper group">
            <video ref="videoRef" :src="generatedResult" controls class="result-video" />
            <div class="hover-actions">
              <a-button type="primary" ghost @click="handleDownload"> <Icon icon="ant-design:download-outlined" /> Tải xuống </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ImageViewer v-if="previewVisible" :imageUrl="generatedResult" @hide="previewVisible = false" />
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm, JImageUpload } from '@/components/Form';
  import { clothImageFormSchema, clothVideoFormSchema } from './AiClothChange.data';
  import ImageViewer from '../aiapp/chat/components/ImageViewer.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Icon } from '@/components/Icon';
  import { defHttp } from '@/utils/http/axios';
  import { useGlobSetting } from '@/hooks/setting';

  const TASK_ID_KEY = 'ai_cloth_task_id';

  const { createMessage } = useMessage();
  const { domainUrl } = useGlobSetting();

  //  Trạng thái
  // Loại tạo
  const genType = ref<'image' | 'video'>('image');

  // Hình ảnh trang phục đã tải lên (JImageUpload trả về chuỗi đường dẫn nối bằng dấu phẩy)
  const clothUploads = ref<string>('');
  const loading = ref(false);
  const generatedResult = ref('');
  const previewVisible = ref(false);
  const videoRef = ref<HTMLVideoElement | null>(null);
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  // Biểu mẫu
  const [registerForm, { validate, resetSchema, getFieldsValue, setFieldsValue }] = useForm({
    schemas: clothImageFormSchema,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  // Không còn phân biệt món đơn/Nhiều món, prompt có thể do người dùng tùy chỉnh

  watch(genType, (val) => {
    generatedResult.value = '';
    resetSchema(val === 'image' ? clothImageFormSchema : clothVideoFormSchema);
  });

  /**
   * Xây dựng prompt
   * @param values
   */
  function buildPrompt(values: any, clothCount: number): string {
    const userPrompt: string = (values.userPrompt || '').toString().trim();
    const modelImageNumber = clothCount + 1;
    const clothImageNumbers = Array.from({ length: clothCount }, (_, index) => `[Hình${index + 1}]`).join(', ');
    const fixedPrompt =
      `Vai trò hình ảnh:${clothImageNumbers}Là chất liệu trang phục bắt buộc phải dùng，[Hình${modelImageNumber}]Là hình đích của người mẫu.` +
      `Lấy[Hình${modelImageNumber}]làm canvas duy nhất, sẽ${clothImageNumbers}mặc chính xác trang phục trong đó lên người người mẫu.` +
      'Phải giữ nguyên kiểu dáng, màu sắc, họa tiết, cổ áo, kiểu tay áo và chi tiết trang trí của chất liệu trang phục, không được tự thiết kế hoặc thay bằng trang phục khác;' +
      'Chỉ sửa vùng trang phục của người mẫu, giữ nguyên danh tính nhân vật, khuôn mặt, kiểu tóc, tư thế, vóc dáng, nền và bố cục, trang phục ôm tự nhiên, ánh sáng nhất quán, tả thực độ nét cao.';
    return userPrompt ? `${fixedPrompt}\nYêu cầu bổ sung:${userPrompt}` : fixedPrompt;
  }
  
  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
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
              const reg = /#\s*{\s*domainURL\s*}/g;
              generatedResult.value = (res.result as string).replace(reg, domainUrl + '/sys/common/static');
              loading.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.success(genType.value === 'image' ? 'Tạo hình ảnh thành công!' : 'Tạo video thành công!');
            }
          } else {
            loading.value = false;
            localStorage.removeItem(TASK_ID_KEY);
            createMessage.warning(res.message || 'Tạo thất bại!');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
  }
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------

  /**
   * Tạo
   */
  async function handleGenerate() {
    // Kiểm chứng ảnh trang phục (phân tích từ clothUploads)
    const validCloth = (clothUploads.value || '').split(',').filter(Boolean);
    if (validCloth.length === 0) {
      createMessage.warning('Vui lòng tải lên hình ảnh trang phục');
      return;
    }

    try {
      const values = await validate();
      loading.value = true;
      generatedResult.value = '';

      // Hợp nhất nhiều ảnh lấy ảnh cuối cùng làm canvas đích: chất liệu trang phục ở trước, ảnh người mẫu ở cuối cùng
      const modelImage = (values.modelImage || '').toString().split(',')[0];
      const imgUrls: string[] = [...validCloth, modelImage];

      if (genType.value === 'image') {
        values.imageSize = '720*1280';
      } else {
        createMessage.info('Hãy đón chờ');
        loading.value = false;
        return;
      }
      const prompt = buildPrompt(values, validCloth.length);

      const params: Record<string, any> = {
        drawModelId: values.drawModelId,
        //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
        content: prompt,
        imageUrl: imgUrls.join(','),
        //update-end---author:wangshuai ---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
        type: genType.value === 'image' ? 'cloth_image' : 'cloth_video',
        imageSize: values.imageSize,
      };

      //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
      const res = await defHttp.post(
        { url: '/airag/chat/genAiPosterAsync', params },
        { isTransformResponse: false },
      );
      if (res.success && res.result) {
        const taskId = res.result as string;
        localStorage.setItem(TASK_ID_KEY, taskId);
        startPolling(taskId);
      } else {
        loading.value = false;
        createMessage.warning('Gửi nhiệm vụ thất bại!');
      }
      //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
    } catch {
      loading.value = false;
    }
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      loading.value = true;
      generatedResult.value = '';
      startPolling(savedTaskId);
    }
  });

  onUnmounted(() => {
    if (pollTimer) {
      clearTimeout(pollTimer);
      pollTimer = null;
    }
  });
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------

  /**
   * Tải xuống
   */
  function handleDownload() {
    if (!generatedResult.value) {
      return;
    }
    const a = document.createElement('a');
    a.href = generatedResult.value;
    a.download = `ai-cloth-change-${Date.now()}.${genType.value === 'image' ? 'jpg' : 'mp4'}`;
    a.target = '_blank';
    a.click();
  }

  // Thao tác prompt ví dụ
  function useExample1() {
    const example =
      'Thay thế hoàn toàn trang phục gốc của người mẫu, ưu tiên tái hiện phom dáng, màu sắc, họa tiết và chi tiết trang trí của chất liệu trang phục.';
    const modelImage = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/model_1772695749704.jpg';
    clothUploads.value = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/dress_1772700962866.jpg';
    setFieldsValue({ userPrompt: example, modelImage: modelImage });
  }

  function useExample2() {
    const example =
      'Hình 1 là chất liệu áo trên, hình 2 là chất liệu trang phục dưới; thay thế chính xác áo trên và trang phục dưới của người mẫu, tạo thành bộ trang phục hoàn chỉnh, không được nhầm lẫn hai món trang phục.';
    const modelImage = 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/model_1772695749704.jpg';
    clothUploads.value =
      'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/jacket_1772701290346.jpg,https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/pants_1772701320192.jpg';
    setFieldsValue({ userPrompt: example, modelImage: modelImage });
  }
</script>

<style lang="less" scoped>
  @import 'AiClothChange.less';
</style>
