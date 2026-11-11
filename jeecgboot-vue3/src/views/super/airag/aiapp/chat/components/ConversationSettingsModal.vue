<template>
  <a-modal
    v-model:open="visible"
    title="Cài đặt hội thoại"
    :width="600"
    :maskClosable="false"
    :keyboard="false"
    @ok="handleOk"
    okText="Bắt đầu hội thoại"
    :cancelButtonProps="{ style: { display: 'none' } }"
    :okButtonProps="{ disabled: !canSubmit }"
  >
    <div class="settings-content">
      <a-form :model="formData" layout="vertical">
        <a-form-item
          v-for="input in flowInputs"
          :key="input.field"
          :label="input.name"
          :required="input.required"
        >
          <!-- Loại văn bản - dùng nhập một dòng -->
          <a-input
            v-if="input.type === 'string'"
            v-model:value="formData[input.field]"
            :placeholder="`Vui lòng nhập${input.name}`"
            :maxlength="input.maxLength"
            show-count
          />
          <!-- Loại số -->
          <a-input-number
            v-else-if="input.type === 'number'"
            v-model:value="formData[input.field]"
            :placeholder="`Vui lòng nhập${input.name}`"
            style="width: 100%"
            :min="input.min"
            :max="input.max"
          />
          
            <!-- Loại hình ảnh - dùng kiểu tải lên tương tự chat.vue, cố định hỗ trợ 3 hình ảnh -->
          <div v-else-if="input.type === 'picture'" class="image-upload-container">
            <div class="image-list-wrapper">
              <!-- Hình ảnh đã tải lên -->
              <div v-for="(img, idx) in imageFileList[input.field]" :key="idx" class="image-preview-item">
                <img :src="getImageUrl(img)" @click="handlePreview(img)" />
                <div class="image-remove-icon" @click="handleRemove(idx, input.field)">
                  <Icon icon="ant-design:close-outlined" :size="12" />
                </div>
              </div>
              <!-- Nút tải lên -->
              <a-upload
                v-if="(imageFileList[input.field]?.length || 0) < 3"
                accept=".jpg,.jpeg,.png"
                name="file"
                :showUploadList="false"
                :headers="uploadHeaders"
                :beforeUpload="(file) => beforeUpload(file, input.field)"
                @change="(info) => handleChange(info, input.field)"
                :multiple="true"
                :action="uploadUrl"
                :max-count="3"
              >
                <div class="upload-trigger">
                  <Icon icon="ant-design:plus-outlined" :size="20" />
                  <div class="upload-text">Tải lên hình ảnh</div>
                </div>
              </a-upload>
            </div>
          </div>
          <!-- Loại mảng chuỗi (ví dụ history) -->
          <a-select
            v-else-if="input.type === 'string[]'"
            v-model:value="formData[input.field]"
            mode="tags"
            :placeholder="`Vui lòng nhập${input.name}`"
            style="width: 100%"
          />
          <!-- Loại tệp -->
          <JFileUploadSlot
            v-else-if="input.type === 'file'"
            :value="formData[input.field] || ''"
            :uploadUrl="uploadUrl"
            :accept="input.accept"
            :useAuthHeaders="useAuthHeaders"
            @update:value="(val) => updateFormField(input.field, val)"
          />
          <!-- Loại khác dùng nhập văn bản -->
          <a-input
            v-else
            v-model:value="formData[input.field]"
            :placeholder="`Vui lòng nhập${input.name}`"
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { message } from 'ant-design-vue';
  import { Icon } from '/@/components/Icon';
  import { getFileAccessHttpUrl, getHeaders } from '@/utils/common/compUtils';
  import { useGlobSetting } from '@/hooks/setting';
  import { JFileUploadSlot } from '@jeecg/aiflow';

  const props = defineProps<{
    flowInputs: any[];
    conversationId: string;
    existingSettings?: Record<string, any>;
    appId?: string;
    shareToken?: string;
  }>();

  const emit = defineEmits<{
    (e: 'ok', data: Record<string, any>): void;
    (e: 'cancel'): void;
  }>();

  const visible = ref(false);
  const formData = ref<Record<string, any>>({});
  const imageFileList = ref<Record<string, any[]>>({});

  const globSetting = useGlobSetting();
  const baseUploadUrl = globSetting.uploadUrl;
  // update-begin--author:wangshuai---date:20260820---for：【LHZP-1627】Cần truyền token và shareToken
  const useAuthHeaders = computed(() => !props.shareToken);
  const uploadHeaders = computed(() => (useAuthHeaders.value ? getHeaders() : {}));
  const uploadUrl = computed(() => {
    const url = `${baseUploadUrl}/airag/chat/upload`;
    if (!props.appId) {
      return url;
    }
    const params = new URLSearchParams({ appId: props.appId });
    if (props.shareToken) {
      params.set('shareToken', props.shareToken);
    }
    return `${url}?${params.toString()}`;
  });
  // update-end--author:wangshuai---date:20260820---for：【LHZP-1627】Cần truyền token và shareToken

  // Lọc bỏ tham số cố định (history, content, images)
  const flowInputs = computed(() => {
    const fixedParams = ['history', 'content', 'images'];
    return props.flowInputs?.filter((input) => !fixedParams.includes(input.field)) || [];
  });

  // Kiểm tra có thể gửi hay không (các mục bắt buộc đều đã điền)
  const canSubmit = computed(() => {
    if (flowInputs.value.length === 0) return true;
    return flowInputs.value.every((input) => {
      if (!input.required) return true;
    // update-begin--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
      return hasFieldValue(input);
    });
  });

  /**
   * Phán đoán tham số đầu vào của quy trình đã có giá trị hợp lệ chưa, dùng cho kiểm tra bắt buộc và trạng thái nút “Bắt đầu trò chuyện”.
   * Hình ảnh kiểm tra danh sách tải lên, tệp kiểm tra địa chỉ trả về sau khi tải lên, các mảng khác và giá trị thường phán đoán riêng.
   *
   * @param input cấu hình tham số đầu vào quy trình
   */
  function hasFieldValue(input: any): boolean {
    const value = formData.value[input.field];
    if (input.type === 'picture') {
      const list = imageFileList.value[input.field];
      return Array.isArray(list) && list.length > 0;
    }
    if (input.type === 'file') {
      return typeof value === 'string' && value.trim() !== '';
    }
    if (Array.isArray(value)) {
      return value.length > 0;
    }
    return value !== undefined && value !== null && value !== '';
  }

  /**
   * Cập nhật tham số đầu vào của quy trình chỉ định, và thông qua tạo đối tượng mới kích hoạt cập nhật reactive của Vue.
   * Loại tệp lưu địa chỉ tệp do giao diện tải lên trả về, loại hình ảnh lưu mảng địa chỉ tệp.
   *
   * @param field tên trường tham số đầu vào quy trình
   * @param value giá trị tham số đầu vào quy trình
   */
  function updateFormField(field: string, value: any) {
    formData.value = { ...formData.value, [field]: value };
  }
  // update-end--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên

  // Xử lý trước khi tải lên
  const beforeUpload = (file: any, field: string) => {
    const isImage = file.type?.startsWith('image/');
    if (!isImage) {
      message.error('Chỉ được tải lên tệp hình ảnh!');
      return false;
    }
    
    const currentCount = imageFileList.value[field]?.length || 0;
    
    if (currentCount >= 3) {
      message.warning('Tối đa chỉ được tải lên 3 hình ảnh!');
      return false;
    }
    
    return true;
  };

  // Xử lý thay đổi trạng thái tải lên
  const handleChange = (info: any, field: string) => {
    const { file } = info;
    
    if (file.status === 'error' || (file.response && file.response.code === 500)) {
      message.error(file.response?.message || `${file.name} Tải lên thất bại`);
      return;
    }
    
    if (file.status === 'done' && file.response) {
      const imageUrl = file.response.message;
      if (!imageFileList.value[field]) {
        imageFileList.value[field] = [];
      }
      
      // Kiểm tra đã đạt giới hạn trên hay chưa
      if (imageFileList.value[field].length >= 3) {
        message.warning('Tối đa chỉ được tải lên 3 hình ảnh!');
        return;
      }
      
      imageFileList.value[field].push(imageUrl);
      
      // Loại hình ảnh luôn lưu dưới dạng mảng, kích hoạt cập nhật reactive
      imageFileList.value = { ...imageFileList.value };
      // update-begin--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
      updateFormField(field, [...imageFileList.value[field]]);
      // update-end--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
    }
  };

  // Lấy URL hình ảnh
  const getImageUrl = (img: any) => {
    if (typeof img === 'string') {
      return getFileAccessHttpUrl(img);
    }
    return getFileAccessHttpUrl(img.url || img);
  };

  // Xem trước hình ảnh
  const handlePreview = (img: any) => {
    const url = typeof img === 'string' ? img : (img.url || img);
    const imageUrl = getFileAccessHttpUrl(url);
    // Có thể dùng chức năng xem trước Image của ant-design-vue
    window.open(imageUrl, '_blank');
  };

  // Loại bỏ hình ảnh
  const handleRemove = (index: number, field: string) => {
    if (imageFileList.value[field]) {
      imageFileList.value[field].splice(index, 1);
      
      // Kích hoạt cập nhật reactive
      imageFileList.value = { ...imageFileList.value };

      // update-begin--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
      updateFormField(
        field,
        imageFileList.value[field].length > 0 ? [...imageFileList.value[field]] : [],
      );
      // update-end--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
    }
  };

  // Mở popup
  const open = () => {
    visible.value = true;
    // Khởi tạo dữ liệu biểu mẫu
    formData.value = {};
    imageFileList.value = {};
    
    // Nếu có cài đặt đã tồn tại, điền vào biểu mẫu
    if (props.existingSettings && Object.keys(props.existingSettings).length > 0) {
      Object.keys(props.existingSettings).forEach((key) => {
        const input = props.flowInputs.find((i) => i.field === key);
        if (input) {
          if (input.type === 'picture') {
            // Đảm bảo là định dạng mảng
            const urls = Array.isArray(props.existingSettings![key]) 
              ? props.existingSettings![key] 
              : (props.existingSettings![key] ? [props.existingSettings![key]] : []);
            // update-begin--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
            imageFileList.value[key] = urls.filter((url) => url);
            updateFormField(key, [...imageFileList.value[key]]);
          } else if (input.type === 'file') {
            updateFormField(key, props.existingSettings![key] || '');
          } else {
            updateFormField(key, props.existingSettings![key]);
            // update-end--author:wangshuai---date:20260820---for：【LHZP-1627】aiFile quy trình AI là bắt buộc, nhưng trong trò chuyện AI là nhập file chứ không phải tải file lên
          }
        }
      });
    }
  };

  // Xác định
  const handleOk = async () => {
    if (!canSubmit.value) {
      message.warning('Vui lòng điền tất cả mục bắt buộc');
      return;
    }
    
    // Xây dựng dữ liệu cuối cùng
    const result: Record<string, any> = {};
    flowInputs.value.forEach((input) => {
      const value = formData.value[input.field];
      if (value !== undefined && value !== null && value !== '') {
        result[input.field] = value;
      }
    });
    
    // Lưu trước, rồi kích hoạt sự kiện
    // Trả về trực tiếp dữ liệu cài đặt qua emit, không cần lưu riêng
    // Cài đặt sẽ tự động lưu vào backend khi gửi tin nhắn
    emit('ok', result);
    visible.value = false;
  };

  // Lộ ra phương thức
  defineExpose({
    open,
  });
</script>

<style scoped lang="less">
  .settings-content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 10px 5px;
    
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }
    
    :deep(.ant-form-item-label) {
      padding: 0 5px;
    }
    
    :deep(.ant-input),
    :deep(.ant-input-number),
    :deep(.ant-select) {
      margin: 0 5px;
      width: calc(100% - 10px) !important;
    }
    
    :deep(.ant-upload) {
      margin: 0 5px;
    }
  }
  
  .image-upload-container {
    margin: 0 5px;
    
    .image-list-wrapper {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
    }
    
    .image-preview-item {
      position: relative;
      width: 80px;
      height: 80px;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #d9d9d9;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        cursor: pointer;
      }
      
      .image-remove-icon {
        position: absolute;
        top: 4px;
        right: 4px;
        background-color: rgba(0, 0, 0, 0.6);
        color: white;
        border-radius: 50%;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s;
        
        &:hover {
          background-color: rgba(0, 0, 0, 0.8);
        }
      }
      
      &:hover .image-remove-icon {
        opacity: 1;
      }
    }
    
    .upload-trigger {
      width: 80px;
      height: 80px;
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      background-color: #fafafa;
      flex-shrink: 0;
      
      &:hover {
        border-color: #1890ff;
        color: #1890ff;
      }
      
      .upload-text {
        margin-top: 4px;
        font-size: 12px;
        color: rgba(0, 0, 0, 0.65);
      }
      
      &:hover .upload-text {
        color: #1890ff;
      }
    }
  }
</style>

