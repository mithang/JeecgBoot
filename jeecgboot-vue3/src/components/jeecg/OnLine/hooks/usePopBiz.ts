import { reactive, ref, unref, defineAsyncComponent, toRaw, markRaw, isRef, watch, onUnmounted } from 'vue';
import { httpGroupRequest } from '/@/components/Form/src/utils/GroupRequest';
import { defHttp } from '/@/utils/http/axios';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil.js';
import { useMessage } from '/@/hooks/web/useMessage';
import { OnlineColumn } from '/@/components/jeecg/OnLine/types/onlineConfig';
import { h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMethods } from '/@/hooks/system/useMethods';
import { importViewsFile, _eval } from '/@/utils';
import {getToken} from "@/utils/auth";
import {replaceUserInfoByExpression} from "@/utils/common/compUtils";
import { isString } from '/@/utils/is';

export function usePopBiz(ob, tableRef?) {
  // Giải thích logic mã: 【issues/675] Dữ liệu popup của trường bảng con không cập nhật
  let props: any;
  if (isRef(ob)) {
    props = ob.value;
    const stopWatch = watch(ob, (newVal) => {
      props = newVal;
    });
    onUnmounted(() => stopWatch());
  } else {
    props = ob;
  }
  const { createMessage } = useMessage();
  //Trạng thái hiển thị popup
  const visible = ref(false);
  //Tải bảng
  const loading = ref(false);
  //cgRpConfigId
  const cgRpConfigId = ref('');
  //Tiêu đề
  const title = ref('Danh sách');
  // Trường sắp xếp, mặc định không sắp xếp
  const iSorter = ref<any>('');
  // Đối tượng tìm kiếm
  const queryInfo = ref([]);
  // Tham số truy vấn
  const queryParam = ref<any>({});
  // Tham số động
  const dynamicParam = ref<any>({});
  //Mục cấu hình danh mục
  const dictOptions = ref({});
  //Tập dữ liệu
  const dataSource = ref<Array<object>>([]);
  //Định nghĩa thông tin bảng
  const columns = ref<Array<object>>([]);
  // Route hiện tại
  const route = useRoute();
  //Định nghĩa thông tin URL yêu cầu
  const configUrl = reactive({
    //Trang danh sách tải column vàdata
    getColumnsAndData: '/online/cgreport/api/getColumnsAndData/',
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
    getQueryInfo: '/online/cgreport/api/getQueryInfo/',
    export: '/online/cgreport/api/exportManySheetXls/',
  });
  //Giá trị đã chọn
  const checkedKeys = ref<Array<string | number>>([]);
  //Bản ghi hàng đã chọn
  const selectRows = ref<Array<any>>([]);
  // Nhấp vào ô để chọn dòng, popup cần nhưng xem trước báo cáo thì không cần
  let clickThenCheckFlag = true;
  if (props.clickToRowSelect === false) {
    clickThenCheckFlag = false;
  }

  /**
   * Cấu hình cột lựa chọn
   */
  const rowSelection = {
    fixed: true,
    type: props.multi ? 'checkbox' : 'radio',
    selectedRowKeys: checkedKeys,
    selectionRows: selectRows,
    onChange: onSelectChange,
  };

  /**
   * Cấu hình cột số thứ tự
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: '15px',
  };
  /**
   * Cấu hình phân trang
   */
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    // showTotal: (total, range) => {
    //     return range[0] + '-' + range[1] + ' Tổng cộng' + total + 'mục'
    // },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
    // Logic tính tổng [Cần tối ưu 3.0]
    showTotal: (total) => onShowTotal(total),
    realPageSize: 10,
    realTotal: 0,
    // Có cột tổng cộng không, mặc định là"", sau lần đầu tiên lấy được dữ liệu sẽ được thiết kế thành ture hoặcfalse
    isTotal: <string | boolean>'',
    onShowSizeChange: (current, pageSize) => onSizeChange(current, pageSize),
  });

  /**
   * Sự kiện chọn bảng
   * @param selectedRowKeys
   * @param selectRow
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    // Sửa lỗi giá trị đã chọn khi chỉnh sửa chọn đơn ở trang thứ hai, khi lưu ở trang thứ nhất thì lưu không được
    const prevRows = [...selectRows.value];
    // Giải thích logic mã: 【QQYUN-7514] hiển thị radio khi popup chọn đơnradio
    if (!props.multi) {
      selectRows.value = [];
      checkedKeys.value = [];
      // selectedRowKeys = [selectedRowKeys[selectedRowKeys.length - 1]];
    }
    // Giải thích logic mã: 【QQYUN-4263] vấn đề xuất khi chọn qua nhiều trang
    if (!selectedRowKeys || selectedRowKeys.length == 0) {
      selectRows.value = [];
      checkedKeys.value = [];
    } else {
      if (selectRows.value.length > selectedRowKeys.length) {
        // Hủy
        selectRows.value.forEach((item, index) => {
          const rowKey = combineRowKey(item);
          if (!selectedRowKeys.find((key) => key === rowKey)) {
            selectRows.value.splice(index, 1);
          }
        });
      } else {
        // Thêm mới
        const append: any = [];
        const beforeRowKeys = selectRows.value.map((item) => combineRowKey(item));
        selectedRowKeys.forEach((key) => {
          if (!beforeRowKeys.find((item) => item === key)) {
            // Đó chính là dòng mới được chọn thêm
            // update-begin--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa popup, popup hiển thị lại phần đã tick
            const row = getRowByKey(key) || prevRows.find((item) => combineRowKey(item) === key);
            // update-end--author:liaozhiyang---date:20261029---for：【LHZP-3] Khi chỉnh sửa popup, popup hiển thị lại phần đã tick
            row && append.push(row);
          }
        });
        selectRows.value = [...selectRows.value, ...append];
      }
      checkedKeys.value = [...selectedRowKeys];
    }
  }
  /**
   * Lọc các tùy chọn không dùng
   * @param selectedRowKeys
   */
  function filterUnuseSelect() {
    selectRows.value = unref(selectRows).filter((item) => {
      let combineKey = combineRowKey(item);
      return unref(checkedKeys).indexOf(combineKey) >= 0;
    });
  }

  /**
   * Lấy thông tin row theo key
   * @param key
   */
  function getRowByKey(key) {
    let row = unref(dataSource).filter((record) => combineRowKey(record) === key);
    return row && row.length > 0 ? row[0] : '';
  }

  /**
   * TảirowKey
   */
  function combineRowKey(record) {
    let res = record?.id || '';
    if (props?.rowkey) {
      // Giải thích logic mã: 【issues/3656] Hiển thị lại popupdict
      res = record[props.rowkey];
    } else {
      Object.keys(record).forEach((key) => {
        res = key == 'rowIndex' ? record[key] + res : res + record[key];
      });
      res = res.length > 50 ? res.substring(0, 50) : res;
    }
    return res;
  }

  /**
   * Tải thông tin cột
   */
  function loadColumnsInfo() {
    const {code} = handleCodeParams(true)
    let url = `${configUrl.getColumns}${code}`;
    //Cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = res.result.cgRpConfigId;
        title.value = res.result.cgRpConfigName;
        let currColumns = res.result.columns;
        for (let a = 0; a < currColumns.length; a++) {
          if (currColumns[a].customRender) {
            let dictCode = currColumns[a].customRender;
            currColumns[a].customRender = ({ text }) => {
              return filterMultiDictText(unref(dictOptions)[dictCode], text + '');
            };
          }
          // Trường sắp xếp được kiểm soát
          if (unref(iSorter) && currColumns[a].dataIndex === unref(iSorter).column) {
            currColumns[a].sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
          }
        }
        // Giải thích logic mã: 【issues/946] Độ rộng cột popup và độ rộng cột báo cáo online đọc cấu hình
        currColumns.forEach((item) => {
          if (item.fieldWidth != null) {
            if (isString(item.fieldWidth) && item.fieldWidth.trim().length == 0) return;
            item.width = item.fieldWidth;
            delete item.fieldWidth;
          }
        });
        if (currColumns[0].key !== 'rowIndex') {
          currColumns.unshift({
            title: 'STT',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              // Giải thích logic mã: 【QQYUN-7584] Khi popup có tổng, cột số thứ tự sẽ xuất hiệnNaN
              if (text == undefined) {
                return '';
              } else {
                return parseInt(text) + 1;
              }
            },
          });
        }
        columns.value = [...currColumns];
        initQueryInfo(null);
      }
    });
  }

  /**
   * Tải cột và dữ liệu[Dành riêng cho danh sách]
   */
  function loadColumnsAndData() {
    // Lần tải đầu tiên đặt trống isTotal, gọi tại đây để đảm bảo phương thức này chỉ tải một lần sau khi vào trang, các lần truy vấn còn lại không đi qua phương thức này
    pagination.isTotal = '';
    let url = `${configUrl.getColumnsAndData}${props.id}`;

    const {query} = handleCodeParams()
    if (query) {
      url = url + query
    }
    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1296]Khi thanh địa chỉ có tham số thì yêu cầu đầu tiên mang theo luôn, tránh kích hoạt thêm hai yêu cầu nữa
    // Trước lần yêu cầu đầu tiên, hợp nhất trước các tham số trên thanh địa chỉ để đảm bảo nhất quán với điều kiện tìm kiếm sau đó
    if (props.routeQuery && Object.keys(props.routeQuery).length > 0) {
      queryParam.value = Object.assign({}, queryParam.value, props.routeQuery);
    }
    let params = getQueryParams();
    params['onlRepUrlParamStr'] = getUrlParamString();
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1296]Khi thanh địa chỉ có tham số thì yêu cầu đầu tiên mang theo luôn, tránh kích hoạt thêm hai yêu cầu nữa
    //Cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}${JSON.stringify(params)}` : '';
    httpGroupRequest(() => defHttp.get({ url, params }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = props.id;
        let { columns: metaColumnList, cgreportHeadName, fieldHrefSlots, isGroupTitle } = res.result;
        title.value = cgreportHeadName;
        // href Chuyển hướng
        const fieldHrefSlotKeysMap = {};
        fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));
        let currColumns: any = handleColumnHrefAndDict(metaColumnList, fieldHrefSlotKeysMap);
        // Giải thích logic mã: 【issues/946] Độ rộng cột popup và độ rộng cột báo cáo online đọc cấu hình
        currColumns.forEach((item) => {
          if (isString(item.fieldWidth) && item.fieldWidth.trim().length == 0) return;
          if (item.fieldWidth != null) {
            item.width = item.fieldWidth;
            delete item.fieldWidth;
          }
        });

        // popupCần số thứ tự, danh sách thường thì không cần
        if (clickThenCheckFlag === true) {
          currColumns.unshift({
            title: 'STT',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              return parseInt(text) + 1;
            },
          });
        }

        // Gộp tiêu đề bảng
        if (isGroupTitle === true) {
          currColumns = handleGroupTitle(currColumns);
        }
        columns.value = [...currColumns];
        initQueryInfo(res.result.data);
      } else {
        // Giải thích logic mã: VUEN-583【vue3】JeecgBootException: sqlKiểm chứng danh sách đen chưa đạt,Vui lòng liên hệ quản trị viên!,Giao diện không hiển thị nhắc nhở
        createMessage.warning(res.message);
      }
    });
  }

  // Xử lý tham số động và biến hệ thống
  function handleCodeParams(onlyCode: boolean = false) {
    if (!props.code) {
      return {code: '', query: ''}
    }
    const firstIndex = props.code.indexOf('?')
    if (firstIndex === -1) {
      return {code: props.code, query: ''}
    }
    const code = props.code.substring(0, firstIndex)
    if (onlyCode) {
      return {code: code, query: ''}
    }
    const queryOrigin = props.code.substring(firstIndex, props.code.length);
    let query: string
    // Thay thế biến hệ thống
    query = replaceUserInfoByExpression(queryOrigin)
    // Lấy giá trị biểu mẫu
    if (typeof props.getFormValues === 'function') {
      const values = props.getFormValues()
      // Thay thế tham số động, nếu có ${xxx} Sẽ thay bằng giá trị thực tế
      query = query.replace(/\${([^}]+)}/g, (_$0, $1) => {
        if (values[$1] == null) {
          return ''
        }
        return values[$1]
      });

    }

    return {code, query, queryOrigin}
  }

  /**
   * Xử lý logic tổng cộng của cột tính tổng [Cần tối ưu 3.0]
   */
  function handleSumColumn(metaColumnList: OnlineColumn[], dataTotal: number): void {
    // Lấy dataIndex của cột cần tổng hợpdataIndex
    let sumColumnList = getNeedSumColumns(metaColumnList);
    // Xác định có phải lần đầu tiên lấy dữ liệu hay không, nếu đúng thì cần đặt lạipageSize
    if (pagination.isTotal == '') {
      if (sumColumnList.length > 0) {
        pagination.isTotal = true;
        // Khi có trường tổng, mỗi lần truy vấn tối đa số bản ghi gốcpageSize-1bản ghi, ngoài ra lần đầu tiên cần xóa bản ghi cuối cùng trong 10 bản ghi đã truy vấn
        // Xóa bản ghi dữ liệu cuối cùng: nếu độ dài dữ liệu lấy được lần đầu bằng pageSize thì xóa bản ghi cuối cùng
        if (dataSource.value.length == pagination.pageSize) {
          let remove_data = dataSource.value.pop();
        }
        pagination.realPageSize = pagination.pageSize - 1;
      } else {
        pagination.isTotal = false;
      }
    }
    // Cần thêm trường tổng cộng
    if (pagination.isTotal) {
      let totalRow = {};
      sumColumnList.forEach((dataIndex) => {
        let count = 0;
        dataSource.value.forEach((row) => {
          // Thống kê loại bỏ null và dữ liệu rỗng
          if (row[dataIndex] != null && row[dataIndex] != '') {
            count += parseFloat(row[dataIndex]);
          }
        });
        totalRow[dataIndex] = isNaN(count) ? 'Chứa nội dung không phải số' : count.toFixed(2);

        // Khi là số nguyên dài, tổng cộng không hiển thị.00Hậu tố
        let v = metaColumnList.find((v) => v.dataIndex == dataIndex);
        if (v && v.fieldType == 'Long') {
          totalRow[dataIndex] = parseInt(totalRow[dataIndex]);
        }
      });
      dataSource.value.push(totalRow);
      pagination.realTotal = dataTotal;
      pagination.total = Number(dataTotal) + Number(Math.floor(dataTotal / pagination.realPageSize));
    }
  }

  /**
   * Lấy dataIndex của cột cần tính tổng dataIndex
   * @param columns
   */
  function getNeedSumColumns(columns: OnlineColumn[]): string[] {
    let arr: string[] = [];
    for (let column of columns) {
      if (column.isTotal === '1') {
        arr.push(column.dataIndex!);
      }
        // 【VUEN-1569] [Báo cáo online] Tổng không có hiệu lực
      if (column.children && column.children.length > 0) {
        let subArray = getNeedSumColumns(column.children);
        if (subArray.length > 0) {
          arr.push(...subArray);
        }
      }
    }
    return arr;
  }

  /**
   * Xử lý href và bản dịch từ điển của cột
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // online Trong báo cáo, loại được cấu hình là ngày (yyyy-MM-dd ), nhưng thực tế hiển thị theo định dạng ngày giờ(yyyy-MM-dd HH:mm:ss) issues/3042
      if (fieldType == 'Date') {
        column.customRender = ({ text }) => {
          if (!text) {
            return '';
          }
          if (text.length > 10) {
            return text.substring(0, 10);
          }
          return text;
        };
      } else {
        if (!hrefSlotName && column.scopedSlots && column.scopedSlots.customRender) {
          //Danh mục và href loại trừ lẫn nhau, tại đây thông qua fieldHrefSlotKeysMap tìm trước cột là href
          if (fieldHrefSlotKeysMap.hasOwnProperty(column.scopedSlots.customRender)) {
            hrefSlotName = column.scopedSlots.customRender;
          }
        }
        // Nếu customRender có giá trị thì nghĩa là đã dùng danh mục
        // Nếu hrefSlotName có giá trị thì có nghĩa là đã sử dụng chuyển hướng href
        // Hai bên có thể tương thích. Ý tưởng tương thích cụ thể là: trước tiên lấy giá trị thay thế của danh mục, sau đó thêm liên kết href để chuyển hướng
        if (customRender || hrefSlotName) {
          let dictCode = customRender as string;
          let replaceFlag = '_replace_text_';
          column.customRender = ({ text, record }) => {
            let value = text;
            // Nếu dictCode có giá trị thì thực hiện chuyển đổi danh mục
            if (dictCode) {
              if (dictCode.startsWith(replaceFlag)) {
                let textFieldName = dictCode.replace(replaceFlag, '');
                value = record[textFieldName];
              } else {
                value = filterMultiDictText(unref(dictOptions)[dictCode], text + '');
              }
            }
            // Tham số mở rộng đặt độ dài nội dung của cột
            if (column.showLength) {
              if (value && value.length > column.showLength) {
                value = value.substr(0, column.showLength) + '...';
              }
            }
            // Nếu hrefSlotName có giá trị, sẽ tạo một thẻ a, bao lấy giá trị sau khi đã thay thế bằng danh mục (hoặc giá trị gốc)
            if (hrefSlotName) {
              let field = fieldHrefSlotKeysMap[hrefSlotName];
              if (field) {
                return h(
                  'a',
                  {
                    onClick: () => handleClickFieldHref(field, record),
                  },
                  value
                );
              }
            }
            return value;
          };
        }
      }
    }
    return columns;
  }

  /**
   * Xử lý tiêu đề bảng gộp
   * @param columns
   */
  function handleGroupTitle(columns: OnlineColumn[]): OnlineColumn[] {
    let newColumns: OnlineColumn[] = [];
    for (let column of columns) {
      //Trường sắp xếp được kiểm soát  ---- Logic này là logic mới được thêm vào, đang chờ
      if (unref(iSorter) && column.dataIndex === unref(iSorter).column) {
        column.sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
      }
      //Kiểm tra trường có cần hợp nhất tiêu đề bảng hay không
      if (column.groupTitle) {
        let clIndex = newColumns.findIndex((im) => im.title === column.groupTitle);
        if (clIndex !== -1) {
          //Tiêu đề bảng đã tồn tại thì trực tiếppush children
          newColumns[clIndex].children!.push(column);
        } else {
          //Nếu không có tiêu đề bảng thì ghép thông tin tiêu đề bảng
          let clGroup: OnlineColumn = {},
            child: OnlineColumn[] = [];
          child.push(column);
          clGroup.title = column.groupTitle;
          clGroup.align = 'center';
          clGroup.children = child;
          newColumns.push(clGroup);
        }
      } else {
        newColumns.push(column);
      }
    }
    return newColumns;
  }

  // Lấy đối tượng router, dùng cho chuyển hướng href
  let router = useRouter();
  /**
   * href Sự kiện nhấn
   * @param field
   * @param record
   */
  function handleClickFieldHref(field, record) {
    let href = field.href;
    let urlPattern = /(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?/;
    let compPattern = /\.vue(\?.*)?$/;
    let jsPattern = /{{([^}]+)}}/g; // {{ xxx }}
    if (typeof href === 'string') {
      href = href.trim().replace(/\${([^}]+)?}/g, (s1, s2) => record[s2]);
      // Thực thi {{...}} JSCâu lệnh tăng cường
      if (jsPattern.test(href)) {
        href = href.replace(jsPattern, function (text, s0) {
          try {
            // Hỗ trợ {{ ACCESS_TOKEN }} Placeholder
            if (s0.trim() === 'ACCESS_TOKEN') {
              return getToken()
            }

            // Giải thích logic mã: 【QQYUN-6390]Thay eval bằng new Function, khắc phục cảnh báo build
            return _eval(s0);
          } catch (e) {
            console.error(e);
            return text;
          }
        });
      }
      if (urlPattern.test(href)) {
        window.open(href, '_blank');
      } else if (compPattern.test(href)) {
        // Xử lý popup
        openHrefCompModal(href);
      } else {
        router.push(href);
      }
    }
  }

  /**
   * Xuất
   */
  function handleExport() {
    const { handleExportXls } = useMethods();
    let url = `${configUrl.export}${cgRpConfigId.value}`;
    let params = getQueryParams(); //Điều kiện tìm kiếm
    // 【VUEN-1568] Nếu đã chọn một số dòng, thì chỉ xuất các dòng đã chọn
    let keys = unref(checkedKeys);
    if (keys.length > 0) {
      keys = keys
        .map((i) => selectRows.value.find((item) => combineRowKey(item) === i)?.id)
        .filter((i) => i != null && i !== '');
      // Kiểm tra có trường ID không
      if (keys.length === 0) {
        createMessage.warning('Do dữ liệu thiếu trường ID, nên không thể sử dụng chức năng xuất các mục đã chọn');
        return;
      }
      params['force_id'] = keys.join(',');
    }
    handleExportXls(title.value, url, params);
  }

  /**
   * Logic tính tổng [Cần tối ưu 3.0]
   * Sự kiện thay đổi kích thước phân trang
   * @param _current
   * @param size
   */
  function onSizeChange(_current, size) {
    pagination.isTotal = '';
    pagination.pageSize = size;
    if (pagination.isTotal) {
      pagination.realPageSize = size - 1;
    } else {
      pagination.realPageSize = size;
    }
    pagination.current = 1;
  }

  /**
   *  Logic tính tổng [Cần tối ưu 3.0]
   * Hiển thị tổng số bản ghi
   * @param total
   */
  function onShowTotal(total) {
    // Tính lại dữ liệu hiển thị mỗi trang dựa trên có tổng hay không
    let start = (pagination.current - 1) * pagination.realPageSize + 1;
    let end = start + (pagination.isTotal ? dataSource.value.length - 1 : dataSource.value.length) - 1;
    let realTotal = pagination.isTotal ? pagination.realTotal : total;
    return start + '-' + end + ' Tổng cộng' + realTotal + 'mục';
  }

  /**
   * Sự kiện kích hoạt khi popup hiển thị/ẩn
   */
  async function visibleChange($event) {
    visible.value = $event;
    $event && loadColumnsInfo();
  }

  /**
   * Khởi tạo điều kiện tìm kiếm
   * @param data Tập kết quả dữ liệu
   */
  function initQueryInfo(data) {
    let url = `${configUrl.getQueryInfo}${unref(cgRpConfigId)}`;
    //Cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      // console.log("Lấy điều kiện truy vấn", res);
      if (res.success) {
        dynamicParamHandler(res.result);
        queryInfo.value = res.result;
        console.log('queryInfo==>', queryInfo.value);
        // update-begin--author:liaozhiyang---date:20260804---for: Khi đã có dữ liệu danh sách thì không loadData lần hai nữa, tránh request lặp lại do mang tham số trên thanh địa chỉ
        // Sau khi điều kiện truy vấn được tải xong mới request dữ liệu; nếu loadColumnsAndData đã mang về dữ liệu, điền trực tiếp vào, không request lặp lại nữa
        if (data) {
          setDataSource(data);
        } else {
          loadData(1);
        }
        // update-end--author:liaozhiyang---date:20260804---for: Khi đã có dữ liệu danh sách thì không loadData lần hai nữa, tránh request lặp lại do mang tham số trên thanh địa chỉ
      } else {
        createMessage.warning(res.message);
      }
    });
  }

  /**
   * Tải dữ liệu bảng
   * @param arg
   */
  function loadData(arg?) {
    if (arg == 1) {
      pagination.current = 1;
    }
    let params = getQueryParams(); //Điều kiện tìm kiếm
    params['onlRepUrlParamStr'] = getUrlParamString();
    console.log('params', params);
    loading.value = true;
    // Giải thích logic mã: 【TV360X-578]Dịch SQL báo cáo online, trang thứ hai không lật trang dữ liệu
    let url = `${configUrl.getColumnsAndData}${unref(cgRpConfigId)}`;
    const {query} = handleCodeParams()
    if (query) {
      url = url + query
    }
    //Cachekey
    let groupIdKey = props.groupId ? `${props.groupId}${url}${JSON.stringify(params)}` : '';
    httpGroupRequest(() => defHttp.get({ url, params }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      // Giải thích logic mã: 【TV360X-578]Dịch SQL báo cáo online, trang thứ hai không lật trang dữ liệu
      res.result.dictOptions && initDictOptionData(res.result.dictOptions);
      loading.value = false;
      // Giải thích logic mã: 【TV360X-578]Dịch SQL báo cáo online, trang thứ hai không lật trang dữ liệu
      let data = res.result.data;
      console.log('Thông tin bảng:', data);
      setDataSource(data);
    });
  }

  /**
   * Lấy tham số trên thanh địa chỉ
   */
  function getUrlParamString() {
   let query = route.query;
   let arr:any[] = []
   if(query && Object.keys(query).length>0){
     Object.keys(query).map(k=>{
       arr.push(`${k}=${query[k]}`)
     })
   }
   return arr.join('&')
  }

  /**
   * Cài đặtdataSource
   */
  function setDataSource(data) {
    if (data) {
      pagination.total = Number(data.total);
      let currentPage = pagination?.current ?? 1;
      for (let a = 0; a < data.records.length; a++) {
        if (!data.records[a].rowIndex) {
          data.records[a].rowIndex = a + (currentPage - 1) * 10;
        }
      }
      dataSource.value = data.records;
      // Giải thích logic mã: issues/426 Lỗi hồi quy được đưa vào khi sửa 356 JPopupOnlReportModal.vue chưa được sửa trong
      tableRef?.value && tableRef?.value?.setPagination?.({
        total: Number(data.total)
      })
    } else {
      pagination.total = 0;
      dataSource.value = [];
    }
    // Logic tính tổng [Cần tối ưu 3.0]
    handleSumColumn(columns.value, pagination.total);
  }

  /**
   * Lấy tham số truy vấn
   */
  function getQueryParams() {
    let paramTarget = {};
    if (unref(dynamicParam)) {
      //Xử lý tham số tùy chỉnh
      Object.keys(unref(dynamicParam)).map((key) => {
        paramTarget['self_' + key] = unref(dynamicParam)[key];
      });
    }
    let param = Object.assign(paramTarget, unref(queryParam), unref(iSorter));
    param.pageNo = pagination.current;
    // Logic tính tổng [Cần tối ưu 3.0]
    //  Khi tìm kiếm thực tế không dùng pageSize của component table mà dùng giá trị tùy chỉnhrealPageSize,realPageSizeSẽ thay đổi sau lần đầu tiên lấy được dữ liệu
    param.pageSize = pagination.realPageSize;
    return filterObj(param);
  }

  /**
   * Xử lý tham số động
   */
  function dynamicParamHandler(arr?) {
    if (arr && arr.length > 0) {
      //Trước khi tải điều kiện truy vấn lần đầu, khởi tạo queryParam thành đối tượng rỗng
      let queryTemp = {};
      for (let item of arr) {
        if (item.mode === 'single') {
          queryTemp[item.field] = '';
        }
      }
      queryParam.value = { ...queryTemp };
    }
    // Gộp tham số route
    if (props.routeQuery) {
      queryParam.value = Object.assign(queryParam.value, props.routeQuery);
    }

    let dynamicTemp = {};
    if (props.param) {
      Object.keys(props.param).map((key) => {
        let str = props.param[key];
        //【issues/8426] Giải quyết component JPopup truyền tham số không nhận được
        if (key in queryParam.value) {
          if (str && str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length - 1);
          }
          //Nếu điều kiện truy vấn chứa tham số thì đặt giá trị
          unref(queryParam)[key] = str;
        }
        dynamicTemp[key] = props.param[key];
      });
    }
    dynamicParam.value = { ...dynamicTemp };
  }

  /**
   * Phân trang
   * @param page
   * @param filters
   * @param sorter
   */
  function handleChangeInTable(page, filters, sorter) {
    console.log(page, filters, sorter);
    //Kích hoạt khi phân trang, sắp xếp, lọc thay đổi
    if (Object.keys(sorter).length > 0) {
      iSorter.value = {
        column: sorter.field,
        order: 'ascend' === sorter.order ? 'asc' : 'desc',
      };
      // Trường sắp xếp được kiểm soát
      unref(columns).forEach((col) => {
        if (col['dataIndex'] === sorter.field) {
          col['sortOrder'] = sorter.order;
        }
      });
    }
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadData();
  }

  /**
   * Sự kiện nhấp hàng
   * @param record
   */
  function clickThenCheck(record) {
    if (clickThenCheckFlag === true) {
      // Giải thích logic mã: 【QQYUN-7514] hiển thị radio khi popup chọn đơnradio
      if (!props.multi) {
        selectRows.value = [];
        checkedKeys.value = [];
      }
      let rowKey = combineRowKey(record);
      if (!unref(checkedKeys) || unref(checkedKeys).length == 0) {
        let arr1: any[] = [],
          arr2: any[] = [];
        arr1.push(record);
        arr2.push(rowKey);
        checkedKeys.value = arr2;
        //selectRows.value = arr1;
      } else {
        if (unref(checkedKeys).indexOf(rowKey) < 0) {
          //Không tồn tại thì chọn
          checkedKeys.value.push(rowKey);
          //selectRows.value.push(record);
        } else {
          //Đã chọn thì hủy
          let rowKey_index = unref(checkedKeys).indexOf(rowKey);
          checkedKeys.value.splice(rowKey_index, 1);
          //selectRows.value.splice(rowKey_index, 1);
        }
      }
      // Giải thích logic mã: 【issues/5357] nhấp vào hàng để chọn
      tableRef.value.setSelectedRowKeys([...checkedKeys.value]);
    }
  }

  //Ngăn chặn dữ liệu rác trong từ điển
  function initDictOptionData(arr) {
    let obj = {};
    Object.keys(arr).map((k) => {
      obj[k] = arr[k].filter((item) => {
        return item != null;
      });
    });
    dictOptions.value = obj;
  }

  /**
   * Lọc thuộc tính trống trong đối tượng
   * @param obj
   * @returns {*}
   */
  function filterObj(obj) {
    if (!(typeof obj == 'object')) {
      return;
    }

    for (let key in obj) {
      if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
        delete obj[key];
      }
    }
    return obj;
  }

  // Kiểu
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // Cấu hình thuộc tính popup
  const hrefComponent = ref({
    model: {
      title: '',
      okText: 'Đóng',
      width: '100%',
      open: false,
      destroyOnClose: true,
      style: dialogStyle,
      // dialogStyle: dialogStyle,
      bodyStyle: {
        padding: '8px',
        height: 'calc(100vh - 108px)',
        overflow: 'auto',
        overflowX: 'hidden',
      },
      // Ẩn nút Hủy
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.value.model.open = false),
      cancel: () => (hrefComponent.value.model.open = false),
    },
    is: <any>null,
    params: {},
  });

  // Sự kiện nhấp vào siêu liên kết--> Mở một cửa sổ modal
  function openHrefCompModal(href) {
    // Phân tích tham số href
    let index = href.indexOf('?');
    let path = href;
    if (index !== -1) {
      path = href.substring(0, index);
      let paramString = href.substring(index + 1, href.length);
      let paramArray = paramString.split('&');
      let params = {};
      paramArray.forEach((paramObject) => {
        let paramItem = paramObject.split('=');
        params[paramItem[0]] = paramItem[1];
      });
      hrefComponent.value.params = params;
    } else {
      hrefComponent.value.params = {};
    }
    hrefComponent.value.model.open = true;
    hrefComponent.value.model.title = 'Thao tác';
    hrefComponent.value.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  /**
   * emitSự kiện: lấy dữ liệu hàng đã chọn
   */
  function getOkSelectRows(): any[] {
    let arr = unref(selectRows);
    let selectedRowKeys = checkedKeys.value;
    console.log('arr', arr);
    if (!selectedRowKeys || selectedRowKeys.length <= 0) {
      return [];
    }
    if (!arr || arr.length <= 0) {
      return [];
    }
    let rows: any = [];
    for (let key of selectedRowKeys) {
      for (let i = 0; i < arr.length; i++) {
        let combineKey = combineRowKey(arr[i]);
        if (key === combineKey) {
          rows.push(toRaw(arr[i]));
          break;
        }
      }
    }
    return rows;
  }

  return [
    {
      visibleChange,
      loadColumnsInfo,
      loadColumnsAndData,
      dynamicParamHandler,
      loadData,
      handleChangeInTable,
      combineRowKey,
      clickThenCheck,
      filterUnuseSelect,
      handleExport,
      getOkSelectRows,
    },
    {
      hrefComponent,
      visible,
      rowSelection,
      checkedKeys,
      selectRows,
      pagination,
      dataSource,
      columns,
      indexColumnProps,
      loading,
      title,
      iSorter,
      queryInfo,
      queryParam,
      dictOptions,
    },
  ];
}
