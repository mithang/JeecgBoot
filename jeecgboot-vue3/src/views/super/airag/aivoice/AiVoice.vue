<template>
  <div class="ai-voice-page">
    <!-- Khu vực đầu -->
    <div class="page-header">
      <div class="title">AI Giọng nói</div>
      <div class="subtitle">Chuyển đổi nhanh văn bản thành giọng nói tự nhiên trôi chảy</div>
    </div>

    <div class="content-wrapper">
      <!-- Điều khiển giọng nói bên trái -->
      <div class="control-panel">
        <div class="panel-title">Điều khiển giọng nói</div>
        <div class="form-container">
          <BasicForm @register="registerForm" :schemas="leftFormSchemas" />
        </div>
      </div>

      <!-- Khu vực giữa: nghe thử + Nội dung văn bản + Tình huống thường dùng + Nút tạo -->
      <div class="middle-wrapper">
        <!-- Khu vực nghe thử -->
        <div class="preview-panel">
          <div class="panel-title">Khu vực nghe thử</div>
          <div class="preview-content">
            <div v-if="!currentAudioUrl" class="empty-state">
              <Icon icon="ant-design:customer-service-outlined" size="72" color="#c0c4cc" />
              <p>Điền nội dung văn bản phía dưới và nhấp「Bắt đầu tổng hợp」</p>
              <p class="tip">Hỗ trợ điều chỉnh tốc độ phát, độ tăng âm lượng và âm sắc, tạo hiệu quả giọng nói cá nhân hóa hơn</p>
            </div>

            <div v-else class="audio-player-wrapper">
              <audio ref="audioRef" :src="currentAudioUrl" controls class="audio-control" />
            </div>
          </div>
          <!-- Thông tin âm thanh đang phát -->
          <div v-if="currentAudioUrl" class="audio-info-section">
            <div class="current-audio-info">
              <span class="info-label">Giọng nói hiện tại:</span>
              <span class="info-text">{{ currentText }}</span>
            </div>
          </div>
        </div>

        <!-- Nhập nội dung văn bản và tình huống thường dùng -->
        <div class="input-section">
          <!-- Ô nhập nội dung văn bản -->
          <div class="form-item-group">
            <label class="form-label">Nội dung văn bản</label>
            <a-textarea v-model:value="formText" :rows="4" :maxlength="500" show-count placeholder="Vui lòng nhập nội dung văn bản muốn tổng hợp" />
          </div>

          <!-- Tình huống thường dùng -->
          <div class="preset-group">
            <div class="preset-label">Tình huống thường dùng</div>
            <div class="preset-items">
              <div v-for="item in presetTexts" :key="item" class="preset-item" @click.prevent="handleApplyPreset(item)">
                {{ item }}
              </div>
            </div>
          </div>

          <!-- Nút bắt đầu tổng hợp -->
          <div class="action-btn-group">
            <a-button type="primary" size="large" block :loading="generating" @click="handleSynthesize">
              <Icon icon="ant-design:sound-outlined" />
              Bắt đầu tổng hợp
            </a-button>
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
            <div v-for="item in historyList" :key="item.id" class="history-item">
              <div class="item-header">
                <span class="item-title" :title="item.content">{{ item.content }}</span>
                <span class="item-time">{{ item.createTime }}</span>
              </div>
              <div class="item-actions">
                <a-button type="text" size="small" @click="handlePlay(item)">
                  <Icon icon="ant-design:sound-outlined" />
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
  import { ref, onMounted, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { Icon } from '@/components/Icon';
  import { useMessage } from '@/hooks/web/useMessage';
  import { voiceFormSchemas } from './AiVoice.data';
  import { generateVoiceAsync, queryVoiceTask, getVoiceListByUser, deleteVoiceRecord } from '@/views/super/airag/aivoice/AiVoice.api';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import { useUserStore } from '@/store/modules/user';

  const TASK_ID_KEY = 'ai_voice_task_id';

  const { createMessage } = useMessage();
  const userStore = useUserStore();

  // Biểu mẫu bên trái: chỉ bao gồm mô hình, tốc độ phát, âm lượng, âm sắc, không bao gồm nội dung văn bản
  const leftFormSchemas = voiceFormSchemas.filter((item) => !['text'].includes(item.field));

  const [registerForm, { validate }] = useForm({
    schemas: leftFormSchemas,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  const generating = ref(false);
  const audioRef = ref<HTMLAudioElement | null>(null);
  const currentAudioUrl = ref<string>('');
  const currentText = ref<string>('');
  const formText = ref<string>(''); // Trạng thái độc lập của ô nhập nội dung văn bản
  const historyList = ref<any[]>([]);
  const isPresetApplying = ref(false); // Cờ chống rung
  let pollTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * Tải danh sách giọng nói theo id người dùng hiện tại
   */
  async function loadVoiceList() {
    try {
      const userId = userStore.getUserInfo?.id;
      if (!userId) {
        return;
      }
      await getVoiceListByUser({ userId }).then((res) =>{
        if(res && res.result){
          for (const rs of res.result) {
              if (rs.voiceUrl) {
                rs.audioUrl = getFileAccessHttpUrl(rs.voiceUrl) || '';
              }
          }
          historyList.value = res.result || [];
        }
      });
    } catch (e) {
     
    }
  }

  // Khi tải trang lấy danh sách lịch sử
  loadVoiceList();

  const presetTexts = [
    'Chào mừng đến với nền tảng của chúng tôi, chúc bạn sử dụng vui vẻ!',
    'Thời tiết hôm nay rất đẹp, phù hợp ra ngoài đi dạo.',
    'Kính gửi quý khách, đơn hàng của quý khách đã được giao đi, vui lòng chú ý nhận hàng.',
    'Welcome to our platform, we hope you enjoy the experience!',
  ];

  /**
   * Áp dụng nội dung văn bản đặt sẵn
   */
  function handleApplyPreset(text: string) {
    if (isPresetApplying.value) return;

    isPresetApplying.value = true;
    formText.value = text;

    // Sau 300ms hủy cờ chống rung
    setTimeout(() => {
      isPresetApplying.value = false;
    }, 300);
  }

  /**
   * Bắt đầu tổng hợp (chế độ thăm dò bất đồng bộ)
   */
  async function handleSynthesize() {
    try {
      const values = await validate();

      if (!formText.value.trim()) {
        createMessage.warn('Vui lòng nhập nội dung văn bản');
        return;
      }

      generating.value = true;
      currentAudioUrl.value = '';
      values.content = formText.value.trim();

     //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
      const res = await generateVoiceAsync(values);
      if (res.success && res.result) {
        const taskId = res.result as string;
        localStorage.setItem(TASK_ID_KEY, taskId);
        startPolling(taskId);
      } else {
        generating.value = false;
        createMessage.error(res.message || 'Gửi nhiệm vụ thất bại');
      }
    } catch (e) {
      generating.value = false;
    }
  }

  /** Thăm dò truy vấn kết quả nhiệm vụ giọng nói */
  function startPolling(taskId: string) {
    const poll = () => {
      queryVoiceTask(taskId)
  //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
        .then((res) => {
          if (res.success) {
            //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
            if (res.result === 'pending' || res.result === null) {
              pollTimer = setTimeout(poll, 3000);
            } else {
              generating.value = false;
              localStorage.removeItem(TASK_ID_KEY);
              currentAudioUrl.value = getFileAccessHttpUrl(res.result.voiceUrl) || '';
              currentText.value = formText.value;
              createMessage.success('Tổng hợp giọng nói hoàn thành');
              loadVoiceList();
            }
          } else {
            generating.value = false;
            localStorage.removeItem(TASK_ID_KEY);
            createMessage.error(res.message || 'Tổng hợp giọng nói thất bại');
          }
        })
        .catch(() => {
          pollTimer = setTimeout(poll, 3000);
        });
    };
    poll();
    //update-end---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  }

  //update-begin---wangshuai---date:20260415  for：[QQYUN-14944]AI đổi thành bất đồng bộ, hỗ trợ chuyển menu------------
  onMounted(() => {
    const savedTaskId = localStorage.getItem(TASK_ID_KEY);
    if (savedTaskId) {
      generating.value = true;
      currentAudioUrl.value = '';
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
   * Phát từ trong lịch sử
   */
  function handlePlay(record: any) {
    currentAudioUrl.value = getFileAccessHttpUrl(record.voiceUrl) || '';
    currentText.value = record.content;
    setTimeout(() => {
      (audioRef.value as any)?.play?.();
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
   * Tải xuống tệp giọng nói
   */
  function handleDownload(record: any) {
    const url = getFileAccessHttpUrl(record.voiceUrl);
    if (!url) {
      createMessage.error('Địa chỉ tải xuống không tồn tại');
      return;
    }
    const link = document.createElement('a');
    link.href = url;
    link.download = record.fileName || 'voice.wav';
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
      const res = await deleteVoiceRecord({ userId: userId, recordId: id });
      if (res.success) {
        createMessage.success('Đã xóa');
        loadVoiceList();
      } else {
        createMessage.error(res.message || 'Xóa thất bại');
      }
    } catch (e) {
      createMessage.error('Xóa thất bại');
    }
  }
</script>

<style lang="less" scoped>
  @import './AiVoice.less';
</style>
