/**
 * jsTăng cường
 */
import { reactive } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { _eval } from '/@/utils';
import { useMessage } from '/@/hooks/web/useMessage';

export function useEnhance(onlineTableContext, isList = true) {
  let EnhanceJS = reactive<any>({});

  const getAction = (url, params) => {
    return defHttp.get({ url: url, params }, { isTransformResponse: false });
  };

  const postAction = (url, params) => {
    return defHttp.post({ url: url, params }, { isTransformResponse: false });
  };

  const putAction = (url, params) => {
    return defHttp.put({ url: url, params }, { isTransformResponse: false });
  };

  const deleteAction = (url, params) => {
    return defHttp.delete({ url: url, params }, { isTransformResponse: false });
  };

  if (isList === true) {
    onlineTableContext['_getAction'] = getAction;
    onlineTableContext['_postAction'] = postAction;
    onlineTableContext['_putAction'] = putAction;
    onlineTableContext['_deleteAction'] = deleteAction;
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】jsPhần tăng cường cung cấpuseMessagePhương thức
    onlineTableContext['_useMessage'] = useMessage;
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】jsPhần tăng cường cung cấpuseMessagePhương thức
  } else {
    onlineTableContext.addObject2Context('_getAction', getAction);
    onlineTableContext.addObject2Context('_postAction', postAction);
    onlineTableContext.addObject2Context('_putAction', putAction);
    onlineTableContext.addObject2Context('_deleteAction', deleteAction);
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】jsPhần tăng cường cung cấpuseMessagePhương thức
    onlineTableContext.addObject2Context('_useMessage', useMessage);
    // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8342】jsPhần tăng cường cung cấpuseMessagePhương thức
  }

  /**
   * Khởi tạo
   * @param str (res.result.enhanceJs)
   */
  function initCgEnhanceJs(str: string) {
    //console.log("--onlineList-jsTăng cường"+isList,str)
    if (str) {
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-338】jsMã tăng cường báo lỗi không được ảnh hưởng đến việc render trang
      let Obj: any;
      let result;
      try {
        // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
        Obj = _eval(str);
        // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
        result = new Obj(getAction, postAction, deleteAction);
        //return new Function(str)(getAction,postAction,deleteAction);
      } catch (error) {
        result = {};
        const { createMessage } = useMessage();
        createMessage.warning(`jsMã tăng cường có lỗi cú pháp, vui lòng kiểm tra mã~ ${error}`);
      }
      return result;
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-338】jsMã tăng cường báo lỗi không được ảnh hưởng đến việc render trang
    } else {
      return {};
    }
  }

  /**
   * 【】
   * Kích hoạtjsPhương thức tăng cường
   * @param that
   * @param formData
   */
  function triggerJsFun(that, buttonCode) {
    if (EnhanceJS && EnhanceJS[buttonCode]) {
      EnhanceJS[buttonCode](that);
    }
  }

  /**
   * 【Biểu mẫu】
   * Xử lýjsTăng cường Tùy chỉnh Sự kiện trước khi gửi
   * @param that
   * @param formData
   */
  function customBeforeSubmit(that, formData) {
    if (EnhanceJS && EnhanceJS['beforeSubmit']) {
      return EnhanceJS['beforeSubmit'](that, formData);
    } else {
      return Promise.resolve();
    }
  }

  /**
   * Xử lý nghiệp vụ trước khi xóa
   * @param that
   * @param record
   */
  function beforeDelete(that, record) {
    if (EnhanceJS && EnhanceJS['beforeDelete']) {
      return EnhanceJS['beforeDelete'](that, record);
    } else {
      return Promise.resolve();
    }
  }

  if (isList === true) {
    if (onlineTableContext) {
      onlineTableContext['beforeDelete'] = (record) => {
        const onlEnhanceJS = onlineTableContext['EnhanceJS'];
        if (onlEnhanceJS && onlEnhanceJS['beforeDelete']) {
          return onlEnhanceJS['beforeDelete'](onlineTableContext, record);
        } else {
          return Promise.resolve();
        }
      };

      onlineTableContext['beforeEdit'] = (record) => {
        const onlEnhanceJS = onlineTableContext['EnhanceJS'];
        if (onlEnhanceJS && onlEnhanceJS['beforeEdit']) {
          return onlEnhanceJS['beforeEdit'](onlineTableContext, record);
        } else {
          return Promise.resolve();
        }
      };
    }
  }

  return {
    EnhanceJS,
    initCgEnhanceJs,
    customBeforeSubmit,
    beforeDelete,
    triggerJsFun,
  };
}
