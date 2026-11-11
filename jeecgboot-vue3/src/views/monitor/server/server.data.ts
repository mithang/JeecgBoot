import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tham số',
    dataIndex: 'param',
    width: 80,
    align: 'left',
    slots: { customRender: 'param' },
  },
  {
    title: 'Mô tả',
    dataIndex: 'text',
    slots: { customRender: 'text' },
    width: 80,
  },
  {
    title: 'Giá trị hiện tại',
    dataIndex: 'value',
    slots: { customRender: 'value' },
    width: 80,
  },
];
