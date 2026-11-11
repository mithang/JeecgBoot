export interface ListItem {
  id: string;
  avatar: string;
  // Nội dung tiêu đề thông báo
  title: string;
  // Có hiển thị gạch ngang trên tiêu đề không
  titleDelete?: boolean;
  datetime: string;
  type: string;
  read?: boolean;
  description: string;
  clickClose?: boolean;
  extra?: string;
  color?: string;
  // Mức ưu tiên
  priority?: string;
}

export enum PriorityTypes {
  // Ưu tiên thấp, tin nhắn thông thường
  L = 'L',
  // Mức ưu tiên trung bình, tin nhắn quan trọng
  M = 'M',
  // Ưu tiên cao, tin nhắn khẩn cấp
  H = 'H',
}

export interface TabItem {
  key: string;
  name: string;
  list: ListItem[];
  unreadlist?: ListItem[];
  count: number;
}

export const tabListData: TabItem[] = [
  {
    key: '1',
    name: 'Thông báo',
    list: [
      {
        id: '000000001',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png',
        title: 'Bạn đã nhận được 14 báo cáo tuần mới',
        description: '',
        datetime: '2017-08-09',
        type: '1',
      },
      {
        id: '000000002',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/OKJXDXrmkNshAMvwtvhu.png',
        title: 'Bạn giới thiệu Khúc Ni Ni đã qua vòng phỏng vấn thứ ba',
        description: '',
        datetime: '2017-08-08',
        type: '1',
      },
      {
        id: '000000003',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/kISTdvpyTAhtGxpovNWd.png',
        title: 'Mẫu này có thể phân biệt nhiều loại thông báo',
        description: '',
        datetime: '2017-08-07',
        // read: true,
        type: '1',
      },
      {
        id: '000000004',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000005',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Tiêu đề có thể cài đặt tự động hiển thị dấu ba chấm, trong ví dụ này số dòng tiêu đề đã đặt là 1 hàng, nếu nội dung vượt quá 1 hàng sẽ tự động cắt ngắn và hỗ trợ tooltip hiển thị tiêu đề đầy đủ.',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000006',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000007',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000008',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000009',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000010',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Biểu tượng bên trái dùng để phân biệt các loại khác nhau',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
    ],
    count: 0,
  },
  {
    key: '2',
    name: 'Tin nhắn hệ thống',
    list: [
      {
        id: '000000006',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'Khúc Lệ Lệ đã bình luận về bạn',
        description: 'Thông tin mô tả thông tin mô tả thông tin mô tả',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000007',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'Chu Thiên Hữu đã trả lời bạn',
        description: 'Mẫu này dùng để nhắc ai đã tương tác với bạn',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000008',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'Tiêu đề',
        description:
          'Vui lòng di chuột đến đây, để kiểm tra tin nhắn siêu dài ở đây sẽ được xử lý thế nào. Số dòng tối đa của mô tả đặt trong ví dụ này là 2, vượt quá 2 hàng nội dung mô tả sẽ bị lược và có thể thông qua tooltip xem toàn bộ nội dung',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
    ],
    count: 0,
  },
  // {
  //   key: '3',
  //   name: 'Việc cần làm',
  //   list: [
  //     {
  //       id: '000000009',
  //       avatar: '',
  //       title: 'Tên nhiệm vụ',
  //       description: 'Nhiệm vụ cần phải 2017-01-12 20:00 trước khi khởi động',
  //       datetime: '',
  //       extra: 'Chưa bắt đầu',
  //       color: '',
  //       type: '3',
  //     },
  //     {
  //       id: '000000010',
  //       avatar: '',
  //       title: 'Thay đổi mã khẩn cấp của bên thứ ba',
  //       description: 'Quán Lâm Cần ở 2017-01-07 trước khi hoàn thành nhiệm vụ thay đổi mã',
  //       datetime: '',
  //       extra: 'Sắp hết hạn',
  //       color: 'red',
  //       type: '3',
  //     },
  //     {
  //       id: '000000011',
  //       avatar: '',
  //       title: 'Kỳ thi an toàn thông tin',
  //       description: 'Giao cho Trúc Nhĩ vào 2017-01-09 trước khi hoàn thành cập nhật và phát hành',
  //       datetime: '',
  //       extra: 'Đã tốn thời gian 8 ngày',
  //       color: 'gold',
  //       type: '3',
  //     },
  //     {
  //       id: '000000012',
  //       avatar: '',
  //       title: 'ABCD Phát hành phiên bản',
  //       description: 'Giao cho Trúc Nhĩ vào 2017-01-09 trước khi hoàn thành cập nhật và phát hành',
  //       datetime: '',
  //       extra: 'Đang tiến hành',
  //       color: 'blue',
  //       type: '3',
  //     },
  //   ],
  // },
];
