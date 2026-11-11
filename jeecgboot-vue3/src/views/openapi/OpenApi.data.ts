import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import {JVxeTypes,JVxeColumn} from '/@/components/jeecg/JVxeTable/types'
//Dữ liệu danh sách
export const columns: BasicColumn[] = [
   {
    title: 'Tên API',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: 'Địa chỉ API',
    align:"center",
    dataIndex: 'requestUrl',
    width: 120,
   },
   {
    title: 'Phương thức yêu cầu',
    align:"center",
    dataIndex: 'requestMethod',
    width: 100,
   },
   {
    title: 'API gốc',
    align:"center",
    dataIndex: 'originUrl',
    ellipsis: true,
   },
   {
    title: 'IP Danh sách trắng',
    align:"center",
    dataIndex: 'whiteList',
    ellipsis: true,
    customRender: ({ text }) => {
      if (!text) return 'Không giới hạn';
      const count = text.split(/[,\n]/).filter(item => item.trim()).length;
      return count + ' quy tắc';
    }
   },
];
//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: "Tên API",
    field: "name",
    component: 'JInput',
  },
  {
    label: "Địa chỉ API",
    field: "requestUrl",
    component: 'JInput',
  },
];
// Tùy chọn phương thức yêu cầu (cho biểu mẫu slot Tái sử dụng)
export const requestMethodOptions = [
  { label: 'POST', value: 'POST' },
  { label: 'GET', value: 'GET' },
  { label: 'HEAD', value: 'HEAD' },
  { label: 'PUT', value: 'PUT' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'OPTIONS', value: 'OPTIONS' },
  { label: 'TRACE', value: 'TRACE' },
];

//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: 'Tên API',
    field: 'name',
    component: 'Input',
    dynamicRules: () => {
          return [
                 { required: true, message: 'Vui lòng nhập tên API!'},
          ];
     },
  },
  {
    label: 'Địa chỉ API',
    field: 'requestUrl',
    component: 'Input',
    slot: 'requestUrlSlot',
    dynamicDisabled: true,
  },
  {
    label: 'Phương thức yêu cầu',
    field: 'requestMethod',
    defaultValue:'GET',
    component: 'Input',
    show: false,
  },
  {
    label: 'API gốc',
    field: 'originUrl',
    component: 'Input',
    componentProps: {
      placeholder: 'Địa chỉ API gốc của hệ thống hiện tại, ví dụ /sys/user/list',
    },
    helpMessage: 'Đường dẫn API gốc được proxy trong hệ thống hiện tại',
    colProps: { span: 24 },
    dynamicRules: () => {
      return [
        { required: true, message: 'Vui lòng nhập đường dẫn API gốc!' },
        {
          validator: (_, value) => {
            if (value && !value.startsWith('/')) {
              return Promise.reject('Đường dẫn API gốc phải bắt đầu bằng /');
            }
            if (value && value.includes('//')) {
              return Promise.reject('Đường dẫn API gốc không được chứa //');
            }
            if (value && value.includes('..')) {
              return Promise.reject('Đường dẫn API gốc không được chứa ..');
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },

  {
    label: 'IP Danh sách trắng',
    field: 'whiteList',
    helpMessage: 'Hỗ trợ IP chính xác, dải mạng CIDR (ví dụ: 192.168.1.0/24), ký tự đại diện (ví dụ: 10.2.3.*), mỗi dòng một địa chỉ hoặc phân tách bằng dấu phẩy, để trống thì không giới hạn',
    component: 'InputTextArea',
    slot: 'whiteListSlot',
    componentProps: {
      rows: 5,
      placeholder: 'Ví dụ:\n192.168.1.100\n10.0.0.0/8\n172.16.*.*',
    },
    colProps: { span: 24 },
  },
  {
    label: 'Ghi chú',
    field: 'remarks',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      placeholder: 'Vui lòng nhập ghi chú danh sách trắng',
    },
    colProps: { span: 24 },
  },
  {
    label: 'Mô tả API',
    field: 'description',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
      placeholder: 'Vui lòng nhập mô tả API',
    },
    colProps: { span: 24 },
  },
  {
    label: 'Cờ xóa',
    field: 'delFlag',
    component: 'Input',
    defaultValue:0,
    show:false
  },
  {
    label: 'Trạng thái',
    field: 'status',
    component: 'Input',
    defaultValue:"1",
    show:false
  },
	// TODO Trường khóa chính ẩn, hiện đang cố định là ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];
//Dữ liệu biểu mẫu con
//Dữ liệu danh sách bảng con
export const openApiHeaderColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: 'Key header yêu cầu',
    align:"center",
    dataIndex: 'headerKey'
   },
   {
    title: 'Có bắt buộc không',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: 'Giá trị mặc định',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: 'Ghi chú',
    align:"center",
    dataIndex: 'note'
   },
];
//Dữ liệu danh sách bảng con
export const openApiParamColumns: BasicColumn[] = [
   // {
   //  title: 'apiId',
   //  align:"center",
   //  dataIndex: 'apiId'
   // },
   {
    title: 'Key tham số',
    align:"center",
    dataIndex: 'paramKey'
   },
   {
    title: 'Có bắt buộc không',
    align:"center",
    dataIndex: 'required_dictText'
   },
   {
    title: 'Giá trị mặc định',
    align:"center",
    dataIndex: 'defaultValue'
   },
   {
    title: 'Ghi chú',
    align:"center",
    dataIndex: 'note'
   },
];
//Cấu hình bảng của bảng con
// update-begin--author:liaozhiyang---date:20260804---for: [LHZP-1266] Các request header mặc định appKey/signature/timestamp bị chặn kiểm tra khi xóa và bị vô hiệu hóa bắt buộc
export const FIXED_HEADER_KEYS = ['appKey', 'signature', 'timestamp'];
// update-end--author:liaozhiyang---date:20260804---for: [LHZP-1266] Các request header mặc định appKey/signature/timestamp bị chặn kiểm tra khi xóa và bị vô hiệu hóa bắt buộc
export const openApiHeaderJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: 'Vui lòng nhập${title}',
    //   defaultValue:'',
    // },
    {
      title: 'Key header yêu cầu',
      key: 'headerKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
    {
      title: 'Có bắt buộc không',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
      customValue: ['1','0'],
      // update-begin--author:liaozhiyang---date:20260804---for: [LHZP-1266] Các request header mặc định appKey/signature/timestamp bị chặn kiểm tra khi xóa và bị vô hiệu hóa bắt buộc
      props: {
        isDisabledCell: ({ row }) => FIXED_HEADER_KEYS.includes(row.headerKey),
      },
      // update-end--author:liaozhiyang---date:20260804---for: [LHZP-1266] Các request header mặc định appKey/signature/timestamp bị chặn kiểm tra khi xóa và bị vô hiệu hóa bắt buộc
    },
    {
      title: 'Loại tham số',
      key: 'paramType',
      type: JVxeTypes.select,
      width: '120px',
      options: [
        { title: 'string', value: 'string' },
        { title: 'integer', value: 'integer' },
        { title: 'number', value: 'number' },
        { title: 'boolean', value: 'boolean' },
        { title: 'array', value: 'array' },
        { title: 'object', value: 'object' },
      ],
      defaultValue: 'string',
    },
    {
      title: 'Giá trị mặc định',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
    {
      title: 'Giá trị ví dụ',
      key: 'example',
      type: JVxeTypes.input,
      width: '200px',
      placeholder: 'Vui lòng nhập${title}',
      defaultValue: '',
    },
    {
      title: 'Ghi chú',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
  ]
export const openApiParamJVxeColumns: JVxeColumn[] = [
    // {
    //   title: 'apiId',
    //   key: 'apiId',
    //   type: JVxeTypes.input,
    //   width:"200px",
    //   placeholder: 'Vui lòng nhập${title}',
    //   defaultValue:'',
    // },
    {
      title: 'Key tham số',
      key: 'paramKey',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
    {
      title: 'Có bắt buộc không',
      key: 'required',
      type: JVxeTypes.checkbox,
      options:[],
      // dictCode:"yn",
      width:"100px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
      customValue: ['1','0']
    },
    {
      title: 'Loại tham số',
      key: 'paramType',
      type: JVxeTypes.select,
      width: '120px',
      options: [
        { title: 'string', value: 'string' },
        { title: 'integer', value: 'integer' },
        { title: 'number', value: 'number' },
        { title: 'boolean', value: 'boolean' },
        { title: 'array', value: 'array' },
        { title: 'object', value: 'object' },
      ],
      defaultValue: 'string',
    },
    {
      title: 'Giá trị mặc định',
      key: 'defaultValue',
      type: JVxeTypes.input,
      width:"200px",
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
    {
      title: 'Giá trị ví dụ',
      key: 'example',
      type: JVxeTypes.input,
      width: '200px',
      placeholder: 'Vui lòng nhập${title}',
      defaultValue: '',
    },
    {
      title: 'Ghi chú',
      key: 'note',
      type: JVxeTypes.input,
      placeholder: 'Vui lòng nhập${title}',
      defaultValue:'',
    },
  ]

export const responseFieldJVxeColumns: JVxeColumn[] = [
  {
    title: 'Tên trường',
    key: 'fieldName',
    type: JVxeTypes.input,
    width: '200px',
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
  {
    title: 'Loại',
    key: 'fieldType',
    type: JVxeTypes.select,
    width: '120px',
    options: [
      { title: 'string', value: 'string' },
      { title: 'integer', value: 'integer' },
      { title: 'number', value: 'number' },
      { title: 'boolean', value: 'boolean' },
      { title: 'array', value: 'array' },
      { title: 'object', value: 'object' },
    ],
    defaultValue: 'string',
  },
  {
    title: 'Giải thích',
    key: 'fieldDesc',
    type: JVxeTypes.input,
    placeholder: 'Vui lòng nhập${title}',
    defaultValue: '',
  },
];

// Dữ liệu tìm kiếm nâng cao
export const superQuerySchema = {
  name: {title: 'Tên API',order: 0,view: 'text', type: 'string',},
  requestMethod: {title: 'Phương thức yêu cầu',order: 1,view: 'list', type: 'string',dictCode: '',},
  requestUrl: {title: 'Địa chỉ API',order: 2,view: 'text', type: 'string',},
  whiteList: {title: 'IP Danh sách trắng',order: 3,view: 'text', type: 'string',},
  status: {title: 'Trạng thái',order: 5,view: 'number', type: 'number',},
  createBy: {title: 'Người tạo',order: 6,view: 'text', type: 'string',},
  createTime: {title: 'Thời gian tạo',order: 7,view: 'datetime', type: 'string',},
  //Truy vấn nâng cao bảng con
  openApiHeader: {
    title: 'Bảng header yêu cầu',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        headerKey: {title: 'Key header yêu cầu',order: 1,view: 'text', type: 'string',},
        required: {title: 'Có bắt buộc không',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: 'Giá trị mặc định',order: 3,view: 'text', type: 'string',},
        note: {title: 'Ghi chú',order: 4,view: 'text', type: 'string',},
    }
  },
  openApiParam: {
    title: 'Phần tham số yêu cầu',
    view: 'table',
    fields: {
        // apiId: {title: 'apiId',order: 0,view: 'text', type: 'string',},
        paramKey: {title: 'Key tham số',order: 1,view: 'text', type: 'string',},
        required: {title: 'Có bắt buộc không',order: 2,view: 'number', type: 'number',dictCode: 'yn',},
        defaultValue: {title: 'Giá trị mặc định',order: 3,view: 'text', type: 'string',},
        note: {title: 'Ghi chú',order: 4,view: 'text', type: 'string',},
    }
  },
};

/**
* Biểu mẫu quy trình gọi phương thức này để lấy formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // Mặc định giữ nhất quán với biểu mẫu gốc. Nếu trong quy trình đã cấu hình dữ liệu quyền, ở đây cần xử lý riêng formSchema
  return formSchema;
}
