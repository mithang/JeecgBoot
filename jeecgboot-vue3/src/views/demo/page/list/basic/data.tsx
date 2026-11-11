export const cardList = (() => {
  const result: any[] = [];
  for (let i = 0; i < 6; i++) {
    result.push({
      id: i,
      title: 'Jeecg Admin',
      description: 'Dựa trênVue Next, TypeScript, Ant Design Vuemột hệ thống quản lý backend cấp doanh nghiệp hoàn chỉnh',
      datetime: '2020-11-26 17:39',
      extra: 'Chỉnh sửa',
      icon: 'logos:vue',
      color: '#1890ff',
      author: 'Jeecg',
      percent: 20 * (i + 1),
    });
  }
  return result;
})();
