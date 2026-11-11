import { BasicColumn } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Mục cấu hình',
    dataIndex: 'key',
    width: 120,
    align: 'left',
    customRender: ({ text }) => {
      return text;
    },
  },
  {
    title: 'Giải thích',
    dataIndex: 'description',
    width: 200,
    align: 'left',
    ellipsis: true,
  },
  {
    title: 'Giá trị',
    dataIndex: 'value',
    width: 80,
    align: 'right',
  },
];
