export interface GrowCardItem {
  icon: string;
  title: string;
  value?: number;
  total: number;
  color?: string;
  action?: string;
  footer?: string;
}

export const growCardList: GrowCardItem[] = [
  {
    title: 'Số lượt truy cập',
    icon: 'visit-count|svg',
    value: 2000,
    total: 120000,
    color: 'green',
    action: 'tháng',
  },
  {
    title: 'Doanh số giao dịch',
    icon: 'total-sales|svg',
    value: 20000,
    total: 500000,
    color: 'blue',
    action: 'tháng',
  },
  {
    title: 'Số lượt tải xuống',
    icon: 'download-count|svg',
    value: 8000,
    total: 120000,
    color: 'orange',
    action: 'Tuần',
  },
  {
    title: 'Số giao dịch thành công',
    icon: 'transaction|svg',
    value: 5000,
    total: 50000,
    color: 'purple',
    action: 'năm',
  },
];

export const chartCardList: GrowCardItem[] = [
  {
    title: 'Tổng doanh số',
    icon: 'visit-count|svg',
    total: 126560,
    value: 234.56,
    footer: 'Doanh số trung bình ngày',
  },
  {
    title: 'Số lượng đơn hàng',
    icon: 'total-sales|svg',
    value: 1234,
    total: 8846,
    color: 'blue',
    footer: 'Số đơn hàng trong ngày',
  },
  {
    title: 'Số lần thanh toán',
    icon: 'download-count|svg',
    value: 60,
    total: 6560,
    color: 'orange',
    footer: 'Tỷ lệ chuyển đổi',
  },
  {
    title: 'Hiệu quả hoạt động vận hành',
    icon: 'transaction|svg',
    total: 78,
  },
];
export const bdcCardList: GrowCardItem[] = [
  {
    title: 'Số lượng tiếp nhận',
    icon: 'ant-design:info-circle-outlined',
    total: 100,
    value: 60,
    footer: 'Số lượng tiếp nhận hôm nay',
  },
  {
    title: 'Số lượng hoàn thành',
    icon: 'ant-design:info-circle-outlined',
    value: 54,
    total: 87,
    color: 'blue',
    footer: 'Số lượng hoàn thành hôm nay',
  },
  {
    title: 'Số lượng tiếp nhận của người dùng',
    icon: 'ant-design:info-circle-outlined',
    value: 13,
    total: 15,
    color: 'orange',
    footer: 'Số lượng tiếp nhận hôm nay của người dùng',
  },
  {
    title: 'Số lượng hoàn thành của người dùng',
    icon: 'ant-design:info-circle-outlined',
    total: 9,
    value: 7,
    footer: 'Số lượng hoàn thành hôm nay của người dùng',
  },
];

export const table = {
  dataSource: [
    { reBizCode: '1', type: 'Đăng ký chuyển nhượng', acceptBy: 'Trương Tam', acceptDate: '2019-01-22', curNode: 'Phân công nhiệm vụ', flowRate: 60 },
    { reBizCode: '2', type: 'Đăng ký thế chấp', acceptBy: 'Lý Tứ', acceptDate: '2019-01-23', curNode: 'Lãnh đạo xét duyệt', flowRate: 30 },
    { reBizCode: '3', type: 'Đăng ký chuyển nhượng', acceptBy: 'Vương Vũ', acceptDate: '2019-01-25', curNode: 'Xử lý nhiệm vụ', flowRate: 20 },
    { reBizCode: '4', type: 'Đăng ký chuyển nhượng', acceptBy: 'Triệu Lâu', acceptDate: '2019-11-22', curNode: 'Phòng ban xét duyệt', flowRate: 80 },
    { reBizCode: '5', type: 'Đăng ký chuyển nhượng', acceptBy: 'Tiền Tựu', acceptDate: '2019-12-12', curNode: 'Phân công nhiệm vụ', flowRate: 90 },
    { reBizCode: '6', type: 'Đăng ký chuyển nhượng', acceptBy: 'Tôn Ba', acceptDate: '2019-03-06', curNode: 'Xử lý nhiệm vụ', flowRate: 10 },
    { reBizCode: '7', type: 'Đăng ký thế chấp', acceptBy: 'Chu Đại', acceptDate: '2019-04-13', curNode: 'Phân công nhiệm vụ', flowRate: 100 },
    { reBizCode: '8', type: 'Đăng ký thế chấp', acceptBy: 'Ngô Nhị', acceptDate: '2019-05-09', curNode: 'Báo cáo nhiệm vụ', flowRate: 50 },
    { reBizCode: '9', type: 'Đăng ký thế chấp', acceptBy: 'Trịnh Sảng', acceptDate: '2019-07-12', curNode: 'Xử lý nhiệm vụ', flowRate: 63 },
    { reBizCode: '20', type: 'Đăng ký thế chấp', acceptBy: 'Lâm Hữu', acceptDate: '2019-12-12', curNode: 'Trả lại nhiệm vụ', flowRate: 59 },
    { reBizCode: '11', type: 'Đăng ký chuyển nhượng', acceptBy: 'Gitee', acceptDate: '2019-09-10', curNode: 'Nhận nhiệm vụ', flowRate: 87 },
  ],
  columns: [
    {
      title: 'Mã nghiệp vụ',
      align: 'center',
      dataIndex: 'reBizCode',
    },
    {
      title: 'Loại nghiệp vụ',
      align: 'center',
      dataIndex: 'type',
    },
    {
      title: 'Người tiếp nhận',
      align: 'center',
      dataIndex: 'acceptBy',
    },
    {
      title: 'Thời gian tiếp nhận',
      align: 'center',
      dataIndex: 'acceptDate',
    },
    {
      title: 'Nút hiện tại',
      align: 'center',
      dataIndex: 'curNode',
    },
    {
      title: 'Thời gian xử lý',
      align: 'center',
      dataIndex: 'flowRate',
    },
  ],
  ipagination: {
    current: 1,
    pageSize: 5,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' Tổng cộng' + total + 'mục';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  },
};
export const table1 = {
  dataSource: [
    { reBizCode: 'A001', type: 'Đăng ký chuyển nhượng', acceptBy: 'Trương Tứ', acceptDate: '2019-01-22', curNode: 'Phân công nhiệm vụ', flowRate: 12 },
    { reBizCode: 'A002', type: 'Đăng ký thế chấp', acceptBy: 'Lý Ba', acceptDate: '2019-01-23', curNode: 'Nhận nhiệm vụ', flowRate: 3 },
    { reBizCode: 'A003', type: 'Đăng ký chuyển nhượng', acceptBy: 'Vương Tam', acceptDate: '2019-01-25', curNode: 'Xử lý nhiệm vụ', flowRate: 24 },
    { reBizCode: 'A004', type: 'Đăng ký chuyển nhượng', acceptBy: 'Triệu Nhị', acceptDate: '2019-11-22', curNode: 'Phòng ban xét duyệt', flowRate: 10 },
    { reBizCode: 'A005', type: 'Đăng ký chuyển nhượng', acceptBy: 'Tiền Đại', acceptDate: '2019-12-12', curNode: 'Nhận nhiệm vụ', flowRate: 8 },
    { reBizCode: 'A006', type: 'Đăng ký chuyển nhượng', acceptBy: 'Tôn Tựu', acceptDate: '2019-03-06', curNode: 'Xử lý nhiệm vụ', flowRate: 10 },
    { reBizCode: 'A007', type: 'Đăng ký thế chấp', acceptBy: 'Chu Vựng', acceptDate: '2019-04-13', curNode: 'Phòng ban xét duyệt', flowRate: 24 },
    { reBizCode: 'A008', type: 'Đăng ký thế chấp', acceptBy: 'Ngô Hữu', acceptDate: '2019-05-09', curNode: 'Phòng ban xét duyệt', flowRate: 30 },
    { reBizCode: 'A009', type: 'Đăng ký thế chấp', acceptBy: 'Trịnh Vũ', acceptDate: '2019-07-12', curNode: 'Phân công nhiệm vụ', flowRate: 1 },
    { reBizCode: 'A0010', type: 'Đăng ký thế chấp', acceptBy: 'Lâm Sảng', acceptDate: '2019-12-12', curNode: 'Phòng ban xét duyệt', flowRate: 16 },
    { reBizCode: 'A0011', type: 'Đăng ký chuyển nhượng', acceptBy: 'Mã Lâu', acceptDate: '2019-09-10', curNode: 'Phòng ban xét duyệt', flowRate: 7 },
  ],
  columns: [
    {
      title: 'Mã nghiệp vụ',
      align: 'center',
      dataIndex: 'reBizCode',
    },
    {
      title: 'Người tiếp nhận',
      align: 'center',
      dataIndex: 'acceptBy',
    },
    {
      title: 'Thời gian khởi tạo',
      align: 'center',
      dataIndex: 'acceptDate',
    },
    {
      title: 'Nút hiện tại',
      align: 'center',
      dataIndex: 'curNode',
    },
    {
      title: 'Thời gian timeout',
      align: 'center',
      dataIndex: 'flowRate',
    },
  ],
  ipagination: {
    current: 1,
    pageSize: 5,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' Tổng cộng' + total + 'mục';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  },
};
