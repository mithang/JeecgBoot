<!--popupComponent-->
<template>
  <div class="JPopup components-input-demo-presuffix" v-if="avalid">
    <!--Hộp nhập-->
    <a-input @click="handleOpen" @update:value="handleValueChange" :value="isQueryingShowText ? 'Đang tải...' : (innerShowText || showText)" :placeholder="placeholder" readOnly v-bind="attrs">
      <template #prefix>
        <Icon v-if="!isQueryingShowText" icon="ant-design:cluster-outlined"></Icon>
        <Icon v-else icon="ant-design:loading-outlined" spin></Icon>
      </template>
      <!-- <template #suffix>
                <Icon icon="ant-design:close-circle-outlined" @click="handleEmpty" title="Xóa sạch" v-if="showText"></Icon>
            </template>-->
    </a-input>
    <a-form-item>
      <!--popupPopup-->
      <JPopupOnlReportModal
        @register="regModal"
        :code="code"
        :multi="multi"
        :sorter="sorter"
        :groupId="uniqGroupId"
        :param="param"
        :showAdvancedButton="showAdvancedButton"
        :getContainer="getContainer"
        :getFormValues="getFormValues"
        :echoRows="echoRows"
        @ok="callBack"
      ></JPopupOnlReportModal>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import JPopupOnlReportModal from './modal/JPopupOnlReportModal.vue';
  import { defineComponent, ref, reactive, onMounted, watchEffect, watch, computed, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';

  // Định nghĩa thông tin url của yêu cầu
  const configUrl = reactive({
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
  });

  export default defineComponent({
    name: 'JPopup',
    components: {
      JPopupOnlReportModal,
    },
    inheritAttrs: false,
    props: {
      code: propTypes.string.def(''),
      value: propTypes.oneOfType([propTypes.string, propTypes.number]).def(''),
      sorter: propTypes.string.def(''),
      width: propTypes.number.def(1200),
      placeholder: propTypes.string.def('Vui lòng chọn'),
      multi: propTypes.bool.def(false),
      param: propTypes.object.def({}),
      spliter: propTypes.string.def(','),
      groupId: propTypes.string.def(''),
      formElRef: propTypes.object,
      setFieldsValue: propTypes.func,
      getFormValues: propTypes.func,
      getContainer: propTypes.func,
      fieldConfig: {
        type: Array,
        default: () => [],
      },
      showAdvancedButton: propTypes.bool.def(true),
      // Có đang dùng trong bộ lọc (search) hay không
      inSearch: propTypes.bool.def(false),
    },
    emits: ['update:value', 'register', 'popUpChange', 'focus'],
    setup(props, { emit, refs }) {
      const { createMessage } = useMessage();
      const attrs = useAttrs();
      //popCó hiển thị hay không
      const avalid = ref(true);
      const showText = ref('');
      const innerShowText = ref('')
      const loading = ref(false);
      const isQueryingShowText = ref(false);
      const cgRpConfigId = ref('');
      // Hiển thị lại khi chỉnh sửa: Các dòng đã chọn chờ khớp trong popup
      const echoRows = ref<any[]>([]);
      //Đăng kýmodel
      const [regModal, { openModal }] = useModal();
      //Giá trị biểu mẫu
      let {code, fieldConfig } = props;
      //Nhóm duy nhấtgroupId
      const uniqGroupId = computed(() => (props.groupId ? `${props.groupId}_${code}_${fieldConfig[0]['source']}_${fieldConfig[0]['target']}` : ''));
      /**
       * Kiểm tra mục cấu hình popup có đúng hay không
       */
      onMounted(() => {
        if (props.fieldConfig.length == 0) {
          createMessage.error('popupTham số popup chưa được cấu hình đúng!!');
          avalid.value = false;
        }
      });
      // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-2558] Giá trị popup lưu trữ nếu là số thì không hiển thị lại
      /**
       * value Chuyển thành văn bản hiển thị (khi điền lại chọn đơn sẽ giữ kiểu số)
       */
      function getValueText(val) {
        if (val === undefined || val === null || val === '') {
          return '';
        }
        return String(val).split(props.spliter).join(',');
      }
      // update-end--author:liaozhiyang---date:20261029---for：【LHZP-2558] Giá trị popup lưu trữ nếu là số thì không hiển thị lại
      /**
       * Lắng nghe giá trị value
       */
      watch(
        () => props.value,
        (val) => {
          const text = getValueText(val);
          if (!text) {
            innerShowText.value = '';
          }
          // Trong thời gian tra ngược tạm thời không hiển thị id, đợi API trả về rồi mới quyết định hiển thị văn bản hay hiển thị lại value
          if (!isQueryingShowText.value) {
            showText.value = text;
          }
          // Trong tình huống lọc, sau khi component được xây dựng lại thì innerShowText sẽ bị mất, cần dựa vào value để tra ngược lại văn bản hiển thị của báo cáo online
          if (props.inSearch && text && !innerShowText.value) {
            queryShowTextByValue(String(val));
          }
        },
        { immediate: true }
      );

      /**
       * Tra ngược văn bản hiển thị theo value
       */
      function queryShowTextByValue(val) {
        if (!props.code || props.fieldConfig.length === 0) {
          return;
        }
        const sourceField = props.fieldConfig[0]['source'];
        const labelField = props.fieldConfig[0]['label'];
        if (!sourceField || !labelField) {
          return;
        }
        // Trong thời gian tra ngược hiển thị loading, đồng thời xóa showText trước để tránh hiện thoáng qua id
        isQueryingShowText.value = true;
        showText.value = '';
        const doQuery = () => {
          loadDataForShowText(val, sourceField, labelField);
        };
        if (cgRpConfigId.value) {
          doQuery();
        } else {
          loadColumnsInfo({ callBack: doQuery });
        }
      }

      /**
       * Tải thông tin cột để lấy cgRpConfigId cgRpConfigId
       */
      function loadColumnsInfo({ callBack }) {
        loading.value = true;
        let url = `${configUrl.getColumns}${props.code}`;
        defHttp
          .get({ url }, { isTransformResponse: false, successMessageMode: 'none' })
          .then((res) => {
            if (res.success) {
              cgRpConfigId.value = res.result.cgRpConfigId;
              callBack?.();
            }
          })
          .catch((err) => {
            console.error('[JPopup] Tải thông tin cột báo cáo thất bại', err);
          })
          .finally(() => {
            loading.value = false;
            // Khi tải thông tin cột thất bại sẽ khôi phục hiển thị value, và đóng loading
            if (!cgRpConfigId.value) {
              isQueryingShowText.value = false;
              showText.value = getValueText(props.value);
            }
          });
      }

      /**
       * Tải văn bản hiển thị theo value
       */
      function loadDataForShowText(val, sourceField, labelField) {
        loading.value = true;
        let url = `${configUrl.getData}${unref(cgRpConfigId)}`;
        defHttp
          .get(
            { url, params: { ['force_' + sourceField]: val } },
            { isTransformResponse: false, successMessageMode: 'none' }
          )
          .then((res) => {
            let data = res.result;
            if (data.records?.length) {
              const labels = data.records.map((item) => item[labelField]);
              innerShowText.value = labels.join(',');
            }
          })
          .catch((err) => {
            console.error('[JPopup] Tra ngược văn bản hiển thị theo value thất bại', err);
          })
          .finally(() => {
            loading.value = false;
            // Kết thúc tra ngược thì tắt loading; nếu không tra được văn bản thì quay lại hiển thị value
            isQueryingShowText.value = false;
            if (!innerShowText.value) {
              showText.value = getValueText(props.value);
            }
          });
      }

      /**
       * Mở hộp thoại pop
       */
      function handleOpen() {
        emit('focus');
        // Giải thích logic mã: 【TV360X-317]Sau khi vô hiệu hóa, JPopup và JPopupdic vẫn có thể nhấn để mở popup
        if (attrs.value.disabled) {
          return;
        }
        // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa popup, popup hiển thị lại phần đã tick
        echoRows.value = buildEchoRows();
        // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa popup, popup hiển thị lại phần đã tick
        openModal(true);
      }

      /**
       * Căn cứ vào giá trị của các trường điền lại trong biểu mẫu, khôi phục các hàng đã chọn theo vị trí [{Trường nguồn: Giá trị}], dùng để hiển thị lại lựa chọn đã tick trong popup
       */
      function buildEchoRows() {
        let formValues: Recordable = {};
        try {
          formValues = props.getFormValues?.() ?? props.formElRef?.getFieldsValue?.() ?? {};
        } catch (e) {
          formValues = {};
        }
        // Khi không lấy được giá trị biểu mẫu (ví dụ như truy vấn nâng cao), ít nhất hãy dùng giá trị của chính nó
        const codeField = attrs.value.codeField;
        if (codeField && formValues[codeField] === undefined) {
          formValues = { ...formValues, [codeField]: props.value };
        }
        const columns: { source: string; values: string[] }[] = [];
        props.fieldConfig.forEach((item: any) => {
          const target = item.target.split(',')[0];
          const val = formValues[target];
          // Nếu trong biểu mẫu không có trường này (ví dụ như bị ẩn do không có quyền) thì không tham gia đối sánh
          if (val === undefined) {
            return;
          }
          const text = val === null ? '' : String(val);
          // Khi chọn đơn, toàn bộ là một giá trị; khi chọn nhiều, tách theo ký tự phân tách
          columns.push({ source: item.source, values: props.multi ? text.split(props.spliter) : [text] });
        });
        // Tất cả các trường đều trống nghĩa là không có dữ liệu đã chọn
        if (!columns.some((col) => col.values.some((v) => v !== ''))) {
          return [];
        }
        const count = Math.max(...columns.map((col) => col.values.length));
        const rows: Recordable[] = [];
        for (let i = 0; i < count; i++) {
          const row = {};
          columns.forEach((col) => {
            // Trường có giá trị rỗng không tham gia đối sánh (ví dụ trường điền lại bị xóa sạch, chỉ còn trường lưu trữ)
            if (col.values[i] !== undefined && col.values[i] !== '') {
              row[col.source] = col.values[i];
            }
          });
          // Hàng hoàn toàn trống không tham gia đối sánh
          Object.keys(row).length && rows.push(row);
        }
        return rows;
      }

      /**
       * TODO Xóa sạch
       */
      function handleEmpty() {
        showText.value = '';
      }

      /**
       * Sự kiện thay đổi giá trị ô nhập (dùng để xử lý allowClear trong chế độ chỉ đọc)
       */
      function handleValueChange(value) {
        emit('update:value', value);
        if (value) {
          return;
        }
        innerShowText.value = '';
        const values = {};
        props.fieldConfig.forEach((item) => {
          item.target.split(',').forEach((target) => {
            values[target] = '';
          });
        });
        props.formElRef && props.formElRef.setFieldsValue(values);
        props.setFieldsValue && props.setFieldsValue(values);
        emit('popUpChange', values);
      }

      /**
       * Callback truyền giá trị
       */
      function callBack(rows) {
        let { fieldConfig } = props;
        //Khớp với giá trị callback mà popup đã đặt
        let values = {};
        let labels = []
        for (let item of fieldConfig) {
          let val = rows.map((row) => row[item.source]);
          // Giải thích logic mã: 【QQYUN-7535]Mảng chỉ có một phần tử và là kiểu number, join sẽ thay đổi kiểu của giá trị thànhstring
          val = val.length == 1 ? val[0] : val.join(',');
          item.target.split(',').forEach((target) => {
            values[target] = val;
          });

          if (props.inSearch) {
            // Xử lý giá trị hiển thị
            if (item.label) {
              let txt = rows.map((row) => row[item.label]);
              txt = txt.length == 1 ? txt[0] : txt.join(',');
              labels.push(txt);
            } else {
              labels.push(val);
            }
          }

        }
        innerShowText.value = labels.join(',');
        //Gán giá trị theo cách truyền vào ví dụ biểu mẫu
        props.formElRef && props.formElRef.setFieldsValue(values);
        //Gán giá trị theo cách truyền vào phương thức gán giá trị
        props.setFieldsValue && props.setFieldsValue(values);
        // Giải thích logic mã: 【issues/5213] JPopup ném sự kiện change
        emit('popUpChange', values);
      }

      return {
        showText,
        innerShowText,
        avalid,
        uniqGroupId,
        loading,
        isQueryingShowText,
        attrs,
        regModal,
        echoRows,
        handleOpen,
        handleEmpty,
        handleValueChange,
        callBack,
      };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-9260] Ở chế độ bắt buộc sẽ ảnh hưởng đến kiểu của component antd trong popup
  .JPopup {
    > .ant-form-item {
      display: none;
    }
  }
  .components-input-demo-presuffix .anticon-close-circle {
    cursor: pointer;
    color: #ccc;
    transition: color 0.3s;
    font-size: 12px;
  }

  .components-input-demo-presuffix .anticon-close-circle:hover {
    color: #f5222d;
  }

  .components-input-demo-presuffix .anticon-close-circle:active {
    color: #666;
  }
</style>
