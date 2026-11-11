import { FormSchema } from '/@/components/Form';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { bindMapFormSchema } from '/@/utils/common/compUtils';
import { computed, ref } from 'vue';
import { usePermissionStore } from '/@/store/modules/permission';

// Biểu mẫu popup
export function useFormSchemas(_, handler) {
  const permissionStore = usePermissionStore();
  // Do cần thay đổi bố cục động nên sử dụng computed
  type SpanType = 'one' | 'tow' | 'threeTow' | 'three';
  // Bố cục động
  const mapFormSchema = bindMapFormSchema<SpanType>(
    {
      // một cột
      one: {
        colProps: { xs: 24, sm: 24 },
        itemProps: {
          labelCol: { xs: 24, sm: 2 },
          wrapperCol: { xs: 24, sm: 22 },
        },
      },
      // hai cột
      tow: {
        colProps: { xs: 24, sm: 12 },
        itemProps: {
          labelCol: { xs: 24, sm: 4 },
          wrapperCol: { xs: 24, sm: 20 },
        },
      },
      // cột hai phần ba
      threeTow: {
        colProps: { xs: 24, sm: 16 },
        itemProps: {
          labelCol: { xs: 24, sm: 3 },
          wrapperCol: { xs: 24, sm: 21 },
        },
      },
      // ba cột
      three: {
        colProps: { xs: 24, sm: 8 },
        itemProps: {
          labelCol: { xs: 24, sm: 6 },
          wrapperCol: { xs: 24, sm: 18 },
        },
      },
    },
    'three'
  );

  const dataType = ref('sql');
  const isCombination = ref('combination');
  // Căn cứ vào các giá trị khác nhau, hiển thị khác nhauform
  const formInfo = {
    cgrSql: {
      sql: { label: 'Truy vấnSQL', placeholder: 'Vui lòng nhập truy vấnSQL', language: 'sql' },
      json: { label: 'Dữ liệuJSON', placeholder: 'Vui lòng nhập dữ liệuJSON', language: 'javascript' },
      api: { label: 'APIAPI (giao diện)', placeholder: 'Vui lòng nhậpAPIAPI (giao diện)', language: 'javascript' },
    },
  };
  const cgrSqlFormInfo = computed<typeof formInfo.cgrSql.sql>(() => {
    return formInfo.cgrSql[dataType.value];
  });

  const formSchemas = computed<FormSchema[]>(() => [
    { label: 'ID', field: 'id', component: 'Input', show: false },
    mapFormSchema({
      label: 'Tên biểu đồ',
      field: 'name',
      component: 'Input',
      required: true,
    }),
    mapFormSchema({
      label: 'Mã (code)',
      field: 'code',
      component: 'Input',
      dynamicRules({ model }) {
        return [
          { required: true, message: 'Vui lòng nhập mã!' },
          {
            async validator({}, value) {
              if (/[\u4E00-\u9FA5]/g.test(value)) {
                return Promise.reject('Mã không được là chữ Hán');
              }
              let { success, message } = await duplicateCheckDelay({
                tableName: 'onl_graphreport_head',
                fieldName: 'code',
                fieldVal: value,
                dataId: model.id,
              });
              if (!success) {
                return Promise.reject(message);
              }
            },
          },
        ];
      },
    }),
    mapFormSchema({
      label: 'Mẫu hiển thị',
      field: 'displayTemplate',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'TabPhong cách', value: 'tab' },
          { label: 'Bố cục một hàng', value: 'single' },
          { label: 'Bố cục hai hàng', value: 'double' },
        ],
      },
      defaultValue: 'tab',
    }),
    mapFormSchema({
      label: 'XTrường trục',
      field: 'xaxisField',
      component: 'Input',
      required: true,
    }),
    mapFormSchema(
      {
        label: 'YTrường trục',
        field: 'yaxisField',
        component: 'JDictSelectTag',
        componentProps: {
          mode: 'tags',
          open: false,
          dictCode: 'online_graph_display_template',
        },
        required: true,
      },
      'threeTow'
    ),
    mapFormSchema({
      label: 'Loại dữ liệu',
      field: 'dataType',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'online_graph_data_type',
        showChooseOption: false,
        onChange: (value) => (dataType.value = value),
      },
      defaultValue: 'sql',
    }),
    mapFormSchema({
      label: 'Nguồn dữ liệu',
      field: 'dbSource',
      component: 'Select',
      componentProps: {
        options: handler.dbSourceOptions.value,
      },
      rules: [{ required: permissionStore.sysSafeMode, message: 'Vui lòng chọn nguồn dữ liệu!' }],
      ifShow: ({ model }) => model.dataType === 'sql',
    }),
    mapFormSchema(
      {
        label: 'Loại biểu đồ',
        field: 'graphType',
        component: 'JDictSelectTag',
        componentProps: {
          mode: isCombination.value === 'single' ? 'default' : 'multiple',
          dictCode: 'online_graph_type',
          showChooseOption: false,
        },
        defaultValue: ['bar'],
      },
      dataType.value === 'sql' ? 'three' : 'threeTow'
    ),
    mapFormSchema(
      {
        label: 'Mô tả',
        field: 'content',
        component: 'Input',
      },
      'one'
    ),
    mapFormSchema(
      {
        label: cgrSqlFormInfo.value?.label,
        field: 'cgrSql',
        required: true,
        component: 'JCodeEditor',
        componentProps: {
          placeholder: cgrSqlFormInfo.value?.placeholder,
          language: cgrSqlFormInfo.value?.language,
          fullScreen: true,
          autoHeight: '!ie',
          height: '100px',
        },
        dynamicRules() {
          return [
            {
              required: true,
              // Căn cứ sự khác nhau của loại dữ liệu, hiển thị thông tin lỗi khác nhau
              message: cgrSqlFormInfo.value?.placeholder,
            },
            {
              // Kiểm chứng tùy chỉnh: kiểm chứngJSONĐịnh dạng chuỗi ký tự có đúng hay không
              async validator({}, value) {
                if (value && dataType.value === 'json') {
                  try {
                    JSON.parse(value);
                  } catch {
                    return Promise.reject('JSONĐịnh dạng không đúng!');
                  }
                }
              },
            },
          ];
        },
      },
      'one'
    ),
    mapFormSchema(
      {
        label: ' ', // SQLPhân tích (parse)
        field: 'cgrSql',
        component: 'Input',
        slot: 'SQLAnalyzeButton',
        itemProps: { colon: false },
        // ifShow: ({ model }) => model.dataType === 'sql',
      },
      'one'
    ),
    mapFormSchema(
      {
        label: 'JSTăng cường',
        field: 'extendJs',
        component: 'JCodeEditor',
        componentProps: {
          placeholder: 'JSTăng cường',
          language: 'javascript',
          fullScreen: true,
          autoHeight: '!ie',
          height: '100px',
        },
      },
      'one'
    ),
  ]);

  return { formSchemas, dataType, isCombination };
}
