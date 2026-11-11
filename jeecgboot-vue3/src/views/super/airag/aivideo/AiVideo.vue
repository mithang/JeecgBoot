<template>
  <div class="ai-video-page">
    <!-- Khu vực đầu -->
    <div class="page-header">
      <div class="title">AI Video</div>
      <div class="subtitle">Chuyển đổi nhanh văn bản thành nội dung video sinh động</div>
    </div>

    <div class="content-wrapper">
      <!-- Bên trái: điều khiển video + Nội dung văn bản + Tình huống thường dùng + Nút tạo -->
      <div class="control-panel">
        <div class="panel-title">Cấu hình video</div>
        <div class="form-container">
          <BasicForm @register="registerForm" :schemas="leftFormSchemas" />

          <!-- Ô nhập nội dung văn bản -->
          <div class="form-item-group">
            <label class="form-label">Nội dung văn bản</label>
            <a-textarea v-model:value="formText" :rows="4" :maxlength="500" show-count placeholder="Vui lòng nhập nội dung văn bản của video muốn tạo" />
          </div>

          <!-- Tình huống thường dùng -->
          <div class="preset-group">
            <div class="preset-label">Tình huống thường dùng</div>
            <div class="preset-items">
              <div v-for="(item, index) in presetTexts" :key="index" class="preset-item" @click.prevent="handleApplyPreset(item.content)">
                {{ item.title }}
              </div>
            </div>
          </div>
        </div>

        <!-- Nút bắt đầu tạo -->
        <div class="action-btn-group">
          <a-button type="primary" size="large" block :loading="generating" @click="handleGenerate">
            <Icon icon="ant-design:video-camera-outlined" />
            Bắt đầu tạo
          </a-button>
        </div>
      </div>

      <!-- Ở giữa: xem trước video -->
      <div class="preview-panel">
        <div class="panel-title">Khu vực xem trước</div>
        <div class="preview-content">
          <div v-if="!currentVideoUrl && !generating" class="empty-state">
            <Icon icon="ant-design:video-camera-outlined" size="72" color="#c0c4cc" />
            <p>Điền nội dung văn bản bên trái và nhấp「Bắt đầu tạo」</p>
            <p class="tip">Hỗ trợ điều chỉnh độ dài video, phong cách và hiệu ứng, tạo nội dung video cá nhân hóa hơn</p>
          </div>

          <div v-if="generating" class="loading-state">
            <a-spin size="large" />
            <div class="loading-text">
              <p>Đang tạo video, vui lòng kiên nhẫn chờ đợi...</p>
              <p class="elapsed-time">Đã chờ {{ elapsedTimeText }}</p>
              <p class="status-text">{{ statusText }}</p>
            </div>
          </div>

          <div v-if="currentVideoUrl && !generating" class="video-player-wrapper">
            <video ref="videoRef" :src="currentVideoUrl" controls class="video-control" />
          </div>
        </div>
        <!-- Thông tin video đang phát -->
        <div v-if="currentVideoUrl" class="video-info-section">
          <div class="current-video-info">
            <span class="info-label">Video hiện tại:</span>
            <span class="info-text">{{ currentText }}</span>
          </div>
        </div>
      </div>

      <!-- Lịch sử tạo bên phải - kiểu danh sách -->
      <div class="history-panel">
        <div class="panel-title">Lịch sử tạo</div>
        <div class="history-list-wrapper">
          <div v-if="historyList.length === 0" class="empty-history">
            <p>Tạm thời chưa có lịch sử tạo</p>
          </div>
          <div v-else class="history-list">
            <div v-for="(item, index) in historyList" :key="item.id" class="history-item">
              <div class="item-header">
                <span class="item-title" :title="item.content">{{ item.content }}</span>
                <span class="item-time">{{ formatTime(item) }}</span>
              </div>
              <div class="item-actions">
                <a-button type="text" size="small" @click="handlePlay(item)">
                  <Icon icon="ant-design:play-circle-outlined" />
                  Phát
                </a-button>
                <a-button type="text" size="small" @click="handleUseText(item)">
                  <Icon icon="ant-design:copy-outlined" />
                  Tái sử dụng nội dung văn bản
                </a-button>
                <a-button type="text" size="small" @click="handleDownload(item)">
                  <Icon icon="ant-design:download-outlined" />
                  Tải xuống
                </a-button>
                <a-button type="text" size="small" danger @click="handleDelete(item.id)">
                  <Icon icon="ant-design:delete-outlined" />
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { Icon } from '@/components/Icon';
  import { useMessage } from '@/hooks/web/useMessage';
  import { videoFormSchemas } from './AiVideo.data';
  import { submitVideoTask, queryVideoTask, getVideoListByUser, deleteVideoRecord } from './AiVideo.api';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import { useUserStore } from '@/store/modules/user';

  const TASK_ID_KEY = 'ai_video_task_id';

  const { createMessage } = useMessage();
  const userStore = useUserStore();

  // Biểu mẫu bên trái: không bao gồm nội dung văn bản
  const leftFormSchemas = videoFormSchemas.filter((item) => !['text'].includes(item.field));

  const [registerForm, { validate }] = useForm({
    schemas: leftFormSchemas,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  const generating = ref(false);
  const videoRef = ref<HTMLVideoElement | null>(null);
  const currentVideoUrl = ref<string>('');
  const currentText = ref<string>('');
  const formText = ref<string>('');
  const historyList = ref<any[]>([]);
  const isPresetApplying = ref(false);
  const elapsedSeconds = ref(0);
  const statusText = ref('Nhiệm vụ đã gửi, đang xếp hàng...');

  let pollTimer: ReturnType<typeof setInterval> | null = null;
  let elapsedTimer: ReturnType<typeof setInterval> | null = null;

  const elapsedTimeText = computed(() => {
    const minutes = Math.floor(elapsedSeconds.value / 60);
    const seconds = elapsedSeconds.value % 60;
    if (minutes > 0) {
      return `${minutes} phút ${seconds} giây`;
    }
    return `${seconds} giây`;
  });

  const presetTexts = [
    { title: 'Chó Golden trên bãi biển', content: 'Một chú chó Golden chạy trên bãi cát vàng, sóng biển nhẹ nhàng vỗ vào bờ, nắng đẹp, cảnh quay chậm' },
    { title: 'Toàn cảnh dãy núi chụp từ trên không', content: 'Quay từ trên cao toàn cảnh dãy núi hùng vĩ, mây mù bao quanh giữa các đỉnh núi, ống kính tiến chậm về phía trước.' },
    { title: 'Đặc tả macro cà phê', content: 'Một ly cà phê được rót từ từ vào cốc thủy tinh trong suốt, cà phê và sữa hòa quyện tạo thành họa tiết đẹp, cận cảnh macro.' },
    { title: 'Chụp ảnh time-lapse cực quang', content: 'Cực quang dưới bầu trời sao nhảy múa trên không, sắc màu rực rỡ, hiệu ứng chụp timelapse.' },
    {
      title: 'Giảng viên nữ giảng dạy',
      content:
        'Giảng viên nữ, đứng trước PPT cầm que chỉ vào nội dung, biểu cảm nghiêm túc thân thiện, giảng giải tự nhiên trôi chảy, cử chỉ đoan trang, ánh sáng sáng rõ, nền sạch gọn, 1080P độ nét cao, hình ảnh ổn định mượt mà, phù hợp giảng giải kiến thức, dạy học khóa học, phong cách chuyên nghiệp, rõ ràng, có sức thuyết phục',
    },
    {
      title: 'Streamer bán hàng livestream son môi',
      content:
        'Chủ thể hình ảnh: Một nữ streamer Trung Quốc trẻ đẹp, cảnh cận, mỉm cười nhìn vào ống kính.\n' +
        'Ngoại hình trang phục: Cô ấy mặc trang phục công sở sáng màu, trang điểm tinh tế không tì vết.\n' +
        'Động tác cốt lõi: Một tay cô ấy cầm một thỏi son cao cấp, tay còn lại thanh nhã mở nắp, để lộ chất son mượt mà. Cô ấy dịu dàng thoa son lên môi mình, động tác nhẹ nhàng, ánh mắt tập trung và mỉm cười.\n' +
        'Môi trường nền: Phía sau là phòng livestream sáng sủa gọn gàng, có đèn vòng bổ sung ánh sáng, nền có hiệu ứng mờ mềm.\n' +
        'Chất cảm hình ảnh: Độ bão hòa màu cao, bố trí ánh sáng cấp điện ảnh, độ phân giải 4k, 60fps.\n' +
        'Hiệu ứng đặc biệt: Dùng cảnh quay chậm ghi lại chất cảm mượt mà trong khoảnh khắc thoa son.\n'+
        'Phong cách ngôn ngữ: tiếng Trung, phù hợp tình huống livestream bán hàng thương mại điện tử, phong cách chuyên nghiệp, rõ ràng, có sức thuyết phục',
    },
  ];

  /**
   * Tải danh sách video theo id người dùng hiện tại
   */
  async function loadVideoList() {
    try {
      const userId = userStore.getUserInfo?.id;
      if (!userId) return;
      const res = await getVideoListByUser({ userId });
      if (res && res.result) {
        const list = Array.isArray(res.result) ? res.result : res.result?.records || [];
        historyList.value = list.map((item) => ({
          ...item,
          videoFullUrl: item.videoUrl ? getFileAccessHttpUrl(item.videoUrl) : '',
        }));
      }
    } catch (e) {
      // ignore
    }
  }

  // Khi tải trang lấy danh sách lịch sử
  loadVideoList();

  /**
   * Áp dụng nội dung văn bản đặt sẵn
   */
  function handleApplyPreset(text: string) {
    if (isPresetApplying.value) return;
    isPresetApplying.value = true;
    formText.value = text;
    setTimeout(() => {
      isPresetApplying.value = false;
    }, 300);
  }

  /**
   * Xóa tất cả bộ hẹn giờ
   */
  function clearTimers() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
    if (elapsedTimer) {
      clearInterval(elapsedTimer);
      elapsedTimer = null;
    }
  }

  /**
   * Bắt đầu tạo (chế độ thăm dò bất đồng bộ)
   */
  async function handleGenerate() {
    try {
      const values = await validate();

      if (!formText.value.trim()) {
        createMessage.warn('Vui lòng nhập nội dung văn bản');
        return;
      }

      // Đặt lại trạng thái
      generating.value = true;
      currentVideoUrl.value = '';
      elapsedSeconds.value = 0;
      statusText.value = 'Nhiệm vụ đã gửi, đang xếp hàng...';
      values.prompt = formText.value.trim();

      // Khởi động bộ hẹn giờ
      elapsedTimer = setInterval(() => {
        elapsedSeconds.value++;
      }, 1000);

      // Gửi nhiệm vụ
      const submitResult = await submitVideoTask(values);
      if (!submitResult || !submitResult.success || !submitResult.result?.taskId) {
        createMessage.error(submitResult?.message || 'Gửi nhiệm vụ thất bại');
        generating.value = false;
        clearTimers();
        return;
      }

      const taskId = submitResult.result.taskId;
      localStorage.setItem(TASK_ID_KEY, taskId);
      statusText.value = 'Đang tạo video...';

      // Bắt đầu thăm dò
      pollTimer = setInterval(async () => {
        try {
          const queryResult = await queryVideoTask(taskId);
          if (queryResult?.success && queryResult.result) {
            const { status, videoUrl, message } = queryResult.result;
            if (status === 'SUCCESS') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentVideoUrl.value = getFileAccessHttpUrl(videoUrl) || '';
              currentText.value = formText.value;
              createMessage.success('Tạo video thành công!');
              await loadVideoList();
            } else if (status === 'FAIL') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.error(message || 'Tạo video thất bại');
            }
            // Trạng thái PROCESSING tiếp tục thăm dò
          }
        } catch (e: any) {
          clearTimers();
          generating.value = false;
          createMessage.error('Truy vấn trạng thái nhiệm vụ thất bại: ' + (e.message || 'Lỗi không rõ'));
        }
      }, 5000);
    } catch (error: any) {
      clearTimers();
      generating.value = false;
      if (error?.errorFields) {
        return;
      }
      createMessage.error(error.message || 'Gửi nhiệm vụ thất bại');
    }
  }

  /**
   * Phát từ trong lịch sử
   */
  function handlePlay(record: any) {
    currentVideoUrl.value = record.videoFullUrl || getFileAccessHttpUrl(record.videoUrl) || '';
    currentText.value = record.content;
    setTimeout(() => {
      (videoRef.value as any)?.play?.();
    }, 0);
  }

  /**
   * Điền lại nội dung văn bản của lịch sử vào ô nhập nội dung văn bản
   */
  function handleUseText(record: any) {
    formText.value = record.content;
    createMessage.success('Đã điền nội dung văn bản vào ô nhập');
  }

  /**
   * Tải xuống tệp video
   */
  function handleDownload(record: any) {
    const url = record.videoFullUrl || getFileAccessHttpUrl(record.videoUrl);
    if (!url) {
      createMessage.error('Địa chỉ tải xuống không tồn tại');
      return;
    }
    const link = document.createElement('a');
    link.href = url;
    link.download = record.fileName || `video-${Date.now()}.mp4`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Xóa lịch sử
   */
  async function handleDelete(id: string) {
    const userId = userStore.getUserInfo?.id;
    if (!userId) return;
    try {
      const res = await deleteVideoRecord({ userId: userId, recordId: id });
      if (res.success) {
        createMessage.success('Đã xóa');
        await loadVideoList();
      } else {
        createMessage.error(res.message || 'Xóa thất bại');
      }
    } catch (e) {
      createMessage.error('Xóa thất bại');
    }
  }

  /**
   * Hiển thị thời gian đã định dạng
   */
  function formatTime(item: any) {
    return item?.createTime || '';
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      generating.value = true;
      currentVideoUrl.value = '';
      elapsedSeconds.value = 0;
      statusText.value = 'Nhiệm vụ đang khôi phục, tiếp tục chờ đợi...';
      elapsedTimer = setInterval(() => {
        elapsedSeconds.value++;
      }, 1000);
      pollTimer = setInterval(async () => {
        try {
          const queryResult = await queryVideoTask(savedTaskId);
          if (queryResult?.success && queryResult.result) {
            const { status, videoUrl, message } = queryResult.result;
            if (status === 'SUCCESS') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentVideoUrl.value = getFileAccessHttpUrl(videoUrl) || '';
              createMessage.success('Tạo video thành công!');
              await loadVideoList();
            } else if (status === 'FAIL') {
              clearTimers();
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              createMessage.error(message || 'Tạo video thất bại');
            }
          }
        } catch (e: any) {
          clearTimers();
          generating.value = false;
          createMessage.error('Truy vấn trạng thái nhiệm vụ thất bại: ' + (e.message || 'Lỗi không rõ'));
        }
      }, 5000);
    }
  });
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  onUnmounted(() => {
    clearTimers();
  });
</script>

<style lang="less" scoped>
  @import './AiVideo.less';
</style>
