import { FormSchema } from '/@/components/Form';
import JCronValidator from '/@/components/Form/src/jeecg/components/JEasyCron/validator';
import { usePermission } from '/@/hooks/web/usePermission';

const { isDisabledAuth } = usePermission();
export const schemas: FormSchema[] = [
  {
    field: 'jdst',
    component: 'JDictSelectTag',
    label: 'Danh sách thả xuống giới tính',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dictCode: 'sex',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'jdst1',
    component: 'JDictSelectTag',
    label: 'Chọn giới tính',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dictCode: 'sex',
      type: 'radioButton',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'jdst2',
    component: 'JDictSelectTag',
    label: 'Dropdown bảng từ điển',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dictCode: 'sys_user,realname,id',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'jdst3',
    component: 'JDictSelectTag',
    label: 'Dropdown bảng từ điển(Có điều kiện)',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dictCode: "sys_user,realname,id,username!='admin' order by create_time",
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst3',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'jsst',
    component: 'JSearchSelect',
    label: 'Tìm kiếm từ điển(Đồng bộ)',
    colProps: { span: 12 },
    componentProps: {
      //dict: "sys_depart,depart_name,id",
      dictOptions: [
        {
          text: 'Tùy chọn 1',
          value: '1',
        },
        {
          text: 'Tùy chọn 2',
          value: '2',
        },
        {
          text: 'Tùy chọn 3',
          value: '3',
        },
      ],
    },
  },
  {
    field: 'jsst',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'jsst2',
    component: 'JSearchSelect',
    label: 'Tìm kiếm từ điển(Bất đồng bộ)',
    colProps: { span: 12 },
    componentProps: {
      dict: 'sys_depart,depart_name,id',
      pageSize: 6,
      async: true,
    },
  },
  {
    field: 'jsst2',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'xldx',
    component: 'JDictSelectTag',
    label: 'Dropdown từ điển chọn nhiều',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
      mode: 'multiple',
    },
  },
  {
    field: 'xldx',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'xldx2',
    component: 'JSelectMultiple',
    label: 'Dropdown từ điển chọn nhiều2',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
    },
  },
  {
    field: 'xldx2',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'dxxlk',
    component: 'JDictSelectTag',
    label: 'Dropdown từ điển chọn đơn',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
    },
  },
  {
    field: 'dxxlk',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    label: 'Dropdown có thể nhập',
    field: 'selectInput',
    component: 'JSelectInput',
    componentProps: {
      options: [
        { label: 'Tùy chọn 1', value: '1' },
        { label: 'Tùy chọn 2', value: '2' },
        { label: 'Tùy chọn 3', value: '3' },
      ],
    },
    colProps: { span: 12 },
  },
  {
    field: 'selectInput',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'depart3',
    component: 'JSelectDept',
    label: 'Chọn phòng ban — Giá trị tùy chỉnh',
    helpMessage: ['componentChế độ'],
    componentProps: { showButton: false, rowKey: 'orgCode', primaryKey: 'orgCode' },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart3',
    component: 'JEllipsis',
    label: 'Phòng ban đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'depart2',
    component: 'JSelectDept',
    label: 'Chọn phòng ban',
    helpMessage: ['componentChế độ'],
    componentProps: { showButton: false },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart2',
    component: 'JEllipsis',
    label: 'Phòng ban đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'depart4',
    component: 'JSelectDepartPost',
    label: 'Chọn vị trí công tác',
    helpMessage: ['componentChế độ'],
    componentProps: { showButton: false },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart4',
    component: 'JEllipsis',
    label: 'Chọn vị trí công tác',
    colProps: { span: 12 },
  },
  {
    field: 'user2',
    component: 'JSelectUser',
    label: 'Component chọn người dùng',
    helpMessage: ['componentChế độ'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'id',
      showSelected: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user2',
    component: 'JEllipsis',
    label: 'Người dùng đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'user3',
    component: 'JSelectUserByDept',
    label: 'Chọn người dùng theo phòng ban',
    helpMessage: ['componentChế độ'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user3',
    component: 'JEllipsis',
    label: 'Người dùng đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'userPost1',
    component: 'JSelectUserByDeptPost',
    label: 'Chọn người dùng theo vị trí phòng ban',
    helpMessage: ['componentChế độ'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'userPost1',
    component: 'JEllipsis',
    label: 'Người dùng đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'user4',
    component: 'JSelectUserByDepartment',
    label: 'Chọn người dùng theo phòng ban',
    helpMessage: ['componentChế độ'],
    defaultValue: '',
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user4',
    component: 'JEllipsis',
    label: 'Người dùng đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'role2',
    component: 'JSelectRole',
    label: 'Component chọn vai trò',
    helpMessage: ['componentChế độ'],
    colProps: {
      span: 12,
    },
  },
  {
    field: 'role2',
    component: 'JEllipsis',
    label: 'Vai trò đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'position2',
    component: 'JSelectPosition',
    label: 'Component chọn chức danh',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
    componentProps: { async: true, showSelectTable: true },
  },
  {
    field: 'position2',
    component: 'JEllipsis',
    label: 'Chức danh đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'checkbox1',
    component: 'JCheckbox',
    label: 'JCheckboxComponent1',
    helpMessage: ['componentChế độ'],
    defaultValue: '1,2',
    componentProps: {
      options: [
        { label: 'Nam', value: '1' },
        { label: 'Nữ', value: '2' },
      ],
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'checkbox1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'checkbox2',
    component: 'Input',
    label: 'JCheckboxComponent2',
    defaultValue: '1',
    helpMessage: ['Chế độ slot'],
    slot: 'JCheckbox',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'checkbox2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'data1',
    label: 'Chọn ngày',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data2',
    label: 'Chọn khoảng năm',
    component: 'RangePicker',
    componentProps: {
      picker: 'year',
      valueFormat: 'YYYY',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'hk',
    component: 'Input',
    label: 'Mã xác thực trượt',
    helpMessage: ['Chế độ slot'],
    slot: 'dargVerify',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'hk',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JTreeDict',
    component: 'JTreeDict',
    label: 'Từ điển cây',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
  },
  {
    field: 'JTreeDict',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts',
    component: 'JTreeSelect',
    label: 'Chọn cây thả xuống',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField: 'is_leaf',
      converIsLeafVal: 0,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'ts1',
    component: 'JTreeSelect',
    label: 'Chọn nhiều trên cây thả xuống',
    helpMessage: ['componentChế độ'],
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField: 'is_leaf',
      converIsLeafVal: 0,
      multiple: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'category',
    component: 'JCategorySelect',
    label: 'Cây từ điển phân loại',
    helpMessage: ['componentChế độ'],
    defaultValue: '',
    componentProps: {
      pcode: 'B01',
      multiple: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'category',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'JEasyCron',
    component: 'JEasyCron',
    label: 'JEasyCron',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
    defaultValue: '* * * * * ? *',
    rules: [{ validator: JCronValidator }],
  },
  {
    field: 'JEasyCron',
    component: 'JEllipsis',
    label: 'Chọn giá trị',
    colProps: { span: 12 },
  },
  {
    field: 'JInput',
    component: 'JInput',
    label: 'Component truy vấn đặc biệt',
    helpMessage: ['Chế độ slot'],
    slot: 'JInput',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jinputtype',
    component: 'Select',
    label: 'Loại tìm kiếm',
    componentProps: {
      options: [
        { value: 'like', label: 'Mờ (like)' },
        { value: 'ne', label: 'Không bằng (ne)' },
        { value: 'ge', label: 'Lớn hơn hoặc bằng (ge)' },
        { value: 'le', label: 'Nhỏ hơn hoặc bằng (le)' },
      ],
    },
    colProps: {
      span: 6,
    },
  },
  {
    field: 'JInput',
    component: 'JEllipsis',
    label: 'Giá trị nhập',
    colProps: { span: 6 },
  },
  {
    field: 'field1',
    component: 'Select',
    label: 'Chọn Tỉnh/Thành phố/Quận huyện',
    helpMessage: ['Chế độ slot'],
    slot: 'jAreaLinkage',
    colProps: {
      span: 12,
    },
    defaultValue: ['130000', '130200'],
  },
  {
    field: 'field1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field0',
    component: 'Select',
    label: 'Vô hiệu hóa component(Cách 1)',
    helpMessage: ['Chế độ slot'],
    slot: 'jAreaLinkage1',
    colProps: {
      span: 12,
    },
    defaultValue: ['130000', '130200'],
  },

  {
    field: 'field0',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field2',
    component: 'JAreaLinkage',
    label: 'Vô hiệu hóa component(Cách 2)',
    helpMessage: ['componentChế độ'],
    colProps: {
      span: 12,
    },
    dynamicDisabled: ({ values }) => {
      console.log(values);
      return isDisabledAuth(['demo.dbarray']);
    },
    defaultValue: ['140000', '140300', '140302'],
  },
  {
    field: 'field2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pca1',
    component: 'JAreaSelect',
    label: 'Liên cấp Tỉnh/Thành phố/Quận huyện',
    helpMessage: ['componentChế độ'],
    defaultValue: '140302',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pca1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop1',
    component: 'Input',
    label: 'JPopupVí dụ',
    helpMessage: ['Chế độ slot'],
    slot: 'JPopup',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop2',
    component: 'Input',
    label: 'JPopupVí dụ có tham số',
    helpMessage: ['Chế độ slot'],
    slot: 'JPopup2',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop3',
    component: 'Input',
    label: 'JPopupVí dụ JPopup có tham số điều kiện truy vấn',
    helpMessage: ['Chế độ slot'],
    slot: 'JPopup3',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop3',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JInputPop',
    component: 'JInputPop',
    label: 'JInputPop',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
  },
  {
    field: 'JInputPop',
    component: 'JEllipsis',
    label: 'Giá trị nhập',
    colProps: { span: 12 },
  },
  {
    field: 'JTreeDictAsync',
    component: 'JTreeDict',
    label: 'Bất đồng bộJTreeDict',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
    componentProps: { async: true },
  },
  {
    field: 'JTreeDictAsync',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'JSwitch',
    component: 'JSwitch',
    label: 'JSwitch',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
  },
  {
    field: 'JSwitch',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'JSwitchSelect',
    component: 'JSwitch',
    label: 'JSwitchSelect',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
    componentProps: { query: true },
  },
  {
    field: 'JSwitchSelect',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  
  {
    field: 'userSelect2',
    component: 'UserSelect',
    label: 'Chọn người dùng nâng cao',
    helpMessage: ['componentChế độ'],
    colProps: { span: 12 },
  },
  {
    field: 'userSelect2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  
  {
    field: 'superQuery',
    component: 'Input',
    label: 'Tìm kiếm nâng cao',
    helpMessage: ['Chế độ slot'],
    slot: 'superQuery',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery1',
    component: 'Input',
    label: 'Tìm kiếm nâng cao',
    helpMessage: ['Chế độ slot-Tự lưu điều kiện tìm kiếm'],
    slot: 'superQuery1',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'pop2',
    component: 'JPopupDict',
    label: 'JPopupDictVí dụ',
    colProps: {
      span: 12,
    },
    componentProps:{
      placeholder: 'Vui lòng chọn',
      dictCode: 'report_user,username,id',
      multi: true,
    },
  },
  {
    field: 'pop2',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'sex',
    component: 'JDictSelectTag',
    label: 'Giới tính(Kiểm soát khóa học bên dướioptions)',
    helpMessage: ['componentChế độ','Giới tính khác nhau thì các tùy chọn khóa học hiển thị bên dưới cũng khác nhau'],
    componentProps: {
      dictCode: 'sex',
      type: 'radioButton',
      onChange: (value) => {
        console.log(value);
      },
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'sex',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'course',
    component: 'Select',
    label: 'Khóa học',
    dynamicPropskey: 'options',
    dynamicPropsVal: ({ model }) => {
      let options;
      if (model.sex == 1) {
        return [
          { value: '0', label: 'java - Nam' },
          { value: '1', label: 'vue - Nam' },
        ];
      } else if (model.sex == 2) {
        return [
          { value: '2', label: 'Yoga - Nữ' },
          { value: '3', label: 'Làm móng - Nữ' },
        ];
      } else {
        return [];
      }
    },
    componentProps: {
      disabled: false,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'course',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'field100',
    component: 'JInputSelect',
    label: 'JInputSelect',
    helpMessage: ['componentChế độ'],
    componentProps: {
      selectPlaceholder: 'Có thể chọn biến hệ thống',
      inputPlaceholder: 'Vui lòng nhập',
      selectWidth:'200px',
      options: [
        {
          label: 'Tài khoản người dùng đã đăng nhập',
          value: '#{sys_user_code}',
        },
        {
          label: 'Tên người dùng đã đăng nhập',
          value: '#{sys_user_name}',
        },
        {
          label: 'Ngày hiện tại',
          value: '#{sys_date}',
        },
        {
          label: 'Thời gian hiện tại',
          value: '#{sys_time}',
        },
        {
          label: 'Phòng ban của người dùng đã đăng nhập',
          value: '#{sys_org_code}',
        },
        {
          label: 'Phòng ban mà người dùng sở hữu',
          value: '#{sys_multi_org_code}',
        },
        {
          label: 'Tenant của người dùng đã đăng nhập',
          value: '#{tenant_id}',
        },
      ],
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field100',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'JAreaLinkage',
    component: 'JAreaLinkage',
    label: 'Chọn Tỉnh/Thành phố/Quận huyện',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JAreaLinkage',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'tabsSelectUser',
    component: 'Input',
    label: 'Chọn người dùng',
    helpMessage: ['Chế độ slot-Tự lưu điều kiện tìm kiếm'],
    slot: 'tabsSelectUser',
    colProps: { span: 12 },
  },
  {
    field: 'tabsSelectUser',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
  {
    field: 'linkCard1',
    component: 'JLinkTableCard',
    label: 'Bản ghi liên kết(onlineChế độ)',
    helpMessage: ['componentChế độ', 'queryMode=online(Mặc định)', 'Chỉ hỗ trợ các bảng đã cấu hình trong biểu mẫu Online'],
    colProps: { span: 12 },
    componentProps: {
      queryMode: 'online',
      tableName: 'test_demo',
      valueField: 'id',
      textField: 'name,age,sex',
      multi: false,
    },
  },
  {
    field: 'linkCard1',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn(id)',
    colProps: { span: 12 },
  },
  {
    field: 'linkCard3',
    component: 'JLinkTableCard',
    label: 'Bản ghi liên kết(tableChế độ)',
    helpMessage: ['componentChế độ', 'queryMode=table + multi=true', 'Hỗ trợ tất cả các bảng trong cơ sở dữ liệu, đa chọn'],
    colProps: { span: 12 },
    componentProps: {
      queryMode: 'table',
      tableName: 'sys_user',
      valueField: 'id',
      textField: 'realname,username,sex',
      imageField: 'avatar',
      multi: true,
    },
  },
  {
    field: 'linkCard3',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn(idphân tách bằng dấu phẩy)',
    colProps: { span: 12 },
  },
  {
    field: 'orderAuth',
    component: 'Input',
    label: 'Quyền hạn chỉ thị',
    helpMessage: ['Nếu có quyền, phía bên phải"Giá trị đã chọn"Hiển thị, nếu không sẽ không hiển thị'],
    colProps: {
      span: 12,
    },
  },
  {
    field: 'orderAuth',
    auth: 'demo:order:auth',
    component: 'JEllipsis',
    label: 'Giá trị đã chọn',
    colProps: { span: 12 },
  },
];
