import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * bản ghi liên kết
 */
export default class LinkTableWidget extends IFormSchema {
  dictTable: string;
  dictText: string;
  dictCode: string;
  view: string;
  componentString: string;
  linkFields: Array<string>;
  
  constructor(key, data) {
    super(key, data);
    this.dictTable = data.dictTable;
    this.dictText = data.dictText;
    this.dictCode = data.dictCode;
    this.view = data.view;
    this.componentString = ''
    this.linkFields = []
  }

  getItem(): FormSchema {
    let item = super.getItem();
    const componentProps = this.getComponentProps()
    return Object.assign({}, item, {
      component: this.componentString,
      componentProps: componentProps
    });
  }

  getComponentProps() {
    let props = {
      textField: this.dictText,
      tableName: this.dictTable,
      valueField: this.dictCode,
    };
    let extend = this.getExtendData();
    // Có chọn nhiều hay không
    if (extend.multiSelect) {
      props['multi'] = true;
    }else{
      props['multi'] = false;
    }
    //Ảnh bìa
    if (extend.imageField) {
      props['imageField'] = extend.imageField;
    }else{
      props['imageField'] = ''
    }
    //Loại hiển thị
    if (extend.showType=='select') {
      this.componentString = 'LinkTableSelect'
      let popContainer = this.getPopContainer();
      props['popContainer'] = popContainer
      // update-begin--author:liaozhiyang---date:20260807---for:【LHZP-604】Bản ghi liên kết dạng dropdown được gắn trênbodyphía trên, tránh không đủ không gian
      props['getPopupContainer'] = () => document.body;
      // update-end--author:liaozhiyang---date:20260807---for:【LHZP-604】Bản ghi liên kết dạng dropdown được gắn trênbodyphía trên, tránh không đủ không gian
    }else{
      this.componentString = 'LinkTableCard'
    }
    if(this.linkFields.length>0){
      props['linkFields'] = this.linkFields;
    }
    return props;
  }

  // Trường bảng khác dùng để dịch
  setOtherInfo(arr){
    // ["Trường biểu mẫu,Trường từ điển bảng","Trường biểu mẫu,Trường từ điển bảng"]
      this.linkFields = arr;
  }
}
