import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: 'Số đơn hàng',
    dataIndex: 'orderCode',
    width: 260,
  },
  {
    title: 'Loại đơn hàng',
    dataIndex: 'ctype',
    width: 160,
    customRender: ({ text }) => {
      return text == '1' ? 'Đơn hàng nội địa' : text == '2' ? 'Đơn hàng quốc tế' : '';
    },
  },
  {
    title: 'Ngày đặt hàng',
    dataIndex: 'orderDate',
    width: 300,
  },
  {
    title: 'Số tiền đơn hàng',
    width: 200,
    dataIndex: 'orderMoney',
  },
  {
    title: 'Ghi chú đơn hàng',
    width: 200,
    dataIndex: 'content',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Số đơn hàng',
    field: 'orderCode',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Loại đơn hàng',
    field: 'ctype',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: 'Đơn hàng nội địa',
          value: '1',
          key: '1',
        },
        {
          label: 'Đơn hàng quốc tế',
          value: '2',
          key: '2',
        },
      ],
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Số đơn hàng',
    field: 'orderCode',
    component: 'Input',
    required: true,
  },
  {
    label: 'Loại đơn hàng',
    field: 'ctype',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: 'Đơn hàng nội địa',
          value: '1',
          key: '1',
        },
        {
          label: 'Đơn hàng quốc tế',
          value: '2',
          key: '2',
        },
      ],
    },
  },
  {
    label: 'Ngày đặt hàng',
    field: 'orderDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD hh:mm:ss',
    },
  },
  {
    label: 'Số tiền đơn hàng',
    field: 'orderMoney',
    component: 'InputNumber',
  },
  {
    label: 'Ghi chú đơn hàng',
    field: 'content',
    component: 'Input',
  },
];

export const customColumns: BasicColumn[] = [
  {
    title: 'Tên khách hàng',
    dataIndex: 'name',
    width: 260,
  },
  {
    title: 'Giới tính',
    dataIndex: 'sex',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: 'Số chứng minh nhân dân',
    dataIndex: 'idcard',
    width: 300,
  },
  {
    title: 'Điện thoại',
    width: 200,
    dataIndex: 'telphone',
  },
];

export const customerFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên khách hàng',
    field: 'name',
    component: 'Input',
    required: true,
  },
  {
    label: 'Giới tính',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
    },
  },
  {
    label: 'Số chứng minh nhân dân',
    field: 'idcard',
    component: 'Input',
  },
  {
    label: 'Bản scan chứng minh nhân dân',
    field: 'idcardPic',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 2,
    },
  },
  {
    label: 'Thông tin liên hệ',
    field: 'telphone',
    component: 'Input',
    rules: [{ required: false, pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại sai' }],
  },
  {
    label: 'orderId',
    field: 'orderId',
    component: 'Input',
    show: false,
  },
];

export const ticketColumns: BasicColumn[] = [
  {
    title: 'Số hiệu chuyến bay',
    dataIndex: 'ticketCode',
  },
  {
    title: 'Thời gian chuyến bay',
    dataIndex: 'tickectDate',
  },
  {
    title: 'Người tạo',
    dataIndex: 'createBy',
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
  },
];

export const ticketFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Số hiệu chuyến bay',
    field: 'ticketCode',
    component: 'Input',
    required: true,
  },
  {
    label: 'Thời gian chuyến bay',
    field: 'tickectDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      getPopupContainer:()=>document.body,
    },
  },
  {
    label: 'orderId',
    field: 'orderId',
    component: 'Input',
    show: false,
  },
];
