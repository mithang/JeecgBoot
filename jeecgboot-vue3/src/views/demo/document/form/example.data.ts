import { FormSchema } from '/@/components/Form';

import dayjs from 'dayjs';

export const schemas: FormSchema[] = [
  {
    label: 'Hộp văn bản',
    field: 'name',
    component: 'Input',
    componentProps: {
      prefix: 'Tiếng Trung',
      showCount: true,
    },
    defaultValue: 'Trương Tam',
  },
  {
    label: 'Mật khẩu',
    field: 'password',
    component: 'InputPassword',
    componentProps: {
      //Có hiển thị nút chuyển đổi hay điều khiển hiện/ẩn mật khẩu hay không
      visibilityToggle: true,
      prefix: 'Mật khẩu',
    },
  },
  {
    label: 'Hộp tìm kiếm',
    field: 'searchBox',
    component: 'InputSearch',
    componentProps: {
      onSearch: (value) => {
        console.log(value);
      },
    },
  },
  {
    label: 'Vùng văn bản',
    field: 'textArea',
    component: 'InputTextArea',
    componentProps: {
      //Có thể nhấp vào biểu tượng xóa để xóa nội dung
      allowClear: true,
      //Có hiển thị số ký tự không
      showCount: true,
      //Chiều cao tự thích ứng theo nội dung, có thể đặt thành true | false hoặc đối tượng:{ minRows: 2, maxRows: 6 }
      autoSize: {
        //Số dòng hiển thị tối thiểu
        minRows: 2,
        //Số dòng hiển thị tối đa
        maxRows: 3,
      },
    },
  },
  {
    label: 'Ô nhập số',
    field: 'number',
    component: 'InputNumber',
    componentProps: {
      //input có nhãn, đặt nhãn phía sau
      addonAfter: 'Giữ lại hai chữ số thập phân',
      //Giá trị lớn nhất
      max: 100,
      //Kinh độ số
      precision: 2,
      //Số bước
      step: 0.1,
    },
  },

  {
    label: 'Dropdown',
    field: 'jinputtype',
    component: 'Select',
    componentProps: {
      options: [
        { value: 'like', label: 'Mờ (like)' },
        { value: 'ne', label: 'Không bằng (ne)' },
        { value: 'ge', label: 'Lớn hơn hoặc bằng (ge)' },
        { value: 'le', label: 'Nhỏ hơn hoặc bằng (le)' },
      ],
      //Danh sách thả xuống chọn nhiều
      mode: 'multiple',
      //Cấu hình có thể tìm kiếm hay không
      showSearch: true,
    },
  },
  {
    field: 'TreeSelect',
    label: 'Cây dropdown',
    component: 'TreeSelect',
    componentProps: {
      //Có hiển thị hộp thả xuống không, mặc địnhfalse
      treeCheckable: true,
      //Tiêu đề
      title: 'Cây dropdown',
      //Cây dropdown
      treeData: [
        {
          label: 'Máy giặt',
          value: '0',
          children: [
            {
              label: 'Máy giặt lồng ngang',
              value: '0-1',
            },
          ],
        },
        {
          label: 'Tivi',
          value: '1',
          children: [
            {
              label: 'TV màn hình phẳng',
              value: '1-1',
              disabled: true,
            },
            {
              label: 'CRTTivi',
              value: '1-2',
            },
            {
              label: 'TV máy chiếu',
              value: '1-3',
            },
          ],
        },
      ],
    },
  },
  {
    label: 'RadioButtonGroupComponent',
    field: 'status',
    component: 'RadioButtonGroup',
    componentProps: {
      options: [
        { label: 'Hiệu lực', value: 1 },
        { label: 'Không hợp lệ', value: 0 },
      ],
    },
  },
  {
    label: 'Hộp chọn đơn',
    field: 'radioSex',
    component: 'RadioGroup',
    componentProps: {
      //optionsBên trong gồm từng radio một,Hỗ trợ vô hiệu hóa disabled
      options: [
        { label: 'Nam', value: 1, disabled: false },
        { label: 'Nữ', value: 0 },
      ],
    },
  },
  {
    label: 'Hộp chọn nhiều',
    field: 'checkbox',
    component: 'Checkbox',
    componentProps: {
      //Có vô hiệu hóa hay không,Mặc địnhfalse
      disabled: false,
    },
  },
  {
    label: 'Nhóm checkbox',
    field: 'checkSex',
    component: 'CheckboxGroup',
    componentProps: {
      //RadioGroup bên dưới tất cả input[type="radio"] Thuộc tính name của
      name: 'Sở thích',
      //optionsHỗ trợ vô hiệu hóa disabled
      options: [
        { label: 'Thể thao', value: 0, disabled: true },
        { label: 'Nghe nhạc', value: 1 },
        { label: 'Đọc sách', value: 2 },
      ],
    },
    defaultValue: [2],
  },
  {
    label: 'Component tự động hoàn thành',
    field: 'AutoComplete',
    component: 'AutoComplete',
    componentProps: {
      options: [{ value: 'Burns Bay Road' }, { value: 'Downing Street' }, { value: 'Wall Street' }],
    },
  },
  {
    label: 'Chọn liên cấp',
    field: 'cascade',
    component: 'Cascader',
    componentProps: {
      //Hiển thị tối đa bao nhiêu tagtag
      maxTagCount: 2,
      //Vị trí đặt trước của lớp nổi
      placement: 'bottomRight',
      //Hiển thị hộp tìm kiếm trong hộp chọn,Mặc địnhfalse
      showSearch: true,
      options: [
        {
          label: 'Bắc Kinh',
          value: 'BeiJin',
          children: [
            {
              label: 'Quận Hải Điến',
              value: 'HaiDian',
            },
          ],
        },
        {
          label: 'Giang Tô',
          value: 'JiangSu',
          children: [
            {
              label: 'Nam Kinh',
              value: 'Nanjing',
              children: [
                {
                  label: 'Cổng Trung Hoa',
                  value: 'ZhongHuaMen',
                },
              ],
            },
          ],
        },
      ],
    },
  },
  {
    label: 'Chọn ngày',
    field: 'dateSelect',
    component: 'DatePicker',
    componentProps: {
      //Định dạng ngày, giá trị hiển thị trên trang
      format: 'YYYY-MM-DD',
      //Định dạng giá trị trả về (định dạng của giá trị liên kết)
      valueFormat: 'YYYY-MM-DD',
      //Có hiển thị nút hôm nay không
      showToday: true,
      //Ngày không thể chọn
      disabledDate: (currentDate) => {
        let date = dayjs(currentDate).format('YYYY-MM-DD');
        let nowDate = dayjs(new Date()).format('YYYY-MM-DD');
        //Ngày hôm đó không thể chọn
        if (date == nowDate) {
          return true;
        }
        return false;
      },
    },
  },
  {
    label: 'Chọn tháng',
    field: 'monthSelect',
    component: 'MonthPicker',
    componentProps: {
      //Ngày không thể chọn
      disabledDate: (currentDate) => {
        let date = dayjs(currentDate).format('YYYY-MM');
        let nowDate = dayjs(new Date()).format('YYYY-MM');
        //Ngày hôm đó không thể chọn
        if (date == nowDate) {
          return true;
        }
        return false;
      },
    },
  },
  {
    label: 'Chọn tuần',
    field: 'weekSelect',
    component: 'WeekPicker',
    componentProps: {
      size: 'small',
    },
  },
  {
    label: 'Chọn thời gian',
    field: 'timeSelect',
    component: 'TimePicker',
    componentProps: {
      size: 'default',
      //Ở chế độ ngày giờ hoặc chế độ thời gian có hiển thị thời điểm hiện tại hay không, không hỗ trợ phạm vi ngày giờ và phạm vi thời gian
      showNow: true,
    },
  },
  {
    label: 'Phạm vi ngày giờ',
    field: 'dateTimeRangeSelect',
    component: 'RangePicker',
    componentProps: {
      //Có hiển thị thời gian không
      showTime: true,
      //Định dạng ngày
      format: 'YYYY/MM/DD HH:mm:ss',
      //Mô tả văn bản khoảng dùng tập hợp
      placeholder: ['Vui lòng chọn ngày giờ bắt đầu', 'Vui lòng chọn ngày giờ kết thúc'],
    },
  },
  {
    label: 'Phạm vi ngày',
    field: 'dateRangeSelect',
    component: 'RangeDate',
    componentProps: {
      //Định dạng ngày
      format: 'YYYY/MM/DD',
      //Mô tả văn bản khoảng dùng tập hợp
      placeholder: ['Vui lòng chọn ngày bắt đầu', 'Vui lòng chọn ngày kết thúc'],
    },
  },
  {
    label: 'Phạm vi thời gian',
    field: 'timeRangeSelect',
    component: 'RangeTime',
    componentProps: {
      //Định dạng ngày
      format: 'HH/mm/ss',
      //Mô tả văn bản khoảng dùng tập hợp
      placeholder: ['Vui lòng chọn thời gian bắt đầu', 'Vui lòng chọn thời gian kết thúc'],
    },
  },
  {
    label: 'Công tắc',
    field: 'switch',
    component: 'Switch',
    componentProps: {
      //Kích thước công tắc, giá trị tùy chọn:default small
      size: 'default',
      //Nội dung khi không được chọn
      unCheckedChildren: 'Bật',
      //Giá trị khi không được chọn
      unCheckedValue: '0',
      //Nội dung khi được chọn
      checkedChildren: 'Đóng',
      //Giá trị khi được chọn
      checkedValue: '1',
      //Có vô hiệu hóa hay không
      disabled: false,
    },
  },
  {
    label: 'Thanh nhập trượt',
    field: 'slider',
    component: 'Slider',
    componentProps: {
      //Giá trị nhỏ nhất
      min: -20,
      //Giá trị lớn nhất
      max: 100,
      //Có phải chế độ hai thanh trượt không
      range: true,
      //Đánh dấu vạch chia
      marks: {
        '-20': '-20°C',
        0: '0°C',
        26: '26°C',
        37: '37°C',
        100: {
          style: {
            color: '#f50',
          },
          label: '100°C',
        },
      },
    },
  },
  {
    label: 'Đánh giá',
    field: 'rate',
    component: 'Rate',
    componentProps: {
      //Có cho phép chọn một nửa không
      allowHalf: true,
      //star Tổng số
      count: 5,
      //tooltipGợi ý: Có bao nhiêu sao thì viết bấy nhiêu
      tooltips: ['Rất kém', 'Kém', 'Bình thường', 'Rất tốt', 'Chưa tốt lắm'],
    },
  },
  {
    label: 'Đường phân cách',
    field: 'divisionLine',
    component: 'Divider',
    componentProps: {
      //Có đường nét đứt hay không
      dashed: false,
      //Vị trí tiêu đề đường phân cách (left | right | center）
      orientation: 'center',
      //Chữ có hiển thị theo kiểu văn bản thường hay không
      plain: true,
      //Loại ngang hay dọc (horizontal | vertical）
      type: 'horizontal',
    },
  },
];
