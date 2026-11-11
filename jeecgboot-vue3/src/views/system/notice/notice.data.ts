import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { h } from 'vue';
import { Tinymce } from '@/components/Tinymce';

export const columns: BasicColumn[] = [
  {
    title: 'Tiêu đề',
    width: 150,
    dataIndex: 'titile',
  },
  {
    title: 'Loại tin nhắn',
    dataIndex: 'msgCategory',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_category');
    },
  },
  {
    title: 'Người phát hành',
    width: 100,
    dataIndex: 'sender_dictText',
  },
  {
    title: 'Mức ưu tiên',
    dataIndex: 'priority',
    width: 70,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: 'Đối tượng thông cáo',
    dataIndex: 'msgType',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_type');
    },
  },
  {
    title: 'Trạng thái phát hành',
    dataIndex: 'sendStatus',
    width: 70,
    customRender: ({ text }) => {
      const color = text == '0' ? 'red' : text == '1' ? 'green' : 'gray';
      return render.renderTag(render.renderDict(text, 'send_status'), color);
    },
  },
  {
    title: 'Thời gian phát hành',
    width: 100,
    dataIndex: 'sendTime',
  },
  {
    title: 'Thời gian thu hồi',
    width: 100,
    dataIndex: 'cancelTime',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: 'Tiêu đề',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    field: 'msgCategory',
    label: 'Loại tin nhắn',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'msg_category',
      placeholder: 'Vui lòng chọn loại',
    },
    colProps: { span: 6 },
  },
  {
    field: 'msgClassify',
    label: 'Phân loại thông báo',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'notice_type',
      placeholder: 'Vui lòng chọn phân loại thông báo',
    },
    colProps: { span: 6 },
  },
  {
    field: 'sendTime',
    label: 'Thời gian phát hành',
    component: 'RangePicker',
    componentProps: {
      valueType: 'Date',
    },
    colProps: { span: 6 },
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
    field: 'msgCategory',
    label: 'Loại tin nhắn',
    required: true,
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      type: 'radio',
      dictCode: 'msg_category',
      placeholder: 'Vui lòng chọn loại',
    },
  },
  {
    field: 'izTop',
    label: 'Có ghim lên đầu không',
    defaultValue: '0',
    component: 'JSwitch',
    componentProps: {
      //Lấy giá trị options
      options: ['1', '0'],
      //Văn bảnoption
      labelOptions: ['Có', 'Không'],
      placeholder: 'Có ghim lên đầu không',
      checkedChildren: 'Có',
      unCheckedChildren: 'Không',
    },
  },
  {
    field: 'titile',
    label: 'Tiêu đề thông cáo',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: 'Vui lòng nhập tiêu đề',
    },
    // Giải thích logic mã: 【TV360X-1632】Tiêu đề quá dài gây lỗi khi lưu, kiểm tra độ dài
    dynamicRules() {
      return [
        {
          validator: (_, value) => {
            return new Promise<void>((resolve, reject) => {
              if (value.length > 100) {
                reject('Tối đa 100 ký tự');
              }
              resolve();
            });
          },
        },
      ];
    },
  },
  {
    field: 'msgAbstract',
    label: 'Tóm tắt thông cáo',
    component: 'InputTextArea',
    componentProps: {
      allowClear: true,
      autoSize: {
        minRows: 2,
        maxRows: 5,
      },
    },
    required: true,
  },
  // {
  //   field: 'endTime',
  //   label: 'Ngày kết thúc',
  //   component: 'DatePicker',
  //   componentProps: {
  //     showTime: true,
  //     valueFormat: 'YYYY-MM-DD HH:mm:ss',
  //     placeholder: 'Vui lòng chọn ngày kết thúc',
  //   },
  //   dynamicRules: ({ model }) => rules.endTime(model.startTime, true),
  // },
  {
    field: 'msgType',
    label: 'Người dùng tiếp nhận',
    defaultValue: 'ALL',
    component: 'JDictSelectTag',
    required: true,
    componentProps: {
      type: 'radio',
      dictCode: 'msg_type',
      placeholder: 'Vui lòng chọn phạm vi phát hành',
    },
  },
  {
    field: 'userIds',
    label: 'Người dùng chỉ định',
    component: 'JSelectUserByDepartment',
    required: true,
    componentProps: {
      rowKey: 'id',
      // Giải thích logic mã: 【TV360X-1627】Component chọn người dùng trong thông báo chưa được dịch
      labelKey: 'realname',
    },
    ifShow: ({ values }) => values.msgType == 'USER',
  },
  {
    field: 'msgClassify',
    label: 'Phân loại thông báo',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'notice_type',
      placeholder: 'Vui lòng chọn phân loại thông báo',
    },
  },
  {
    field: 'priority',
    label: 'Cấp độ ưu tiên',
    defaultValue: 'H',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'priority',
      type: 'radio',
      placeholder: 'Vui lòng chọn mức ưu tiên',
    },
  },
  {
    field: 'izApproval',
    label: 'Có phê duyệt không',
    component: 'RadioGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        {
          label: 'Có',
          value: '1',
        },
        {
          label: 'Không',
          value: '0',
        },
      ],
    },
  },
  {
    field: 'msgTemplate',
    label: 'Mẫu thông báo',
    component: 'Input',
    slot: 'msgTemplate',
  },
  {
    field: 'files',
    label: 'Tệp đính kèm thông cáo',
    component: 'JUpload',
    componentProps: {
      //Có hiển thị nút chọn không
      text: 'Tải tệp lên',
      //Số lượng tải lên tối đa
      maxCount: 20,
      //Có hiển thị nút tải xuống không
      download: true,
    },
  },
  {
    field: 'msgContent',
    label: 'Nội dung thông cáo',
    component: 'Input',
    colProps: { span: 24 },
    render: render.renderTinymce,
  },
];

/**
 * Biểu mẫu quy trình gọi phương thức này để lấyformSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // Mặc định giữ nhất quán với biểu mẫu gốc; nếu trong quy trình có cấu hình dữ liệu quyền thì tại đây cần xử lý riêngformSchema
  return [
    {
      field: 'id',
      label: 'id',
      component: 'Input',
      show: false,
    },
    {
      field: 'msgCategory',
      label: 'Loại tin nhắn',
      required: true,
      component: 'JDictSelectTag',
      defaultValue: '1',
      componentProps: {
        type: 'radio',
        dictCode: 'msg_category',
        placeholder: 'Vui lòng chọn loại',
      },
    },
    {
      field: 'izTop',
      label: 'Có ghim lên đầu không',
      defaultValue: '0',
      component: 'JSwitch',
      componentProps: {
        //Lấy giá trị options
        options: ['1', '0'],
        //Văn bảnoption
        labelOptions: ['Có', 'Không'],
        placeholder: 'Có ghim lên đầu không',
        checkedChildren: 'Có',
        unCheckedChildren: 'Không',
      },
    },
    {
      field: 'titile',
      label: 'Tiêu đề thông cáo',
      component: 'Input',
      required: true,
      componentProps: {
        placeholder: 'Vui lòng nhập tiêu đề',
      },
      // Giải thích logic mã: 【TV360X-1632】Tiêu đề quá dài gây lỗi khi lưu, kiểm tra độ dài
      dynamicRules() {
        return [
          {
            validator: (_, value) => {
              return new Promise<void>((resolve, reject) => {
                if (value.length > 100) {
                  reject('Tối đa 100 ký tự');
                }
                resolve();
              });
            },
          },
        ];
      },
    },
    {
      field: 'msgAbstract',
      label: 'Tóm tắt thông cáo',
      component: 'InputTextArea',
      required: true,
    },
    {
      field: 'msgType',
      label: 'Người dùng tiếp nhận',
      defaultValue: 'ALL',
      component: 'JDictSelectTag',
      required: true,
      componentProps: {
        type: 'radio',
        dictCode: 'msg_type',
        placeholder: 'Vui lòng chọn phạm vi phát hành',
      },
    },
    {
      field: 'userIds',
      label: 'Người dùng chỉ định',
      component: 'JSelectUserByDepartment',
      required: true,
      componentProps: {
        rowKey: 'id',
        // Giải thích logic mã: 【TV360X-1627】Component chọn người dùng trong thông báo chưa được dịch
        labelKey: 'realname',
      },
      ifShow: ({ values }) => values.msgType == 'USER',
    },
    {
      field: 'msgClassify',
      label: 'Phân loại thông báo',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'notice_type',
        placeholder: 'Vui lòng chọn phân loại thông báo',
      },
    },
    {
      field: 'priority',
      label: 'Cấp độ ưu tiên',
      defaultValue: 'H',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'priority',
        type: 'radio',
        placeholder: 'Vui lòng chọn mức ưu tiên',
      },
    },
    {
      field: 'msgTemplate',
      label: 'Mẫu thông báo',
      component: 'Input',
      slot: 'msgTemplate',
    },
    {
      field: 'files',
      label: 'Tệp đính kèm thông cáo',
      component: 'JUpload',
      componentProps: {
        //Có hiển thị nút chọn không
        text: 'Tải tệp lên',
        //Số lượng tải lên tối đa
        maxCount: 2,
        //Có hiển thị nút tải xuống không
        download: true,
      },
    },
    {
      field: 'msgContent',
      label: 'Nội dung thông cáo',
      component: 'Input',
      colProps: { span: 24 },
      ifShow: ({}) => _formData.disabled == false,
      render: ({ model, field }) => {
        return h(Tinymce, {
          showImageUpload: false,
          disabled: _formData.disabled !== false,
          height: 300,
          value: model[field],
          onChange: (value: string) => {
            model[field] = value;
          },
        });
      },
    },
    {
      field: 'msgContent',
      label: 'Nội dung thông cáo',
      component: 'Input',
      colProps: { span: 24 },
      ifShow: ({}) => _formData.disabled !== false,
      slot: 'msgContent',
    },
  ];
}
