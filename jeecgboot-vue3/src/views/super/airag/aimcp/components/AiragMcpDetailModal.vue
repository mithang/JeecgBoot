<template>
  <BasicModal
    destroyOnClose
    @register="registerModal"
    :canFullscreen="false"
    width="720px"
    wrapClassName="airag-mcp-detail-modal"
  >
    <template #title>
      <div class="detail-modal-title">
        <span>Chi tiết</span>
        <a-button 
          class="detail-edit-btn" 
          type="text" 
          @click="emitEdit"
          :title="'Chỉnh sửa'"
        >
          <Icon icon="ant-design:edit-outlined" :size="18" />
        </a-button>
      </div>
    </template>
  <div class="detail-modal" v-loading="loading">
      <div class="detail-header">
        <img :src="displayIcon" class="detail-icon" />
        <div class="detail-titles">
          <div class="detail-name" :title="record?.name">{{ record?.name || '-' }}</div>
          <div class="detail-type-status">
            <a-tag color="blue">{{ record?.category === 'plugin' ? 'Plugin' : (record?.type || 'Loại không rõ') }}</a-tag>
            <a-tag v-if="record?.synced" :color="record?.status === 'enable' ? 'green' : 'red'">
              {{ record?.status === 'enable' ? 'Đã kích hoạt' : 'Chưa kích hoạt' }}
            </a-tag>
            <a-tag v-else color="orange">Chưa đồng bộ</a-tag>
          </div>
        </div>
      </div>
      <div class="detail-descr" :title="record?.descr">{{ record?.descr || 'Tạm thời chưa có mô tả' }}</div>
      <div class="tools-wrapper">
        <div class="tools-header">
          <div class="tools-title">Danh sách công cụ</div>
          <a-button v-if="record?.category === 'plugin'" type="primary" size="small" @click="handleAddTool">Thêm công cụ</a-button>
        </div>
        <div class="tools-grid" v-if="(pluginTools && pluginTools.length) || (tools && tools.length)">
          <!-- Công cụ loại plugin -->
          <template v-if="record?.category === 'plugin'">
            <div
              v-for="tool in pluginTools"
              :key="tool.name"
              class="tool-item"
              @click="handleEditTool(tool)"
            >
              <div class="tool-header-item" @click.stop>
                <div class="tool-name" :title="tool.name">{{ tool.name }}</div>
                <div class="tool-actions">
                  <a-switch 
                    v-model:checked="tool.enabled" 
                    size="small" 
                    @change="handleToolEnabledChange(tool)"
                    @click.stop
                  />
                  <a-button 
                    type="text" 
                    size="small" 
                    @click.stop="handleEditTool(tool)"
                    :title="'Chỉnh sửa công cụ'"
                  >
                    <Icon icon="ant-design:edit-outlined" :size="16" />
                  </a-button>
                  <a-button 
                    type="text" 
                    danger
                    size="small" 
                    @click.stop="handleDeleteTool(tool)"
                    :title="'Xóa công cụ'"
                  >
                    <Icon icon="ant-design:delete-outlined" :size="16" />
                  </a-button>
                </div>
              </div>
              <div class="tool-descr" :title="tool.description">{{ tool.description || 'Không có mô tả' }}</div>
              <div v-if="tool.method || tool.path" class="tool-meta">
                <a-tag v-if="tool.method" size="small" :color="getMethodColor(tool.method)">{{ tool.method }}</a-tag>
                <span v-if="tool.path" class="tool-path">{{ tool.path }}</span>
              </div>
            </div>
          </template>
          <!-- Công cụ loại MCP -->
          <template v-else>
            <div
              v-for="tool in tools"
              :key="tool.name"
              class="tool-item"
            >
              <div class="tool-header-item" @click.stop>
                <div class="tool-name" :title="tool.name">{{ tool.name }}</div>
              </div>
              <div class="tool-descr" :title="tool.descr">{{ tool.descr || 'Không có mô tả' }}</div>
            </div>
          </template>
        </div>
        <a-empty v-else description="Tạm thời chưa có công cụ" />
      </div>
    </div>
    <template #footer>
      <a-button @click="closeModal()">Đóng</a-button>
    </template>
  </BasicModal>
  <!-- Popup chỉnh sửa công cụ -->
  <PluginToolEditModal @register="registerToolEditModal" @success="handleToolEditSuccess" />
</template>
<script lang="ts" setup>
import { ref, computed } from 'vue';
import { useModalInner, useModal } from '/@/components/Modal';
import BasicModal from '/@/components/Modal/src/BasicModal.vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { queryById } from '../AiragMcp.api';
import defaultLogo from '../imgs/mcpLogo.png';
import PluginToolEditModal from './PluginToolEditModal.vue';

const { createMessage } = useMessage();

interface McpToolItem {
  name: string;
  descr?: string;
  enabled?: boolean;
}

interface PluginToolItem {
  name: string;
  description?: string;
  path?: string;
  method?: string;
  parameters?: any[];
  responses?: any[];
  enabled?: boolean;
}

interface McpRecord {
  id?: string;
  name?: string;
  descr?: string;
  icon?: string;
  type?: string;
  category?: string;
  status?: string;
  synced?: boolean;
  tools?: McpToolItem[] | string;
  endpoint?: string;
  headers?: string;
  metadata?: string | any;
}

const emit = defineEmits(['register','edit', 'success']);
const record = ref<McpRecord | null>(null);
const tools = ref<McpToolItem[]>([]);
const pluginTools = ref<PluginToolItem[]>([]);
const loading = ref<boolean>(false);

// Popup chỉnh sửa công cụ
const [registerToolEditModal, { openModal: openToolEditModal }] = useModal();

const displayIcon = computed(() => {
  const icon = record.value?.icon;
  if (!icon) return defaultLogo;
  return icon.startsWith('http') ? icon : icon;
});

const [registerModal, { closeModal }] = useModalInner(async (data: McpRecord) => {
  if(!data?.id){
    record.value = { ...data };
    // Khởi tạo danh sách công cụ theo category
    if (data.category === 'plugin') {
      try {
        const toolsData = typeof data.tools === 'string' ? JSON.parse(data.tools) : data.tools;
        pluginTools.value = Array.isArray(toolsData) ? toolsData : [];
      } catch (e) {
        pluginTools.value = [];
      }
      tools.value = [];
    } else {
      tools.value = Array.isArray(data.tools) ? data.tools : [];
      pluginTools.value = [];
    }
    return;
  }
  loading.value = true;
  try {
    const res = await queryById(data.id);
    // Cấu trúc trả về của backend dùng trực tiếp res.result hoặc res depending on transform; Giả sử thống nhất là res.result
    const detail = res.result || res; // Tương thích với các gói trả về khác nhau
    record.value = {
      id: detail.id,
      name: detail.name,
      descr: detail.descr,
      icon: detail.icon,
      type: detail.type,
      category: detail.category,
      status: detail.status,
      synced: !!detail.synced,
      endpoint: detail.endpoint,
      headers: detail.headers,
      metadata: detail.metadata,
    };
    
    // Phân tích công cụ theo category
    if (detail.category === 'plugin') {
      // Loại plugin: phân tích trường tools thành danh sách công cụ plugin
      let parsedPluginTools: PluginToolItem[] = [];
      const rawTools = detail.tools;
      if (rawTools) {
        try {
          if (typeof rawTools === 'string') {
            parsedPluginTools = JSON.parse(rawTools);
          } else if (Array.isArray(rawTools)) {
            parsedPluginTools = rawTools;
          }
        } catch (e) {
          parsedPluginTools = [];
        }
      }
      // Đảm bảo mỗi công cụ đều có trường enabled, mặc định là true
      pluginTools.value = parsedPluginTools.map((t: any) => ({
        ...t,
        enabled: t.enabled !== undefined ? t.enabled : true
      }));
      tools.value = [];
    } else {
      // MCPLoại MCP: phân tích trường tools thành danh sách công cụ MCP
      let parsedTools: McpToolItem[] = [];
      const rawTools = detail.tools;
      if (rawTools) {
        try {
          if (typeof rawTools === 'string') {
            const arr = JSON.parse(rawTools);
            parsedTools = arr.map((t: any) => ({ 
              name: t.name, 
              descr: t.description,
              enabled: t.enabled !== undefined ? t.enabled : true
            }));
          } else if (Array.isArray(rawTools)) {
            parsedTools = rawTools.map((t: any) => ({ 
              name: t.name, 
              descr: t.description,
              enabled: t.enabled !== undefined ? t.enabled : true
            }));
          }
        } catch (e) {
          parsedTools = [];
        }
      }
      tools.value = parsedTools;
      pluginTools.value = [];
    }
  } finally {
    loading.value = false;
  }
});


function handleAddTool() {
  openToolEditModal(true, {
    pluginId: record.value?.id,
    plugin: record.value,
    tool: null, // Thêm mới
  });
}

function handleEditTool(tool: PluginToolItem) {
  openToolEditModal(true, {
    pluginId: record.value?.id,
    plugin: record.value,
    tool: tool,
  });
}

function getMethodColor(method: string): string {
  const colorMap: Record<string, string> = {
    'GET': 'blue',
    'POST': 'green',
    'PUT': 'orange',
    'DELETE': 'red',
    'PATCH': 'purple',
  };
  return colorMap[method] || 'default';
}

async function handleToolEnabledChange(tool: PluginToolItem) {
  // Cập nhật trạng thái kích hoạt công cụ
  if (!record.value?.id) return;
  
  try {
    const res = await queryById(record.value.id);
    const detail = res.result || res;
    let tools: any[] = [];
    if (detail.tools) {
      try {
        tools = typeof detail.tools === 'string' ? JSON.parse(detail.tools) : detail.tools;
      } catch (e) {
        tools = [];
      }
    }
    const index = tools.findIndex((t: any) => t.name === tool.name);
    if (index >= 0) {
      tools[index].enabled = tool.enabled;
      const { saveTools } = await import('../AiragMcp.api');
      await saveTools(record.value.id, JSON.stringify(tools));
      emit('success');
    }
  } catch (e) {
    console.error('Cập nhật trạng thái kích hoạt công cụ thất bại:', e);
    // Khôi phục trạng thái
    tool.enabled = !tool.enabled;
  }
}

function handleDeleteTool(tool: PluginToolItem) {
  // Xác nhận lần hai trước khi xóa công cụ
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: `Xác nhận muốn xóa công cụ"${tool.name}"không? Thao tác này không thể khôi phục.`,
    okText: 'Xác định',
    okType: 'danger',
    cancelText: 'Hủy',
    onOk: async () => {
      if (!record.value?.id) {
        createMessage.error('ID plugin không tồn tại');
        return;
      }
      
      try {
        // Lấy danh sách công cụ mới nhất
        const res = await queryById(record.value.id);
        const detail = res.result || res;
        let tools: any[] = [];
        if (detail.tools) {
          try {
            tools = typeof detail.tools === 'string' ? JSON.parse(detail.tools) : detail.tools;
          } catch (e) {
            tools = [];
          }
        }
        
        // Loại bỏ công cụ này khỏi danh sách công cụ
        const filteredTools = tools.filter((t: any) => t.name !== tool.name);
        
        if (filteredTools.length === tools.length) {
          createMessage.warning('Không tìm thấy công cụ cần xóa');
          return;
        }
        
        // Lưu danh sách công cụ sau khi cập nhật
        const { saveTools } = await import('../AiragMcp.api');
        const saveRes = await saveTools(record.value.id, JSON.stringify(filteredTools));
        
        if (saveRes.success) {
          createMessage.success('Xóa thành công');
          // Cập nhật hiển thị frontend
          pluginTools.value = pluginTools.value.filter((t: any) => t.name !== tool.name);
          // Kích hoạt sự kiện thành công, thông báo component cha làm mới
          emit('success');
        } else {
          createMessage.error(saveRes.message || 'Xóa thất bại');
        }
      } catch (e) {
        console.error('Xóa công cụ thất bại:', e);
        createMessage.error('Xóa công cụ thất bại');
      }
    },
  });
}

function emitEdit() {
  // Kích hoạt sự kiện chỉnh sửa, truyền record cho component cha
  if (record.value) {
    emit('edit', record.value);
    closeModal();
  }
}

function handleToolEditSuccess() {
  // Tải lại dữ liệu chi tiết
  if (record.value?.id) {
    loading.value = true;
    queryById(record.value.id).then((res: any) => {
      const detail = res.result || res;
      
      // Cập nhật thông tin record
      record.value = {
        id: detail.id,
        name: detail.name,
        descr: detail.descr,
        icon: detail.icon,
        type: detail.type,
        category: detail.category,
        status: detail.status,
        synced: !!detail.synced,
        endpoint: detail.endpoint,
        headers: detail.headers,
        metadata: detail.metadata,
      };
      
      // Phân tích công cụ theo category
      if (detail.category === 'plugin') {
        // Loại plugin: phân tích trường tools thành danh sách công cụ plugin
        let parsedPluginTools: PluginToolItem[] = [];
        const rawTools = detail.tools;
        if (rawTools) {
          try {
            if (typeof rawTools === 'string') {
              parsedPluginTools = JSON.parse(rawTools);
            } else if (Array.isArray(rawTools)) {
              parsedPluginTools = rawTools;
            }
          } catch (e) {
            parsedPluginTools = [];
          }
        }
        pluginTools.value = parsedPluginTools.map((t: any) => ({
          ...t,
          enabled: t.enabled !== undefined ? t.enabled : true
        }));
        tools.value = [];
      } else {
        // MCPLoại MCP: phân tích trường tools thành danh sách công cụ MCP
        let parsedTools: McpToolItem[] = [];
        const rawTools = detail.tools;
        if (rawTools) {
          try {
            if (typeof rawTools === 'string') {
              const arr = JSON.parse(rawTools);
              parsedTools = arr.map((t: any) => ({ name: t.name, descr: t.description }));
            } else if (Array.isArray(rawTools)) {
              parsedTools = rawTools.map((t: any) => ({ name: t.name, descr: t.description }));
            }
          } catch (e) {
            parsedTools = [];
          }
        }
        tools.value = parsedTools;
        pluginTools.value = [];
      }
      loading.value = false;
      // Kích hoạt sự kiện success, thông báo trang danh sách làm mới
      emit('success');
    }).catch(() => {
      loading.value = false;
    });
  }
}
</script>
<style scoped lang="less">
.detail-modal {
  padding: 12px 16px 8px 16px;
  max-height: 520px;
  overflow-y: auto;
  .detail-header {
    display: flex;
    align-items: center;
    .detail-icon {
      width: 56px;
      height: 56px;
      border-radius: 8px;
      margin-right: 16px;
      background: #f5f6f7;
      object-fit: cover;
    }
    .detail-titles {
      flex: 1;
      min-width: 0;
      .detail-name {
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;
        line-height: 28px;
        max-width: 480px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .detail-type-status { margin-top: 4px; }
    }
  }
  .detail-descr {
    margin-top: 12px;
    font-size: 13px;
    line-height: 20px;
    color: #4e5969;
    background: #f5f6f7;
    padding: 8px 12px;
    border-radius: 6px;
    max-height: 80px;
    overflow-y: auto;
  }
  .tools-wrapper {
    margin-top: 16px;
    .tools-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }
    .tools-title {
      font-size: 16px;
      font-weight: 600;
    }
    .tools-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      max-height: 280px;
      overflow-y: auto;
      padding-right: 4px;
    }
    .tool-item {
      border: 1px solid #e5e6eb;
      border-radius: 8px;
      background: #fff;
      padding: 10px 12px;
      min-height: 88px;
      display: flex;
      flex-direction: column;
      transition: box-shadow 0.25s;
      cursor: default;
      &:hover { box-shadow: 0 4px 10px rgba(0,0,0,0.08); }
      &.tool-item-plugin {
        cursor: pointer;
      }
      .tool-header-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;
        .tool-actions {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .ant-btn-text {
            padding: 0;
            height: 24px;
            width: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            
            &:hover {
              background-color: rgba(255, 77, 79, 0.1);
            }
          }
        }
      }
      .tool-name {
        font-size: 14px;
        font-weight: 600;
        color: #1d2129;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
      }
      .tool-descr {
        font-size: 12px;
        color: #4e5969;
        line-height: 18px;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 8px;
      }
      .tool-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: auto;
        .tool-path {
          font-size: 12px;
          color: #86909c;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}

.detail-modal-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 40px;
  
  .detail-edit-btn {
    padding: 4px 8px;
    &:hover {
      color: #1890ff;
      background-color: rgba(24, 144, 255, 0.1);
    }
  }
}
</style>
<style lang="less">
.airag-mcp-detail-modal { 
  .jeecg-basic-modal-close > span { margin-left: 0 !important; }
  
  :deep(.ant-modal-header) {
    padding: 16px 24px;
  }
  
  :deep(.ant-modal-title) {
    width: 100%;
  }
}
</style>
