import { reactive, ref, Ref, unref } from 'vue';
import { merge } from 'lodash-es';
import { DynamicProps } from '/#/utils';
import { BasicTableProps, TableActionType, useTable } from '/@/components/Table';
import { ColEx } from '/@/components/Form/src/types';
import { FormActionType } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { useMethods } from '/@/hooks/system/useMethods';
import { useDesign } from '/@/hooks/web/useDesign';
import { filterObj } from '/@/utils/common/compUtils';
import { isFunction } from '@/utils/is';
const { handleExportXls, handleImportXls } = useMethods();

// Định nghĩa useListPage Tham số cần cho phương thức
interface ListPageOptions {
  // Phạm vi tác dụng của kiểu
  designScope?: string;
  // [Bắt buộc] Cấu hình tham số bảng
  tableProps: TableProps;
  // Có phân trang không
  pagination?: boolean;
  // Cấu hình xuất
  exportConfig?: {
    url: string | (() => string);
    // Tên tệp xuất
    name?: string | (() => string);
    //Tham số xuất
    params?: object | (() => object);
  };
  // Cấu hình import
  importConfig?: {
    // Giải thích logic mã: erpTạo mã Bảng con Địa chỉ import là động
    url: string | (() => string);
    // Callback sau khi xuất thành công
    success?: (fileInfo?: any) => void;
  };
}

interface IDoRequestOptions {
  // Có hiển thị hộp thoại xác nhận không, mặc định true
  confirm?: boolean;
  // Có tự động làm mới bảng không, mặc định true
  reload?: boolean;
  // Có tự động xóa lựa chọn không, mặc định true
  clearSelection?: boolean;
}

/**
 * listPagePhương thức chung của trang
 *
 * @param options
 */
export function useListPage(options: ListPageOptions) {
  const $message = useMessage();
  let $design = {} as ReturnType<typeof useDesign>;
  if (options.designScope) {
    $design = useDesign(options.designScope);
  }

  const tableContext = useListTable(options.tableProps);

  const [, { getForm, reload, setLoading, getColumns }, { selectedRowKeys }] = tableContext;

  // Xuất excel
  async function onExportXls() {
    // Giải thích logic mã: Xuất thêm tham số tùy chỉnh------------
    let { url, name, params } = options?.exportConfig ?? {};
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      let title = typeof name === 'function' ? name() : name;
      // Giải thích logic mã: erpTạo mã Bảng con Xuất báo lỗi, nguyên nhân chưa rõ-
      let paramsForm:any = {};
      try {
        //KhiuseSearchForKhông bằngfalsekhi, mới kích hoạtvalidate
        if (options?.tableProps?.useSearchForm !== false) {
          paramsForm = await getForm().validate();
          console.log('paramsForm', paramsForm);
          // Ở đây thực thibeforeFetch
          if (options?.tableProps?.beforeFetch) {
            paramsForm = await options?.tableProps?.beforeFetch(paramsForm);
          }
        }
      } catch (e) {
        console.warn(e);
      }

      // Giải thích logic mã: [/issues/409]Chức năng xuất không xuất theo kết quả sắp xếp,Cài đặt sắp xếp mặc định khi export, thời gian tạo giảm dần
      if(!paramsForm?.column){
         Object.assign(paramsForm,{column:'createTime',order:'desc'});
      }

      //Nếu tham số không trống, thì gộp lại với nhau
      // Giải thích logic mã: erpTạo mã Bảng con Cài đặt động xuấtmainId
      if (params) {
        // Giải thích logic mã: [QQYUN-11627]Tạo mã biểu mẫu gốc, xuất dữ liệu, frontend báo lỗi, và tham số phạm vi chưa được chuyển đổi #7962
        const realParams = isFunction(params) ? await params() : { ...(params || {}) };
        Object.keys(realParams).map((k) => {
          let temp = (realParams as object)[k];
          if (temp) {
            paramsForm[k] = unref(temp);
          }
        });
      }
      if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
        paramsForm['selections'] = selectedRowKeys.value.join(',');
      }

      //Có hiển thị cài đặt bảng không
      if(options?.tableProps?.showTableSetting !== false){
        //Có hiển thị trường cấu hình không
        if(options?.tableProps?.tableSetting?.setting !== false){
          await exportColumns(paramsForm)
        }
      }
    
      return handleExportXls(title as string, realUrl, filterObj(paramsForm));
    } else {
      $message.createMessage.warn('Chưa truyền exportConfig.url Tham số');
      return Promise.reject();
    }
  }

  /**
   * Cài đặt cột export
   * 
   * @param paramsForm
   */
  async function exportColumns(paramsForm: any) {
    //Lấy cột của bảng
    let columns = getColumns();
    if(columns && columns.length >0){
      //Trường cần export
      let exportFields:any = [];
      //Có cột ẩn không
      let hiddenColumns = false;
      for (const column of columns) {
        if(!column.defaultHidden){
          let dataIndex = column?.dataIndex;
          if(column?.dataIndex?.toString()?.indexOf('_dictText') !== -1){
            dataIndex = column?.dataIndex?.toString().replace('_dictText','')
          }
          exportFields.push(dataIndex);
        } else {
          hiddenColumns = true;
        }
      }
      if(hiddenColumns){
        paramsForm['exportFields'] = exportFields.join(",");
      }
    }
  }

  // Import excel
  function onImportXls(file) {
    let { url, success } = options?.importConfig ?? {};
    // Giải thích logic mã: erpTạo mã Bảng con Địa chỉ import là động
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      return handleImportXls(file, realUrl, success || reload);
    } else {
      $message.createMessage.warn('Chưa truyền importConfig.url Tham số');
      return Promise.reject();
    }
  }

  /**
   * Phương thức xử lý yêu cầu chung, có thể tự động làm mới bảng, tự động xóa lựa chọn
   * @param api Yêu cầuapi
   * @param options Có hiển thị hộp xác nhận không
   */
  function doRequest(api: () => Promise<any>, options?: IDoRequestOptions) {
    return new Promise((resolve, reject) => {
      const execute = async () => {
        try {
          setLoading(true);
          const res = await api();
          if (options?.reload ?? true) {
            reload();
          }
          if (options?.clearSelection ?? true) {
            selectedRowKeys.value = [];
          }
          resolve(res);
        } catch (e) {
          reject(e);
        } finally {
          setLoading(false);
        }
      };
      if (options?.confirm ?? true) {
        $message.createConfirm({
          iconType: 'warning',
          title: 'Xóa',
          content: 'Bạn có chắc chắn muốn xóa không?',
          onOk: () => execute(),
          onCancel: () => reject(),
        });
      } else {
        execute();
      }
    });
  }

  /** Thực hiện một thao tác xóa */
  function doDeleteRecord(api: () => Promise<any>) {
    return doRequest(api, { confirm: false, clearSelection: false });
  }

  return {
    ...$design,
    ...$message,
    onExportXls,
    onImportXls,
    doRequest,
    doDeleteRecord,
    tableContext,
  };
}

// Định nghĩa tham số cần cho bảng
type TableProps = Partial<DynamicProps<BasicTableProps>>;
type UseTableMethod = TableActionType & {
  getForm: () => FormActionType;
};

/**
 * useListTable Tham số bảng chuẩn của trang danh sách
 *
 * @param tableProps Tham số bảng
 */
export function useListTable(tableProps: TableProps): [
  (instance: TableActionType, formInstance: UseTableMethod) => void,
  TableActionType & {
    getForm: () => FormActionType;
  },
  {
    rowSelection: any;
    selectedRows: Ref<Recordable[]>;
    selectedRowKeys: Ref<any[]>;
  }
] {
  // Cấu hình cột tự thích ứng
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 12, // ≥576px
    md: 12, // ≥768px
    lg: 8, // ≥992px
    xl: 8, // ≥1200px
    xxl: 6, // ≥1600px
  };
  const defaultTableProps: TableProps = {
    rowKey: 'id',
    // Sử dụng vùng điều kiện truy vấn
    useSearchForm: true,
    // Cấu hình vùng điều kiện truy vấn
    formConfig: {
      // Chế độ gọn
      compact: true,
      // labelChiều rộng mặc định
      // labelWidth: 120,
      // Nhấn Enter sẽ tự động gửi
      autoSubmitOnEnter: true,
      // Mặc định row Cấu hình
      rowProps: { gutter: 8 },
      // Mặc định col Cấu hình
      baseColProps: {
        ...adaptiveColProps,
      },
      labelCol: {
        xs: 24,
        sm: 8,
        md: 6,
        lg: 8,
        xl: 6,
        xxl: 6,
      },
      wrapperCol: {},
      // Có hiển thị không Mở rộng/Thu gọn Nút
      showAdvancedButton: true,
      // Vượt số cột chỉ định thì mặc định thu gọn
      autoAdvancedCol: 3,
      // Cấu hình nút thao tác
      actionColOptions: {
        ...adaptiveColProps,
        style: { textAlign: 'left' },
      },
    },
    // Vằn ngựa vằn
    striped: false,
    // Có thể tự thích ứng chiều cao không
    canResize: true,
    // Chiều cao tối thiểu bảng
    // update-begin--author:liaozhiyang---date:20240603---for【TV360X-861]Vùng truy vấn danh sách không cuộn lên được
    minHeight: 300,
    // update-end--author:liaozhiyang---date:20240603---for【TV360X-861]Vùng truy vấn danh sách không cuộn lên được
    // Nhấp hàng để chọn
    clickToRowSelect: false,
    // Có hiển thị viền không
    bordered: true,
    // Có hiển thị cột số thứ tự không
    showIndexColumn: false,
    // Hiển thị cài đặt bảng
    showTableSetting: true,
    // Cài đặt toàn màn hình bảng
    tableSetting: {
      fullScreen: false,
    },
    // Có hiển thị cột thao tác không
    showActionColumn: true,
    // Cột thao tác
    actionColumn: {
      width: 120,
      title: 'Thao tác',
      //Có khóa giá trị cột thao tác không right ,left,false
      fixed: false,
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  };
  // Hợp nhất cấu hình cá nhân hóa của người dùng
  if (tableProps) {
    // Giải thích logic mã: 【issues/6180]Cấu hình mã frontend cho bảng, đổi điều kiện truy vấn thì cột hiển thị không có hiệu lực---
    if(tableProps.formConfig){
      setTableProps(tableProps.formConfig);
    }
    // merge Phương thức có thể hợp nhất sâu đối tượng
    merge(defaultTableProps, tableProps);
  }

  // Phương thức gọi trước khi gửi yêu cầu
  function beforeFetch(params) {
    // Kiểm tra đã có tham số sắp xếp chưa (defSort Khi là mảng sẽ chuyển thành defSortString)
    const hasSortParams = params.column || params.defSortString || params.order;
    // Mặc định với createTime Sắp xếp giảm dần
    return Object.assign(hasSortParams ? {} : { column: 'createTime', order: 'desc' }, params);
  }

  // Phương thức hợp nhất
  Object.assign(defaultTableProps, { beforeFetch });
  if (typeof tableProps.beforeFetch === 'function') {
    defaultTableProps.beforeFetch = function (params) {
      params = beforeFetch(params);
      // @ts-ignore
      tableProps.beforeFetch(params);
      return params;
    };
  }

  // Hàng đang chọn
  const selectedRowKeys = ref<any[]>([]);
  // Bản ghi hàng đã chọn
  const selectedRows = ref<Recordable[]>([]);

  // Cấu hình cột chọn bảng
  const rowSelection: any = tableProps?.rowSelection ?? {};
  const defaultRowSelection = reactive({
    ...rowSelection,
    type: rowSelection.type ?? 'checkbox',
    // Chiều rộng cột chọn, mặc định 50
    columnWidth: rowSelection.columnWidth ?? 50,
    selectedRows: selectedRows,
    selectedRowKeys: selectedRowKeys,
    onChange(...args) {
      selectedRowKeys.value = args[0];
      selectedRows.value = args[1];
      if (typeof rowSelection.onChange === 'function') {
        rowSelection.onChange(...args);
      }
    },
  });
  delete defaultTableProps.rowSelection;

  /**
   * Cài đặt tham số bảng
   *
   * @param formConfig
   */
  function setTableProps(formConfig: any) {
    const replaceAttributeArray: string[] = ['baseColProps','labelCol'];
    for (let item of replaceAttributeArray) {
      if(formConfig && formConfig[item]){
        if(defaultTableProps.formConfig){
          let defaultFormConfig:any = defaultTableProps.formConfig;
          defaultFormConfig[item] = formConfig[item];
        }
        formConfig[item] = {};
      }
    }
  }

  return [
    ...useTable(defaultTableProps),
    {
      selectedRows,
      selectedRowKeys,
      rowSelection: defaultRowSelection,
    },
  ];
}
