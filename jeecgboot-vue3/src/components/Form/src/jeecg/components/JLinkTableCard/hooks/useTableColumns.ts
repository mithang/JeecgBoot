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
 * Lấy cấu hình column mà danh sách thực tế cần
 * @param onlineTableContext Dữ liệu truy vấn được từ cơ sở dữ liệu
 * @param extConfigJson Cấu hình mở rộngJSON
 */
export function useTableColumns(onlineTableContext, extConfigJson: Ref<ExtConfigType | undefined>) {
  // Lấy đối tượng router, dùng cho chuyển hướng href
  let router = useRouter();

  // Thông tin cột
  const columns = ref<Array<OnlineColumn>>([]);
  /**
   * 20260309
   * liaozhiyang
   * 【issues/9336] không kéo được độ rộng cột
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

  // Có hay khôngbpm_status
  //const hasBpmStatus = ref<boolean>(false)
  // Thông tin từ điển
  const dictOptionInfo = ref<any>({});
  //Giá trị đã chọn
  const selectedKeys = ref<any[]>([]);
  //Bản ghi hàng đã chọn
  //const selectRows = ref<Array<any>>([]);
  // Cấu hình cột lựa chọn --computedcó vấn đề
  const rowSelection = ref<any>(null);
  // Có thanh cuộn không
  let enableScrollBar = ref(true);
  // tableThuộc tínhscroll
  let tableScroll = computed(() => {
    if (enableScrollBar.value == true) {
      return undefined;
    } else {
      // Xtrục không có thanh cuộn
      return { x: false };
    }
  });

  //Dùng cho sự kiện nhấn popup của một cột trong danh sách online-Popup hiển thị biểu mẫu khác
  const [registerOnlineHrefModal, { openModal: openOnlineHrefModal }] = useModal();
  const hrefMainTableId = ref('')
  // Dùng trong biểu mẫu online để bật biểu mẫu khác
  const [registerPopModal, { openModal: openPopModal }] = useModal();
  const popTableId = ref('')

  // Phương thức xử lý kết quả yêu cầu thông tin cột truy vấn
  function handleColumnResult(result, type = 'checkbox') {
    // Cài đặt từ điển
    dictOptionInfo.value = result.dictOptions;
    // rowSelectionCài đặt
    if (result.checkboxFlag == 'Y') {
      rowSelection.value = {
        selectedRowKeys: selectedKeys,
        onChange: onSelectChange,
        type,
      };
    } else {
      rowSelection.value = null;
    }
    // Có cho phép thanh cuộn không
    enableScrollBar.value = result.scrollFlag == 1;

    let dataColumns = result.columns;
    dataColumns.forEach((column) => {
      if (extConfigJson?.value?.canResizeColumn === 1) {
        // update-begin--author:liaozhiyang---date:20260309---for:【issues/9336] không kéo được độ rộng cột
        applyResizableColumns([column]);
        // update-end--author:liaozhiyang---date:20260309---for:【issues/9336] không kéo được độ rộng cột
      }
      
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-4161] cột hỗ trợ chức năng cố định
      if (column.fieldExtendJson) {
        const json = JSON.parse(column.fieldExtendJson);
        if (!!json.isFixed) {
          column.fixed = 'left';
        }
      }
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-4161] cột hỗ trợ chức năng cố định
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-129] Thêm cấu hình href chuyển hướng cho control rich text
      if (column.hrefSlotName && column.scopedSlots) {
        const obj = result.fieldHrefSlots?.find((item) => item.slotName === column.hrefSlotName);
        if (obj) {
          column.fieldHref = obj;
        }
      }
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-129] Thêm cấu hình href chuyển hướng cho control rich text
      Object.keys(column).map((key) => {
        // Xóa trường có giá trị trống (không xóa chuỗi trống('') hoặc số 0)
        if (column[key] == null) {
          delete column[key];
        }
      });
    });

    // href Chuyển hướng
    let fieldHrefSlots: HrefSlots[] = result.fieldHrefSlots;
    const fieldHrefSlotKeysMap = {};
    fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));

    let tableColumns: OnlineColumn[] = [];
    // Xử lý chuyển hướng href và từ điển dict trong cột, để cả hai có thể tương thích cùng tồn tại
    tableColumns = handleColumnHrefAndDict(dataColumns, fieldHrefSlotKeysMap);
    // Có hay không bpm_statusTrường, nếu có, nút thao tác danh sách cần thêm nút gửi quy trình
    bpmStatusFilter(tableColumns);

    console.log('-----Cấu hình cột danh sách----', tableColumns);
    // Nếu là danh sách cây thì cần thiết lập trường cột đầu tiên và cột đầu tiênalign
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
        //Nếu là 0 hoặc-1Không cần xử lý
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
   * Xử lý href và bản dịch từ điển của cột
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // online Trong báo cáo, loại được cấu hình là ngày (yyyy-MM-dd ), nhưng thực tế hiển thị theo định dạng ngày giờ(yyyy-MM-dd HH:mm:ss) issues/3042
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
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930] Trong danh sách bảng hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        const fieldExtendJson = column.fieldExtendJson ?? '{}';
        const json = JSON.parse(fieldExtendJson);
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930] Trong danh sách bảng hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        column.customRender = ({ text, record }) => {
          if (!text) {
            return '';
          }
          if(onlineTableContext.isPopList===true){
            // Nếu là danh sách của popup, cột bản ghi liên kết chỉ hỗ trợ dịch dữ liệu, không cần logic chuyển hướng
            return record[column.dataIndex+"_dictText"]
          }else{
            let tempIdArray = (text+'').split(',');
            //update-begin-author:taoyan date:2023-2-15 for: QQYUN-4286[Biểu mẫu online] Bảng chính-bảng con bật truy vấn liên hợp, kiểm thử chức năng báo lỗi không mở được
            let tempLabelArray = [];
            if(record[column.dataIndex+"_dictText"]){
              tempLabelArray = record[column.dataIndex+"_dictText"].split(',');
            }
            //update-end-author:taoyan date:2023-2-15 for: QQYUN-4286[Biểu mẫu online] Bảng chính-bảng con bật truy vấn liên hợp, kiểm thử chức năng báo lỗi không mở được
            let renderResult:any = []
            for(let i=0;i<tempIdArray.length;i++){
              let renderObj = h(
                LinkTableListPiece,
                {
                  id: tempIdArray[i],
                  text: tempLabelArray[i],
                  onTab:(id)=>handleClickLinkTable(id, hrefSlotName, json.isListReadOnly)
                }
              );
              renderResult.push(renderObj)
            }
            if(renderResult.length==0){
              return ''
            }
            //Nếu cần hiển thị đầy đủ nhưng sẽ xuống dòng:display: flex;width: 100%;flex-wrap: wrap;flex-direction: row;
            return h('div',{style:{'overflow':'hidden'}}, renderResult);
          }
        };
      } else if (fieldType === 'popup_dict') {
        // update-begin--author:liaozhiyang---date:20240402---for：【QQYUN-8833] Dịch danh sách của JPopupDict
        column.customRender = ({ text, record }) => {
          const dict = record[column.dataIndex + '_dictText'];
          if (dict != undefined) {
            return record[column.dataIndex + '_dictText'];
          }
          return text;
        };
        // update-end--author:liaozhiyang---date:20240402---for：【QQYUN-8833] Dịch danh sách của JPopupDict
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
          // Cột của hàm render tùy chỉnh cần cấu hình thủ côngellipsis
          column.ellipsis = true;
          column.customRender = ({ text, record }) => {
            let value = text;
            const valueSpan: any[] = [];
            const getValue = () => valueSpan.length ? valueSpan : value;
            // Nếu dictCode có giá trị thì thực hiện chuyển đổi danh mục
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
                  getValue(),
                );
              }
            }
            return h('span', {}, getValue());
          };
        }

        //  Phiên bản cũ gọi là scopedSlots, phiên bản mới gọi làslots
        if (column.scopedSlots) {
          // slotCột của slot cần cấu hình thủ côngellipsis
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
              // Hỗ trợ {{ ACCESS_TOKEN }} Placeholder
              if (s0.trim() === 'ACCESS_TOKEN') {
                return getToken()
              }

              // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390]Thay eval bằng new Function, khắc phục cảnh báo build
              return _eval(s0);
              // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390]Thay eval bằng new Function, khắc phục cảnh báo build
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

  // Kiểu
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366]Nâng cấp lênantd4.x
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
      // Ẩn nút Hủy
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.model.open = false),
      cancel: () => (hrefComponent.model.open = false),
    },
    is: <any>null,
    params: {},
  });
  // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366]Nâng cấp lênantd4.x

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
      hrefComponent.params = params;
    } else {
      hrefComponent.params = {};
    }
    // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366]Nâng cấp lênantd4.x
    hrefComponent.model.open = true;
    // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366]Nâng cấp lênantd4.x
    hrefComponent.model.title = 'Thao tác';
    hrefComponent.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  //Nếu là danh sách cây, cột thao tác chỉ có thể cố định bên phải
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
  // update-begin--author:liaozhiyang---date:20260309---for:【issues/9336] không kéo được độ rộng cột
  if (extConfigJson?.value?.canResizeColumn === 1 && columns.value.length > 0) {
    applyResizableColumns(columns.value);
    onlineTableContext.reloadTable();
  }
  // update-end--author:liaozhiyang---date:20260309---for:【issues/9336] không kéo được độ rộng cột
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
    // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-8020]Biểu mẫu online có nhiều tệp thì đi qua API tải xuống
    if (text.indexOf(',') > 0) {
      downloadFile(`/online/cgform/field/download/${id}/${record.id}/${column.dataIndex}`, `Tệp_${record.id}.zip`);
    } else {
      const url = getFileAccessHttpUrl(text);
      window.open(url);
    }
    // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-8020]Biểu mẫu online có nhiều tệp thì đi qua API tải xuống
  }

  /**
   * Hình ảnh
   * @param text
   */
  function getImgView(text) {
    if (text && text.indexOf(',') > 0) {
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990]Tham số hình ảnh chứa dấu phẩy sẽ bị nhận nhầm thành nhiều ảnh
      text = split(text)[0];
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990]Tham số hình ảnh chứa dấu phẩy sẽ bị nhận nhầm thành nhiều ảnh
    }
    return getFileAccessHttpUrl(text);
  }

  /**
   * Lấy văn bản tỉnh/thành phố/quận huyện theo mã
   * @param code
   */
  function getPcaText(code, column) {
    if (!code) {
      return '';
    }
    // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
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
    // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
  }

  /**
   * Định dạng ngày
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
    // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094] ngày online(Năm tháng ngày)Control thêm các định dạng năm, năm-tháng, tuần của năm, quý của năm, v.v.
    let fieldExtendJson = column?.fieldExtendJson;
    if (fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        const result = getWeekMonthQuarterYear(a);
        return result[fieldExtendJson.picker];
      }
    }
    // update-end--author:liaozhiyang---date:20240430---for：【issues/6094] ngày online(Năm tháng ngày)Control thêm các định dạng năm, năm-tháng, tuần của năm, quý của năm, v.v.
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
   * Xem trước hình ảnh cell trong danh sách
   * @param text
   */
  function viewOnlineCellImage(text) {
    if (text) {
      let imgList: any = [];
      // update-begin--author:liaozhiyang---date:20250325---for：【issues/7990]Tham số hình ảnh chứa dấu phẩy sẽ bị nhận nhầm thành nhiều ảnh
      const arr = split(text);
      // update-end--author:liaozhiyang---date:20250325---for：【issues/7990]Tham số hình ảnh chứa dấu phẩy sẽ bị nhận nhầm thành nhiều ảnh
      for (let str of arr) {
        if (str) {
          imgList.push(getFileAccessHttpUrl(str));
        }
      }
      createImgPreview({ imageList: imgList });
    }
  }

  /**
   * link tableControl hiển thị trên danh sách, hỗ trợ bấm để chuyển đến biểu mẫu
   * @param id
   * @param hrefTableName
   */
  const onlinePopModalRef = ref();
  async function handleClickLinkTable(id, hrefTableName, isListReadOnly){
    popTableId.value = hrefTableName;
    let formStatus =  await onlinePopModalRef.value.getFormStatus();
    // Kiểm tra xem biểu mẫu hiện tại có hỗ trợ chỉnh sửa hay không, nếu không thể chỉnh sửa thì chuyển sang biểu mẫu chi tiết
    if(formStatus==true){
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
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930] Trong danh sách bảng hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        disableSubmit: isListReadOnly ? true : false,
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930] Trong danh sách bảng hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
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
