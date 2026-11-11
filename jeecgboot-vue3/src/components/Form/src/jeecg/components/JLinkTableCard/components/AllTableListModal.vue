<template>
  <BasicModal
    @register="registerModal"
    :width="popModalFixedWidth"
    :dialogStyle="{ top: '70px' }"
    :bodyStyle="modalBodyStyle"
    title="Chọn bản ghi"
    wrapClassName="jeecg-all-table-pop-list-modal"
  >
    <template #footer>
      <a-button key="back" @click="handleCancel">Đóng</a-button>
      <a-button :disabled="submitDisabled" key="submit" type="primary" @click="handleSubmit" :loading="submitLoading">Xác nhận</a-button>
    </template>

    <a-form-item-rest>
      <a-form-item class="atm-form-item">
        <div class="atm-layout">
          <!-- Thanh tìm kiếm + Gợi ý đã chọn -->
          <div class="atm-toolbar">
            <a-input-search
              v-model:value="searchText"
              placeholder="Vui lòng nhập từ khóa, nhấn Enter để tìm kiếm"
              style="width: 240px"
              @search="doSearch"
              @pressEnter="doSearch"
            />
            <span v-if="selectedKeys.length > 0" class="atm-selected-tip">
              Đã chọn {{ selectedKeys.length }} bản ghi
              <a class="atm-clear-link" @click="clearSelection">Xóa sạch</a>
            </span>
          </div>

          <div class="atm-content">
            <!-- Đang tải -->
            <div v-if="loading" class="atm-spin-wrap">
              <a-spin />
            </div>

            <!-- Không có dữ liệu -->
            <a-empty v-else-if="dataList.length === 0" class="atm-empty" description="Không có dữ liệu" />

            <!-- Lưới thẻ -->
            <div v-else class="atm-card-grid">
              <div
                v-for="record in dataList"
                :key="getRecordKey(record)"
                class="atm-card"
                :class="{ 'atm-card--selected': isSelected(record) }"
                @click="handleToggle(record)"
              >
                <!-- Chỉ ở chế độ đa chọn mới hiển thị biểu tượng đã chọn ở góc trên bên phải -->
                <span v-if="multi" class="atm-card-check">
                  <CheckSquareFilled v-if="isSelected(record)" class="atm-check-icon--on" />
                  <BorderOutlined v-else class="atm-check-icon--off" />
                </span>

                <!-- Khu vực nội dung -->
                <div class="atm-card-inner" :class="{ 'atm-has-image': hasImage(record) }">
                  <div class="atm-card-title">{{ getMainContent(record) }}</div>
                  <div class="atm-card-fields">
                    <div v-for="field in otherTextFields" :key="field" class="atm-card-field-row">
                      <span class="atm-field-label">{{ field }}</span>
                      <span class="atm-field-value">{{ record[field] }}</span>
                    </div>
                  </div>
                </div>

                <!-- Khu vực hình ảnh -->
                <div v-if="hasImage(record)" class="atm-card-image">
                  <img :src="getImageSrc(record)" alt="" @error="handleImageError" />
                </div>
              </div>
            </div>
          </div>

          <!-- Phân trang -->
          <div class="atm-pagination">
            <a-pagination
              v-model:current="currentPage"
              :total="total"
              :pageSize="pageSize"
              :show-total="(t) => `Tổng cộng ${t} mục`"
              show-size-changer
              @change="handlePageChange"
              @showSizeChange="handleSizeChange"
            />
          </div>
        </div>
      </a-form-item>
    </a-form-item-rest>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { defHttp } from '/@/utils/http/axios';
  import { useFixedHeightModal } from '../hooks/useLinkTable';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { CheckSquareFilled, BorderOutlined } from '@ant-design/icons-vue';
  import placeholderImage from '/@/assets/images/placeholderImage.png';

  /** Gọi backend /sys/dict/queryTableDataForLinkRecord API */
  function queryTableData(params: Record<string, any>) {
    return defHttp.get({ url: '/sys/dict/queryTableDataForLinkRecord', params });
  }

  export default defineComponent({
    name: 'AllTableListModal',
    components: { BasicModal, CheckSquareFilled, BorderOutlined },
    props: {
      tableName: { type: String, default: '' },
      textField: { type: String, default: '' },
      valueField: { type: String, default: '' },
      imageField: { type: String, default: '' },
      multi: { type: Boolean, default: false },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
      const modalBodyStyle = computed(() => ({ ...popBodyStyle.value, overflow: 'hidden', padding: '16px 20px' }));

      // ---------- Trạng thái dữ liệu ----------
      const loading = ref(false);
      const dataList = ref<any[]>([]);
      const total = ref(0);
      const currentPage = ref(1);
      const pageSize = ref(10);
      const searchText = ref('');

      // ---------- Trạng thái đã chọn (lưu bền vững qua các trang) ----------
      const selectedKeys = ref<string[]>([]);
      const selectedRecordsMap = ref<Record<string, any>>({});

      function getRecordKey(record: any): string {
        return String(record[props.valueField] ?? '');
      }

      function isSelected(record: any): boolean {
        return selectedKeys.value.includes(getRecordKey(record));
      }

      function handleToggle(record: any) {
        const key = getRecordKey(record);
        if (props.multi) {
          const idx = selectedKeys.value.indexOf(key);
          if (idx >= 0) {
            selectedKeys.value.splice(idx, 1);
            delete selectedRecordsMap.value[key];
          } else {
            selectedKeys.value.push(key);
            selectedRecordsMap.value[key] = record;
          }
        } else {
          selectedKeys.value = [key];
          selectedRecordsMap.value = { [key]: record };
        }
      }

      function clearSelection() {
        selectedKeys.value = [];
        selectedRecordsMap.value = {};
      }

      // ---------- Tính toán trường ----------
      const mainTextField = computed(() => {
        return (props.textField || '').split(',').filter(Boolean)[0] || props.valueField;
      });

      const otherTextFields = computed(() => {
        return (props.textField || '').split(',').filter(Boolean).slice(1);
      });

      function getMainContent(record: any): string {
        return record[mainTextField.value] ?? '';
      }

      function hasImage(record: any): boolean {
        return !!(props.imageField && record[props.imageField]);
      }

      function getImageSrc(record: any): string {
        if (!props.imageField) return '';
        let url = record[props.imageField];
        if (typeof url === 'string') url = url.split(',')[0];
        return getFileAccessHttpUrl(url);
      }

      function handleImageError(e: Event) {
        (e.target as HTMLImageElement).src = placeholderImage;
      }

      // ---------- Tải dữ liệu ----------
      async function loadData() {
        if (!props.tableName || !props.valueField || !props.textField) return;

        const showFieldSet = new Set((props.textField || '').split(',').filter(Boolean));
        if (props.imageField) showFieldSet.add(props.imageField);
        const showFields = Array.from(showFieldSet).join(',');

        loading.value = true;
        try {
          const res = await queryTableData({
            tableName: props.tableName,
            showFields,
            valueField: props.valueField,
            keyword: searchText.value || undefined,
            pageNo: currentPage.value,
            pageSize: pageSize.value,
          });
          dataList.value = res?.records || [];
          total.value = res?.total || 0;
        } catch (e) {
          console.error('[AllTableListModal] Tải dữ liệu thất bại', e);
          dataList.value = [];
          total.value = 0;
        } finally {
          loading.value = false;
        }
      }

      function doSearch() {
        currentPage.value = 1;
        loadData();
      }

      function handlePageChange(page: number) {
        currentPage.value = page;
        loadData();
      }

      function handleSizeChange(_current: number, size: number) {
        pageSize.value = size;
        currentPage.value = 1;
        loadData();
      }

      // ---------- Vòng đời popup ----------
      const [registerModal, { closeModal }] = useModalInner((data) => {
        // Đặt lại tìm kiếm và phân trang
        searchText.value = '';
        currentPage.value = 1;
        dataList.value = [];
        total.value = 0;

        // Khôi phục bản ghi đã chọn được truyền vào
        selectedKeys.value = (data.selectedRowKeys || []).map(String);
        selectedRecordsMap.value = {};
        for (const row of data.selectedRows || []) {
          if (row && row[props.valueField] != null) {
            selectedRecordsMap.value[String(row[props.valueField])] = row;
          }
        }

        // Tải chậm (đợi popup được mount hoàn toàn rồi mới yêu cầu)
        setTimeout(() => {
          loadData();
          resetBodyStyle();
        }, 100);
      });

      // ---------- Gửi ----------
      const submitLoading = ref(false);
      const submitDisabled = computed(() => selectedKeys.value.length === 0);

      function handleCancel() {
        closeModal();
      }

      function handleSubmit() {
        submitLoading.value = true;
        const rows = Object.values(selectedRecordsMap.value);
        if (rows.length > 0) {
          emit('success', rows);
          closeModal();
        }
        setTimeout(() => {
          submitLoading.value = false;
        }, 200);
      }

      return {
        registerModal,
        popModalFixedWidth,
        modalBodyStyle,
        loading,
        dataList,
        total,
        currentPage,
        pageSize,
        searchText,
        selectedKeys,
        getRecordKey,
        isSelected,
        handleToggle,
        clearSelection,
        otherTextFields,
        getMainContent,
        hasImage,
        getImageSrc,
        handleImageError,
        doSearch,
        handlePageChange,
        handleSizeChange,
        handleCancel,
        handleSubmit,
        submitDisabled,
        submitLoading,
      };
    },
  });
</script>

<style scoped lang="less">
  .atm-form-item {
    height: 100%;
    margin-bottom: 0;

    :deep(.ant-form-item-row),
    :deep(.ant-form-item-control),
    :deep(.ant-form-item-control-input),
    :deep(.ant-form-item-control-input-content) {
      height: 100%;
    }
  }

  .atm-layout {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
  }

  .atm-toolbar {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    gap: 12px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .atm-selected-tip {
    padding: 4px 10px;
    font-size: 13px;
    color: #1890ff;
    background: #e6f7ff;
    border-radius: 4px;
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
    height: 100%;
  }

  .atm-content {
    flex: 1;
    min-height: 0;
    padding: 16px 2px;
    overflow-y: auto;
  }

  .atm-empty {
    display: flex;
    flex-direction: column;
    justify-content: center;
    height: 100%;
    margin: 0;
  }

  .atm-card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    align-content: start;
    gap: 12px;
  }

  /* Thẻ */
  .atm-card {
    position: relative;
    display: inline-flex;
    flex-direction: row;
    width: 100%;
    background: #fff;
    min-height: 104px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    cursor: pointer;
    transition:
      box-shadow 0.2s ease,
      border-color 0.2s ease,
      transform 0.2s ease;

    &:hover {
      border-color: #91caff;
      box-shadow: 0 6px 16px rgba(24, 144, 255, 0.1);
      transform: translateY(-1px);
    }

    &.atm-card--selected {
      border-color: #1890ff;
      background: #f6fbff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.12);
    }
  }

  /* Icon đã chọn (góc trên bên phải, nằm trong thẻ) */
  .atm-card-check {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 1;
    line-height: 1;
    font-size: 18px;
  }

  .atm-check-icon--on {
    color: #1890ff;
  }

  .atm-check-icon--off {
    color: #d9d9d9;
  }

  /* Khu vực nội dung */
  .atm-card-inner {
    flex: 1;
    min-width: 0;
    padding: 16px 34px 14px 16px;
    overflow: hidden;

    &.atm-has-image {
      width: calc(100% - 80px);
    }
  }

  .atm-card-title {
    margin-bottom: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #262626;
    line-height: 22px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .atm-card-fields {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .atm-card-field-row {
    display: flex;
    align-items: center;
    gap: 6px;
    line-height: 20px;
    overflow: hidden;
  }

  .atm-field-label {
    font-size: 12px;
    color: #8c8c8c;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .atm-field-value {
    font-size: 12px;
    color: #595959;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  /* Khu vực hình ảnh */
  .atm-card-image {
    width: 88px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px 16px 12px 0;
    overflow: hidden;

    img {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: #f5f5f5;
      border: 1px solid #f0f0f0;
      object-fit: cover;
    }
  }

  /* Phân trang */
  .atm-pagination {
    display: flex;
    justify-content: flex-end;
    flex-shrink: 0;
    padding-top: 14px;
    border-top: 1px solid #f0f0f0;
  }

  @media (max-width: 992px) {
    .atm-card-grid {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    }

    .atm-toolbar {
      flex-wrap: wrap;
    }
  }
</style>
