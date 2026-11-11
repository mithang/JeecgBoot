<template>
  <div class="auth-field-config">
    <BasicTable @register="registerTable" :loading="tableLoading">
      <template #headerCell="{ column }">
        <template v-if="column.dataIndex === 'switch'">
          <a-switch
            size="small"
            :checked="allSwitch"
            :loading="allSwitchLoading"
            :disabled="!buttonDataSource.length"
            @change="handleChangeAllSwitch"
          />
          Kích hoạt
        </template>
        <template v-else>
          {{ column.customTitle }}
        </template>
      </template>
      <template #switch="{ record }">
        <a-switch size="small" :checked="record.status === 1" :disabled="allSwitchLoading" @change="(flag) => onUpdateStatus(flag, record)" />
      </template>

      <template #control> Hiển thị (có thể thấy) </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';
  import { cloneDeep } from 'lodash-es';
  import { BasicTable, useTable } from '/@/components/Table';
  import { authButtonLoadData, authButtonEnable, authButtonDisable, batchAuthButtonUpdateStatus } from '../auth.api';
  import { authButtonColumns, authButtonFixedList } from '../auth.data';

  export default defineComponent({
    name: 'AuthButtonConfig',
    components: { BasicTable },
    props: {
      headId: {
        type: String,
        default: '',
        required: true,
      },
      // 1bảng đơn 2bảng chính 3Bảng phụ
      tableType: {
        type: Number,
        default: 1,
      },
      isTree: {
        type: String,
        default: 'N',
      },
    },
    setup(props) {
      const cgformId = ref('');
      const pageType = ref(2);
      const pageControlList = ref(3);
      const pageControlForm = ref(5);
      const allSwitch = ref(false);
      const allSwitchLoading = ref(false);
      const tableLoading = ref(false);
      const buttonDataSource = ref<Recordable[]>([]);
      const [registerTable, { reload, getTableRef, setPagination }] = useTable({
        api: loadData,
        rowKey: 'code',
        bordered: true,
        columns: authButtonColumns,
        showIndexColumn: false,
      });

      watch(
        () => props.headId,
        (headId) => {
          cgformId.value = headId.split('?')[0];
          // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-149】Sau khi nhấp kiểm soát quyền để vào trang, phân trang không được đặt lại
          getTableRef().value && setPagination({ current: 1, pageSize: 10 });
          // update-end--author:liaozhiyang---date:20240520---for：【TV360X-149】Sau khi nhấp kiểm soát quyền để vào trang, phân trang không được đặt lại
          reload().catch(() => null);
        },
        { immediate: true }
      );

      /**
       * Lấy nút tích hợp sẵn
       * liaozhiyang
       * 2024-06-14
       * 【TV360X-1017】Quyền nút trong quản lý quyền bảng con chỉ hiển thị các nút cần thiết theo mẫu giao diện của bảng chính
       * */
      const getButtonList = (res) => {
        const buttons = cloneDeep(authButtonFixedList).filter((item) => item.code !== 'addSub' || props.isTree === 'Y');
        if (res.mainRelationType != null && res.mainThemeTemplate != null && props.tableType == 3) {
          // Bảng con (một-một, một-nhiều)
          let result: any = [];
          switch (res.mainThemeTemplate) {
            case 'normal':
            case 'innerTable':
            case 'tab':
              if (res.mainRelationType == 1) {
                // một-một
                result = [];
              } else {
                // Một-nhiều:add(Thêm mới)、popup_add(Thêm mới bằng popup)、update(Chỉnh sửa bảng con)、batch_delete(Xóa hàng loạt)
                result = buttons.filter((item) => ['add', 'popup_add', 'update', 'batch_delete'].includes(item.code));
              }
              break;
            case 'erp':
              // ERP Bảng phụ không trưng bày truy vấn nâng cao, gửi quy trình
              result = buttons.filter((item) => !['super_query', 'bpm'].includes(item.code));
              break;
          }
          return result;
        } else {
          // bảng chính\Bảng đơn: lọc bỏ quyền nút popup chuyên dụng của bảng concode（popup_addThêm mới bằng popup,popup_updateChỉnh sửa bằng popup,updatechỉnh sửa bằng popup)
          // updatecũng là cái cũ của nút chỉnh sửa ở trang danh sáchcode（useListButton.ts:187phán đoán kiểm soát kép), bảng đơn/Bật trên bảng chính sẽ khiến nút chỉnh sửa bị ẩn
          return buttons.filter((item) => !['popup_add', 'popup_update', 'update'].includes(item.code));
        }
      };

      // Tải dữ liệu
      async function loadData(params) {
        let result = await authButtonLoadData(cgformId.value, params);
        let { authList, buttonList } = result;
        let dataSource: Recordable[] = [];
        // concat Nút cố định
        // -update-begin--author:liaozhiyang---date:20240614---for：【TV360X-1017】Quyền nút trong quản lý quyền bảng con chỉ hiển thị các nút cần thiết theo mẫu giao diện của bảng chính
        // Lấy nút tích hợp sẵn
        const buttons = getButtonList(result);
        // -update-end--author:liaozhiyang---date:20240614---for：【TV360X-1017】Quyền nút trong quản lý quyền bảng con chỉ hiển thị các nút cần thiết theo mẫu giao diện của bảng chính
        for (let btn of buttons) {

          // Loại bỏ dữ liệu trùng lặp
          const findBtnIdx = buttonList.findIndex((item) => item.buttonCode === btn.code);
          const findBtn: Recordable = {}
          if (findBtnIdx !== -1) {
            findBtn.title = buttonList[findBtnIdx].buttonName;
            buttonList.splice(findBtnIdx, 1);
          }

          let item = {
            status: 0,
            page: pageControlList.value,
          };
          let auth = authList.find((auth) => auth.code == btn.code);
          Object.assign(btn, item, auth, findBtn);
          dataSource.push(btn);
        }
        // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-187】Bỏ truy vấn nâng cao của quyền nút trong quản lý quyền bảng con
        if (props.tableType == 3) {
          const findIndex = dataSource.findIndex((item) => item.code === 'super_query');
          if (findIndex != -1) {
            dataSource.splice(findIndex, 1);
          }
        }
        // update-end--author:liaozhiyang---date:20240520---for：【TV360X-187】Bỏ truy vấn nâng cao của quyền nút trong quản lý quyền bảng con
        // update-begin--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử
        if ([2, 3].includes(+props.tableType)) {
          const findIndex = dataSource.findIndex((item) => item.code == 'aigc_mock_data');
          if (findIndex != -1) {
            dataSource.splice(findIndex, 1);
          }
        }
        // update-end--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử
        // concat Nút tùy chỉnh của truy vấn
        const resultData = concatCustomButton(authList, buttonList, dataSource);
        buttonDataSource.value = resultData;
        syncAllSwitchStatus();
        return resultData;
      }

      function concatCustomButton(authList: any[], buttonList: any[], dataSource: any[]) {
        for (let btn of buttonList) {
          //update-begin-author:taoyan date:2022-5-25 for: VUEN-1103 Nút tùy chỉnh, sau khi bật kiểm soát quyền, mở lại chưa lưu lên
          let auth = authList.find((auth) => auth.code == btn.buttonCode);
          //update-end-author:taoyan date:2022-5-25 for: VUEN-1103 Nút tùy chỉnh, sau khi bật kiểm soát quyền, mở lại chưa lưu lên
          let item = {
            code: btn.buttonCode,
            title: btn.buttonName,
            status: 0,
            page: btn.buttonStyle == 'form' ? pageControlForm.value : pageControlList.value,
          };
          dataSource.push(Object.assign(item, auth));
        }
        return dataSource;
      }

      async function onUpdateStatus(flag, record) {
        await (flag ? doEnableAuthButton(record) : doDisableAuthButton(record));
        syncAllSwitchStatus();
      }

      function syncAllSwitchStatus() {
        allSwitch.value = buttonDataSource.value.length > 0 && buttonDataSource.value.every((item) => item.status === 1);
      }

      async function handleChangeAllSwitch(checked) {
        const changedButtons = buttonDataSource.value.filter((item) => (checked ? item.status !== 1 : item.status === 1));
        if (!changedButtons.length) return;
        allSwitchLoading.value = true;
        tableLoading.value = true;
        try {
          await batchAuthButtonUpdateStatus(
            changedButtons.map((item) => ({
              id: item.id,
              cgformId: cgformId.value,
              code: item.code,
              control: 5,
              page: item.page,
              status: checked ? 1 : 0,
            }))
          );
        } finally {
          await reload().catch(() => null);
          allSwitchLoading.value = false;
          tableLoading.value = false;
        }
      }

      // Kích hoạt quyền nút
      async function doEnableAuthButton(record: Recordable) {
        let result = await authButtonEnable({
          id: record.id,
          code: record.code,
          page: record.page,
          cgformId: cgformId.value,
          type: pageType.value,
          control: 5,
          status: 1,
        });
        record.id = result.id;
        record.status = 1;
      }

      // Quyền vô hiệu hóa nút
      async function doDisableAuthButton(record: Recordable) {
        await authButtonDisable(record.id);
        record.status = 0;
      }

      return { registerTable, onUpdateStatus, allSwitch, allSwitchLoading, tableLoading, buttonDataSource, handleChangeAllSwitch };
    },
  });
</script>

<style lang="less" scoped></style>
