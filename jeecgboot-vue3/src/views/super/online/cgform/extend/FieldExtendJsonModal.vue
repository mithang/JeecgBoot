<template>
  <BasicModal
    wrapClassName="field-extend-config-modal"
    v-bind="$attrs"
    title="Mục cấu hình mở rộng của trường"
    @register="registerModal"
    keyboard
    :canFullscreen="false"
    cancelText="Đóng"
    @ok="handleSubmit"
  >
    <a-spin :spinning="spinningLoading">
      <BasicForm @register="registerForm">
        <template #switchOptions="{ model, field }">
          <SetSwitchOptions v-model:value="model[field]" />
        </template>
      </BasicForm>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, h, reactive, toRaw, toRefs } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import SetSwitchOptions from './SetSwitchOptions.vue';
  import { ref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { pick } from 'lodash-es';
  import { LABELLENGTH } from '../util/constant';
  import { isArray } from '/@/utils/is';
  import { message, Tooltip } from 'ant-design-vue';
  export default defineComponent({
    name: 'FieldExtendJsonModal',
    components: {
      BasicModal,
      BasicForm,
      SetSwitchOptions,
    },
    emits: ['success', 'register'],
    setup(_p, { emit }) {
      const spinningLoading = ref(false);

      function initExtendConfig() {
        extendConfig.uploadnum = 0;
        extendConfig.showLength = '';
        extendConfig.popupMulti = true;
        extendConfig.multiple = false;
        extendConfig.multiSelect = true;
        extendConfig.store = '';
        extendConfig.text = '';
        extendConfig.orderRule = '';
        extendConfig.validateError = '';
        extendConfig.labelLength = LABELLENGTH;
        extendConfig.displayLevel = 'all';
        //Có phải trường tiêu đề hay không
        extendConfig.isTitleField = 0;
      }

      const extendConfig = reactive({
        uploadnum: 0,
        showLength: '',
        popupMulti: true,
        multiple: false,
        store: '',
        text: '',
        multiSelect: true,
        orderRule: '',
        validateError: '',
        labelLength: LABELLENGTH,
        displayLevel: 'all',
        isTitleField: 0,
      });
      const fieldShowType = ref('');
      const rowKey = ref('');
      const sortFlag = ref('0');
      const dbType = ref('');
      //Có phải bảng con hay không
      const isSubTable = ref(false);

      const formSchemas: any[] = [
        {
          label: 'rowKey',
          field: 'rowKey',
          component: 'Input',
          show: false,
        },
        {
          label: 'Số lượng tệp tải lên',
          field: 'uploadnum',
          component: 'InputNumber',
          componentProps: {
            style: {
              width: '100%',
            },
          },
          ifShow: () => {
            return fieldShowType.value === 'file' || fieldShowType.value === 'image';
          },
        },
        {
          label: 'Có chọn nhiều hay không',
          field: 'popupMulti',
          component: 'RadioGroup',
          defaultValue: true,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'popup' || fieldShowType.value === 'popup_dict';
          },
        },
        // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
        {
          label: 'Có chọn nhiều hay không',
          field: 'multiple',
          component: 'RadioGroup',
          defaultValue: false,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'sel_search';
          },
        },
        // update-end--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
        {
          label: 'Có chọn nhiều hay không',
          field: 'multiSelect',
          component: 'RadioGroup',
          defaultValue: true,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'sel_user' || fieldShowType.value === 'sel_depart';
          },
        },
        {
          label: 'Trường lưu trữ',
          field: 'store',
          component: 'Input',
          ifShow: () => {
            return fieldShowType.value === 'sel_user' || fieldShowType.value === 'sel_depart';
          },
        },
        {
          label: 'Trường hiển thị',
          field: 'text',
          component: 'Input',
          ifShow: () => {
            return fieldShowType.value === 'sel_user' || fieldShowType.value === 'sel_depart';
          },
        },
        {
          label: 'Sắp xếp mặc định',
          field: 'orderRule',
          component: 'RadioGroup',
          defaultValue: '',
          componentProps: {
            options: [
              { label: 'Giảm dần', value: 'desc' },
              { label: 'Tăng dần', value: 'asc' },
              { label: 'không sắp xếp theo mặc định', value: '' },
            ],
          },
          ifShow: () => {
            // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-308】Datetime、string、Date,int、BigDecimal、doubleLoại mặc định hiển thị chức năng sắp xếp
            return sortFlag.value === '1' || ['Datetime', 'string', 'Date', 'int', 'BigDecimal', 'double', 'long'].includes(dbType.value);
            // update-end--author:liaozhiyang---date:20240523---for：【TV360X-308】Datetime、string、Date,int、BigDecimal、doubleLoại mặc định hiển thị chức năng sắp xếp
          },
        },
        {
          label: 'Nhắc nhở kiểm chứng',
          field: 'validateError',
          component: 'Input',
          componentProps: {
            placeholder: 'Vui lòng nhập văn bản nhắc nhở kiểm chứng',
          },
        },
        {
          label: () => h(Tooltip, { title: 'Số lượng ký tự hiển thị' }, { default: () => 'Truy vấnlabelĐộ dài' }),
          field: 'labelLength',
          component: 'InputNumber',
          componentProps: {
            placeholder: 'Vui lòng nhậplabelĐộ dài',
          },
        },
        {
          label: 'Có cố định hay không',
          field: 'isFixed',
          component: 'RadioGroup',
          defaultValue: 0,
          componentProps: {
            options: [
              { label: 'là', value: 1 },
              { label: 'Không', value: 0 },
            ],
          },
        },
        // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        {
          label: 'Có chiếm riêng một hàng hay không',
          field: 'isOneRow',
          component: 'RadioGroup',
          defaultValue: false,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'markdown' || fieldShowType.value === 'umeditor';
          },
        },
        // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        {
          label: 'Định dạng ngày',
          field: 'picker',
          component: 'RadioGroup',
          defaultValue: 'default',
          componentProps: {
            options: [
              { label: 'Năm', value: 'year' },
              { label: 'Năm-tháng', value: 'month' },
              { label: 'Năm-tuần', value: 'week' },
              { label: 'Năm-quý', value: 'quarter' },
              { label: 'Năm-tháng-ngày', value: 'default' },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'date';
          },
        },
        // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
        {
          label: 'Cấp hiển thị',
          field: 'displayLevel',
          component: 'RadioGroup',
          defaultValue: 'all',
          componentProps: {
            options: [
              { label: 'Tỉnh/Thành/Quận huyện', value: 'all' },
              { label: 'Tỉnh', value: 'province' },
              { label: 'thành phố', value: 'city' },
              { label: 'khu', value: 'region' },
            ],
          },
          ifShow: () => {
            return fieldShowType.value === 'pca';
          },
        },
        // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
        //update-begin---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
        {
          label: 'Có phải trường tiêu đề hay không',
          field: 'isTitleField',
          component: 'RadioGroup',
          defaultValue: 0,
          componentProps: {
            options: [
              { label: 'là', value: 1 },
              { label: 'Không', value: 0 },
            ],
            onChange: (e) => {
              const newValue = e?.target?.value ?? e;
              if (newValue === 1) {
                message.info('Trường tiêu đề là duy nhất, sẽ ghi đè các mục khác đã thiết lập');
              }
            },
          },
          ifShow: () => {
            //Bảng con không cấu hình trường tiêu đề
            return !isSubTable.value;
          },
        },
        //update-end---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
        // update-begin--author:liaozhiyang---date:20240308---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
        {
          label: 'Giá trị công tắc',
          field: 'switchOptions',
          component: 'Input',
          slot: 'switchOptions',
          ifShow: () => {
            return fieldShowType.value === 'switch';
          },
        },
        // update-end--author:liaozhiyang---date:20240308---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
      ];

      // Cấu hình biểu mẫu
      const [registerForm, { validate, setFieldsValue, resetFields }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
        labelWidth: 100,
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        console.log('extend json', data);
        initExtendConfig();
        if (data.jsonStr) {
          let json = JSON.parse(data.jsonStr);
          // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
          if (data.fieldShowType === 'sel_search' && json.multiple == null) {
            extendConfig.multiple = false;
          }
          // update-end--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-519】Công tắc thiết lập giá trị mặc định và tương thích cách viết mảng
          if (data.fieldShowType === 'switch') {
            // Tương thích[0,1]Cách viết
            if (isArray(json) && json.length == 2) {
              extendConfig.switchOptions = json;
            }
          }
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-519】Công tắc thiết lập giá trị mặc định và tương thích cách viết mảng
          Object.keys(json).map((k) => {
            console.log('Tham số mở rộng:' + k + '=' + json[k]);
            extendConfig[k] = json[k];
          });
        } else {
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-519】Công tắc thiết lập giá trị mặc định và tương thích cách viết mảng
          // Công tắc thiết lập giá trị mặc định
          if (data.fieldShowType === 'switch') {
            extendConfig.switchOptions = ['Y', 'N'];
          }
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-519】Công tắc thiết lập giá trị mặc định và tương thích cách viết mảng
        }
        fieldShowType.value = data.fieldShowType;
        rowKey.value = data.id;
        sortFlag.value = data.sortFlag;
        dbType.value = data.dbType;
        isSubTable.value = !!data.isSubTable;
        let temp = toRaw(extendConfig);
        await resetFields();
        await setFieldsValue({
          ...temp,
          rowKey: data.id,
        });
      });

      async function handleSubmit() {
        let data = await validate();
        console.log('datga', data);
        let type = fieldShowType.value;
        let obj: any = {};
        if (type === 'file' || type === 'image') {
          if (data.uploadnum && data.uploadnum > 0) {
            obj.uploadnum = data.uploadnum;
          }
        } else if (type === 'textarea' || type === 'text') {
          if (data.showLength && data.showLength > 0) {
            obj.showLength = data.showLength;
          }
        } else if (type === 'sel_user' || type === 'sel_depart') {
          obj = pick(data, 'store', 'text', 'multiSelect');
        } else if (type === 'popup'||type === 'popup_dict') {
          obj.popupMulti = data.popupMulti;
        } else if (type === 'sel_search') {
          obj.multiple = data.multiple;
        }
        // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-308】Datetime、string、Date,int、BigDecimal、doubleLoại mặc định hiển thị chức năng sắp xếp
        if (data.orderRule) {
          obj.orderRule = data.orderRule;
        }
        // update-end--author:liaozhiyang---date:20240523---for：【TV360X-308】Datetime、string、Date,int、BigDecimal、doubleLoại mặc định hiển thị chức năng sắp xếp
        if (data.validateError) {
          obj.validateError = data.validateError;
        }
        if (data.labelLength) {
          obj.labelLength = data.labelLength;
        }
        // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Cột hỗ trợ tính năng cố định
        if (data.isFixed) {
          obj.isFixed = data.isFixed;
        }
        // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Cột hỗ trợ tính năng cố định
        //update-begin---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
        if (!isSubTable.value && data.isTitleField) {
          obj.isTitleField = data.isTitleField;
        }
        //update-end---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
        // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        // update-begin--author:liaozhiyang---date:20260827---for：【LHZP-371】Văn bản giàu vàMarkdownSau khi chọn Không cho chiếm riêng một hàng, lưu đúng
        if ((type === 'markdown' || type === 'umeditor') && typeof data.isOneRow === 'boolean') {
          obj.isOneRow = data.isOneRow;
        }
        // update-end--author:liaozhiyang---date:20260827---for：【LHZP-371】Văn bản giàu vàMarkdownSau khi chọn Không cho chiếm riêng một hàng, lưu đúng
        // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        if (data.picker) {
          obj.picker = data.picker;
        }
        // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
        if (data.switchOptions) {
          const arr = data.switchOptions.split(',');
          let Y = Number(arr[0]);
          let N = Number(arr[1]);
          // Đều có thể chuyển thànhnumberthì chuyển đổi, nếu không thì làstring
          if (Number.isNaN(Y) || Number.isNaN(N)) {
            Y = arr[0];
            N = arr[1];
          }
          obj.switchOptions = [Y, N];
        }
        // update-end--author:liaozhiyang---date:20240522---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
        if (data.displayLevel) {
          obj.displayLevel = data.displayLevel;
        }
        console.log('obj....', obj);
        for (let key in obj) {
          if (obj[key] === '') {
            delete obj[key];
          }
        }
        console.log(obj);
        emit('success', obj, data.rowKey);
        closeModal();
      }

      return {
        spinningLoading,
        registerModal,
        registerForm,
        fieldShowType,
        rowKey,
        isSubTable,
        handleSubmit,
        ...toRefs(extendConfig),
      };
    },
  });
</script>

<style scoped></style>
