export interface ListItem {
  title: string;
  icon: string;
  color?: string;
}

export interface TabItem {
  key: string;
  name: string;
  component: string;
}

export const tags: string[] = ['Rất nhiều ý tưởng', 'Chuyên tâm thiết kế', 'Cô gái Tứ Xuyên', 'Chân dài', 'Biển lớn chứa trăm sông', 'Phát triển frontend', 'vue3'];
<span class="iconify" data-icon="jam:codepen-circle" data-inline="false"></span>;
export const teams: ListItem[] = [
  {
    icon: 'ri:alipay-fill',
    title: 'Nhóm cày code khoa học',
    color: '#ff4000',
  },
  {
    icon: 'emojione-monotone:letter-a',
    title: 'Nhóm thiếu niên Chuunibyou',
    color: '#7c51b8',
  },
  {
    icon: 'ri:alipay-fill',
    title: 'Thiết kế đẳng cấp',
    color: '#00adf7',
  },
  {
    icon: 'jam:codepen-circle',
    title: 'Đời thường của lập trình viên',
    color: '#00adf7',
  },
  {
    icon: 'fa:behance-square',
    title: 'Nhóm cày code khoa học',
    color: '#7c51b8',
  },
  {
    icon: 'jam:codepen-circle',
    title: 'Đời thường của lập trình viên',
    color: '#ff4000',
  },
];

export const details: ListItem[] = [
  {
    icon: 'ic:outline-contacts',
    title: 'Chuyên gia tương tác',
  },
  {
    icon: 'grommet-icons:cluster',
    title: 'Nhóm sự nghiệp XXX',
  },
  {
    icon: 'bx:bx-home-circle',
    title: 'Thành phố Hạ Môn, tỉnh Phúc Kiến',
  },
];

export const achieveList: TabItem[] = [
  {
    key: '1',
    name: 'Bài viết',
    component: 'Article',
  },
  {
    key: '2',
    name: 'Ứng dụng',
    component: 'Application',
  },
  {
    key: '3',
    name: 'Dự án',
    component: 'Project',
  },
];

export const actions: any[] = [
  { icon: 'clarity:star-line', text: '156', color: '#018ffb' },
  { icon: 'bx:bxs-like', text: '156', color: '#459ae8' },
  { icon: 'bx:bxs-message-dots', text: '2', color: '#42d27d' },
];

export const articleList = (() => {
  const result: any[] = [];
  for (let i = 0; i < 4; i++) {
    result.push({
      title: 'Jeecg Admin',
      description: ['Jeecg', 'Ngôn ngữ thiết kế', 'Typescript'],
      content: 'Dựa trênVue Next, TypeScript, Ant Designmột hệ thống quản lý backend cấp doanh nghiệp hoàn chỉnh.',
      time: '2020-11-14 11:20',
    });
  }
  return result;
})();

export const applicationList = (() => {
  const result: any[] = [];
  for (let i = 0; i < 8; i++) {
    result.push({
      title: 'Jeecg Admin',
      icon: 'emojione-monotone:letter-a',
      color: '#1890ff',
      active: '100',
      new: '1,799',
      download: 'bx:bx-download',
    });
  }
  return result;
})();

export const projectList = (() => {
  const result: any[] = [];
  for (let i = 0; i < 8; i++) {
    result.push({
      title: 'Jeecg Admin',
      content: 'Dựa trênVue Next, TypeScript, Ant Designmột hệ thống quản lý backend cấp doanh nghiệp hoàn chỉnh.',
    });
  }
  return result;
})();
