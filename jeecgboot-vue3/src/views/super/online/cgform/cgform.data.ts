import { BasicColumn, FormSchema } from '/@/components/Table';
import { getDictItemsByCode } from '/@/utils/dict';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { buildUUID } from '/@/utils/uuid';

// Kiểm chứng thất bại flag
export const VALIDATE_FAILED = 'validate-failed';

export const columns: BasicColumn[] = [
  {
    title: 'Loại bảng',
    align: 'center',
    sorter: true,
    dataIndex: 'tableType',
    width: 140,
    customRender({ text, record }) {
      let tableTypeDictOptions = getDictItemsByCode('cgform_table_type');
      let tbTypeText = filterDictText(tableTypeDictOptions, text);
      if (record.isTree === 'Y') {
        tbTypeText += '(cây)';
      }
      if (record.themeTemplate === 'innerTable') {
        tbTypeText += '(Nhúng)';
      } else if (record.themeTemplate === 'erp') {
        tbTypeText += '(ERP)';
      } else if (record.themeTemplate === 'tab') {
        tbTypeText += '(TAB)';
      }
      return tbTypeText;
    },
  },
  {
    title: 'Tên bảng',
    sorter: true,
    align: 'center',
    dataIndex: 'tableName',
    width: 240,
  },
  {
    title: 'Mô tả bảng',
    align: 'center',
    dataIndex: 'tableTxt',
    width: 220,
  },
  {
    title: 'Phiên bản',
    align: 'center',
    dataIndex: 'tableVersion',
    width: 120,
  },
  {
    title: 'Trạng thái đồng bộ',
    align: 'center',
    sorter: true,
    dataIndex: 'isDbSynch',
    slots: { customRender: 'dbSync' },
    width: 120,
  },
  {
    title: 'Thời gian tạo',
    align: 'center',
    sorter: true,
    dataIndex: 'createTime',
    width: 240,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên bảng',
    field: 'tableName',
    component: 'JInput',
  },
  {
    label: 'Loại bảng',
    field: 'tableType_MultiString',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'cgform_table_type',
      mode: 'multiple',
    },
  },
  {
    label: 'Mô tả bảng',
    field: 'tableTxt',
    component: 'JInput',
  },
];

/** Mở rộngJSONGiá trị mặc định */
export const ExtConfigDefaultJson = {
  // Kết nối in báo cáo
  reportPrintShow: 0,
  reportPrintUrl: '',
  joinQuery: 0,
  modelFullscreen: 0,
  modalMinWidth: '',
  commentStatus: 0,
  tableFixedAction: 1,
  tableFixedActionType: 'right',
  // Excel Cách thức xuất hình ảnh (embed Nhúng hình ảnh,path đường dẫn hình ảnh)
  imageExportMode: 'path',
  // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
  formLabelLengthShow: 0,
  formLabelLength: null,
  // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
  // Có kích hoạt liên kết ngoài hay không
  enableExternalLink: 0,
  externalLinkActions: 'add,edit,detail',
  // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
  // Có bật đa nguồn dữ liệu hay không (chỉUITrạng thái, khi đóng thì xóa trống dbSource; cuối cùng lưu trữ bền vững vào onl_cgform_head.db_source cột)
  enableMultiDataSource: 0,
  dbSource: '',
  // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
};

/** Lấy dữ liệu khởi tạo của bảng chính */
export function useInitialData() {
  let initialData = [
    {
      dbFieldName: 'id',
      dbFieldTxt: 'Khóa chính',
      dbLength: 36,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '1',
      dbIsNull: '0',
      // table2
      isShowForm: '0',
      isShowList: '0',
      isReadOnly: '1',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'create_by',
      dbFieldTxt: 'Người tạo',
      dbLength: 50,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'create_time',
      dbFieldTxt: 'Ngày tạo',
      dbLength: 0,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'Datetime',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'datetime',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'update_by',
      dbFieldTxt: 'Người cập nhật',
      dbLength: 50,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'update_time',
      dbFieldTxt: 'Ngày cập nhật',
      dbLength: 0,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'Datetime',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'datetime',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'sys_org_code',
      dbFieldTxt: 'Phòng ban trực thuộc',
      dbLength: 64,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    // {
    //   dbFieldName: 'sys_org_code',
    //   dbFieldTxt: 'Phòng ban trực thuộc',
    //   dbLength: 50,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // },
    // {
    //   dbFieldName: 'sys_company_code',
    //   dbFieldTxt: 'Công ty trực thuộc',
    //   dbLength: 50,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // },
    // {
    //   dbFieldName: 'bpm_status',
    //   dbFieldTxt: 'Trạng thái quy trình',
    //   dbLength: 32,
    //   dbPointLength: 0,
    //   dbDefaultVal: '',
    //   dbType: 'string',
    //   dbIsKey: false,
    //   dbIsNull: true
    // }
  ];
  // tạm thời id, không lưu vào cơ sở dữ liệu
  let tempIds: string[] = [];
  initialData.forEach((record) => {
    record['id'] = buildUUID();
    tempIds.push(record['id']);
  });
  return { initialData, tempIds };
}

/** Lấy dữ liệu khởi tạo của cây */
export function useTreeNeedFields() {
  return [
    {
      dbFieldName: 'pid',
      dbFieldTxt: 'Nút cấp cha',
      dbLength: 32,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '1',
      isShowList: '0',
      fieldShowType: 'text',
      fieldLength: '200',
      queryMode: 'single',
      dbIsSync: '1'
    },
    {
      dbFieldName: 'has_child',
      dbFieldTxt: 'Có nút con hay không',
      dbLength: 3,
      dbPointLength: 0,
      dbDefaultVal: '',
      dbType: 'string',
      dbIsKey: '0',
      dbIsNull: '1',
      // table2
      isShowForm: '0',
      isShowList: '0',
      fieldShowType: 'list',
      fieldLength: '200',
      queryMode: 'single',
      // table3
      dictField: 'yn',
      dbIsSync: '1'
    },
  ];
}

/**
 * online Nút mặc định
 */
export const onlineDefaultButton = [
  { code: 'add', title: 'Thêm mới', status: 0 },
  { code: 'edit', title: 'Chỉnh sửa', status: 0 },
  { code: 'delete', title: 'Xóa', status: 0 },
  { code: 'export', title: 'Xuất (export)', status: 0 },
  { code: 'import', title: 'Nhập (import)', status: 0 },
  { code: 'query', title: 'Truy vấn', status: 0 },
];
