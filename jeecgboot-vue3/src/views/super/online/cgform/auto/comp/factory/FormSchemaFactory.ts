import InputWidget from './impl/InputWidget';
import { FormSchema } from '/@/components/Form';
import DateWidget from './impl/DateWidget';
import SelectWidget from './impl/SelectWidget';
import PasswordWidget from './impl/PasswordWidget';
import FileWidget from './impl/FileWidget';
import ImageWidget from './impl/ImageWidget';
import TextAreaWidget from './impl/TextAreaWidget';
import SelectMultiWidget from './impl/SelectMultiWidget';
import SelectSearchWidget from './impl/SelectSearchWidget';
import PopupWidget from './impl/PopupWidget';
// update-begin--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
import PopupDictWidget from './impl/PopupDictWidget';
// update-end--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
import TreeCategoryWidget from './impl/TreeCategoryWidget';
import SelectDepartWidget from './impl/SelectDepartWidget';
import SelectUserWidget from './impl/SelectUserWidget';
import EditorWidget from './impl/EditorWidget';
import MarkdownWidget from './impl/MarkdownWidget';
import PcaWidget from './impl/PcaWidget';
import AreaLinkage from './impl/AreaLinkage';
import TreeSelectWidget from './impl/TreeSelectWidget';
import RadioWidget from './impl/RadioWidget';
import CheckboxWidget from './impl/CheckboxWidget';
import SwitchWidget from './impl/SwitchWidget';
import TimeWidget from './impl/TimeWidget';
import LinkDownWidget from './impl/LinkDownWidget';
import SlotWidget from './impl/SlotWidget';
import NumberWidget from './impl/NumberWidget';
import LinkTableWidget from './impl/LinkTableWidget'
import LinkTableFieldWidget from './impl/LinkTableFieldWidget'
import LinkTableForQueryWidget from './impl/LinkTableForQueryWidget'
import CascaderPcaForQueryWidget from './impl/CascaderPcaForQueryWidget'
import SelectUser2Widget from './impl/SelectUser2Widget'
import RangeWidget from "./impl/RangeWidget";
import CategoryLinkageForQueryWidget from './impl/CategoryLinkageForQueryWidget';

export default class FormSchemaFactory {
  static createFormSchema(key, data, queryItem) {
    let view = data.view;
    switch (view) {
      case 'password':
        //2.Ô nhập mật khẩu
        return new PasswordWidget(key, data);
      case 'list':
        //3.Ô dropdown
        return new SelectWidget(key, data);
      case 'radio':
        // 4. Chọn đơn
        return new RadioWidget(key, data);
      case 'checkbox':
        // 5.Chọn nhiều
        return new CheckboxWidget(key, data);
      case 'date':
      case 'datetime':
        // 6.Ngày
        // 7.Ngày giờ
        return new DateWidget(key, data, queryItem);
      case 'time':
        // 8 Thời gian
        return new TimeWidget(key, data);
      case 'file':
        // 9.Tệp
        return new FileWidget(key, data);
      case 'image':
        // 10.Hình ảnh
        return new ImageWidget(key, data);
      case 'textarea':
        // 11.Văn bản nhiều dòng
        return new TextAreaWidget(key, data);
      case 'list_multi':
        // 12.Ô dropdown chọn nhiều
        return new SelectMultiWidget(key, data);
      case 'sel_search':
        // 13.Ô tìm kiếm dropdown
        return new SelectSearchWidget(key, data);
      case 'popup':
        // 14. popup
        return new PopupWidget(key, data);
      case 'cat_tree':
        // 15.Cây từ điển phân loại
        return new TreeCategoryWidget(key, data);
      case 'sel_depart':
        // 16.Chọn phòng ban
        return new SelectDepartWidget(key, data);
      case 'sel_user':
        // 17.Lựa chọn của người dùng
        return new SelectUserWidget(key, data);
      case 'umeditor':
        // 18.Văn bản giàu (rich text)
        return new EditorWidget(key, data);
      case 'markdown':
        // 19.MarkDown
        return new MarkdownWidget(key, data);
      case 'pca':
        // 20.Tỉnh/Thành/Quận huyện
        // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-501】Đổi component mới cho tỉnh/thành/quận huyện
        //  return new PcaWidget(key, data);
        return new AreaLinkage(key, data);
      // update-end--author:liaozhiyang---date:20240607---for：【TV360X-501】Đổi component mới cho tỉnh/thành/quận huyện
      case 'link_down':
        // 21.Component liên động
        return new LinkDownWidget(key, data);
      case 'sel_tree':
        // 22.Control cây tùy chỉnh
        return new TreeSelectWidget(key, data);
      case 'switch':
        // 23.Component công tắc
        return new SwitchWidget(key, data);
      case 'link_table':
        // 24.bản ghi liên kết
        return new LinkTableWidget(key, data);
      case 'link_table_field':
        // 25.trường bảng khác
        return new LinkTableFieldWidget(key, data);
      // update-begin--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
      case 'popup_dict':
        // 14. popuptừ điển
        return new PopupDictWidget(key, data);
      // update-end--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
      case 'slot':
        // slot
        return new SlotWidget(key, data);
      case 'LinkTableForQuery':
        return new LinkTableForQueryWidget(key, data);
      case 'CascaderPcaForQuery':
        return new CascaderPcaForQueryWidget(key, data, queryItem);
      // update-begin--author:wangshuai---date:20260819---for：【LHZP-1983】【Trình thiết kế biểu mẫu】Hỗ trợ component liên động
      case 'JCategoryLinkage':
        return new CategoryLinkageForQueryWidget(key, data);
      // update-end--author:wangshuai---date:20260819---for：【LHZP-1983】【Trình thiết kế biểu mẫu】Hỗ trợ component liên động
      case 'select_user2':
        return new SelectUser2Widget(key, data);
      case 'rangeDate':
      case 'rangeTime':
      case 'rangeNumber':
        return new RangeWidget(key, data);
      case 'hidden':
        // Control đã ẩn  như văn bản của cây phân loại
        return new InputWidget(key, data).isHidden();
      default:
        if (data.type == 'number') {
          return new NumberWidget(key, data);
        } else {
          //1.Ô nhập thông thường
          return new InputWidget(key, data);
        }
    }
  }

  static createSlotFormSchema(key, data) {
    let slotFs = new SlotWidget(key, data);
    let view = data.view;
    if ('date' == view) {
      slotFs.groupDate();
    } else if ('datetime' == view) {
      slotFs.groupDatetime();
    } else if ('time' == view) {
      // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn
      slotFs.groupTime();
      // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn
    } else {
      let type = data.type;
      if (type == 'number' || type == 'integer') {
        slotFs.groupNumber();
      }
    }
    return slotFs;
  }

  /**
   * Biểu mẫuID Mặc định là ẩn
   */
  static createIdField(): FormSchema {
    return {
      label: '',
      field: 'id',
      component: 'Input',
      show: false,
    };
  }
}
