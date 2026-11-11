import { useModalInner } from '/@/components/Modal';
import { computed, nextTick, reactive, ref, unref } from 'vue';
import { getRefPromise } from '../../hooks/auto/useAutoForm';
import { defHttp } from '/@/utils/http/axios';
import {ONL_FORM_TABLE_NAME} from '../../types/onlineRender'
import { useAppInject } from '/@/hooks/web/useAppInject';
/**
 * TạoonlineDùng cho popup biểu mẫu
 */
export function useAutoModal(isBpm?: boolean, { emit } = {} as any, callback?:any) {
  const onlineFormCompRef = ref<any>(null);
  // Có ẩn nút xác nhận hay không
  const disableSubmit = ref(false);
  //Phong cách biểu mẫu 1cột 2cột 3cột quyết định độ rộng của popup
  const formTemplate = ref(1);
  // Nút tùy chỉnh
  const cgButtonList = ref([]);
  // jsTăng cường
  //const enhanceJsObject = ref<any>('')
  // Biểu mẫu đã render xong hay chưa
  const formRendered = ref(false);
  // Độ rộng tối thiểu của popup biểu mẫu Phụ thuộc vào cấu hình mở rộng
  const modalMinWidth = ref(0);
  // Phán đoán có phải biểu mẫu dạng cây hay không- 【VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống】
  const isTreeForm = ref(false);
  // Nếu là cây, nút chaidTên trường tương ứng của trường-【VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống】
  const pidFieldName = ref('');
  // VUEN-1105 Biểu mẫu Nút xác nhận có thể nhấp nhiều lần, lưu nhiều bản ghi dữ liệu
  const submitLoading = ref(false);
  // Có phải trang sửa đổi hay không
  const isUpdate = ref(false);
  // Có phải bảng đơn hay không
  const single = ref(true);
  // extConfigJson
  const extConfigJson = reactive<any>({});
  // Hiển thị bảng con- Trang chi tiếtmodal cũng sẽ gọi cái nàyhook, cho trang chi tiết dùng
  const showSub = ref(true);
  const customTitle = ref('')
  // Sau khi lưu biểu mẫu xong có đóng hay khôngmodal
  const successThenClose = ref(true);
  // Nhắc có lưu hay không
  const topTipVisible = ref(false)
  // Kiểm soát chiều cao popup
  const { popModalFixedWidth, resetBodyStyle, popBodyStyle } = useFixedHeightModal();
  // Không có quyền chỉnh sửa: Mặc địnhfalse
  const FORM_DISABLE_UPDATE = ref(false);
  // Loại mẫu giao diện
  const themeTemplate = ref('');

  const { getIsMobile } = useAppInject();
  const modalObject = {
    handleOpenModal: (_data) => {},
  };

  //Tham số vùng bình luận
  const tableId = ref('')
  const tableName = ref('')
  const formDataId = ref('')
  const enableComment = ref(false);
  let onlineExtConfig = {}

  //Tiêu đề popup
  const title = computed(() => {
    let temp = customTitle.value;
    if(temp){
      return temp;
    }
    if (unref(disableSubmit) === true) {
      return 'Chi tiết';
    }
    if (unref(isUpdate) === true) {
      return 'Chỉnh sửa';
    }
    return 'Thêm mới';
  });

  // Hiển thị popup Kích hoạtonlineFormCompRef---show
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    customTitle.value = '';
    topTipVisible.value = false;
    if (isBpm === true) {
      await modalObject.handleOpenModal(data);
    } else {
      await handleOpenOnlineModal(data);
    }
    // Điều chỉnhmodalChiều rộng và chiều cao
    resetBodyStyle();
    if(callback){
      callback();
    }
  });

  /**
   * Trường dùng để kiểm soát bản ghi liên kết Quyền popup trên danh sách, nếu không có quyền chỉnh sửa, chỉ có thể mở trang chi tiết
   */
  const loadItemSuccess = ref(false);
  async function getFormStatus(){
    await getRefPromise(loadItemSuccess);
    return FORM_DISABLE_UPDATE.value;
  }
  
  
  async function handleOpenOnlineModal(data) {
    setModalProps({ confirmLoading: false });
    isUpdate.value = data.isUpdate;
    disableSubmit.value = data.disableSubmit || false;
    // hrefCái chuyển hướng không cần xem bảng con
    if(data?.hideSub===true){
      showSub.value = false;
    }
    // Thiết lập tiêu đề, hỗ trợ đọc từ tham số truyền vào
    if(data?.title){
      customTitle.value = data.title;
    }
    if(data?.record){
      formDataId.value = data.record.id;
    }else{
      formDataId.value = ''
    }
    await nextTick(async () => {
      await getRefPromise(formRendered);
      
      //Bắt buộc phải chờformRenderedsau đó mới có thể gọi
      handleCommentConfig();
      await onlineFormCompRef.value.show(data?.isUpdate, data?.record, data?.param);
    });
  }

  // Render hoàn tất, thay đổi trạng thái
  function renderSuccess(extConfig) {
    formRendered.value = true;
    modalMinWidth.value = extConfig.modalMinWidth;
    if (extConfig.modelFullscreen == 1) {
      //Nếu toàn màn hình
      setModalProps({ defaultFullscreen: true });
    } else {
      setModalProps({ defaultFullscreen: false });
    }
    onlineExtConfig = extConfig
    // update-begin--author:liaozhiyang---date:20230327---for：【QQYUN-8644】Hiệu ứng phía di động khi đóng cửa sổ trò chuyện (trang chi tiết)
    if (getIsMobile.value) {
      onlineExtConfig['commentStatus'] = 0;
    }
    // update-end--author:liaozhiyang---date:20230327---for：【QQYUN-8644】Hiệu ứng phía di động khi đóng cửa sổ trò chuyện (trang chi tiết)
  }

  // Cấu hình liên quan vùng bình luận
  function handleCommentConfig(){
    let dataIdValue = formDataId.value;
    //Nếu có cấu hình bình luận và Chỉnh sửa/Trang chi tiết mới cần bật bình luận
    if(onlineExtConfig['commentStatus'] == 1 && dataIdValue){
      enableComment.value = true;
      setModalProps({ defaultFullscreen: true });
    }else{
      enableComment.value = false;
    }
  }

  const singleWidth = 800;
  const one2ManyWidth = 1100;
  const modalWidth = computed(() => {
    // Số cột khác nhau thì hiển thị độ rộng khác nhau
    let diff = 200 * (formTemplate.value - 1);
    // Giá trị cơ sở cộng thêm ngưỡng
    let width = (!unref(single) ? one2ManyWidth : singleWidth) + diff;
    // Khi văn bản quá dài, sẽ che khuất trang 【issues/I44F0R】
    width = calcModalMixWidth(width);
    let minWidth = modalMinWidth.value;
    console.log({ minWidth });
    //Phán đoán độ rộng đã tính toán ra có nhỏ hơn tham số độ rộng trong cấu hình mở rộng hay không Nếu là lấy tham số của cấu hình mở rộng
    if (minWidth && width < minWidth) {
      width = minWidth;
    }
    console.log({ width });
    return width;
  });

  /** Tính độ rộng tối thiểu của popup */
  function calcModalMixWidth(width) {
    let minWidth = extConfigJson.modalMinWidth;
    if (minWidth != null && minWidth !== '') {
      try {
        minWidth = Number.parseInt(minWidth);
        if (width < minWidth) {
          return minWidth;
        }
      } catch {
        console.warn('error modalMinWidth value: ', minWidth);
      }
    }
    return width;
  }

  // Nút tùy chỉnh Sự kiện kích hoạt phần tăng cường
  function handleCgButtonClick(optType, buttonCode) {
    onlineFormCompRef.value.handleCgButtonClick(optType, buttonCode);
  }

  function handleSubmit() {
    //update-begin-author:taoyan date:2022-5-25 for:  VUEN-1105 Biểu mẫu Nút xác nhận có thể nhấp nhiều lần, lưu nhiều bản ghi dữ liệu
    submitLoading.value = true;
    setTimeout(() => {
      submitLoading.value = false;
    }, 1500);
    //update-end-author:taoyan date:2022-5-25 for:  VUEN-1105 Biểu mẫu Nút xác nhận có thể nhấp nhiều lần, lưu nhiều bản ghi dữ liệu
    onlineFormCompRef.value.handleSubmit();
  }

  function handleCancel() {
    closeModal();
  }

  function loadFormItems(id, params={}) {
    let url = `/online/cgform/api/getFormItem/${id}`;
    return new Promise((resolve, reject) => {
      defHttp
        .get({ url, params }, { isTransformResponse: false })
        .then((res) => {
          console.log('Kết quả biểu mẫu 》》modal:', res);
          if (res.success) {
            resolve(res.result);
          } else {
            reject(res.message);
          }
        })
        .catch(() => {
          reject();
        });
    });
  }

  async function handleFormConfig(id, params, callBack?, taskId?, currentTableName?) {
    // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
    let result: any = null;
    if (taskId && currentTableName) {
      const url = `/online/cgform/api/getFormItemBytbname/${currentTableName}`;
      const params = { taskId };
      result = await defHttp.get({ url, params });
    } else {
      result = await loadFormItems(id, params);
    }
    // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
    // modalTrang chỉ xử lý nút,JSTăng cường, độ rộng popup
    let dataFormTemplate = result.head.formTemplate;
    formTemplate.value = dataFormTemplate ? Number(dataFormTemplate) : 1;
    cgButtonList.value = result.cgButtonList;
    isTreeForm.value = result.head.isTree === 'Y';
    pidFieldName.value = result.head.treeParentIdField || '';
    tableId.value = result.head.id;
    tableName.value = result.head.tableName;
    themeTemplate.value = result.head.themeTemplate;
    //enhanceJsObject.value = initCgEnhanceJs(result.enhanceJs)
    if(result['form_disable_update']===true){
      FORM_DISABLE_UPDATE.value = true
    }else{
      FORM_DISABLE_UPDATE.value = false;
    }
    loadItemSuccess.value = true;
    emit && emit('formConfig', result);
    // -update-begin--author:liaozhiyang---date:20230823---for：【QQYUN-6305】tabChủ đề một-nhiều--
    callBack && callBack(result);
    // -update-end--author:liaozhiyang---date:20230823---for：【QQYUN-6305】tabChủ đề một-nhiều--
    await nextTick(async () => {
      let myForm = (await getRefPromise(onlineFormCompRef)) as any;
      await myForm.createRootProperties(result);
    });
  }

  /**
   * Sự kiện sau khi lưu biểu mẫu xong
   */
  function handleSuccess(formData) {
    // Thiết lập tên bảng vào trong dữ liệu
    formData[ONL_FORM_TABLE_NAME] = tableName.value;
    emit('success', formData);
    if(successThenClose.value == true){
      closeModal();
    }else{
      // không đóng popup Nhắc nhở thành công
    }
    //Khôi phục giá trị mặc định
    topTipVisible.value = false;
    successThenClose.value = true;
  }

  /**
   * modalSự kiện đóng
   */
  function onCloseEvent(){
    if(onlineFormCompRef.value){
      onlineFormCompRef.value.onCloseModal();
    }
    // update-begin--author:liaozhiyang---date:20240618---for：【TV360X-1305】Mở popup chỉnh sửa bình luận sẽ toàn màn hình, khi đóng popup phải bỏ chế độ toàn màn hình, nếu không sẽ ảnh hưởng popup thêm mới
    if (isUpdate.value) {
      const extConfig: any = onlineExtConfig ?? {};
      if (extConfig.commentStatus == 1) {
        setModalProps({ defaultFullscreen: false });
      }
    }
    // update-end--author:liaozhiyang---date:20240618---for：【TV360X-1305】Mở popup chỉnh sửa bình luận sẽ toàn màn hình, khi đóng popup phải bỏ chế độ toàn màn hình, nếu không sẽ ảnh hưởng popup thêm mới
  }
  
  return {
    // modal
    title,
    modalWidth,
    registerModal,
    closeModal,
    modalObject,
    onCloseEvent,

    // Nút tùy chỉnh
    cgButtonList,
    handleCgButtonClick,

    // Gửi/Nút đóng
    disableSubmit,
    handleSubmit,
    submitLoading,
    handleCancel,
    successThenClose,
    handleSuccess,
    topTipVisible,

    //Biểu mẫu
    handleFormConfig,
    onlineFormCompRef,
    formTemplate,
    isTreeForm,
    pidFieldName,
    renderSuccess,
    formRendered,
    isUpdate,
    showSub,
    themeTemplate,

    // Tham số vùng bình luận
    tableId,
    tableName,
    formDataId,
    enableComment,
    popBodyStyle,
    popModalFixedWidth,
    getFormStatus
  };
}


/**
 * Sử dụng chiều cao cố địnhmodal
 */
export function useFixedHeightModal() {
  const minWidth = 800;
  const popModalFixedWidth = ref(800);
  let tempWidth = window.innerWidth - 300;
  if(tempWidth < minWidth){
    tempWidth = minWidth;
  }
  popModalFixedWidth.value = tempWidth;

  // Kiểm soát chiều cao popup
  const popBodyStyle = ref({});
  function resetBodyStyle(){
    let height = window.innerHeight - 210;
    popBodyStyle.value = {
      height: height+'px',
      overflowY: 'auto'
    }
  }
  
  return {
    popModalFixedWidth,
    popBodyStyle,
    resetBodyStyle
  }
}

