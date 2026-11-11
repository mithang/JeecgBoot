import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';
import { getDepartName } from "@/utils/common/compUtils";

export const columns: BasicColumn[] = [
  {
    title: 'Họ tên',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'Phòng ban',
    dataIndex: 'departName',
    customRender:({ text })=>{
      return getDepartName(text);
    }
  },
  {
    title: 'Vị trí chính',
    dataIndex: 'postName',
    customRender:({ text })=>{
      return getDepartName(text);
    }
  },
  {
    title: 'Vị trí kiêm nhiệm',
    dataIndex: 'otherPostName',
    customRender:({ text })=>{
      return getDepartName(text);
    }
  },
/*  {
    title: 'Chức danh',
    dataIndex: 'post',
    width: 150,
    slots: { customRender: 'post' },
  },*/
  {
    title: 'Điện thoại',
    width: 110,
    dataIndex: 'phone',
    customRender:( { record, text })=>{
      if(record.izHideContact && record.izHideContact === '1'){
        return '/';
      }
      return text;
    }
  },
  {
    title: 'Email',
    width: 180,
    dataIndex: 'email',
    customRender:( { record, text })=>{
      if(record.izHideContact && record.izHideContact === '1'){
        return text?'/':'';
      }
      return text;
    }
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Họ tên',
    field: 'realname',
    component: 'Input',
    colProps: { span: 6 },
  },
];
