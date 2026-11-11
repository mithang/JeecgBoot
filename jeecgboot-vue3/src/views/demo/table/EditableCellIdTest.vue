<template>
  <PageWrapper title="EditableCell IDKiểm thử trường (Issue #8924)">
    <a-alert
      message="🧪 Mục đích kiểm thử: Xác minh beforeEditSubmit có lọc bỏ trường id hay không"
      description="Vui lòng chỉnh sửa bất kỳ ô nào và click vào dấu tick, xem kết quả kiểm tra bên dưới. Nếu trong record không có trường id, nghĩa là vấn đề tồn tại."
      type="warning"
      show-icon
      class="mb-4"
    />

    <div class="p-4">
      <a-card title="🔬 Kịch bản kiểm thử 1: trường id không nằm trong columns (kịch bản phổ biến nhất)" class="mb-4">
        <a-alert
          message="⚠️ Kiểm thử cốt lõi: id có trong dữ liệu, nhưng không hiển thị trong columns"
          description="Đây là kịch bản phổ biến nhất: trường khóa chính thường không cần hiển thị trong bảng, nhưng khi cập nhật dữ liệu thì bắt buộc phải dùng."
          type="info"
          show-icon
          class="mb-3"
        />
        
        <a-space direction="vertical" style="width: 100%">
          <a-card size="small" title="📋 Giải thích dữ liệu kiểm tra" :bordered="false">
            <p><strong>Nguồn dữ liệu bao gồm:</strong>id, name, age, email, address</p>
            <p><strong>Columns Hiển thị:</strong>name, age, email, address（⚠️ Không có cột id)</p>
            <p><strong>rowKey Cấu hình:</strong>'id'</p>
          </a-card>
          
          <BasicTable
            @register="registerTable1"
            :beforeEditSubmit="handleBeforeEditSubmit1"
          />
          
          <a-card 
            size="small" 
            :title="testResult1.title" 
            :bordered="false"
            :headStyle="{ backgroundColor: testResult1.bgColor, color: 'white' }"
          >
            <a-descriptions bordered :column="1" size="small">
              <a-descriptions-item label="Có chứa không id">
                <a-tag :color="testResult1.hasId ? 'success' : 'error'">
                  {{ testResult1.hasId ? '✅ Chứa' : '❌ Không chứa' }}
                  {{ testResult1.hasId ? `(id=${testResult1.idValue})` : '' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="record Trường được bao gồm">
                <a-tag v-for="field in testResult1.fields" :key="field" color="blue">{{ field }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="Nội dung đầy đủ của record">
                <pre class="test-result-json">{{ testResult1.recordJson }}</pre>
              </a-descriptions-item>
              <a-descriptions-item label="Kết luận kiểm thử">
                <a-alert 
                  :message="testResult1.conclusion" 
                  :type="testResult1.hasId ? 'success' : 'error'" 
                  show-icon 
                />
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-space>
      </a-card>

      <a-card title="💡 Hướng dẫn kiểm thử" class="mb-4">
        <a-space direction="vertical" style="width: 100%">
          <a-alert
            message="Làm thế nào để kiểm tra?"
            description="1. Nhấp vào bất kỳ ô nào trong bảng phía trên để chỉnh sửa
2. Sau khi sửa nội dung, nhấp vào dấu tick ✓ Gửi
3. Xem kết quả kiểm tra, quan sát xem record có chứa trường id không"
            type="info"
            show-icon
          />
          
          <a-alert
            message="Kết quả mong đợi"
            type="success"
            show-icon
          >
            <template #description>
              <p><strong>Nếu mã bình thường:</strong></p>
              <p>✅ record Phải chứa trường id</p>
              <p>✅ Có thể sử dụng record.id Tiến hành cập nhật dữ liệu</p>
              <p>✅ Bảng điều khiển hiển thị thông báo thành công màu xanh lá</p>
            </template>
          </a-alert>
          
          <a-alert
            message="Bug Triệu chứng (Issue #8924）"
            type="error"
            show-icon
          >
            <template #description>
              <p><strong>Nếu tồn tại Bug:</strong></p>
              <p>❌ record trong record không có trường id</p>
              <p>❌ record Chỉ chứa các trường được định nghĩa trong columns (name, age, email, address）</p>
              <p>❌ Không thể thực hiện thao tác cập nhật dữ liệu</p>
              <p>❌ Bảng điều khiển hiển thị thông báo lỗi màu đỏ</p>
            </template>
          </a-alert>
          
          <a-card size="small" title="🔍 Phân tích nguyên nhân" :bordered="false">
            <p>Mã gốc sử dụng <code>pick(record, keys)</code> Lọc trường:</p>
            <pre class="code-block">const keys = columns.map(c => c.dataIndex).filter(f => !!f);
// keys = ['name', 'age', 'email', 'address']  // ⚠️ Không có id

record: pick(record, keys)
// Chỉ giữ lại các trường trong keys, id đã bị lọc bỏ</pre>
          </a-card>
        </a-space>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, BasicColumn } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  // ============ Kịch bản kiểm thử 1: trường id không nằm trong columns ============
  const testResult1 = ref<{
    title: string;
    bgColor: string;
    hasId: boolean;
    idValue: any;
    fields: string[];
    recordJson: string;
    conclusion: string;
  }>({
    title: '📊 Kết quả kiểm tra (hiển thị sau khi nhấp dấu tích)',
    bgColor: '#909399',
    hasId: false,
    idValue: null,
    fields: [],
    recordJson: 'Chưa có dữ liệu, vui lòng chỉnh sửa ô và bấm dấu tích',
    conclusion: 'Đang chờ kiểm thử...',
  });

  const columns1: BasicColumn[] = [
    // Lưu ý: Ở đây không có cột id
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 150,
      edit: true,
      editComponent: 'Input',
    },
    {
      title: 'Tuổi',
      dataIndex: 'age',
      width: 120,
      edit: true,
      editComponent: 'InputNumber',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      width: 200,
      edit: true,
      editComponent: 'Input',
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      width: 200,
      edit: true,
      editComponent: 'Input',
    },
  ];

  const dataSource1 = [
    { id: 1, name: 'Trương Tam', age: 25, email: 'zhangsan@example.com', address: 'Quận Triều Dương, Bắc Kinh' },
    { id: 2, name: 'Lý Tứ', age: 30, email: 'lisi@example.com', address: 'Quận Phố Đông, thành phố Thượng Hải' },
    { id: 3, name: 'Vương Ngũ', age: 28, email: 'wangwu@example.com', address: 'Quận Thiên Hà, Quảng Châu' },
  ];

  const [registerTable1] = useTable({
    rowKey: 'id',  // Dùng trường id mặc định làm khóa chính,
    columns: columns1,
    dataSource: dataSource1,
    pagination: false,
    showIndexColumn: true,
    canResize: false,
  });

  async function handleBeforeEditSubmit1({ record, index, key, value }) {
    console.log('🧪 Kiểm tra kịch bản 1 - beforeEditSubmit Dữ liệu đã nhận:', { record, index, key, value });
    console.log('🔍 record Nội dung chi tiết:', JSON.stringify(record, null, 2));
    
    // Phân tích record
    const hasId = 'id' in record;
    const fields = Object.keys(record);
    
    // Cập nhật kết quả kiểm tra
    testResult1.value = {
      title: hasId ? '✅ Kiểm thử đạt' : '❌ Kiểm thử thất bại - Phát hiện Bug!',
      bgColor: hasId ? '#67C23A' : '#F56C6C',
      hasId: hasId,
      idValue: record.id || null,
      fields: fields,
      recordJson: JSON.stringify(record, null, 2),
      conclusion: hasId 
        ? `✅ record chứa trường id (giá trị là ${record.id}), có thể cập nhật dữ liệu bình thường` 
        : `❌ Bug Đã xác nhận: record thiếu trường id! Chỉ chứa ${fields.join(', ')}. Điều này sẽ khiến không thể thực hiện thao tác cập nhật dữ liệu.`,
    };

    if (!hasId) {
      createMessage.error('❌ Kiểm tra thất bại: record thiếu trường id! Đây chính là Issue #8924 vấn đề đã mô tả.');
      console.error('❌ Bug Tái hiện: trong nguồn dữ liệu có id, nhưng record mà beforeEditSubmit nhận được lại không có id');
      return false;
    }

    createMessage.success(`✅ Kiểm tra đạt: đã lấy được id=${record.id}`);
    console.log('✅ Mô phỏng yêu cầu cập nhật:', {
      url: '/api/user/update',
      params: { id: record.id, [key]: value },
    });
    
    return true;
  }

</script>

<style scoped>
  .test-result-json {
    font-size: 12px;
    line-height: 1.5;
    background-color: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
    max-height: 200px;
    overflow: auto;
  }
  
  .code-block {
    font-size: 13px;
    line-height: 1.6;
    background-color: #282c34;
    color: #abb2bf;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
  }
  
  p {
    margin: 8px 0;
  }
</style>
