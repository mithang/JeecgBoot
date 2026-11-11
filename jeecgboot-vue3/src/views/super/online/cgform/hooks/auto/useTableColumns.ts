import type { Ref } from 'vue';
import type { ExtConfigType } from '../../types';
import { HrefSlots, OnlineColumn } from '/@/components/jeecg/OnLine/types/onlineConfig';
import { filterMultiDictObjs } from '/@/utils/dict/JDictSelectUtil';
import { computed, defineAsyncComponent, h, reactive, ref, toRaw, unref, watch, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { Tag as ATag } from 'ant-design-vue';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { getAreaTextByCodeAnyLevel } from '/@/components/Form/src/utils/Area';
import { createImgPreview } from '@/components/Preview';
import { importViewsFile, _eval } from '/@/utils';
import { useModal } from '/@/components/Modal';
import LinkTableListPiece from '../../extend/linkTable/LinkTableListPiece.vue'
import { getToken } from "/@/utils/auth";
import { downloadFile } from '/@/api/common/api';
import { getWeekMonthQuarterYear, split } from '/@/utils';
import { getItemColor } from "@/utils/dict/DictColors";

/**
 * Lấy cái danh sách thực tế cầncolumnCấu hình
 * @param onlineTableContext Dữ liệu tra ra từ cơ sở dữ liệu
 * @param extConfigJson Cấu hình mở rộngJSON
 */
export function useTableColumns(onlineTableContext, extConfigJson: Ref<ExtConfigType | undefined>) {
  // Lấy đối tượng router hrefDùng khi chuyển hướng
  let router = useRouter();

  // Thông tin cột
  const columns = ref<Array<OnlineColumn>>([]);
  /**
   * 20260309
   * liaozhiyang
   * 【issues/9336】Không kéo được độ rộng cột
   * */
  function applyResizableColumns(cols: OnlineColumn[]) {
    cols.forEach((column) => {
      if (!column.width) {
        if (column.fieldType === 'date' || column.fieldType === 'Date') {
          column.width = 120;
        } else if (column.fieldType === 'link_table') {
          column.width = 180;
        } else {
          column.width = 150;
        }
      }
      column.resizable = true;
    });
  }

  // Có hay không cóbpm_status
  //const hasBpmStatus = ref<boolean>(false)
  // Thông tin từ điển
  const dictOptionInfo = ref<any>({});
  //Giá trị đã chọn
  const selectedKeys = ref<any[]>([]);
  //Bản ghi hàng đã chọn
  //const selectRows = ref<Array<any>>([]);
  // Cấu hình cột lựa chọn --computedCó vấn đề
  const rowSelection = ref<any>(null);
  // Có thanh cuộn hay không
  let enableScrollBar = ref(true);
  // tablethuộc tínhscroll
  let tableScroll = computed(() => {
    if (enableScrollBar.value == true) {
      return undefined;
    } else {
      // XTrục không có thanh cuộn
      return { x: false };
    }
  });

  //Dùng để onlinecủa danh sách Sự kiện popup khi nhấp vào một cột nào đó-Popup hiển thị biểu mẫu khác
  const [registerOnlineHrefModal, { openModal: openOnlineHrefModal }] = useModal();
  const hrefMainTableId = ref('')
  // Dùng để onlineTrong biểu mẫu Bật ra biểu mẫu khác
  const [registerPopModal, { openModal: openPopModal }] = useModal();
  const popTableId = ref('')
  const formDisabledColumns = ref<string[]>([]);

  // Kết quả yêu cầu thông tin cột truy vấn Phương thức xử lý
  function handleColumnResult(result, type = 'checkbox') {
    // Thiết lập từ điển
    dictOptionInfo.value = result.dictOptions;
    // update-begin--author:liusq---date:20260715---for：LHZP-330【Quyền bản ghi liên kết】Ghi lại các trường mà người dùng hiện tại không thể chỉnh sửa trên biểu mẫu
    formDisabledColumns.value = result.formDisabledColumns || [];
    // update-end--author:liusq---date:20260715---for：LHZP-330【Quyền bản ghi liên kết】Ghi lại các trường mà người dùng hiện tại không thể chỉnh sửa trên biểu mẫu
    // rowSelectionThiết lập
    if (result.checkboxFlag == 'Y') {
      rowSelection.value = {
        selectedRowKeys: selectedKeys,
        onChange: onSelectChange,
        type,
        // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-407】Bảng cây trả về từ backend2mục, dấu tích hiển thị là4mục (điều)
        getCheckboxProps: (record) => ({
          disabled: !!record?.isLoading,
        }),
        // update-end--author:liaozhiyang---date:20260714---for：【LHZP-407】Bảng cây trả về từ backend2mục, dấu tích hiển thị là4mục (điều)
      };
    } else {
      rowSelection.value = null;
    }
    // Có cho phép thanh cuộn hay không
    enableScrollBar.value = result.scrollFlag == 1;

    let dataColumns = result.columns;
    dataColumns.forEach((column) => {
      if (extConfigJson?.value?.canResizeColumn === 1) {
        // update-begin--author:liaozhiyang---date:20260309---for:【issues/9336】Không kéo được độ rộng cột
        applyResizableColumns([column]);
        // update-end--author:liaozhiyang---date:20260309---for:【issues/9336】Không kéo được độ rộng cột
      }
      
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Cột hỗ trợ tính năng cố định
      if (column.fieldExtendJson) {
        const json = JSON.parse(column.fieldExtendJson);
        if (!!json.isFixed) {
          column.fixed = 'left';
        }
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-4161】Cột hỗ trợ tính năng cố định
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-129】Bổ sung cấu hình control văn bản giàu (rich text)hrefChuyển hướng (nhảy trang)
      if (column.hrefSlotName && column.scopedSlots) {
        const obj = result.fieldHrefSlots?.find((item) => item.slotName === column.hrefSlotName);
        if (obj) {
          column.fieldHref = obj;
        }
      }
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-129】Bổ sung cấu hình control văn bản giàu (rich text)hrefChuyển hướng (nhảy trang)
      Object.keys(column).map((key) => {
        // Xóa các trường có giá trị rỗng (không xóa Chuỗi rỗng('') hoặc Chữ số 0 ）
        if (column[key] == null) {
          delete column[key];
        }
      });
    });

    // href Chuyển hướng (nhảy trang)
    let fieldHrefSlots: HrefSlots[] = result.fieldHrefSlots;
    const fieldHrefSlotKeysMap = {};
    fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));

    let tableColumns: OnlineColumn[] = [];
    // Xử lý trong cột href Chuyển hướng và dict từ điển, khiến cả hai có thể tồn tại tương thích với nhau
    tableColumns = handleColumnHrefAndDict(dataColumns, fieldHrefSlotKeysMap);
    // Có hay không có bpm_statustrường Nếu có, nút thao tác trong danh sách cần thêm nút gửi quy trình
    bpmStatusFilter(tableColumns);

    console.log('-----Cấu hình cột danh sách----', tableColumns);
    // Nếu là danh sách cây Cần thiết lập trường cột đầu tiên và Cột đầu tiênalign
    if (onlineTableContext.isTree() === true) {
      // Tìm cấu hình của cột đầu tiên
      let firstField = result.textField;
      let index = -1;
      for (let i = 0; i < tableColumns.length; i++) {
        if (tableColumns[i].dataIndex == firstField) {
          index = i;
          break;
        }
      }
      if (index > 0) {
        //Nếu là0hoặc là-1không cần xử lý
        let deleteColumns = tableColumns.splice(index, 1);
        tableColumns.unshift(deleteColumns[0]);
      }
      //Cột đầu tiên căn trái
      if (tableColumns.length > 0) {
        tableColumns[0].align = 'left';
      }
    }
    columns.value = tableColumns;
    // Cột đã thay đổi, cần render lại bảng
    onlineTableContext.reloadTable();
  }

  /**
   * Sự kiện chọn bảng [expose]
   * @param selectedRowKeys
   * @param selectRow
   */
  function onSelectChange(selectedRowKeys, selectedRows) {
    selectedKeys.value = selectedRowKeys;
    onlineTableContext['selectedRows'] = toRaw(selectedRows);
    onlineTableContext['selectedRowKeys'] = toRaw(selectedRowKeys);
  }

  /**
   * Xử lý của cộthrefvà dịch từ điển
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // online Trong báo cáo, loại được cấu hình là ngày (yyyy-MM-dd ) , nhưng hiển thị thực tế là định dạng ngày giờ(yyyy-MM-dd HH:mm:ss) issues/3042
      if (fieldType == 'date' || fieldType == 'Date') {
        column.customRender = ({ text }) => {
          if (!text) {
            return '';
          }
          if (text.length > 10) {
            return text.substring(0, 10);
          }
          return text;
        };
      } else if (fieldType == 'link_table') {
        // Hiển thị danh sách bản ghi liên kết
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        const fieldExtendJson = column.fieldExtendJson ?? '{}';
        const json = JSON.parse(fieldExtendJson);
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        column.customRender = ({ text, record }) => {
          if (!text) {
            return '';
          }
          if(onlineTableContext.isPopList===true){
            // Nếu là danh sách dạng popup, cột của bản ghi liên kết chỉ hỗ trợ dịch dữ liệu, không cần logic chuyển hướng
            return record[column.dataIndex+"_dictText"]
          }else{
            let tempIdArray = (text+'').split(',');
            //update-begin-author:taoyan date:2023-2-15 for: QQYUN-4286【onlineBiểu mẫu 】Bảng chính-con bật truy vấn liên hợp Kiểm thử chức năng báo lỗi, không mở được
            let tempLabelArray = [];
            if(record[column.dataIndex+"_dictText"]){
              tempLabelArray = record[column.dataIndex+"_dictText"].split(',');
            }
            //update-end-author:taoyan date:2023-2-15 for: QQYUN-4286【onlineBiểu mẫu 】Bảng chính-con bật truy vấn liên hợp Kiểm thử chức năng báo lỗi, không mở được
            let renderResult:any = []
            for(let i=0;i<tempIdArray.length;i++){
              let renderObj = h(
                LinkTableListPiece,
                {
                  id: tempIdArray[i],
                  text: tempLabelArray[i],
                  onTab:(id)=>handleClickLinkTable(id, hrefSlotName, json.isListReadOnly, column.dataIndex)
                }
              );
              renderResult.push(renderObj)
            }
            if(renderResult.length==0){
              return ''
            }
            //Nếu cần hiển thị đầy đủ, nhưng sẽ xuống dòng:display: flex;width: 100%;flex-wrap: wrap;flex-direction: row;
            // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
            return h('div',{style:{'overflow':'hidden'}, title: tempLabelArray.join(',')}, renderResult);
            // update-end--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
          }
        };
      } else if (fieldType === 'popup_dict') {
        // update-begin--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictdịch danh sách của
        column.customRender = ({ text, record }) => {
          const dict = record[column.dataIndex + '_dictText'];
          if (dict != undefined) {
            return record[column.dataIndex + '_dictText'];
          }
          return text;
        };
        column.ellipsis = true;
        // update-end--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictdịch danh sách của
      } else {
        if (!hrefSlotName && column.scopedSlots && column.scopedSlots.customRender) {
          //【OnlineBáo cáo】từ điển vàhrefLoại trừ lẫn nhau Tại đây thông quafieldHrefSlotKeysMap Trước tiên tìm xem làhrefcột của
          if (fieldHrefSlotKeysMap.hasOwnProperty(column.scopedSlots.customRender)) {
            hrefSlotName = column.scopedSlots.customRender;
          }
        }
        // Nếu customRender Có giá trị thì có nghĩa là đã sử dụng từ điển
        // Nếu hrefSlotName Có giá trị thì có nghĩa là đã sử dụnghrefChuyển hướng (nhảy trang)
        // Hai cách có thể tương thích. Ý tưởng tương thích cụ thể là: trước tiên lấy giá trị đã được thay thế bằng từ điển, sau đó thêm vàohrefChuyển hướng liên kết
        if (customRender || hrefSlotName) {
          let dictCode = customRender as string;
          let replaceFlag = '_replace_text_';
          // Cột của hàm render tùy chỉnh Cần cấu hình thủ côngellipsis
          column.ellipsis = true;
          column.customRender = ({ text, record }) => {
            let value = text;
            const valueSpan: any[] = [];
            const getValue = () => valueSpan.length ? valueSpan : value;
            // Nếu dictCode Có giá trị thì tiến hành chuyển đổi từ điển
            if (dictCode) {
              if (dictCode.startsWith(replaceFlag)) {
                let textFieldName = dictCode.replace(replaceFlag, '');
                value = record[textFieldName];
              } else {
                const dictItems = filterMultiDictObjs(unref(dictOptionInfo)[dictCode], text);
                value = dictItems.map((item) => {
                  if (item.hasColor) {
                    //Lấy màu phông chữ
                    const fontColor = getItemColor(item.color);
                    valueSpan.push(h(ATag, {
                      color: item.color,
                      style: {
                        'color': fontColor,
                        'margin-left': '5px',
                      },
                    }, () => item.text))
                  }
                  return item.text;
                }).join(',');
              }
            }
            // Tham số mở rộng thiết lập độ dài nội dung của cột
            if (column.showLength) {
              if (value && value.length > column.showLength) {
                value = value.substr(0, column.showLength) + '...';
              }
            }
            // Nếu hrefSlotName Có giá trị thì tạo ra một a Nhãn, bao bọc giá trị sau khi thay thế bằng từ điển (hoặc giá trị gốc)
            if (hrefSlotName) {
              let field = fieldHrefSlotKeysMap[hrefSlotName];
              if (field) {
                return h(
                  'a',
                  {
                    // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
                    title: value,
                    // update-end--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
                    onClick: () => handleClickFieldHref(field, record),
                  },
                  getValue(),
                );
              }
            }
            // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
            return h('span', { title: value }, getValue());
            // update-end--author:liaozhiyang---date:20260713---for：【LHZP-393】Ô bảng cộng thêmtitlethuộc tính, di chuột qua có thể hiển thị
          };
        }

        //  Phiên bản cũ gọi làscopedSlots Phiên bản mới gọi làslots
        if (column.scopedSlots) {
          // slotcột của Cần cấu hình thủ côngellipsis
          column.ellipsis = true;
          let slots = column.scopedSlots;
          column['slots'] = slots;
          delete column.scopedSlots;
        }
      }
    }
    return columns;
  }

  /**
   * href Sự kiện nhấp
   * @param field
   * @param record
   */
  function handleClickFieldHref(field, record) {
    let href = field.href;
    let urlPattern = /(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?/;
    let compPattern = /\.vue(\?.*)?$/;
    let jsPattern = /{{([^}]+)}}/g; // {{ xxx }}
    if (typeof href === 'string') {
      if(href.startsWith('ONLINE:')){
        // ONLINE:tableId:fieldName
        let arr = href.split(':')
        hrefMainTableId.value = arr[1];
        let fieldName = arr[2];
        openOnlineHrefModal(true, {
          isUpdate: true,
          disableSubmit: true,
          hideSub: true,
          record:{id: record[fieldName]},
        })
      }else{
        href = href.trim().replace(/\${([^}]+)?}/g, (_s1, s2) => record[s2]);
        // Thực thi {{...}} JSCâu lệnh tăng cường
        if (jsPattern.test(href)) {
          href = href.replace(jsPattern, function (text, s0) {
            try {
              // Hỗ trợ {{ ACCESS_TOKEN }} Phần giữ chỗ (placeholder)
              if (s0.trim() === 'ACCESS_TOKEN') {
                return getToken()
              }

              // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
              return _eval(s0);
              // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
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
  }

  // Kiểu dáng
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366】Nâng cấp lênantd4.x
  // Cấu hình thuộc tính popup
  const hrefComponent = reactive({
    model: {
      title: '',
      okText: 'Đóng',
      width: '100%',
      open: false,
      destroyOnClose: true,
      style: dialogStyle,
      // dialogStyle: dialogStyle,
      bodyStyle: { padding: '8px', height: 'calc(100vh - 108px)', overflow: 'auto', overflowX: 'hidden' },
      // Ẩn bỏ nút hủy
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.model.open = false),
      cancel: () => (hrefComponent.model.open = false),
    },
    is: <any>null,
    params: {},
  });
  // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366】Nâng cấp lênantd4.x

  // Sự kiện nhấp siêu liên kết--> Mở mộtmodalCửa sổ
  function openHrefCompModal(href) {
    // Phân tích (parse) href Tham số
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
      hrefComponent.params = params;
    } else {
      hrefComponent.params = {};
    }
    // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366】Nâng cấp lênantd4.x
    hrefComponent.model.open = true;
    // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366】Nâng cấp lênantd4.x
    hrefComponent.model.title = 'Thao tác';
    hrefComponent.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  //Nếu là danh sách cây Cột thao tác chỉ được cố định ở phía bên phải
  let fixedAction:any = 'right';
  if(onlineTableContext.isTree()){
    fixedAction = 'right'
  }
  const actionColumn = reactive<OnlineColumn>({
    title: 'Thao tác',
    dataIndex: 'action',
    slots: { customRender: 'action' },
    fixed: fixedAction,
    align: 'center',
    width: 150,
  });

  // Lắng nghe cấu hình cột cố định của tham số mở rộng, thay đổi động cách cố định cột thao tác
watch(() => extConfigJson?.value, () => {
  if (extConfigJson?.value?.tableFixedAction === 1) {
    actionColumn.fixed = extConfigJson?.value?.tableFixedActionType || 'right';
    if (onlineTableContext.isTree()) {
      actionColumn.fixed = 'right';
    }
  }
  // update-begin--author:liaozhiyang---date:20260309---for:【issues/9336】Không kéo được độ rộng cột
  if (extConfigJson?.value?.canResizeColumn === 1 && columns.value.length > 0) {
    applyResizableColumns(columns.value);
    onlineTableContext.reloadTable();
  }
  // update-end--author:liaozhiyang---date:20260309---for:【issues/9336】Không kéo được độ rộng cột
});

  // Trạng thái nút quy trình
  function bpmStatusFilter(tableColumns: OnlineColumn[]): boolean {
    let flag = false;
    for (let i = 0; i < tableColumns.length; i++) {
      let item = tableColumns[i];
      let fieldName = item.dataIndex;
      if (fieldName!.toLowerCase() == 'bpm_status') {
        flag = true;
        break;
      }
    }
    onlineTableContext['hasBpmStatus'] = flag;
    return flag;
  }

  /**
   * Tệp
   * @param text
   */
  function downloadRowFile(text, record, column, id) {
    if (!text) {
      return;
    }
    // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-8020】online Biểu mẫu có nhiều tệp đi qua interface tải xuống
    if (text.indexOf(',') > 0) {
      downloadFile(`/online/cgform/field/download/${id}/${record.id}/${column.dataIndex}`, `Tệp_${record.id}.zip`);
    } else {
      const url = getFileAccessHttpUrl(text);
      window.open(url);
    }
    // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-8020】online Biểu mẫu có nhiều tệp đi qua interface tải xuống
  }

  /**
   * Hình ảnh
   * @param text
   */
  function getImgView(text) {
    if (text && text.indexOf(',') > 0) {
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990】Tham số hình ảnh chứa dấu phẩy sẽ bị nhận diện sai thành nhiều hình
      text = split(text)[0];
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990】Tham số hình ảnh chứa dấu phẩy sẽ bị nhận diện sai thành nhiều hình
    }
    return getFileAccessHttpUrl(text);
  }

  /**
   * Lấy văn bản tỉnh/thành/quận huyện theo mã
   * @param code
   */
  function getPcaText(code, column) {
    if (!code) {
      return '';
    }
    // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
    let includeParent = true;
    let fieldExtendJson = column?.fieldExtendJson;
    let level = 3;
    if (fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (['province', 'city', 'region'].includes(fieldExtendJson.displayLevel)) {
        if (fieldExtendJson.displayLevel === 'province') {
          level = 1;
        } else if (fieldExtendJson.displayLevel === 'city') {
          level = 2;
        } else if (fieldExtendJson.displayLevel === 'region') {
          level = 3;
        }
        includeParent = false;
      }
    }
    return getAreaTextByCodeAnyLevel(code, includeParent, level as 1 | 2 | 3);
    // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
  }

  /**
   * Định dạng hóa ngày
   * @param text
   */
  function getFormatDate(text, column) {
    if (!text) {
      return '';
    }
    let a = text;
    if (a.length > 10) {
      a = a.substring(0, 10);
    }
    // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
    let fieldExtendJson = column?.fieldExtendJson;
    if (fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        const result = getWeekMonthQuarterYear(a);
        return result[fieldExtendJson.picker];
      }
    }
    // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
    return a;
  }

  watch(selectedKeys, () => {
    onlineTableContext['selectedRowKeys'] = toRaw(selectedKeys.value);
  });

  onlineTableContext['clearSelectedRow'] = () => {
    selectedKeys.value = [];
    onlineTableContext['selectedRows'] = [];
    onlineTableContext['selectedRowKeys'] = [];
  };

  /**
   * Danh sách xem trước cell Hình ảnh
   * @param text
   */
  function viewOnlineCellImage(text) {
    if (text) {
      let imgList: any = [];
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990】Tham số hình ảnh chứa dấu phẩy sẽ bị nhận diện sai thành nhiều hình
      const arr = split(text);
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990】Tham số hình ảnh chứa dấu phẩy sẽ bị nhận diện sai thành nhiều hình
      for (let str of arr) {
        if (str) {
          imgList.push(getFileAccessHttpUrl(str));
        }
      }
      createImgPreview({ imageList: imgList });
    }
  }

  /**
   * link tableControl hiển thị trên danh sách Hỗ trợ nhấp để chuyển tới biểu mẫu
   * @param id
   * @param hrefTableName
   */
  const onlinePopModalRef = ref();
  async function handleClickLinkTable(id, hrefTableName, isListReadOnly, fieldName?){
    popTableId.value = hrefTableName;
    const formStatus = await onlinePopModalRef.value.getFormStatus();
    // Khi cấu hình danh sách là chỉ đọc hoặc biểu mẫu hiện tại không hỗ trợ chỉnh sửa, thống nhất chuyển tới biểu mẫu chi tiết chỉ đọc
    if(formStatus==true || isListReadOnly){
      hrefMainTableId.value = hrefTableName;
      openOnlineHrefModal(true, {
        isUpdate: true,
        disableSubmit: true,
        hideSub: true,
        record:{id: id},
      })
    }else{
      openPopModal(true, {
        isUpdate: true,
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        // update-begin--author:cc---date:20260715---for: 【Quyền bản ghi liên kết】Danh sách chỉ đọc=là, hoặc trường đó đối với người dùng hiện tại"Biểu mẫu có thể chỉnh sửa"Khi chưa ủy quyền, popup ở chế độ chỉ đọc, không thể chỉnh sửa
        disableSubmit: (isListReadOnly || formDisabledColumns.value.includes(fieldName)) ? true : false,
        // update-end--author:cc---date:20260715---for: 【Quyền bản ghi liên kết】Danh sách chỉ đọc=là, hoặc trường đó đối với người dùng hiện tại"Biểu mẫu có thể chỉnh sửa"Khi chưa ủy quyền, popup ở chế độ chỉ đọc, không thể chỉnh sửa
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        record: {
          id: id
        }
      });
    }
  }
  
  return {
    columns,
    actionColumn,
    selectedKeys,
    rowSelection,
    enableScrollBar,
    tableScroll,
    downloadRowFile,
    getImgView,
    getPcaText,
    getFormatDate,
    handleColumnResult,
    onSelectChange,
    hrefComponent,
    viewOnlineCellImage,
    hrefMainTableId,
    registerOnlineHrefModal,
    registerPopModal,
    openPopModal,
    openOnlineHrefModal,
    onlinePopModalRef,
    popTableId,
    handleClickFieldHref,
  };
}
