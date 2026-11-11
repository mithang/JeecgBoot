<template>
  <div class="mcp">
    <!-- Khu vực truy vấn -->
    <div class="jeecg-basic-table-form-container">
      <a-form
        ref="formRef"
        @keyup.enter="searchQuery"
        :model="queryParam"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        style="background-color: #f7f8fc !important;"
      >
        <a-row :gutter="24">
          <a-col :lg="6">
            <a-form-item name="name" label="Tên">
              <JInput v-model:value="queryParam.name" />
            </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-form-item name="category" label="Loại">
              <a-select v-model:value="queryParam.category" placeholder="Tất cả" allowClear>
                <a-select-option value="plugin">Plugin</a-select-option>
                <a-select-option value="mcp">MCP</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Truy vấn</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">Đặt lại</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- Khu vực thẻ -->
    <a-row :span="24" class="mcp-row">
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24">
        <a-card class="add-mcp-card" @click="handleAdd" >
          <div class="flex">
            <Icon icon="ant-design:plus-outlined" class="add-mcp-card-icon" size="20" />
            <span class="add-mcp-card-title">Thêm mới MCP</span>
          </div>
        </a-card>
      </a-col>
      <a-col v-for="item in mcpList" :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" :key="item.id" v-if="mcpList && mcpList.length > 0">
        <a-card class="mcp-card" @click="handleDetailClick(item)">
          <div class="mcp-header">
            <div class="flex">
              <img :src="getIcon(item.icon)" class="header-img" />
              <div class="header-text" :title="item.name">{{ item.name }}</div>
            </div>
          </div>
          <div class="mt-6">
            <ul>
              <li class="flex mr-14">
                <span class="described" :title="item.descr">{{ item.descr || '-' }}</span>
              </li>
            </ul>
          </div>
          <div class="mcp-btn">
            <a-button class="mcp-icon" size="small" @click.prevent.stop="handleEditClick(item)" v-auth="'llm:airag_mcp:edit'">
              <Icon icon="ant-design:edit-outlined" />
            </a-button>
            <a-dropdown placement="bottomRight" :trigger="['click']" :getPopupContainer="(node) => node.parentNode">
              <div class="ant-dropdown-link pointer mcp-icon" @click.prevent.stop>
                <Icon icon="ant-design:ellipsis-outlined" />
              </div>
              <template #overlay>
                <a-menu>
                  <!-- Loại MCP: hiển thị nút đồng bộ -->
                  <a-menu-item
                    v-if="item.category === 'mcp'"
                    key="sync"
                    @click.prevent.stop="handleSync(item)"
                    v-auth="'llm:airag_mcp:sync'"
                  >
                    <Icon icon="ant-design:cloud-sync-outlined" size="16" /> Đồng bộ
                  </a-menu-item>
                  <!-- Loại plugin: hiển thị nút quản lý công cụ -->
                  <a-menu-item
                    v-if="item.category === 'plugin'"
                    key="toolManage"
                    @click.prevent.stop="handleToolManage(item)"
                    v-auth="'llm:airag_mcp:edit'"
                  >
                    <Icon icon="ant-design:tool-outlined" size="16" /> Quản lý công cụ
                  </a-menu-item>
                  <!-- Chỉnh sửa: luôn hiển thị, không bị ảnh hưởng bởi vô hiệu hóa/kích hoạt -->
                  <a-menu-item
                    key="edit"
                    @click.prevent.stop="handleEditClick(item)"
                    v-auth="'llm:airag_mcp:edit'"
                  >
                    <Icon icon="ant-design:edit-outlined" size="16" /> Chỉnh sửa
                  </a-menu-item>
                  <a-menu-item
                    v-if="item.synced"
                    key="toggle"
                    @click.prevent.stop="handleToggleStatus(item)"
                    v-auth="'llm:airag_mcp:edit'"
                  >
                    <Icon :icon="item.status === 'enable' ? 'ant-design:stop-outlined' : 'ant-design:check-circle-outlined'" size="16" />
                    {{ item.status === 'enable' ? 'Vô hiệu hóa' : 'Kích hoạt' }}
                  </a-menu-item>
                  <a-menu-item
                    v-if="item.status === 'disable' || !item.synced"
                    key="delete"
                    @click.prevent.stop="handleDeleteClick(item)"
                    v-auth="'llm:airag_mcp:delete'"
                  >
                    <Icon icon="ant-design:delete-outlined" size="16" /> Xóa
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div class="card-footer" v-if="true">
            <div class="pill type-pill" :title="'Loại: '+(item.category === 'plugin' ? 'Plugin' : 'MCP')">
              <Icon :icon="getCategoryIcon(item.category)" class="pill-icon" />
              <span class="pill-text">{{ item.category === 'plugin' ? 'Plugin' : 'MCP' }}</span>
            </div>
            <div class="pill status-pill" :class="item.synced ? (item.status==='enable'?'status-enable-pill':'status-disable-pill'):'status-unsynced-pill'" :title="item.synced ? (item.status==='enable'?'Đã kích hoạt':'Chưa kích hoạt'):'Chưa đồng bộ'">
              <Icon :icon="getStatusIcon(item)" class="pill-icon" />
              <span class="pill-text">{{ item.synced ? (item.status==='enable'?'Kích hoạt':'Vô hiệu hóa') : 'Chưa đồng bộ' }}</span>
            </div>
            <div class="pill tool-pill" :title="getToolCount(item)+' công cụ'">
              <Icon icon="ant-design:tool-outlined" class="pill-icon" />
              <span class="pill-text">{{ getToolCount(item) }} công cụ</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <Pagination
      v-if="mcpList.length > 0"
      :current="pageNo"
      :page-size="pageSize"
      :page-size-options="pageSizeOptions"
      :total="total"
      :showQuickJumper="true"
      :showSizeChanger="true"
      @change="handlePageChange"
      class="list-footer"
      size="small"
      :show-total="() => `Tổng cộng${total}Mục` "
    />
  </div>
  <!-- Khu vực popup -->
  <AiragMcpAddModal @register="registerModal" @success="reload" />
  <AiragMcpDetailModal @register="registerDetailModal" @edit="handleDetailEdit" @success="reload" />
</template>
<script lang="ts" name="llm-airagMcp" setup>
import { reactive, ref } from 'vue';
import { Pagination } from 'ant-design-vue';
import AiragMcpAddModal from './components/AiragMcpAddModal.vue';
import AiragMcpDetailModal from './components/AiragMcpDetailModal.vue';
import { list, deleteOne, syncMcp, toggleStatus} from './AiragMcp.api';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
import JInput from '@/components/Form/src/jeecg/components/JInput.vue';
import defaultLogo from './imgs/mcpLogo.png'

// Dữ liệu danh sách
const mcpList = ref<any[]>([]);
// Phân trang
const pageNo = ref<number>(1);
const pageSize = ref<number>(30);
const total = ref<number>(0);
const pageSizeOptions = ref<any>(['10', '20', '30']);

// Tham số truy vấn
const queryParam = reactive<any>({});
const formRef = ref();
const { createMessage } = useMessage();

// Độ rộng label của khu vực truy vấn
const labelCol = reactive({
  xs: 24,
  sm: 4,
  xl: 6,
  xxl: 6,
});
const wrapperCol = reactive({
  xs: 24,
  sm: 20,
});

// Popup (thêm mới/Chỉnh sửa)
const [registerModal, { openModal }] = useModal();
// Popup chi tiết
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

// Khởi tạo
reload();

function reload() {
  const params: any = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    column: 'createTime',
    order: 'desc',
    ...queryParam,
  };
  list(params).then((res) => {
    if (res.records) {
      mcpList.value = res.records;
      total.value = res.total;
    } else {
      mcpList.value = [];
      total.value = 0;
    }
  });
}

function handlePageChange(page, current) {
  pageNo.value = page;
  pageSize.value = current;
  reload();
}

function handleAdd() {
  openModal(true, {});
}

function handleEditClick(item) {
  // Danh sách mô hình tham khảo, chỉ truyền id; nếu cần backend truy vấn có thể mở rộng bên trong popup
  openModal(true, { id: item.id, ...item });
}

function handleDetailClick(item){
  // Chỉ truyền id, bên trong popup chi tiết gọi queryById để lấy dữ liệu mới nhất
  openDetailModal(true, { id: item.id });
}

function handleDetailEdit(record){
  // Kích hoạt chỉnh sửa từ bên trong popup chi tiết
  openModal(true, { id: record.id, ...record });
}

async function handleDeleteClick(item) {
  if (mcpList.value.length === 1 && pageNo.value > 1) {
    pageNo.value = pageNo.value - 1;
  }
  await deleteOne({ id: item.id }, reload);
}

async function handleSync(item) {
  try {
    const res = await syncMcp(item.id);
    if (res.success) {
      createMessage.success(res.message || 'Đồng bộ thành công');
    } else {
      createMessage.error(res.message || 'Đồng bộ thất bại');
    }
  } catch (error: any) {
    createMessage.error(error?.message || 'Đồng bộ thất bại');
  } finally {
    reload();
  }
}

async function handleToggleStatus(item) {
  const newStatus = item.status === 'enable' ? 'disable' : 'enable';
  await toggleStatus(item.id , newStatus).finally(() => reload());
}

function searchQuery() {
  pageNo.value = 1;
  reload();
}

function searchReset() {
  formRef.value?.resetFields();
  Object.keys(queryParam).forEach((k) => (queryParam[k] = ''));
  searchQuery();
}

// Xử lý biểu tượng
function getIcon(icon?: string) {
  return icon ? getFileAccessHttpUrl(icon) : defaultLogo;
}

// Số lượng công cụ: plugin thống kê theo danh sách tools, MCP ưu tiên đọc metadata
function getToolCount(item: any): number {
  //update-begin---wangshuai---date:20260805  for：[LHZP-1590]Khi dùng mcp plugin, công cụ danh sách không hiển thị số lượng------------
  let metaObj: any = item?.metadata;
  if (typeof metaObj === 'string') {
    try {
      metaObj = JSON.parse(metaObj);
    } catch (e) {
      metaObj = null;
    }
  }
  let tools = item?.tools;
  if (typeof tools === 'string') {
    try {
      tools = JSON.parse(tools);
    } catch (e) {
      tools = [];
    }
  }
  if (item?.category === 'plugin' && Array.isArray(tools)) {
    //update-begin---author:scott ---date:20260806  for：【LHZP-1496】Thẻ plugin chỉ thống kê công cụ đã kích hoạt-----------
    return tools.filter((tool: any) => tool?.enabled !== false).length;
    //update-end---author:scott ---date:20260806  for：【LHZP-1496】Thẻ plugin chỉ thống kê công cụ đã kích hoạt-----------
  }
  const count = metaObj?.tool_count;
  if (count != null) {
    return typeof count === 'number' ? count : parseInt(count, 10) || 0;
  }
  return Array.isArray(tools) ? tools.length : 0;
  //update-end---author:wangshuai ---date:20260805  for：[LHZP-1590]Khi dùng mcp plugin, công cụ danh sách không hiển thị số lượng------------
}

// Ánh xạ biểu tượng loại
function getTypeIcon(type?: string) {
  switch (type) {
    case 'sse':
      return 'ant-design:thunderbolt-outlined';
    case 'stdio':
      return 'ant-design:code-outlined';
    default:
      return 'ant-design:appstore-outlined';
  }
}

// Ánh xạ biểu tượng category
function getCategoryIcon(category?: string) {
  if (category === 'plugin') {
    return 'ant-design:api-outlined';
  }
  return 'ant-design:tool-twotone';
}

// Quản lý công cụ - Mở trang chi tiết
function handleToolManage(item: any) {
  openDetailModal(true, { id: item.id });
}

// Trạng thái/Đồng bộ biểu tượng
function getStatusIcon(item: any) {
  if (!item.synced) return 'ant-design:cloud-sync-outlined';
  return item.status === 'enable' ? 'ant-design:check-circle-outlined' : 'ant-design:stop-outlined';
}

// <script setup> tự động lộ ra template ở phía dưới, không cần export
</script>

<style lang="less" scoped>
  .mcp {
    height: calc(100vh - 115px);
    background: #f7f8fc;
    padding: 24px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .jeecg-basic-table-form-container {
      flex-shrink: 0;
    }

    .mcp-row {
      flex: 1;
      min-height: 0;
      margin-top: 20px;
      padding-bottom: 12px;
      overflow-y: auto;
      overflow-x: hidden;
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(min(270px, 100%), 1fr));
      grid-auto-rows: 152px;
      align-content: flex-start;
      gap: 20px;
      :deep(.ant-col) {
        width: auto;
        max-width: none;
      }
      .mcp-card, .add-mcp-card { width: 100%; }
      .mcp-header {
        position: relative;
        font-size: 14px;
        .header-img {
          width: 32px;
          height: 32px;
          margin-right: 12px;
        }
        .header-text {
          font-size: 16px;
          font-weight: bold;
          color: #354052;
          width: calc(100% - 80px);
          overflow: hidden;
          align-content: center;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .list-footer {
      flex-shrink: 0;
      margin-top: 12px;
      padding-top: 4px;
    }
  }

  .label {
    font-weight: 400;
    font-size: 12px;
    align-self: center;
    color: #8a8f98;
    overflow-wrap: break-word;
  }
  .no-activate {
    font-size: 10px;
    color: #ff4d4f;
    border: 1px solid #ff4d4f;
    border-radius: 10px;
    padding: 0 6px;
    height: 14px;
    line-height: 12px;
    margin-left: 6px;
    align-self: center;
  }
  .described {
    font-weight: 400;
    margin-left: 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  /* Fallback for supporting browsers */
  line-clamp: 3;
    overflow: hidden;
    font-size: 12px;
    max-width: 100%;
    line-height: 18px;
    color: #848b99;
  }
  .status-enable {
    color: #52c41a;
  }
  .status-disable {
    color: #ff4d4f;
  }
  .status-unsynced {
    color: #fa8c16;
  }
  .flex {
    display: flex;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .mr-14 {
    margin-right: 14px;
  }
  .mt-6 {
    margin-top: 6px;
  }
  .ml-4 {
    margin-left: 4px;
  }
  .mcp-btn {
    position: absolute;
    right: 4px;
    top: 6px;
    height: auto;
    display: none;
  }
  .mcp-card {
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    border-radius: 10px;
    height: 152px;
    cursor: pointer;
    position: relative;
  }
  .mcp-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
    .mcp-btn {
      display: flex;
    }
  }
  .tool-count {
    position: absolute;
    left: 16px;
    bottom: 12px;
    font-size: 12px;
    color: #4e5969;
    display: flex;
    align-items: center;
    background: rgba(245,246,247,0.9);
    padding: 2px 8px;
    border-radius: 12px;
    line-height: 18px;
  }
  .card-footer {
    position: absolute;
    left: 16px;
    bottom: 12px;
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
  }
  .pill {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px 2px 8px;
    border-radius: 14px;
    font-size: 12px;
    line-height: 18px;
    font-weight: 500;
    backdrop-filter: saturate(180%) blur(4px);
    box-shadow: 0 0 0 1px rgba(0,0,0,0.05);
    .pill-icon { margin-right: 4px; }
  }
  .type-pill { background: linear-gradient(135deg,#e6f4ff,#f0f9ff); color:#0958d9; }
  .status-enable-pill { background: linear-gradient(135deg,#e8f9e9,#f0fff0); color:#2f952f; }
  .status-disable-pill { background: linear-gradient(135deg,#fff1f0,#ffecec); color:#c43826; }
  .status-unsynced-pill { background: linear-gradient(135deg,#fff7e6,#fff3d9); color:#d46b08; }
  .tool-pill { background: linear-gradient(135deg,#f5f6f7,#f0f1f2); color:#555; }
  .pointer {
    cursor: pointer;
  }
  .list-footer {
    text-align: right;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    :deep(.ant-form) {
      background-color: transparent;
    }
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
  }
  .add-mcp-card {
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    border-radius: 10px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    cursor: pointer;
    height: 152px;
    width: 270px;
    .add-mcp-card-icon {
      padding: 8px;
      color: #1f2329;
      background-color: #f5f6f7;
      margin-right: 12px;
    }
    .add-mcp-card-title {
      font-size: 16px;
      color: #1f2329;
      font-weight: 400;
      align-self: center;
    }
  }
  .add-mcp-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
  }
  .mcp-icon {
    background-color: unset;
    border: none;
    margin-right: 2px;
  }
  .mcp-icon:hover {
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }
  .ant-dropdown-link {
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    align-content: center;
    text-align: center;
  }
</style>
