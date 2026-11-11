<template>
  <BasicDrawer v-bind="getProps">
    <div class="dynamic-column-config">
      <!-- Danh sách cấu hình cột -->
      <div class="column-list">
        <!-- Đã loại bỏ component draggable, dùng trực tiếp div để hiển thị danh sách -->
        <div v-for="(element, index) in dataSet" :key="index" class="column-item">
          <!-- Khu vực thao tác - đặt ở góc trên phải -->
          <div class="action-area">
            <a-button type="text" danger @click="handleRemoveData(index)" title="Xóa">
              <DeleteOutlined />
            </a-button>
          </div>
          <!-- Biểu mẫu cấu hình cột -->
          <div class="column-form">
            <div class="form-row">
              <div class="form-item" v-for="item in columns" :key="item.name">
                <label class="form-label">{{ item.name }}</label>
                <!-- Ô nhập tệp đính kèm -->
                <JImageUpload :maxCount="1" v-if="item.dataType === 'FILE'" v-model:value="element[item.name]"></JImageUpload>
                <a-input
                    v-else
                    v-model:value="element[item.name]"
                    :placeholder="'Vui lòng nhập' + item.name"
                    :class="{ 'has-error': !element[item.name] }"
                    @blur="validateColumn(element[item.name])"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      <a-button v-if="!isUpdate" type="dashed" style="width: 100%" @click="handleAddData"> <PlusOutlined />Thêm dữ liệu </a-button>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import type { DrawerProps } from '/@/components/Drawer';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { computed, unref, ref } from 'vue';
  import { useAttrs } from '@/hooks/core/useAttrs';
  import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { saveOrUpdate } from '@/views/super/airag/aiprompts/AiragExtData.api';
  import { message } from 'ant-design-vue';
  import {JImageUpload} from "@/components/Form";
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  const attrs = useAttrs();
  // Gợi ý tin nhắn
  const { createMessage } = useMessage();
  // Đăng ký ngăn kéo
  const [registerDrawer, { closeDrawer }] = useDrawerInner(open);
  // props cuối cùng của ngăn kéo
  const getProps = computed(() => {
    let drawerProps: Partial<DrawerProps> = {
      width: 1000,
      title: 'Cấu hình dữ liệu',
      showFooter: true,
      destroyOnClose: true,
    };
    let finalProps: Recordable = {
      ...unref(attrs),
      ...drawerProps,
      okText: 'Lưu',
      onOk: handleSubmit,
      onCancel: closeDrawer,
      onRegister: registerDrawer,
    };
    return finalProps;
  });
  /** Cập nhật trạng thái */
  const isUpdate = ref<boolean>(false);
  /** dataId */
  const dataId = ref<any>('');
  /** Danh sách dữ liệu */
  const allData = ref<any>([]);
  /** Danh sách dữ liệu */
  const dataSet = ref<any>([]);
  // Cấu hình cột
  const columns = ref<any>([]);
  // Kiểm chứng dữ liệu không trống
  const validateColumn = (value) => {
    return value && value.trim() !== '';
  };
  // Tạo ID duy nhất
  function generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }
  //Thêm dữ liệu
  function handleAddData() {
    dataSet.value.push({
      id: generateId(),
    });
  }
  //Lưu dữ liệu
  async function handleSubmit() {
    // Kiểm chứng dữ liệu
    const invalidData = dataSet.value.filter((item) => {
      return columns.value.some((col) => col.required && !validateColumn(item[col.name]));
    });
    if (invalidData.length > 0) {
      createMessage.error('Vui lòng điền tất cả mục bắt buộc (tên)');
      return;
    }

    try {
      // Cấu trúc dữ liệu gửi
      let dataSource:any = [];
      if (isUpdate.value) {
        dataSource = allData.value.map(item =>
            item.id === dataSet.value[0].id ? dataSet.value[0] : item
        );
      } else {
        dataSource = allData.value.concat(dataSet.value);
      }
      const submitData = {
        datasetValue: JSON.stringify({
          columns: columns.value,
          dataSource: dataSource,
        }),
        id: dataId.value,
      };

      console.log('Dữ liệu gửi:', submitData);

      await saveOrUpdate(submitData, true, false);

      message.success('Lưu dữ liệu thành công!');
      // Đóng popup và kích hoạt sự kiện thành công
      closeDrawer();
      emit('success');
    } catch (error) {
      console.error('Gửi thất bại:', error);
    } finally {
      //loading.value = false;
    }
  }
  // Xóa cột
  const handleRemoveData = (index: number) => {
    dataSet.value.splice(index, 1);
  };
  /** Ngăn kéo mở */
  function open(data) {
    isUpdate.value = data.isUpdate;
    dataId.value = data?.id || '';
    allData.value = data?.dataSource || [];
    columns.value = data?.columns || [];
    console.log("columns.value",columns.value)
    if (isUpdate.value) {
      console.log("data?.record",data?.record)
      dataSet.value = data?.record?[data?.record] : [];
    } else {
      dataSet.value = [];
    }
  }
</script>

<style scoped lang="less">
  .dynamic-column-config {
    max-width: 1200px;
    margin: 0 auto;
    max-height: calc(100vh - 100px); /* Đặt chiều cao tối đa là 70 của chiều cao viewport% */
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
    flex-direction: column;
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
