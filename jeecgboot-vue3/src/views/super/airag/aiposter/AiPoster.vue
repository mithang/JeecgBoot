<template>
  <div class="ai-poster-page">
    <div class="page-header">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="poster" tab="AI Poster" />
        <a-tab-pane key="painting" tab="AI Vẽ tranh" />
      </a-tabs>
    </div>

    <div class="content-wrapper" v-show="activeKey === 'poster'">
      <!-- Chọn mẫu bên trái -->
      <div class="template-panel">
        <div class="panel-title">Chọn mẫu</div>
        <div class="template-list">
          <div
            v-for="item in templates"
            :key="item.id"
            class="template-item"
            :class="{ active: activeTemplateId === item.id }"
            @click="handleSelectTemplate(item)"
          >
            <div class="template-cover">
              <img :src="item.url" style="width: 100%" />
            </div>
            <div class="template-name">{{ item.name }}</div>
          </div>
        </div>
      </div>
      <!-- Cấu hình tham số ở giữa -->
      <div class="config-panel">
        <div class="panel-title">Cấu hình tham số</div>
        <div class="form-container">
          <BasicForm @register="registerForm" />
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
            <a-spin size="large" tip="Đang vẽ poster, vui lòng chờ..." />
          </div>

          <div v-if="generatedImage" class="result-image-wrapper group">
            <img :src="generatedImage" class="result-image" alt="Generated Poster" />
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

    <AiPainting v-if="activeKey === 'painting'" />

    <ImageViewer v-if="previewVisible" :imageUrl="generatedImage" @hide="previewVisible = false" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { formSchema } from './AiPoster.data';
  import ImageViewer from '../aiapp/chat/components/ImageViewer.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Icon } from '@/components/Icon';
  import { defHttp } from '@/utils/http/axios';
  import { useGlobSetting } from '@/hooks/setting';
  import AiPainting from './AiPainting.vue';

  const TASK_ID_KEY = 'ai_poster_task_id';
  const ACTIVE_TAB_KEY = 'ai_poster_active_tab';
  const activeKey = ref(localStorage.getItem(ACTIVE_TAB_KEY) || 'poster');
  watch(activeKey, (val) => localStorage.setItem(ACTIVE_TAB_KEY, val));

  const { createMessage } = useMessage();
  const loading = ref(false);
  const generatedImage = ref('');
  const previewVisible = ref(false);

  const activeTemplateId = ref<number | null>(null);
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  const templates = [
    {
      id: 1,
      name: 'Phong chính quyền thanh nhã',
      prompt:
        'Poster ngang phong chính quyền thanh nhã, tông màu chính xanh nhạt + Trắng gạo + xám nhạt, phong cách vector phẳng, chồng thêm họa tiết giấy Tuyên mịn; Yếu tố cốt lõi của hình ảnh: đường nét tòa nhà văn phòng chính quyền giản lược (đường nét đơn giản), bút máy vàng, cuốn công văn mở ra, họa tiết mây lành xanh nhạt, trang trí cành ô liu; Nền là chuyển màu trắng ngà + họa tiết sọc dọc xanh nhạt, điểm xuyết viền vàng mảnh; Thiết kế chữ: ở giữa viết ‘Chính quyền vì dân・Hiệu quả chuẩn mực’ bằng phông Hei, phía dưới thêm chữ nhỏ xanh nhạt ‘Phục vụ tận tâm・Thiết thực vững vàng’; Không khí tổng thể thanh nhã trang trọng, chuyên nghiệp đơn giản, phân lớp rõ ràng, ánh sáng mềm, chi tiết độ nét cao, bố cục dọc 9:16',
      size: '720*1280',
      url: 'https://upload.jeecg.com/jeecg/AI/simple.png',
    },
    {
      id: 2,
      name: 'Poster ngày lễ',
      prompt:
        'Poster dọc Tết phong cách Trung Quốc quốc triều, tông màu chính đỏ vàng + chuyển màu cam ấm, phong cách minh họa quốc triều, vector phẳng + chất cảm chuyển màu mềm, chồng thêm họa tiết giấy Tuyên mịn; Lớp hình ảnh: tiền cảnh là hoa mai phong cách cắt giấy màu đỏ, nhãn dán chữ Phúc vàng, bánh chẻo căng tròn, dãy đèn lồng đỏ, trung cảnh là mái hiên nhà dân truyền thống tường đỏ ngói vàng, mây lành dạng dải bay, viễn cảnh là pháo hoa vàng nhạt nở rộ + nền quầng sáng đỏ ấm; điểm xuyết họa tiết đồng tiền vàng, thỏ gấp giấy, họa tiết như ý; giữa hình ảnh hơi lệch lên trên viết ‘Chúc mừng năm mới’ bằng phông thư pháp vàng, phía dưới thêm\'Vạn sự như ý\'chữ khải bốn chữ; Không khí tổng thể vui mừng điềm lành, đoàn viên ấm áp, phân lớp rõ ràng, chính phụ phân minh, ánh sáng mềm không chói mắt, chi tiết độ nét cao, bố cục dọc 9:16',
      url: 'https://upload.jeecg.com/jeecg/AI/image89444392111.png',
      size: '720*1280',
    },
    {
      id: 3,
      name: 'Tuyên truyền công nghệ',
      prompt:
        'Poster tuyên truyền cảm giác công nghệ tương lai, tông màu chính chuyển màu xanh tím + màu kim loại trắng bạc, hiệu ứng ánh sáng neon lạnh, chất cảm đường nét cyberpunk; Yếu tố cốt lõi của hình ảnh: mô hình dữ liệu Trái Đất chiếu toàn tức, dòng dữ liệu xanh lam chảy động, họa tiết bảng mạch phát sáng, chip và bánh răng cơ khí lơ lửng, đường nét kiến trúc đậm chất tương lai; điểm xuyết hiệu ứng ánh sáng hạt, giao diện toàn tức trong suốt, dải ánh sáng neon; Thiết kế chữ: ở giữa viết ‘Công nghệ tiếp sức・Trí tuệ mở lối’ bằng phông không chân đậm chất tương lai, phía dưới thêm chữ nhỏ ‘Đổi mới dẫn dắt・Hướng tới tương lai’, chữ có viền phát sáng nhẹ; Không khí tổng thể đơn giản cao cấp, tràn đầy cảm giác tương lai, phân lớp rõ ràng, ánh sáng sắc nét, chi tiết độ nét cao, bố cục ngang 16:9',
      size: '720*1280',
      url: 'https://upload.jeecg.com/jeecg/AI/technology.png',
    },
    {
      id: 4,
      name: 'Thanh nhã cổ điển',
      prompt:
        'Poster dọc phong cách Dân Quốc thanh nhã cổ điển, tông màu chính vàng be + Đỏ đậu sa + đen mực, chất cảm phim độ bão hòa thấp, chồng thêm họa tiết báo cũ và hạt nhiễu nhẹ; Yếu tố cốt lõi của hình ảnh: bóng nghiêng của người phụ nữ mặc sườn xám trắng ngà (tóc búi cài trâm ngọc trai), ô giấy dầu, máy hát cổ điển, khung cửa sổ gỗ chạm khắc, họa tiết dây leo quấn ngọc trai; Nền là đường nét kiến trúc Shikumen Thượng Hải xưa mờ ảo, điểm xuyết hoa mộc lan hồng nhạt, câu thơ trình bày bằng phông chữ cổ điển (‘Năm tháng bình yên, dịu dàng như xưa’); Thiết kế chữ: phía trên viết ‘Thời gian tao nhã’ bằng chữ viết tay thời Dân Quốc, phía dưới dùng phông có chân ‘Tái hiện phong nhã Dân Quốc’, chữ có hiệu ứng cũ nhẹ; Không khí tổng thể dịu dàng tri thức, tĩnh lặng tao nhã, ánh sáng mềm (ánh sáng bên phác họa đường nét nhân vật), phân lớp rõ ràng, chi tiết độ nét cao, bố cục dọc 9:16',
      size: '720*1280',
      url: 'https://upload.jeecg.com/jeecg/AI/retro.png',
    },
    {
      id: 5,
      name: 'Cyberpunk',
      prompt:
        'Poster ngang cyberpunk quốc triều, tông màu chính đỏ Trung Quốc + Đen không gian sâu + Neon mạ vàng, họa tiết truyền thống và yếu tố công nghệ va chạm, chồng thêm hiệu ứng ánh sáng chuyển màu đỏ vàng + họa tiết thẻ tre; Yếu tố cốt lõi của hình ảnh: dải ánh sáng neon hình rồng (thân rồng quấn quanh bảng mạch), kiến trúc cổ phong cyber tường đỏ ngói vàng (mái hiên treo đèn lồng neon), nhân vật cyber mặc Hán phục cải tiến (kèm trâm tóc phát sáng / tay áo cơ khí), bảng đèn neon chữ Hán chiếu toàn tức (‘Giang hồ’ ‘Tương lai’); điểm xuyết dòng dữ liệu mây lành, họa tiết hồi văn truyền thống chất cảm kim loại, thiết bị cơ khí hình thỏi vàng mạ vàng lơ lửng; Thiết kế chữ: phía trên viết ‘Giang hồ cyber’ bằng phông thư pháp vàng, phía dưới thêm ‘TECH & TRADITION’, chữ có hiệu ứng phát sáng neon đỏ vàng; Không khí tổng thể hoành tráng sành điệu, truyền thống và tương lai hòa quyện, ánh sáng mạnh và giàu sức tác động, phân lớp rõ ràng, chi tiết độ nét cao, bố cục ngang 16:9',
      size: '720*1280',
      url: 'https://upload.jeecg.com/jeecg/AI/cyberpunk.png',
    },
  ];
  const { domainUrl } = useGlobSetting();
  const [registerForm, { validate, setFieldsValue }] = useForm({
    schemas: formSchema,
    labelWidth: 100,
    actionColOptions: { span: 24 },
    showActionButtonGroup: false,
  });

  /**
   * Sự kiện chọn mẫu
   * @param template
   */
  function handleSelectTemplate(template: any) {
    activeTemplateId.value = template.id;
    setFieldsValue({
      content: template.prompt,
      imageUrl: template.url,
      imageSize: template.size,
    });
    createMessage.success(`Đã áp dụng mẫu:${template.name}`);
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
              // Tiếp tục thăm dò
              pollTimer = setTimeout(poll, 3000);
            } else {
              // Thành công
              let imageUrl = res.result as string;
              const reg = /#\s*{\s*domainURL\s*}/g;
              imageUrl = imageUrl.replace(reg, domainUrl + '/sys/common/static');
              generatedImage.value = imageUrl;
              loading.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.success('Tạo poster thành công!');
            }
          } else {
            // Thất bại
            loading.value = false;
            localStorage.removeItem(TASK_ID_KEY);
            createMessage.warning(res.message || 'Tạo poster thất bại!');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
  }
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  
  async function handleGenerate() {
    try {
      const values = await validate();
      loading.value = true;
      generatedImage.value = '';
      //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
      values.type = 'poster';
      const res = await defHttp.post(
        { url: '/airag/chat/genAiPosterAsync', params: values },
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
    } catch (error) {
      loading.value = false;
      console.error('Validation failed:', error);
    }
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  onMounted(() => {
    // Khi chuyển về menu, nếu có nhiệm vụ chưa hoàn thành thì khôi phục thăm dò
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
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
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------

  function handlePreview() {
    previewVisible.value = true;
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
    a.download = `ai-poster-${Date.now()}.jpg`;
    a.target = '_blank';
    a.click();
  }
</script>

<style lang="less" scoped>
  .ai-poster-page {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    padding: 16px;
    background-color: #f0f2f5;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    overflow: hidden;

    .page-header {
      margin-bottom: 16px;
      background: #fff;
      padding: 16px 24px;
      border-radius: 8px;

      .title {
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;
        margin-right: 12px;
      }

      .subtitle {
        color: #8f959e;
        font-size: 14px;
      }
    }

    .content-wrapper {
      flex: 1;
      display: flex;
      gap: 16px;
      overflow: hidden;
    }

    .template-panel {
      width: 200px;
      min-width: 180px;
      background: #fff;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      padding: 12px;
      overflow-y: auto;

      .template-list {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .template-item {
        border: 1px solid #f0f0f0;
        border-radius: 6px;
        padding: 8px;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        &.active {
          border-color: #1890ff;
          background-color: #e6f7ff;
        }

        .template-cover {
          width: 100%;
          aspect-ratio: 9 / 16;
          height: auto;
          background-color: #f5f5f5;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #8c8c8c;
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 4px;
            display: block;
          }
        }

        .template-name {
          font-size: 14px;
          color: #1f2329;
          text-align: center;
          max-width: 100%;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
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

      .form-container {
        flex: 1;
        overflow-y: auto;
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
  }
</style>
