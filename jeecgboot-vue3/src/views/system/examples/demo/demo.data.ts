import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: 'Họ tên',
    dataIndex: 'name',
    width: 170,
    align: 'left',
    resizable: true,
    sorter: {
      multiple:1
    }
  },
  {
    title: 'Từ khóa',
    dataIndex: 'keyWord',
    width: 130,
    resizable: true,
  },
  {
    title: 'Thời gian chấm công',
    dataIndex: 'punchTime',
    width: 140,
    resizable: true,
  },
  {
    title: 'Lương',
    dataIndex: 'salaryMoney',
    width: 140,
    resizable: true,
    sorter: {
      multiple: 2
    }
  },
  {
    title: 'Tiền thưởng',
    dataIndex: 'bonusMoney',
    width: 140,
    resizable: true,
  },
  {
    title: 'Giới tính',
    dataIndex: 'sex',
    sorter: {
      multiple: 3
    },
    customRender: ({ record }) => {
      return render.renderDict(record.sex, 'sex', true);
      // let v = record.sex ? (record.sex == '1' ? 'Nam' : 'Nữ') : '';
      // return h('span', v);
    },
    width: 120,
    resizable: true,
  },
  {
    title: 'Ngày sinh',
    dataIndex: 'birthday',
    width: 120,
    resizable: true,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    width: 120,
    resizable: true,
  },
  {
    title: 'Giới thiệu cá nhân',
    dataIndex: 'content',
    width: 120,
    resizable: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Họ tên',
    component: 'Input',
    componentProps: {
      trim: true,
    },
    colProps: { span: 8 },
  },
  {
    field: 'birthday',
    label: 'Ngày sinh',
    component: 'RangePicker',
    componentProps: {
      valueType: 'Date'
    },
    colProps: { span: 8 },
  },
  {
    field: 'age',
    label: 'Tuổi',
    component: 'Input',
    slot: 'age',
    colProps: { span: 8 },
  },
  {
    field: 'sex',
    label: 'Giới tính',
    colProps: { span: 8 },
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'createBy',
    label: 'createBy',
    component: 'Input',
    show: false,
  },
  {
    field: 'createTime',
    label: 'createTime',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: 'Tên',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Vui lòng nhập tên',
    },
  },
  {
    field: 'keyWord',
    label: 'Từ khóa',
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập từ khóa',
    },
  },
  {
    field: 'punchTime',
    label: 'Thời gian chấm công',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: 'Vui lòng chọn thời gian chấm công',
    },
  },
  {
    field: 'salaryMoney',
    label: 'Lương',
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập lương',
    },
  },
  {
    field: 'sex',
    label: 'Giới tính',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      type: 'radio',
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
    },
  },
  {
    field: 'age',
    label: 'Tuổi',
    component: 'InputNumber',
    defaultValue: 1,
    componentProps: {
      placeholder: 'Vui lòng nhập tuổi',
    },
  },
  {
    field: 'birthday',
    label: 'Ngày sinh',
    component: 'DatePicker',
    defaultValue: '',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      placeholder: 'Vui lòng chọn ngày sinh',
    },
  },
  {
    field: 'email',
    label: 'Email',
    component: 'Input',
    rules: [{ required: false, type: 'email', message: 'Định dạng email không đúng', trigger: 'blur' }],
    componentProps: {
      placeholder: 'Vui lòng nhập email',
    },
  },
  {
    field: 'content',
    label: 'Giới thiệu cá nhân - To introduce myself',
    component: 'InputTextArea',
    labelLength: 4,
    componentProps: {
      placeholder: 'Vui lòng nhập giới thiệu cá nhân',
    },
  },
  {
    field: 'updateCount',
    label: 'Khóa lạc quan (optimistic lock)',
    show: false,
    component: 'Input',
  },
];
