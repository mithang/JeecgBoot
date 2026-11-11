<template>
  <!--popupHộp chọn-->
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="title"
      :width="1200"
      @ok="handleSubmit"
      @cancel="handleCancel"
      cancelText="Đóng"
      wrapClassName="j-popup-modal"
      @visible-change="visibleChange"
    >
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" v-if="showSearchFlag" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" @keyup.enter.native="searchQuery">
          <a-row :gutter="24">
            <template v-for="(item, index) in queryInfo">
              <template v-if="item.hidden === '1'">
                <a-col :md="8" :sm="24" :key="'query' + index" v-show="toggleSearchStatus">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
              <template v-else>
                <a-col :md="8" :sm="24" :key="'query' + index">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
            </template>

            <a-col :md="8" :sm="8" v-if="showAdvancedButton">
              <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Truy vấn</a-button>
                  <a-button preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">Đặt lại</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? 'Thu gọn' : 'Mở rộng' }}
                    <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                  </a>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <BasicTable
        ref="tableRef"
        :canResize="false"
        :bordered="true"
        :loading="loading"
        :rowKey="rowkey ? rowkey : combineRowKey"
        :columns="columns"
        :showIndexColumn="false"
        :dataSource="dataSource"
        :pagination="pagination"
        :rowSelection="rowSelection"
        @row-click="clickThenCheck"
        @change="handleChangeInTable"
      >
        <template #tableTitle></template>
         <template #bodyCell="{text, column}">
          <template v-if="column.fieldType === 'Image'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">Không có hình ảnh</span>
            <img v-else :src="getImgView(text)" alt="Hình ảnh không tồn tại" class="cellIamge" @click="viewOnlineCellImage($event, text)" />
          </template>
        </template>
      </BasicTable>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, unref, ref, watch, watchEffect, reactive, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { usePopBiz } from '/@/components/jeecg/OnLine/hooks/usePopBiz';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { createImgPreview } from '/@/components/Preview/index';

  export default defineComponent({
    name: 'JPopupOnlReportModal',
    components: {
      //Tại đây cần tải bất đồng bộBasicTable
      BasicModal,
      SearchFormItem: createAsyncComponent(() => import('/@/components/jeecg/OnLine/SearchFormItem.vue'), { loading: false }),
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: ['multi', 'code', 'sorter', 'groupId', 'param','showAdvancedButton', 'getFormValues', 'selected', 'rowkey', 'echoRows'],
    emits: ['ok', 'register'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 6 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 18 },
      });
      //Đăng ký popup
      const [register, { closeModal }] = useModalInner();
      const formRef = ref();
      const tableRef = ref();
      const toggleSearchStatus = ref(false);
      const attrs = useAttrs();
      const tableScroll = ref({ x: true });
      // Giải thích logic mã: 【issues/675] Dữ liệu popup của trường bảng con không cập nhật
      const getBindValue = computed(() => {
        return Object.assign({}, unref(props), unref(attrs));
      });
      const [
        {
          visibleChange,
          loadColumnsInfo,
          dynamicParamHandler,
          loadData,
          handleChangeInTable,
          combineRowKey,
          clickThenCheck,
          filterUnuseSelect,
          getOkSelectRows,
        },
        {
          visible,
          rowSelection,
          checkedKeys,
          selectRows,
          pagination,
          dataSource,
          columns,
          loading,
          title,
          iSorter,
          queryInfo,
          queryParam,
          dictOptions,
        },
      ] = usePopBiz(getBindValue,tableRef);

      const showSearchFlag = computed(() => unref(queryInfo) && unref(queryInfo).length > 0);
      /**
       *Lắng nghecode
       */
      watch(
        () => props.code,
        () => {
          loadColumnsInfo();
        }
      );
      /**
       *Lắng nghe tham số động của popup, hỗ trợ cú pháp biến hệ thống
       */
      watch(
        () => props.param,
        () => {
          // Giải thích logic mã: 【issues/901] Component JPopup sau khi cấu hình tham số param thì bất thường
          if (visible.value) {
            dynamicParamHandler();
            loadData();
          }
        }
      );
      /**
       *Lắng nghe trường sắp xếp sorter
       */
      watchEffect(() => {
        if (props.sorter) {
          let arr = props.sorter.split('=');
          if (arr.length === 2 && ['asc', 'desc'].includes(arr[1].toLowerCase())) {
            iSorter.value = { column: arr[0], order: arr[1].toLowerCase() };
            // Trường sắp xếp được kiểm soát
            unref(columns).forEach((col) => {
              if (col.dataIndex === unref(iSorter).column) {
                col['sortOrder'] = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
              } else {
                col['sortOrder'] = false;
              }
            });
          } else {
            console.warn('[JPopup] Tham số sorter không hợp lệ');
          }
        }
      });

      // Giải thích logic mã: VUEN-1156 popup Khi có nhiều dữ liệu và có phân trang, chọn trang khác, đóng popup rồi mở lại, phân trang vẫn chọn trang đã click lần trước, nhưng dữ liệu lại là dữ liệu của trang đầu tiên, chưa được làm mới
      watch(
        () => pagination.current,
        (current) => {
          if (current) {
            tableRef.value.setPagination({
              current: current,
            });
          }
        }
      );

      function handleToggleSearch() {
        toggleSearchStatus.value = !unref(toggleSearchStatus);
      }
      /**
       * Hủy/Đóng
       */
      function handleCancel() {
        closeModal();
        checkedKeys.value = [];
        selectRows.value = [];
        // Giải thích logic mã: 【issues/742] Sau khi chọn rồi xóa, giá trị mặc định vẫn tồn tại
        tableRef.value.clearSelectedRowKeys();
      }

      /**
       *Xác nhận gửi
       */
      function handleSubmit() {
        filterUnuseSelect();
        if (!props.multi && unref(selectRows) && unref(selectRows).length > 1) {
          createMessage.warning('Chỉ được chọn một bản ghi');
          return false;
        }
        if (!unref(selectRows) || unref(selectRows).length == 0) {
          createMessage.warning('Vui lòng chọn ít nhất một bản ghi');
          return false;
        }
        // Giải thích logic mã: VUEN-1155 popup Khi chọn dữ liệu, sẽ chọn nhiều bản ghi trùng lặp
        let rows = getOkSelectRows!();
        emit('ok', rows);
        handleCancel();
      }

      /**
       * Truy vấn
       */
      function searchQuery() {
        loadData(1);
      }
      /**
       * Đặt lại
       */
      function searchReset() {
        queryParam.value = {};
        loadData(1);
      }

      /**
       * 2024-07-24
       * liaozhiyang
       * 【TV360X-1756] báo cáo thêm loại hình ảnh
       * Hình ảnh
       * @param text
       */
      function getImgView(text) {
        if (text && text.indexOf(',') > 0) {
          text = text.substring(0, text.indexOf(','));
        }
        return getFileAccessHttpUrl(text);
      }
      /**
       * 2024-07-24
       * liaozhiyang
       * 【TV360X-1756] báo cáo thêm loại hình ảnh
       * Xem trước hình ảnh cell trong danh sách
       * @param text
       */
      function viewOnlineCellImage(e, text) {
        e.stopPropagation();
        if (text) {
          let imgList: any = [];
          let arr = text.split(',');
          for (let str of arr) {
            if (str) {
              imgList.push(getFileAccessHttpUrl(str));
            }
          }
          createImgPreview({ imageList: imgList });
        }
      }
      // Giải thích logic mã: 【issues/3656] Hiển thị lại popupdict
      watchEffect(() => {
        if (props.selected && props.rowkey && unref(visible)) {
          const selected = Array.isArray(props.selected) ? props.selected : [props.selected];
          checkedKeys!.value = selected.map((item) => item[props.rowkey]);
          selectRows!.value = selected;
        }
      });

      // update-begin--author:liaozhiyang---date:20261029---for: khi chỉnh sửa popup, popup hiển thị lại trạng thái đã chọn
      // Mục đang chờ khớp:{ match: Giá trị trường nguồn, placeholder: Hàng giữ chỗ, matched: Đã khớp chưa }
      let echoPending: any[] = [];
      /**
       * Khi popup mở ra, trước tiên đưa mục cần khớp vào danh sách đã chọn dưới dạng hàng giữ chỗ (đảm bảo dữ liệu gốc không ở trang hiện tại mà xác nhận trực tiếp cũng không bị mất giá trị)
       */
      function initEcho() {
        echoPending = [];
        const echoRows = props.echoRows;
        if (!Array.isArray(echoRows) || echoRows.length === 0) {
          return;
        }
        const rows = props.multi ? echoRows : echoRows.slice(0, 1);
        echoPending = rows.map((match, index) => ({
          match,
          placeholder: { ...match, rowIndex: `__echo_${index}` },
          matched: false,
        }));
        selectRows.value = echoPending.map((item) => item.placeholder);
        checkedKeys.value = selectRows.value.map((row) => combineRowKey(row));
        syncTableSelection();
      }
      /**
       * Khớp mục cần khớp trong dữ liệu trang hiện tại: tất cả các trường nguồn đều bằng nhau mới tính là trúng, trúng nhiều mục thì lấy mục đầu tiên, các mục đã khớp sẽ không khớp lại ở các trang sau
       */
      function matchEchoRows(records) {
        const pending = echoPending.filter((item) => !item.matched);
        if (!pending.length || !records?.length) {
          return;
        }
        // Hàng đã bị mục hiển thị lại khác chiếm, cùng một hàng chỉ tương ứng với một mục hiển thị lại
        const claimedKeys = new Set(echoPending.filter((item) => item.matched && item.rowKey).map((item) => item.rowKey));
        let changed = false;
        pending.forEach((item) => {
          const fields = Object.keys(item.match);
          const row = records.find((record) => {
            return !claimedKeys.has(combineRowKey(record)) && fields.every((field) => String(record[field] ?? '') === String(item.match[field] ?? ''));
          });
          if (!row) {
            return;
          }
          const placeholderKey = combineRowKey(item.placeholder);
          const rowKey = combineRowKey(row);
          const index = checkedKeys.value.indexOf(placeholderKey);
          item.matched = true;
          item.rowKey = rowKey;
          claimedKeys.add(rowKey);
          // Hàng giữ chỗ đã bị hủy (ví dụ khi chọn đơn mà đổi sang hàng khác), không hiển thị lại nữa
          if (index === -1) {
            return;
          }
          if (checkedKeys.value.includes(rowKey)) {
            // Dòng này đã được tick thủ công, chỉ cần loại bỏ dòng giữ chỗ
            checkedKeys.value.splice(index, 1);
            selectRows.value = selectRows.value.filter((r) => combineRowKey(r) !== placeholderKey);
          } else {
            checkedKeys.value.splice(index, 1, rowKey);
            selectRows.value = selectRows.value.map((r) => (combineRowKey(r) === placeholderKey ? row : r));
          }
          changed = true;
        });
        changed && syncTableSelection();
      }
      /**
       * Đồng bộ trạng thái tích chọn bên trong BasicTable
       */
      function syncTableSelection() {
        nextTick(() => {
          tableRef.value?.setSelectedRowKeys?.([...checkedKeys.value]);
        });
      }
      watch(
        () => unref(visible),
        (val) => {
          val ? initEcho() : (echoPending = []);
        }
      );
      watch(dataSource, (records) => matchEchoRows(records));
      // update-end--author:liaozhiyang---date:20261029---for: khi chỉnh sửa popup, popup hiển thị lại trạng thái đã chọn
      return {
        attrs,
        register,
        tableScroll,
        dataSource,
        pagination,
        columns,
        rowSelection,
        checkedKeys,
        loading,
        title,
        handleCancel,
        handleSubmit,
        clickThenCheck,
        loadData,
        combineRowKey,
        handleChangeInTable,
        visibleChange,
        queryInfo,
        queryParam,
        tableRef,
        formRef,
        labelCol,
        wrapperCol,
        dictOptions,
        showSearchFlag,
        toggleSearchStatus,
        handleToggleSearch,
        searchQuery,
        searchReset,
        getImgView,
        viewOnlineCellImage,
      };
    },
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 5px;

    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 0;
      white-space: nowrap;
    }
  }
  :deep(.jeecg-basic-table .ant-table-wrapper .ant-table-title){
    min-height: 0;
  }
  .cellIamge {
    height: 25px !important;
    margin: 0 auto;
    max-width: 80px;
    font-size: 12px;
    font-style: italic;
    cursor: pointer;
  }
</style>
