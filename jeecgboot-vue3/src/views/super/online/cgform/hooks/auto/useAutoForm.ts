import { ref, watch, reactive, toRaw, nextTick, computed } from 'vue';
import { FormSchema } from '/@/components/Form';
import FormSchemaFactory from '../../auto/comp/factory/FormSchemaFactory';
import IFormSchema from '../../auto/comp/factory/IFormSchema';
import { JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
import { duplicateCheck } from '/@/views/system/user/user.api';
import { initDefValueConfig, initSubTableDefValueConfig } from '../../util/FieldDefVal';
import { usePermissionStore } from '/@/store/modules/permission';
import { ONL_AUTH_PRE } from '../../types/onlineRender';
import { pick } from 'lodash-es';
import { DetailFormSchema } from '../../extend/form/useDetailForm';
import {useExtendComponent} from './useExtendComponent'
import componentSetting from '/@/settings/componentSetting';
import { LABELLENGTH } from '../../util/constant';
import { useAppInject } from '/@/hooks/web/useAppInject';

export const LINK_DOWN = 'link_down';
export const LINK_TABLE_FIELD = 'link_table_field';
export const LINK_TABLE = 'link_table';

export interface OnlSubTab {
  key: string;
  formTemplate: number;
  properties?: any[];
  columns?: any[];
  foreignKey: string;
  describe: string;
  relationType: number;
  requiredFields?: string[];
  order: number;
  id?:string;
}
/**
 * Lấy thông tin cấu hình của biểu mẫu thực tế
 */
export function useFormItems(props, onlineFormRef) {
  // Thêm component biểu mẫu-chuyên dành cho online Dùng cho biểu mẫu
  useExtendComponent();
  
  // Các component như ô dropdown cần thông qua đâyclasslàm cấp chacontainer
  const modalClass = props.modalClass;
  // Cấu hình dùng khi render biểu mẫu
  const formSchemas = ref<any>([]);
  // Tên bảng
  const tableName = ref('');
  // Trang chỉnh sửa Dữ liệu gốc của cơ sở dữ liệu
  const dbData = ref<any>({});
  // Trạng thái hiển thị của trường
  const fieldDisplayStatus = reactive<any>({});
  const hasSubTable = ref(false);

  const subTabInfo = ref<OnlSubTab[]>([]);
  const subDataSource = ref({});
  const refMap = {};
  // Danh sách component liên động
  const linkDownList = ref<any>([]);
  /**
   * Trường có giá trị mặc định của biểu mẫu
   * Tên bảng: [{field, value, type}]
   */
  const defaultValueFields = reactive<any>({});
  // Lưới biểu mẫu
  const baseColProps = ref<any>('');
  baseColProps.value = { sm: 24, xs: 24, md: 12, lg: 12, xl: 12, xxl: 12 };
  // update-begin--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 4 }, md: { span: 4 }, lg: { span: 4 }, xl: { span: 4 }, xxl: { span: 4 } });
  // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
  const wrapperCol = ref<any>(null);
  // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
  // update-end--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
  // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
  const labelWidth = ref<any>(6 * 14 + 10);
  // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài

  function createFormSchemas(properties: any[], required, checkOnlyFieldValue, onlineExtConfigJson = {}) {
    //let properties:any[] = result.schema.properties
    clearObj(defaultValueFields);
    defaultValueFields[tableName.value] = [];
    let subInfo: OnlSubTab[] = [];
    let arr: IFormSchema[] = [];
    let hideFields: string[] = [];
    let dataSourceObj = {};
    let tableLinkInfo:any = {}
    Object.keys(properties).map((key) => {
      const item = properties[key];
      // uiSchema Vô dụng
      //const uiItem = this.uiSchema[key];// method、formTemplate、url
      if (item.view == 'tab') {
        hasSubTable.value = true;
        defaultValueFields[key] = [];
        let temp: OnlSubTab = {
          key,
          formTemplate: Number(item.formTemplate || 1),
          // Cái nàyforeignKeylà trường của bảng chính
          foreignKey: item['foreignKey'],
          describe: item.describe,
          relationType: item.relationType,
          requiredFields: item.required || [],
          order: item.order,
          id: item.id
        };
        if (item.relationType == 1) {
          refMap[key] = ref(null);
          temp['properties'] = item.properties;
        } else {
          dealSubProerties(item);
          refMap[key] = ref<JVxeTableInstance>();
          temp['columns'] = item.columns;
          dataSourceObj[key] = [];
          // TODO Xử lý quyền nút thêm mới/xóa của bảng con
          //this.handleSubTableButtonAuth(item)
        }
        subInfo.push(temp);
        // Ghi lại quyền nút của bảng con
        handleSubTableButtonAuth(key, item);
      } else {
        initDefValueConfig(key, item, defaultValueFields[tableName.value]);
        if (item.view === LINK_DOWN) {
          let array = handleLinkDown(item, key);
          for (let linkDownItem of array) {
            // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-314】Component liên động thêm giá trị mặc định của component
            const fItem = linkDownItem.key == key ? item : item.others?.find((item) => item.field === linkDownItem.key);
            fItem && initDefValueConfig(linkDownItem.key, fItem, defaultValueFields[tableName.value]);
            // update-end--author:liaozhiyang---date:20240522---for：【TV360X-314】Component liên động thêm giá trị mặc định của component
            fieldDisplayStatus[linkDownItem.key] = true;
            fieldDisplayStatus[linkDownItem.key + '_load'] = true;
            // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-75】Biểu mẫulabelThiết lập độ dài, component liên động chưa có hiệu lực
            setFieldExtend(onlineExtConfigJson, linkDownItem);
            // update-end--author:liaozhiyang---date:20240521---for：【TV360X-75】Biểu mẫulabelThiết lập độ dài, component liên động chưa có hiệu lực
            let temp = FormSchemaFactory.createFormSchema(linkDownItem.key, linkDownItem);
            // update-begin--author:liaozhiyang---date:20251230---for：【issues/9223】jstăng cường, dùngloadedTrong phương thức thêm ẩn một trường nào đó, khiến khi mở cửa sổ, các trường khác đã thiết lập chỉ đọc lại khôi phục thành có thể ghi
            fieldDisplayStatus[linkDownItem.key + '_disabled'] = temp.disabled ?? false;
            // update-end--author:liaozhiyang---date:20251230---for：【issues/9223】jstăng cường, dùngloadedTrong phương thức thêm ẩn một trường nào đó, khiến khi mở cửa sổ, các trường khác đã thiết lập chỉ đọc lại khôi phục thành có thể ghi
            if (checkOnlyFieldValue) {
              temp.setOnlyValidateFun(checkOnlyFieldValue);
            }
            temp.isRequired(required);
            temp.setFormRef(onlineFormRef);
            // Control liên động Tính chỉ đọc do trạng thái chỉ đọc của control đầu tiên quyết định
            temp.handleWidgetAttr(item);
            let tempIndex = getFieldIndex(arr, linkDownItem.key);
            if (tempIndex == -1) {
              arr.push(temp);
            } else {
              arr[tempIndex] = temp;
            }
          }
        } else {
          // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-314】Component liên động thêm giá trị mặc định của component
          initDefValueConfig(key, item, defaultValueFields[tableName.value]);
          // update-end--author:liaozhiyang---date:20240522---for：【TV360X-314】Component liên động thêm giá trị mặc định của component
          fieldDisplayStatus[key] = true;
          fieldDisplayStatus[key + '_load'] = true;
          let tempIndex = getFieldIndex(arr, key);
          if (tempIndex == -1) {
            // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
            setFieldExtend(onlineExtConfigJson, item);
            // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
            let temp = FormSchemaFactory.createFormSchema(key, item);
            // update-begin--author:liaozhiyang---date:20251230---for：【issues/9223】jstăng cường, dùngloadedTrong phương thức thêm ẩn một trường nào đó, khiến khi mở cửa sổ, các trường khác đã thiết lập chỉ đọc lại khôi phục thành có thể ghi
            fieldDisplayStatus[key + '_disabled'] = temp.disabled ?? false;
            // update-end--author:liaozhiyang---date:20251230---for：【issues/9223】jstăng cường, dùngloadedTrong phương thức thêm ẩn một trường nào đó, khiến khi mở cửa sổ, các trường khác đã thiết lập chỉ đọc lại khôi phục thành có thể ghi
            if (checkOnlyFieldValue) {
              temp.setOnlyValidateFun(checkOnlyFieldValue);
            }
            temp.isRequired(required);
            temp.setFormRef(onlineFormRef);
            arr.push(temp);
            hideFields.push(...temp.getRelatedHideFields());
            
            //update-begin-author:taoyan date:2022-8-5 for: Lấy thông tin trường của bảng khác
            //Nếu là trường bảng khác thì lấy thông tin liên kết
            if(item.view === LINK_TABLE_FIELD){
              let tempInfo = temp.getLinkFieldInfo();
              if(tempInfo){
                if(tableLinkInfo[tempInfo[0]]){
                  let tableLinkInfoEle:string[] = tableLinkInfo[tempInfo[0]];
                  tableLinkInfoEle.push(tempInfo[1]);
                }else{
                  tableLinkInfo[tempInfo[0]] = [tempInfo[1]]
                }
              }
            }
            //update-end-author:taoyan date:2022-8-5 for: Lấy thông tin trường của bảng khác
            
          }
        }
        //fp.checkOnlyMethod = this.$Jdebounce(this.checkOnlyFieldValue, 1000);
      }
    });
    // 1.Đối vớiarrSắp xếp
    arr.sort(function (a, b) {
      return a.order - b.order;
    });
    // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
    const oneRowData: any = [];
    (() => {
      for (let i = 0, len = arr.length; i < len; i++) {
        const item = arr[i];
        if (getFieldExtend(item?._data, 'isOneRow')) {
          oneRowData.push(arr.splice(i, 1)[0]);
          i--;
          len--;
        }
      }
    })();
    arr = [...arr,...oneRowData];
    // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
    // 2.Lấy cấu hình biểu mẫu thật
    let formSchemaArray: FormSchema[] = [];
    formSchemaArray.push(FormSchemaFactory.createIdField());
    let longestLabelComponet: any = null;
    let isComponetRequired = false;
    for (let a of arr) {
      // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      const curLabelLen = a.label.length;
      if (longestLabelComponet) {
        if (longestLabelComponet.label.length < curLabelLen) {
          longestLabelComponet = a;
        } else if(longestLabelComponet.label.length === curLabelLen) {
          // Văn tự (chữ)labaelgiống nhau, thì phán đoán có bắt buộc điền hay không. (Bắt buộc điền*chiếm13pixel)
          if(!longestLabelComponet.required && a.required) {
            longestLabelComponet = a;
          }
        }
      } else {
        longestLabelComponet = a;
      }
      // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      // update-begin--author:liaozhiyang---date:20230105---for：【TV360X-209】Biểu mẫulabelĐộ dài đã thiết lập và trường có bắt buộc điền, độ rộng tính không đúng
      if (a.required) {
        isComponetRequired = true;
      }
      // update-end--author:liaozhiyang---date:20230105---for：【TV360X-209】Biểu mẫulabelĐộ dài đã thiết lập và trường có bắt buộc điền, độ rộng tính không đúng
      //update-begin-author:taoyan date:2022-8-5 for: Đưa thông tin cấu hình của trường bảng khác Thêm vào tới trên trường liên kết
      //Trường bản ghi liên kết thiết lập cấu hình mới
      if(a['view'] && a['view']==LINK_TABLE){
        if(tableLinkInfo[a.field]){
          a.setOtherInfo(tableLinkInfo[a.field])
        }
      }
      //update-end-author:taoyan date:2022-8-5 for: Đưa thông tin cấu hình của trường bảng khác Thêm vào tới trên trường liên kết
      //Thiết lậphiddentrường của
      if (hideFields.indexOf(a.field) >= 0) {
        a.isHidden();
      }
      // popModal-Container cấp cha của ô dropdown cần tùy chỉnh, nếu không sẽ bị che khuất
      if(modalClass){
        a.setCustomPopContainer(modalClass)
      }
      // update-begin--author:liaozhiyang---date:20231222---for：【QQYUN-7515】online Từ điển dropdown, component chọn đơn (radio)optionsĐi theo loại cơ sở dữ liệu
      const result = a.getFormItemSchema();
      if (result.component === 'JDictSelectTag' && ['number', 'integer'].includes(a?._data?.type)) {
        result.componentProps.stringToNumber = true;
      }
      // update-end-author:liaozhiyang---date:20231222---for：【QQYUN-7515】online Từ điển dropdown, component chọn đơn (radio)optionsĐi theo loại cơ sở dữ liệu
      // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
      if (props.formTemplate > 1 && getFieldExtend(a?._data, 'isOneRow')) {
        result.colProps = { span: 24 };
        const colGrid = getFormItemColProps();
        const { labelCol = {} } = colGrid;
        const itemLabelCol = {};
        const itemWrapperCol = {};
        Object.keys(labelCol).forEach((key) => {
          if (['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].includes(key)) {
            const span = labelCol[key].span;
            const value = Math.round(span / props.formTemplate);
            itemLabelCol[key] = { span: value };
            itemWrapperCol[key] = { span: 24 - value - 1 };
          }
        });
        result.itemProps = { labelCol: itemLabelCol, wrapperCol: itemWrapperCol };
      }
      // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
      // update-begin--author:liaozhiyang---date:20251011---for：【issues/8791】jsTăng cườngpopupcủa popuponlChange()Chưa có hiệu lực
      if (result.component === 'JPopup') {
         result.changeEvent = 'popUpChange';
      }
      // update-end--author:liaozhiyang---date:20251011---for：【issues/8791】jsTăng cườngpopupcủa popuponlChange()Chưa có hiệu lực
      formSchemaArray.push(result);
    }
    formSchemas.value = formSchemaArray;
    //return formSchemaArray;
    //update-begin-author:taoyan date:2022-5-31 for: VUEN-1147 bảng chính-con Thứ tự của bảng con không sắp xếp theo thứ tự thiết lập
    subInfo.sort(function (a, b) {
      return a.order - b.order;
    });
    //update-end-author:taoyan date:2022-5-31 for: VUEN-1147 bảng chính-con Thứ tự của bảng con không sắp xếp theo thứ tự thiết lập
    // update-begin--author:liaozhiyang---date:20231009---for：【issues/5371】Bảng con một-nhiềupopupThêm chọn nhiều
    subInfo.forEach((sItem) => {
      const columns: any = sItem.columns;
      if(sItem.columns){
        columns.forEach((cItem) => {
          // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-452】Bảng con một-nhiềupopupChọn nhiều mặc định chưa có hiệu lực
          if (sItem.relationType == 0) {
            if (['popup', 'popup_dict'].includes(cItem.type)) {
              // Chỉ có1Chỉ quan hệ đối-nhiều mới cần xử lý,1Đối với1hoặc đã được xử lý trong component trực tiếp của bảng đơn.
              let popupMulti = true;
              if (cItem.fieldExtendJson) {
                const fieldExtendJson = JSON.parse(cItem.fieldExtendJson);
                popupMulti = fieldExtendJson.popupMulti;
              }
              const props = cItem.props ?? {};
              cItem.props = { ...props, multi: popupMulti };
            }
          }
          // update-end--author:liaozhiyang---date:20240529---for：【TV360X-452】Bảng con một-nhiềupopupChọn nhiều mặc định chưa có hiệu lực
          // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9205】Một-nhiều(jVxetableComponentdate)Hỗ trợ năm, năm-tháng, năm, năm-tuần
          if (cItem.type === 'date' && cItem.fieldExtendJson) {
            const fieldExtendJson = JSON.parse(cItem.fieldExtendJson);
            if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
              Object.assign(cItem, { picker: fieldExtendJson.picker });
            }
          }
          // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9205】Một-nhiều(jVxetableComponentdate)Hỗ trợ năm, năm-tháng, năm, năm-tuần
        });
      }
    });
    // update-end--author:liaozhiyang---date:20231009---for：【issues/5371】Bảng con một-nhiềupopupThêm chọn nhiều
    subTabInfo.value = subInfo;
    subDataSource.value = dataSourceObj;
    // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
    if (onlineExtConfigJson.formLabelLengthShow && onlineExtConfigJson.formLabelLength) {
      // 14là văn tựsize，24là khoảng hở (gap)
      labelWidth.value = onlineExtConfigJson.formLabelLength * 14 + 10 + (+`${isComponetRequired ? 13 : 0}`);
      wrapperCol.value = null;
    } else {
      // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      // numGiá trị này làlabelĐộ dài thực trước khi cắt, nếu lớn hơn giá trị khởi tạo thì vẫn phải sử dụng giá trị khởi tạo
      if (longestLabelComponet) {
        let realLabelLen = longestLabelComponet.label.length;
        realLabelLen = realLabelLen > LABELLENGTH ? LABELLENGTH : realLabelLen;
        const required = longestLabelComponet.required;
        const num = realLabelLen * 14 + 10 + (+`${required ? 13 : 0}`);
        labelWidth.value = num;
      }
      // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
    }
    // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
  }
  
  watch(
    fieldDisplayStatus,
    (val) => {
      let ref = onlineFormRef.value;
      let arr: any[] = [];
      let map = toRaw(val);
      Object.keys(map).map((k) => {
        if (k.endsWith('_load')) {
        } else {
          let item = {
            field: k,
            show: map[k],
          };
          let loadKey = k + '_load';
          if (map.hasOwnProperty(loadKey)) {
            item['ifShow'] = map[loadKey];
          }
          // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8537】jstăng cường, kiểm soát việc vô hiệu hóa trường của biểu mẫu
          let disabledKey = k + '_disabled';
          if (map.hasOwnProperty(disabledKey)) {
            item['dynamicDisabled'] = () => {
              return map[disabledKey];
            };
          }
          // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-8537】jstăng cường, kiểm soát việc vô hiệu hóa trường của biểu mẫu
          arr.push(item);
        }
      });
      if (ref) {
        ref.updateSchema(arr);
      }
    },
    { immediate: false }
  );

  function dealSubProerties(subInfo) {
    useOnlineVxeTableColumns(subInfo, (column)=>{
      initSubTableDefValueConfig(column, defaultValueFields[subInfo.key]);
    })
  }
  /*
   2024-03-06
   liaozhiyang
   của tham số mở rộng trong biểu mẫulabelLengthThiết lập thànhonlineExtConfigJson.formLabelLength
  */
  function setFieldExtend(onlineExtConfigJson, data, key = 'labelLength') {
    const { formLabelLengthShow, formLabelLength } = onlineExtConfigJson;
    if (formLabelLengthShow && formLabelLength) {
      let fieldExtendJson = data?.fieldExtendJson;
      if (fieldExtendJson) {
        fieldExtendJson = JSON.parse(fieldExtendJson);
        fieldExtendJson[key] = formLabelLength;
      } else {
        fieldExtendJson = { [key]: formLabelLength };
      }
      data.fieldExtendJson = JSON.stringify(fieldExtendJson);
    } else if (data?.fieldExtendJson) {
      // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-34】Truy vấnlabelĐộ dài thiết lập chỉ nên có hiệu lực với độ dài ký tự của vùng truy vấn, không nên có hiệu lực với độ dài biểu mẫu
      const fieldExtendJson = JSON.parse(data?.fieldExtendJson);
      delete fieldExtendJson.labelLength;
      data.fieldExtendJson = JSON.stringify(fieldExtendJson);
      // update-end--author:liaozhiyang---date:20260714---for：【LHZP-34】Truy vấnlabelĐộ dài thiết lập chỉ nên có hiệu lực với độ dài ký tự của vùng truy vấn, không nên có hiệu lực với độ dài biểu mẫu
    }
  }

  /*
   2024-01-05
   liaozhiyang
   Lấy tham số mở rộng
  */
  function getFieldExtend(data: any = {}, key: string) {
    let fieldExtendJson = data?.fieldExtendJson;
    if (fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      return fieldExtendJson[key];
    }
  }

  //Lắng nghe của biểu mẫu bảng chínhformTemplate
  watch(
    () => props.formTemplate,
    () => {
      //Render lại biểu mẫu
      const result = getFormItemColProps()
      baseColProps.value = result.baseColProps;
      labelCol.value = result.labelCol;
      wrapperCol.value = result.wrapperCol;
    },
    { immediate: true }
  );

  function getFormItemColProps() {
    let temp = props.formTemplate;
    // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
    // update-begin--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
    // const form: any = componentSetting.form || {};
    // const { labelCol = {} } = form;
    // const { wrapperCol = {} } = form;
    // update-end--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
    if (temp == 2) {
      // update-begin--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
      return {
        baseColProps: { sm: 24, xs: 24, md: 12, lg: 12, xl: 12, xxl: 12 },
        // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        // labelCol: { xs: { span: 24 }, sm: { span: 4 }, md: { span: 4 }, lg: { span: 4 }, xl: { span: 4 }, xxl: { span: 4 } },
        // wrapperCol: { xs: { span: 24 }, sm: { span: 19 }, md: { span: 19 }, lg: { span: 19 }, xl: { span: 19 }, xxl: { span: 19 } },
        // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      };
      // update-end--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
    } else if (temp == 3) {
      return {
        baseColProps: { sm: 24, xs: 24, md: 8, lg: 8, xl: 8, xxl: 8 },
        // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        // labelCol: { xs: { span: 24 }, sm: { span: 6 }, md: { span: 6 }, lg: { span: 6 }, xl: { span: 6 }, xxl: { span: 6 } },
        // wrapperCol: { xs: { span: 24 }, sm: { span: 17 }, md: { span: 17 }, lg: { span: 17 }, xxl: { span: 17 } },
        // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      };
    } else if (temp == 4) {
      return {
        baseColProps: { sm: 24, xs: 24, md: 6, lg: 6, xl: 6, xxl: 6 },
        // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        // labelCol: { xs: { span: 24 }, sm: { span: 4 }, md: { span: 4 }, lg: { span: 4 }, xl: { span: 4 }, xxl: { span: 4 } },
        // wrapperCol: { xs: { span: 24 }, sm: { span: 18 }, md: { span: 18 }, lg: { span: 18 }, xl: { span: 18 }, xxl: { span: 18 } },
        // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      };
    } else {
      // update-begin--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
      return {
        baseColProps: { sm: 24, xs: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
        // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        // labelCol: { xs: { span: 24 }, sm: { span: 4 }, md: { span: 4 }, lg: { span: 4 }, xl: { span: 4 }, xxl: { span: 4 } },
        // wrapperCol:{ xs: { span: 24 }, sm: { span: 18 }, md: { span: 18 }, lg: { span: 18 }, xl: { span: 18 }, xxl: { span: 18 } },
        // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
      };
      // update-end--author:liaozhiyang---date:20240311---for：【QQYUN-8440】Căn giữa trên màn hình nhỏ(Theovue2Đồng bộ lưới (grid))
    }
    // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
  }
  // Kiểm chứng tính duy nhất
  function checkOnlyFieldValue(rule, value) {
    return new Promise((resolve) => {
      if (!value) {
        resolve('');
      }
      //Đối với view Cần đưa của sau tên bảng$+Chữ số Bỏ đi
      let realTableName = tableName.value.replace(/\$\d+/, '');
      let param = {
        tableName: realTableName,
        fieldName: rule.field,
        fieldVal: value,
      };
      let formData: any = dbData.value;
      if (formData.id) {
        param['dataId'] = formData.id;
      }
      //console.log("Kiểm chứng tính duy nhất---》",param)
      duplicateCheck(param)
        .then((res) => {
          if (res.success) {
            resolve('');
          } else {
            resolve(res.message);
          }
        })
        .catch((msg) => {
          resolve(msg);
        });
    });
  }

  /**
   * Một số dữ liệu ở định dạng mảng Ép chuyển thành chuỗi
   */
  function changeDataIfArray2String(data) {
    Object.keys(data).map((k) => {
      if (data[k]) {
        if (data[k] instanceof Array) {
          data[k] = data[k].join(',');
        }
      }
    });
    return data;
  }

  return {
    formSchemas,
    defaultValueFields,
    tableName,
    dbData,
    checkOnlyFieldValue,
    createFormSchemas,
    fieldDisplayStatus,
    subTabInfo,
    hasSubTable,
    subDataSource,
    baseColProps,
    changeDataIfArray2String,
    linkDownList,
    refMap: refMap,
    labelCol,
    wrapperCol,
    labelWidth,
  };
}

/**
 * Xử lýonlinebảng con jvxeTablethông tin cấu hình cột của
 */
export function useOnlineVxeTableColumns(subInfo, callback?){
  const uploadColumnMinWidth = 180;
  const uploadColumnTypes = ['image', 'file'];
  // mới và cũjvxetable Loại của các cột không nhất quán
  const vxeTypeMap = {
    inputNumber: 'input-number',
    sel_depart: 'depart-select',
    sel_user: 'user-select',
    list_multi: 'select-multiple',
    input_pop: 'textarea',
    sel_search: 'select-search',
    'select-dict-search': 'selectDictSearch',
  };
  
  //bảng con một-nhiều nếu là nút radio thì đổi thành dropdown
  subInfo.columns.forEach((column) => {
    if (column.type === 'radio') {
      column.type = 'select';
    } else if (vxeTypeMap[column.type]) {
      const originalType = column.type;
      column.type = vxeTypeMap[originalType];
      // OnlineSố chữ số thập phân của trường số được truyền choJVxeTable, bảo đảm xử lý độ chính xác của chỉnh sửa hàng và chỉnh sửa popup nhất quán
      if (originalType === 'inputNumber' && typeof column.dbPointLength === 'number') {
        column.props = {
          ...(column.props ?? {}),
          precision: column.dbPointLength,
        };
      }
    } else if (column.type === 'popup') {
      handleSubPopup(column);
    } else if (column.type === 'popup_dict') {
      // update-begin--author:liaozhiyang---date:20260507---for：【issues/7617】vxetableBảng con hỗ trợpopuptừ điển
      const { dictTable, dictCode, dictText } = column;
      column.fieldConfig = `${dictTable},${dictText ?? ''},${dictCode ?? ''}`;
      column.type = 'popupDict';
      column.multi = true;
      const str = column.fieldExtendJson;
      if (str) {
        try {
          const json = JSON.parse(str);
          if (json.multiSelect === false) {
            column.multi = false;
          }
        } catch (e) {
          console.log('Bảng con lấypopupdictCấu hình mở rộng của xuất hiện lỗi', e);
        }
      }
      // update-end--author:liaozhiyang---date:20260507---for：【issues/7617】vxetableBảng con hỗ trợpopuptừ điển
    } else if (column.type === 'link_table') {
      handleSubLinkTable(column, subInfo.columns);
    } else if (column.type === 'link_table_field') {
      // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      column.type = 'input';
      column.flag = 'link-table-field';
      column.props = {
        ...(column.props ?? {}),
        disabled: true,
      };
      // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
    }
    // Component chọn cây phòng ban cần thiết lập Nút cha và nút con không liên kết
    if (column.type === 'depart-select') {
      column['checkStrictly'] = true;
    }
    // Lựa chọn người dùng trong bảng con Kiểm soát có chọn nhiều hay không
    if (column.type === 'user-select') {
      handleSubUserSelect(column);
    }
    if (column.type === 'pca') {
      column.width = '230px';
    }
    // update-begin--author:liaozhiyang---date:20260413---for:【issues/7633】onlineBảng con hỗ trợ cây từ điển phân loại, cây tùy chỉnh
    // Cây tùy chỉnh
    if (column.type === 'sel_tree') {
      const { dictTable, dictCode, dictText } = column;
      const [id, pid, name, child] = dictText.split(',');
      column.type = 'sel-tree';
      column.dict = `${dictTable},${name},${id}`;
      column.pidField = pid;
      column.pidValue = dictCode ?? '0';
      column.hasChildField = child;
      delete column.dictText;
      delete column.dictCode;
      delete column.dictTable;
    }
    // Cây từ điển phân loại
    if (column.type === 'cat_tree') {
       const { dictCode } = column;
      column.type = 'cat-tree';
      column.pcode = dictCode ?? '0';
      delete column.dictCode;
    }
    // update-end--author:liaozhiyang---date:20260413---for:【issues/7633】onlineBảng con hỗ trợ cây từ điển phân loại, cây tùy chỉnh
    //update-begin-author:taoyan date:2022-4-24 for: VUEN-855 Bảng con quan hệ đối-nhiều Tệp, hình ảnh phía bên phải thiếu một đường viền
    // Cột hình ảnh, tệp đính kèm cần chừa không gian cho biểu tượng tải lên, chữ trên nút và lề trong của ô, tránh nội dung bị cắt xén
    const columnWidth = Number.parseFloat(column.width);
    if (uploadColumnTypes.includes(column.type) && columnWidth > 0 && columnWidth < uploadColumnMinWidth) {
      column.width = `${uploadColumnMinWidth}px`;
    }
    //Nếu không có độ rộng Mặc định thiết lập một độ rộng
    if (!column.width) {
      column.width = '200px';
    }
    if(callback){
      callback(column)
    }
    //update-end-author:taoyan date:2022-4-24 for: VUEN-855 Bảng con quan hệ đối-nhiều Tệp, hình ảnh phía bên phải thiếu một đường viền
  });

  // bảng conpopupXử lý đặc biệt
  function handleSubPopup(column) {
    let { destFields, orgFields } = column;
    let fieldConfig: any[] = [];
    if (!destFields || destFields.length == 0) {
    } else {
      let arr1 = destFields.split(',');
      let arr2 = orgFields.split(',');
      for (let i = 0; i < arr1.length; i++) {
        fieldConfig.push({
          target: arr1[i],
          source: arr2[i],
        });
      }
    }
    column.fieldConfig = fieldConfig;
  }

  // bảng con Xử lý đặc biệt cho lựa chọn của người dùng
  function handleSubUserSelect(column) {
    let str = column.fieldExtendJson;
    let isRadioSelection = false;
    if (str) {
      try {
        let json = JSON.parse(str);
        if (json.multiSelect === false) {
          isRadioSelection = true;
        }
      } catch (e) {
        console.log('Bảng con lấy cấu hình mở rộng của component người dùng xảy ra lỗi', e);
      }
    }
    column.isRadioSelection = isRadioSelection;
  }
  /**
   * 20260317
   * 【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
   * liaozhiang
   * */
  function handleSubLinkTable(column, columns) {
    column.type = 'link-table';
    column.tableName = column.dictTable || '';
    column.valueField = column.dictCode || 'id';
    column.textField = column.dictText || '';
    column.multi = false;
    column.linkFields = [];
    columns.forEach(item => {
      if (item.type === 'link_table_field' && item.dictTable === column.key) {
        column.linkFields.push(`${item.key},${item.dictText}`);
      }
    });
    delete column.dictTable;
    delete column.dictCode;
    delete column.dictText;
    let str = column.fieldExtendJson;
    if (str) {
      try {
        let json = JSON.parse(str);
        if (json.multiSelect === true) {
          column.multi = true;
        }
      } catch (e) {
        console.log('Bảng con lấy cấu hình mở rộng của component bản ghi liên kết xảy ra lỗi', e);
      }
    }
    if (!column.width || column.width === '200px') {
      column.width = '240px';
    }
  }
  
}

/***
 * Ngữ cảnh biểu mẫu
 */
export function useOnlineFormContext(props) {
  let that = {};
  const CONTEXT_DESCRIPTION = {
      addSubRows: '<m> bảng con một-nhiều, thêm hàng tùy chỉnh',
      changeOptions: '<m> Thay đổi tùy chọn ô dropdown',
      clearSubRows: '<m> Xóa sạch các hàng của bảng con một-nhiều',
      clearThenAddRows: '<m> Xóa sạch các hàng của bảng con một-nhiều, sau đó thêm hàng tùy chỉnh',
      executeMainFillRule: '<m> Làm mới giá trị quy tắc gia tăng của bảng chính',
      executeSubFillRule: '<m> Làm mới giá trị quy tắc gia tăng của bảng con',
      getFieldsValue: '<m> Lấy giá trị của control biểu mẫu',
      getSubTableInstance: '<m> Lấy instance của bảng con',
      isUpdate: '<p> Phán đoán có phải chế độ chỉnh sửa hay không',
      loading: '<p> Trạng thái tải trang',
      onlineFormRef: '<p> Biểu mẫu hiện tạirefđối tượng',
      refMap: '<p> bảng conrefđối tượngmap',
      setFieldsValue: '<m> Thiết lập giá trị của control biểu mẫu',
      sh: '<p> Trạng thái hiển thị/ẩn của control biểu mẫu',
      subActiveKey: '<p> Kích hoạt bảng contab, tương ứng tên bảng của bảng con',
      subFormHeight: '<p> chiều cao biểu mẫu bảng con một-một',
      submitFlowFlag: '<p> Có gửi trạng thái quy trình hay không',
      subTableHeight: '<p> chiều cao bảng của bảng con một-nhiều',
      tableName: '<p> Tên bảng hiện tại',
      triggleChangeValues: '<m> Sửa nhiều giá trị biểu mẫu',
      triggleChangeValue: '<m> Sửa giá trị biểu mẫu',
      updateSchema: '<m> Sửa cấu hình control của biểu mẫu',
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
      changeSubTableOptions: '<m> Thay đổi tùy chọn ô dropdown của bảng con một-nhiều',
      changeSubFormbleOptions: '<m> Thay đổi tùy chọn ô dropdown của bảng con một-một',
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
      // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-5806】jsPhần tăng cường thay đổi tìm kiếm dropdownoptions
      changeRemoteOptions:'<m> Thay đổi tùy chọn ô dropdown từ xa',
     // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-5806】jsPhần tăng cường thay đổi tìm kiếm dropdownoptions
     // update-begin--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
     submitFormAndFlow: '<m> Gửi biểu mẫu và khởi tạo quy trình',
    // update-end--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
  };
  const onlineFormContext = new Proxy(CONTEXT_DESCRIPTION, {
    get(_target: any, prop: string): any {
      return Reflect.get(that, prop);
    },
  });

  function addObject2Context(prop, object) {
    that[prop] = object;
  }

  function resetContext(context) {
    Object.keys(context).map((k) => {
      that[k] = context[k];
    });
  }
  addObject2Context('$nextTick', nextTick);
  addObject2Context('addObject2Context', addObject2Context);

  // Nút tùy chỉnh
  const createBIButtonCfg = (btnKey, defBtn) => computed(() => {
    const {buttonSwitch} = props
    const cfg = {
      enabled: true,
      buttonIcon: defBtn[0],
      buttonName: defBtn[1],
    }
    if (buttonSwitch?.[btnKey] === false) {
      cfg.enabled = false
      return cfg
    }
    const {cgBIBtnMap} = props
    return cgBIBtnMap?.[btnKey] ? cgBIBtnMap[btnKey] : cfg
  })

  const getSubAddBtnCfg = createBIButtonCfg('form_sub_add', ['ant-design:plus-outlined', 'Thêm mới'])
  const getSubRemoveBtnCfg = createBIButtonCfg('form_sub_batch_delete', ['ant-design:minus-outlined', 'Xóa'])
  const getSubOpenAddBtnCfg = createBIButtonCfg('form_sub_open_add', ['ant-design:form-outlined', 'Thêm mới'])
  const getSubOpenEditBtnCfg = createBIButtonCfg('form_sub_open_edit', ['ant-design:form-outlined', 'Chỉnh sửa bằng popup'])

  return {
    onlineFormContext,
    addObject2Context,
    resetContext,

    getSubAddBtnCfg,
    getSubRemoveBtnCfg,
    getSubOpenAddBtnCfg,
    getSubOpenEditBtnCfg,
  };
}

/**
 * Tìm component liên động
 * @param properties
 */
export function handleLinkDown(item, field) {
  const {
    config: { table, key, txt, linkField, idField, pidField, condition },
    others,
    order,
    title,
  } = item;
  let commonProp = {
    dictTable: table,
    dictText: txt,
    dictCode: key,
    pidField: pidField,
    idField: idField,
    view: LINK_DOWN,
    type: item.type,
  };
  let array: any = [];
  let main = {
    key: field,
    title,
    order,
    condition,
    origin: true,
    ...commonProp,
  };

  if (linkField && linkField.length > 0) {
    let fields = linkField.split(',');
    main['next'] = fields[0];
    for (let i = 0; i < fields.length; i++) {
      for (let o of others) {
        if (o.field == fields[i]) {
          let temp = {
            key: o.field,
            title: o.title,
            order: o.order,
            origin: false,
            ...commonProp,
          };
          if (i + 1 < fields.length) {
            temp['next'] = fields[i + 1];
          }
          array.push(temp);
        }
      }
    }
  }
  array.push(main);
  //let ls = linkDownList.value
  // ls.push(...array)
  // linkDownList.value = ls;
  return array;
}

/**
 * Lấy Chỉ mục của trường
 * @param arr
 * @param key
 */
export function getFieldIndex(arr: IFormSchema[], key: string) {
  let index = -1;
  for (let i = 0; i < arr.length; i++) {
    let item = arr[i];
    if (item.field === key) {
      index = i;
      break;
    }
  }
  return index;
}

/**
 * Lấy bằng vòng thăm dò (polling) ref Giá trị của đối tượng, lấy thànhtruehoặc thực sự tồn tại thì thực thi logic bước tiếp theo, có thể dùng để phán đoán trạng thái hoặc việc tải của component đã hoàn tất hay chưa
 * @param componentRef
 */
export function getRefPromise(componentRef) {
  return new Promise((resolve) => {
    (function next() {
      let ref = componentRef.value;
      if (ref) {
        resolve(ref);
      } else {
        setTimeout(() => {
          next();
        }, 100);
      }
    })();
  });
}

function clearObj(obj) {
  Object.keys(obj).map((k) => {
    delete obj[k];
  });
}

//update-begin-author:taoyan date:2022-6-1 for:  VUEN-1162 Nút của bảng con không được kiểm soát
/**
 * Ghi lại quyền nút của bảng con-Mã nút đã ẩn
 */
const permissionStore = usePermissionStore();
function handleSubTableButtonAuth(tableName, item) {
  let arr = item.hideButtons;
  let code = ONL_AUTH_PRE + tableName + ':';
  if (!arr) {
    arr = [];
  }
  permissionStore.setOnlineSubTableAuth(code, arr);
}
//update-end-author:taoyan date:2022-6-1 for:  VUEN-1162 Nút của bảng con không được kiểm soát



/**
 * Lấy DetailFormSchema[onlineDùng]
 */
export function getDetailFormSchemas(props) {
  const detailFormSchemas = ref<DetailFormSchema[]>([]);
  const refMap = {};
  const showStatus = reactive({
    
  });
  const hasSubTable = ref(false);
  const subTabInfo = ref<OnlSubTab[]>([]);
  const subDataSource = ref({});
  const { getIsMobile } = useAppInject();
  const formSpan = computed(() => {
    let temp = props.formTemplate;
    // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-82】Trang chi tiết phía di động chỉ hiển thị một cột
    if (getIsMobile.value) {
      return 24;
    }
    // update-end--author:liaozhiyang---date:20240522---for：【TV360X-82】Trang chi tiết phía di động chỉ hiển thị một cột
    if (temp == '2') {
      return 12;
    } else if (temp == '3') {
      return 8;
    } else if (temp == '4') {
      return 6;
    } else {
      return 24;
    }
  });

  function createFormSchemas(properties: any[]) {
    //let properties:any[] = result.schema.properties
    let subInfo: OnlSubTab[] = [];
    console.log('111', properties);
    let arr: DetailFormSchema[] = [];
    let dataSourceObj = {};
    // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
    const tableLinkInfo: any = {};
    // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
    Object.keys(properties).map((key) => {
      const item = properties[key];
      // uiSchema Vô dụng
      //const uiItem = this.uiSchema[key];// method、formTemplate、url
      if (item.view == 'tab') {
        hasSubTable.value = true;
        let temp: OnlSubTab = {
          key,
          formTemplate: Number(item.formTemplate || 1),
          // Cái nàyforeignKeylà trường của bảng chính
          foreignKey: item['foreignKey'],
          describe: item.describe,
          relationType: item.relationType,
          requiredFields: item.required || [],
          order: item.order,
        };
        if (item.relationType == 1) {
          refMap[key] = ref(null);
          temp['properties'] = item.properties;
        } else {
          dealSubProerties(item);
          refMap[key] = ref<JVxeTableInstance>();
          temp['columns'] = item.columns;
          dataSourceObj[key] = [];
          showStatus[key] = false;
        }
        subInfo.push(temp);
      } else {
        if (item.view === LINK_DOWN) {
          let array = handleLinkDown(item, key);
          for (let linkDownItem of array) {
            let tempIndex = getFieldIndex(arr, linkDownItem.key);
            let temp = {
              field: linkDownItem.key,
              label: linkDownItem.title,
              view: linkDownItem.view,
              order: linkDownItem.order,
              dictTable: linkDownItem.dictTable,
              linkField: linkDownItem.linkField||'',
            };
            if (tempIndex == -1) {
              arr.push(temp);
            } else {
              arr[tempIndex] = temp;
            }
          }
        } else if (item.view == 'hidden') {
          //Cái đã ẩn không xử lý
        } else {
          let tempIndex = getFieldIndex(arr, key);
          if (tempIndex == -1) {
            // update-begin--author:liusq---date:20261102---for：onlineHiển thị nút cha của nút cấp cao nhất trong chi tiết bảng cây0, mang theopidComponent/pidValueDùng để nhận diện trường nút cha của bảng cây
            let temp = Object.assign(
              {
                field: key,
                label: item.title,
              },
              pick(item, ['view', 'order', 'fieldExtendJson', 'dictTable', 'dictText', 'dictCode', 'dict', 'pidComponent', 'pidValue'])
            );
            // update-end--author:liusq---date:20261102---for：onlineHiển thị nút cha của nút cấp cao nhất trong chi tiết bảng cây0, mang theopidComponent/pidValueDùng để nhận diện trường nút cha của bảng cây
            if (item.view == 'file') {
              temp['span'] = 24;
              temp['isFile'] = true;
            }
            if (item.view == 'image') {
              temp['span'] = 24;
              temp['isImage'] = true;
            }
            if (item.view == 'link_table') {
              // Phán đoán có phải thẻ (card) hay không
              if(item.fieldExtendJson){
                try{
                  let json = JSON.parse(item.fieldExtendJson);
                  if(json.showType!='select'){
                   // temp['span'] = 24;
                    temp['isCard'] = true;
                  }
                  if(json.multiSelect==true){
                    temp['multi'] = true;
                  }
                  // update-begin--author:liaozhiyang---date:20260811---for:【LHZP-4】onlineBản ghi liên kết của chi tiết thêm hiển thị hình ảnh, tải tệp xuống và xem trước hình ảnh
                  if(json.imageField){
                    temp['imageField'] = json.imageField;
                  }
                  // update-end--author:liaozhiyang---date:20260811---for:【LHZP-4】onlineBản ghi liên kết của chi tiết thêm hiển thị hình ảnh, tải tệp xuống và xem trước hình ảnh
                }catch (e) {
                  console.error('Phân tích (parse)jsonChuỗi ký tự xảy ra lỗi', item.fieldExtendJson)
                }
              }
            }
            if (item.view == 'umeditor' || item.view == 'markdown') {
              temp['isHtml'] = true;
              temp['span'] = 24;
            }
            arr.push(temp);
            // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
            if (item.view === 'link_table_field') {
              if (!tableLinkInfo[item.dictTable]) {
                tableLinkInfo[item.dictTable] = [];
              }
              tableLinkInfo[item.dictTable].push(`${key},${item.dictText}`);
            }
            // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
          }
        }
      }
    });
    // 1.Đối vớiarrSắp xếp
    arr.sort(function (a, b) {
      return a.order - b.order;
    });
    // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
    arr.forEach((item) => {
      if (item.view === 'link_table' && tableLinkInfo[item.field]) {
        item['linkFields'] = tableLinkInfo[item.field];
      }
    });
    // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
    // 2.Sắp xếp cho bảng con
    subInfo.sort(function (a, b) {
      return a.order - b.order;
    });
    subTabInfo.value = subInfo;
    for (let i = 0; i < arr.length; i++) {
      let temp = arr[i];
      if (temp.isFile === true || temp.isImage === true || temp.isHtml === true) {
        if (i > 0) {
          let last = arr[i - 1];
          let span = last.span || formSpan.value;
          last.span = span;
        }
      }
    }
    detailFormSchemas.value = arr;
    subDataSource.value = dataSourceObj;
    console.log('adadad', arr);
  }

  function dealSubProerties(subInfo) {
    useOnlineVxeTableColumns(subInfo);
  }

  function getFieldIndex(arr: DetailFormSchema[], key: string) {
    let index = -1;
    for (let i = 0; i < arr.length; i++) {
      let item = arr[i];
      if (item.field === key) {
        index = i;
        break;
      }
    }
    return index;
  }

  function handleLinkDown(item, field){
    let all:any[] = []
    const {
      config: { table, key, txt, linkField },
      order,
      title,
      others,
    } = item;
    let obj = {
      table, key, txt
    }
    let temp = {
      view: 'link_down',
      order,
      title,
      dictTable: JSON.stringify(obj)
    };
    all.push(Object.assign({}, {linkField, key: field}, temp));
    if(linkField){
      let arr = linkField.split(',');
      for(let a of arr){
        let title = ''
        for(let o of others){
          if(o.field==a){
            title = o.title
          }
        }
        all.push(Object.assign({}, {key: a}, temp, {title}));
      }
    }
    return all;
  }
  return {
    detailFormSchemas,
    hasSubTable,
    subTabInfo,
    refMap,
    showStatus,
    createFormSchemas,
    formSpan,
    subDataSource,
  };
}
