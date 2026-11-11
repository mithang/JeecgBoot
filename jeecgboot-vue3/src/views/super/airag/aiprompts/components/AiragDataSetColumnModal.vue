<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :canFullscreen="true"
    destroyOnClose
    title="Cấu hình cột"
    okText="Lưu"
    :width="1000"
    @ok="handleSubmit"
  >
    <div class="dynamic-column-config">
      <a-card size="small">
        <!-- Danh sách cấu hình cột -->
        <div class="column-list">
          <div v-for="(element, index) in columns" :key="element.id" class="column-item">
            <!-- Khu vực thao tác - đặt ở góc trên phải -->
            <div class="action-area">
              <a-button type="text" @click="handleCopyColumn(element)" title="Sao chép">
                <copy-outlined />
              </a-button>
              <a-button type="text" danger @click="handleRemoveColumn(index)" :disabled="columns.length < 2" title="Xóa">
                <DeleteOutlined />
              </a-button>
            </div>

            <!-- Biểu mẫu cấu hình cột -->
            <div class="column-form">
              <div class="form-row">
                <div class="form-item">
                  <label class="form-label">Tên</label>
                  <a-input
                    v-model:value="element.name"
                    placeholder="Vui lòng nhập tên cột"
                    :class="{ 'has-error': !element.name }"
                    @blur="validateColumn(element)"
                  />
                </div>

                <div class="form-item">
                  <label class="form-label">Loại dữ liệu</label>
                  <a-select v-model:value="element.dataType" placeholder="Vui lòng chọn loại dữ liệu" style="width: 100%">
                    <a-select-option value="String">Chuỗi</a-select-option>
                    <a-select-option value="FILE">Tệp đính kèm</a-select-option>
                  </a-select>
                </div>

                <div class="form-item">
                  <label class="form-label">Bắt buộc</label>
                  <a-radio-group v-model:value="element.required">
                    <a-radio :value="true">Là</a-radio>
                    <a-radio :value="false">Không</a-radio>
                  </a-radio-group>
                </div>
              </div>

              <div class="form-row">
                <div class="form-item full-width">
                  <label class="form-label">Mô tả</label>
                  <a-textarea v-model:value="element.description" placeholder="Vui lòng nhập mô tả cột" :rows="2" />
                </div>
              </div>
            </div>
          </div>
          <a-button type="dashed" style="width: 100%" @click="handleAddColumn"> <PlusOutlined />Thêm cột </a-button>
        </div>
        <!-- Nút thao tác ở đáy -->
        <!--        <div class="footer-actions">-->
        <!--          <a-button type="primary" @click="handleSubmit" :loading="loading"> Tạo </a-button>-->
        <!--          <a-button @click="handleReset" style="margin-left: 8px"> Đặt lại </a-button>-->
        <!--        </div>-->
      </a-card>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { message } from 'ant-design-vue';
  import { PlusOutlined, DeleteOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { saveOrUpdate } from '../AiragExtData.api';
  import {JImageUpload} from "@/components/Form";
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  // Giao diện cấu hình cột
  interface ColumnConfig {
    id: string;
    name: string;
    description: string;
    dataType: string;
    required: boolean;
  }

  // Dữ liệu responsive
  const loading = ref(false);
  const columns = ref<ColumnConfig[]>([
    {
      id: generateId(),
      name: '',
      description: '',
      dataType: 'String',
      required: false,
    },
  ]);
  const defaultColumns = [
    {
      id: generateId(),
      name: 'input',
      description: 'Đưa làm đầu vào cho đối tượng đánh giá',
      dataType: 'String',
      required: false,
    },
    {
      id: generateId(),
      name: 'reference_output',
      description: 'Đầu ra lý tưởng kỳ vọng, có thể dùng làm tiêu chuẩn tham khảo khi đánh giá',
      dataType: 'String',
      required: false,
    },
  ];
  //ID dữ liệu
  const dataId = ref('');
  const datasetValue = ref<any>({});
  // Tạo ID duy nhất
  function generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }
  //Gán giá trị biểu mẫu
  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    console.log('data:', data);
    dataId.value = data.id;
    if(data.datasetValue){
      datasetValue.value = data.datasetValue
      if(data.datasetValue?.columns && data.datasetValue.columns.length){
        columns.value = data.datasetValue.columns;
      }else{
        columns.value = defaultColumns;
      }
    }
  });
  // Thêm cột
  const handleAddColumn = () => {
    columns.value.push({
      id: generateId(),
      name: '',
      description: '',
      dataType: 'String',
      required: false,
    });
  };

  // Xóa cột
  const handleRemoveColumn = (index: number) => {
    if (columns.value.length > 1) {
      columns.value.splice(index, 1);
    }
  };
  // Sao chép cột
  const handleCopyColumn = (element) => {
    columns.value.push({
      ...element,
      id: generateId(),
    });
  };

  // Kiểm chứng cấu hình cột
  const validateColumn = (column: ColumnConfig) => {
    return column.name && column.name.trim() !== '';
  };

  // Gửi cấu hình
  const handleSubmit = async () => {
    // Kiểm chứng dữ liệu
    const invalidColumns = columns.value.filter((col) => !validateColumn(col));

    if (invalidColumns.length > 0) {
      message.error('Vui lòng điền tất cả mục bắt buộc (tên)');
      return;
    }

    loading.value = true;

    try {
      datasetValue.value.columns = columns.value
      // Cấu trúc dữ liệu gửi
      const submitData = {
        datasetValue: JSON.stringify( datasetValue.value),
        id: dataId.value,
      };

      console.log('Dữ liệu gửi:', submitData);

      await saveOrUpdate(submitData, true,false);

      message.success('Tạo cấu hình thành công!');
      // Đóng popup và kích hoạt sự kiện thành công
      closeModal();
      emit('success');
    } catch (error) {
      console.error('Gửi thất bại:', error);
    } finally {
      loading.value = false;
    }
  };

  // Đặt lại cấu hình
  const handleReset = () => {
    columns.value = [...defaultColumns];
  };
</script>

<style lang="less" scoped>
  .dynamic-column-config {
    max-width: 1200px;
    margin: 0 auto;
    max-height: 70vh; /* Đặt chiều cao tối đa là 70 của chiều cao viewport% */
    overflow-y: auto; /* Khi vượt quá hiển thị thanh cuộn dọc */

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-card-body) {
      padding: 12px;
    }

    :deep(.ant-card-extra) {
      padding: 0;
    }
  }

  .column-list {
    margin-bottom: 24px;
  }

  .column-item {
    position: relative; /* Cung cấp tham chiếu cho khu vực thao tác định vị tuyệt đối */
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    margin-bottom: 16px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    transition: all 0.3s;

    &:hover {
      border-color: #1890ff;
      background: #f6fbff;
    }

    /* Kiểu khu vực thao tác */
    .action-area {
      position: absolute;
      top: 8px;
      right: 8px;

      :deep(.ant-btn) {
        width: 24px;
        height: 24px;
        min-width: 24px;
        padding: 0;

        .anticon {
          font-size: 14px;
        }
      }
    }
  }

  .column-form {
    flex: 1;
    width: 100%; /* Đảm bảo biểu mẫu chiếm hết độ rộng */
  }

  .form-row {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .form-item {
    flex: 1;

    &.full-width {
      flex: 3;
    }
  }

  .form-label {
    display: block;
    margin-bottom: 6px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);

    &::after {
      content: '：';
    }
  }

  .error-text {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
  }

  .has-error {
    :deep(.ant-input) {
      border-color: #ff4d4f;
    }
  }

  .form-actions {
    display: none; /* Ẩn khu vực nút xóa ban đầu */
  }

  .fixed-columns {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px dashed #d9d9d9;

    .tip-text {
      margin-bottom: 16px;
      color: #666;
      font-size: 14px;
    }
  }

  .fixed-column-item {
    margin-bottom: 16px;
    padding: 16px;
    background: #f5f5f5;
    border: 1px solid #e8e8e8;
    border-radius: 6px;

    &:last-child {
      margin-bottom: 0;
    }

    :deep(.ant-input-group-addon) {
      background: #e6f7ff;
      color: #1890ff;
      font-weight: 500;
    }
  }

  .footer-actions {
    display: flex;
    justify-content: center;
    margin-top: 32px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    position: sticky;
    bottom: 0;
    background: #fff;
    padding-bottom: 8px;
  }
</style>
