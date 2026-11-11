import { BasicColumn, FormSchema } from '/@/components/Table';
import { usePermission } from '/@/hooks/web/usePermission';
import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
const { isDisabledAuth, hasPermission, initBpmFormData} = usePermission();

export const columns: BasicColumn[] = [
  {
    title: 'Số đơn hàng',
    dataIndex: 'orderCode',
    width: 260,
  },
  {
    title: 'Loại đơn hàng',
    dataIndex: 'ctype',
    slots: { customRender: 'ctype' },
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
  {
    title: 'Trạng thái quy trình',
    width: 200,
    dataIndex: 'bpmStatus',
    customRender: ({ text }) => {
      if (!text || text == '1') {
        return 'Chờ gửi';
      } else if (text == '2') {
        return 'Đang xử lý';
      } else if (text == '2') {
        return 'Đã hoàn thành';
      } else {
        return text;
      }
    },
  },
];

export function getBpmFormSchema(formData) {
  //Chèn quyền biểu mẫu của nút quy trình
  initBpmFormData(formData);
  
  const formSchema2: FormSchema[] = [
    {
      label: 'Số đơn hàng',
      field: 'orderCode',
      component: 'Input',
      show: ({ values }) => {
        return hasPermission('order:orderCode');
      },
    },
    {
      label: 'Loại đơn hàng',
      field: 'ctype',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Đơn hàng nội địa', value: '1', key: '1' },
          { label: 'Đơn hàng quốc tế', value: '2', key: '2' },
        ],
      },
    },
    {
      label: 'Ngày đặt hàng',
      field: 'orderDate',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        style: {
          width: '100%',
        },
      },
    },
    {
      label: 'Số tiền đơn hàng',
      field: 'orderMoney',
      component: 'Input',
    },
    {
      label: 'Ghi chú đơn hàng',
      field: 'content',
      component: 'Input',
    },
  ];
  return formSchema2;
}

export function getOrderCustomerFormSchema(formData) {
  //Chèn quyền biểu mẫu của nút quy trình
  initBpmFormData(formData);
  
  const formSchema2: FormSchema[] = [
    {
      label: 'Tên khách hàng',
      field: 'name',
      component: 'Input',
      dynamicDisabled: ({ values }) => {
        return isDisabledAuth('order:name');
      },
    },
    {
      label: 'Giới tính',
      field: 'sex',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Nam', value: '1', key: '1' },
          { label: 'Nữ', value: '2', key: '2' },
        ],
      },
    },
    {
      label: 'Số chứng minh nhân dân',
      field: 'idcard',
      component: 'Input',
    },
    {
      label: 'Số điện thoại',
      field: 'telphone',
      component: 'Input',
    },
  ];
  return formSchema2;
}

export const jeecgOrderTicketColumns: JVxeColumn[] = [
  {
    title: 'Số hiệu chuyến bay',
    key: 'ticketCode',
    width: 180,
    type: JVxeTypes.input,
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'Thời gian chuyến bay',
    key: 'tickectDate',
    width: 180,
    type: JVxeTypes.date,
    placeholder: 'Vui lòng chọn${title}',
    defaultValue: '',
  },
];
