<template>
  <div :id="tableName + '_form'">
    <!-- Nút in của báo cáo Jimu, chỉ khi đã cấu hình reportUrl mới hiển thị -->
    <div
      v-if="!!formData.id && !!onlineExtConfigJson.reportPrintShow"
      style="text-align: right; position: absolute; top: 15px; right: 20px; z-index: 999"
    >
      <PrinterOutlined title="In" @click="onOpenReportPrint" style="font-size: 16px" />
    </div>

    <a-tabs
      class="tabTheme"
      @change="onTabChange"
      v-model:activeKey="subActiveKey"
    >
      <a-tab-pane tab="bảng chính" :key="'-1'">
        <detail-form :schemas="detailFormSchemas" :data="formData" :span="formSpan" />
      </a-tab-pane>
      <template v-if="hasSubTable && showSub">
        <a-tab-pane v-for="(sub, index) in subTabInfo" :tab="sub.describe" :key="index + ''" :forceRender="true">
          <div :style="{ 'overflow-y': 'auto', 'overflow-x': 'hidden', 'max-height': subFormHeight + 'px' }" v-if="sub.relationType == 1">
            <!-- bảng con-một-một -->
            <online-sub-form-detail
              :key="subReloadKey"
              :table="sub.key"
              :form-template="sub.formTemplate"
              :main-id="getSubTableForeignKeyValue(sub.foreignKey)"
              :properties="sub.properties"
            >
            </online-sub-form-detail>
          </div>
          <div v-else>
            <!-- bảng con-một-nhiều -->
            <JVxeTable
              v-if="showStatus[sub.key]"
              :ref="refMap[sub.key]"
              toolbar
              keep-source
              :row-number="rowNumber"
              row-selection
              :height="subTableHeight"
              :disabled="true"
              :columns="sub.columns"
              :dataSource="subDataSource[sub.key]"
              :authPre="getSubTableAuthPre(sub.key)"
            />
            <a-spin v-else :spinning="true" />
          </div>
        </a-tab-pane>
      </template>
    </a-tabs>
    <Loading :loading="loading" :absolute="false" />
    <slot name="bottom"></slot>
  </div>
</template>

<script lang="ts">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref, reactive, watch } from 'vue';
  import { Loading } from '/@/components/Loading';
  import { getToken } from '/@/utils/auth';
  import { goJmReportViewPage } from '/@/utils';
  import { PrinterOutlined } from '@ant-design/icons-vue';
  import DetailForm from '../../../extend/form/DetailForm.vue';
  import OnlineSubFormDetail from '../../comp/OnlineSubFormDetail.vue';
  import { getDetailFormSchemas } from '../../../hooks/auto/useAutoForm';
  import { defHttp } from '/@/utils/http/axios';
  import { ERP, TAB } from '../../../util/constant';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  
  export default {
    name: 'OnlineTabFormDetail',
    components: {
      DetailForm,
      Loading,
      PrinterOutlined,
      OnlineSubFormDetail,
    },
    props: {
      id: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      isTree: {
        type: Boolean,
        default: false,
      },
      pidField: {
        type: String,
        default: '',
      },
      submitTip: {
        type: Boolean,
        default: true,
      },
      showSub: {
        type: Boolean,
        default: true,
      },
      themeTemplate: {
        type: String,
        default: '',
      },
      tabIndex: {
        type: String,
        default: '',
      },
    },
    emits: ['success', 'rendered'],
    setup(props, { emit }) {
      console.log('onlineForm-setup》》');
      const { createMessage: $message } = useMessage();
      const { getIsMobile } = useAppInject();

      const tableName = ref('');
      const single = ref(true);
      // Trạng thái tải
      const loading = ref(false);
      const tableType = ref(1);
      const formData = ref<any>({});
      // update-begin-author:liaozhiyang---date:20240313---for：【QQYUN-9034】onlinePopup: thiết lập chiều cao nội dung của bảng con một-một ở phía di động không hợp lý
      const subFormHeight = ref('auto');
      // update-end-author:liaozhiyang---date:20240313---for：【QQYUN-9034】onlinePopup: thiết lập chiều cao nội dung của bảng con một-một ở phía di động không hợp lý
      const subReloadKey = ref(0);
      // Chiều cao bảng dữ liệu của bảng con
      // 【VUEN-803】Cố định bảng con một-nhiều340chiều cao, sửa vấn đề component cột tùy chỉnh bị che khuất
      const subTableHeight = ref(340);
      const subActiveKey = ref('0');

      const rowNumber = ref(getIsMobile.value ? false : true);

      watch(
        () => props.tabIndex,
        (value, oldValue) => {
          subActiveKey.value = value;
          if (oldValue) {
            //Lần đầu vào không thực thi, sau đó khi lắng nghe thấy thay đổi mới thực thi.(Giao diện chi tiết số thứ tự bị lệch vị trí, nhấp một cái là bình thường)
            onTabChange();
          }
        },
        {
          immediate: true,
        }
      );
      /**
       * onlineCấu hình mở rộng biểu mẫu
       */
      const onlineExtConfigJson = reactive({
        reportPrintShow: 0,
        reportPrintUrl: '',
        joinQuery: 0,
        modelFullscreen: 0,
        modalMinWidth: '',
      });

      const { detailFormSchemas, hasSubTable, subTabInfo, refMap, showStatus, subDataSource, createFormSchemas, formSpan } =
        getDetailFormSchemas(props);

      /**
       * Xử lý cấu hình mở rộng
       */
      function handleExtConfigJson(jsonStr) {
        let extConfigJson = { reportPrintShow: 0, reportPrintUrl: '', joinQuery: 0, modelFullscreen: 1, modalMinWidth: '' };
        if (jsonStr) {
          extConfigJson = JSON.parse(jsonStr);
        }
        Object.keys(extConfigJson).map((k) => {
          onlineExtConfigJson[k] = extConfigJson[k];
        });
      }

      // Render biểu mẫu
      async function createRootProperties(data) {
        tableType.value = data.head.tableType;
        tableName.value = data.head.tableName;
        single.value = data.head.tableType == 1;
        handleExtConfigJson(data.head.extConfigJson);
        createFormSchemas(data.schema.properties);
        emit('rendered', onlineExtConfigJson);
      }

      /**
       * status: Có phải trang sửa đổi hay không
       * record: Dữ liệu hàng của trang danh sách
       * param： Danh sách dạng cây thêm nút con nút cấp cha được truyền vàoid
       * */
      async function show(_status, record) {
        console.log('Vào chi tiết biểu mẫu 》》form', record);
        // -update-begin--author:liaozhiyang---date:20251209---for：【QQYUN-13970】Sau khi chỉnh sửa bảng con một-một, xem chi tiết không được cập nhật
        subReloadKey.value++;
        // -update-end--author:liaozhiyang---date:20251209---for：【QQYUN-13970】Sau khi chỉnh sửa bảng con một-một, xem chi tiết không được cập nhật
        await edit(record);
        changeShowStatus(true);
      }

      function getFormData(dataId) {
        let url = `/online/cgform/api/detail/${props.id}/${dataId}`;
        return new Promise((resolve, reject) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              if (res.success) {
                resolve(res.result);
              } else {
                reject();
                $message.warning(res.message);
              }
            })
            .catch(() => {
              reject();
            });
        });
      }

      //update-begin-author:taoyan date:2023-2-13 for: QQYUN-4226【vue3】online bảng con một-nhiều Giao diện chi tiết, số thứ tự bị lệch vị trí Nhấp một cái vào bảng của bảng con là bình thường trở lại
      function changeShowStatus(flag) {
        Object.keys(showStatus).map((k) => {
          showStatus[k] = flag;
        });
      }

      function onTabChange() {
        changeShowStatus(false);
        setTimeout(() => {
          changeShowStatus(true);
        }, 300);
      }
      //update-end-author:taoyan date:2023-2-13 for: QQYUN-4226【vue3】online bảng con một-nhiều Giao diện chi tiết, số thứ tự bị lệch vị trí Nhấp một cái vào bảng của bảng con là bình thường trở lại

      async function edit(record) {
        let temp: any = await getFormData(record.id);
        //Gán giá trị biểu mẫu
        formData.value = { ...temp };
        editSubVxeTableData(temp);
      }

      function editSubVxeTableData(record) {
        if (!record) {
          // Trang thêm mới cần xóa sạch dữ liệu bảng con
          record = {};
        }
        let keys = Object.keys(subDataSource.value);
        if (keys && keys.length > 0) {
          let obj = {};
          for (let key of keys) {
            obj[key] = record[key] || [];
          }
          subDataSource.value = obj;
        }
      }

      function getSubTableAuthPre(table) {
        return 'online_' + table + ':';
      }

      //Chuyển tới trang báo cáo Jimu
      function onOpenReportPrint() {
        let url = onlineExtConfigJson.reportPrintUrl;
        let temp: any = formData.value;
        if (temp) {
          let id = temp.id;
          let token = getToken();
          goJmReportViewPage(url, id, token);
        }
      }

      function getSubTableForeignKeyValue(key) {
        let temp = formData.value;
        console.log('getValueIgnoreCase(temp, key)', temp, key, getValueIgnoreCase(temp, key));
        return getValueIgnoreCase(temp, key);
      }

      /**
       * VUEN-1056 30、một-nhiều được tạo ra, khi chỉnh sửa thì dữ liệu bảng con không gắn lên được
       */
      function getValueIgnoreCase(data, key) {
        if (data) {
          let temp = data[key];
          if (!temp && temp !== 0) {
            temp = data[key.toLowerCase()];
            if (!temp && temp !== 0) {
              temp = data[key.toUpperCase()];
            }
          }
          return temp;
        }
        return '';
      }

      return {
        detailFormSchemas,
        formData,
        formSpan,

        //bảng chính
        tableName,
        loading,

        //bảng con
        hasSubTable,
        subTabInfo,
        subFormHeight,
        subTableHeight,
        refMap,
        onTabChange,
        subReloadKey,
        //bảng con một-nhiều
        subDataSource,
        getSubTableAuthPre,

        //Lời gọi của component cha
        show,
        createRootProperties,

        // Cấu hình mở rộng
        onOpenReportPrint,
        onlineExtConfigJson,
        getSubTableForeignKeyValue,
        showStatus,
        ERP,
        TAB,
        subActiveKey,
        rowNumber,
      };
    },
  };
</script>

<style lang="less" scoped>
  .tabTheme {
    :deep(.ant-tabs-nav) {
      display: none;
    }
  }
</style>
