import { FormSchema } from '/@/components/Form/index';

export const searchList = (() => {
  const result: any[] = [];
  for (let i = 0; i < 6; i++) {
    result.push({
      id: i,
      title: 'Jeecg Admin',
      description: ['Jeecg', 'Ngôn ngữ thiết kế', 'Typescript'],
      content: 'Dựa trênVue Next, TypeScript, Ant Designmột hệ thống quản lý backend cấp doanh nghiệp hoàn chỉnh.',
      time: '2020-11-14 11:20',
    });
  }
  return result;
})();

export const actions: any[] = [
  { icon: 'clarity:star-line', text: '156', color: '#018ffb' },
  { icon: 'bx:bxs-like', text: '156', color: '#459ae8' },
  { icon: 'bx:bxs-message-dots', text: '2', color: '#42d27d' },
];

export const schemas: FormSchema[] = [
  {
    field: 'field1',
    component: 'InputSearch',
    label: 'Tên dự án',
    colProps: {
      span: 8,
    },
    componentProps: {
      onChange: (e: any) => {
        console.log(e);
      },
    },
  },
];
