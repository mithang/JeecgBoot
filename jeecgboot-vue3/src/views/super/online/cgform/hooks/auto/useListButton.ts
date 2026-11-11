import type { Ref } from 'vue';
import type { ExtConfigType } from '../../types';
import { computed, reactive, toRaw, ref } from 'vue';
import { CgFormButton } from '../../types/onlineRender';
import { pick } from 'lodash-es';
import { useModal } from '/@/components/Modal';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { filterObj } from '/@/utils/common/compUtils';
import { useMethods } from '/@/hooks/system/useMethods';
import { getToken } from '/@/utils/auth';
import { goJmReportViewPage } from '/@/utils'
import { Tree } from '../../util/constant';

/**Tiền tố mã quy trình làm việc*/
const FLOW_CODE_PRE = 'onl_';
/**
 * Phụ trách nút của trang danh sách và sự kiện của chúng--tạiinitAutoListthực thi sau đó,Bắt buộc phải lấy thông tin nút trước
 * - Cái này dùng chung cho mọi phong cách và bắt buộc phải có
 */
export function useListButton(onlineTableContext, extConfigJson: Ref<ExtConfigType | undefined>, extraParameter = {}) {
  const buttonStatus = {
    add: true,
    addSub: true,
    // edit = của nút chỉnh sửacode
    edit: true,
    // update = cái cũ của nút chỉnh sửacode
    update: true,
    delete: true,
    batch_delete: true,
    import: true,
    export: true,
    print: true,
    detail: true,
    query: true,
    reset: true,
    super_query: true,
    bpm: true,
    form_confirm: true,
    // Thêm mới bảng con
    form_sub_add: true,
    // Xóa bảng con
    form_sub_batch_delete: true,
    // Thêm mới bảng con
    form_sub_open_add: true,
    // Chỉnh sửa bảng con
    form_sub_open_edit: true,
    // Tạo dữ liệu kiểm thử
    aigc_mock_data: true,
  };

  // Sự kiện popup
  const [registerModal, { openModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();
  const [registerBpmModal, { openModal: openBpmModal }] = useModal();
  const { createMessage: $message } = useMessage();

  // Liên quan đến nút
  const buttonSwitch = reactive(buttonStatus);
  const cgLinkButtonList = reactive<CgFormButton[]>([]);
  const cgTopButtonList = reactive<CgFormButton[]>([]);

  interface CgBIBtnType extends CgFormButton {
    enabled: boolean,
  }

  // OnlineDanh sách nút tích hợp sẵn của biểu mẫu
  const cgBIBtnMap = reactive<Record<string, CgBIBtnType>>({});

  // Tạo cấu hình nút tích hợp sẵn (dùng để kiểm soát quyền nút)
  const createBIButtonCfg = (btnKey: string) => computed(() => buttonSwitch[btnKey] === true ? cgBIBtnMap[btnKey] : {enabled: false})
  // Cấu hình nút truy vấn
  const getQueryButtonCfg = createBIButtonCfg('query')
  // Cấu hình nút đặt lại
  const getResetButtonCfg = createBIButtonCfg('reset')
  // Cấu hình nút xác nhận của popup biểu mẫu
  const getFormConfirmButtonCfg = createBIButtonCfg('form_confirm')

  const testDataLoading = ref(false);
  const testDataBtnShow = ref(true);
  setTimeout(() => {
    testDataBtnShow.value = false;
  }, 4e3);
  /**
   * Lấy theo cấu hình buttonvàlink
   */
  function initButtonList(btnList) {
    cgLinkButtonList.length = 0;
    cgTopButtonList.length = 0;
    if (btnList && btnList.length > 0) {
      for (let i = 0; i < btnList.length; i++) {
        let temp = pick(btnList[i], 'buttonCode', 'buttonName', 'buttonStyle', 'optType', 'exp', 'buttonIcon', 'buttonStatus', 'enabled');
        if (temp.buttonStyle == 'button') {
          cgTopButtonList.push(temp);
        } else if (temp.buttonStyle == 'link') {
          cgLinkButtonList.push(temp);
        } else if (temp.buttonStyle == 'built-in') {
          if (temp.buttonIcon) {
            temp.buttonIcon = 'ant-design:' + temp.buttonIcon;
          }
          temp.enabled = temp.buttonStatus === '1'
          cgBIBtnMap[temp.buttonCode] = temp;
        }
      }
    }
  }

  /**
   * Thiết lập của nút theo cấu hình Hiển thị/Ẩn Trạng thái
   */
  function initButtonSwitch(hideColumns) {
    Object.keys(buttonSwitch).forEach((key) => {
      buttonSwitch[key] = true;
    });
    if (hideColumns && hideColumns.length > 0) {
      Object.keys(buttonSwitch).forEach((key) => {
        if (hideColumns.indexOf(key) >= 0) {
          buttonSwitch[key] = false;
        }
      });
    }
  }

  // Thêm sự kiện
  function handleAdd(param) {
    let data = { isUpdate: false };
    if (param) {
      data['param'] = param;
    }
    openModal(true, data);
  }

  // Sửa sự kiện
  function handleEdit(record) {
    onlineTableContext
      .beforeEdit(record)
      .then(() => {
        openModal(true, {
          isUpdate: true,
          record,
        });
      })
      .catch((msg) => {
        $message.warning(msg);
      });
  }

  /**
   *
   * [Thêm]trong mục dropdown [Xóa]
   */
  const getDeleteButton = (record) => {
    return {
      label: cgBIBtnMap['delete'].buttonName,
      ifShow: () => cgBIBtnMap['delete'].enabled,
      popConfirm: {
        title: 'Có xóa hay không?',
        confirm: handleDeleteOne.bind(null, record),
      },
    };
  };

  // Sự kiện xóa
  function handleDeleteOne(record) {
    onlineTableContext
      .beforeDelete(record)
      .then(() => {
        handleDelete(record.id, false);
      })
      .catch((msg) => {
        $message.warning(msg);
      });
  }

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    //update-begin-author:taoyan date:2022-10-17 for: VUEN-2351【vue3 onlineBiểu mẫu 】onlineBiểu mẫu Sau khi khởi tạo quy trình, vẫn có thể chỉnh sửa dữ liệu
    let bpmStatusValue = getBpmStatusValue(record);
    // Trường hợp cho phép chỉnh sửa--> bpmCó giá trị và giá trị là1，bpmKhông có giá trị, 
    //update-begin-author:taoyan date:2023-2-6 for:  QQYUN-4135【online】Quy trình đã phê duyệt xong và quy trình đã thu hồi/hủy bỏ thì có thể chỉnh sửa
    let canEdit = (bpmStatusValue && (bpmStatusValue=='1' || bpmStatusValue=='3' || bpmStatusValue=='4')) || !bpmStatusValue;
    //update-end-author:taoyan date:2023-2-6 for:  QQYUN-4135【online】Quy trình đã phê duyệt xong và quy trình đã thu hồi/hủy bỏ thì có thể chỉnh sửa
    if ((toRaw(buttonSwitch.edit) === true && toRaw(buttonSwitch.update) === true) && canEdit) {
    //update-end-author:taoyan date:2022-10-17 for: VUEN-2351【vue3 onlineBiểu mẫu 】onlineBiểu mẫu Sau khi khởi tạo quy trình, vẫn có thể chỉnh sửa dữ liệu
      return [
        {
          label: cgBIBtnMap['edit'].buttonName,
          ifShow: () => cgBIBtnMap['edit'].enabled,
          onClick: (e) => {
            // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
            // erpKhi nhấp chỉnh sửa ở bảng chính, cần ngăn việc đảo lựa chọn của dữ liệu đang được chọn hiện tại
            extraParameter['editClickCallback'] && extraParameter['editClickCallback'](record.id, e);
            handleEdit(record);
            // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
          },
        },
      ];
    }
    return [];
  }

  /**
   *  Cột thao tác[Gửi quy trình]
   */
  function getSubmitFlowButton(record) {
    return {
      label: cgBIBtnMap['bpm'].buttonName,
      ifShow: () => cgBIBtnMap['bpm'].enabled,
      popConfirm: {
        title: 'Xác nhận gửi quy trình chứ?',
        confirm: handleSubmitFlow.bind(null, record),
      },
    };
  }

  /**
   * Cột thao tác[Tiến độ phê duyệt]
   * @param record
   */
  function getViewBpmGraphicButton(record){
    return {
      label: 'Tiến độ phê duyệt',
      onClick: handleViewGraphic.bind(null, record),
    };
  }

  /**
   * Xem sơ đồ quy trình
   * @param record
   */
  function handleViewGraphic(record){
    const { currentTableName } = onlineTableContext;
    
    //Phán đoán currentTableNameNếu là view, cắt bỏ hậu tố
    let currentTableNameVariable = currentTableName;
    if (currentTableName.includes('$')) {
      currentTableNameVariable = currentTableName.split('$')[0];
    }
    
    let flowCode = FLOW_CODE_PRE + currentTableNameVariable;
    let dataId = record.id;
    openBpmModal(true, {
      flowCode,
      dataId
    })
  }

  /**
   *  Cột thao tác[Thêm mục dropdown]
   */
  function getDropDownActions(record, params = {} ) {
    let arr: any = [];
    if (toRaw(buttonSwitch.detail) === true) {
      arr.push({
        label: cgBIBtnMap['detail'].buttonName,
        ifShow: () => cgBIBtnMap['detail'].enabled,
        onClick: handleDetail.bind(null, record),
      });
    }
    // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1101】Bảng cây mở nút gửi quy trình và thêm popup chi tiết phê duyệt
    if (onlineTableContext['hasBpmStatus'] === true && toRaw(buttonSwitch.bpm) === true) {
      // Điều kiện hiển thị nút gửi quy trình: Cóbpm_statustrường, đồng thời có thao tácbpmQuyền của nút
      let bpmStatusValue = getBpmStatusValue(record);
      if (!bpmStatusValue || bpmStatusValue == '1') {
        //đồng thời bpm_statusgiá trị của là1 hoặc để trống
        arr.push(getSubmitFlowButton(record));
      }else{
        arr.push(getViewBpmGraphicButton(record))
      }
    }
    // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1101】Bảng cây mở nút gửi quy trình và thêm popup chi tiết phê duyệt
    // Kết nối in báo cáo Jimu
    if (extConfigJson.value) {
      let { reportPrintShow, reportPrintUrl } = extConfigJson.value;
      if (reportPrintShow && reportPrintUrl && toRaw(buttonSwitch.print) === true && cgBIBtnMap['print']?.enabled !== false) {
        arr.push({
          label: 'In',
          onClick() {
            //Chuyển tới trang báo cáo Jimu
            let url = reportPrintUrl;
            let id = record.id;
            let token = getToken();
            goJmReportViewPage(url, id, token);
          },
        });
      }
    }
    //update-begin-author:taoyan date:2022-10-17 for: VUEN-2351【vue3 onlineBiểu mẫu 】onlineBiểu mẫu Sau khi khởi tạo quy trình, vẫn có thể chỉnh sửa dữ liệu
    // Trường hợp cho phép xóa--> bpmCó giá trị và giá trị là1，bpmKhông có giá trị
    let bpmStatusValue = getBpmStatusValue(record);
    let canDelete = (bpmStatusValue && bpmStatusValue=='1') || !bpmStatusValue;
    if (toRaw(buttonSwitch.delete) === true && canDelete) {
    //update-end-author:taoyan date:2022-10-17 for: VUEN-2351【vue3 onlineBiểu mẫu 】onlineBiểu mẫu Sau khi khởi tạo quy trình, vẫn có thể chỉnh sửa dữ liệu
      arr.push(getDeleteButton(record));
    }
    let buttonList = cgLinkButtonList;
    if (buttonList && buttonList.length > 0) {
      for (let item of buttonList) {
        if (showLinkButtonOfExpression(item.exp || '', record) === true) {
          arr.push({
            label: item.buttonName,
            onClick: cgButtonLinkHandler.bind(null, record, item.buttonCode, item.optType),
          });
        }
      }
    }
    return arr;
  }

  /**
   * Lấybpm_statusgiá trị của Chữ hoa và chữ thường đều lấy một lượt
   * @param record
   */
  function getBpmStatusValue(record) {
    const key = 'bpm_status';
    let value = record[key];
    if (!value) {
      value = record[key.toUpperCase()];
    }
    return value;
  }

  /**
   * Xem chi tiết
   * @param record
   */
  function handleDetail(record) {
    openDetailModal(true, {
      isUpdate: true,
      disableSubmit: true,
      record,
    });
  }

  /**
   * Yêu cầu gửi quy trình
   * @param record
   */
  function startProcess(record) {
    const {
      currentTableName,
      onlineUrl: { startProcess },
    } = onlineTableContext;
    
    //Phán đoán currentTableNameNếu là view, cắt bỏ hậu tố
    let currentTableNameVariable = currentTableName;
    if (currentTableName.includes('$')) {
      currentTableNameVariable = currentTableName.split('$')[0];
    }
    
    let postConfig = {
      url: startProcess,
      params: {
        flowCode: FLOW_CODE_PRE + currentTableNameVariable,
        id: record.id,
        // TODO Biểu mẫu quy trình không có
        formUrl: 'modules/bpm/task/form/OnlineFormDetail',
        formUrlMobile: 'check/onlineForm/detail',
      },
    };
    let postOption = { isTransformResponse: false };
    return new Promise((resolve, reject) => {
      defHttp.post(postConfig, postOption).then((res) => {
        if (res.success) {
          resolve(res);
          $message.success(res.message);
        } else {
          reject();
          $message.warning(res.message);
        }
      });
    });
  }

  /**
   * Sự kiện kích hoạt của nút gửi quy trình
   * @param record
   */
  async function handleSubmitFlow(record) {
    await startProcess(record);
    onlineTableContext.loadData();
  }

  //Yêu cầu xóa
  function handleDelete(dataId: String, isBatch = true) {
    console.log('Xóa dữ liệuidgiá trị', dataId);
    let url = `${onlineTableContext.onlineUrl.optPre}${onlineTableContext.ID}/${dataId}`;
    // update-begin--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
    if (onlineTableContext['isErpSubTable'] === true) {
      url = `${url}?tabletype=3`;
    }
    // update-end--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
    return new Promise((resolve, reject) => {
      defHttp
        .delete(
          {
            url,
          },
          { isTransformResponse: false }
        )
        .then((res) => {
          if (res.success) {
            $message.success(res.message);
            // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-206】Danh sách xóa dữ liệu ở trang cuối, khi trang nhảy về trang trước thì dữ liệu trống
            onlineTableContext.loadData({ delNum: dataId.split(',').length });
            // update-end--author:liaozhiyang---date:20240528---for：【TV360X-206】Danh sách xóa dữ liệu ở trang cuối, khi trang nhảy về trang trước thì dữ liệu trống
            // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
            // erpKhi bản ghi của bảng chính đang được chọn bị xóa, cần xóa lựa chọnkey
            if (!isBatch) {
              extraParameter['singleDelCallback'] && extraParameter['singleDelCallback'](dataId);
            }
            // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
            resolve(true);
          } else {
            $message.warning(res.message);
            reject();
          }
        });
    });
  }

  // Sự kiện xóa hàng loạt
  async function handleBatchDelete() {
    let arr = onlineTableContext['selectedRowKeys'];
    if (arr.length <= 0) {
      $message.warning('Vui lòng chọn một bản ghi!');
      return false;
    } else {
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
      const selectedRows = onlineTableContext['selectedRows'] || [];
      const rowMap = new Map();
      selectedRows.forEach((row) => {
        if (row && row.id != null) {
          rowMap.set(String(row.id), row);
        }
      });
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
      let idSet: any = [];
      let skippedCount = 0;
      arr.forEach(function (val) {
        let temp = val;
        //Danh sách dạng cây keyPhía sau sẽ mang theo_loadChild
        if (temp && temp.endsWith('_loadChild')) {
          temp = temp.replace('_loadChild', '');
        }
        // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
        // Cho phép xóa:bpm_status rỗng, hoặc giá trị là 1(chờ gửi)
        const record = rowMap.get(String(temp));
        if (record) {
          const bpmStatusValue = getBpmStatusValue(record);
          const canDelete = (bpmStatusValue && bpmStatusValue == '1') || !bpmStatusValue;
          if (!canDelete) {
            skippedCount++;
            return;
          }
        }
        // update-end--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
        // Khử trùng lặp
        if (idSet.indexOf(temp) < 0) {
          idSet.push(temp);
        }
      });
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
      if (idSet.length <= 0) {
        $message.warning('Dữ liệu đã chọn đều không thể xóa (quy trình đã khởi tạo)!');
        return false;
      }
      if (skippedCount > 0) {
        $message.warning(`Có ${skippedCount} bản ghi dữ liệu đã khởi tạo quy trình không thể xóa, sẽ bỏ qua`);
      }
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-839】Xóa hàng loạt và「Thêm-xóa」nhất quán, bản ghi đã khởi tạo quy trình thì không thể xóa
      // update-begin--author:liaozhiyang---date:20261028---for：【LHZP-226】Xóa hàng loạt cũng thực thiJSSự kiện tiền tố trước khi xóa của phần tăng cườngbeforeDelete
      const beforeDelete = onlineTableContext['beforeDelete'];
      if (typeof beforeDelete === 'function') {
        const results = await Promise.allSettled(
          idSet.map((id) => {
            // Khi chọnข้าม trang, trang hiện tại có thể không có dữ liệu hàng đầy đủ, chỉ truyềnid
            const record = rowMap.get(String(id)) || { id };
            return Promise.resolve().then(() => beforeDelete(record));
          })
        );
        const rejectMsgs: string[] = [];
        idSet = idSet.filter((_id, index) => {
          const result = results[index];
          if (result.status === 'rejected') {
            if (result.reason && rejectMsgs.indexOf(String(result.reason)) < 0) {
              rejectMsgs.push(String(result.reason));
            }
            return false;
          }
          return true;
        });
        const enhanceSkippedCount = results.filter((item) => item.status === 'rejected').length;
        if (enhanceSkippedCount > 0) {
          const reason = rejectMsgs.length > 0 ? `：${rejectMsgs.join('；')}` : '';
          if (idSet.length <= 0) {
            $message.warning(`Dữ liệu đã chọn đều không thể xóa${reason}`);
            return false;
          }
          $message.warning(`Có ${enhanceSkippedCount} bản ghi dữ liệu không thể xóa, sẽ bỏ qua${reason}`);
          skippedCount += enhanceSkippedCount;
        }
      }
      // update-end--author:liaozhiyang---date:20261028---for：【LHZP-226】Xóa hàng loạt cũng thực thiJSSự kiện tiền tố trước khi xóa của phần tăng cườngbeforeDelete
      let ids = idSet.join(',');
      Modal.confirm({
        title: 'Xác nhận xóa',
        content: skippedCount > 0 ? `Có xóa những mục có thể xóa hay không ${idSet.length} bản ghi dữ liệu?` : 'Có xóa dữ liệu đã chọn hay không',
        okText: 'Xác nhận',
        cancelText: 'Hủy',
        onOk: async () => {
          await handleDelete(ids);
          onlineTableContext.clearSelectedRow();
        },
      });
    }
  }
  /*
   * liaozhiyang
   * 20250403
   * 【QQYUN-11801】Tạo dữ liệu kiểm thử
   * */
  const handleAddTestData = (currentTableName, reload) => {
    testDataLoading.value = true;
    defHttp
      .post({ url: `/online/cgform/api/aigc/mock/data/${currentTableName}`, timeout: 120000 }, { isTransformResponse: false })
      .then((res) => {
        if (res.code == 200) {
          $message.success('Tạo dữ liệu kiểm thử thành công~');
          reload();
        } else {
          $message.warn(res.message);
        }
        testDataLoading.value = false;
      })
      .catch((err) => {
        testDataLoading.value = false;
        console.log(err);
      });
  }

  /**
   * Sự kiện kích hoạt nút tùy chỉnh-linkNút
   * @param record
   * @param buttonCode
   * @param optType js/bus
   */
  function cgButtonLinkHandler(record, buttonCode, optType) {
    if (optType == 'js') {
      onlineTableContext['execButtonEnhance'](buttonCode, record);
    } else if (optType == 'action') {
      let params = {
        formId: onlineTableContext['ID'],
        buttonCode: buttonCode,
        dataId: record.id,
      };
      //console.log("Nút tùy chỉnhlinkTham số yêu cầu backend:",params)
      let url = `${onlineTableContext.onlineUrl.buttonAction}`;
      defHttp
        .post(
          {
            url,
            params,
          },
          { isTransformResponse: false }
        )
        .then((res) => {
          if (res.success) {
            onlineTableContext.loadData();
            $message.success('Xử lý hoàn tất!');
          } else {
            $message.warning(res.message);
          }
        });
    }
  }

  /**
   * Nút phía trên danh sách -jssự kiện
   * @param buttonCode
   */
  function cgButtonJsHandler(buttonCode) {
    // Chờ kiểm thử
    onlineTableContext['execButtonEnhance'](buttonCode);
  }

  /**
   * Nút phía trên danh sách -actionsự kiện
   * @param buttonCode
   */
  function cgButtonActionHandler(buttonCode) {
    let arr = onlineTableContext['selectedRowKeys'];
    if (!arr || arr.length == 0) {
      $message.warning('Vui lòng chọn một bản ghi trước');
      return false;
    }
    let dataId = arr.join(',');
    let params = {
      formId: onlineTableContext['ID'],
      buttonCode: buttonCode,
      dataId: dataId,
    };
    //console.log("Tham số yêu cầu backend của nút tùy chỉnh:",params)
    let url = `${onlineTableContext.onlineUrl.buttonAction}`;
    defHttp
      .post(
        {
          url,
          params,
        },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.success) {
          onlineTableContext.loadData();
          onlineTableContext.clearSelectedRow();
          $message.success('Xử lý hoàn tất!');
        } else {
          $message.warning(res.message);
        }
      });
  }

  // Sự kiện nhập
  function onImportExcel() {
    // update-begin--author:liaozhiyang---date:20240429---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền nhập dữ liệu bảng phụ thuộc
    if (onlineTableContext['foreignKeyField'] && onlineTableContext['foreignKeyValue']) {
      openImportModal(true, {
        [onlineTableContext['foreignKeyField']]: onlineTableContext['foreignKeyValue'],
      });
    } else {
      openImportModal(true);
    }
    // update-end--author:liaozhiyang---date:20240429---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền nhập dữ liệu bảng phụ thuộc
  }

  // Địa chỉ nhập
  const importUrl = () => {
    // update-begin--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
    let url = `${onlineTableContext.onlineUrl.importXls}${onlineTableContext.ID}`;
    if (onlineTableContext['isErpSubTable'] === true) {
      url = `${url}?tabletype=3`;
    } else if (onlineTableContext.isTree() === true) {
      url = `${url}?tabletype=2`;
    }
    return url;
    // update-end--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
  };

  // Sự kiện xuất
  const { handleExportXlsx } = useMethods();
  function onExportExcel() {
    let params = onlineTableContext.getLoadDataParams();
    let selections = onlineTableContext['selectedRowKeys'];
    if (selections && selections.length > 0) {
      params['selections'] = selections.join(',');
    }
    // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
    let tabletype = {};
    if (onlineTableContext['isErpSubTable'] === true) {
      // update-begin--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
      tabletype = { tabletype: 3 };
      // update-end--author:liaozhiyang---date:20240428---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xóa và xuất dữ liệu bảng phụ thuộc
      if (onlineTableContext['foreignKeyField'] && onlineTableContext['foreignKeyValue']) {
        params[onlineTableContext['foreignKeyField']] = onlineTableContext['foreignKeyValue'];
      }
    } else if (onlineTableContext.isTree() === true) {
      // Việc xuất bảng cây cần mang theo loại, backend căn cứ vào đó giữ lại cấp bậc nút và ghi thuyết minh quy tắc nhập vào tệp.
      tabletype = { tabletype: 2 };
    }
    // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
    //console.log("Tham số xuất",params)
    let paramsStr = JSON.stringify(filterObj(params));
    let url = `${onlineTableContext.onlineUrl.exportXls}${onlineTableContext.ID}`;
    const description = onlineTableContext.description;
    const imageExportMode = extConfigJson.value?.imageExportMode || 'path';
    return handleExportXlsx(description, url, { paramsStr: paramsStr, imageExportMode, ...tabletype });
  }

  /**
   * liaozhiyang
   * 20231008
   * Trước tiên chuyển biểu mẫu tùy chỉnh thành giá trị boolean, rồi sử dụngnew FunctionQuy đổi quy tắc thực sự
   */
  function multipleLinkButtonOfExpression(expression, row) {
    const gather: any = [];
    expression.split('||').forEach(oItem => {
      const arr: any = [];
      oItem
        .trim()
        .split('&&')
        .forEach(nItem => {
          arr.push(oneLinkButtonOfExpression(nItem.trim(), row));
        });
      gather.push(arr.join('&&'));
    });
    const r = gather.join('||');
    console.log('---Nhiều biểu thức---', r);
    return new Function(`return ${r}`)();
  }
  /**
   * liaozhiyang
   * 20231008
   * Phân biệt có biểu thức hay không
   */
  function showLinkButtonOfExpression(expression, row) {
    if (!expression || expression == '') {
      return true;
    }
    if (expression.indexOf('||') == -1 && expression.indexOf('&&') == -1) {
      return oneLinkButtonOfExpression(expression, row);
    } else {
      return multipleLinkButtonOfExpression(expression, row);
    }
  }

  /**
   *  Dùng để xử lý linkBiểu thức của nút Trả về giá trị boolean
   * @param expression Biểu thức
   * @param row Dữ liệu của hàng đang ở
   */
  function oneLinkButtonOfExpression(expression: string, row: any): boolean {
    if (!expression || expression == '') {
      return true;
    }
    // tên trường#Điều kiện#giá trị
    let arr = expression.split('#');
    //Lấy giá trị trường
    let fieldValue = row[arr[0]];
    //Lấy biểu thức
    let exp = arr[1].toLowerCase();
    //Biểu thức phán đoán
    if (exp === 'eq') {
      return fieldValue == arr[2];
    } else if (exp === 'ne') {
      return !(fieldValue == arr[2]);
    } else if (exp === 'empty') {
      if (arr[2] === 'true') {
        return !fieldValue || fieldValue == '';
      } else {
        return fieldValue && fieldValue.length > 0;
      }
    } else if (exp === 'in') {
      let arr2 = arr[2].split(',');
      return arr2.indexOf(String(fieldValue)) >= 0;
    }
    return false;
  }

  return {
    buttonSwitch,
    cgLinkButtonList,
    cgBIBtnMap,
    getQueryButtonCfg,
    getResetButtonCfg,
    getFormConfirmButtonCfg,
    cgTopButtonList,
    importUrl,
    registerModal,
    handleAdd,
    handleEdit,
    handleBatchDelete,
    handleAddTestData,
    testDataLoading,
    testDataBtnShow,
    registerImportModal,
    onImportExcel,
    onExportExcel,
    getDropDownActions,
    getActions,
    cgButtonJsHandler,
    cgButtonActionHandler,
    cgButtonLinkHandler,
    initButtonList,
    initButtonSwitch,
    getDeleteButton,
    handleSubmitFlow,
    getSubmitFlowButton,
    registerDetailModal,
    registerBpmModal,
    openDetailModal
  };
}
