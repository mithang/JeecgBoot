import {computed, watch} from 'vue'
import { FormSchema, Rule } from '/@/components/Form';
import { FieldExtends, POP_CONTAINER } from '../../../types/onlineRender';
import { LABELLENGTH } from '../../../util/constant';
import {replaceUserInfoByExpression} from "@/utils/common/compUtils";
/**
 * 1.Chọn phòng ban/Lựa chọn của người dùng Không: cấu hình chọn đơn
 * Loại control
 */
export default abstract class IFormSchema {
  _data;
  field: string;
  label: string;
  labelLength: number;
  formRef: any;
  hidden: boolean;
  order: number;
  required: boolean;
  onlyValidator: any;
  hasChange: boolean;
  pre: string;
  setFieldsValue: any;
  schemaProp: any;
  searchForm: boolean;
  disabled: boolean;
  popContainer: string;
  inPopover: boolean;
  onlyInteger: boolean;

  constructor(key, data) {
    // Cân nhắc không cần lưudata
    this._data = data;
    this.field = key;
    this.label = data.title;
    this.hidden = false;
    this.order = data.order || 999;
    this.required = false;
    this.onlyValidator = '';
    this.setFieldsValue = '';
    this.hasChange = true;
    if (key.indexOf('@') > 0) {
      this.pre = key.substring(0, key.indexOf('@') + 1);
    } else {
      this.pre = '';
    }
    this.schemaProp = {};
    this.searchForm = false;
    this.disabled = false;
    this.popContainer = '';
    this.handleWidgetAttr(data);
    this.inPopover = false;
    this.onlyInteger = false;
    this.labelLength = LABELLENGTH;
    this.initLabelLength();
  }

  /**
   * Lấy mục cấu hình biểu mẫu cuối cùng, bên ngoài gọi phương thức này để lấy
   */
  getFormItemSchema(): FormSchema {
    let schema = this.getItem();
    this.addDefaultChangeEvent(schema);
    return schema;
  }

  /**
   * Lấy cấu hình biểu mẫu, lớp con ghi đè phương thức này
   */
  getItem(): FormSchema {
    let fs: FormSchema = {
      field: this.field,
      label: this.label,
      labelLength: this.labelLength,
      component: 'Input',
      itemProps:{
        labelCol:{
          class: 'online-form-label'
        }
      }
    };
    let rules = this.getRule();
    if (rules.length > 0 && this.onlyValidator) {
      fs['rules'] = rules;
    }
    if (this.hidden === true) {
      fs['show'] = false;
    }
    return fs;
  }

  /**
   * Thiết lập biểu mẫuref
   * popup、dùng khi cây phân loại cần liên kết để thiết lập giá trị của biểu mẫu khác
   * @param ref
   */
  setFormRef(ref) {
    this.formRef = ref;
  }

  /**
   * Thiết lập ẩn phần tử biểu mẫu
   */
  isHidden() {
    this.hidden = true;
    return this;
  }

  /**
   * Thiết lập có phải mục bắt buộc điền hay không
   * @param array
   */
  isRequired(array) {
    // Bảng con bắt buộc điền   TODO
    if (array && array.length > 0) {
      if (array.indexOf(this.field) >= 0) {
        this.required = true;
      }
    }
    return this;
  }

  /**
   * Khởi tạo labelĐộ dài
   */
  initLabelLength(){
    let obj = this.getExtendData()
    if(obj && obj.labelLength){
      this.labelLength = obj.labelLength;
    }
  }

  /**
   * Lấy tham số mở rộng
   */
  getExtendData() {
    let extend: FieldExtends = {};
    let { fieldExtendJson } = this._data;
    if (fieldExtendJson) {
      if (typeof fieldExtendJson == 'string') {
        try {
          let json = JSON.parse(fieldExtendJson);
          extend = { ...json };
        } catch (e) {
          console.error(e);
        }
      }
    }
    return extend;
  }

  /***
   * Lấy các trường khác liên quan đến trường này Cần thiết lập nó thành ẩn
   */
  getRelatedHideFields(): string[] {
    return [];
  }

  /**
   * placeholder
   */
  getPlaceholder(view) {
    let text = 'Vui lòng nhập';
    // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-218】Đối với từng component, nhắc câu kiểm chứng tương ứng riêng
    if (
      [
        'list',
        'radio',
        'checkbox',
        'date',
        'datetime',
        'time',
        'list_multi',
        'sel_search',
        'popup',
        'cat_tree',
        'sel_depart',
        'sel_user',
        'pca',
        'link_down',
        'sel_tree',
        'switch',
        'link_table',
        'link_table_field',
        'popup_dict',
        'LinkTableForQuery',
        'CascaderPcaForQuery',
        'select_user2',
        'rangeDate',
        'rangeTime',
        'rangeNumber',
      ].includes(view)
    ) {
      text = 'Vui lòng chọn';
    } else if (['file', 'image'].includes(view)) {
      text = 'Vui lòng tải lên';
    }
    // update-end--author:liaozhiyang---date:20240521---for：【TV360X-218】Đối với từng component, nhắc câu kiểm chứng tương ứng riêng
    return text + this.label;
  }

  /**
   * Kiểm chứng tính duy nhất
   */
  setOnlyValidateFun(validateFun) {
    if (validateFun) {
      this.onlyValidator = async (rule, value) => {
        let error = await validateFun(rule, value);
        if (!error) {
          return Promise.resolve();
        } else {
          return Promise.reject(error);
        }
      };
    }
  }

  /**
   * Lấy quy tắc kiểm chứng
   */
  getRule(): any[] {
    let rules: Rule[] = [];
    const { view, errorInfo, pattern, type, fieldExtendJson } = this._data;
    if (this.required === true) {
      let msg = this.getPlaceholder(view);
      // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-80】Nhắc kiểm chứng trong cấu hình tham số mở rộng không có hiệu lực
      if (fieldExtendJson) {
        const json = JSON.parse(fieldExtendJson);
        if (json.validateError) {
          msg = json.validateError;
        }
      }
      // update-end--author:liaozhiyang---date:20240520---for：【TV360X-80】Nhắc kiểm chứng trong cấu hình tham số mở rộng không có hiệu lực
      if (errorInfo) {
        msg = errorInfo;
      }
      if (view == 'sel_depart' || view == 'sel_user') {
        //Nếu là component phòng ban và người dùng Sử dụng required：true
        this.schemaProp['required'] = true;
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-9109】onlineKhi sử dụng component phòng ban và người dùng ở dạng bắt buộc điềnlabelPhía trước không có mục bắt buộc điền*số
        rules.push({ required: true, message: msg });
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-9109】onlineKhi sử dụng component phòng ban và người dùng ở dạng bắt buộc điềnlabelPhía trước không có mục bắt buộc điền*số
      } else {
        rules.push({ required: true, message: msg });
      }
    }
    if ('sel_user' == view) {
      if (pattern === 'only' && this.onlyValidator) {
        rules.push({ validator: this.onlyValidator });
      }
    }
    if ('list' === view || 'radio' === view || 'markdown' === view || 'pca' === view || view.indexOf('sel') >= 0 || 'time' === view) {
      return rules;
    }
    if (view.indexOf('upload') >= 0 || view.indexOf('file') >= 0 || view.indexOf('image') >= 0) {
      return rules;
    }
    if (pattern) {
      if (pattern === 'only') {
        if (this.onlyValidator) {
          rules.push({ validator: this.onlyValidator });
        }
      } else if (pattern === 'z') {
        if (type == 'number' || type == 'integer') {
          this.onlyInteger = true;
          rules.push({ pattern: /^-?\d+$/, message: 'Vui lòng nhập số nguyên' });
        } else {
          rules.push({ pattern: /^-?\d+$/, message: 'Vui lòng nhập số nguyên' });
        }
      } else {
        let msg = errorInfo || 'Kiểm chứng biểu thức chính quy thất bại';
        let reg
        try {
          reg = new RegExp(pattern);
          if (!reg) {
            reg = pattern;
          }
        } catch {
          reg = pattern;
        }
        rules.push({ pattern: reg, message: msg });
      }
    }
    return rules;
  }

  /**
   * Thêm cái mặc địnhchangesự kiện
   * @param schema
   */
  addDefaultChangeEvent(schema) {
    if (this.hasChange) {
      if (!schema.componentProps) {
        schema.componentProps = {};
      }
      //update-begin-author:taoyan date:2022-5-24 for: VUEN-1095 Chỉ đọc chưa được kiểm soát chặt
      if (this.disabled == true) {
        schema.componentProps.disabled = true;
      }
      //update-end-author:taoyan date:2022-5-24 for: VUEN-1095 Chỉ đọc chưa được kiểm soát chặt
      if (!schema.componentProps.hasOwnProperty('onChange')) {
        schema.componentProps['onChange'] = (value, formData) => {
          if (value instanceof Event) {
            // Ô nhập valuelàeventđối tượng
            value = (value.target as any).value;
          }
          // của sự kiện component phòng ban ném ravaluelà mảng
          if (value instanceof Array) {
            value = value.join(',');
          }
          // VUEN-1467【vue3 Quy trình làm việc】Xử lý quy trình biểu mẫu một-nhiều bảng contabSau khi chuyển đổi, không đóng được Khiến toàn bộ trình duyệt không thể thao tác được Thao tác thêm vài lần, không nhất thiết lần nào cũng tái hiện---
          if(!this.formRef || !this.formRef.value || !this.formRef.value.$formValueChange){
            console.log('Biểu mẫu hiện tại không thể kích hoạtchangesự kiện,field：'+this.field)
          }else{
            this.formRef.value.$formValueChange(this.field, value, formData)
          }
        };
        // update-begin--author:liaozhiyang---date:20251011---for：【issues/8791】jsTăng cườngpopupcủa popuponlChange()Chưa có hiệu lực
        if (schema.component === 'JPopup') {
          schema.componentProps['onPopUpChange'] = schema.componentProps['onChange']
        }
        // update-end--author:liaozhiyang---date:20251011---for：【issues/8791】jsTăng cườngpopupcủa popuponlChange()Chưa có hiệu lực
      }
    }
    // Tiện thể xử lý các khác schemaProp
    Object.keys(this.schemaProp).map((k) => {
      schema[k] = this.schemaProp[k];
    });
  }

  noChange() {
    this.hasChange = false;
  }

  updateField(field) {
    this.field = field;
  }

  /**
   * Truy vấn nâng cao Không có biểu mẫurefđối tượng Thiết lập thủ côngsetFieldValuePhương thức dùng cho popupThiết lập giá trị biểu mẫu
   */
  setFunctionForFieldValue(func) {
    if (func) {
      this.setFieldsValue = func;
    }
  }

  asSearchForm() {
    this.searchForm = true;
  }

  /**Lấymodallàm component dropdown dạng classpopcontainer cha của*/
  getModalAsContainer() {
    let ele = this.getPopContainer();
    // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7150】onlineRoute có bộ nhớ đệm mở nhiều trang dẫn đến component loại dropdown không mở được
    if (ele != 'body') {
      const elems = document.querySelectorAll(ele);
      if (elems && elems.length > 1) {
        const data: HTMLElement[] = [];
        elems.forEach((item: HTMLElement) => {
          if (!(item.offsetWidth == 0 && item.offsetHeight == 0)) {
            data.push(item);
          }
        });
        if (data.length === 1) {
          return data[0];
        }
      }
    }
    // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7150】onlineRoute có bộ nhớ đệm mở nhiều trang dẫn đến component loại dropdown không mở được
    return document.querySelector(ele);
  }

  /**Phân biệtmodalBiểu mẫu và biểu mẫu truy vấn*/
  getPopContainer() {
    if (this.searchForm === true) {
      return 'body';
    } else if(this.inPopover === true){
      return `.${this.popContainer}`;
    }else if(this.popContainer){
      return `.${this.popContainer} .ant-modal-content`
    }else {
      return POP_CONTAINER;
    }
  }

  handleWidgetAttr(data) {
    if (data.ui) {
      if (data.ui.widgetattrs) {
        if (data.ui.widgetattrs.disabled == true) {
          this.disabled = true;
        }
      }
    }
  }

  /**
   * Thiết lập popContainer
   */
  setCustomPopContainer(modalClass){
    this.popContainer = modalClass;
  }

  //update-begin-author:taoyan date:2022-8-5 for: trường bảng khác/Bản ghi liên kết dùng
  // Lấy của trường bảng khác Thông tin cấu hình
  getLinkFieldInfo():any{
    return '';
  }

  // 1.Thiết lập thông tin cấu hình của trường bảng khác lên trường của bản ghi liên kết
  setOtherInfo(_arg){
  }
  //update-end-author:taoyan date:2022-8-5 for: trường bảng khác/Bản ghi liên kết dùng
  
  // Dùng cho truy vấn nâng cao của trình thiết kế biểu mẫu
  isInPopover(){
    this.inPopover = true;
  }

  handleDictTableParams() {
    if (!this.formRef.value) {
      return
    }
    const dictTable = this._data.dictTable as string
    if (!dictTable) {
      return
    }
    const matches = dictTable.match(/\${([^}]+)}/g)
    if (!matches || matches.length == 0) {
      return
    }
    // Loại bỏ ${}
    const keys = matches.map((item: string) => item.replace('${', '').replace('}', ''))
    const values = computed(() => {
      const formModel = this.formRef.value.formModel
      return keys.map((key) => formModel[key]).join('');
    })
    let timer: ReturnType<typeof setTimeout> | null = null;
    watch(values, () => {
      if (timer) {
        clearTimeout(timer)
      }
      timer = setTimeout(() => {
        const formModel = this.formRef.value.formModel
        // Thay thế tham số động, nếu có ${xxx} thì thay thế bằng giá trị thực tế
        let tempDictTable = dictTable.replace(/\${([^}]+)}/g, (_$0, $1) => {
          if (formModel[$1] == null) {
            return ''
          }
          return formModel[$1]
        });
        this.updateDictTable(tempDictTable)
      }, 150)
    }, {immediate: true})
  }

  updateDictTable(_dictTable: string) {
    console.log('Vui lòng triển khai ở lớp con updateDictTable Phương thức')
  }

  /**
   * Lấy mã của từ điển bảng, có thể thay thế biến hệ thống
   * @param dictTable
   * @param dictText
   * @param dictCode
   */
  genDictTableCode(dictTable: string, dictText: string, dictCode: string) {
    // Thay thế biến hệ thống
    dictTable = replaceUserInfoByExpression(dictTable)
    return encodeURI(`${dictTable},${dictText},${dictCode}`);
  }

}
