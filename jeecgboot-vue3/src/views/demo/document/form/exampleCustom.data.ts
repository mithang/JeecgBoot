import { FormSchema } from '/@/components/Form';
import { defHttp } from '/@/utils/http/axios';

export const schemas: FormSchema[] = [
  {
    label: 'Mã xác thực',
    field: 'code',
    component: 'InputCountDown',
    componentProps: {
      //'default': Mặc định, 'large': Lớn nhất, 'small': Nhỏ nhất
      size:'default',
      //Đếm ngược
      count: 120,
    },
  },
  {
    label: 'ApiChọn trong danh sách thả xuống',
    field: 'apiSelect',
    component: 'ApiSelect',
    componentProps: {
      //multiple: Chọn nhiều; không điền là chọn một
      mode: 'multiple',
      //Yêu cầuapi,Trả về kết quả{ result: { records:[{'id':'1',name:'scott'},{'id':'2',name:'Tiểu Trương'}] }}
      api: () => defHttp.get({ url: '/test/jeecgDemo/list' }),
      //Chuyển giá trị thànhString
      numberToString: false,
      //Trường tiêu đề
      labelField: 'name',
      //Trường giá trị
      valueField: 'id',
      //Tham số yêu cầu
      params: {},
      //Trường kết quả trả về
      resultField: 'records',
    },
  },
  {
    label: 'ApiChọn cây',
    field: 'apiTreeSelect',
    component: 'ApiTreeSelect',
    componentProps: {
      /* Yêu cầuapi,Trả về kết quả
         { result: { list: [{ title:'Tùy chọn0',value:'0',key:'0',
           children: [ {"title": "Tùy chọn0-0","value": "0-0","key": "0-0"},...]
           }, ...]
         }} */
      api: () => defHttp.get({ url: '/mock/tree/getDemoOptions' }),
      //Tham số yêu cầu
      params: {},
      //Trường kết quả trả về
      resultField: 'list',
    },
  },
  {
    label: 'Kiểm chứng độ mạnh mật khẩu',
    field: 'pwd',
    component: 'StrengthMeter',
    componentProps: {
      //Có hiển thị hộp văn bản mật khẩu không
      showInput: true,
      //Có vô hiệu hóa hay không
      disabled: false,
    },
  },
  {
    label: 'Liên động Tỉnh/Thành phố/Huyện',
    field: 'province',
    component: 'JAreaLinkage',
    componentProps: {
      //Có hiển thị quận huyện không, mặc địnhtrue,Nếu không chỉ hiển thị tỉnh
      showArea: true,
      //Có phải là toàn bộ văn bản không, mặc địnhfalse
      showAll: true,
    },
  },
  {
    label: 'Chọn vị trí công tác',
    field: 'post',
    component: 'JSelectPosition',
    componentProps: {
      //Có hiển thị danh sách đã chọn ở bên phải không
      showSelected: true,
      //Số lượng chọn tối đa
      maxSelectCount: 1,
      //Tiêu đề vị trí công tác
      modalTitle: 'Vị trí công tác',
    },
  },
  {
    label: 'Chọn vai trò',
    field: 'role',
    component: 'JSelectRole',
    componentProps: {
      //Tham số yêu cầu, ví dụparams:{"code":"001"}
      params: {},
      //Có chọn một hay không,Mặc địnhfalse
      isRadioSelection: true,
      //Tiêu đề vai trò
      modalTitle: 'Vai trò',
    },
  },
  {
    label: 'Chọn người dùng',
    field: 'user',
    component: 'JSelectUser',
    componentProps: {
      //Cấu hình trường lấy giá trị,Thường là trường khóa chính
      rowKey: 'username',
      //Hiển thị cấu hình trường
      labelKey: 'realname',
      //Có hiển thị nút chọn không
      showButton: false,
      //Tiêu đề người dùng
      modalTitle: 'Người dùng',
    },
  },
  {
    label: 'Tải lên hình ảnh',
    field: 'uploadImage',
    component: 'JImageUpload',
    componentProps: {
      //Văn bản hiển thị của nút
      text:'Tải lên hình ảnh',
      //Hỗ trợ hai kiểu cơ bản picture vàpicture-card
      listType:'picture-card',
      //Dùng để kiểm soát đường dẫn nghiệp vụ tải lên tệp,Mặc địnhtemp
      bizPath:'temp',
      //Có vô hiệu hóa hay không
      disabled:false,
      //Số lượng tải lên tối đa
      fileMax:1,
    },
  },
  {
    label: 'Nhãn từ điển',
    field: 'dictTags',
    component: 'JDictSelectTag',
    componentProps: {
      //Cấu hình code danh mục, ví dụ qua mã danh mục giới tính: sex, cũng có thể dùngdemo,name,id Tên bảng,Tên,dạng giá trị
      dictCode:'sex',
      //Hỗ trợradio(Nút radio)、radioButton(Nút radio kiểu btn)、select(Dropdown)
      type:'radioButton'
    },
  },
  {
    label: 'Chọn phòng ban',
    field: 'dept',
    component: 'JSelectDept',
    componentProps: {
      //Có bật tải bất đồng bộ không
      sync: false,
      //Có hiển thị hộp kiểm không
      checkable: true,
      //Có hiển thị nút chọn không
      showButton: false,
      //Trạng thái chọn của nút cha và nút con không còn liên kết với nhau
      checkStrictly: true,
      //Tiêu đề hộp chọn
      modalTitle: 'Chọn phòng ban',
    },
  },
  {
    label: 'Liên động cấp tỉnh/thành/huyện',
    field: 'provinceArea',
    component: 'JAreaSelect',
    componentProps: {
      //Cấp 1 chỉ hiển thị tỉnh, 2 tỉnh/thành phố, 3 tỉnh/thành phố/quận huyện
      level:3
    },
  },
  {
    label: 'Văn bản phong phú',
    field: 'editor',
    component: 'JEditor',
    componentProps: {
      //Có vô hiệu hóa hay không
      disabled: false
    },
  },
  {
    label: 'markdown',
    field: 'markdown',
    component: 'JMarkdownEditor',
    componentProps: {
      //Có vô hiệu hóa hay không
      disabled: false
    },
  },
  {
    label: 'Hộp dropdown có thể nhập',
    field: 'inputSelect',
    component: 'JSelectInput',
    componentProps: {
      options: [
        { label: 'Default', value: 'default' },
        { label: 'IFrame', value: 'iframe' },
      ],
      //Có phải chế độ tìm kiếm không
      showSearch: true,
      //Có vô hiệu hóa hay không
      disabled: false
    },
  },
  {
    label: 'Component trình chỉnh sửa mã',
    field: 'jCode',
    component: 'JCodeEditor',
    componentProps: {
      //Chiều cao, mặc địnhauto
      height:'150px',
      //Có vô hiệu hóa hay không
      disabled:false,
      //Có toàn màn hình hay không
      fullScreen:false,
      //Tọa độ sau khi toàn màn hình
      zIndex: 999,
      //Chủ đề của mã, hiện chỉ hỗ trợidea,Có thể tự mở rộng trong component
      theme:'idea',
      //Gợi ý mã
      keywords:['console'],
      //Ngôn ngữ như (javascript,vue,markdown) có thể tự mở rộng trong component
      language:'javascript'
    },
  },
  {
    label: 'Cây từ điển phân loại',
    field: 'dictTree',
    component: 'JCategorySelect',
    componentProps: {
      //Nội dung giữ chỗ
      placeholder:'Vui lòng chọn cây danh mục phân loại',
      //Điều kiện tìm kiếm, ví dụ "{'name':'Laptop'}”
      condition:"",
      //Có chọn nhiều hay không
      multiple: false,
      //code lựa chọn ban đầu, xem mã loại của danh mục phân loại đã cấu hình
      pcode: 'A04',
      //Cấp chaid
      pid:'0',
      //Trả vềkey
      back:'id',
    },
  },
  {
    label: 'Danh sách thả xuống chọn nhiều',
    field: 'selectMultiple',
    component: 'JSelectMultiple',
    componentProps: {
      //Cấu hình code danh mục, ví dụ qua mã danh mục giới tính: sex, cũng có thể dùngdemo,name,id Tên bảng,Tên,dạng giá trị
      dictCode:'company_rank',
      //Có chỉ đọc hay không
      readOnly:false,
    },
  },
  {
    label: 'popup',
    field: 'popup',
    component: 'JPopup',
    componentProps: ({ formActionType }) => {
      const {setFieldsValue} = formActionType;
      return{
        setFieldsValue:setFieldsValue,
        //onlineMã báo cáo
        code:"demo",
        //Có phải chọn nhiều không
        multi:false,
        //Cấu hình trường
        fieldConfig: [
          { source: 'name', target: 'popup' },
        ],
      }
    },
  },
  {
    label: 'Tùy chỉnh công tắc',
    field: 'switch',
    component: 'JSwitch',
    componentProps:{
      //Lấy giá trị options
      options:['Y','N'],
      //Văn bảnoption
      labelOptions:['Có', 'Không'],
      //Có bật danh sách thả xuống không
      query: false,
      //Có vô hiệu hóa hay không
      disabled: false,
    },
  },
  {
    label: 'Chọn biểu thức hẹn giờ',
    field: 'timing',
    component: 'JEasyCron',
    componentProps:{
      //Có ẩn cài đặt tham số giây và năm hay không, nếu ẩn thì tham số giây và năm sẽ bị bỏ qua hoàn toàn.
      hideSecond: false,
      //Có ẩn cài đặt năm tham số hay không, nếu ẩn thì năm tham số sẽ bị bỏ qua hoàn toàn
      hideYear: false,
      //Có vô hiệu hóa hay không
      disabled: false,
      //Hàm lấy danh sách thời gian thực thi xem trước, định dạng là:remote (cronGiá trị, timeDấu thời gian, cbHàm callback)
      remote:(cron,time,cb)=>{}
    },
  },
  {
    label: 'Cây từ điển phân loại',
    field: 'treeDict',
    component: 'JTreeDict',
    componentProps:{
      //Chỉ định trường mà component hiện tại cần lưu trữ, tùy chọn: id(Khóa chính)vàcode(Mã)
      field:'id',
      //Có phải bất đồng bộ không
      async: true,
      //Có vô hiệu hóa hay không
      disabled: false,
      //Chỉ định mã của một nút,Tải tất cả dữ liệu từ điển dưới nút đó,Nếu không chỉ định, mặc định tải tất cả dữ liệu
      parentCode:'A04'
    },
  },
  {
    label: 'Cửa sổ nhập nhiều dòng',
    field: 'inputPop',
    component: 'JInputPop',
    componentProps:{
      //Tiêu đề
      title:'Cửa sổ nhập nhiều dòng',
      //Vị trí hiển thị popup
      position:'bottom',
    },
  },
  {
    label: 'Chọn nhiều',
    field: 'multipleChoice',
    component: 'JCheckbox',
    componentProps:{
      //Cấu hình mã danh mục (dict code), ví dụ qua mã danh mục chức danh:company_rank, cũng có thể sử dụngdemo,name,id Tên bảng,Tên,dạng giá trị
      dictCode:'company_rank',
      //Có vô hiệu hóa hay không
      disabled: false,
      //Không có mã danh mục (dict code) có thể dùng option để định nghĩa
      // options:[
      //   {label:'CE0',value:'1'}
      // ]
    },
  },
  {
    label: 'Chọn cây thả xuống',
    field: 'treeCusSelect',
    component: 'JTreeSelect',
    componentProps: {
      //Cấu hình code danh mục, ví dụ qua mã danh mục giới tính: sex, cũng có thể dùngsys_permission,name,id Tên bảng,Tên,dạng giá trị
      dict: 'sys_permission,name,id',
      //Trường id cấp cha
      pidField: 'parent_id',
    },
  },
  {
    label: 'Component chọn người dùng theo phòng ban',
    field: 'userByDept',
    component: 'JSelectUserByDept',
    componentProps: {
      //Có hiển thị nút chọn không
      showButton: true,
      //Tiêu đề hộp chọn
      modalTitle: 'Chọn người dùng theo phòng ban'
    },
  },
  {
    label: 'Tải lên tệp',
    field: 'uploadFile',
    component: 'JUpload',
    componentProps: {
      //Có hiển thị nút chọn không
      text: 'Tải lên tệp',
      //Số lượng tải lên tối đa
      maxCount: 2,
      //Có hiển thị nút tải xuống không
      download: true,
    },
  },
  {
    label: 'Tìm kiếm bảng từ điển',
    field: 'dictSearchSelect',
    component: 'JSearchSelect',
    componentProps: {
      //Cấu hình code từ điển, thông qua demo,name,id Tên bảng,Tên,dạng giá trị
      dict: 'demo,name,id',
      //Có tải bất đồng bộ không
      async: true,
      //Có hiệu lực khi async được đặt thành true, nghĩa là khi tìm kiếm bất đồng bộ, số lượng dữ liệu lấy mỗi lần, mặc định10
      pageSize:3
    },
  },
  {
    label: 'Tạo động ô input',
    field: 'jAddInput',
    component: 'JAddInput',
    componentProps: {
      //Tùy chỉnh vượt quá bao nhiêu dòng mới hiển thị nút xóa, mặc định là1
      min:1
    },
  },
  {
    label: 'Component chọn người dùng',
    field: 'userCusSelect',
    component: 'UserSelect',
    componentProps: {
      //Có chọn nhiều hay không
      multi: true,
      //Chọn một cột từ bảng người dùng, giá trị của cột đó làm giá trị lưu trữ của control này, mặc định là cột id
      store: 'id',
      //Có loại trừ chính tôi không(Người dùng đang đăng nhập)
      izExcludeMy: false,
      //Có vô hiệu hóa hay không
      disabled: false,
    },
  },  
  {
    label: 'Component chọn vai trò',
    field: 'roleSelect',
    component: 'RoleSelect',
    componentProps: {
      //Số lượng chọn tối đa  
      maxSelectCount: 4,
      //Có chọn một hay không
      multi: true
    },
  },  
  {
    label: 'Ô nhập phạm vi số',
    field: 'rangeNumber',
    component: 'JRangeNumber',
  }, 
  {
    label: 'Nhóm radio Api từ xa',
    field: 'apiRadioGroup',
    component: 'ApiRadioGroup',
    componentProps:{
      //Kết quả trả về của API yêu cầu{ result:{ list: [ name: 'Tùy chọn0',id: '1' ] }}
      api:()=> defHttp.get({ url: '/mock/select/getDemoOptions' }),
      //Tham số yêu cầu
      params:{},
      //Có phải loại kiểu nút hay không, mặc địnhfalse
      isBtn: false,
      //Tên tập hợp trả về
      resultField: 'list',
      //Tên trường tiêu đề
      labelField: 'name',
      //Tên trường giá trị
      valueField: 'id',
    }
  },
];
