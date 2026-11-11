import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';

export const columns: JVxeColumn[] = [
  {
    title: 'Tên khách hàng',
    key: 'name',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Vui lòng nhập${title}',
    validateRules: [{ required: true, message: '${title}Không được trống' }],
  },
  {
    title: 'Giới tính',
    key: 'sex',
    width: 180,
    type: JVxeTypes.select,
    options: [
      // Tùy chọn danh sách thả xuống
      { title: 'Nam', value: '1' },
      { title: 'Nữ', value: '2' },
    ],
    defaultValue: '',
    placeholder: 'Vui lòng chọn${title}',
  },
  {
    title: 'Số chứng minh nhân dân',
    key: 'idcard',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Vui lòng nhập${title}',
    validateRules: [
      {
        pattern: '^\\d{6}(18|19|20)?\\d{2}(0[1-9]|1[012])(0[1-9]|[12]\\d|3[01])\\d{3}(\\d|[xX])$',
        message: '${title}Định dạng không đúng',
      },
    ],
  },
  {
    title: 'Số điện thoại',
    key: 'telphone',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Vui lòng nhập${title}',
    validateRules: [
      {
        pattern: '^1[3456789]\\d{9}$',
        message: '${title}Định dạng không đúng',
      },
    ],
  },
];
export const columns1: JVxeColumn[] = [
  {
    title: 'Số hiệu chuyến bay',
    key: 'ticketCode',
    width: 180,
    type: JVxeTypes.input,
    defaultValue: '',
    placeholder: 'Vui lòng nhập${title}',
    validateRules: [{ required: true, message: '${title}Không được trống' }],
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
