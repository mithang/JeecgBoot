import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getAllRolesListNoByTenant, getDepPostIdByDepId } from './user.api';
import { rules, createPasswordValidator } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getDepartPathNameByOrgCode, getDepartName, getMultiDepartPathName, getDepartPathName } from '@/utils/common/compUtils';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
export const columns: BasicColumn[] = [
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
    width: 120,
    resizable: true,
  },
  {
    title: 'Họ tên người dùng',
    dataIndex: 'realname',
    width: 100,
    resizable: true,
  },
/*  {
    title: 'Ảnh đại diện',
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },*/
  {
    title: 'Giới tính',
    dataIndex: 'sex',
    width: 80,
    resizable: true,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
/*  {
    title: 'Ngày sinh',
    dataIndex: 'birthday',
    width: 100,
  },*/
  {
    title: 'Số điện thoại',
    dataIndex: 'phone',
    width: 100,
    resizable: true,
    customRender:( { record, text })=>{
      if(record.izHideContact && record.izHideContact === '1'){
        return '/';
      }
      return text;
    }
  },
  {
    title: 'Phòng ban',
    width: 150,
    resizable: true,
    dataIndex: 'belongDepIds',
    customRender:( { record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getMultiDepartPathName(record.orgCodeTxt,text));
    }
  },
  {
    title: 'Phòng ban phụ trách',
    width: 150,
    resizable: true,
    dataIndex: 'departIds',
    customRender:( { record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getMultiDepartPathName(record.departIds_dictText,text));
    }
  },
  {
    title: 'Vị trí chính',
    width: 150,
    resizable: true,
    dataIndex: 'mainDepPostId',
    customRender: ({ record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getDepartPathName(record.mainDepPostId_dictText,text,false));
    }
  },
  {
    title: 'Vị trí kiêm nhiệm',
    width: 150,
    resizable: true,
    dataIndex: 'otherDepPostId',
    customRender:({ record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getMultiDepartPathName(record.otherDepPostId_dictText,text));
    }
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status_dictText',
    width: 80,
    resizable: true,
  },
];

export const recycleColumns: BasicColumn[] = [
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
    width: 100,
  },
  {
    title: 'Họ tên người dùng',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'Ảnh đại diện',
    dataIndex: 'avatar',
    width: 80,
    customRender: render.renderAvatar,
  },
  {
    title: 'Giới tính',
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tài khoản',
    field: 'username',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: 'Tên',
    field: 'realname',
    component: 'JInput',
   //colProps: { span: 6 },
  },
  {
    label: 'Giới tính',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
  {
    label: 'Số điện thoại',
    field: 'phone',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: 'Trạng thái người dùng',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'user_status',
      placeholder: 'Vui lòng chọn trạng thái',
      stringToNumber: true,
    },
   //colProps: { span: 6 },
  },
  {
    label: 'Phòng ban trực thuộc',
    field: 'departId',
    component: 'JSelectDept',
    componentProps: {
      placeholder: 'Vui lòng chọn phòng ban trực thuộc',
      showButton: false,
      checkStrictly: true
    },
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
    label: 'Tài khoản người dùng',
    field: 'username',
    component: 'Input',
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'username', model, schema, true),
  },
  {
    label: 'Mật khẩu đăng nhập',
    field: 'password',
    component: 'StrengthMeter',
    componentProps:{
      autocomplete: 'new-password',
    },
    rules: [
      {
        required: true,
        message: 'Vui lòng nhập mật khẩu đăng nhập',
      },
      //update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Chuyển đổi linh hoạt quy tắc kiểm tra theo trạng thái bật/tắt của bảo mật cấp ba-----------
      {
        validator: createPasswordValidator(),
      },
      //update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Chuyển đổi linh hoạt quy tắc kiểm tra theo trạng thái bật/tắt của bảo mật cấp ba-----------
    ],
  },
  {
    label: 'Xác nhận mật khẩu',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
  {
    label: 'Họ tên người dùng',
    field: 'realname',
    required: true,
    component: 'Input',
  },
  {
    label: 'Mã nhân viên',
    field: 'workNo',
    required: false,
    component: 'Input',
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'work_no', model, schema, false),
  },
/*  {
    label: 'Chức danh',
    field: 'post',
    required: false,
    component: 'JSelectPosition',
    componentProps: {
      labelKey: 'name',
    },
  },*/
  {
    label: 'Chức danh',
    field: 'positionType',
    required: false,
    component: 'JDictSelectTag',
    helpMessage: 'Chức danh này là mục từ điển, không phải cấp bậc chức danh, không thể dùng cho phê duyệt quy trình',
    componentProps: {
      dictCode: "user_position",
      mode: 'multiple',
    },
  },
  {
    label: 'Vai trò',
    field: 'selectedroles',
    component: 'ApiSelect',
    componentProps: {
      mode: 'multiple',
      api: getAllRolesListNoByTenant,
      labelField: 'roleName',
      valueField: 'id',
      immediate: false,
    },
  },
  {
    label: 'Phòng ban trực thuộc',
    field: 'selecteddeparts',
    component: 'JSelectDept',
    componentProps: ({ formActionType, formModel }) => {
      return {
        sync: false,
        checkStrictly: true,
        defaultExpandLevel: 2,

        onSelect: (options, values) => {
          const { updateSchema } = formActionType;
          const departIds = values ? values.value.join(',') : '';
          //Sau khi sửa phòng ban trực thuộc, cập nhật dữ liệu hộp thả xuống phòng ban phụ trách
          updateSchema([
            //Sửa tham số của vị trí chính và vị trí kiêm nhiệm
            {
              field: 'mainDepPostId',
              //update-begin---wangshuai---date:20260803  for：[LHZP-742]Vị trí của người dùng hệ thống: sau khi chọn xong phòng ban, vị trí chính và vị trí kiêm nhiệm không được làm mới theo thời gian thực, phải lưu lại mới có tác dụng------------
              componentProps: { key: departIds, params: { departIds } },
              //update-end---author:wangshuai ---date:20260803  for：[LHZP-742]Vị trí của người dùng hệ thống: sau khi chọn xong phòng ban, vị trí chính và vị trí kiêm nhiệm không được làm mới theo thời gian thực, phải lưu lại mới có tác dụng------------
            },
            {
              field: 'otherDepPostId',
              //update-begin---wangshuai---date:20260803  for：[LHZP-742]Vị trí của người dùng hệ thống: sau khi chọn xong phòng ban, vị trí chính và vị trí kiêm nhiệm không được làm mới theo thời gian thực, phải lưu lại mới có tác dụng------------
              componentProps: { key: departIds, params: { departIds } },
              //update-end---wangshuai---date:20260803  for：[LHZP-742]Vị trí của người dùng hệ thống: sau khi chọn xong phòng ban, vị trí chính và vị trí kiêm nhiệm không được làm mới theo thời gian thực, phải lưu lại mới có tác dụng------------
            }
          ]);
          //Cập nhật của phòng ban phụ tráchoption
          updateDepartOption(options, updateSchema);
          if(!values){
            formModel.departIds = [];
            formModel.mainDepPostId = "";
            formModel.otherDepPostId = "";
            return;
          }
          //Sau khi sửa phòng ban trực thuộc, cập nhật dữ liệu phòng ban phụ trách
          formModel.departIds && (formModel.departIds = formModel.departIds.filter((item) => values.value.indexOf(item) > -1));
        },
        onChange: async (values) => {
          // Khi phòng ban trực thuộc thay đổi, cần hủy giá trị đã chọn của vị trí chính và vị trí kiêm nhiệm
          await removeDepPostByDepId(formModel, values, formActionType);
        }
      };
    },
  },
  {
    label: 'Vị trí chính',
    field: 'mainDepPostId',
    component: 'JSelectDepartPost',
    componentProps: {
      rowKey: 'id',
      multiple: false,
      izShowDepPath: true,
    },
    ifShow:  ({ values }) => {
      if(!values.selecteddeparts){
        return false;
      }
      return !(values.selecteddeparts instanceof Array && values.selecteddeparts.length == 0);
    },
  },
  {
    label: 'Vị trí kiêm nhiệm',
    field: 'otherDepPostId',
    component: 'JSelectDepartPost',
    componentProps: {
      rowKey: 'id',
      izShowDepPath: true,
    },
    ifShow:  ({ values }) => {
      if(!values.selecteddeparts){
        return false;
      }
      return !(values.selecteddeparts instanceof Array && values.selecteddeparts.length == 0);
    },
  },
  {
    label: 'Đơn vị thuê (tenant)',
    field: 'relTenantIds',
    component: 'JSearchSelect',
    componentProps: {
      dict:"sys_tenant,name,id",
      async: true,
      multiple: true
    },
  },
  {
    label: 'Danh tính',
    field: 'userIdentity',
    component: 'RadioGroup',
    defaultValue: 1,
    componentProps: ({ formModel }) => {
      return {
        options: [
          { label: 'Người dùng thường', value: 1, key: '1' },
          { label: 'Cấp trên', value: 2, key: '2' },
        ],
        onChange: () => {
          formModel.userIdentity == 1 && (formModel.departIds = []);
        },
      };
    },
  },
  {
    label: 'Phòng ban phụ trách',
    field: 'departIds',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      tagRender: ({ label, value, closable, onClose }) => {
        // Tính toán văn bản hiển thị: dấu ba chấm phía trước + ký tự phía sau
        let displayLabel = label;
        if(displayLabel && label.length >= 20) {
          displayLabel = "..." + displayLabel.substring(label.length - 20);
        }
        return h(Tag, {
          style: {
            position: 'relative',
            boxSizing: 'border-box',
            height: '24px',
            marginTop: '2px',
            fontSize: '14px',
            marginBottom: '2px',
            lineHeight: '22px',
            background: 'rgba(51, 51, 51, 0.06)',
            border: '1px solid rgba(5, 5, 5, 0.06)',
            borderRadius: '4px',
            cursor: 'default'
          },
          title: label,
          closable,
          onClose:(e)=>{
            e.stopPropagation();
            onClose();
          }
        }, () => displayLabel);
      }
    },
    ifShow: ({ values }) => values.userIdentity == 2,
  },
  {
    label: 'Sắp xếp',
    field: 'sort',
    component: 'InputNumber',
    defaultValue: 1000,
    componentProps: {
      min: 1,
      max: 999999,
      step: 1,
      precision: 0
    }
  },
  {
    label: 'Ảnh đại diện',
    field: 'avatar',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 1,
    },
  },
  {
    label: 'Ngày sinh',
    field: 'birthday',
    component: 'DatePicker',
  },
  {
    label: 'Giới tính',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
      stringToNumber: true,
    },
  },
  {
    label: 'Email',
    field: 'email',
    component: 'Input',
    required: false,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, false)[0], trigger: 'blur' },
        { ...rules.rule('email', false)[0], trigger: 'blur' },
      ];
    },
  },
  {
    label: 'Số điện thoại',
    field: 'phone',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, true)[0], trigger: 'blur' },
        { pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại không đúng', trigger: 'blur' },
      ];
    },
  },
  {
    label: 'Điện thoại cố định',
    field: 'telephone',
    component: 'Input',
    rules: [{ pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: 'Vui lòng nhập số điện thoại cố định đúng' }],
  },
  {
    label: 'Động cơ quy trình làm việc (workflow)',
    field: 'activitiSync',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'activiti_sync',
      type: 'radio',
      stringToNumber: true,
    },
  },
  {
    label: 'Ẩn phương thức liên hệ',
    field: 'izHideContact',
    defaultValue: '0',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'yn',
      type: 'radio',
    },
  },
];

export const formPasswordSchema: FormSchema[] = [
  {
    label: 'Tài khoản người dùng',
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Mật khẩu đăng nhập',
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: 'Vui lòng nhập mật khẩu đăng nhập',
    },
    rules: [
      {
        required: true,
        message: 'Vui lòng nhập mật khẩu đăng nhập',
      },
      //update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Chuyển đổi linh hoạt quy tắc kiểm tra theo trạng thái bật/tắt của bảo mật cấp ba-----------
      {
        validator: createPasswordValidator(),
      },
      //update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Chuyển đổi linh hoạt quy tắc kiểm tra theo trạng thái bật/tắt của bảo mật cấp ba-----------
    ],
  },
  {
    label: 'Xác nhận mật khẩu',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];


//Danh sách người dùng đơn vị thuê
export const userTenantColumns: BasicColumn[] = [
  {
    title: 'Tài khoản người dùng',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: 'Họ tên người dùng',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'Ảnh đại diện',
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: 'Số điện thoại',
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: 'Phòng ban',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 80,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'Bình thường';
      } else if (text === '3') {
        return 'Đang phê duyệt';
      } else {
        return 'Đã từ chối';
      }
    },
  },
];

//Biểu mẫu tìm kiếm đơn vị thuê của người dùng
export const userTenantFormSchema: FormSchema[] = [
  {
    label: 'Tài khoản',
    field: 'username',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Tên',
    field: 'realname',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Giới tính',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn giới tính',
      stringToNumber: true,
    },
    colProps: { span: 6 },
  },
];


/**
 * Xóa dữ liệu không thuộc phòng ban hiện tại
 * Khi phòng ban trực thuộc thay đổi, hủy giá trị đã chọn của vị trí chính và vị trí kiêm nhiệm
 * 
 * @param formModel Mô hình biểu mẫu
 * @param values Giá trị phòng ban đã chọn
 * @param formActionType Phương thức thao tác biểu mẫu
 */
async function removeDepPostByDepId(formModel, values, formActionType) {
  const { setFieldsValue } = formActionType;
  if (values) {
    let departIds = "";
    if (values instanceof Array) {
      departIds = values.join(",");
    } else {
      departIds = values;
    }
    if (departIds) {
      try {
        // Truy vấn các vị trí thuộc phòng ban đang chọnID
        const { result } = await getDepPostIdByDepId({ depIds: departIds });
        const validPostIds = result.split(",") || [];
        
        // Kiểm tra vị trí chính có thuộc phòng ban hiện tại hay không
        if (formModel.mainDepPostId) {
          const mainPostId = Array.isArray(formModel.mainDepPostId) 
            ? formModel.mainDepPostId[0] 
            : formModel.mainDepPostId;
          
          if (mainPostId && !validPostIds.includes(mainPostId)) {
            // Vị trí chính không thuộc phòng ban hiện tại, xóa trống vị trí chính
            setFieldsValue({ mainDepPostId: null });
            formModel.mainDepPostId = null;
          }
        }

        // Kiểm tra vị trí kiêm nhiệm có thuộc phòng ban hiện tại hay không
        if(typeof formModel.otherDepPostId === "string"){
          formModel.otherDepPostId = formModel.otherDepPostId.split(",");
        }
        if (formModel.otherDepPostId && Array.isArray(formModel.otherDepPostId)) {
          const validOtherPosts = formModel.otherDepPostId.filter(postId => 
            validPostIds.includes(postId)
          );
          // Có vị trí kiêm nhiệm không thuộc phòng ban hiện tại, cập nhật vị trí kiêm nhiệm
          setFieldsValue({ otherDepPostId: validOtherPosts });
          formModel.otherDepPostId = validOtherPosts;
        }
      } catch (error) {
        console.error('Truy vấn vị trí phòng ban thất bại:', error);
        // Khi truy vấn thất bại, xóa mọi lựa chọn vị trí
        setFieldsValue({
          mainDepPostId: null,
          otherDepPostId: []
        });
        formModel.mainDepPostId = null;
        formModel.otherDepPostId = [];
      }
    } else {
      // Khi không chọn phòng ban, xóa mọi lựa chọn vị trí
      setFieldsValue({
        mainDepPostId: null,
        otherDepPostId: []
      });
      formModel.mainDepPostId = null;
      formModel.otherDepPostId = [];
    }
  }
}

/**
 * Cập nhật của phòng ban phụ tráchoptions
 *
 * @param options
 * @param updateSchema
 */
async function updateDepartOption(options, updateSchema) {
  if (options && options.length > 0) {
    // Xử lý song song mọi thao tác không đồng bộ
    const updatedOptions = await Promise.all(
      options.map(async (item) => {
        const departPathName = await getDepartPathNameByOrgCode('', item.label, item.value);
        return { ...item, label: departPathName };
      })
    );
    updateSchema([
      {
        field: 'departIds',
        componentProps: { options: updatedOptions },
      },
    ]);
  } else {
    updateSchema([
      {
        field: 'departIds',
        componentProps: { options: [] },
      },
    ]);
  }
}
