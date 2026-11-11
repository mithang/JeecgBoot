import { FormSchema } from '/@/components/Form';
import { getPositionByDepartId } from "./depart.api";
import { useMessage } from "@/hooks/web/useMessage";
import { BasicColumn } from "@/components/Table";
import {
  getDepartName,
  getDepartPathName,
  getDepartPathNameByOrgCode,
  getMultiDepartPathName
} from '@/utils/common/compUtils';
import { h, ref } from 'vue';

const { createMessage: $message } = useMessage();
//Tên phòng ban
const departNamePath = ref<Record<string, string>>({});

// Biểu mẫu cơ bản của phòng ban
export function useBasicFormSchema(treeData) {
  const basicFormSchema: FormSchema[] = [
    {
      field: 'departName',
      label: 'Tên cơ quan',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập tên cơ quan/phòng ban',
      },
      rules: [{ required: true, message: 'Tên cơ quan không được để trống' }],
    },
    {
      field: 'departNameAbbr',
      label: 'Tên viết tắt cơ quan',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập tên viết tắt của cơ quan/phòng ban',
      }
    },
    {
      field: 'parentId',
      label: 'Phòng ban cấp trên',
      component: 'TreeSelect',
      componentProps: {
        treeData: [],
        placeholder: 'Không có',
        treeCheckAble: true,
        multiple: true,
        dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
        tagRender: (options) => {
          const { value, label, option } = options;
          if (departNamePath.value[value]) {
            return h(
                'span', {  style: { marginLeft: '10px' } },
                departNamePath.value[value]
            );
          }
          getDepartPathNameByOrgCode('', label, option.id).then((data) => {
            departNamePath.value[value] = data;
          });
        },
      },
    },
    {
      field: 'orgCode',
      label: 'Mã cơ quan',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập mã cơ quan',
      },
    },
    {
      field: 'orgCategory',
      label: 'Loại cơ quan',
      component: 'RadioButtonGroup',
      componentProps: { options: [] },
    },
    {
      field: 'positionId',
      label: 'Cấp bậc chức danh',
      component: 'JDictSelectTag',
      componentProps: ({ formModel, formActionType }) => {
        return {
          dictCode: "sys_position,name,id,id is not null order by post_level asc",
          getPopupContainer: ()=> document.body,
          onChange: (value) => {
            formModel.depPostParentId = "";
            return positionChange(value, formModel, treeData);
          },
        }
      },
      ifShow:({ values })=>{
        return values.orgCategory === '3'
      },
      required: true,
    },
    {
      field: 'depPostParentId',
      label: 'Vị trí cấp trên',
      component: 'TreeSelect',
      ifShow:({ values })=>{
        return values.orgCategory === '3'
      },
      slot: 'depPostParentId',
    },
    {
      field: 'departOrder',
      label: 'Sắp xếp',
      component: 'InputNumber',
      componentProps: {},
    },
    {
      field: 'mobile',
      label: 'Điện thoại',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập điện thoại',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'fax',
      label: 'Fax',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập fax',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'address',
      label: 'Địa chỉ',
      component: 'Input',
      componentProps: {
        placeholder: 'Vui lòng nhập địa chỉ',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'memo',
      label: 'Ghi chú',
      component: 'InputTextArea',
      componentProps: {
        placeholder: 'Vui lòng nhập ghi chú',
      },
      ifShow:({ values })=>{
        return values.orgCategory !== '3'
      },
    },
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
  ];
  return { basicFormSchema };
}

// Tùy chọn loại cơ quan
export const orgCategoryOptions = {
  // Phòng ban cấp một
  root: [{ value: '1', label: 'Công ty' }],
  // Phòng ban cấp con
  child: [
    { value: '4', label: 'Công ty con' },
    { value: '2', label: 'Phòng ban' },
    { value: '3', label: 'Vị trí công tác' },
  ],
  //Vị trí phòng ban
  childDepartPost: [
    { value: '2', label: 'Phòng ban' },
    { value: '3', label: 'Vị trí công tác' },
  ],
  //Vị trí công tác
  childPost: [
    { value: '3', label: 'Vị trí công tác' },
  ]
};

/**
 * Danh sách người dùng
 */
export const userColumns: BasicColumn[] = [
  {
    title: 'Họ tên',
    dataIndex: 'realname',
    width: 150,
  },
  {
    title: 'Điện thoại',
    width: 150,
    dataIndex: 'phone',
    customRender:( { record, text })=>{
      if(record.izHideContact && record.izHideContact === '1'){
        return '/';
      }
      return text;
    }
  },
  {
    title: 'Vị trí chính',
    dataIndex: 'mainDepPostId',
    customRender: ({ record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getDepartPathName(record.mainDepPostId_dictText,text,false));
    },
    width: 200,
  },
  {
    title: 'Vị trí kiêm nhiệm',
    dataIndex: 'otherDepPostId',
    customRender: ({ record, text })=>{
      if(!text){
        return '';
      }
      return getDepartName(getMultiDepartPathName(record.otherDepPostId_dictText,text));
    },
    width: 200,
  },
];

/**
 * Sự kiện thay đổi chức vụ
 * @param value
 * @param model
 * @param treeData
 */
export function positionChange(value, model, treeData) {
  if(value && model.parentId){
    getPositionByDepartId({ parentId: model.parentId, departId: model.id ? model.id:'', positionId: value }).then((res) =>{
      if(res.success){
        treeData.value = res.result;
      }else{
        treeData.value = [];
        $message.warning(res.message);
      }
    });
  } else {
    treeData.value = [];
  }
}
