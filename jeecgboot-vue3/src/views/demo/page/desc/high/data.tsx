import { BasicColumn } from '/@/components/Table/src/types/table';

import { Badge } from 'ant-design-vue';

export const refundTimeTableSchema: BasicColumn[] = [
  {
    title: 'Thời gian',
    width: 150,
    dataIndex: 't1',
  },
  {
    title: 'Tiến độ hiện tại',
    width: 150,
    dataIndex: 't2',
  },
  {
    title: 'Trạng thái',
    width: 150,
    dataIndex: 't3',
    customRender: ({ record }) => {
      return <Badge status="success" text={record.t3} />;
    },
  },
  {
    title: 'Người vận hànhID	',
    width: 150,
    dataIndex: 't4',
  },
  {
    title: 'Thời gian tốn',
    width: 150,
    dataIndex: 't5',
  },
];

export const refundTimeTableData: any[] = [
  {
    t1: '2017-10-01 14:10',
    t2: 'Liên hệ khách hàng',
    t3: 'Đang tiến hành',
    t4: 'Nhân viên lấy hàng ID1234',
    t5: '5mins',
  },
  {
    t1: '2017-10-01 14:10',
    t2: 'Người lấy hàng đã xuất phát',
    t3: 'Thành công',
    t4: 'Nhân viên lấy hàng ID1234',
    t5: '5mins',
  },
  {
    t1: '2017-10-01 14:10',
    t2: 'Người lấy hàng đã nhận đơn',
    t3: 'Thành công',
    t4: 'Hệ thống',
    t5: '5mins',
  },
  {
    t1: '2017-10-01 14:10',
    t2: 'Đơn đã được phê duyệt',
    t3: 'Thành công',
    t4: 'Người dùng',
    t5: '1h',
  },
];
