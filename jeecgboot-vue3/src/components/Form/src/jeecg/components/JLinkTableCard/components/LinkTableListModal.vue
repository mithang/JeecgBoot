<template>
  <BasicModal
    @register="registerModal"
    :width="popModalFixedWidth"
    :dialogStyle="{ top: '70px' }"
    :bodyStyle="popBodyStyle"
    :title="modalTitle"
    wrapClassName="jeecg-online-pop-list-modal"
  >
    <template #footer>
      <a-button key="back" @click="handleCancel">Đóng</a-button>
      <a-button :disabled="submitDisabled" key="submit" type="primary" @click="handleSubmit" :loading="submitLoading">Xác nhận</a-button>
    </template>

    <!-- Thanh công cụ: Tìm kiếm + Gợi ý đã chọn -->
    <div class="ltm-toolbar">
      <a-input-search
        v-model:value="searchText"
        @search="onSearch"
        @pressEnter="onSearch"
        placeholder="Vui lòng nhập từ khóa, nhấn Enter để tìm kiếm"
        style="width: 240px"
      />
      <span v-if="cardSelectedKeys.length > 0" class="atm-selected-tip">
        Đã chọn {{ cardSelectedKeys.length }} bản ghi
        <a class="atm-clear-link" @click="clearCardSelection">Xóa sạch</a>
      </span>
    </div>

    <!-- Chế độ xem thẻ -->
    <div v-if="cardLoading" class="atm-spin-wrap">
      <a-spin />
    </div>
    <a-empty v-else-if="cardDataList.length === 0" style="margin: 40px 0" description="Không có dữ liệu" />
    <div v-else class="atm-card-grid">
      <div
        v-for="record in cardDataList"
        :key="getCardKey(record)"
        class="atm-card"
        :class="{ 'atm-card--selected': isCardSelected(record) }"
        @click="handleCardToggle(record)"
      >
        <!-- Biểu tượng góc trên bên phải ở chế độ chọn nhiều -->
        <span v-if="multi" class="atm-card-check">
          <CheckSquareFilled v-if="isCardSelected(record)" class="atm-check-icon--on" />
          <BorderOutlined v-else class="atm-check-icon--off" />
        </span>

        <!-- Khu vực nội dung -->
        <div class="atm-card-inner" :class="{ 'atm-has-image': hasCardImage(record) }">
          <div class="atm-card-title">{{ getCardMainContent(record) }}</div>
          <div class="atm-card-fields">
            <div v-for="field in cardOtherDisplayFields" :key="field.key" class="atm-card-field-row">
              <span class="atm-field-label">{{ field.title }}</span>
              <span class="atm-field-value">{{ record[field.key] }}</span>
            </div>
          </div>
        </div>

        <!-- Khu vực hình ảnh -->
        <div v-if="hasCardImage(record)" class="atm-card-image">
          <img :src="getCardImageSrc(record)" alt="" @error="handleCardImageError" />
        </div>
      </div>
    </div>

    <!-- Phân trang thẻ -->
    <div class="atm-pagination">
      <a-pagination
        v-model:current="cardCurrentPage"
        :total="cardTotal"
        :pageSize="cardPageSize"
        :show-total="(t) => `Tổng cộng ${t} mục`"
        show-size-changer
        @change="handleCardPageChange"
        @showSizeChange="handleCardSizeChange"
      />
    </div>
  </BasicModal>
</template>

<script lang="ts">
import { defineComponent, watch, ref, toRaw, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import { useTableColumns } from '@/views/super/online/cgform/hooks/auto/useTableColumns';
import { useFixedHeightModal } from '../hooks/useLinkTable';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { CheckSquareFilled, BorderOutlined } from '@ant-design/icons-vue';
import placeholderImage from '/@/assets/images/placeholderImage.png';

export default defineComponent({
  name: 'LinkTableListModal',
  props: {
    /**Có thể là tên bảng, có thể làID*/
    id: {
      type: String,
      default: '',
    },
    multi: {
      type: Boolean,
      default: false,
    },
    addAuth: {
      type: Boolean,
      default: true,
    },
  },
  components: {
    BasicModal,
    CheckSquareFilled,
    BorderOutlined,
  },
  emits: ['success', 'register'],
  setup(props, { emit }) {
    const { createMessage: $message } = useMessage();
    // Kiểm soát chiều cao popup
    const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
    const searchText = ref('');

    // ==================== Trạng thái chế độ thẻ ====================
    const cardLoading = ref(false);
    const cardDataList = ref<any[]>([]);
    const cardTotal = ref(0);
    const cardCurrentPage = ref(1);
    const cardPageSize = ref(10);

    // Trạng thái đã chọn ở chế độ thẻ
    const cardSelectedKeys = ref<string[]>([]);
    const cardSelectedRecordsMap = ref<Record<string, any>>({});

    function getCardKey(record: any): string {
      return String(record['id'] ?? '');
    }

    function isCardSelected(record: any): boolean {
      return cardSelectedKeys.value.includes(getCardKey(record));
    }

    function handleCardToggle(record: any) {
      const key = getCardKey(record);
      if (props.multi) {
        const idx = cardSelectedKeys.value.indexOf(key);
        if (idx >= 0) {
          cardSelectedKeys.value.splice(idx, 1);
          delete cardSelectedRecordsMap.value[key];
        } else {
          cardSelectedKeys.value.push(key);
          cardSelectedRecordsMap.value[key] = record;
        }
      } else {
        cardSelectedKeys.value = [key];
        cardSelectedRecordsMap.value = { [key]: record };
      }
    }

    function clearCardSelection() {
      cardSelectedKeys.value = [];
      cardSelectedRecordsMap.value = {};
    }

    // ==================== Tính toán trường thẻ (dựa trên columns) ====================
    // Trường hiển thị chính (cột đầu tiên không phải hình ảnh/Cột tệp)
    const cardMainField = computed(() => {
      const cols = columns.value || [];
      const first = cols.find((c) => !isImageOrFileColumn(c) && c.dataIndex !== 'action');
      return first?.dataIndex || '';
    });

    // Các trường văn bản khác (loại trừ trường chính, cột hình ảnh, cột tệp, tối đa 3 trường)
    const cardOtherDisplayFields = computed(() => {
      const cols = columns.value || [];
      const mainKey = cardMainField.value;
      return cols
        .filter((c) => c.dataIndex !== mainKey && c.dataIndex !== 'action' && !isImageOrFileColumn(c))
        .slice(0, 3)
        .map((c) => ({ key: c.dataIndex, title: c.title || c.dataIndex }));
    });

    // Trường hình ảnh (cột imgSlot đầu tiên)
    const cardImageField = computed(() => {
      const cols = columns.value || [];
      const imgCol = cols.find((c) => getColumnSlot(c) === 'imgSlot');
      return imgCol?.dataIndex || '';
    });

    function getColumnSlot(col: any): string {
      return col?.slots?.customRender || col?.customRender || '';
    }

    function isImageOrFileColumn(col: any): boolean {
      const slot = getColumnSlot(col);
      return slot === 'imgSlot' || slot === 'fileSlot';
    }

    function getCardMainContent(record: any): string {
      return record[cardMainField.value] ?? '';
    }

    function hasCardImage(record: any): boolean {
      const field = cardImageField.value;
      return !!(field && record[field]);
    }

    function getCardImageSrc(record: any): string {
      const field = cardImageField.value;
      if (!field) return '';
      let url = record[field];
      if (typeof url === 'string') url = url.split(',')[0];
      return getFileAccessHttpUrl(url);
    }

    function handleCardImageError(e: Event) {
      (e.target as HTMLImageElement).src = placeholderImage;
    }

    // ==================== Tải dữ liệu thẻ ====================
    async function loadCardData() {
      if (!props.id) return;
      cardLoading.value = true;
      try {
        const params = addQueryParams({
          column: 'id',
          pageNo: cardCurrentPage.value,
          pageSize: cardPageSize.value,
        });
        const res = await queryTableData(params);
        cardDataList.value = res?.records || [];
        cardTotal.value = res?.total || 0;
      } catch (e) {
        console.error('[LinkTableListModal] Tải dữ liệu thẻ thất bại', e);
        cardDataList.value = [];
        cardTotal.value = 0;
      } finally {
        cardLoading.value = false;
      }
    }

    function handleCardPageChange(page: number) {
      cardCurrentPage.value = page;
      loadCardData();
    }

    function handleCardSizeChange(_current: number, size: number) {
      cardPageSize.value = size;
      cardCurrentPage.value = 1;
      loadCardData();
    }

    // ==================== Popup ====================
    const [registerModal, { closeModal }] = useModalInner((data) => {
      searchText.value = '';

      // Đồng bộ trạng thái đã chọn của thẻ
      cardSelectedKeys.value = (data.selectedRowKeys || []).map(String);
      cardSelectedRecordsMap.value = {};
      for (const row of data.selectedRows || []) {
        if (row && row['id'] != null) {
          cardSelectedRecordsMap.value[String(row['id'])] = row;
        }
      }

      setTimeout(async () => {
        cardCurrentPage.value = 1;
        await loadCardData();
        resetBodyStyle();
      }, 100);
    });

    function handleCancel() {
      closeModal();
    }

    const submitDisabled = computed(() => cardSelectedKeys.value.length === 0);

    const submitLoading = ref(false);
    function handleSubmit() {
      submitLoading.value = true;
      const arr = Object.values(cardSelectedRecordsMap.value);
      if (arr && arr.length > 0) {
        emit('success', arr);
        closeModal();
      }
      setTimeout(() => {
        submitLoading.value = false;
      }, 200);
    }

    // ==================== Yêu cầu dữ liệu ====================
    function queryTableData(params) {
      const url = '/online/cgform/api/getData/' + props.id;
      return defHttp.get({ url, params });
    }

    const onlineTableContext = {
      isPopList: true,
      reloadTable() {},
      isTree() {
        return false;
      },
    };
    const extConfigJson = ref<any>({});

    const { columns, handleColumnResult } = useTableColumns(onlineTableContext, extConfigJson);

    function getColumnList() {
      const url = '/online/cgform/api/getColumns/' + props.id;
      return new Promise((resolve, reject) => {
        defHttp.get({ url }, { isTransformResponse: false }).then((res) => {
          if (res.success) {
            resolve(res.result);
          } else {
            $message.warning(res.message);
            reject();
          }
        });
      });
    }

    const modalTitle = ref('');
    watch(
      () => props.id,
      async () => {
        let columnResult: any = await getColumnList();
        handleColumnResult(columnResult);
        modalTitle.value = columnResult.description;
      },
      { immediate: true }
    );

    function onSearch() {
      cardCurrentPage.value = 1;
      loadCardData();
    }
    const eqConditonTypes = ['int', 'double', 'Date', 'Datetime', 'BigDecimal'];
    function addQueryParams(params) {
      let text = searchText.value;
      if (!text) {
        params['superQueryMatchType'] = 'or';
        params['superQueryParams'] = '';
        return params;
      }
      let arr = columns.value;
      let conditions: any[] = [];
      if (arr && arr.length > 0) {
        for (let item of arr) {
          if (item.dbType) {
            if (item.dbType == 'string') {
              conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'like', val: text });
            } else if (item.dbType == 'Date') {
              if (text.length == '2020-10-10'.length) {
                conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
              }
            } else if (item.dbType == 'Datetime') {
              if (text.length == '2020-10-10 10:10:10'.length) {
                conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
              }
            } else if (eqConditonTypes.indexOf(item.dbType)) {
              conditions.push({ field: item.dataIndex, type: item.dbType.toLowerCase(), rule: 'eq', val: text });
            }
          }
        }
      }
      params['superQueryMatchType'] = 'or';
      params['superQueryParams'] = encodeURI(JSON.stringify(conditions));
      return params;
    }

    return {
      registerModal,
      handleCancel,
      submitDisabled,
      submitLoading,
      handleSubmit,

      searchText,
      onSearch,
      modalTitle,

      popModalFixedWidth,
      popBodyStyle,

      // Chế độ thẻ
      cardLoading,
      cardDataList,
      cardTotal,
      cardCurrentPage,
      cardPageSize,
      cardSelectedKeys,
      getCardKey,
      isCardSelected,
      handleCardToggle,
      clearCardSelection,
      cardOtherDisplayFields,
      getCardMainContent,
      hasCardImage,
      getCardImageSrc,
      handleCardImageError,
      handleCardPageChange,
      handleCardSizeChange,
    };
  },
});
</script>

<style scoped lang="less">
/* ===== Thanh công cụ ===== */
.ltm-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

/* ===== Kiểu chung của thẻ ===== */
.atm-selected-tip {
  font-size: 13px;
  color: #1890ff;
}

.atm-clear-link {
  margin-left: 6px;
  color: #ff4d4f;
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
}

.atm-spin-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.atm-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
  padding: 0 4px;
  min-height: 100px;
}

.atm-card {
  position: relative;
  display: inline-flex;
  flex-direction: row;
  width: 100%;
  background: #fff;
  border-radius: 4px;
  border: 2px solid transparent;
  box-shadow:
    0 1px 4px rgba(0, 0, 0, 0.12),
    0 0 2px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition:
    box-shadow 0.2s,
    border-color 0.2s;

  &:hover {
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
  }

  &.atm-card--selected {
    border-color: #1890ff;
  }
}

.atm-card-check {
  position: absolute;
  top: 6px;
  right: 8px;
  z-index: 1;
  line-height: 1;
  font-size: 16px;
}

.atm-check-icon--on {
  color: #1890ff;
}

.atm-check-icon--off {
  color: #d9d9d9;
}

.atm-card-inner {
  flex: 1;
  padding: 12px 28px 12px 14px;
  overflow: hidden;

  &.atm-has-image {
    width: calc(100% - 80px);
  }
}

.atm-card-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 20px;
  margin-bottom: 6px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.atm-card-fields {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.atm-card-field-row {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 20px;
  overflow: hidden;
}

.atm-field-label {
  font-size: 12px;
  color: #9e9e9e;
  white-space: nowrap;
  flex-shrink: 0;
}

.atm-field-value {
  font-size: 12px;
  color: #555;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.atm-card-image {
  width: 72px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 8px 8px 0;
  overflow: hidden;

  img {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
  }
}

.atm-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
