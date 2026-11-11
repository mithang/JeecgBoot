import { useModalInner } from '/@/components/Modal';
import { randomString } from '/@/utils/common/compUtils';
import { reactive, ref, toRaw, watch } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { createLocalStorage } from '/@/utils/cache';
import { useRoute } from 'vue-router';
import FormSchemaFactory from '../../auto/comp/factory/FormSchemaFactory';
import { FORM_VIEW_TO_QUERY_VIEW } from '../../types/onlineRender';

// Tiền tố mã lưu trữ của điều kiện truy vấn
const SAVE_CODE_PRE = 'JSuperQuerySaved_';

/**
 * Mục truy vấn
 * */
interface SuperQueryItem {
  field: string | undefined;
  rule: string | undefined;
  val: string | number;
  key: string;
}
/**
 * Mục truy vấn-Cây control đầu tiênmodel
 * */
interface TreeModel {
  title: string;
  value: string;
  isLeaf?: boolean;
  disabled?: boolean;
  children?: TreeModel[];
  order?: number;
}

/**
 * Cấu trúc lưu thông tin truy vấn
 * */
interface SaveModel {
  title: string;
  content: string;
  type: string;
}

export function useSuperQuery() {
  const { createMessage: $message } = useMessage();
  /** Biểu mẫuref*/
  const formRef = ref<any>();

  /** Dữ liệu*/
  const dynamicRowValues = reactive<{ values: SuperQueryItem[] }>({
    values: [],
  });
  /** and/or */
  const matchType = ref('and');

  // Hiển thị popup
  const [registerModal, { setModalProps }] = useModalInner(() => {
    setModalProps({ confirmLoading: false });
  });

  // Loại truy vấn nâng cao không hỗ trợ component liên động, cần thiết lập thêm của component liên độngviewlàtext
  const view2QueryViewMap = Object.assign({}, { link_down: 'text' }, FORM_VIEW_TO_QUERY_VIEW);

  /**
   * Sự kiện nút xác nhận
   */
  function handleSubmit() {
    console.log('handleSubmit', dynamicRowValues.values);
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
    console.log('setFormModel', key, value);
    // formModel[key] = value;
    item['val'] = value;
  }

  // trường-Properties
  const fieldProperties = ref<any>({});
  // trường-Mục truy vấn phía bên trái-Dữ liệu control cây
  const fieldTreeData = ref<any>([]);

  /**
   * Khởi tạo dữ liệu-Phương thức ở lúc bắt đầu nhất
   * 1.Lấy Tên bảng@tên trường-->Cấu hình một cái như thế nàymap
   * 2.Lấy dữ liệu cấu trúc dạng cây Hiển thị:văn bản; Lưu trữ:Tên bảng@tên trường
   * Khi cây thay đổi, kịp thời lấy cấu hình để cập nhật biểu mẫu
   * @param json
   */
  function init(json) {
    let { allFields, treeData } = getAllFields(json);
    fieldProperties.value = allFields;
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
      val: '',
      key: randomString(16),
    };
    if (index === false) {
      // Sau khi đặt lại cần gọi
      dynamicRowValues.values = [];
      dynamicRowValues.values.push(item);
    } else if (index === true) {
      // Khi mở popup cần gọi
      if (dynamicRowValues.values.length == 0) {
        dynamicRowValues.values.push(item);
      }
    } else {
      // Phần còn lại chính là Nhấp dấu cộng bình thường để thêm hàng
      dynamicRowValues.values.splice(++index, 0, item);
    }
  }

  /**
   * Mục truy vấn phía bên trái Xóa một hàng
   */
  function removeOne(item: SuperQueryItem) {
    let arr = toRaw(dynamicRowValues.values);
    let index = -1;
    for (let i = 0; i < arr.length; i++) {
      if (item.key == arr[i].key) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      dynamicRowValues.values.splice(index, 1);
    }
  }

  // Ô nhập mặc định
  const defaultInput = {
    field: 'val',
    label: 'Kiểm thử',
    component: 'Input',
  };

  /**
   * Mục truy vấn phía bên trái valComponent schemaLấy, Thay thế cây trường phía bên tráichangesự kiện
   * @param item
   * @param index
   */
  function getSchema(item, index) {
    let map = fieldProperties.value;
    let prop = map[item.field];
    if (!prop) {
      return defaultInput;
    }
    if (view2QueryViewMap[prop.view]) {
      // Nếu xuất hiện tình huống component liên động sinh ra từ điều kiện truy vấn, vui lòng theo dõi tại đây
      prop.view = view2QueryViewMap[prop.view];
    }
    let temp = FormSchemaFactory.createFormSchema(item.field, prop);
    // temp.setFormRef(formRef)
    temp.noChange();
    // trong điều kiện truy vấn Ô dropdownpopContainerlàparentNode
    temp.asSearchForm();
    temp.updateField(item.field + index);
    const setFieldValue = (values) => {
      item['val'] = values[item.field];
    };
    temp.setFunctionForFieldValue(setFieldValue);
    let schema = temp.getFormItemSchema();
    //schema['valueField'] = 'val'
    return schema;
  }

  /*-----------------------Liên quan đến thông tin lưu ở phía bên phải-begin---------------------------*/

  /**
   * Cây phía bên phải của Dữ liệu
   */
  const saveTreeData = ref<any>('');
  // Bộ nhớ đệm cục bộ
  const $ls = createLocalStorage();
  //Thông tin cần lưu (một mục)
  const saveInfo = reactive({
    visible: false,
    title: '',
    content: '',
    saveCode: '',
  });
  //Nútloading
  const loading = ref(false);

  // Route của trang hiện tại
  const route = useRoute();
  // Lắng nghe thông tin route, khi route thay đổi thì lấy lại thông tin truy vấn đã lưu-->currentPageSavedArray
  watch(
    () => route.fullPath,
    (val) => {
      console.log('fullpath', val);
      initSaveQueryInfoCode();
    }
  );

  // được lưu trữ của trang hiện tại Thông tin truy vấn
  const currentPageSavedArray = ref<SaveModel[]>([]);
  // Lắng nghe xem trang hiện tại có dữ liệu mới được lưu hay không, sau đó cập nhật dữ liệu phía bên phải->saveTreeData
  watch(
    () => currentPageSavedArray.value,
    (val) => {
      let temp: any[] = [];
      if (val && val.length > 0) {
        val.map((item) => {
          let key = randomString(16);
          temp.push({
            title: item.title,
            slots: { icon: 'custom' },
            value: key,
          });
        });
      }
      saveTreeData.value = temp;
    },
    { immediate: true, deep: true }
  );

  // Lấy lại thông tin truy vấn đã lưu
  function initSaveQueryInfoCode() {
    let code = SAVE_CODE_PRE + route.fullPath;
    saveInfo.saveCode = code;
    let list = $ls.get(code);
    if (list && list instanceof Array) {
      currentPageSavedArray.value = list;
    }
  }

  // Thực thi một lần Lấy thông tin truy vấn đã lưu
  initSaveQueryInfoCode();

  /**
   * Sự kiện nút lưu
   */
  function handleSave() {
    // Lấy dữ liệu thực tế chuyển thành chuỗi
    let fieldArray = getQueryInfo();
    if (!fieldArray) {
      $message.warning('Điều kiện trống không thể lưu');
      return;
    }
    let content = JSON.stringify(fieldArray);
    openSaveInfoModal(content);
  }

  // Nhập tiêu đề để lưu Hiển thị popup
  function openSaveInfoModal(content) {
    saveInfo.visible = true;
    saveInfo.title = '';
    saveInfo.content = content;
  }

  /**
   * Xác nhận lưu thông tin truy vấn
   */
  function doSaveQueryInfo() {
    let { title, content, saveCode } = saveInfo;
    let index = getTitleIndex(title);
    if (index >= 0) {
      // Đã tồn tại, có ghi đè hay không
      Modal.confirm({
        title: 'Nhắc nhở',
        content: `${title} Đã tồn tại, có ghi đè hay không?`,
        okText: 'Xác nhận',
        cancelText: 'Hủy',
        onOk: () => {
          currentPageSavedArray.value.splice(index, 1, {
            content,
            title,
            type: matchType.value,
          });
          $ls.set(saveCode, currentPageSavedArray.value);
          saveInfo.visible = false;
        },
      });
    } else {
      currentPageSavedArray.value.push({
        content,
        title,
        type: matchType.value,
      });
      $ls.set(saveCode, currentPageSavedArray.value);
      saveInfo.visible = false;
    }
  }

  // Căn cứ vào cái đã điền titleTìm thông tin lưu trữ cục bộ, nếu có thì cần hỏi có ghi đè hay không
  function getTitleIndex(title) {
    let savedArray = currentPageSavedArray.value;
    let index = -1;
    for (let i = 0; i < savedArray.length; i++) {
      if (savedArray[i].title == title) {
        index = i;
        break;
      }
    }
    return index;
  }

  /**
   * Lấy mọi điều kiện truy vấn phía bên trái, nếu không có/hoặc điều kiện không hợp lệ thì trả vềfalse
   */
  function getQueryInfo(isEmit = false) {
    let arr = dynamicRowValues.values;
    if (!arr || arr.length == 0) {
      return false;
    }
    let fieldArray: any = [];
    let fieldProps = fieldProperties.value;
    for (let item of arr) {
      if (item.field && (item.val || item.val === 0) && item.rule) {
        let tempVal: any = toRaw(item.val);
        if (tempVal instanceof Array) {
          tempVal = tempVal.join(',');
        }
        let obj = {
          field: item.field,
          rule: item.rule,
          val: tempVal,
        };
        if (isEmit === true) {
          //Nếu dữ liệu hiện tại được dùng choemitsự kiện, cần thiết lậpdbtypevàtype
          let prop = fieldProps[item.field];
          if (prop) {
            obj['type'] = prop.view;
            obj['dbType'] = prop.type;
          }
        }
        fieldArray.push(obj);
      }
    }
    if (fieldArray.length == 0) {
      return false;
    }
    return fieldArray;
  }

  /**
   * Dữ liệu phía bên phải Sự kiện nhấp, hiển thị lại dữ liệu sang phía bên trái
   * @param key
   * @param node
   */
  function handleTreeSelect(key, { node }) {
    console.log(key, node);
    let title = node.dataRef.title;
    let arr = currentPageSavedArray.value.filter((item) => item.title == title);
    if (arr && arr.length > 0) {
      // Lấy dữ liệu để render
      let { content, type } = arr[0];
      let data = JSON.parse(content);
      let rowsValues: SuperQueryItem[] = [];
      for (let item of data) {
        rowsValues.push(Object.assign({}, { key: randomString(16) }, item));
      }
      dynamicRowValues.values = rowsValues;
      matchType.value = type;
    }
  }

  /**
   * Dữ liệu phía bên phải Sự kiện xóa
   */
  function handleRemoveSaveInfo(title) {
    console.log(title);
    let index = getTitleIndex(title);
    if (index >= 0) {
      currentPageSavedArray.value.splice(index, 1);
      $ls.set(saveInfo.saveCode, currentPageSavedArray.value);
    }
  }

  /*-----------------------Liên quan đến thông tin lưu ở phía bên phải-end---------------------------*/

  // Lấy thông tin mọi cấu hình trường
  function getAllFields(json) {
    // Lấy mọi cấu hình Trường truy vấn Có truy vấn liên hợp hay không
    const { properties, table, title } = json;
    let allFields = {};
    let order = 1;
    let treeData: TreeModel[] = [];
    let mainNode: TreeModel = {
      title,
      value: table,
      disabled: true,
      children: [],
    };
    treeData.push(mainNode);
    Object.keys(properties).map((field) => {
      let item = properties[field];
      if (item.view == 'table') {
        // trường của bảng con
        // Chỉ khi bật truy vấn liên hợp mới cần trường của bảng con làm điều kiện truy vấn
        let subProps = item['properties'];
        let subTableOrder = order * 100;
        let subNode: TreeModel = {
          title: item.title,
          value: field,
          disabled: true,
          children: [],
        };
        Object.keys(subProps).map((subField) => {
          let subItem = subProps[subField];
          // Đảm bảo sắp xếp thống nhất
          subItem['order'] = subTableOrder + subItem['order'];
          // Dấu phân tách của bảng con phải đổi thành dấu phẩy, backend mới nhận diện được
          let subFieldKey = field + ',' + subField;
          allFields[subFieldKey] = subItem;
          subNode.children!.push({
            title: subItem.title,
            value: subFieldKey,
            isLeaf: true,
            order: subItem['order'],
          });
        });
        orderField(subNode);
        treeData.push(subNode);
        order++;
      } else {
        // trường bảng chính
        let fieldKey = table + '@' + field;
        allFields[fieldKey] = item;
        mainNode.children!.push({
          title: item.title,
          value: fieldKey,
          isLeaf: true,
          order: item.order,
        });
      }
    });
    orderField(mainNode);
    return { allFields, treeData };
  }

  //Căn cứ vào của trườngorderSắp xếp lại
  function orderField(data) {
    let arr = data.children;
    arr.sort(function (a, b) {
      return a.order - b.order;
    });
  }

  function initDefaultValues(values) {
    const { params, matchType } = values;
    if (params) {
      let rowsValues: SuperQueryItem[] = [];
      for (let item of params) {
        rowsValues.push(Object.assign({}, { key: randomString(16) }, item));
      }
      dynamicRowValues.values = rowsValues;
      matchType.value = matchType;
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
  };
}
