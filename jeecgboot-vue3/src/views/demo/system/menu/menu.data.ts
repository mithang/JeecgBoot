import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { Icon } from '/@/components/Icon';

export const columns: BasicColumn[] = [
  {
    title: 'Tên menu',
    dataIndex: 'menuName',
    width: 200,
    align: 'left',
  },
  {
    title: 'Biểu tượng',
    dataIndex: 'icon',
    width: 50,
    customRender: ({ record }) => {
      return h(Icon, { icon: record.icon });
    },
  },
  {
    title: 'Định danh quyền hạn',
    dataIndex: 'permission',
    width: 180,
  },
  {
    title: 'Component',
    dataIndex: 'component',
  },
  {
    title: 'Sắp xếp',
    dataIndex: 'orderNo',
    width: 50,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      const enable = ~~status === 0;
      const color = enable ? 'green' : 'red';
      const text = enable ? 'Kích hoạt' : 'Vô hiệu hóa';
      return h(Tag, { color: color }, () => text);
    },
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 180,
  },
];

const isDir = (type: string) => type === '0';
const isMenu = (type: string) => type === '1';
const isButton = (type: string) => type === '2';

export const searchFormSchema: FormSchema[] = [
  {
    field: 'menuName',
    label: 'Tên menu',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Kích hoạt', value: '0' },
        { label: 'Vô hiệu hóa', value: '1' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'type',
    label: 'Loại menu',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Danh mục', value: '0' },
        { label: 'Menu', value: '1' },
        { label: 'Nút', value: '2' },
      ],
    },
    colProps: { lg: 24, md: 24 },
  },
  {
    field: 'menuName',
    label: 'Tên menu',
    component: 'Input',
    required: true,
  },

  {
    field: 'parentMenu',
    label: 'Menu cấp trên',
    component: 'TreeSelect',
    componentProps: {
      replaceFields: {
        title: 'menuName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
  },

  {
    field: 'orderNo',
    label: 'Sắp xếp',
    component: 'InputNumber',
    required: true,
  },
  {
    field: 'icon',
    label: 'Biểu tượng',
    component: 'IconPicker',
    required: true,
    ifShow: ({ values }) => !isButton(values.type),
  },

  {
    field: 'routePath',
    label: 'Địa chỉ route',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => !isButton(values.type),
  },
  {
    field: 'component',
    label: 'Đường dẫn component',
    component: 'Input',
    ifShow: ({ values }) => isMenu(values.type),
  },
  {
    field: 'permission',
    label: 'Định danh quyền hạn',
    component: 'Input',
    ifShow: ({ values }) => !isDir(values.type),
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Kích hoạt', value: '0' },
        { label: 'Vô hiệu hóa', value: '1' },
      ],
    },
  },
  {
    field: 'isExt',
    label: 'Có liên kết ngoài hay không',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Không', value: '0' },
        { label: 'Có', value: '1' },
      ],
    },
    ifShow: ({ values }) => !isButton(values.type),
  },

  {
    field: 'keepalive',
    label: 'Có cache hay không',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Không', value: '0' },
        { label: 'Có', value: '1' },
      ],
    },
    ifShow: ({ values }) => isMenu(values.type),
  },

  {
    field: 'show',
    label: 'Có hiển thị không',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Có', value: '0' },
        { label: 'Không', value: '1' },
      ],
    },
    ifShow: ({ values }) => !isButton(values.type),
  },
];
