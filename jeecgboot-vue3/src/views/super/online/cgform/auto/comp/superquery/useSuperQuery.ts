import { useModalInner } from '/@/components/Modal';
import { randomString } from '/@/utils/common/compUtils';
import { reactive, ref, toRaw, watch } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { createLocalStorage } from '/@/utils/cache';
import { useRoute } from 'vue-router';
import FormSchemaFactory from '../factory/FormSchemaFactory';
import {useExtendComponent} from '../../../hooks/auto/useExtendComponent'
import { cloneDeep } from 'lodash-es';
/**
 * Chuyển loại biểu mẫu thành loại truy vấn
 * Sự khác biệt giữa component truy vấn thông thường và truy vấn nâng cao : truy vấn nâng cao không hỗ trợ component liên động
 */
const FORM_VIEW_TO_QUERY_VIEW = {
  "password": "text",
  "file": "text",
  "image": "text",
  "textarea": "text",
  "umeditor": "text",
  "markdown": "text",
  "checkbox": "list_multi",
  "radio": "list",
}

// Tiền tố mã lưu trữ của điều kiện truy vấn
const SAVE_CODE_PRE = 'JSuperQuerySaved_';

/**
 * Mục truy vấn
 * */
interface SuperQueryItem {
  field: string|undefined;
  rule: string|undefined;
  val: string|number;
  key: string;
  // Giải quyếtinputNumberVấn đề kiểu dáng component không thẳng hàng
  curLineAlign: string | undefined;
  fileType: string;
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
  view: string;
  // Loại component gốc đầu tiên nhất;viewTrường có thể bị thay đổi
  originView?: string;
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
}
/**
 * Mục truy vấn-Cây control đầu tiênmodel
 * */
interface TreeModel {
  title: string,
  value: string,
  isLeaf?: boolean,
  disabled?: boolean,
  children?: TreeModel[],
  order?: number,
  fieldType?: string;
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
  view: string;
  originView?: string;
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
}

/**
 * Cấu trúc lưu thông tin truy vấn
 * */
interface SaveModel{
  title: string,
  content: string,
  type: string,
}

export function useSuperQuery(props){
  // Thêm component biểu mẫu
  const {linkTableCard2Select} = useExtendComponent();
  
  const { createMessage: $message } = useMessage();
  /** Biểu mẫuref*/
  const formRef = ref<any>();
  
  /** Dữ liệu*/
  const dynamicRowValues = reactive<{ values: SuperQueryItem[] }>({
    values: [],
  });
  /** and/or */
  const matchType = ref('and');

  // Nút xác nhận của popup lưu truy vấnloadingTrạng thái
  const saveModalLoading = ref(false);
  // Hiển thị popup
  const [registerModal, {setModalProps}] = useModalInner(() => {
    setModalProps({confirmLoading: false});
  })
  
  // Loại truy vấn nâng cao không hỗ trợ component liên động, cần thiết lập thêm của component liên độngviewlàtext
  const view2QueryViewMap = Object.assign({}, {"link_down":"text"}, FORM_VIEW_TO_QUERY_VIEW)
  
  /**
   * Sự kiện nút xác nhận
   */
  function handleSubmit() {
    console.log('handleSubmit', dynamicRowValues.values)
  }

  /**
   * Sự kiện nút đóng
   */
  function handleCancel() {
    //closeModal();
  }
  
  /**
   * valGán giá trị cho component
   */
  function setFormModel(key: string, value: any, item: any) {
    console.log('setFormModel', key, value)
   // formModel[key] = value;
    item['val'] = value;
  }

  // trường-Properties
  const fieldProperties = ref<any>({})
  // trường-Mục truy vấn phía bên trái-Dữ liệu control cây
  const fieldTreeData = ref<any>([])
  // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-503】Lọc các component hình ảnh, tệp, mật khẩu
  const filterComponent = (data) => {
    const { properties = {} } = data;
    Object.entries(properties).forEach(([field, value]) => {
      if (value.view === 'table') {
        filterComponent(value);
      }
      // update-begin--author:liaozhiyang---date:20260713---for: lọc bỏ trường của bảng khác(link_table_field), không làm điều kiện truy vấn nâng cao
      if (['link_down', 'link_table_field'].includes(value.originView || value.view)) {
        delete properties[field];
      }
      // update-end--author:liaozhiyang---date:20260713---for: lọc bỏ trường của bảng khác(link_table_field), không làm điều kiện truy vấn nâng cao
    });
  };
  // update-end--author:liaozhiyang---date:20240607---for：【TV360X-503】Lọc các component hình ảnh, tệp, mật khẩu
  /**
   * Khởi tạo dữ liệu-Phương thức ở lúc bắt đầu nhất
   * 1.Lấy Tên bảng@tên trường-->Cấu hình một cái như thế nàymap
   * 2.Lấy dữ liệu cấu trúc dạng cây Hiển thị:văn bản; Lưu trữ:Tên bảng@tên trường
   * Khi cây thay đổi, kịp thời lấy cấu hình để cập nhật biểu mẫu
   * @param json
   */
  function init(json) {
    console.log('=============')
    console.log('=============', json)
    console.log('=============')
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-503】Lọc các component hình ảnh, tệp, mật khẩu
    filterComponent(json);
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-503】Lọc các component hình ảnh, tệp, mật khẩu
    let { allFields, treeData } = getAllFields(json);
    fieldProperties.value = allFields;
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】Khi có bảng con, cấu trúc hóa bảng chính và rút gọn khi quá dài
    const properties = json.properties ?? {};
    const subTable: string[] = [];
    const tableName = json.table;
    Object.entries(properties).forEach(([key, value]: [string, any]) => {
      if (value.view === 'table') {
        subTable.push(key);
      }
    });
    if (subTable.length) {
      let arr: TreeModel[] = [];
      arr = treeData.filter((item) => !subTable.includes(item.value));
      for (let i = 0, len = treeData.length; i < len; i++) {
        const item = treeData[i];
        if (!subTable.includes(item.value)) {
          treeData.splice(i, 1);
          i--;
          len--;
        }
      }
      treeData.unshift({ title: 'bảng chính', value: tableName, disabled: true, order: 200, children: arr, view: 'table' });
    }
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】Khi có bảng con, cấu trúc hóa bảng chính và rút gọn khi quá dài
    fieldTreeData.value = treeData;
  }

  /**
   * Mục truy vấn phía bên trái Thêm một hàng
   * @param index
   */
  function addOne(index) {
    let item = {
      field: undefined,
      rule: 'eq',
      val:'',
      key: randomString(16)
    }
    if(index===false){
      // Sau khi đặt lại cần gọi
      dynamicRowValues.values = []
      dynamicRowValues.values.push(item)
    }else if(index===true){
      // Khi mở popup cần gọi
      if(dynamicRowValues.values.length==0){
        dynamicRowValues.values.push(item)
      }
    }else{
      // Phần còn lại chính là Nhấp dấu cộng bình thường để thêm hàng
      dynamicRowValues.values.splice(++index, 0, item)
    }
  }

  /**
   * Mục truy vấn phía bên trái Xóa một hàng
   */
  function removeOne(item: SuperQueryItem) {
    let arr = toRaw(dynamicRowValues.values);
    let index = -1;
    for(let i=0;i<arr.length;i++){
      if(item.key == arr[i].key){
        index = i;
        break;
      }
    }
    if(index != -1){
      dynamicRowValues.values.splice(index, 1);
    }
  }

  // Ô nhập mặc định
  const defaultInput = {
    field: "val",
    label: "Kiểm thử",
    component:"Input"
  }

  /**
   * Mục truy vấn phía bên trái valComponent schemaLấy, Thay thế cây trường phía bên tráichangesự kiện
   * @param item
   * @param index
   */
  function getSchema(item, index) {
    let map = fieldProperties.value
    let prop = map[item.field]
    if(!prop){
      return defaultInput
    }
    if(view2QueryViewMap[prop.view]){
      // Nếu xuất hiện tình huống component liên động sinh ra từ điều kiện truy vấn, vui lòng theo dõi tại đây
      prop.view = view2QueryViewMap[prop.view]
    }
    let temp = FormSchemaFactory.createFormSchema(item.field, prop)
   // temp.setFormRef(formRef)
    temp.noChange()
    // trong điều kiện truy vấn Ô dropdownpopContainerlàparentNode
    temp.asSearchForm();
    temp.updateField(item.field+index)
    const setFieldValue = (values)=>{
      item['val'] = values[item.field]
    }
    temp.setFunctionForFieldValue(setFieldValue)
    let schema = temp.getFormItemSchema()
    //schema['valueField'] = 'val'
    // Quy tắc đặc biệt, cần vô hiệu hóa component
    // Rỗng, không rỗng
    if (['empty', 'not_empty'].includes(item.rule)) {
      schema.componentProps = { ...schema.componentProps, disabled: true };
    }
    linkTableCard2Select(schema);
    // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
    if (schema.component === 'LinkTableSelect') {
      let componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, editBtnShow: false };
      // update-begin--author:scott---date:20260713---for: truy vấn nâng cao chọn nhiều-Bản ghi liên kết ở"tại...trong"/"Bằng"Ngữ nghĩa ở dưới là bao hàm nhiều giá trị, bắt buộc chọn nhiều, không bị giới hạn bởi cấu hình chọn đơn của trường
      if (['in', 'eq'].includes(item.rule)) {
        schema.componentProps.multi = true;
      }
      // update-end--author:scott---date:20260713---for: truy vấn nâng cao chọn nhiều-Bản ghi liên kết ở"tại...trong"/"Bằng"Ngữ nghĩa ở dưới là bao hàm nhiều giá trị, bắt buộc chọn nhiều, không bị giới hạn bởi cấu hình chọn đơn của trường
    }
    // update-end--author:liaozhiyang---date:20240607---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
    // update-begin--author:liaozhiyang---date:20231219---for：【QQYUN-7640】Component số trong truy vấn nâng cao bị lệch
    if (schema && schema.component === 'InputNumber') {
      item.curLineAlign = 'start';
    }
    // update-end--author:liaozhiyang---date:20231219---for：【QQYUN-7640】Component số trong truy vấn nâng cao bị lệch
    // update-begin--author:liaozhiyang---date:20240223---for：【QQYUN-8229】Chọn nâng cao: dropdown cây tùy chỉnh hiển thị không đầy đủ
    if (schema?.component === 'JTreeSelect') {
      let componentProps: any = schema.componentProps;
      if (componentProps) {
        componentProps.getPopupContainer = () => document.body
      } else {
        schema.componentProps = { getPopupContainer: () => document.body }
      };
    }
    // update-end--author:liaozhiyang---date:20240223---for：【QQYUN-8229】Chọn nâng cao: dropdown cây tùy chỉnh hiển thị không đầy đủ
    // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-499】Component công tắc truy vấn nâng cao đổi thành dropdown, component người dùng không hiển thị nút
    if (schema?.component === 'JSwitch') {
      const componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, query: true };
    }
    if (schema?.component === 'JSelectUser') {
      const componentProps = schema.componentProps ?? {};
      schema.componentProps = { ...componentProps, showButton: false };
    }
    // update-end--author:liaozhiyang---date:20240529---for：【TV360X-499】Công tắc truy vấn nâng cao đổi thành dropdown, component người dùng không hiển thị nút
    return schema
  }

  /*-----------------------Liên quan đến thông tin lưu ở phía bên phải-begin---------------------------*/

  /**
   * Cây phía bên phải của Dữ liệu
   */
  const saveTreeData = ref<any>('')
  // Bộ nhớ đệm cục bộ
  const $ls = createLocalStorage();
  //Thông tin cần lưu (một mục)
  const saveInfo = reactive({
    visible: false,
    title: '',
    content: '',
    saveCode: ''
  });
  //Nútloading
  const loading = ref(false)

  // Route của trang hiện tại
  const route = useRoute();
  // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
  if (props.isCustomSave) {
    watch(props.saveSearchData, () => {
      currentPageSavedArray.value = props.saveSearchData;
    });
  } else {
    // Lắng nghe thông tin route, khi route thay đổi thì lấy lại thông tin truy vấn đã lưu-->currentPageSavedArray
    watch(()=>route.fullPath, (val)=>{
      console.log('fullpath', val);
        initSaveQueryInfoCode();
    });
  }
  // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
  
  // được lưu trữ của trang hiện tại Thông tin truy vấn
  const currentPageSavedArray = ref<SaveModel[]>([]);
  // Lắng nghe xem trang hiện tại có dữ liệu mới được lưu hay không, sau đó cập nhật dữ liệu phía bên phải->saveTreeData
  watch(()=>currentPageSavedArray.value, (val)=>{
    let temp:any[] = []
    if(val && val.length>0){
      val.map(item=>{
        let key = randomString(16)
        temp.push({
          title: item.title,
          slots: { icon: 'custom' },
          value: key
        })
      })
    }
    saveTreeData.value = temp
  }, {immediate:true, deep: true})


  // Lấy lại thông tin truy vấn đã lưu
  function initSaveQueryInfoCode(){
    // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
    if (props.isCustomSave) {
      currentPageSavedArray.value = cloneDeep(props.saveSearchData);
    } else {
      let code = SAVE_CODE_PRE + route.fullPath;
      saveInfo.saveCode = code;
      let list = $ls.get(code);
      if(list && list instanceof Array){
        currentPageSavedArray.value = list
      }
    }
    // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
  }

  // Thực thi một lần Lấy thông tin truy vấn đã lưu
  initSaveQueryInfoCode();
  
  /**
   * Sự kiện nút lưu
   */
  function handleSave(){
    // Lấy dữ liệu thực tế chuyển thành chuỗi
    let fieldArray = getQueryInfo();
    if(!fieldArray){
      $message.warning('Điều kiện trống không thể lưu')
      return;
    }
    let content = JSON.stringify(fieldArray)
    openSaveInfoModal(content)
  }

  // Nhập tiêu đề để lưu Hiển thị popup
  function openSaveInfoModal(content){
    saveInfo.visible = true;
    saveInfo.title = '';
    saveInfo.content = content
  }

  /**
   * Xác nhận lưu thông tin truy vấn
   */
  function doSaveQueryInfo(){
    let { title, content, saveCode } = saveInfo;
    let index = getTitleIndex(title);
    const saveSearchCondition = (type) => {
      // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
      const curPageSave: any = cloneDeep(currentPageSavedArray.value);
      saveModalLoading.value = true;
      if (type) {
        // Ghi đè cái đã có
        curPageSave.splice(index, 1, {
          content,
          title,
          type: matchType.value,
        });
      } else {
        curPageSave.push({
          content,
          title,
          type: matchType.value,
        });
      }
      const run = () => {
        saveInfo.visible = false;
        $message.success('Lưu thành công');
        currentPageSavedArray.value = curPageSave;
        saveModalLoading.value = false;
      };
      if (props.isCustomSave) {
        props
          .save(curPageSave, 0)
          .then(() => {
            run();
          })
          .catch((err) => {
            saveModalLoading.value = false;
          });
      } else {
        // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8357】Cache điều kiện truy vấn đã lưu của truy vấn nâng cao đổi thành một tháng
        const expire = 60 * 60 * 24 * 30;
        // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8357】Cache điều kiện truy vấn đã lưu của truy vấn nâng cao đổi thành một tháng
        $ls.set(saveCode, curPageSave, expire);
        run();
      }
      // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
    };
    if (index >= 0) {
      // Đã tồn tại, có ghi đè hay không
      Modal.confirm({
        title: 'Nhắc nhở',
        content: `${title} Đã tồn tại, có ghi đè hay không?`,
        okText: 'Xác nhận',
        cancelText: 'Hủy',
        onOk: () => {
          saveSearchCondition(1);
        },
      });
    } else {
      saveSearchCondition(0);
    }
  }

  // Căn cứ vào cái đã điền titleTìm thông tin lưu trữ cục bộ, nếu có thì cần hỏi có ghi đè hay không
  function getTitleIndex(title){
    let savedArray = currentPageSavedArray.value
    let index = -1;
    for(let i=0;i<savedArray.length;i++){
      if(savedArray[i].title == title){
        index = i;
        break;
      }
    }
    return index;
  }
  
  /**
   * Lấy mọi điều kiện truy vấn phía bên trái, nếu không có/hoặc điều kiện không hợp lệ thì trả vềfalse
   */
  function getQueryInfo(isEmit = false){
    let arr = dynamicRowValues.values;
    if(!arr || arr.length==0){
      return false;
    }
    let fieldArray:any = [];
    let fieldProps = fieldProperties.value;
    for(let item of arr){
      let allowEmpty = ['empty', 'not_empty'].includes(item.rule!)
      if(item.field && (allowEmpty || item.val || item.val===0) && item.rule){
        let prop = fieldProps[item.field]
        // Dữ liệu định dạng hóa tùy chỉnh
        let formatValue = prop?.formatValue ?? (v => v)
        let tempVal:any = toRaw(item.val)
        if(tempVal instanceof Array){
          tempVal = tempVal.map(v => formatValue(v)).join(',')
        } else {
          tempVal = formatValue(tempVal)
        }
        let fieldName = getRealFieldName(item)
        let obj = {
          field: fieldName,
          rule: item.rule,
          val: tempVal,
          fileType: item.fileType ,
        };
        if(isEmit===true){
          //Nếu dữ liệu hiện tại được dùng choemitsự kiện, cần thiết lậpdbtypevàtype
          let prop = fieldProps[item.field]
          if(prop){
            obj['type'] = prop.view
            obj['dbType'] = prop.type
          }
        }
        fieldArray.push(obj)
      }
    }
    if(fieldArray.length==0){
      return false;
    }
    return fieldArray
  }

  //update-begin-author:taoyan date:2022-5-31 for: VUEN-1148 Trong liên động chính-con, truy vấn nâng cao tra dữ liệu bảng con, không có hiệu lực
  /**
   * Tham số truy vấn nâng cao tên trường
   * Lấy cái backend cần Định dạng tên trường: tên bảng,tên trường
   * @param item
   */
  function getRealFieldName(item){
    let fieldName = item.field
    if(fieldName.indexOf('@')>0){
      fieldName = fieldName.replace('@', ',')
    }
    return fieldName;
  }
  //update-end-author:taoyan date:2022-5-31 for: VUEN-1148 Trong liên động chính-con, truy vấn nâng cao tra dữ liệu bảng con, không có hiệu lực

  /**
   * Dữ liệu phía bên phải Sự kiện nhấp, hiển thị lại dữ liệu sang phía bên trái
   * @param key
   * @param node
   */
  function handleTreeSelect(key, {node}){
    console.log(key, node)
    let title = node.dataRef.title
    let arr = currentPageSavedArray.value.filter(item=>item.title==title)
    if(arr && arr.length>0){
      // Lấy dữ liệu để render
      let { content, type } = arr[0]
      let data = JSON.parse(content)
      let rowsValues: SuperQueryItem[] = []
      for(let item of data){
        // update-begin--author:liaozhiyang---date:20240108---for：【issues/962】Truy vấn đã lưu của truy vấn nâng cao là bảng con, lần sau truy vấn không ra kết quả
        item.field = item.field.replace(',','@');
        // update-end--author:liaozhiyang---date:20240108---for：【issues/962】Truy vấn đã lưu của truy vấn nâng cao là bảng con, lần sau truy vấn không ra kết quả
        rowsValues.push(Object.assign({}, {key: randomString(16)}, item))
      }
      dynamicRowValues.values = rowsValues
      matchType.value = type
    }
  }

  /**
   * Dữ liệu phía bên phải Sự kiện xóa
   */
  function handleRemoveSaveInfo(title){
    console.log(title)
    let index = getTitleIndex(title)
    if(index>=0){
      // update-begin--author:liaozhiyang---date:20240513---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
      if (props.isCustomSave) {
        const curPageSave = cloneDeep(currentPageSavedArray.value);
        curPageSave.splice(index, 1);
        props
          .save(curPageSave, 1)
          .then(() => {
            currentPageSavedArray.value = curPageSave;
          })
          .catch((err) => {
            console.log(`Xóa chứ gì~，${err}`);
          });
      } else {
        currentPageSavedArray.value.splice(index, 1);
        $ls.set(saveInfo.saveCode, currentPageSavedArray.value);
      }
      // update-end--author:liaozhiyang---date:20240513---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
    }
  }

  /*-----------------------Liên quan đến thông tin lưu ở phía bên phải-end---------------------------*/

  // Lấy thông tin mọi cấu hình trường
  function getAllFields(properties){
    // Lấy mọi cấu hình Trường truy vấn Có truy vấn liên hợp hay không
   // const {properties, table, title } = json;
    let allFields = {}
    let order = 1;
    let treeData:TreeModel[] = []
 /*   let mainNode:TreeModel = {
      title,
      value: table,
      disabled: true,
      children: []
    };*/
    //treeData.push(mainNode)
    if(properties.properties){
      properties = properties.properties
    }
    Object.keys(properties).map(field=>{
      let item = properties[field]
      if(item.view == 'table'){
        // trường của bảng con
        // Chỉ khi bật truy vấn liên hợp mới cần trường của bảng con làm điều kiện truy vấn
        let subProps = item['properties'] || item['fields']
        let subTableOrder = order * 100;
        let subNode:TreeModel = {
          title: item.title,
          value: field,
          disabled: true,
          children: [],
          order: subTableOrder,
          // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
          fieldType: item.type,
          // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
        }
        Object.keys(subProps).map(subField=>{
          let subItem = subProps[subField];
          // Đảm bảo sắp xếp thống nhất
          subItem['order'] = subTableOrder + subItem['order']
          let subFieldKey = field+'@'+subField
          allFields[subFieldKey] = subItem
          subNode.children!.push({
            title: subItem.title,
            value: subFieldKey,
            isLeaf: true,
            order: subItem['order'],
            // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
            fieldType: subItem.type,
            view: subItem.view,
            originView: subItem.view,
            // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
          })
        });
        orderField(subNode);
        treeData.push(subNode);
        order++;
      }else{
        // trường bảng chính
        //let fieldKey = table+'@'+field
        let fieldKey = field
        allFields[fieldKey] = item
        treeData.push({
          title: item.title,
          value: fieldKey,
          isLeaf: true,
          order: item.order,
          // update-begin--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
          fieldType: item.type,
          view: item.view,
          originView: item.view,
          // update-end--author:liaozhiyang---date:20240306---for：【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
        });
      }
    });
    orderField(treeData);
    return {allFields, treeData}
  }
  
  //Căn cứ vào của trườngorderSắp xếp lại
  function orderField(data){
    let arr = data.children || data;
    arr.sort(function (a, b) {
      return a.order - b.order
    });
  }

  function initDefaultValues(values) {
    const { params, matchType } = values
    if(params){
      let rowsValues: SuperQueryItem[] = []
      for(let item of params){
        rowsValues.push(Object.assign({}, {key: randomString(16)}, item))
      }
      dynamicRowValues.values = rowsValues
      matchType.value = matchType
    }
  }
  
  return {
    formRef,
    init,
    dynamicRowValues,
    matchType,
    registerModal,
    handleSubmit,
    handleCancel,
    handleSave,
    doSaveQueryInfo,
    saveInfo,
    saveTreeData,
    handleRemoveSaveInfo,
    handleTreeSelect,
    fieldTreeData,
    addOne,
    removeOne,
    setFormModel,
    getSchema,
    loading,
    getQueryInfo,
    initDefaultValues,
    saveModalLoading,
    fieldProperties,
  }
}
