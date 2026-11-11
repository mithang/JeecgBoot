<!--popupComponent-->
<template>
  <div class="JPopupDict components-input-demo-presuffix">
    <!--Hộp nhập-->
    <a-input v-if="useInput" v-model:value="showLabel" v-bind="attrs" :disabled="disabled" @click="handleOpen" readonly :loading="loading" />
    <a-select v-else v-model:value="showText" v-bind="attrs" :disabled="disabled" :mode="multi ? 'multiple' : ''" @click="handleOpen" readOnly :loading="loading" :open="false">
      <a-select-option v-for="item in options" :value="item.value">{{ item.text }}</a-select-option>
    </a-select>
    <a-form-item>
      <!--popupPopup-->
      <JPopupOnlReportModal
        @register="regModal"
        :code="code"
        :multi="multi"
        :echoRows="echoRows"
        :sorter="sorter"
        :groupId="''"
        :param="param"
        :getFormValues="getFormValues"
        :getContainer="getContainer"
        :showAdvancedButton="showAdvancedButton"
        @ok="callBack"
      />
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import JPopupOnlReportModal from './modal/JPopupOnlReportModal.vue';
  import { defineComponent, ref, nextTick, watch, reactive, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  //Định nghĩa thông tin URL yêu cầu
  const configUrl = reactive({
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
  });

  export default defineComponent({
    name: 'JPopupDict',
    components: {
      JPopupOnlReportModal,
    },
    inheritAttrs: false,
    props: {
      /**
       * Ví dụ:demo,name,id
       * demo: onlineMã báo cáo
       * name: onlineTrường của báo cáo, nội dung hiển thị cho người dùng làlabel
       * id: onlineTrường của báo cáo, dùng để lưu trữkey
       */
      dictCode: propTypes.string.def(''),
      value: propTypes.string.def(''),
      sorter: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
      multi: propTypes.bool.def(false),
      param: propTypes.object.def({}),
      spliter: propTypes.string.def(','),
      getFormValues: propTypes.func,
      getContainer: propTypes.func,
      showAdvancedButton: propTypes.bool.def(true),
      useInput: propTypes.bool.def(false),
    },
    emits: ['update:value', 'register', 'change', 'options-change'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const attrs = useAttrs();
      const showText = ref<any>(props.multi ? [] : '');
      const showLabel = ref<string>('');
      const options = ref<any>([]);
      const cgRpConfigId = ref('');
      const loading = ref(false);
      let syncingFromProps = false;
      const code = props.dictCode.split(',')[0];
      const labelFiled = props.dictCode.split(',')[1];
      const valueFiled = props.dictCode.split(',')[2];
      if (!code || !valueFiled || !labelFiled) {
        createMessage.error('popupDictTham số popup chưa được cấu hình đúng!!');
      }
      //Đăng kýmodel
      const [regModal, { openModal }] = useModal();

      /**
       * Mở hộp thoại pop
       */
      function handleOpen() {
        // Giải thích logic mã: 【TV360X-317]Sau khi vô hiệu hóa, JPopup và JPopupdic vẫn có thể nhấn để mở popup
        //update-begin---wangshuai---date:20260104  for：[issue/1560]Phía sau cột động không thể nối cột khác, ví dụ cột động cuối cùng thêm tổng hợp sẽ có vấn đề------------
        //update-end---author:wangshuai ---date:20260104  for：[issue/1560]Phía sau cột động không thể nối cột khác, ví dụ cột động cuối cùng thêm tổng hợp sẽ có vấn đề------------
        if (props.disabled) {
          return;
        }
        echoRows.value = buildEchoRows();
        openModal(true);
      }

      // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
      const echoRows = ref<any[]>([]);
      /**
       * Chuyển giá trị hiện tại thành mảng chuỗi (trường lưu trữ có thể là kiểu số, thống nhất so sánh theo chuỗi)
       */
      function getValues(): string[] {
        const value = props.value;
        if (value === undefined || value === null || value === '') {
          return [];
        }
        const values = props.multi ? String(value).split(props.spliter) : [String(value)];
        return values.filter((v) => v !== '');
      }
      /**
       * Tạo dòng chờ khớp hiển thị lại trong popup theo giá trị hiện tại [{Trường lưu trữ: Giá trị}], khi có nhiều kết quả khớp trong popup thì chọn dòng đầu tiên
       */
      function buildEchoRows() {
        return getValues().map((v) => ({ [valueFiled]: v }));
      }
      /**
       * Tra ngược văn bản hiển thị theo giá trị đã lưu (khi trường lưu trữ không duy nhất thì lấy bản ghi đầu tiên, đồng nhất với bản dịch phía backend của danh sách)
       */
      function translate(callBack) {
        cgRpConfigId.value ? loadData({ callBack }) : loadColumnsInfo({ callBack });
      }
      // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
      /**
       * liaozhiyang
       * 20260522
       * 【QQYUN-15493] thêm mớia-inputtrong chế độ
       * */
      function syncShowLabel() {
        if (!props.useInput) return;
        const currentValue = showText.value;
        if (props.multi) {
          const values = Array.isArray(currentValue) ? currentValue : currentValue ? currentValue.split(props.spliter) : [];
          showLabel.value = values
            .map((v) => {
              const opt = options.value.find((o: any) => String(o.value) === String(v));
              return opt?.text || v;
            })
            .join(props.spliter);
        } else {
          const opt = options.value.find((o: any) => String(o.value) === String(currentValue));
          showLabel.value = opt?.text || currentValue || '';
        }
      }
      /**
       * Lắng nghe giá trị value
       */
      /**
       * Theo props.value Đồng bộ giá trị hiển thị (đồng bộ một chiều, không kích hoạt update:value）
       */
      function syncFromProps() {
        syncingFromProps = true;
        // update-begin--author:liusq---date:20260720---for：【LHZP-1003]Từ điển pop của bảng đơn, sau khi cài đặt, mở chỉnh sửa thì dữ liệu hiển thị lại rơi vào vòng lặp truy vấn vô hạn
        // Giải thích logic mã: Phải sử dụng props.value thời gian thựcprops.valuechứ không phải val mà closure bắt được, nếu không khi callback bất đồng bộ
        // defaultValueKhi khác với giá trị bản ghi, nhiều callBack sẽ ghi đè luân phiên lên formModel, gây làm mới vô hạn
        // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3]Khi trường lưu trữ là số, giá trị hiển thị và kiểu value của options không nhất quán nên không dịch
        const values = getValues();
        showText.value = props.multi ? values : values[0] ?? '';
        // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3]Khi trường lưu trữ là số, giá trị hiển thị và kiểu value của options không nhất quán nên không dịch
        // update-end--author:liusq---date:20260720---for：【LHZP-1003]Từ điển pop của bảng đơn, sau khi cài đặt, mở chỉnh sửa thì dữ liệu hiển thị lại rơi vào vòng lặp truy vấn vô hạn
        syncShowLabel();
        nextTick(() => {
          syncingFromProps = false;
        });
      }
      watch(
        () => props.value,
        () => {
          // Hiển thị trước giá trị đã lưu, sau khi API trả về thì dùng options đã tải được để thay thế thành văn bản hiển thị, tránh popup chỉnh sửa bị trống trong thời gian ngắn
          syncFromProps();
          if (props.value || props.defaultValue) {
            translate(syncFromProps);
          }
        },
        { immediate: true }
      );
      watch(
        () => showText.value,
        (val) => {
          // props Hiển thị lại thuộc về đồng bộ một chiều, không thể dùng làm thao tác của người dùng để xóa ngược hoặc ghi đè giá trị biểu mẫu
          if (syncingFromProps) return;
          let result;
          if (props.multi) {
            result = val.join(',');
          } else {
            result = val;
          }
          nextTick(() => {
            emit('change', result);
            emit('update:value', result);
          });
        }
      );
      /**
       * Tải thông tin cột
       */
      function loadColumnsInfo({ callBack }) {
        loading.value = true;
        let url = `${configUrl.getColumns}${code}`;
        defHttp
          .get({ url }, { isTransformResponse: false, successMessageMode: 'none' })
          .then((res) => {
            if (res.success) {
              cgRpConfigId.value = res.result.cgRpConfigId;
              loadData({ callBack });
            }
          })
          .catch((err) => {
            loading.value = false;
            callBack?.();
          });
      }
      function loadData({ callBack }) {
        loading.value = true;
        let url = `${configUrl.getData}${unref(cgRpConfigId)}`;
        defHttp
          .get(
            { url, params: { ['force_' + valueFiled]: props.value || props.defaultValue } },
            { isTransformResponse: false, successMessageMode: 'none' }
          )
          .then((res) => {
            let data = res.result;
            if (data.records?.length) {
              // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
              // Khi trường lưu trữ không duy nhất, tra ngược sẽ trả về nhiều bản ghi, cùng một giá trị chỉ lấy văn bản hiển thị của bản ghi đầu tiên; value thống nhất chuyển thành chuỗi, nhất quán với kiểu của giá trị hiển thị
              const map = new Map();
              data.records.forEach((item) => {
                const key = String(item[valueFiled]);
                !map.has(key) && map.set(key, { value: key, text: item[labelFiled] });
              });
              options.value = [...map.values()];
              // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
            }
          })
          .finally(() => {
            loading.value = false;
            callBack?.();
          });
      }
      /**
       * Callback truyền giá trị
       */
      function callBack(rows) {
        const dataOptions: any = [];
        const dataValue: any = [];
        let result;
        rows.forEach((item) => {
          dataOptions.push({ value: item[valueFiled], text: item[labelFiled] });
          dataValue.push(item[valueFiled]);
        });
        options.value = dataOptions;
        if (props.multi) {
          showText.value = dataValue;
          result = dataValue.join(props.spliter);
          //update-begin author:liaozhiyang date:20260522 for：【QQYUN-15493] thêm mớia-inputtrong chế độ
          if (props.useInput) {
            showLabel.value = dataOptions.map((o: any) => o.text).join(props.spliter);
          }
          //update-end author:liaozhiyang date:20260522 for：【QQYUN-15493] thêm mớia-inputtrong chế độ
        } else {
          showText.value = dataValue[0];
          result = dataValue[0];
          //update-begin author:liaozhiyang date:20260522 for：【QQYUN-15493] thêm mớia-inputtrong chế độ
          if (props.useInput) {
            showLabel.value = dataOptions[0]?.text || '';
          }
          //update-end author:liaozhiyang date:20260522 for：【QQYUN-15493] thêm mớia-inputtrong chế độ
        }
        // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
        // Sau khi chọn cần dịch lại theo giá trị đã lưu
        const needTranslate = result != null && result !== '' && String(props.value ?? '') === String(result);
        // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa từ điển popup, popup hiển thị lại phần đã tick
        nextTick(() => {
          emit('change', result);
          emit('update:value', result);
          emit('options-change', options.value);
          needTranslate && translate(syncFromProps);
        });
      }

      return {
        showText,
        showLabel,
        attrs,
        regModal,
        handleOpen,
        callBack,
        code,
        options,
        loading,
        echoRows,
        valueFiled,
      };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【QQYUN-9260] Ở chế độ bắt buộc sẽ ảnh hưởng đến kiểu của component antd trong popup
  .JPopupDict {
    > .ant-form-item {
      display: none;
    }
  }
  .components-input-demo-presuffix {
    :deep(.ant-select-dropdown) {
      display: none !important;
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
