//Biểu mẫu cấu hình app bên thứ ba
import { FormSchema } from '/@/components/Form';

//Biểu mẫu app bên thứ ba
export const thirdAppFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'thirdType',
    field: 'thirdType',
    component: 'Input',
    show: false,
  },
  {
    label: 'CorpId',
    field: 'corpId',
    component: 'Input',
    ifShow: ({ values }) => {
      return values.thirdType === 'dingtalk';
    },
    required: true,
  },
  {
    label: 'Agentld',
    field: 'agentId',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => {
    // Feishu không có cấu hình Agentld
      return values.thirdType !== 'feishu';
    }
  },
  {
    label: 'AppKey',
    field: 'clientId',
    component: 'Input',
    required: true,
  },
  {
    label: 'AppSecret',
    field: 'clientSecret',
    component: 'Input',
    required: true,
  },{
    label: 'Kích hoạt',
    field: 'status',
    component: 'Switch',
    componentProps:{
      checkedChildren:'Đóng',
      checkedValue:1,
      unCheckedChildren:'Bật',
      unCheckedValue: 0
    },
    defaultValue: 1
  },{
    label: 'Đơn vị thuê (tenant)id',
    field: 'tenantId',
    component: 'Input',
    show: false,
  },
];
