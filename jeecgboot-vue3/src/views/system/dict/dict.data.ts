import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { dictItemCheck } from './dict.api';
import { rules } from '/@/utils/helper/validator';
import { h } from "vue";

export const columns: BasicColumn[] = [
  {
    title: 'Tên từ điển',
    dataIndex: 'dictName',
    width: 240,
  },
  {
    title: 'Mã từ điển',
    dataIndex: 'dictCode',
    width: 240,
  },
  {
    title: 'Mô tả',
    dataIndex: 'description',
    // width: 120
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: 'Tên từ điển',
    dataIndex: 'dictName',
    width: 120,
  },
  {
    title: 'Mã từ điển',
    dataIndex: 'dictCode',
    width: 120,
  },
  {
    title: 'Mô tả',
    dataIndex: 'description',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên từ điển',
    field: 'dictName',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    label: 'Mã từ điển',
    field: 'dictCode',
    component: 'JInput',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên từ điển',
    field: 'dictName',
    required: true,
    component: 'Input',
  },
  {
    label: 'Mã từ điển',
    field: 'dictCode',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_dict', 'dict_code', model, schema, true),
  },
  {
    label: 'Mô tả',
    field: 'description',
    component: 'Input',
  },
];

export const dictItemColumns: BasicColumn[] = [
  {
    title: 'Tên',
    dataIndex: 'itemText',
    width: 80,
  },
  {
    title: 'Giá trị dữ liệu',
    dataIndex: 'itemValue',
    width: 80,
  },
  {
    title: 'Màu từ điển',
    dataIndex: 'itemColor',
    width: 80,
    align:'center',
    customRender:({ text }) => {
      return h('div', {
        style: {"background": text, "width":"18px","height":"18px","border-radius":"50%","margin":"0 auto"}
      })
    }
  },
];

export const dictItemSearchFormSchema: FormSchema[] = [
  {
    label: 'Tên',
    field: 'itemText',
    component: 'JInput',
  },
  {
    label: 'Trạng thái',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];

export const itemFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên',
    field: 'itemText',
    required: true,
    component: 'Input',
  },
  {
    label: 'Giá trị dữ liệu',
    field: 'itemValue',
    component: 'Input',
    dynamicRules: ({ values, model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('Vui lòng nhập giá trị dữ liệu');
            }
            if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！￥（）—【】‘；：”“。，、？]").test(value)) {
              return Promise.reject('Giá trị dữ liệu không được chứa ký tự đặc biệt!');
            }
            return new Promise<void>((resolve, reject) => {
              let params = {
                dictId: values.dictId,
                id: model.id,
                itemValue: value,
              };
              dictItemCheck(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || 'Kiểm tra không hợp lệ');
                })
                .catch((err) => {
                  reject(err.message || 'Xác thực thất bại');
                });
            });
          },
        },
      ];
    },
  },
  {
    label: 'Giá trị màu',
    field: 'itemColor',
    component: 'Input',
    slot:'itemColor'
  },
  {
    label: 'Mô tả',
    field: 'description',
    component: 'Input',
  },
  {
    field: 'sortOrder',
    label: 'Sắp xếp',
    component: 'InputNumber',
    defaultValue: 1,
  },
  {
    field: 'status',
    label: 'Có kích hoạt không',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      type: 'radioButton',
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];
