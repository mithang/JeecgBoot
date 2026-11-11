interface GroupItem {
  title: string;
  icon: string;
  color: string;
  desc: string;
  date: string;
  group: string;
}

interface NavItem {
  title: string;
  icon: string;
  color: string;
}

interface DynamicInfoItem {
  avatar: string;
  name: string;
  date: string;
  desc: string;
}

export const navItems: NavItem[] = [
  {
    title: 'Trang chủ',
    icon: 'ion:home-outline',
    color: '#1fdaca',
  },
  {
    title: 'Bảng điều khiển',
    icon: 'ion:grid-outline',
    color: '#bf0c2c',
  },
  {
    title: 'Component',
    icon: 'ion:layers-outline',
    color: '#e18525',
  },
  {
    title: 'Quản lý hệ thống',
    icon: 'ion:settings-outline',
    color: '#3fb27f',
  },
  {
    title: 'Quản lý quyền',
    icon: 'ion:key-outline',
    color: '#4daf1bc9',
  },
  {
    title: 'Biểu đồ',
    icon: 'ion:bar-chart-outline',
    color: '#00d8ff',
  },
];

export const dynamicInfoItems: DynamicInfoItem[] = [
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: 'Vừa xong',
    desc: `Trong <a>Nhóm mã nguồn mở</a> Đã tạo dự án <a>Vue</a>`,
  },
  {
    avatar: 'dynamic-avatar-2|svg',
    name: 'Aiwen',
    date: '1 giờ trước',
    desc: `Đã theo dõi <a>William</a> `,
  },
  {
    avatar: 'dynamic-avatar-3|svg',
    name: 'Chris',
    date: '1 ngày trước',
    desc: `Đã đăng <a>Hoạt động cá nhân</a> `,
  },
  {
    avatar: 'dynamic-avatar-4|svg',
    name: 'Jeecg',
    date: '2 ngày trước',
    desc: `Đăng bài viết <a>Cách viết một Vite Plugin</a> `,
  },
  {
    avatar: 'dynamic-avatar-5|svg',
    name: 'Pete',
    date: '3 ngày trước',
    desc: `Đã trả lời <a>Jack</a> vấn đề <a>Cách tối ưu hóa dự án?</a>`,
  },
  {
    avatar: 'dynamic-avatar-6|svg',
    name: 'Jack',
    date: '1 tuần trước',
    desc: `Đã đóng vấn đề <a>Cách chạy dự án</a> `,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: '1 tuần trước',
    desc: `Đã đăng <a>Hoạt động cá nhân</a> `,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'William',
    date: '2021-04-01 20:00',
    desc: `Đã push mã đến <a>Github</a>`,
  },
];

export const groupItems: GroupItem[] = [
  {
    title: 'Github',
    icon: 'carbon:logo-github',
    color: '',
    desc: 'Đừng chờ đợi cơ hội, mà hãy tạo ra cơ hội.',
    group: 'Nhóm mã nguồn mở',
    date: '2021-04-01',
  },
  {
    title: 'Vue',
    icon: 'ion:logo-vue',
    color: '#3fb27f',
    desc: 'Bạn của hiện tại quyết định bạn của tương lai.',
    group: 'Nhóm thuật toán',
    date: '2021-04-01',
  },
  {
    title: 'Html5',
    icon: 'ion:logo-html5',
    color: '#e18525',
    desc: 'Không có tài năng nào quan trọng hơn sự nỗ lực.',
    group: 'Lười biếng ở chỗ làm',
    date: '2021-04-01',
  },
  {
    title: 'Angular',
    icon: 'ion:logo-angular',
    color: '#bf0c2c',
    desc: 'Nhiệt huyết và khát vọng có thể vượt qua mọi khó khăn.',
    group: 'UI',
    date: '2021-04-01',
  },
  {
    title: 'React',
    icon: 'bx:bxl-react',
    color: '#00d8ff',
    desc: 'Cơ thể khỏe mạnh là nền tảng để thực hiện mục tiêu.',
    group: 'Kỹ thuật giỏi',
    date: '2021-04-01',
  },
  {
    title: 'Js',
    icon: 'ion:logo-javascript',
    color: '#4daf1bc9',
    desc: 'Đường là đi ra, không phải nghĩ suông mà ra.',
    group: 'Nhóm kiến trúc',
    date: '2021-04-01',
  },
];
