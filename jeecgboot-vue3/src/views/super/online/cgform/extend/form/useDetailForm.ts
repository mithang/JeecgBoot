import { FormSchema, RenderCallbackParams } from '/@/components/Form';
import { computed, ref, watch } from 'vue';
import { getDictItemsByCode } from '/@/utils/dict';
import { filterMultiDictText, filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { initDictOptions } from '/@/utils/dict/index';
import { loadDictItem, queryDepartTreeSync, getUserList } from '/@/api/common/api';
import { defHttp } from '/@/utils/http/axios';
import { getAreaTextByCodeAnyLevel } from '/@/components/Form/src/utils/Area';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { createImgPreview } from '/@/components/Preview/index';
import { useMessage } from '/@/hooks/web/useMessage';

/***
 * Kết quả phân tích cấu hình mở rộng của trường biểu mẫu
 */
interface FieldExtends {
  //Số lượng tải lên
  uploadnum?: number | string;

  //Giới hạn độ dài trưng bày của văn bản lớn ở trang danh sách
  showLength?: number | string;

  //popupCó hỗ trợ chọn nhiều hay không
  popupMulti?: boolean;

  //Component phòng ban, người dùng Tên trường dùng để lưu trữ
  store?: string;

  //Component phòng ban, người dùng Tên trường dùng để hiển thị
  text?: string;

  //Component phòng ban, người dùng Có chọn nhiều hay không
  multiSelect?: boolean;

  //Quy tắc sắp xếp truy vấn
  orderRule?: 'asc' | 'desc';
  //Loại hiển thị bản ghi liên kết
  showType?:string;
}

export interface DetailFormSchema {
  field: string;
  label: string;
  span?: number;
  view?: string;
  isHtml?: boolean;
  isImage?: boolean;
  isFile?: boolean;
  isCard?: boolean;
  // update-begin--author:liaozhiyang---date:20260811---for:【LHZP-4】onlineBản ghi liên kết của chi tiết thêm hiển thị hình ảnh, tải tệp xuống và xem trước hình ảnh
  imageField?: string;
  // update-end--author:liaozhiyang---date:20260811---for:【LHZP-4】onlineBản ghi liên kết của chi tiết thêm hiển thị hình ảnh, tải tệp xuống và xem trước hình ảnh
  multi?:boolean;
  order?: any;
  dictTable?: string;
  dictText?: string;
  dictCode?: string;
  dict?: string;
  // Có phải trường nút cha của bảng cây hay không 1là 0Không
  pidComponent?: number;
  // Cha của nút gốc câyIDgiá trị, mặc định0
  pidValue?: string;
  fieldExtendJson?: string;
  ifShow?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);
  // update-begin--author:liaozhiyang---date:20240425---for：【issues/6139】onlineChi tiết hỗ trợjsTăng cườngloadedSự kiện cùng các chức năng đặt giá trị, lấy giá trị, ẩn
  // jsẨn phần tăng cường
  hidden?: boolean;
  // update-end--author:liaozhiyang---date:20240425---for：【issues/6139】onlineChi tiết hỗ trợjsTăng cườngloadedSự kiện cùng các chức năng đặt giá trị, lấy giá trị, ẩn
}

/*interface DetailFormProps {
  span?: number;
  schemas?: DetailFormSchema[];
  data?: any;
  containerClass?: string;
}*/

export function useDetailForm(props: any) {
  console.log(props);
  const dictOptionsMap = {};
  const currentLinkFields: string[] = [];
  const detailFormData = ref({});
  const { createMessage } = useMessage();

  const formContainerClass = computed(() => {
    if (props.containerClass) {
      return `jeecg-detail-form ${props.containerClass}`;
    } else {
      return 'jeecg-detail-form';
    }
  });

  watch(
    () => props.data,
    async (formData) => {
      if (formData) {
        let arr = props.schemas;
        let temp = {};
        if (arr && arr.length > 0) {
          for (let item of arr) {
            let field = item.field;
            try {
              temp[field] = await getItemContent(item);
            } catch (e) {
              console.error('trường 【' + field + '】Lấy văn bản thất bại', e);
            }
          }
        }
        detailFormData.value = temp;
      }
    },
    { deep: true, immediate: true }
  );

  async function getItemContent(item) {
    let formData = props.data;
    if (formData) {
      let value = formData[item.field];
      if (!value && value !== '0' && value !== 0) {
        return '';
      }
      let str = value;
      let view = item.view;
      if (view == 'list' || view == 'radio' || view == 'checkbox' || view == 'list_multi') {
        str = await getSelectText(item, formData);
      } else if (view == 'sel_search') {
        str = await getTableDataText(item, formData);
      } else if (view == 'cat_tree') {
        //Cây từ điển phân loại
        str = await getCategoryDataText(item, formData);
      } else if (view == 'link_table') {
        str = await getLinkTableData(item, formData);
      } else if (view == 'sel_depart') {
        //Chọn phòng ban
        str = await getDepartDataText(item, formData);
      } else if (view == 'sel_user') {
        // Lựa chọn của người dùng
        str = await getUserDataText(item, formData);
      } else if (view == 'pca') {
        //Tỉnh/Thành/Quận huyện
        // update-begin--author:liaozhiyang---date:20260227---for:【QQYUN-14788】onlineTỉnh/thành riêng lẻ trong chi tiết không hiển thị
        let includeParent = true;
        let fieldExtendJson = item?.fieldExtendJson;
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
        str = getAreaTextByCodeAnyLevel(value, includeParent, level as 1 | 2 | 3);
        // update-end--author:liaozhiyang---date:20260227---for:【QQYUN-14788】onlineTỉnh/thành riêng lẻ trong chi tiết không hiển thị
      } else if (view == 'link_down') {
        //Component liên động
        str = await getLinkDownDataText(item, formData);
      } else if (view == 'sel_tree') {
        //Control cây tùy chỉnh
        str = await getTreeDataText(item, formData);
      } else if (view == 'switch') {
        //Component công tắc
        str = await getSwitchDataText(item, formData);
      } else if (view == 'image' || view == 'file') {
        str = getFileList(item, formData);
      } else if (view == 'popup_dict') {
        // update-begin--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictdịch danh sách của
        const ditc = formData[`${item.field}_dictText`];
        if (ditc !== undefined) {
          str = ditc;
        }
        // update-end--author:liaozhiyang---date:20240402---for：【QQYUN-8833】JPopupDictdịch danh sách của
      } else {
        if (currentLinkFields.indexOf(item.field) >= 0) {
          let arr = dictOptionsMap[item.field];
          if (arr && arr.length > 0) {
            str = filterMultiDictText(arr, value);
          }
        }
      }
      return str;
    }
    return '';
  }

  // Từ điển dữ liệu/Từ điển bảng
  async function getSelectText(item, formData) {
    // Trước tiên lấy từ bộ nhớ đệm
    let dictCode = getRequestDictCode(item);
    let value = formData[item.field];
    if (!dictCode) {
      return value;
    }
    let options = getDictItemsByCode(dictCode);
    if (options && options.length > 0) {
      return filterMultiDictText(options, value);
    } else {
      let dictRes = [];
      if (dictOptionsMap[dictCode]) {
        dictRes = dictOptionsMap[dictCode];
      } else {
        //Không lấy được thì yêu cầu lại
        dictRes = (await initDictOptions(dictCode)) || [];
      }
      if (dictRes && dictRes.length > 0) {
        dictOptionsMap[dictCode] = dictRes;
        return filterMultiDictText(dictRes, value);
      }
    }
    return '';
  }

  function getRequestDictCode(item) {
    let temp = '';
    let { dictCode, dictTable, dictText } = item;
    if (!dictTable) {
      temp = dictCode;
    } else {
      temp = encodeURI(`${dictTable},${dictText},${dictCode}`);
    }
    return temp;
  }

  // Từ điển bảng-Tìm kiếm trong dropdown
  async function getTableDataText(item, formData) {
    let dictCode = getRequestDictCode(item);
    let value = formData[item.field];
    if (!value) {
      return '';
    }

    let arr: any[] = [];
    // update-begin--author:liaozhiyang---date:20250813---for：【issues/8689】onlineÔ tìm kiếm dropdown: khi xem chi tiết không đọc được từ điển dữ liệu
    // Từ điển hệ thống
    if (dictCode.indexOf(',') === -1) {
      const options = await initDictOptions(dictCode);
      if (options && options.length > 0) {
        options.forEach((item: any) => {
          if (item.value === value) {
            arr.push(item.text || item.label);
          }
        });
      }
    } else {
      // Từ điển bảng
      if (dictOptionsMap[dictCode+value]) {
        arr = dictOptionsMap[dictCode+value];
      } else {
        //Không lấy được thì yêu cầu lại
        arr = (await defHttp.get({ url: `/sys/dict/loadDictItem/${dictCode}`, params: { key: value } })) || [];
      }
    }
    // update-end--author:liaozhiyang---date:20250813---for：【issues/8689】onlineÔ tìm kiếm dropdown: khi xem chi tiết không đọc được từ điển dữ liệu
    if (arr && arr.length > 0) {
      dictOptionsMap[dictCode+value] = arr;
      return arr.join(',')
      //return filterMultiDictText(arr, value);
    }
    return '';
  }

  // Từ điển phân loại
  async function getCategoryDataText(item, formData) {
    let value = formData[item.field];
    if (!value) {
      return '';
    }
    let arr = (await loadDictItem({ ids: value })) || [];
    if (arr && arr.length > 0) {
      return arr.join(',');
    }
    return '';
  }

  // Dữ liệu phòng ban
  async function getDepartDataText(item, formData) {
    let value = formData[item.field];
    if (!value) {
      return '';
    }
    let extend = getExtendConfig(item);
    let storeField = extend.store || 'id';
    let labelKey = extend.text || 'departName';
    let arr = (await queryDepartTreeSync({ ids: value, primaryKey: storeField })) || [];
    if (arr && arr.length > 0) {
      let temp: string[] = [];
      for (let item of arr) {
        if (item[labelKey]) {
          temp.push(item[labelKey]);
        } else {
          temp.push(item.title);
        }
      }
      return temp.join(',');
    }
    return '';
  }

  //Dữ liệu người dùng
  async function getUserDataText(item, formData) {
    let value = formData[item.field];
    if (!value) {
      return '';
    }
    let extend = getExtendConfig(item);
    let storeField = extend.store || 'username';
    let params = {
      [storeField]: value,
    };
    let res = (await getUserList(params)) || {};
    let arr = res.records || [];
    if (arr && arr.length > 0) {
      let temp: string[] = [];
      console.log('getUserDataText', arr);
      let textField = extend.text || 'realname';
      for (let item of arr) {
        temp.push(item[textField]);
      }
      return temp.join(',');
    }
    return '';
  }

  function getExtendConfig(item) {
    let extend: FieldExtends = {};
    let { fieldExtendJson } = item;
    if (fieldExtendJson) {
      if (typeof fieldExtendJson == 'string') {
        try {
          let json = JSON.parse(fieldExtendJson);
          extend = { ...json };
        } catch (e) {
          console.error(e);
        }
      }
    }
    return extend;
  }

  // Component liên động
  async function getLinkDownDataText(item, formData) {
    let { dictTable, field } = item;
    let arr: any[] = [];
    if (dictOptionsMap[field]) {
      arr = dictOptionsMap[field];
    } else {
      if (dictTable) {
        let json = JSON.parse(dictTable);
        if (json) {
          let { table, txt, key, linkField } = json;
          let dictCode = `${table},${txt},${key}`;
          let temp: any[] = (await initDictOptions(dictCode)) || [];
          arr = [...temp];
          if (arr && arr.length > 0) {
            dictOptionsMap[field] = arr;
            if (linkField) {
              let fieldArray = linkField.split(',');
              for (let item of fieldArray) {
                dictOptionsMap[item] = arr;
                currentLinkFields.push(item);
              }
            }
          }
        }
      }
    }
    if (arr && arr.length > 0) {
      let value = formData[field];
      return filterMultiDictText(arr, value);
    }
    return '';
  }

  //Cây tùy chỉnh
  async function getTreeDataText(item, formData) {
    let { dict, field } = item;
    // update-begin--author:liusq---date:20261102---for：onlineHiển thị nút cha của nút cấp cao nhất trong chi tiết bảng cây0
    // Trường nút cha của bảng cây: cha của nút cấp cao nhấtIDlà giá trị nút gốc(Mặc định0), không có nút tương ứng, không hiển thị (với trang chỉnh sửaJTreeSelectnhất quán)
    if (item.pidComponent == 1 && formData[field] == (item.pidValue || '0')) {
      return '';
    }
    // update-end--author:liusq---date:20261102---for：onlineHiển thị nút cha của nút cấp cao nhất trong chi tiết bảng cây0
    let arr = [];
    if (dictOptionsMap[field]) {
      arr = dictOptionsMap[field];
    } else {
      if (dict) {
        arr = await initDictOptions(dict);
      }
    }
    if (arr && arr.length > 0) {
      let value = formData[field];
      return filterMultiDictText(arr, value);
    }
    return '';
  }

  //Công tắc
  async function getSwitchDataText(item, formData) {
    let { fieldExtendJson, field } = item;
    let options = ['Y', 'N'];
    if (fieldExtendJson) {
      //update-begin---author:wangshuai---date:2025-11-03---for:【issues/9036】online Phát triển biểu mẫu, Thiết lập trường Khi loại control là công tắc, lúc xem trang chi tiết Trường công tắc hiển thị giá trị gốc---
      const extendConfig = JSON.parse(fieldExtendJson);
      // update-begin--author:liaozhiyang---date:20260813---for：【LHZP-4】onlineCông tắc dữ liệu cũ trong popup chi tiết không render ra
      if (Array.isArray(extendConfig)) {
        // Tương thích dữ liệu cũ: bản thân mảng chính là tùy chọn công tắc  Dữ liệu hiện có:{switchOptions:[1,2]}   Dữ liệu cũ:[1,2]
        options = extendConfig;
      } else if (Array.isArray(extendConfig?.switchOptions)) {
        options = extendConfig.switchOptions;
      }
      // update-end--author:liaozhiyang---date:20260813---for：【LHZP-4】onlineCông tắc dữ liệu cũ trong popup chi tiết không render ra
      //update-end---author:wangshuai---date:2025-11-03---for:【issues/9036】online Phát triển biểu mẫu, Thiết lập trường Khi loại control là công tắc, lúc xem trang chi tiết Trường công tắc hiển thị giá trị gốc---
    }
    let arr: any[] = [
      { value: options[0], text: 'là' },
      { value: options[1], text: 'Không' },
      { value: options[0]+'', text: 'là' },
      { value: options[1]+'', text: 'Không' },
    ];
    let value = formData[field];
    return filterDictText(arr, value);
  }

  function getItemSpan(item) {
    if (item.span) {
      return item.span;
    }
    return props.span;
  }

  function getFileList(item, formData) {
    let str = formData[item.field];
    if (!str) {
      return [];
    }
    let arr = str.split(',');
    let result: string[] = [];
    for (let item of arr) {
      let src = getFileAccessHttpUrl(item) || '';
      if (src) {
        result.push(src);
      }
    }
    return result;
  }

  function handleDownloadFile(url) {
    if (url) {
      window.open(url);
    }
  }

  function handleViewImage(field) {
    let values = detailFormData.value[field];
    if (!values || values.length == 0) {
      createMessage.warning('Không có hình ảnh!');
      return;
    }
    createImgPreview({ imageList: values });
  }

  function getFilename(url) {
    if (!url) {
      return '';
    }
    return url.substring(url.lastIndexOf('/') + 1);
  }

  /**
   * VUEN-1772【vue3 online Chi tiết 】ai—ai_control_single Component công tắc chưa được dịch, khi trường nằm trên một hàng, không căn trái
   */
  const span24ViewArray = ['file', 'image', 'markdown', 'umeditor'];
  function getLabelWidthClass(item) {
    if(span24ViewArray.indexOf(item.view)>=0){
      if(props.span==12){
        return 'span12';
      }else if(props.span==8){
        return 'span8';
      }else if(props.span==6){
        return 'span6';
      }else{
        return 'span24';
      }
    }
    return ''
  }
  
  // bản ghi liên kết
  async function getLinkTableData(item, formData) {
    let value = formData[item.field];
    let extend = getExtendConfig(item);
    if(extend.showType=='select'){
      if (!value) {
        return '';
      }
      return formData[item.field+'_dictText'];
    }else{
      if (!value) {
        return '';
      }
      return formData[item.field];
    }
    
    let storeField = extend.store || 'id';
    let arr = (await queryDepartTreeSync({ ids: value, primaryKey: storeField })) || [];
    if (arr && arr.length > 0) {
      let temp: string[] = [];
      for (let item of arr) {
        temp.push(item.title);
      }
      return temp.join(',');
    }
    return '';
  }

  return {
    formContainerClass,
    detailFormData,
    getItemSpan,
    handleDownloadFile,
    handleViewImage,
    getFilename,
    getLabelWidthClass
  };
}

/**
 * TODO Chưa được triển khai
 * Lấy DetailFormSchema[Dùng cho phát triển tùy chỉnh]
 */
export function transDetailFormSchemas(formSchemas: FormSchema[]) {
  const detailFormSchemas = ref<DetailFormSchema[]>([]);
  console.log(formSchemas);
  return detailFormSchemas;
}
