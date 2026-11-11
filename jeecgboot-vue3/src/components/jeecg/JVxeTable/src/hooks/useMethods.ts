import { Ref, watch } from 'vue';
import XEUtils from 'xe-utils';
import { simpleDebounce } from '/@/utils/common/compUtils';
import { JVxeDataProps, JVxeRefs, JVxeTableProps, JVxeTypes } from '../types';
import { getEnhanced } from '../utils/enhancedUtils';
import { VxeTableInstance, VxeTablePrivateMethods } from 'vxe-table';
import { cloneDeep } from 'lodash-es';
import { isArray, isEmpty, isNull, isString } from '/@/utils/is';
import { useLinkage } from './useLinkage';
import { useWebSocket } from './useWebSocket';
import { getPrefix, getJVxeAuths } from '../utils/authUtils';
import { excludeKeywords } from '../componentMap';
import { useColumnsCache } from './useColumnsCache';
import { isEnabledVirtualYScroll } from '/@/components/jeecg/JVxeTable/utils';

export function useMethods(props: JVxeTableProps, { emit }, data: JVxeDataProps, refs: JVxeRefs, instanceRef: Ref) {
  let xTableTemp: VxeTableInstance & VxeTablePrivateMethods;

  function getXTable() {
    if (!xTableTemp) {
      // !. là khẳng định không null của typescript
      xTableTemp = refs.gridRef.value!.getRefMaps().refTable.value;
    }
    return xTableTemp;
  }

  // noinspection JSUnusedGlobalSymbols
  const hookMethods = {
    getXTable,
    addRows,
    pushRows,
    insertRows,
    addOrInsert,
    setValues,
    getValues,
    getTableData,
    getNewData,
    getNewDataWithId,
    getIfRowById,
    getNewRowById,
    getDeleteData,
    getSelectionData,
    getSelectedData,
    removeRows,
    removeRowsById,
    removeSelection,
    resetScrollTop,
    validateTable,
    fullValidateTable,
    clearSelection,
    filterNewRows,
    isDisabledRow,
    recalcDisableRows,
    rowResort,
  };

  // Liên động nhiều cấp
  const linkageMethods = useLinkage(props, data, hookMethods);
  // WebSocket Làm mới không dấu vết
  const socketMethods = useWebSocket(props, data, hookMethods);

  // Phương thức có thể gọi trực tiếp từ bên ngoài
  const publicMethods = {
    ...hookMethods,
    ...linkageMethods,
    ...socketMethods,
  };

  /** Lắng nghe vị trí thanh cuộn vxe */
  function handleVxeScroll(event) {
    let { scroll } = data;

    // Ghi lại vị trí thanh cuộn
    scroll.top = event.scrollTop;
    scroll.left = event.scrollLeft;

    refs.subPopoverRef.value?.close();
    data.scrolling.value = true;
    closeScrolling();
  }

  // Sự kiện được kích hoạt khi chọn thủ công chọn đơn
  function handleVxeRadioChange(event) {
    let row = event.$table.getRadioRecord();
    data.selectedRows.value = row ? [row] : [];
    handleSelectChange('radio', data.selectedRows.value, event);
  }

  // Sự kiện được kích hoạt khi chọn thủ công chọn tất cả
  function handleVxeCheckboxAll(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox-all', data.selectedRows.value, event);
  }

  // Sự kiện kích hoạt khi tick thủ công và giá trị thay đổi
  function handleVxeCheckboxChange(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox', data.selectedRows.value, event);
  }

  // Sự kiện change khi chọn hàng
  function handleSelectChange(type, selectedRows, $event) {
    let action;
    if (type === 'radio') {
      action = 'selected';
    } else if (type === 'checkbox') {
      action = selectedRows.includes($event.row) ? 'selected' : 'unselected';
    } else {
      action = 'selected-all';
    }

    data.selectedRowIds.value = selectedRows.map((row) => row.id);
    trigger('selectRowChange', {
      type: type,
      action: action,
      $event: $event,
      row: $event.row,
      selectedRows: data.selectedRows.value,
      selectedRowIds: data.selectedRowIds.value,
    });
  }

  // Sự kiện kích hoạt khi nhấn vào ô
  function handleCellClick(event) {
    let { row, column, $event, $table } = event;

    // Đã nhấn vào ô có thể chỉnh sửa
    if (column.editRender) {
      refs.subPopoverRef.value?.close();
      return;
    }

    // Hiển thị thông tin chi tiết
    if (column.params?.showDetails) {
      refs.detailsModalRef.value?.open(event);
    } else if (refs.subPopoverRef.value) {
      refs.subPopoverRef.value.toggle(event);
    } else if (props.clickSelectRow) {
      let className = $event.target.className || '';
      className = isString(className) ? className : className.toString();
      // Nhấp vào expand thì không xử lý
      if (className.includes('vxe-table--expand-btn')) {
        return;
      }
      // Bấm vào là checkbox, không xử lý
      if (className.includes('vxe-checkbox--icon') || className.includes('vxe-cell--checkbox')) {
        return;
      }
      // Nhấp vào radio thì không xử lý
      if (className.includes('vxe-radio--icon') || className.includes('vxe-cell--radio')) {
        return;
      }

      // Giải thích logic mã: 【issues/9038] JVxeTable bảng bấm dòng để chọnBUG---
      if(!data.disabledRowIds.includes(row.id)){
        if (props.rowSelectionType === 'radio') {
          $table.setRadioRow(row);
          handleVxeRadioChange(event);
        } else {
          $table.toggleCheckboxRow(row);
          handleVxeCheckboxChange(event);
        }
      }

    }
  }

  // Sự kiện này sẽ kích hoạt khi ô được kích hoạt chỉnh sửa
  function handleEditActived({ column }) {
    // Thực thi tăng cường
    getEnhanced(column.params.type).aopEvents.editActived!.apply(instanceRef.value, arguments as any);
  }

  // Sự kiện này sẽ kích hoạt khi ô đang ở trạng thái chỉnh sửa bị đóng
  function handleEditClosed({ column }) {
    // Thực thi tăng cường
    getEnhanced(column.params.type).aopEvents.editClosed!.apply(instanceRef.value, arguments as any);
  }

  // Giá trị trả về quyết định hàng có thể chọn được không
  function handleCheckMethod({ row }) {
    if (props.disabled) {
      return false;
    }
    return !data.disabledRowIds.includes(row.id);
  }

  // Giá trị trả về quyết định ô có thể chỉnh sửa hay không
  function handleActiveMethod({ row, column }) {
    let flag = (() => {
      if (props.disabled) {
        return false;
      }
      if (data.disabledRowIds.includes(row.id)) {
        return false;
      }
      if (column.params?.disabled) {
        return false;
      }
      // Thực thi tăng cường
      return getEnhanced(column.params.type).aopEvents.activeMethod!.apply(instanceRef.value, arguments as any) ?? true;
    })();
    if (!flag) {
      // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1404] cảnh báo vxetable
      getXTable().clearEdit();
      // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1404] cảnh báo vxetable
    }
    return flag;
  }

  /**
   * Kiểm tra có phải dòng bị vô hiệu hóa không
   * @param row Dữ liệu hàng
   * @param rowIndex Số dòng
   * @param force Có bắt buộc kiểm tra không
   */
  function isDisabledRow(row, rowIndex: number | boolean = -1, force = true) {
    if(typeof rowIndex === 'boolean'){
      force = rowIndex;
      rowIndex = -1;
    }
    if (!force) {
      return !data.disabledRowIds.includes(row.id);
    }
    if (props.disabledRows == null || isEmpty(props.disabledRows)) {
      return false;
    }
    let disabled: boolean = false;
    let keys: string[] = Object.keys(props.disabledRows);
    for (const key of keys) {
      // Kiểm tra có thuộc tính đó không
      if (row.hasOwnProperty(key)) {
        let value = row[key];
        let temp: any = props.disabledRows![key];
        // Quy tắc vô hiệu hóa có thể là một hàm
        if (typeof temp === 'function') {
          disabled = temp(value, row, rowIndex);
        } else if (isArray(temp)) {
          // Quy tắc vô hiệu hóa có thể là một mảng
          disabled = temp.includes(value);
        } else {
          // Quy tắc vô hiệu hóa có thể là một giá trị cụ thể
          disabled = temp === value;
        }
        if (disabled) {
          break;
        }
      }
    }
    return disabled;
  }

  // Tính lại các hàng bị vô hiệu hóa
  function recalcDisableRows() {
    let xTable = getXTable();
    data.disabledRowIds = [];
    const { tableFullData } = xTable.internalData;
    tableFullData.forEach((row, rowIndex) => {
      // Kiểm tra có phải dòng bị vô hiệu hóa không
      if (isDisabledRow(row, rowIndex)) {
        data.disabledRowIds.push(row.id);
      }
    });
    xTable.updateData();
  }

  // Lắng nghe disabledRows, khi thay đổi thì tính toán lại các dòng bị vô hiệu hóa
  watch(
    () => props.disabledRows,
    () => recalcDisableRows()
  );

  // Giá trị trả về quyết định có cho phép mở rộng/thu gọn dòng hay không
  function handleExpandToggleMethod({ expanded }) {
    return !(expanded && props.disabled);
  }

  // Cài đặt data.scrolling Chế độ debounce
  const closeScrolling = simpleDebounce(function () {
    data.scrolling.value = false;
  }, 100);

  /** Phương thức xử lý dữ liệu chân bảng, dùng để hiển thị thông tin thống kê */
  function handleFooterMethod({ columns, data: $data }) {
    const { statistics } = data;
    let footers: any[] = [];
    if (statistics.has) {
      if (statistics.sum.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: 'Tổng cộng',
            checks: statistics.sum,
            method: (column) => XEUtils.sum($data, column.property),
          })
        );
      }
      if (statistics.average.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: 'Trung bình',
            checks: statistics.average,
            method: (column) => XEUtils.mean($data, column.property),
          })
        );
      }
    }
    return footers;
  }

  /** Lấy thống kê ở cuốiMap */
  function getFooterStatisticsMap({ columns, title, checks, method }) {
    return columns.map((column, columnIndex) => {
      if (columnIndex === 0) {
        return title;
      }
      if (checks.includes(column.property)) {
        return method(column, columnIndex);
      }
      return null;
    });
  }

  // Tạo dòng mới, tự động thêm giá trị mặc định
  function createRow(record: Recordable = {}) {
    let xTable = getXTable();
    // Thêm giá trị mặc định
    xTable.internalData.tableFullColumn.forEach((column) => {
      let col = column.params;
      // Cột không thể đăng ký thì không lấy tăng cường
      if (col && !excludeKeywords.includes(col.type)) {
        if (col.key && (record[col.key] == null || record[col.key] === '')) {
          // Đặt giá trị mặc định
          let createValue = getEnhanced(col.type).createValue;
          let defaultValue = col.defaultValue ?? '';
          let ctx = { context: { row: record, column, $table: xTable } };
          record[col.key] = createValue(defaultValue, ctx);
        }
        // Xử lý cột liên động
        if (col.type === JVxeTypes.select && data.innerLinkageConfig.size > 0) {
          // Kiểm tra xem cột hiện tại có phải là cột liên động không
          if (data.innerLinkageConfig.has(col.key)) {
            let configItem = data.innerLinkageConfig.get(col.key);
            linkageMethods.getLinkageOptionsAsync(configItem, '');
          }
        }
      } else if (col?.type === JVxeTypes.hidden) {
        record[col.key] = col.defaultValue ?? '';
      }
    });
    return record;
  }

  async function addOrInsert(rows: Recordable | Recordable[] = {}, index, triggerName, options?: IAddRowsOptions) {
    let xTable = getXTable();
    let records;
    if (isArray(rows)) {
      records = rows;
    } else {
      records = [rows];
    }
    // Duyệt và thêm giá trị mặc định
    records.forEach((record) => createRow(record));
    let setActive = options?.setActive ?? props.addSetActive ?? true;
    let result = await pushRows(records, { index: index, setActive });
    // Duyệt các hàng đã chèn
    // online jsKhi tăng cường, lấy giá trị truyền qua làm chuẩn, không gán giá trị mặc định nữa
    if (!(options?.isOnlineJS ?? false)) {
      if (triggerName != null) {
        for (let i = 0; i < result.rows.length; i++) {
          let row = result.rows[i];
          trigger(triggerName, {
            row: row,
            rows: result.rows,
            insertIndex: index,
            $table: xTable,
            target: instanceRef.value,
            isModalData: options?.isModalData
          });
        }
      }
    }
    return result;
  }

  // Tham số tùy chọn khi thêm mới, chèn một dòng
  interface IAddRowsOptions {
    // Có phải do tăng cường onlineJS kích hoạt hay không
    isOnlineJS?: boolean;
    // Có kích hoạt trạng thái chỉnh sửa không
    setActive?: boolean;
    //Có cần kích hoạt sự kiện change hay không
    emitChange?:boolean
    // Có phải dữ liệu được thêm từ popup modal hay không
    isModalData?:boolean
  }

  /**
   * Thêm một hoặc nhiều hàng
   *
   * @param rows
   * @param options Tham số
   * @return
   */
  async function addRows(rows: Recordable | Recordable[] = {}, options?: IAddRowsOptions) {
    // Giải thích logic mã: VUEN-1892[Popup bảng con online]Khi có JS liên kết chính-phụ, popup bảng con đã sửa dữ liệu nhưng trường bảng chính chưa được sửa
    let result = await addOrInsert(rows, -1, 'added', options);
    if(options && options!.emitChange==true){
      trigger('valueChange', {column: 'all', row: result.row})
    }
    // Giải thích logic mã: 【TV360X-279] Chỉnh sửa dòng thêm trường mới cuộn đến vị trí tương ứng
    let xTable = getXTable();
    setTimeout(() => {
      xTable.scrollToRow(result.row);
    }, 0);
    return result;
  }

  /**
   * Thêm một hoặc nhiều dòng dữ liệu tạm thời, sẽ không điền giá trị mặc định, truyền gì thì thêm vào đó
   * @param rows
   * @param options Tùy chọn
   * @param options.setActive Có kích hoạt chế độ chỉnh sửa của dòng cuối cùng hay không
   */
  async function pushRows(rows: Recordable | Recordable[] = {}, options = { setActive: false, index: -1 }) {
    let xTable = getXTable();
    let { setActive, index } = options;
    index = index === -1 ? index : xTable.internalData.tableFullData[index];
    index = index == null ? -1 : index;
    // Chèn hàng
    let result = await xTable.insertAt(rows, index);
    if (setActive) {
      // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1404] cảnh báo vxetable
      // Kích hoạt chế độ chỉnh sửa của hàng cuối cùng
      xTable.setEditRow(result.rows[result.rows.length - 1], true);
      // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1404] cảnh báo vxetable
    }
    await recalcSortNumber();
    return result;
  }

  /**
   * Chèn một hoặc nhiều hàng dữ liệu tạm thời
   *
   * @param rows
   * @param index Thêm chỉ số dưới, số, bắt buộc
   * @param options Tham số
   * @return
   */
  function insertRows(rows: Recordable | Recordable[] = {}, index: number, options?: IAddRowsOptions) {
    if (index < 0) {
      console.warn(`[JVxeTable]insertRows: index bắt buộc phải truyền số, và lớn hơn-1`);
      return;
    }
    return addOrInsert(rows, index, 'inserted', options);
  }

  /** Lấy giá trị trong biểu mẫu của bảng */
  function getValues(callback, rowIds) {
    let tableData = getTableData({ rowIds: rowIds });
    // Giải thích logic mã: 【issues/7631]Sửa tham số hàm callback getValues của component JVxeTable
    callback(tableData, tableData);
  }

  type getTableDataOptions = {
    rowIds?: string[];
    // Có giữ lại id của hàng mới khôngid
    keepNewId?: boolean;
  }

  /** Lấy dữ liệu bảng */
  function getTableData(options: getTableDataOptions = {}) {
    let { rowIds } = options;
    let tableData;
    // Chỉ truy vấn dòng có id chỉ định
    if (isArray(rowIds) && rowIds.length > 0) {
      tableData = [];
      rowIds.forEach((rowId) => {
        let { row } = getIfRowById(rowId);
        if (row) {
          tableData.push(row);
        }
      });
    } else {
      // Truy vấn tất cả các hàng
      tableData = getXTable().getTableData().fullData;
    }
    return filterNewRows(tableData, {
      keepNewId: options.keepNewId ?? false,
      removeNewLine: false,
    });
  }

  /** Chỉ lấy dữ liệu mới thêm */
  function getNewData() {
    let newData = getNewDataWithId();
    newData.forEach((row) => delete row.id);
    return newData;
  }

  /** Chỉ lấy dữ liệu mới thêm,kèm theoid */
  function getNewDataWithId() {
    let xTable = getXTable();
    return cloneDeep(xTable.getInsertRecords());
  }

  /** Lấy dòng theo ID, dòng mới thêm cũng có thể tra ra */
  function getIfRowById(id) {
    let xTable = getXTable();
    let row = xTable.getRowById(id),
      isNew = false;
    if (!row) {
      row = getNewRowById(id);
      if (!row) {
        console.warn(`JVxeTable.getIfRowById: không tìm thấy hàng có id là"${id}"hàng`);
        return { row: null };
      }
      isNew = true;
    }
    return { row, isNew };
  }

  /** Lấy hàng mới thêm qua ID tạm thời */
  function getNewRowById(id) {
    let records = getXTable().getInsertRecords();
    for (let record of records) {
      if (record.id === id) {
        return record;
      }
    }
    return null;
  }

  type filterNewRowsOptions = {
    keepNewId?: boolean;
    removeNewLine?: boolean;
  } | boolean

  /**
   * Lọc các hàng đã thêm
   * @param rows Dữ liệu hàng cần lọc
   * @param optOrRm Nếu truyền boolean thì đó là tham số removeNewLine (true = Xóa mục mới thêm,false=chỉ xóa id), nếu truyền đối tượng thì là tham số cấu hình
   * @param handler function
   */
  function filterNewRows(rows, optOrRm:filterNewRowsOptions = true, handler?: Fn) {
    let insertRecords = getXTable().getInsertRecords();
    let records: Recordable[] = [];
    optOrRm = typeof optOrRm === 'boolean' ? { removeNewLine: optOrRm } : optOrRm;
    // true = Xóa mục mới thêm,false=Chỉ xóaid
    let removeNewLine = optOrRm?.removeNewLine ?? true;
    for (let row of rows) {
      // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
      let item = { ...row };
      // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
      if (insertRecords.includes(row)) {
        handler ? handler({ item, row, insertRecords }) : null;
        if (removeNewLine) {
          continue;
        }
        if (!optOrRm?.keepNewId) {
          delete item.id;
        }
      }
      records.push(item);
    }
    return records;
  }

  /**
   * Đặt lại vị trí Top của thanh cuộn
   * @param top Vị trí top mới, để trống thì cuộn đến vị trí đã ghi lại lần trước, dùng để giải quyết vấn đề màn hình trắng khi chuyển tab và tự động cuộn thanh cuộn lên đầu
   */
  function resetScrollTop(top?) {
    let xTable = getXTable();
    xTable.scrollTo(null, top == null || top === '' ? data.scroll.top : top);
  }

  /** Kiểm chứng table, thất bại trả về errMap, thành công trả vềnull */
  async function validateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.validate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  /** Kiểm chứng đầy đủ */
  async function fullValidateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.fullValidate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  type setValuesParam = { rowKey: string; values: Recordable };

  /**
   * Đặt giá trị của một hàng, một cột
   *
   * @param values
   * @return Trả về số lượng ô bị ảnh hưởng
   */
  function setValues(values: setValuesParam[]): number {
    if (!isArray(values)) {
      console.warn(`[JVxeTable] setValues Phải truyền mảng`);
      return 0;
    }
    let xTable = getXTable();
    let count = 0;
    values.forEach((item) => {
      let { rowKey, values: record } = item;
      let { row } = getIfRowById(rowKey);
      if (!row) {
        return;
      }
      Object.keys(record).forEach((colKey) => {
        let column = xTable.getColumnByField(colKey);
        if (column) {
          let oldValue = row[colKey];
          let newValue = record[colKey];
          if (newValue !== oldValue) {
            row[colKey] = newValue;
            // Kích hoạt sự kiện valueChange
            trigger('valueChange', {
              type: column.params.type,
              value: newValue,
              oldValue: oldValue,
              col: column.params,
              column: column,
              isSetValues: true,
              row: {...row}
            });
            count++;
          }
        } else {
          console.warn(`[JVxeTable] setValues Không tìm thấy cột có key là"${colKey}"cột`);
        }
      });
    });
    if (count > 0) {
      xTable.updateData();
    }
    return count;
  }

  /** Xóa hàng đã chọn */
  async function clearSelection() {
    const xTable = getXTable();
    let event = { $table: xTable, target: instanceRef.value };
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      await xTable.clearRadioRow();
      handleVxeRadioChange(event);
    } else {
      await xTable.clearCheckboxRow();
      handleVxeCheckboxChange(event);
    }
  }

  /**
   * Lấy dữ liệu đã chọn
   * @param isFull Nếu isFull=true thì lấy dữ liệu đã chọn của toàn bộ bảng
   */
  function getSelectionData(isFull?: boolean) {
    const xTable = getXTable();
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      return filterNewRows([row], false);
    } else {
      return filterNewRows(xTable.getCheckboxRecords(isFull), false);
    }
  }

  /** Chỉ lấy dữ liệu đã bị xóa (dữ liệu mới thêm rồi lại bị xóa sẽ không được lấy) */
  function getDeleteData() {
    return filterNewRows(getXTable().getRemoveRecords(), false);
  }

  /** Xóa một hoặc nhiều dòng dữ liệu */
  async function removeRows(rows, asyncRemove = false) {
    // Giải thích logic mã: vxe-table removeRowsPhương thức thêm xóa bất đồng bộ
    const xTable = getXTable();
    const removeEvent: any = { deleteRows: rows, $table: xTable };
    if (asyncRemove) {
      const selectedRows = Array.isArray(rows) ? rows : [rows];
      const deleteOldRows = filterNewRows(selectedRows);
      if (deleteOldRows.length) {
        return new Promise((resolve) => {
          // Xác nhận xóa, chỉ khi gọi phương thức này mới thực sự xóa
          removeEvent.confirmRemove = async () => {
            const insertRecords = xTable.getInsertRecords();
            selectedRows.forEach((item) => {
              // Xóa dữ liệu mới thêmid
              if (insertRecords.includes(item)) {
                delete item.id;
              }
            });
            const res = await xTable.remove(rows);
            await recalcSortNumber();
            resolve(res);
          };
          trigger('removed', removeEvent);
        });
      } else {
        // Dòng hoàn toàn mới xóa ngay lập tức, không chờ đợi.
        const res = await xTable.remove(rows);
        removeEvent.confirmRemove = () => {};
        trigger('removed', removeEvent);
        await recalcSortNumber();
        return res;
      }
    } else {
      const res = await xTable.remove(rows);
      trigger('removed', removeEvent);
      await recalcSortNumber();
      return res;
    }
  }

  /** Xóa một hoặc nhiều hàng theo id */
  function removeRowsById(rowId) {
    let rowIds;
    if (isArray(rowId)) {
      rowIds = rowId;
    } else {
      rowIds = [rowId];
    }
    let rows = rowIds
      .map((id) => {
        let { row } = getIfRowById(id);
        if (!row) {
          return;
        }
        if (row) {
          return row;
        } else {
          console.warn(`【JVxeTable】removeRowsById：${id}Không tồn tại`);
          return null;
        }
      })
      .filter((row) => row != null);
    return removeRows(rows);
  }

  // Xóa dữ liệu đã chọn
  async function removeSelection() {
    let xTable = getXTable();
    let res;
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      res = await xTable.removeRadioRow();
    } else {
      res = await xTable.removeCheckboxRow();
    }
    await clearSelection();
    await recalcSortNumber();
    return res;
  }

  /** Tính lại giá trị của trường sắp xếp */
  async function recalcSortNumber(force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sortKey = props.sortKey ?? 'orderNum';
      let sortBegin = props.sortBegin ?? 0;
      xTable.internalData.tableFullData.forEach((data) => (data[sortKey] = sortBegin++));
      // 4.1.0
      //await xTable.updateCache();
      // 4.1.1
      await xTable.cacheRowMap(true)
      return await xTable.updateData();
    }
  }

  /**
   * Sắp xếp bảng
   * @param oldIndex
   * @param newIndex
   * @param force Sắp xếp bắt buộc
   */
  async function doSort(oldIndex: number, newIndex: number, force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sort = (array) => {
        // Lưu dữ liệu old và xóa mục đó
        let row = array.splice(oldIndex, 1)[0];
        // Thêm dữ liệu old vào vị trí newIndex
        array.splice(newIndex, 0, row);
      };
      sort(xTable.internalData.tableFullData);
      if (xTable.keepSource) {
        sort(xTable.internalData.tableSourceData);
      }
      // -update-begin--author:liaozhiyang---date:20240620---for：【TV360X-585] Kéo trường thì cuộn ảo không hoạt động tốt
      if (isEnabledVirtualYScroll(props, xTable)) {
        await xTable.loadData(xTable.internalData.tableFullData);
      }
      // -update-end--author:liaozhiyang---date:20240620---for：【TV360X-585] Kéo trường thì cuộn ảo không hoạt động tốt
      return await recalcSortNumber(force);
    }
  }

  /** Sắp xếp lại hàng */
  function rowResort(oldIndex: number, newIndex: number) {
    return doSort(oldIndex, newIndex, true);
  }

  // ---------------- begin Kiểm soát quyền hạn ----------------
  // Tải quyền hạn
  function loadAuthsMap() {
    if (!props.authPre || props.authPre.length == 0) {
      data.authsMap.value = null;
    } else {
      data.authsMap.value = getJVxeAuths(props.authPre);
    }
  }

  /**
   * Lấy quyền theo mã quyền
   * @param authCode
   */
  function getAuth(authCode) {
    if (data.authsMap.value != null && props.authPre) {
      let prefix = getPrefix(props.authPre);
      return data.authsMap.value.get(prefix + authCode);
    }
    return null;
  }

  // Lấy quyền cột
  function getColAuth(key: string) {
    return getAuth(key);
  }

  // Kiểm tra quyền nút
  function hasBtnAuth(key: string) {
    return getAuth('btn:' + key)?.isAuth ?? true;
  }

  // ---------------- end Kiểm soát quyền hạn ----------------

  /* --- Phương thức hỗ trợ ---*/

  function created() {
    loadAuthsMap();
  }

  // Kích hoạt sự kiện
  function trigger(name, event: any = {}) {
    event.$target = instanceRef.value;
    event.$table = getXTable();
    //onlineTương thích tham số tăng cường
    event.target = instanceRef.value;
    emit(name, event);
  }

  /**
   * Lấy hàng đã chọn-Khác với getSelectionData ở chỗ cũng trả về các hàng mới thêmID
   * Dùng choonlinePopForm
   * @param isFull
   */
  function getSelectedData(isFull?: boolean) {
    const xTable = getXTable();
    let rows:any[] = []
    if (['radio', JVxeTypes.rowRadio].includes(props.rowSelectionType ?? '')) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      rows = [row]
    } else {
      rows = xTable.getCheckboxRecords(isFull)
    }
    let records: Recordable[] = [];
    for (let row of rows) {
      // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
      records.push({ ...row });
      // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
    }
    return records;
  }
  /**
   *  2024-03-21
   *  liaozhiyang
   *  VXETableTên trường cache lưu cấu hình cột
   * */
  function handleCustom({ type, $grid }) {
    const { saveSetting, resetSetting } = useColumnsCache({ cacheColumnsKey: props.cacheColumnsKey });
    if (type === 'confirm') {
      saveSetting($grid);
    } else if (type == 'reset') {
      resetSetting($grid);
    }
  }

  return {
    methods: {
      trigger,
      ...publicMethods,
      closeScrolling,
      doSort,
      recalcSortNumber,
      handleVxeScroll,
      handleVxeRadioChange,
      handleVxeCheckboxAll,
      handleVxeCheckboxChange,
      handleFooterMethod,
      handleCellClick,
      handleEditActived,
      handleEditClosed,
      handleCheckMethod,
      handleActiveMethod,
      handleExpandToggleMethod,
      getColAuth,
      hasBtnAuth,
      handleCustom,
    },
    publicMethods,
    created,
  };
}
