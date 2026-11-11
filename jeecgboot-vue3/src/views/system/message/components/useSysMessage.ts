import { ref, reactive, nextTick } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { getDictItemsByCode } from '/@/utils/dict/index';
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '/@/store/modules/app';
import { useTabs } from '/@/hooks/web/useTabs';
import { useModal } from '/@/components/Modal';
import {useMessage} from "/@/hooks/web/useMessage";

/**
 * API danh sách
 * @param params
 */
const queryMessageList = (params) => {
  const url = '/sys/annountCement/vue3List';
  return defHttp.get({ url, params });
};

/**
 * Lấy dữ liệu danh sách tin nhắn
 *
 * setLocaleText Đặt tin nhắn chưa đọc
 */
export function useSysMessage(setLocaleText) {
  const { createMessage } = useMessage();
  const rangeDateArray = getDictItemsByCode('rangeDate');
  console.log('+++++++++++++++++++++');
  console.log('rangeDateArray', rangeDateArray);
  console.log('+++++++++++++++++++++');

  const messageList = ref<any[]>([]);
  const pageNo = ref(1)
  let pageSize = 10;

  const searchParams = reactive({
    fromUser: '',
    rangeDateKey: '',
    rangeDate: [],
    starFlag: '',
    noticeType: ''
  });


  function getQueryParams() {
    let { fromUser, rangeDateKey, rangeDate, starFlag, noticeType } = searchParams;
    let params = {
      fromUser,
      starFlag,
      rangeDateKey,
      beginDate: '',
      endDate: '',
      pageNo: pageNo.value,
      pageSize,
      noticeType
    };
    if (rangeDateKey == 'zdy') {
      params.beginDate = rangeDate[0]+' 00:00:00';
      params.endDate = rangeDate[1]+' 23:59:59';
    }
    return params;
  }

  // Dữ liệu đã tải xong chưa
  const loadEndStatus = ref(false);

  //Dữ liệu yêu cầu
  async function loadData() {
    if(loadEndStatus.value === true){
      return;
    }
    let params = getQueryParams();
    const data = await queryMessageList(params);
    console.log('Lấy kết quả', data);
    if(!data || data.length<=0){
      loadEndStatus.value = true;
      setLocaleText();
      return;
    }
    if(data.length<pageSize){
      loadEndStatus.value = true;
    }
    pageNo.value = pageNo.value+1
    let temp:any[] = messageList.value;
    temp.push(...data);
    messageList.value = temp;
    setLocaleText();
  }

  //Đặt lại
  function reset(){
    messageList.value = []
    pageNo.value = 1;
    loadEndStatus.value = false;
  }

  //Gắn sao
  async function updateStarMessage(item){
    const url = '/sys/sysAnnouncementSend/edit';
    let starFlag = '1';
    if(item.starFlag==starFlag){
      starFlag = '0'
    }
    const params = {
      starFlag,
      id: item.sendId
    }
    // Giải thích logic mã: QQYUN-4491【Ứng dụng】Một số vấn đề nhỏ  4、Gắn sao chắc không cần nhắc nhở
    const data:any = await defHttp.put({url, params}, {isTransformResponse: false});
    if(data.success === true){
    }else{
      createMessage.warning(data.message)
    }
  }


  const loadingMoreStatus = ref(false);
  async function onLoadMore() {
    loadingMoreStatus.value = true;
    await loadData();
    loadingMoreStatus.value = false;
  }

  function noRead(item) {
    if (item.readFlag === '1') {
      return false;
    }
    return true;
  }

  // Loại tin nhắn
  function getMsgCategory(item) {
    if(item.busType=='email'){
      return 'Nhắc qua email:';
    } else if(item.busType=='bpm'){
      return 'Đốc thúc quy trình:';
    } else if(item.busType=='bpm_cc'){
      return 'CC quy trình:';
    }else if(item.busType=='bpm_task'){
      return 'Tác vụ quy trình:';
    } else if (item.busType == 'eoa_co_remind') {
      return 'Cộng tác đốc thúc:';
    } else if (item.busType == 'eoa_co_notify') {
      return 'Nhắc nhở cộng tác:';
    } else if (item.busType == 'eoa_sup_remind') {
      return 'Giám sát đốc thúc:';
    } else if (item.busType == 'eoa_sup_notify') {
      return 'Nhắc nhở giám sát:';
    } else if (item.msgCategory == '2') {
      return 'Tin nhắn hệ thống:';
    } else if (item.msgCategory == '1') {
      return 'Thông báo:';
    }
    return '';
  }

  // QQYUN-4472 Có tin nhắn đến nhưng không có nhắc nhở -- xem chi tiết đổi thành đi xử lý
  function getHrefText(item) {
    if(item.busType === 'bpm'|| item.busType === 'bpm_task' || item.busType === 'tenant_invite'){
      //Kiểm tra có phải đang xem chi tiết hay không
      if (item.msgAbstract) {
        try {
          const json = JSON.parse(item.msgAbstract);
          if (json.taskDetail) {
            return 'Xem chi tiết';
          }
        } catch (e) {
          console.error('getHrefText:msgAbstractTham số không ở định dạng JSON', item.msgAbstract);
        }
      }
      return 'Đi xử lý'
    } else if (['eoa_co_notify', 'eoa_co_remind', 'eoa_sup_notify', 'eoa_sup_remind'].includes(item.busType)) {
      // Giải thích logic mã: 【JHHB-133】Danh sách tin nhắn mở cộng tác làm việc
      return 'Đi xử lý';
    } else {
      return 'Xem chi tiết'
    }
  }

  return {
    messageList,
    reset,
    loadData,
    loadEndStatus,
    searchParams,
    updateStarMessage,
    onLoadMore,
    noRead,
    getMsgCategory,
    getHrefText

  };
}

/**
 * Dùng cho chuyển hướng tin nhắn
 */
export function useMessageHref(emit, props){
  //const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();
  //const [registerTaskModal, { openModal: openTaskModal }] = useModal();
  // Hộp thoại biểu mẫu đăng ký
  //const [registerDesignFormModal, { openModal: openDesignFormModal }] = useModal();
  let messageHrefArray: any[] = getDictItemsByCode('messageHref');
  // Giải thích logic mã: 【JHHB-133】Danh sách tin nhắn mở cộng tác làm việc
  messageHrefArray = [
    ...messageHrefArray,
    { value: 'eoa_co_remind', text: '/collaboration/pending', url: '/collaboration/launch' },
    { value: 'eoa_co_notify', text: '/collaboration/pending', url: '/collaboration/launch' },
    { value: 'eoa_sup_notify', text: '/superviser/pending' },
    { value: 'eoa_sup_remind', text: '/superviser/pending' },
  ];
  const router = useRouter();
  const appStore = useAppStore();
  const rt = useRoute();
  const { close: closeTab, closeSameRoute } = useTabs();

  //*********************************[QQYUN-6713]Thông báo hệ thống mở hộp thoại để sửa, thiết lập hộp thoại độngbegin******************************************
  //Hộp thoại biểu mẫu hiện tại
  const currentModal = ref<string | null>(null);
  //Tham số biểu mẫu hiện tại
  const modalParams = ref<Recordable>({});
  //Bộ nhớ đệm đăng ký biểu mẫu
  const modalRegCache = ref<Recordable>({});
  //Tham số liên kết component
  const bindParams = ref<Recordable>({});

  /**
   * Dựa vào loại để mở các hộp thoại khác nhau
   * @param type
   * @param params
   */
  async function handleOpenType(type, params) {
    currentModal.value = null;
    modalParams.value = { ...params };
    switch (type) {
      case 'task':
        //Xử lý quy trình
        bindParams.value = { actionType: 'todo' };
        currentModal.value = 'ProcessTaskHandleModal';
        break;
      case 'history':
        bindParams.value = {};
        //Quy trình lịch sử
        currentModal.value = 'MyTaskHandleModal';
        break;
      case 'design':
        //Thiết kế biểu mẫu
        currentModal.value = 'DesformViewModal';
        bindParams.value = {
          showRecordCopy: false,
          showRecordShare: false,
          showRecordSysPrint: false,
          showDesignFormBtn: false,
        };
        break;
      case 'cgform':
        //OnlineBiểu mẫu
        // update-begin--author:liusq---date:20261102---for：【Bình luận biểu mẫu online】Xem chi tiết tin nhắn hệ thống chuyển sang dùng hộp thoại chi tiết phiên bản mới
        // Giống 'Chi tiết' ở trang danh sách, dùng OnlineDetailModal, không dùng biểu mẫu bị vô hiệu hóa của OnlineAutoModal nữa
        currentModal.value = 'OnlineDetailModal';
        // update-end--author:liusq---date:20261102---for：【Bình luận biểu mẫu online】Xem chi tiết tin nhắn hệ thống chuyển sang dùng hộp thoại chi tiết phiên bản mới
        bindParams.value = {
          id: params.formId,
        }
        break;
      default:
        currentModal.value = null;
        break;
    }
    //Hộp thoại biểu mẫu đăng ký
    initModalRegister();
    await nextTick(() => {
      if (modalRegCache.value[currentModal.value!]?.isRegister) {
        console.log('Đã đăng ký, đi theo bộ nhớ đệm');
        modalRegCache.value[currentModal.value!].modalMethods.openModal(true, modalParams.value);
      }
    });
  }

  /**
   * Khởi tạo đăng ký hộp thoại
   */
  function initModalRegister() {
    //Nếu biểu mẫu đang chọn là null thì không xử lý
    if (!currentModal.value) {
      return;
    }
    //Kiểm tra có tồn tại trong bộ nhớ đệm hay không, nếu không tồn tại thì đi theo logic bộ nhớ đệm
    if (!modalRegCache.value[currentModal.value]) {
      const [registerModal, modalMethods] = useModal();
      modalRegCache.value[currentModal.value] = {
        isRegister: false,
        register: bindRegisterModal(registerModal, modalMethods),
        modalMethods,
      };
    }
  }

  /**
   * Hộp thoại đăng ký liên kết
   * @param regFn
   * @param modalMethod
   */
  function bindRegisterModal(regFn, modalMethod) {
    return async (...args) => {
      console.log('Bắt đầu đăng ký:', currentModal.value);
      await regFn(...args);
      console.log('Đăng ký hoàn thành:', currentModal.value);
      //Mở hộp thoại
      modalMethod.openModal(true, modalParams.value);
      //Đặt dấu hiệu bộ nhớ đệm
      modalRegCache.value[currentModal.value!].isRegister = true;
    };
  }
  //*************************************[QQYUN-6713]Thông báo hệ thống mở hộp thoại để sửa, thiết lập hộp thoại độngend*********************************************
  // const defaultPath = '/monitor/mynews';
  //const bpmPath = '/task/handle/'

  async function goPage(record, openModalFun?){
    if(!record.busType || record.busType == 'msg_node'){
      if(!openModalFun){
        // Chuyển từ thông báo tin nhắn ở trang chủ
        await goPageFromOuter(record);
      }else{
        // Từ danh sách ở trang tin nhắn, nhấn xem chi tiết để mở trực tiếpmodal
        openModalFun()
      }
      // Giải thích logic mã: QQYUN-4744【Thông báo hệ thống】6、Thông báo hệ thống@sau đó, đối phương không nhìn ra đó là biểu mẫu nào@, không có siêu liên kết
    }else if(record.busType == 'comment'){
      // de
      let msgAbstract = record.msgAbstract;
      if(msgAbstract){
        try {
          let data = JSON.parse(msgAbstract.toString());
          if(data.type == 'designForm'){
            showDesignFormModal(data);
          } else {
            showOnlineCgformModal(data);
          }
        }catch (e) {
          console.error('Mở biểu mẫu bình luận, nhưng tham số msgAbstract không ở định dạng JSON', msgAbstract)
          if(openModalFun){
            openModalFun();
          }
        }
      }
    }else if(record.busType == 'tenant_invite'){
      if(props.isLowApp===true){
        router.push({ name:"myapps-settings-user", query:{ page:'tenantSetting' }})
      }else{
        router.push({ name:"system-usersetting", query:{ page:'tenantSetting' }})
      }
    }else{
      if(props && props.isLowApp===true){
        openLowAppFlowModal(record)
      }else{
        await goPageWithBusType(record)
      }
    }
/*    busId: "1562035005173587970"
    busType: "email"
    openPage: "modules/eoa/email/modals/EoaEmailInForm"
    openType: "component"*/
  }

  /**
   * Mở hộp thoại biểu mẫu của trình thiết kế biểu mẫu
   * @param data
   */
  function showDesignFormModal(data) {
    handleOpenType('design', {
      mode: 'detail',
      desformCode: data.code,
      dataId: data.dataId,
      isOnline: false,
    });
  }

  /**
   * Mở hộp thoại biểu mẫu Online
   * @param data
   */
  function showOnlineCgformModal(data) {
    handleOpenType('cgform', {
      formId: data.formId,
      isUpdate: true,
      disableSubmit: true,
      record: {
        id: data.dataId,
      },
    });
  }

  /**
   * Kiểm tra có phải tin nhắn bình luận của biểu mẫu hay không
   * @param record
   */
  function isFormComment(record) {
    if(record.busType == 'comment'){
      let msgAbstract = record.msgAbstract;
      if(msgAbstract){
        try {
          let data = JSON.parse(msgAbstract);
          if(['cgform', 'designForm'].includes(data.type)){
            return true
          }
        }catch (e) {
          console.error('Mở biểu mẫu bình luận, nhưng tham số msgAbstract không ở định dạng JSON', msgAbstract)
        }
      }
    }
    return false
  }

  /**
   * Nếu là tác vụ quy trình làm việc thì mở trực tiếp trong lowAppmodal
   */
  function openLowAppFlowModal(record){
    const { busType, busId, msgAbstract } = record;
    let temp = messageHrefArray.filter(item=>item.value === busType);
    if(!temp || temp.length==0){
      console.error('Loại nghiệp vụ hiện tại không nhận dạng được', busType);
      return;
    }
    if(busType.indexOf('bpm')<0){
      console.error('low-appKhông hỗ trợ chuyển tới email', busType);
      return;
    }
    //Tham số cố định detailId dùng để truy vấn dữ liệu biểu mẫu
    let query:any = {
      detailId: busId
    };
    // Xử lý tham số bổ sung
    if(msgAbstract){
      try {
        let json = JSON.parse(msgAbstract);
        Object.keys(json).map(k=>{
          query[k] = json[k]
        });
      }catch (e) {
        console.error('msgAbstractTham số không ở định dạng JSON', msgAbstract)
      }
    }
    console.log("busType = ", busType)
    handleOpenType('task', {
      record: {
        id: busId,
        procInsId: query.procInsId,
        processDefinitionId: query.processDefinitionId,
        isDetail: query.taskDetail || 'bpm_cc' == busType
      }
    })
  }

  /**
   * Dựa vào busType khác nhau để chuyển tới các trang khác nhau
   * @param record
   */
  async function goPageWithBusType(record){
    const { busType, busId, msgAbstract } = record;
    let temp = messageHrefArray.filter(item=>item.value === busType);
    if(!temp || temp.length==0){
      console.error('Loại nghiệp vụ hiện tại không nhận dạng được', busType);
      return;
    }
    let path = temp[0].text;
    if (['eoa_co_notify', 'eoa_co_remind'].includes(busType)) {
      if (busId.startsWith('coId-')) {
        path = temp[0].url;
      } else if (busId.startsWith('nodeId-')) {
        path = temp[0].text;
      }
    }
    path = path.replace('{DETAIL_ID}', busId)
    //Tham số cố định detailId dùng để truy vấn dữ liệu biểu mẫu
    let query:any = {
      detailId: busId
    };
    // Xử lý tham số bổ sung
    if(msgAbstract){
      try {
        let json = JSON.parse(msgAbstract);
        Object.keys(json).map(k=>{
          query[k] = json[k]
        });
      }catch (e) {
        console.error('msgAbstractTham số không ở định dạng JSON', msgAbstract)
      }
    }
    if(query.taskDetail){
      // Hộp thoại xem chi tiết tác vụ
      await showHistory(query.procInsId, {taskOriginalId:query.taskId,busType,id:busId,readFlag:record.readFlag})
    }else{
      // Chuyển định tuyến
      appStore.setMessageHrefParams(query);
      if(rt.path.indexOf(path)>=0){
        await closeTab();
        await router.replace({ path: path, query:{ time: new Date().getTime() } });
      }else{
        closeSameRoute(path)
        await router.push({ path: path });
      }
    }
  }

  /**
   * Từ thông báo tin nhắn ở trang chủ, chuyển tới danh sách tin nhắn để mởmodal
   * @param record
   */
  async function goPageFromOuter(record){
    //Chưa định nghĩa loại nghiệp vụ, chuyển thẳng tới trang Tin nhắn của tôi
    emit('detail', record)
  }

  //===============================================================================================================
  // Giải thích logic mã: QQYUN-3485 【Xem quy trình】Tạo một trang xem, không phải trang xử lý, chỉ cần thông qua tham số của thực thể quy trình là được
  async function showHistory(processInstanceId, data?) {
    let { formData, formUrl } = await getTaskInfoForHistory({ processInstanceId });
    formData['PROCESS_TAB_TYPE'] = 'history';
    handleOpenType('history', {
      formData,
      formUrl,
      isCc: data && data.busType == 'bpm_cc',
      record: data,
      title: 'Lịch sử quy trình',
    });
  }

  const nodeInfoUrl = '/act/process/extActProcessNode/getHisProcessNodeInfo'
  const taskNodeInfo = (params) => defHttp.get({ url: nodeInfoUrl, params });

  async function getTaskInfoForHistory(record) {
    //Điều kiện truy vấn
    let params = { procInstId: record.processInstanceId };
    const result = await taskNodeInfo(params);
    console.log('Lấy thông tin tác vụ lịch sử', result);
    let formData: any = {
      dataId: result.dataId,
      taskId: record.id,
      taskDefKey: record.taskId,
      procInsId: record.processInstanceId,
      tableName: result.tableName,
      vars: result.records,
    };
    let tempFormUrl = result.formUrl;
    console.log('Lấy biểu mẫu nút quy trìnhURL', tempFormUrl);
    //URL biểu mẫu cấu hình nút, tham số mở rộng tương ứng với loại component VUE
    if (tempFormUrl && tempFormUrl.indexOf('?') != -1 && !isURL(tempFormUrl) && tempFormUrl.indexOf('{{DOMAIN_URL}}') == -1) {
      tempFormUrl = result.formUrl.split('?')[0];
      console.log('Lấy URL biểu mẫu của nút quy trình (bỏ tham số)', tempFormUrl);
      formData.extendUrlParams = getQueryVariable(result.formUrl);
    }
    return {
      formData,
      formUrl: tempFormUrl,
    };
  }

  /**
   * Lấy tham số trên URL
   * @param url
   */
  function getQueryVariable(url) {
    if (!url) return;

    let t,
      n,
      r,
      i = url.split('?')[1],
      s = {};
    (t = i.split('&')), (r = null), (n = null);
    for (let o in t) {
      let u = t[o].indexOf('=');
      u !== -1 && ((r = t[o].substr(0, u)), (n = t[o].substr(u + 1)), (s[r] = n));
    }
    return s;
  }

  /**
   * URLĐịa chỉ
   * @param {*} s
   */
  function isURL(s) {
    return /^http[s]?:\/\/.*/.test(s);
  }
  //===============================================================================================================

  return {
    goPage,
    isFormComment,
    modalRegCache,
    currentModal,
    bindParams,
  }
}
