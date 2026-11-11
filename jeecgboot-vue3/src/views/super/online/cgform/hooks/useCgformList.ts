import { h, ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { Input, Radio, RadioGroup } from 'ant-design-vue';
import { ActionItem, BasicColumn, FormSchema } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useDrawer } from '/@/components/Drawer';
import { CgformPageType } from '../types';
import { useListPage } from '/@/hooks/system/useListPage';
import {
  doBatchDelete,
  doBatchRemove,
  doSingleDelete,
  doSingleRemove,
  doCopyOnlineView,
  doDatabaseSync,
  doCopyTable,
  list,
  queryHeadById,
} from '../cgform.api';
// import { useCopyModal } from './useCopyCgformModal';
import { isArray } from '/@/utils/is';
import {useCgformStore} from "../store/cgformState";
import {showListDeleteConfirm} from "./useCgformWidgets.jsx";
import { getTenantId, getToken } from '/@/utils/auth';
import { useGlobSetting } from '/@/hooks/setting';

interface IOptions {
  // Loại trang
  pageType: CgformPageType;
  designScope: string;
  columns: BasicColumn[];
  formSchemas: FormSchema[];
}

export function useCgformList(options: IOptions) {
  const isNormalPage = options.pageType === CgformPageType.normal;
  const router = useRouter();
  const glob = useGlobSetting();
  const cgformStore = useCgformStore();
  const tableRef = ref();
  // Tham số, phương thức dùng chung của trang danh sách
  const pageContext = useListPage({
    designScope: options.designScope,
    tableProps: {
      api: list,
      columns: options.columns,
      formConfig: {
        //labelWidth: 200,
        schemas: options.formSchemas,
      },
      beforeFetch: (params) => {
        let copyType = isNormalPage ? 0 : 1;
        let physicId = isNormalPage ? undefined : router.currentRoute.value.params.code;
        // TODO Sau khi các component từ điển v.v. hỗ trợ phân tách bằng dấu phẩy, việc xóa sửa mã 【LOWCOD-2371】
        if (isArray(params.tableType_MultiString)) {
          params.tableType_MultiString = params.tableType_MultiString.join(',');
        }
        return Object.assign(params, { copyType, physicId });
      },
    },
  });
  const { tableContext, createMessage: $message, createConfirm: $confirm } = pageContext;
  // Đăng kýtableDữ liệu
  const [, { reload, setLoading }, { selectedRowKeys, selectedRows }] = tableContext;

  // Đăng ký popup chỉnh sửa
  const [registerCgformModal, cgformModal] = useModal();
  // Đăng ký nhập biểu mẫu từ cơ sở dữ liệu
  const [registerDbToOnlineModal, dbToOnlineModal] = useModal();
  // Đăng kýaiPopup tạo bảng
  const [registerAiToOnlineModal, aiToOnlineModal] = useModal();
  // Đăng ký popup sinh mã
  const [registerCodeGeneratorModal, codeGeneratorModal] = useModal();
  // Đăng ký popup nút tùy chỉnh
  const [registerCustomButtonModal, customButtonModal] = useModal();
  // Đăng kýJSPopup tăng cường
  const [registerEnhanceJsModal, enhanceJsModal] = useModal();
  // Đăng kýSQLPopup tăng cường
  const [registerEnhanceSqlModal, enhanceSqlModal] = useModal();
  // Đăng kýJavaPopup tăng cường
  const [registerEnhanceJavaModal, enhanceJavaModal] = useModal();
  // Đăng ký ngăn kéo (drawer) quản lý quyền
  const [registerAuthManagerDrawer, authManagerDrawer] = useDrawer();
  // Đăng ký popup ủy quyền vai trò
  const [registerAuthSetterModal, authSetterModal] = useModal();

  function onAdd() {
    cgformModal.openModal(true, { isUpdate: false });
  }
  function onAiCreateTable(){
    aiToOnlineModal.openModal(true);
  }
  function  onCreateAiTable() {
    reload();
  }
  let thatRecord: Nullable<Recordable> = null

  async function onEdit(record) {
    thatRecord = record
    // update-begin---author:liusq---date:20261013---for:【LHZP-1967】Sau khi tạo thiết kế in, cấu hình mở rộng của biểu mẫu không mang theo địa chỉ báo cáo
    // Dữ liệu hàng trong danh sách có thể đã hết hạn (thiết kế in sẽ ghi ngược lại ở backend extConfigJson địa chỉ báo cáo trong đó), khi địa chỉ báo cáo trống thì truy vấn lại một lần cấu hình mới nhất,
    // Tránh popup hiển thị giá trị cũ, đồng thời khi lưu dùng cái cũ extConfigJson Ghi đè địa chỉ báo cáo mà backend đã ghi vào
    let latest = record;
    if (!getReportPrintUrl(record)) {
      try {
        const head = await queryHeadById(record.id);
        if (head && head.id) {
          latest = { ...record, ...head };
        }
      } catch (e) {
        console.warn('Lấy cấu hình mới nhất của biểu mẫu truy vấn thất bại, sử dụng dữ liệu danh sách', e);
      }
    }
    cgformModal.openModal(true, { isUpdate: true, record: latest });
    // update-end---author:liusq---date:20261013---for:【LHZP-1967】Sau khi tạo thiết kế in, cấu hình mở rộng của biểu mẫu không mang theo địa chỉ báo cáo
  }

  /**
   * Lấy địa chỉ báo cáo trong cấu hình mở rộng
   */
  function getReportPrintUrl(record) {
    try {
      return JSON.parse(record.extConfigJson || '{}')?.reportPrintUrl;
    } catch (e) {
      return '';
    }
  }

  function onSuccess() {
    if (thatRecord?.id) {
      cgformStore.addChangedTable(thatRecord.id)
      thatRecord = null
    }
    reload();
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(id) {
    await doSingleDelete(id);
    reload();
  }

  /**
   * Loại bỏ sự kiện
   */
  async function handleRemove(id) {
    await doSingleRemove(id);
    reload();
  }

  /**
   * Xóa một bản ghi dữ liệu
   * @param record
   */
  function onDeleteRecord(record: Recordable) {
    return showListDeleteConfirm(() => handleDelete(record.id), () => handleRemove(record.id))
  }

  // Xóa hàng loạt
  function onDeleteBatch() {
    let idList = selectedRowKeys.value as string[];
    if (idList.length <= 0) {
      $message.warning('Vui lòng chọn một bản ghi trước!');
      return;
    }
    showListDeleteConfirm(
      () => executeDelete(doBatchDelete, idList, true),
      () => executeDelete(doBatchRemove, idList, true)
    )
  }

  /**
   * Thực thi thao tác xóa
   * @param fn Phương thức xóa
   * @param idList Tham số xóa
   * @param clearSelected Xóa sạch lựa chọn
   */
  async function executeDelete(fn: Fn, idList: string[], clearSelected = false) {
    try {
      setLoading(true);
      const res = await fn(idList);
      reload();
      if (clearSelected) {
        selectedRowKeys.value = [];
      }
      return res;
    } finally {
      setLoading(false);
    }
    return Promise.reject();
  }

  // Hiển thị popup nút tùy chỉnh
  function onShowCustomButton() {
    getSelectedRows(([row]) => customButtonModal.openModal(true, { row }));
  }

  // Hiển thị js Popup tăng cường
  function onShowEnhanceJs() {
    getSelectedRows(([row]) => enhanceJsModal.openModal(true, { row }));
  }

  // Hiển thị sql Popup tăng cường
  function onShowEnhanceSql() {
    getSelectedRows(([row]) => enhanceSqlModal.openModal(true, { row }));
  }

  // Hiển thị java Popup tăng cường
  function onShowEnhanceJava() {
    getSelectedRows(([row]) => enhanceJavaModal.openModal(true, { row }));
  }

  // Hiển thị popup nhập bảng cơ sở dữ liệu
  function onImportDbTable() {
    dbToOnlineModal.openModal(true, {});
  }

  function getSelectedRows(fn: Fn, min = 1, max = 1) {
    if (selectedRows.value.length < min) {
      $message.warning(`Vui lòng chọn ít nhất trước ${min} bản ghi`);
    } else if (selectedRows.value.length > max) {
      $message.warning(`Chỉ được chọn tối đa ${min} bản ghi`);
    } else {
      fn(selectedRows.value);
    }
  }

  // Hiển thị popup sinh mã
  function onGenerateCode() {
    if (selectedRows.value.length === 0) {
      $message.warning('Vui lòng chọn một bản ghi trước');
    } else if (selectedRows.value.length > 1) {
      $message.warning('Sinh mã chỉ được chọn một bản ghi');
    } else {
      let row = selectedRows.value[0];
      if (!row) {
        $message.warning('Vui lòng chọn dữ liệu của trang hiện tại!');
      } else if (row.isDbSynch != 'Y') {
        $message.warning('Vui lòng đồng bộ cơ sở dữ liệu trước!');
      } else if (row.tableType == 3) {
        $message.warning('Vui lòng chọn bảng chính tương ứng với bảng này');
      } else {
        codeGeneratorModal.openModal(true, { code: row.id });
      }
    }
  }

  // Kiểm thử chức năng
  function onGoToTest(record) {
    console.log(record);
    if (record.isTree == 'Y') {
      router.push({ path: '/online/cgformTreeList/' + record.id });
    } else {
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      switch (record.themeTemplate) {
        case 'erp':
          router.push({ path: '/online/cgformErpList/' + record.id });
          break;
        case 'tab':
          router.push({ path: '/online/cgformTabList/' + record.id });
          break;
        case 'innerTable':
          router.push({ path: '/online/cgformInnerTableList/' + record.id });
          break;
        default:
          router.push({ path: '/online/cgformList/' + record.id });
          break;
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
    }
  }

  // update-begin---author:wangshuai---date:20260831---for:【LHZP-1967】OnlineBiểu mẫu tạo mẫu in báo cáo Jimu
  /**
   * MởOnlineTrang thiết kế báo cáo Jimu tương ứng với biểu mẫu.
   */
  function onPrintTemplate(record, regeneration = false) {
    const openTemplate = () => {
      const params = new URLSearchParams({
        onlineId: record.id,
        token: getToken() || '',
        regeneration: String(regeneration),
        tenantId: getTenantId() || '',
      });
      window.open(`${glob.domainUrl}/jmreport/generateTemplateByOnline?${params.toString()}`, '_blank');
    };
    if (!regeneration) {
      openTemplate();
      return;
    }
    $confirm({
      title: 'Tạo lại báo cáo in',
      content: 'Sau khi tạo lại, kiểu dáng báo cáo sẽ bị đặt lại, có tiếp tục không?',
      iconType: 'warning',
      onOk: openTemplate,
    });
  }
  // update-end---author:wangshuai---date:20260831---for:【LHZP-1967】OnlineBiểu mẫu tạo mẫu in báo cáo Jimu

  // Đồng bộ cơ sở dữ liệu
  function onSyncDatabase(record) {
    const syncMethod = ref('normal');
    const disabled = ref(false);
    const modalFunc = $confirm({
      iconType: 'info',
      title: 'Đồng bộ cơ sở dữ liệu',
      content: () =>
        h(
          'div',
          {
            style: 'margin: 20px 0;',
          },
          h(
            RadioGroup,
            {
              value: syncMethod.value,
              disabled: disabled.value,
              'onUpdate:value': (v) => (syncMethod.value = v),
            },
            () => [h(Radio, { value: 'normal' }, () => 'Đồng bộ thông thường (giữ dữ liệu bảng)'), h(Radio, { value: 'force' }, () => 'Đồng bộ bắt buộc (xóa bảng, tạo lại)')]
          )
        ),
      maskClosable: true,
      okText: 'Bắt đầu đồng bộ',
      async onOk() {
        disabled.value = true;
        modalFunc.update({
          maskClosable: false,
          keyboard: false,
          okText: 'Đang đồng bộ…',
          okButtonProps: { loading: disabled.value },
          cancelButtonProps: { disabled: disabled.value },
        });
        try {
          await doDatabaseSync(record.id, syncMethod.value);
        } catch (e: any) {
          // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-244】Đồng bộ cơ sở dữ liệu, khi không có quyền thì nhắc hai lần
          // $message.warn(e.message || e);
          // update-end--author:liaozhiyang---date:20240521---for：【TV360X-244】Đồng bộ cơ sở dữ liệu, khi không có quyền thì nhắc hai lần
        } finally {
          await reload();
          // update-begin--author:liaozhiyang---date:20250318---for：【issues/7931】Sau khi tích chọn rồi đồng bộ cơ sở dữ liệu, nhấp sinh mã lại thì không sinh được mã
          nextTick(() => {
            if (selectedRows.value.length) {
              selectedRows.value.forEach((item) => {
                const dataSource = tableRef.value.getDataSource() ?? [];
                const findItem = dataSource.find((o) => o['id'] === item['id']);
                if (findItem) {
                  Object.assign(item, findItem);
                }
              });
            }
          });
          // update-end--author:liaozhiyang---date:20250318---for：【issues/7931】Sau khi tích chọn rồi đồng bộ cơ sở dữ liệu, nhấp sinh mã lại thì không sinh được mã
        }
      },
    });
  }

  // const { createCopyModal } = useCopyModal();
  const [registerAddressModal, addressModal] = useModal();

  // Hiển thịonlinePopup địa chỉ
  function onShowOnlineUrl(record) {
    let onlineUrl: string;
    if (record.themeTemplate === 'erp') {
      onlineUrl = `/online/cgformErpList/${record.id}`;
    } else if (record.themeTemplate === 'innerTable') {
      onlineUrl = `/online/cgformInnerTableList/${record.id}`;
    } else if (record.themeTemplate === 'tab') {
      onlineUrl = `/online/cgformTabList/${record.id}`;
    } else if (record.isTree == 'Y') {
      onlineUrl = `/online/cgformTreeList/${record.id}`;
    } else {
      onlineUrl = `/online/cgformList/${record.id}`;
    }
    addressModal.openModal(true, {
      title: `Liên kết menu 【${record.tableTxt}】`,
      content: onlineUrl,
      copyText: onlineUrl,
      copyTitle: `${record.tableTxt}`,
      record,
    });
  }

  /**
   * Hiển thị popup sao chép bảng
   * @param record
   */
  function onCopyTable(record) {
    const tableName = ref(record.tableName + '_copy');
    $confirm({
      title: 'Sao chép bảng',
      content: () =>
        h(
          'div',
          {
            style: 'margin: 20px 0;',
          },
          [
            'Vui lòng nhập tên bảng mới:',
            h(Input, {
              value: tableName.value,
              'onUpdate:value': (v) => (tableName.value = v),
            }),
          ]
        ),
      iconType: 'info',
      closable: true,
      okText: 'Sao chép',
      onOk() {
        if (!tableName.value) {
          $message.warning('Vui lòng nhập tên bảng mới');
        //update-begin---wangshuai---date:20260713  for：【LHZP-465】Sau khi nhấp sao chép, danh sách cần làm mới thủ công mới thấy dữ liệu------------
            return Promise.reject(); // Ngăn chặn modal Đóng
        }
        if (tableName.value === record.tableName) {
          $message.warning('Tên bảng mới và tên bảng cũ không được giống nhau');
          return Promise.reject(); // Ngăn chặn modal Đóng
        }
        return new Promise((resolve, reject) => {
          doCopyTable(record.id, tableName.value)
            .then(() => {
              $message.success('Sao chép bảng thành công');
              // Bắt buộc nhảy tới thứ 1 trang rồi làm mới: sắp xếp mặc định là createTime desc, bảng mới sao chép rơi vào thứ 1 trang,
              // Nếu người dùng hiện đang ở thứ N trang, thông thường reload() sẽ dùng lại theo current, dẫn đến không nhìn thấy dữ liệu mới.
              reload({ page: 1 });
              resolve(true);
            })
            .catch((err) => {
              // Khi thất bại thì đưa promise reject, cho modal không đóng, lỗi do defHttp của errorMessageMode:'message' Gợi ý tự động
              reject(err);
            });
        });
      //update-end---author:wangshuai ---date:20260713  for：【LHZP-465】Sau khi nhấp sao chép, danh sách cần làm mới thủ công mới thấy dữ liệu------------
      },
    });
  }

  /**
   * Xóa view
   */
  function doDeleteView(record) {
    $confirm({
      title: 'Xóa',
      content: 'Xác nhận muốn xóa view này chứ?',
      iconType: 'warning',
      closable: true,
      maskClosable: true,
      onOk: () => {
        handleRemove(record.id)
      }
    })
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: () => onEdit(record),
      },
    ];
  }

  /**
   * Thanh thao tác dropdown
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: 'Đồng bộ cơ sở dữ liệu',
        onClick: () => onSyncDatabase(record),
        ifShow: () => isNormalPage && record.isDbSynch != 'Y',
      },
      {
        // TODO Kiểm thử chức năng
        label: 'Kiểm thử chức năng',
        class: ['low-app-hide'],
        onClick: () => onGoToTest(record),
        ifShow: () => (isNormalPage ? record.isDbSynch == 'Y' && record.tableType !== 3 : true),
      },
      {
        label: 'Địa chỉ cấu hình',
        class: ['low-app-hide'],
        onClick: () => onShowOnlineUrl(record),
        ifShow: () => (isNormalPage ? record.isDbSynch == 'Y' && record.tableType !== 3 : true),
      },
      {
        label: 'Kiểm soát quyền',
        onClick: () => authManagerDrawer.openDrawer(true, { cgformId: record.id, tableType: record.tableType, isTree: record.isTree }),
      },
      {
        label: 'Ủy quyền vai trò',
        onClick: () => authSetterModal.openModal(true, { cgformId: record.id }),
      },
      {
        label: 'Quản lý view',
        class: ['low-app-hide'],
        onClick: () => router.push(`/online/copyform/${record.id}`),
        ifShow: () => isNormalPage && record.hascopy == 1,
      },
      {
        label: 'Tạo view (chế độ xem)',
        class: ['low-app-hide'],
        // @ts-ignore
        popConfirm: {
          title: 'Xác nhận tạo view chứ?',
          placement: 'left',
          confirm: () => {
            setLoading(true);
            doCopyOnlineView(record.id)
              .then(() => {
                $message.success('Đã tạo view thành công');
              })
              .finally(() => {
                setLoading(false);
                reload();
              });
          },
        },
        ifShow: () => isNormalPage,
      },
      {
        label: 'Sao chép bảng',
        onClick: () => onCopyTable(record),
        ifShow: () => isNormalPage,
      },
      // update-begin---author:wangshuai---date:20260831---for:【LHZP-1967】OnlineBiểu mẫu tạo mẫu in báo cáo Jimu
      {
        label: 'Thiết kế in',
        onClick: () => onPrintTemplate(record),
        ifShow: () => isNormalPage && record.isDbSynch == 'Y' && record.tableType !== 3,
      },
      {
        label: 'Tạo bản in',
        onClick: () => onPrintTemplate(record, true),
        ifShow: () => isNormalPage && record.isDbSynch == 'Y' && record.tableType !== 3,
      },
      // update-end---author:wangshuai---date:20260831---for:【LHZP-1967】OnlineBiểu mẫu tạo mẫu in báo cáo Jimu
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】onlineTối ưu nhắc nhở khi xóa
      {
        label: 'Xóa',
        onClick: () => onDeleteRecord(record),
        ifShow: () => isNormalPage,
      },
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】onlineTối ưu nhắc nhở khi xóa
      {
        label: 'Xóa view',
        onClick: () => doDeleteView(record),
        ifShow: () => !isNormalPage,
      },
    ];
  }

  return {
    router,
    pageContext,
    onAdd,
    onAiCreateTable,
    onSuccess,
    onDeleteBatch,
    onImportDbTable,
    onGenerateCode,
    onShowCustomButton,
    onShowEnhanceJs,
    onShowEnhanceSql,
    onShowEnhanceJava,
    onCreateAiTable,
    getTableAction,
    getDropDownAction,
    registerCustomButtonModal,
    registerEnhanceJsModal,
    registerEnhanceSqlModal,
    registerEnhanceJavaModal,
    registerAuthManagerDrawer,
    registerAuthSetterModal,
    registerCgformModal,
    registerDbToOnlineModal,
    registerCodeGeneratorModal,
    registerAiToOnlineModal,
    registerAddressModal,
    tableRef,
  };
}
