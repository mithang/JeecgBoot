import { DescItem } from '/@/components/Description/index';
import { BasicColumn } from '/@/components/Table/src/types/table';
import { Button } from '/@/components/Button';

import { Badge } from 'ant-design-vue';

export const refundData = {
  a1: '1000000000',
  a2: 'Đã lấy hàng',
  a3: '1234123421',
  a4: '3214321432',
};

export const personData = {
  b1: 'Phó Tiểu Tiểu',
  b2: '18100000000',
  b3: 'Kho Cainiao',
  b4: 'Số 18 đường Vạn Đường, quận Tây Hồ, thành phố Hàng Châu, tỉnh Chiết Giang',
  b5: 'Không có',
};
export const refundSchema: DescItem[] = [
  {
    field: 'a1',
    label: 'Số đơn lấy hàng',
  },
  {
    field: 'a2',
    label: 'Trạng thái',
  },
  {
    field: 'a3',
    label: 'Số đơn bán hàng',
  },
  {
    field: 'a4',
    label: 'Đơn hàng con',
  },
];
export const personSchema: DescItem[] = [
  {
    field: 'b1',
    label: 'Họ tên người dùng',
  },
  {
    field: 'b2',
    label: 'Số điện thoại liên hệ',
  },
  {
    field: 'b3',
    label: 'Chuyển phát nhanh thường dùng',
  },
  {
    field: 'b4',
    label: 'Địa chỉ lấy hàng',
  },
  {
    field: 'b5',
    label: 'Ghi chú',
  },
];

export const refundTableSchema: BasicColumn[] = [
  {
    title: 'Mã sản phẩm',
    width: 150,
    dataIndex: 't1',
    customRender: ({ record }) => {
      return (
        <Button type="link" size="small">
          {() => record.t1}
        </Button>
      );
    },
  },
  {
    title: 'Tên sản phẩm',
    width: 150,
    dataIndex: 't2',
  },
  {
    title: 'Mã vạch sản phẩm',
    width: 150,
    dataIndex: 't3',
  },
  {
    title: 'Đơn giá	',
    width: 150,
    dataIndex: 't4',
  },
  {
    title: 'Số lượng (chiếc)	',
    width: 150,
    dataIndex: 't5',
  },
  {
    title: 'Số tiền',
    width: 150,
    dataIndex: 't6',
  },
];
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

export const refundTableData: any[] = [
  {
    t1: 1234561,
    t2: 'Nước khoáng 550ml',
    t3: '12421432143214321',
    t4: '2.00',
    t5: 1,
    t6: 2.0,
  },
  {
    t1: 1234562,
    t2: 'Nước khoáng 550ml',
    t3: '12421432143214321',
    t4: '2.00',
    t5: 2,
    t6: 2.0,
  },
  {
    t1: 1234562,
    t2: 'Nước khoáng 550ml',
    t3: '12421432143214321',
    t4: '2.00',
    t5: 2,
    t6: 2.0,
  },
  {
    t1: 1234562,
    t2: 'Nước khoáng 550ml',
    t3: '12421432143214321',
    t4: '2.00',
    t5: 2,
    t6: 2.0,
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
