import type { ExtConfigType } from '../../types';
import { Page, SpecialConfig, SETUP, ENHANCEJS } from '../../../cgform/types/onlineRender';
import { useRoute } from 'vue-router';
import { router } from '/@/router';
import { onBeforeUnmount, ref, toRaw, nextTick, provide } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { filterObj } from '/@/utils/common/compUtils';
import { useCustomHook, GET_FUN_BODY_REG } from './useCustomHook';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useModal } from '/@/components/Modal';
import { ERP } from "../../util/constant";
import {useMultipleTabStore} from "/@/store/modules/multipleTab";
import {useCgformStore} from "../../store/cgformState";
import { getMenus } from '/@/router/menus';
/**
 * contextMô tả thuộc tính của đối tượng
 * Ghi ở consolejsKhi tăng cường Inconsole.log(this)Có thể lấy được đối tượng này, chứng tỏ
 */
const CONTEXT_PROP_DESCRIPTION = {
  acceptHrefParams: '<p> Thông tin tham số lấy khi chuyển hướng',
  currentPage: '<p> Số trang hiện tại',
  currentTableName: '<p> Tên bảng hiện tại',
  description: '<p> Mô tả bảng hiện tại',
  hasChildrenField: '<p> Tên trường cho biết có nút con hay không, chỉ có hiệu lực với biểu mẫu dạng cây',
  isDesForm: '<p> xx',
  isTree: '<m> Có phải biểu mẫu dạng cây hay không ',
  loadData: '<m> Tải dữ liệu danh sách',
  pageSize: '<p> Số mục hiển thị mỗi trang',
  queryParam: '<p> Đối tượng điều kiện truy vấn, mỗi lần nhấp truy vấn mới cập nhật dữ liệu này',
  selectedRowKeys: '<p> của hàng đã chọnidmảng',
  sortField: '<p> Trường sắp xếp',
  sortType: '<p> Quy tắc sắp xếp',
  total: '<p> Tổng số trang',
  foreignKeyValue: '<p> Erpkhóa ngoại của bảng con một-nhiều chọn giá trị của trường bảng chính tương ứng',
  isErpSubTable: '<p> Có hay khôngErpbảng con một-nhiều',
  foreignKeyField: '<p> Erptrường khóa ngoại của bảng con một-nhiều',
  themeTemplate: '<p> Mẫu giao diện',
  isInnerSubTable: '<p> Có nhúng bảng con một-nhiều hay không',
  innerSubTableId: '<p>Bảng con một-nhiều nhúngID',
  innerSubTableName: '<p> Tên bảng con một-nhiều nhúng',
  mTableSelectedRcordId: '<p>của hàng mở rộng trong bảng chính nhúngid',
  innerSubTableFk: '<p>Trường khóa ngoại của bảng con nhúng',
  loading: '<p> Thiết lập/Lấyloading',
};

/**
 * onlineĐịa chỉ-Hằng số
 */
const onlineUrl = {
  getColumns: '/online/cgform/api/getColumns/',
  getQueryInfo: '/online/cgform/api/getQueryInfo/',
  getData: '/online/cgform/api/getData/',
  getTreeData: '/online/cgform/api/getTreeData/',
  optPre: '/online/cgform/api/form/',
  buttonAction: '/online/cgform/api/doButton',
  exportXls: '/online/cgform/api/exportXlsOld/',
  importXls: '/online/cgform/api/importXls/',
  startProcess: '/act/process/extActProcess/startMutilProcess',
  getErpColumns: '/online/cgform/api/getErpColumns/',
  // API yêu cầu dữ liệu của bảng con một-nhiều trong chủ đề nhúng
  list: '/online/cgform/api/subform/list/',
};

// Giá trị khởi tạo được cấu hình cho mỗi bảng
let config: SpecialConfig = {
  sortField: 'id',
  sortType: 'asc',
  currentPage: 1,
  pageSize: 10,
  total: 0,
  selectedRowKeys: [],
  queryParam: {},
  acceptHrefParams: {},
  description: '',
  currentTableName: '',
  isDesForm: false,
  desFormCode: '',
  cache: false,
  isTree: false,
  hasChildrenField: '',
};

/**
 * Cấu hình phân trang
 */
const metaPagination = {
  current: 1,
  pageSize: 10,
  pageSizeOptions: ['10', '20', '30'],
  showTotal: (total, range) => {
    return range[0] + '-' + range[1] + ' tổng cộng' + total + 'mục (điều)';
  },
  showQuickJumper: true,
  showSizeChanger: true,
  total: 0,
};

/**
 * Lấyonline Ngữ cảnh danh sách
 * 1.Hằng số, dùng toàn cục- Yêu cầuurl
 * 2.Tham số đặc biệt-Điều kiện truy vấn, cách sắp xếp, thông tin phân trang, của hàng đã chọnkeys(chờ kiểm thử, có phải chỉ vớikeycó liên quan, nếu vàrowcó liên quan, cần bỏ cấu hình này)
 * 3.loadDataphương thức, phương thức này được gọi ở nhiều nơi
 * setupChỉ cần thực thi một lần ở ngay phần đầu
 */
const { createMessage: $message, createErrorModal } = useMessage();

export function useOnlineTableContext(params: any = {}) {
  console.log('-------------------------useOnlineTableContext----------------------->');
  // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
  const tableId = params.code ?? '';
  const ID = ref(tableId);
  provide('tableId', ID);
  // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
  const route = useRoute();
  // của biểu mẫu truy vấn của trang danh sáchref
  const onlineQueryFormOuter = ref<any>();
  // Nút truy vấn nâng cao
  const superQueryButtonRef = ref<any>();
  // Cấu hình phân trang
  const pagination = ref<Page | false>(false);
  // table Dữ liệu Chuyển route là dữ liệu thay đổi ngay Trong tình huống bình thường sẽ chạy qua một lượtsetup Trong tình huống có bộ nhớ đệm Đi qua sự kiệnonActivated----Vấn đề là không cần yêu cầu lạicolumnkhông? Chỉ cần tải dữ liệu? Chờ kiểm thử
  const dataSource = ref<Array<object>>([]);
  // Dữ liệu danh sách hiện tại có phải do điều kiện truy vấn (truy vấn thường hoặc truy vấn nâng cao) tra ra hay không
  const hasQueryCondition = ref(false);
  // Bảng có tải lại hay không
  const tableReloading = ref(true);
  // onlineCấu hình mở rộng biểu mẫu
  const onlineExtConfigJson = ref<ExtConfigType>();
  // OnlineTrạng thái toàn cục của biểu mẫu
  const cgformStore = useCgformStore();
  // nhiềuTabTrạng thái
  const tabStore = useMultipleTabStore();

  const isConfigCurRoute = ref(false);
  const pageLoading = ref(false);
  
  let specialConfigMap: { [key: string | symbol ]: SpecialConfig } = {};
  const methods = {
    execButtonEnhance: function (code, record) {
      if (onlineTableContext[ENHANCEJS][code]) {
        if (SETUP === code) {
          executeEnhanceJsHook(code);
        } else {
          let row = toRaw(record);
          return onlineTableContext[ENHANCEJS][code].call(onlineTableContext, onlineTableContext, row);
        }
      } else if (onlineTableContext[ENHANCEJS][code + '_hook']) {

        //update-begin-author:taoyan date:2023-5-15 for: issues/516 Nút tùy chỉnh_hooktham số sau đórowVấn đề chưa xác định (xem#410） #516
        if(record){
          let row = toRaw(record);
          executeEnhanceJsHook(code + '_hook', row);
        }else{
          executeEnhanceJsHook(code + '_hook');
        }
        //update-end-author:taoyan date:2023-5-15 for: issues/516 Nút tùy chỉnh_hooktham số sau đórowVấn đề chưa xác định (xem#410） #516
        
      } else {
        console.log('Không tìm thấy phần tăng cường!', code);
      }
    },
    /**
     * get Có phải biểu mẫu dạng cây hay không
     * @param status Nếu có giá trị thì coi làsetPhương thức
     */
    isTree: function (status?) {
      if (typeof status === 'boolean') {
        //Đã truyền tham số thì thiết lập giá trị
        onlineTableContext['isTreeTable'] = status;
        return status;
      } else {
        return onlineTableContext['isTreeTable'];
      }
    },
  };

  function executeEnhanceJsHook(code, row?) {
    let str = onlineTableContext[ENHANCEJS][code].toLocaleString();
    let arr = str.match(GET_FUN_BODY_REG);
    if (arr.length > 1) {
      let temp = arr[1];
      executeJsEnhanced(temp, row);
    }
  }
  /**
   * Định nghĩa proxy dữ liệu Lấy giá trị thuận tiện onlineTableContext.queryParam
   * Đọc trực tiếponlineTableContext Cái lấy được là{}
   */
  const onlineTableContext: any = new Proxy(CONTEXT_PROP_DESCRIPTION, {
    get(_target: any, prop: string): any {
      //console.log('từSpecialConfigĐọc thuộc tính trong đó:'+prop)
      if (typeof methods[prop] === 'function') {
        return methods[prop];
      } else {
        let temp = specialConfigMap[ID.value];
        if (temp == null) {
          return temp;
        }
        return Reflect.get(temp, prop);
      }
    },
    set(_target: any, prop: string, value: any): boolean {
      //  console.log('Thiết lậpSpecialConfigThuộc tính:'+ prop, value)
      let temp = getCurrentPageSpecialConfigMap();
      if (typeof value === 'function') {
        // Nếu là hàm thì đặt vàomethodsvào trong
        return Reflect.set(methods, prop, value);
      } else {
        return Reflect.set(temp, prop, value);
      }
    },
    deleteProperty(_target, key) {
      // Khi chuyển đổi định tuyến, đóng trang, cần gọi phương thức này một lần để xóa cấu hình
      if (key === ID.value) {
        delete specialConfigMap[key];
        return true;
      } else {
        return false;
      }
    },
  });

  // cái mớijsTăng cường
  const { executeJsEnhanced } = useCustomHook({}, onlineTableContext);

  /**
   * Lấy biểu mẫu trên địa chỉ routeID
   */
  function getTableId() {
    let idValue = route.params.id as string;
    if (!idValue) {
      idValue = '';
    }
    return idValue;
  }

  onMountedOrActivated(({type}) => {
    // Đi qua route có bộ nhớ đệmActivated, cái chưa lưu bộ nhớ đệm thì đi quaMounted, đều cần đi qua một lần
    console.log('-------------------onMountedOrActivated-------------------');
    // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
    //erpcủa bảng con một-nhiềuidkhông thể lấy từ route của trang (khitableIdkhi tồn tại, không lấy từ route của trang)
    !tableId && handlePageChange();
    // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách

    if (type === 'activated') {
      // 【QQYUN-7151】Sau khi sửa biểu mẫu, trang kiểm thử chức năng đã mở không tự động làm mới
      if (cgformStore.checkIsChanged(ID.value)) {
        tabStore.refreshPage(router)
      }
    }

    if (ID.value) {
      cgformStore.removeChangedTable(ID.value);
    }

  });

  // Trước khi route đóng Xóa sạch (làm trống)mapCấu hình bên trong
  onBeforeUnmount(() => {
    console.log('-------------------onBeforeUnmount-------------------');
    delete specialConfigMap[ID.value];
    // Nếu đã lưu vào bộ nhớ đệm  Khi đóng sẽ gọi
    // Không có bộ nhớ đệm Chuyển route là sẽ gọi--Cái này không sao
    // nhưng kiểm thửonlineKhông có hiệu ứng này
  });

  /**
   * Lấy cấu hình trang hiện tại
   */
  function getCurrentPageSpecialConfigMap() {
    let temp = specialConfigMap[ID.value];
    if (!temp) {
      let obj = Object.assign({}, config, { onlineUrl });
      temp = JSON.parse(JSON.stringify(obj));
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      if (params['themeTemplate'] == ERP) {
        // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8387】ErpPhong cách và các phong cách khác cùng tồn tại, dẫn đến số trang hiện tại của phong cách khác không bình thường
        temp.pageSize = 5;
        // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8387】ErpPhong cách và các phong cách khác cùng tồn tại, dẫn đến số trang hiện tại của phong cách khác không bình thường
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      // update-begin--author:liaozhiyang---date:20250423---for：【issues/8117】jsPhần tăng cường có thể thiết lập, lấyloading
      // @ts-ignore
      temp.loading = pageLoading;
      // update-end--author:liaozhiyang---date:20250423---for：【issues/8117】jsPhần tăng cường có thể thiết lập, lấyloading
      specialConfigMap[ID.value] = temp;
    }
    return temp;
  }

  //Chấp nhậnURLTham số
  function handleAcceptHrefParams() {
    let acceptHrefParams = {};
    let hrefParam = route.query;
    if (hrefParam) {
      Object.keys(hrefParam).map((key) => {
        acceptHrefParams[key] = hrefParam[key];
      });
      // queryParam.value rawđối tượng
      onlineTableContext['acceptHrefParams'] = acceptHrefParams;
    }
  }

  /**
   * Truy vấntableThông tin cột và các cấu hình khác
   */
  function getColumnList(themeTemplate = '') {
    let url;
    // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
    if (themeTemplate == ERP) {
      // Erpmột-nhiều lấy bảng chính - bảng concolumns
      url = `${onlineTableContext.onlineUrl.getErpColumns}${ID.value}`;
    } else {
      url = `${onlineTableContext.onlineUrl.getColumns}${ID.value}`;
    }
    // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách

    //update-begin---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
    if(onlineTableContext['isInnerSubTable'] === true){
      url = url+ '?tabletype=3';
    }
    //update-end---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
    return new Promise((resolve, reject) => {
      defHttp
        .get(
          {
            url,
          },
          { isTransformResponse: false }
        )
        .then((res) => {
          // console.log(res)
          if (res.success) {
            resolve(res.result);
          } else {
            $message.warning(res.message);
            reject();
          }
        })
        .catch(() => {
          reject();
        });
    });
  }

  //Truy vấn dữ liệu
  /**
   * @param delNum number Số bản ghi đã xóa 【xóa hàng loạt, xóa】 khi gọi sẽ truyền
   */
  function loadData(options = {}) {
    const { delNum } = options;
    return new Promise((resolve, reject) => {
      // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-206】Danh sách xóa dữ liệu ở trang cuối, khi trang nhảy về trang trước thì dữ liệu trống
      if (delNum != null) {
        const { total, pageSize, current } = pagination.value;
        const lastPage = Math.ceil(total / pageSize);
        // Chỉ khi trang hiện tại là trang cuối cùng, việc xóa dữ liệu mới phán đoán có nhảy về trang trước hay không
        if (current === lastPage) {
          pagination.value.current = Math.ceil((total - delNum) / pageSize);
        }
      }
      // update-end--author:liaozhiyang---date:20240528---for：【TV360X-206】Danh sách xóa dữ liệu ở trang cuối, khi trang nhảy về trang trước thì dữ liệu trống
      let params = getLoadDataParams();
      let url = `${onlineTableContext.onlineUrl.getData}${ID.value}`;
      if (onlineTableContext.isTree() === true) {
        url = `${onlineTableContext.onlineUrl.getTreeData}${ID.value}`;
      } else if (onlineTableContext['isInnerSubTable'] === true) {
        // update-begin--author:liaozhiyang---date:20230822---for：【QQYUN-6305】Chủ đề nhúng một-nhiều
        url = `${onlineTableContext.onlineUrl.getData}${onlineTableContext['innerSubTableId']}`;
        params = Object.assign(params,{pageSize: -521 });
        // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9340】Dữ liệu bảng con nhúng đều được truy vấn ra hết
        if (onlineTableContext['innerSubTableFk'] && onlineTableContext['mTableSelectedRcordId']) {
          params[onlineTableContext['innerSubTableFk']] = onlineTableContext['mTableSelectedRcordId'];
        }
        // update-end--author:liaozhiyang---date:20240514---for：【QQYUN-9340】Dữ liệu bảng con nhúng đều được truy vấn ra hết
        // update-end--author:liaozhiyang---date:20230822---for：【QQYUN-6305】Chủ đề nhúng một-nhiều
        //update-begin---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
        url = url+ '?tabletype=3';
        //update-end---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
      }
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      // erptruy vấn bảng con một-nhiều cần thêm tham số
      if (onlineTableContext['isErpSubTable'] === true) {
        // update-begin--author:liaozhiyang---date:20250722---for：【issues/8575】erpMặc định chọn cái đầu tiên và khi chưa chọn bảng chính thì bảng con không truy vấn
        if (onlineTableContext['foreignKeyValue'] == undefined) {
          return;
        }
        // update-end--author:liaozhiyang---date:20250722---for：【issues/8575】erpMặc định chọn cái đầu tiên và khi chưa chọn bảng chính thì bảng con không truy vấn
        params[onlineTableContext['foreignKeyField']] = onlineTableContext['foreignKeyValue'];
        // 【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xem dữ liệu của bảng phụ thuộc
        params['tabletype'] = 3;
        delete params.hasQuery;
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      console.log('------Tham số truy vấn-----', params);
      defHttp
        .get({ url, params }, { isTransformResponse: false })
        .then((res) => {
          console.log('--onlineList-Truy vấn dữ liệu danh sách', res);
          if (res.success) {
            handleDataResult(res.result);
            resolve(true);
          } else {
            if (res.message === 'NO_DB_SYNC') {
              createErrorModal({
                title: 'Cơ sở dữ liệu chưa đồng bộ',
                content: 'Vui lòng đồng bộ cơ sở dữ liệu trước rồi xem trang này!',
                // Sau khi nhấp xác nhận, tự động quay lại trang trước
                onOk: () => router.back(),
              });
            } else {
              $message.warning(res.message);
            }
            reject(false);
          }
        })
        .catch(() => {
          let error = 'Yêu cầu dữ liệu danh sách bất thường!';
          $message.warning(error);
          reject(false);
        });
    });
  }

  /**
   * Lấy điều kiện truy vấn
   */
  function getLoadDataParams() {
    const { sortField, sortType, acceptHrefParams, queryParam } = onlineTableContext;
    // Tham số cây dùng đến
    const treeParam = {
      hasQuery: 'true',
    };
    if (onlineTableContext.isTree() === true) {
      // update-begin--author:liaozhiyang---date:20231205---for：【issues/888】onlineTìm kiếm nút con của bảng cây không có hiệu lực và có cảnh báo
      // update-begin--author:liusq---date:20260807---for: 【Truy vấn bảng cây】Khi trúng dữ liệu cấp con thì trả về cấp con và toàn bộ cấp bậc tổ tiên
      if (!hasTreeQueryCondition()) {
        treeParam['hasQuery'] = 'false';
      }
      // update-end--author:liusq---date:20260807---for: 【Truy vấn bảng cây】Khi trúng dữ liệu cấp con thì trả về cấp con và toàn bộ cấp bậc tổ tiên
      // update-end--author:liaozhiyang---date:20231205---for：【issues/888】onlineTìm kiếm nút con của bảng cây không có hiệu lực và có cảnh báo
    }
    let params = Object.assign({}, treeParam, acceptHrefParams, queryParam, { column: sortField, order: sortType });
    // TODO Truy vấn theo phạm vi Giá trị cố định gốc cần được làm rõ Chờ xóa
    /*let queryFields = queryFieldArray.value;
    for(let item of queryFields){
      if(item.mode!='single'){
        params[item.field] = ''
      }
    }*/
    if (pagination.value) {
      //Nếu phân trang
      params.pageNo = pagination.value.current;
      params.pageSize = pagination.value.pageSize;
    } else {
      // không phân trang thì truyền một giá trị cố định
      params['pageSize'] = -521;
    }

    let superQueryData = getSuperQueryData();
    //Truy vấn nâng cao
    params.superQueryMatchType = superQueryData.matchType || '';
    params.superQueryParams = superQueryData.params || '';
    return filterObj(params);
  }

  /**
   * liaozhiyang
   * 20261026
   * 【QQYUN-11801】Nút tạo dữ liệu kiểm thử phân biệt có điều kiện truy vấn hay không (truy vấn thông thường/truy vấn nâng cao)
   * Hiện tại có điều kiện truy vấn hay không (truy vấn thường hoặc truy vấn nâng cao)
   */
  function checkHasQueryCondition() {
    const { queryParam, superQuery } = onlineTableContext;
    const isEmpty = (value) => value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0);
    // Truy vấn thông thường: bất kỳ trường truy vấn nào có giá trị
    const hasNormalQuery = !!queryParam && Object.values(queryParam).some((value) => !isEmpty(value));
    // Truy vấn nâng cao: có mục điều kiện truy vấn
    const hasSuperQuery = Array.isArray(superQuery?.params) && superQuery.params.length > 0;
    return hasNormalQuery || hasSuperQuery;
  }

  // Sau khi truy vấn dữ liệu xong Lấy dữ liệu trang, tổng số dữ liệu
  function handleDataResult(result) {
    hasQueryCondition.value = checkHasQueryCondition();
    let total = 0;
    if (Number(result.total) > 0) {
      if (onlineTableContext.isTree() === true) {
        dataSource.value = getTreeDataByResult(result.records);
        // update-begin--author:liusq---date:20260807---for: 【Truy vấn bảng cây】Khi truy vấn không điền ngược dữ liệu cấp con không liên quan của các nút đã mở rộng trong lịch sử
        if (hasTreeQueryCondition()) {
          expandedRowKeys.value = [];
        } else {
          nextTick(() => {
            loadDataByExpandedRows(dataSource.value);
          });
        }
        // update-end--author:liusq---date:20260807---for: 【Truy vấn bảng cây】Khi truy vấn không điền ngược dữ liệu cấp con không liên quan của các nút đã mở rộng trong lịch sử
      } else {
        // update-begin--author:liaozhiyang---date:20250508---for：【issues/8168】idSắp xếp trùng lặp, dữ liệu bị nặng
        dataSource.value = [];
        nextTick(() => {
          dataSource.value = result.records;
        });
        // update-end--author:liaozhiyang---date:20250508---for：【issues/8168】idSắp xếp trùng lặp, dữ liệu bị nặng
      }
      total = Number(result.total);
    } else {
      dataSource.value = [];
    }
    if (pagination.value) {
      pagination.value = { ...pagination.value, total };
    }
  }

  //Kích hoạt khi phân trang, sắp xếp, sàng lọc thay đổi
  function handleChangeInTable($pagination, _filters, sorter) {
    if (sorter && sorter.order) {
      // Cần sắp xếp, lấy quy tắc sắp xếp trước
      onlineTableContext['sortField'] = sorter.field;
      onlineTableContext['sortType'] = 'ascend' == sorter.order ? 'asc' : 'desc';
    } else {
      // Không có quy tắc Đi qua sắp xếp mặc định
      onlineTableContext['sortField'] = 'id';
      onlineTableContext['sortType'] = 'asc';
    }
    if (pagination.value) {
      //console.log('$pagination111', $pagination)
      pagination.value = $pagination;
    }
    loadData();
  }

  /**
   * Trangidsau khi thay đổi,Thực thi truy vấnloadDataTrước đó sẽ thực thi phương thức này
   * Thiết lập thông tin bảng hiện tại theo kết quả truy vấn, thiết lập điều kiện truy vấn, thiết lập điều kiện truy vấn nâng cao, thông tin phân trang, thông tin sắp xếp
   * @param result
   */
  function handleSpecialConfig(result) {
    //1.Thiết lập thông tin bảng hiện tại theo kết quả truy vấn
    onlineTableContext['description'] = result.description;
    onlineTableContext['currentTableName'] = result.currentTableName;
    onlineTableContext['isDesForm'] = result.isDesForm;
    onlineTableContext['desFormCode'] = result.desFormCode;
    onlineTableContext['ID'] = ID.value;
    //2.Thiết lập điều kiện truy vấn
    let { acceptHrefParams, queryParam, superQuery, currentPage, pageSize } = onlineTableContext;
    handleAcceptHrefParams();
    if (!queryParam) {
      onlineTableContext['queryParam'] = {};
    } else {
      // Tăng cường phán đoán, ngăn ngừa trường hợp không có truy vấn
      if (onlineQueryFormOuter.value) {
        onlineQueryFormOuter.value.initDefaultValues(queryParam, acceptHrefParams);
      }
    }
    //3.Thiết lập điều kiện truy vấn nâng cao
    if (!superQuery) {
      onlineTableContext['superQuery'] = { params: '', matchType: '' };
    } else {
      // erpbảng con một-nhiều không có nút truy vấn nâng cao
      if (superQueryButtonRef.value) {
        superQueryButtonRef.value.initDefaultValues(superQuery);
      }
    }
    //4.Thông tin phân trang
    if (result.paginationFlag == 'Y') {
      // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-332】erpMặc định mỗi trang5mục, sau khi chuyển đổi mỗi trang5mục bị mất
      let pageSizeOptions: any = metaPagination.pageSizeOptions;
      if (params['themeTemplate'] == ERP) {
        pageSizeOptions = ['5', '10', '30'];
      }
      // update-end--author:liaozhiyang---date:20240527---for：【TV360X-332】erpMặc định mỗi trang5mục, sau khi chuyển đổi mỗi trang5mục bị mất
      pagination.value = { ...metaPagination, ...{ current: currentPage, pageSize, pageSizeOptions } };
    } else {
      pagination.value = false;
    }
    //5.Thông tin sắp xếp không cần thiết lập Không có khai báo hiển thị, vì vậy nhược điểm là: trên giao diện không nhìn ra cột nào đã được sắp xếp
  }

  /** Tải lại bảng, tạicolumnsKhi thông tin v.v. thay đổi, cần gọi */
  async function reloadTable() {
    tableReloading.value = true;
    await nextTick();
    tableReloading.value = false;
  }

  const add2Context = {
    loadData,
    getLoadDataParams,
    reloadTable,
  };
  Object.keys(add2Context).map((key) => {
    onlineTableContext[key] = add2Context[key];
  });

  //----------------------------------- Dưới đây là những nội dung liên quan đến truy vấn--------------------------------

  // Trạng thái tải truy vấn
  let loading = ref(false);
  // Truy vấn trang chủ
  async function reload(parameter:any = {}) {
    if (pagination.value) {
      // update-begin--author:liaozhiyang---date:20231207---for：【QQYUN-7414】onlineThao tác ngoại trừ truy vấn, các lần làm mới dữ liệu khác đều ở trang hiện tại (bao gồm thêm mới)
      pagination.value = { ...pagination.value, current: parameter.mode == 'search' || !pagination.value.current ? 1 : pagination.value.current };
      // update-end--author:liaozhiyang---date:20231207---for：【QQYUN-7414】onlineThao tác ngoại trừ truy vấn, các lần làm mới dữ liệu khác đều ở trang hiện tại (bao gồm thêm mới)
    }
    // update-begin--author:liusq---date:20260713---for：【LHZP-118】Khi tìm kiếm, xóa lựa chọn ở bảng chính, đảm bảo bảng con đồng bộ
    if (parameter.mode === 'search') {
      onlineTableContext.clearSelectedRow();
      // update-end--author:liaozhiyang---date:20260713---for：【LHZP-118】Khi tìm kiếm, xóa lựa chọn ở bảng chính, đảm bảo bảng con đồng bộ
    } else if (params['themeTemplate'] !== ERP) {
      // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
      onlineTableContext.clearSelectedRow();
      // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
    }
    //loading.value = true
    await loadData();
    //loading.value = false
  }

  //------------------------Danh sách dạng cây--------------------------
  function hasTreeQueryCondition() {
    const { queryParam, superQuery } = onlineTableContext;
    const hasBasicQuery = !!queryParam && Object.values(queryParam).some((value) => value !== undefined && value !== null && value !== '');
    return hasBasicQuery || (Array.isArray(superQuery?.params) && superQuery.params.length > 0);
  }

  function getTreeDataByResult(result) {
    if (result) {
      return result.map((item) => {
        // Khi kết quả truy vấn đã bao gồm chuỗi tổ tiên, giữ lại nút con thật, không được thay thế thành nút giữ chỗ tải lười nữa
        if (Array.isArray(item.children) && item.children.length > 0) {
          item.children = getTreeDataByResult(item.children);
          return item;
        }
        //Phán đoán có đánh dấu là mang nút con hay không
        let hasChildrenField = onlineTableContext['hasChildrenField'];
        if (item[hasChildrenField] == '1') {
          let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true };
          loadChild['jeecg_row_key'] = loadChild.id;
          item.children = [loadChild];
        }
        return item;
      });
    }
  }

  const expandedRowKeys = ref<string[]>([]);

  function handleExpandedRowsChange(expandedRowKeysValue) {
    //console.log(a,b)
    //console.log('handleExpandedRowsChange', expandedRowKeysValue, toRaw(expandedRowKeys.value))
    expandedRowKeys.value = expandedRowKeysValue;
  }

  // Truy vấn dữ liệu theo các hàng đã mở rộng (dùng để tải bất đồng bộ dữ liệu cấp con khi làm mới sau khi lưu)
  function loadDataByExpandedRows(dataList) {
    let expandedRowKeysValue = expandedRowKeys.value;
    if (expandedRowKeysValue.length > 0) {
      const { sortField, sortType, pidField } = onlineTableContext;
      let params = Object.assign({}, { column: sortField, order: sortType });
      params['hasQuery'] = 'in';
      //Truy vấn hàng loạt nút con của các nút đã mở rộng
      let superParams = Object.assign({});
      superParams.rule = 'in';
      superParams.type = 'text';
      superParams.val = expandedRowKeysValue.join(',');
      superParams.field = pidField;
      superParams = [superParams];
      params['superQueryParams'] = encodeURI(JSON.stringify(superParams));
      params['superQueryMatchType'] = 'and';
      params['batchFlag'] = 'true';
      let url = `${onlineTableContext.onlineUrl.getTreeData}${ID.value}`;
      console.log('--onlineList-Tham số truy vấn nút con', superParams);
      defHttp
        .get({ url, params }, { isTransformResponse: false })
        .then((res) => {
          console.log('--onlineList-Truy vấn dữ liệu danh sách nút con', res);
          if (res.success && res.result.records && res.result.records.length > 0) {
            //Các nút con hàng loạt của dữ liệu đã mở rộng
            let records = res.result.records;
            const listMap = new Map();
            for (let item of records) {
              let pid = item[pidField];
              if (expandedRowKeysValue.join(',').includes(pid)) {
                let mapList = listMap.get(pid);
                if (mapList == null) {
                  mapList = [];
                }
                mapList.push(item);
                listMap.set(pid, mapList);
              }
            }
            let childrenMap = listMap;
            let fn = (list) => {
              if (list) {
                list.forEach((data) => {
                  if (expandedRowKeysValue.includes(data.id)) {
                    data.children = getTreeDataByResult(childrenMap.get(data.id));
                    fn(data.children);
                  }
                });
              }
            };
            fn(dataList);
          }
        })
        .catch(() => {
          let error = 'loadDataByExpandedRowsYêu cầu dữ liệu danh sách bất thường!';
          $message.warning(error);
        });
    } else {
      return Promise.resolve();
    }
  }

  /**
   * Lấy điều kiện truy vấn nâng cao
   */
  function getSuperQueryData() {
    if (!onlineTableContext.superQuery) {
      return {};
    }
    const {
      superQuery: { params, matchType },
      currentTableName,
    } = onlineTableContext;
    let pre = currentTableName + '@';
    let arr: any[] = [];
    if (params.length > 0) {
      for (let data of params) {
        let item = { ...data };
        let field = item.field;
        if (field.startsWith(pre)) {
          item.field = field.replace(pre, '');
        }
        arr.push(item);
      }
    }
    let str = arr.length > 0 ? JSON.stringify(arr) : '';
    console.log('Điều kiện truy vấn nâng cao', arr, matchType);
    return {
      params: encodeURIComponent(str),
      matchType,
    };
  }

  /**Trạng thái truy vấn nâng cao-Có điều kiện truy vấn hay không*/
  const superQueryStatus = ref(false);

  /**
   * Đối tượng truy vấn nâng cao
   * 1.Thực thi của component truy vấn nâng caosearchsự kiện, cần gán giá trị chocontext
   * 2.Khi truy vấn, từcontextlấy giá trị tham số trong đó
   * 3.idKhi xảy ra thay đổi thì cần làm gì đó?nothing, giá trị trạng thái cần thay đổi
   */
  function handleSuperQuery(params, matchType) {
    // paramschắc chắn là một mảng, có thểsizelà0
    onlineTableContext['superQuery'] = {
      params,
      matchType,
    };
    // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7309】onlineTruy vấn nâng cao, nút lần thứ hai không có hiệu ứng động
    if (params.length == 0 || params.length == undefined) {
      superQueryStatus.value = false;
    } else {
      superQueryStatus.value = true;
    }
    // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7309】onlineTruy vấn nâng cao, nút lần thứ hai không có hiệu ứng động
    // update-begin--author:liaozhiyang---date:20231207---for：【QQYUN-7414】onlineThao tác ngoại trừ truy vấn, các lần làm mới dữ liệu khác đều ở trang hiện tại (bao gồm thêm mới)
    pagination.value.current = 1;
    // update-end--author:liaozhiyang---date:20231207---for：【QQYUN-7414】onlineThao tác ngoại trừ truy vấn, các lần làm mới dữ liệu khác đều ở trang hiện tại (bao gồm thêm mới)
    loadData();
  }

  /*------------------------Popup tùy chỉnh------------------------------*/
  const [registerCustomModal, { openModal: doOpenCustomModal }] = useModal();
  /**
   * Nút tùy chỉnh Kích hoạt popup
   * @param param
   */
  function openCustomModal(param) {
    if (!param) {
      param = {};
    }
    if (!param.row) {
      let rows = onlineTableContext['selectedRows'];
      if (!rows || rows.length == 0 || rows.length > 1) {
        $message.warning('Vui lòng chọn một bản ghi dữ liệu');
        return;
      }
      param.row = rows[0];
    }
    param['code'] = ID.value;
    doOpenCustomModal(true, param);
  }
  onlineTableContext['openCustomModal'] = openCustomModal;
  /*------------------------Popup tùy chỉnh------------------------------*/

  /**
   * Kích hoạt khi trang thay đổi
   */
  function handlePageChange() {
    let idValue = getTableId();
    ID.value = idValue;
  }
  // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
  //erpcủa bảng conidkhông thể lấy từ route của trang (khitableIdkhi tồn tại, không lấy từ route của trang)
  if (!tableId && !ID.value) {
    handlePageChange()
  }
  // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách

  /** Sự kiện kích hoạt sau khi truy vấn cấu hình biểu mẫu thành công (không cần đợi biểu mẫu mở mới kích hoạt) */
  function handleFormConfig(formConfig) {
    // Xử lý tham số mở rộng
    let extConfigJson = formConfig.head.extConfigJson;
    if (extConfigJson) {
      onlineExtConfigJson.value = JSON.parse(extConfigJson);
    }
  }
  /**
   * liaozhiyang
   * 20250407
   * 【QQYUN-11801】Tạo dữ liệu kiểm thử
   *  Phán đoán là menu đã cấu hình hay mở từ kiểm thử chức năng
   * */
  async function isConfigUrl() {
    const getMatchingisConfigUrl = (menus, path) => {
      for (let i = 0, len = menus.length; i < len; i++) {
        const item = menus[i];
        if (item.path === path && !item.redirect && !item.paramPath) {
          return true;
        } else if (item.children?.length) {
          const result = getMatchingisConfigUrl(item.children, path);
          if (result) {
            return result;
          }
        }
      }
      return false;
    };
    const path = route.path;
    const menus = await getMenus();
    const result = getMatchingisConfigUrl(menus, path);
    isConfigCurRoute.value = result;
  }
  isConfigUrl();
  return {
    ID,
    onlineQueryFormOuter,
    superQueryButtonRef,
    loading,
    reload,
    dataSource,
    hasQueryCondition,
    pagination,
    tableReloading,
    handleSpecialConfig,
    onlineTableContext,
    handleChangeInTable,
    getColumnList,
    getTreeDataByResult,
    expandedRowKeys,
    handleExpandedRowsChange,
    onlineExtConfigJson,
    handleFormConfig,
    superQueryStatus,
    handleSuperQuery,
    registerCustomModal,
    isConfigCurRoute,
    pageLoading,
    ...add2Context,
  };
}

/**
 * Tương thích phiên bản cũjsTăng cường Đóng gói đối tượng--Tạm thời chưa hỗ trợ
 *
 */
export function useCompatibleOldVersion(context) {
  Object.defineProperty(context, 'table', {
    get() {
      const arr = context['selectedRowKeys'];
      const arr2 = context['selectedRows'];
      return {
        selectedRowKeys: arr,
        selectedRows: arr2,
      };
    },
  });
}

/**
 * Gọi chuỗi??
 */
export class AopSetup {
  before: Function = () => {};
  after: Function = () => {};

  constructor(before, after) {
    if (typeof before == 'function') {
      this.before = before;
    }
    if (typeof after == 'function') {
      this.after = after;
    }
  }

  addTarget(prop, context) {
    let key;
    if (typeof prop == 'function') {
      key = prop.name;
    } else if (typeof prop == 'string') {
      key = prop;
    } else {
      return;
    }
    if (typeof context == 'object') {
      context[key] = this.around(context[key], context);
    }
  }
  addTargets(array, context) {
    for (let item of array) {
      this.addTarget(item, context);
    }
  }

  around(targetFunction, context) {
    const _that = this;
    return async function () {
      //console.log('this2', this)
      let res1 = await _that.before(arguments);
      console.log('beforeGiá trị trả về', res1);
      if (res1) {
        console.log('Thông tin lỗi', res1);
        return;
      }
      let result = await targetFunction.apply(context, arguments);
      await _that.after(arguments);
      return result;
    };
  }
}
