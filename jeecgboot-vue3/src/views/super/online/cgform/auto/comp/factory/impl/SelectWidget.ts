import { FormSchema } from '/@/components/Form';
import { h } from 'vue';
import IFormSchema from '../IFormSchema';

/**
 * Ô dropdown
 * //Chờ xử lý: Từ điển bảng lấy dữ liệu có thể cân nhắc truyền tham số để frontend yêu cầu lại
 */
export default class SelectWidget extends IFormSchema {
  schema: Recordable;
  /*title-value*/
  options: any[];
  dictTable: string;
  dictText: string;
  dictCode: string;
  multi: boolean;

  constructor(key, data) {
    super(key, data);
    this.schema = data;
    // Tùy chọn dữ liệu tĩnh (enum) chuyển đổi thành JSelectSingle Định dạng yêu cầu
    this.options = data['enum'] ? this.getOptions(data['enum'], '') : [];
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
    this.dictCode = data['dictCode'];
    this.multi = data['multi'] || false;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let component = this.getFormComponent()
    let componentProps = this.getComponentProps()
    return Object.assign({}, item, {
      component,
      componentProps,
      renderComponentContent: this.getSlots(componentProps),
    });
  }

  getFormComponent(){
    // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
    // if(this.options.length>0){
    //   return  'Select'
    // }else{
    //   return 'JDictSelectTag'
    // }
    return 'JSelectSingle'
    // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
  }

  setFormRef(ref) {
    super.setFormRef(ref);
    this.handleDictTableParams();
  }

  updateDictTable(dictTable: string) {
    this.formRef.value.updateSchema(({
      field: this.field,
      componentProps: {
        dictCode: this.genDictTableCode(dictTable, this.dictText, this.dictCode),
      }
    }))
  }

  getComponentProps() {
    let mode = this.multi===true?'multiple':'combobox'
    let props: any = {
      allowClear: true,
      mode,
      style: {
        width: '100%',
      },
      getPopupContainer: (_node) => {
        return this.getModalAsContainer();
      },
       // update-begin--author:liaozhiyang---date:20260203---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
      // Mở ra ô dropdown/Callback của việc đóng
      // onDropdownVisibleChange: (visible: boolean)=> {
      //   if (visible && typeof this.schema.updateOptions === 'function') {
      //     this.schema.updateOptions()
      //   }
      // },
      // update-end--author:liaozhiyang---date:20260203---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
    }
    // Có kích hoạt tìm kiếm từ xa hay không+Tải theo cuộn (chế độ bảng làm việc truy vấn)
    const loadMore = this.schema['loadMore'] || this._data['loadMore'];
    if (typeof loadMore === 'function') {
      props['remoteLoad'] = true;
      props['onLoad'] = (params: { keyword: string; pageNo: number; pageSize: number }) => {
        return loadMore(params.keyword, params.pageNo);
      };
      // Truyền xuyên suốt số mục mỗi trang thật, nếu không JSelectSingle Theo giá trị mặc định 10 Phán đoán trang cuối, sẽ gửi thêm một yêu cầu trang trống
      const pageSize = this.schema['pageSize'] || this._data['pageSize'];
      if (pageSize) {
        props['pageSize'] = pageSize;
      }
      delete props.options; // remoteLoad Ở chế độ này không dùng tĩnh options
    } else {
      // Chế độ tìm kiếm thông thường
      const filterable = this.schema['filterable'] || this._data['filterable'];
      if (filterable) {
        props['showSearch'] = true;
      }
    }
    // update-begin--author:liaozhiyang---date:20260203---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
    if (!this.dictTable) {
      props['dictCode'] = this.dictCode;
      // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
      props['useDicColor'] = true;
      // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
      // Dữ liệu tĩnh (không có dictCode) thì, đưa enum Tùy chọn truyền trực tiếp cho component
      if (!this.dictCode && !loadMore && this.options.length > 0) {
        props['options'] = this.options;
      }
    } else {
      props['dictCode'] = this.genDictTableCode(this.dictTable, this.dictText, this.dictCode);
      props['scrollLoad'] = true;
      delete props.onDropdownVisibleChange;
    }
    // update-end--author:liaozhiyang---date:20260203---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
    return props
  }

  getSlots(componentProps: Recordable) {
    const {useDicColor} = componentProps;
    return function () {
      return {
        option(option: Recordable) {
          const style: Recordable = {};
          if (useDicColor && option.color) {
            style.color = '#fff';
            style.height = '20px';
            style.lineHeight = '20px';
            style.padding = '0 6px';
            style.fontSize = '12px';
            style.borderRadius = '8px';
            style.backgroundColor = option.color;
            style.display = 'inline-block';
          }
          return h('span', {
            style,
          }, option.text || option.label);
        },
      };
    }
  }

  getOptions(array, type) {
    if (!array || array.length == 0) {
      return [];
    }
    let isNum = 'number' == type;
    let arr: any[] = [];
    for (let item of array) {
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9359】Tăng cường phán đoán, ngăn dữ liệu cónullBáo lỗi
      if (item == null) break;
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9359】Tăng cường phán đoán, ngăn dữ liệu cónullBáo lỗi
      let value = item.value;
      if(isNum){
        value = parseInt(value)
      }
      arr.push({
        ...item,
        value,
        label: item.title,
      });
    }
    return arr;
  }
}
