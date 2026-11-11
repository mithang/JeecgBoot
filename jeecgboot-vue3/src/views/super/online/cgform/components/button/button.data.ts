import { BasicColumn, FormSchema } from '/@/components/Table';
// @ts-ignore
import {getButtonIconRender} from "./button.data.tsx";

export const columns: BasicColumn[] = [
  { title: 'Mã nút', align: 'center', dataIndex: 'buttonCode' },
  { title: 'Tên nút', align: 'center', dataIndex: 'buttonName' },
  {
    title: 'Kiểu dáng nút',
    align: 'center',
    dataIndex: 'buttonStyle',
    customRender({ text, record }) {
      if (text === 'form') {
        let p = record.optPosition;
        return text + '(' + (p == '2' ? 'Phần dưới' : 'Mặt bên') + ')';
      } else {
        return text;
      }
    },
  },
  { title: 'Loại nút', align: 'center', dataIndex: 'optType' },
  { title: 'Sắp xếp', align: 'center', dataIndex: 'orderNum' },
  {
    title: 'Biểu tượng nút',
    align: 'center',
    dataIndex: 'buttonIcon',
    customRender: ({text, record}) => {
      return getButtonIconRender({text, buttonCode: record.buttonCode});
    },
  },
  { title: 'Biểu thức', align: 'center', dataIndex: 'exp' },
  {
    title: 'Trạng thái nút',
    align: 'center',
    dataIndex: 'buttonStatus',
    customRender({ text }) {
      if (text == 1) {
        return 'Kích hoạt';
      } else {
        return 'Chưa kích hoạt';
      }
    },
  },
];

export const formSchemas = ({ redoModalHeight }): FormSchema[] => {
  return [
    {
      label: 'Mã nút',
      field: 'buttonCode',
      component: 'Input',
      required: true,
      // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-139】Mã nút thêm kiểm chứng bằng biểu thức chính quy
      dynamicRules: () => {
        return [
          {
            validator: (_, value) => {
              //Cầnreturn mộtPromiseđối tượng
              return new Promise((resolve, reject) => {
                const reg = /^[a-zA-Z_$][a-zA-Z0-9_$]*$/;
                if (reg.test(value)) {
                  resolve();
                } else {
                  reject('Mã chỉ có thể chứa chữ cái, số, dấu gạch dưới (_) và ký hiệu đô la Mỹ ($)và không được bắt đầu bằng chữ số');
                }
              });
            },
          },
          // update-begin--author:liaozhiyang---date:20240701---for：【TV360X-1693】Loại trừ mã nút tùy chỉnhsqlvàjavaMã tích hợp sẵn của hệ thống
          {
            validator: (_, value) => {
              //Cầnreturn mộtPromiseđối tượng
              return new Promise((resolve, reject) => {
                const exclude = ['add', 'edit', 'detail', 'delete', 'batch_delete', 'import', 'export', 'query', 'reset', 'bpm', 'super_query', 'form_confirm'];
                if (exclude.includes(value)) {
                  reject('Không được sử dụng mã nút tích hợp sẵn, vui lòng sửa nút tích hợp sẵn trong “Quản lý nút tích hợp sẵn”');
                } else {
                  resolve();
                }
              });
            },
          },
          // update-end--author:liaozhiyang---date:20240701---for：【TV360X-1693】Loại trừ mã nút tùy chỉnhsqlvàjavaMã tích hợp sẵn của hệ thống
        ];
      },
      // update-end--author:liaozhiyang---date:20240521---for：【TV360X-139】Mã nút thêm kiểm chứng bằng biểu thức chính quy
    },
    {
      label: 'Tên nút',
      field: 'buttonName',
      component: 'Input',
      required: true,
    },
    {
      label: 'Kiểu dáng nút',
      field: 'buttonStyle',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Link', value: 'link' },
          { label: 'Button', value: 'button' },
          { label: 'Form', value: 'form' },
        ],
        // update-begin--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
        onChange: () => {
          redoModalHeight();
        },
        // update-end--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
      },
      defaultValue: 'link',
    },
    {
      label: 'Vị trí nút',
      field: 'optPosition',
      component: 'Select',
      componentProps: {
        allowClear: false,
        options: [
          // { label: 'Mặt bên', value: '1' },
          { label: 'Phần dưới', value: '2' },
        ],
      },
      defaultValue: '2',
      show: ({ model }) => model.buttonStyle === 'form',
    },
    {
      label: 'Loại nút',
      field: 'optType',
      component: 'Select',
      componentProps: {
        allowClear: false,
        options: [
          { label: 'Js', value: 'js' },
          { label: 'Action', value: 'action' },
        ],
      },
      defaultValue: 'js',
    },
    {
      label: 'Sắp xếp',
      field: 'orderNum',
      component: 'InputNumber',
      componentProps: {
        style: 'width: 100%',
      },
    },
    {
      label: 'Biểu tượng nút',
      field: 'buttonIcon',
      // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-136】Biểu tượng nút đổi thành chọn bằng component biểu tượng
      component: 'IconPicker',
      componentProps: {
        clearSelect: true,
        iconPrefixSave: false,
      },
      // update-end--author:liaozhiyang---date:20240528---for：【TV360X-136】Biểu tượng nút đổi thành chọn bằng component biểu tượng
      ifShow: ({ values, model }) => {
        if (values.buttonStyle == 'button' || values.buttonStyle == 'form') {
          return true;
        } else {
          // model.buttonIcon = null;
          return false;
        }
      },
    },
    {
      label: 'Biểu thức',
      field: 'exp',
      component: 'Input',
      // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-89】Kiểu nút tùy chỉnh làlinkkhi đó, hiển thị cấu hình biểu thức
      ifShow: ({ values, model }) => {
        if (values.buttonStyle == 'link') {
          return true;
        } else {
          model.exp = '';
          return false;
        }
      },
      // update-end--author:liaozhiyang---date:20240603---for：【TV360X-89】Kiểu nút tùy chỉnh làlinkkhi đó, hiển thị cấu hình biểu thức
    },
    {
      label: 'Trạng thái nút',
      field: 'buttonStatus',
      component: 'RadioButtonGroup',
      componentProps: {
        options: [
          { label: 'Kích hoạt', value: '1' },
          { label: 'Chưa kích hoạt', value: '0' },
        ],
      },
      defaultValue: '1',
    },
  ]
};
